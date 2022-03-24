package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import program.ConfigsV2;
import program.Debug;
import program.Options;
import program.Remaper;
import program.SRC;
import program.SRR.NotAuthorizedException;

public class Manager {
	
	private static Hashtable<String, Viewer> viewers = new Hashtable<>();
	private static Hashtable<String, Captain> captains = new Hashtable<>();
	private static Hashtable<String, Integer> poss = new Hashtable<>();
	
	/**
	 * @param cid profile id
	 * @return the Viwer Object of the profile or null if not loaded
	 */
	public static Viewer getViewer(String cid) {
		return viewers.get(cid);
	}
	
	/**
	 * @param cid profile id
	 * @return the Captain Object of the profile or null if not loaded
	 */
	public static Captain getCaptain(String cid) {
		return captains.get(cid);
	}
	
	/**
	 * @param cid profile id
	 * @return a instance unique number assigned to the profile starting from 0 and counting up. Can change when restarted, but keeps the order in which the profiles where added.
	 */
	public static int getProfilePos(String cid) {
		return poss.get(cid);
	}
	
	protected static BotListener blis;
	
	/**
	 * changes the current BotListener
	 * @param blis
	 */
	public static void setBotListener(BotListener blis) {
		Manager.blis = blis;
	}
	
	public static class IniCanceledException extends Exception {
		private static final long serialVersionUID = 1L;
		private IniCanceledException(String reason) {
			super(reason);
		}
	}
	
	/**
	 * Initializes the BotManager
	 * @param blis
	 * @throws IniCanceledException
	 */
	public static void ini(BotListener blis) throws IniCanceledException {
		Manager.blis = blis;
		
		System.out.println("\r\n"
				+ "\u0009███████╗██████╗     ██████╗  ██████╗ ████████╗\r\n"
				+ "\u0009██╔════╝██╔══██╗    ██╔══██╗██╔═══██╗╚══██╔══╝\r\n"
				+ "\u0009███████╗██████╔╝    ██████╔╝██║   ██║   ██║   \r\n"
				+ "\u0009╚════██║██╔══██╗    ██╔══██╗██║   ██║   ██║   \r\n"
				+ "\u0009███████║██║  ██║    ██████╔╝╚██████╔╝   ██║   \r\n"
				+ "\u0009╚══════╝╚═╝  ╚═╝    ╚═════╝  ╚═════╝    ╚═╝   \r\n"
				+ "\r\n");
		
		try {
			Options.load();
		} catch (IOException | NullPointerException fnf) {
			throw new IniCanceledException("Couldnt load options");
		}
		
		System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaiderBot\r\n"
				+ "Version: " + Options.get("botVersion") + "\r\n");
		
		
		Remaper.load();
		
