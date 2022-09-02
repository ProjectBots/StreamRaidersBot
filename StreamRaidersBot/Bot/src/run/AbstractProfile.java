package run;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.NEF;
import include.Http.NoConnectionException;
import program.Configs;
import program.SRR;
import program.SRR.NotAuthorizedException;
import run.AbstractBackEnd.UpdateEventListener;


/**
 * @author ProjectBots
 *
 * Basic Skeleton of a Profile Class ({@link Viewer}, {@link Captain})
 *
 * @param <R> extended interface of {@link BackEndRunnable}
 * @param <B> extended class of {@link AbstractBackEnd}
 */

public abstract class AbstractProfile<R extends AbstractProfile.BackEndRunnable<B>, B extends AbstractBackEnd<B>> {

	public static interface BackEndRunnable<B extends AbstractBackEnd<B>> {
		public void run(B vbe) throws Exception;
	}

	public final String cid;
	private B be_;
	private ProfileType ptype;
	UpdateEventListener<B> uelis = null;
	String currentLayer = "(default)";
	String currentLayerId = null;
	
	public ProfileType getType() {
		return ptype;
	}
	

	public String getCurrentLayer() {
		return currentLayer;
	}
	
	public AbstractProfile(String cid, B be, ProfileType ptype) {
		this.be_ = be;
		this.cid = cid;
		this.ptype = ptype;
		unloadBE();
	}
	
	public Viewer getAsViewer() {
		return this instanceof Viewer ? (Viewer) this : null;
	}
	
	public Captain getAsCaptain() {
		return this instanceof Captain ? (Captain) this : null;
	}
	
	public AbstractProfile<R, B> getAsProfile() {
		return (AbstractProfile<R, B>) this;
	}
	
	
	private final Object be_lock = new Object();
	private int currentBEUses = 1;
	
	public static interface CBERunnable {
		public void run(CaptainBackEnd vbe);
	}
	
	@SuppressWarnings("unchecked")
	private void loadBE() {
		synchronized(be_lock) {
			currentBEUses++;
			if(be_ == null) {
				try {
					be_ = (B) Json.toObj(Json.parseObj(NEF.read("data/temp/"+cid+".srb.json")), be_.getClass());
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(uelis != null) {
					be_.setUpdateEventListener(uelis);
				}
				System.gc();
			}
		}
	}
	
	private void unloadBE() {
		synchronized(be_lock) {
			currentBEUses--;
			if(currentBEUses == 0 && Configs.getGBoo(Configs.freeUpMemoryByUsingDrive)) {
				try {
					NEF.save("data/temp/"+cid+".srb.json", Json.fromObj(be_).toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				be_ = null;
				System.gc();
			}
		}
	}
	
	public void useBackEnd(R ber) throws Exception {
		loadBE();
		ber.run(be_);
		unloadBE();
	};
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractProfile<?,?>>T switchProfileType() throws Exception {
		loadBE();
		try {
			SRR req = be_.getSRR();
			JsonElement status = Json.parseObj(req.switchUserAccountType()).get("status");
			if(!status.isJsonPrimitive() && status.getAsString().equals("success"))
				return null;
			switch(ptype) {
			case CAPTAIN:
				return (T) new Viewer(cid, req);
			case VIEWER:
				return (T) new Captain(cid, req);
			}
			return null;
		} finally {
			unloadBE();
		}
	}
	
	public abstract void saveStats();
	public abstract void setRunning(boolean b, int slot);
	public abstract void setRunningAll(boolean b);
	public abstract boolean isRunning(int slot);
	public abstract boolean hasStopped();
	public abstract void skip(int slot);
	public abstract void skipAll();
	
	public abstract void updateFrame(B be) throws NoConnectionException, NotAuthorizedException;
	public synchronized void updateLayer() {
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units (1 = 5 min)
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288)
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);

		// set current layer
		JsonObject ptimes = Configs.getUObj(cid, ptype == ProfileType.VIEWER ? Configs.ptimesViewer : Configs.ptimesCaptain);
		for(String key : ptimes.keySet()) {
			String[] sp = key.split("-");
			if(Integer.parseInt(sp[0]) <= n && Integer.parseInt(sp[1]) >= n) {
				if(key.equals(currentLayerId))
					break;
				currentLayer = ptimes.get(key).getAsString();
				currentLayerId = key;
				break;
			}
		}
		
		Manager.blis.onProfileUpdateGeneral(cid,
				Configs.getPStr(cid, Configs.pname),
				Configs.getStr(cid, currentLayer, ptype == ProfileType.VIEWER ? Configs.lnameViewer : Configs.lnameCaptain),
				new Color(Configs.getInt(cid, currentLayer, ptype == ProfileType.VIEWER ? Configs.colorViewer : Configs.colorCaptain)));
	}
	
	
	boolean ready = false;
	public void setReady(boolean b) {
		ready = b;
	}
	
	public abstract void updateSlotSync();
	
}
