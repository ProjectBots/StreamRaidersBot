package run;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Hashtable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Remaper;
import run.captain.Captain;
import run.captain.CaptainBackEnd;
import run.viewer.Viewer;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;
import srlib.store.Store;


/**
 * @author ProjectBots
 *
 * Basic Skeleton of a Profile Class ({@link Viewer}, {@link Captain})
 *
 * @param <R> extended interface of {@link BackEndRunnable}
 * @param <B> extended class of {@link AbstractBackEnd}
 */

public abstract class AbstractProfile<B extends AbstractBackEnd<B>> {

	public final String cid;
	private ProfileType ptype;
	protected B be;
	protected String currentLayer = "(default)";
	protected String currentTimeId = null;
	protected boolean isSwitching = false;
	protected final Slot[] slots;
	protected Hashtable<Short, Hashtable<String, Integer>> rews = new Hashtable<>();
	
	public ProfileType getType() {
		return ptype;
	}
	
	public boolean isSwitching() {
		return isSwitching;
	}
	
	public String getCurrentLayer() {
		return currentLayer;
	}
	
	public int getSlotSize() {
		return slots.length;
	}
	
	public AbstractProfile(String cid, B be, ProfileType ptype, int slotSize) {
		this.be = be;
		this.cid = cid;
		this.ptype = ptype;
		slots = new Slot[slotSize];
		iniSlots();
	}
	
	public Viewer getAsViewer() {
		return this instanceof Viewer ? (Viewer) this : null;
	}
	
	public Captain getAsCaptain() {
		return this instanceof Captain ? (Captain) this : null;
	}
	
	public AbstractProfile<B> getAsProfile() {
		return (AbstractProfile<B>) this;
	}
	
	public B getBackEnd() {
		return be;
	}
	
	
	public static interface CBERunnable {
		public void run(CaptainBackEnd vbe);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractProfile<?>>T switchProfileType() throws Exception {
		//	stopping slots
		isSwitching = true;
		setRunningAll(false);
		while(!hasStopped())
			Thread.sleep(100);
		
		for(int i=0; i<slots.length; i++)
			slots[i] = null;
		
		SRR req = be.getSRR();
		JsonElement status = Json.parseObj(req.switchUserAccountType()).get("status");
		if(!status.isJsonPrimitive() || !status.getAsString().equals("success"))
			return null;
		
		req.reload();
		
		switch(ptype) {
		case CAPTAIN:
			return (T) new Viewer(cid, req);
		case VIEWER:
			return (T) new Captain(cid, req);
		}
		return null;
	}
	
	protected abstract void iniSlots();
	public abstract void updateRews();
	

	/**
	 * ["chests", "bought", "event"]
	 */
	protected static final String[] rew_sources = "chests bought event".split(" ");
	public static String getRewSouceName(short rs) {
		return rew_sources[rs];
	}
	
	public void saveRews() {
		JsonObject astats = Configs.getUObj(cid, ptype == ProfileType.VIEWER ? Configs.statsViewer : Configs.statsCaptain);
		for(short s_ : rews.keySet()) {
			String s = rew_sources[s_];
			JsonObject stats = astats.getAsJsonObject(s);
			Hashtable<String, Integer> rew = rews.get(s_);
			for(String v : rew.keySet()) {
				if(stats.has(v))
					stats.addProperty(v, stats.get(v).getAsInt() + rew.get(v));
				else
					stats.addProperty(v, rew.get(v));
				
				rew.put(v, 0);
			}
		}
	}
	
	public void addRew(B be, String con, String type, int amount) {
		type = Remaper.map(type).replace(Options.get("currentEventCurrency"), Store.eventcurrency.get());
		try {
			Hashtable<String, Integer> r = rews.get((short) ArrayUtils.indexOf(rew_sources, con));
			r.put(type, r.get(type) + amount);
			be.addCurrency(type, amount);
		} catch (NullPointerException e) {
			Logger.printException("AbstractProfile -> addRew: err=failed to add reward, con=" + con + ", type=" + type + ", amount=" + amount, e, Logger.runerr, Logger.error, cid, null, true);
		}
	}
	
	public Hashtable<Short, Hashtable<String, Integer>> getRews() {
		return rews;
	}
	
	public void setRunning(int slot, boolean b) {
		slots[slot].setRunning(b);
	}
	public void setRunningAll(boolean b) {
		for(int i=0; i<slots.length; i++)
			slots[i].setRunning(b);
	}
	public boolean isRunning(int slot) {
		return slots[slot].isRunning;
	}
	public boolean hasStopped() {
		boolean b = false;
		for(int i=0; i<slots.length; i++)
			b |= slots[i].isActivelyRunning;
		return !b;
	}
	public void skip(int slot) {
		slots[slot].skipSleep();
	}
	public void skipAll() {
		for(int i=0; i<slots.length; i++)
			slots[i].skipSleep();
	}
	
	public abstract void updateFrame() throws NoConnectionException, NotAuthorizedException;
	
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
				if(key.equals(currentTimeId))
					break;
				currentLayer = ptimes.get(key).getAsString();
				currentTimeId = key;
				break;
			}
		}
		
		Manager.blis().onProfileUpdateGeneral(cid, ptype,
				Configs.getPStr(cid, Configs.pname),
				Configs.getStr(cid, currentLayer, ptype == ProfileType.VIEWER ? Configs.lnameViewer : Configs.lnameCaptain),
				new Color(Configs.getInt(cid, currentLayer, ptype == ProfileType.VIEWER ? Configs.colorViewer : Configs.colorCaptain)));
	}
	
	
	protected boolean ready = false;
	protected void setReady(boolean b) {
		ready = b;
	}
	
	private Object slotLock = new Object();
	public Object getSlotLock() {
		return slotLock;
	}
	
	public void updateSlotSync() {
		boolean[] wasRunning = new boolean[slots.length];
		for(int s=0; s<slots.length; s++)
			wasRunning[s] = slots[s].isRunning;
		
		for(int s=0; s<slots.length; s++) {
			if(!slots[s].canManageItself())
				continue;
			
			final int sync = Configs.getSleepInt(cid, currentLayer, ""+s, ptype == ProfileType.VIEWER ? Configs.syncSlotViewer : Configs.syncSlotCaptain);

			Manager.blis().onProfileUpdateSlotSync(cid, ptype, s, sync);
			
			int before = slots[s].getSync();
			if(before == sync)
				continue;

			slots[s].sync(sync);
			
			if(sync == -1) {
				//	starts the slot if the slot it was synced to was running
				setRunning(s, wasRunning[before]);
			} else {
				setRunning(s, false);
				if(wasRunning[s]) {
					final int ss = s;
					new Thread(() -> {
						//	wait for the slot to finish before starting the other
						//	to prevent concuerent actions on the same slot
						while(slots[ss].isActivelyRunning()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}
						}
						setRunning(sync, true);
					}).start();
				}
			}
		}
	}

}