		ViewerBackEnd.setDataPathEventListener(new run.ViewerBackEnd.DataPathEventListener() {
			@Override
			public void onUpdate(String dataPath, String serverTime, JsonObject data) {
				run.ViewerBackEnd.DataPathEventListener.super.onUpdate(dataPath, serverTime, data);
				Manager.blis.onDataPathUpdate(dataPath, serverTime, data);
			}
		});
		try {
			ConfigsV2.load();
		} catch (IOException e) {
			Debug.printException("err=failed to load config", e, Debug.runerr, Debug.error, null, null, true);
			if(Manager.blis.configNotReadable()) {
				try {
					ConfigsV2.load(true);
				} catch (IOException e1) {
					Debug.printException("err=failed to reset config", e, Debug.runerr, Debug.error, null, null, true);
					throw new IniCanceledException("config not resetable");
				}
			} else {
				throw new IniCanceledException("config not readable");
			}
		}
		setMaxConcurrentActions(ConfigsV2.getGInt(ConfigsV2.maxProfileActions));
		setClockRunning(true);
		int i=0;
		for(String cid : ConfigsV2.getCids())
			poss.put(cid, i++);
		poss.put("(next)", i);
		
	}
	
	
	/**
	 * adds and loads a new profile
	 * @param name profile name
	 * @param access_info cookie which is used to log into the account
	 * @return the profile id assigned
	 */
	public static String addProfile(String name, String access_info) {
		String cid = ConfigsV2.add(name, access_info);
		ConfigsV2.saveb();
		loadProfile(cid);
		blis.onProfileAdded(cid);
		return cid;
	}
	
	/**
	 * unloads and deletes the profile
	 * @param cid profile id
	 */
	public static void remProfile(String cid) {
		if(viewers.containsKey(cid))
			unloadProfile(cid);
		ConfigsV2.remProfile(cid);
		ConfigsV2.saveb();
		poss.remove(cid);
		blis.onProfileRemoved(cid);
		failedProfiles.remove(cid);
	}
	
	private static HashSet<String> loadedProfiles = new HashSet<>();
	private static HashSet<String> failedProfiles = new HashSet<>();
	private static final Object config_load_status_update_sync_lock = new Object();
	
	/**
	 * checks the config for new profiles and loads them
	 */
	public static void loadAllNewProfiles() {
		List<String> cids = ConfigsV2.getCids();
		cids.removeAll(viewers.keySet());
		for(final String cid : cids)
			loadProfile(cid);
		synchronized(config_load_status_update_sync_lock) {
			blis.onConfigLoadStatusUpdate(loadedProfiles.size(), failedProfiles.size(), cids.size());
		}
	}
	
	/**
	 * loads a specific profile
	 * @param cid profile id
	 */
	public static void loadProfile(String cid) {
		if(viewers.containsKey(cid))
			return;
		if(!poss.containsKey(cid))
			poss.put(cid, poss.put("(next)", poss.get("(next)")+1));
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					requestAction();
				} catch (InterruptedException e1) {
					return;
				}
				failedProfiles.remove(cid);
				blis.onProfileStartedLoading(cid);
				Viewer r = null;
				try {
					r = new Viewer(cid);
					viewers.put(cid, r);
					blis.onProfileLoadComplete(cid, poss.get(cid));
					r.setReady(true);
				} catch (Exception e) {
					blis.onProfileLoadError(cid, poss.get(cid), e);
				}
				synchronized(config_load_status_update_sync_lock) {
					(r == null
						?failedProfiles
						:loadedProfiles
						).add(cid);
					blis.onConfigLoadStatusUpdate(loadedProfiles.size(), failedProfiles.size(), ConfigsV2.getCids().size());
				}
				releaseAction();
			}
		});
		t.start();
	}
	
	/**
	 * @return a list of profile ids which are currently loaded
	 */
	public static HashSet<String> getLoadedProfiles() {
		return new HashSet<>(loadedProfiles);
	}
	
	/**
	 * stops and unloads a profile
	 * @param cid profile id
	 */
	public static void unloadProfile(String cid) {
		for(int i=0; i<5; i++)
			setRunning(cid, i, false);
		viewers.remove(cid);
		blis.onProfileUnloaded(cid);
		loadedProfiles.remove(cid);
		new File("data/temp/"+cid+".srb.json").delete();
	}
	
	/**
	 * stops the bot
	 */
	public static void stop() {
		setClockRunning(false);
		for(String key : viewers.keySet())
			viewers.get(key).saveStats();
		for(String cid : getLoadedProfiles())
			unloadProfile(cid);
		ConfigsV2.save();
	}
	
	/**
	 * starts or stops slots for the specified profile
	 * @param cid profile id
	 * @param slot [0-4]
	 * @param b true => start, false => stop
	 */
	public static void setRunning(String cid, int slot, boolean b) {
		viewers.get(cid).setRunning(b, slot);
	}
	
	/**
	 * switches the running state of a slot
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchRunning(String cid, int slot) {
		Viewer r = viewers.get(cid);
		r.setRunning(!r.isRunning(slot), slot);
	}
	
	
	/**
	 * syncs a slot to another slot.
	 * a synced slot will be managed by the slot it's synced to
	 * @param cid profile id
	 * @param lay layer id
	 * @param slot the slot that relinquishes control of itself
	 * @param syncTo the slot that rules from now on, use -1 to unsync
	 */
	public static void syncSlots(String cid, String lay, int slot, int syncTo) {
		ConfigsV2.setSleepInt(cid, lay, ""+slot, ConfigsV2.sync, syncTo);
		if(syncTo != -1)
			ConfigsV2.setSleepInt(cid, lay, ""+syncTo, ConfigsV2.sync, -1);
		String configSyncTo = ConfigsV2.getPStr(cid, ConfigsV2.synced);
		if(!configSyncTo.equals("(none)"))
			cid = configSyncTo;
		
		for(String cid_ : getLoadedProfiles())
			if(ConfigsV2.getPStr(cid_, ConfigsV2.synced).equals(cid) || cid_.equals(cid)) 
				getViewer(cid_).updateSlotSync();
	}
	
	/**
	 * return the current layer of the profile
	 * @param cid profile id
	 * @return the id of the profile's current layer
	 */
	public static String getCurrentLayer(String cid) {
		return viewers.get(cid).getCurrentLayer();
	}
	
	/**
	 * skips the sleep timer of a slot
	 * @param cid profile id
	 * @param slot
	 */
	public static void skipSleep(String cid, int slot) {
		viewers.get(cid).skip(slot);
	}
	
	/**
	 * marks/unmarks a slot to be changed
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchSlotChangeMarker(String cid, int slot) {
		viewers.get(cid).change(slot);
	}
	
	/**
	 * applies the fav value to the captain
	 * @param cid profile
	 * @param slot
	 * @param val value
	 */
	public static void favCaptain(String cid, int slot, int val) {
		viewers.get(cid).fav(slot, val);
	}
	
	/**
	 * @param cid profile id
	 * @param slot
	 * @return the url to the captains stream or null if slot empty
	 */
	public static String getTwitchCaptainLink(String cid, int slot) {
		return viewers.get(cid).getTwitchLink(slot);
	}
	
	private static int currentActions = 0;
	private static int max;
	
	/**
	 * sets the max of concurrent (defined) actions of the bot
	 * @param x the integer value (-1 to disable)
	 */
	public static void setMaxConcurrentActions(int x) {
		max = x;
	}
	
	private static List<Thread> actions = Collections.synchronizedList(new LinkedList<>());
	
	/**
	 * waits in a queue until an action is allowed <br>
	 * {@link #releaseAction()} should be called if action finished
	 * @throws InterruptedException when queue cleared
	 */
	public static void requestAction() throws InterruptedException {
		Thread ct = Thread.currentThread();
		actions.add(ct);
		synchronized (ct) {
			ct.wait();
		}
	}
	
	/**
	 * releases the action taken from {@link #requestAction()}
	 */
	public static void releaseAction() {
		currentActions--;
	}
	
	
	private static boolean isClockRunning = false;
	private static void setClockRunning(boolean b) {
		isClockRunning = b;
		if(b) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while(isClockRunning) {
						if(actions.size() > 0 && (currentActions < max || max < 0)) {
							currentActions++;
							Thread ct = actions.remove(0);
							synchronized (ct) {
								ct.notifyAll();
							}
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
				}
			});
			t.start();
		} else
			while(actions.size() > 0)
				actions.remove(0).interrupt();
	}
	
	/**
	 * forces to update every Profile
	 */
	public static void updateAllProfiles() {
		for(final String key : viewers.keySet())
			updateProfile(key);
	}
	
	/**
	 * forces to update the Profile
	 * @param cid profile id
	 */
	public static void updateProfile(String cid) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					viewers.get(cid).updateFrame(null);
				} catch (NoConnectionException | NotAuthorizedException e1) {
					Debug.printException("Manager -> updateProfile: err=failed to update frame", e1, Debug.general, Debug.error, cid, null, true);
				}
			}
		});
		t.start();
	}
	
	/**
	 * does an action for all profiles<br>
	 * @param con see {@link program.SRC.Manager} for constants
	 * @param delay time between starting each action
	 */
	public static void doAll(int con, int delay) {
		for(String key : viewers.keySet()) {
			for(int i=0; i<5; i++) {
				Viewer r = viewers.get(key);
				switch(con) {
				case SRC.Manager.start:
					r.setRunning(true, i);
					break;
				case SRC.Manager.skip:
					r.skip(i);
					break;
				case SRC.Manager.stop:
					r.setRunning(false, i);
					break;
				}
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	/**
	 * @return the current server time or null if no profile is loaded
	 */
	public static String getServerTime() {
		if(viewers.size() == 0)
			return null;
		StringBuilder st = new StringBuilder();
		viewers.elements().nextElement().useViewerBackEnd(beh -> {
			st.append(beh.getServerTime());
		});
		return st.toString();
	}
	
	
	/**
	 * @return a list with all (SR) user ids
	 */
	public static List<String> getUserIds() {
		List<String> ret = new ArrayList<>(viewers.size());
		for(String key : viewers.keySet()) {
			viewers.get(key).useViewerBackEnd(beh -> {
				ret.add(beh.getUserId());
			});
			
		}
		return ret;
	}
	
}
