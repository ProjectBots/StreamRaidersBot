package run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import program.ConfigsV2;
import program.Debug;
import program.SRC;
import program.SRR.NotAuthorizedException;

public class Manager {
	
	private static Hashtable<String, Run> profiles = new Hashtable<>();
	private static Hashtable<String, Integer> poss = new Hashtable<>();
	
	/**
	 * @param cid profile id
	 * @return the Run Object of the profile or null if not loaded
	 */
	public static Run getProfile(String cid) {
		return profiles.get(cid);
	}
	
	/**
	 * @param cid profile id
	 * @return a instance unique number assigned to the profile starting from 0 and counting up. Can change when restarted, but keeps the order in which the profiles where added.
	 */
	public static int getProfilePos(String cid) {
		return poss.get(cid);
	}
	
	static BotListener blis;
	
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
		BackEndHandler.setDataPathEventListener(new run.BackEndHandler.DataPathEventListener() {
			@Override
			public void onUpdate(String dataPath, String serverTime, JsonObject data) {
				run.BackEndHandler.DataPathEventListener.super.onUpdate(dataPath, serverTime, data);
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
		poss.put(cid, poss.put("(next)", poss.get("(next)")+1));
		loadProfile(cid);
		blis.onProfileAdded(cid);
		return cid;
	}
	
	/**
	 * unloads and deletes the profile
	 * @param cid profile id
	 */
	public static void remProfile(String cid) {
		unloadProfile(cid);
		ConfigsV2.remProfile(cid);
		ConfigsV2.saveb();
		poss.remove(cid);
		blis.onProfileRemoved(cid);
	}
	
	/**
	 * checks the config for new profiles and loads them
	 */
	public static void loadAllNewProfiles() {
		List<String> cids = ConfigsV2.getCids();
		cids.removeAll(profiles.keySet());
		for(final String cid : cids)
			loadProfile(cid);
	}
	
	/**
	 * loads a specific profile
	 * @param cid profile id
	 */
	public static void loadProfile(String cid) {
		if(profiles.containsKey(cid))
			return;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					requestAction();
				} catch (InterruptedException e1) {
					return;
				}
				blis.onProfileStartedLoading(cid);
				try {
					Run r = new Run(cid);
					profiles.put(cid, r);
					blis.onProfileLoadComplete(cid, poss.get(cid));
					r.setReady(true);
				} catch (Exception e) {
					blis.onProfileLoadError(cid, poss.get(cid), e);
				}
				releaseAction();
			}
		});
		t.start();
	}
	
	/**
	 * @return a list of profile ids which are currently loaded
	 */
	public static List<String> getLoadedProfiles() {
		return new ArrayList<>(profiles.keySet());
	}
	
	/**
	 * stops and unloads a profile
	 * @param cid profile id
	 */
	public static void unloadProfile(String cid) {
		for(int i=0; i<5; i++)
			setRunning(cid, i, false);
		profiles.remove(cid);
		blis.onProfileUnloaded(cid);
	}
	
	/**
	 * stops the bot
	 */
	public static void stop() {
		setClockRunning(false);
		for(String key : profiles.keySet())
			profiles.get(key).saveStats();
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
		profiles.get(cid).setRunning(b, slot);
	}
	
	/**
	 * switches the running state of a slot
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchRunning(String cid, int slot) {
		Run r = profiles.get(cid);
		r.setRunning(!r.isRunning(slot), slot);
	}
	
	/**
	 * @param cid profile id
	 * @return the id of the profile's current layer
	 */
	public static String getCurrentLayer(String cid) {
		return profiles.get(cid).getCurrentLayer();
	}
	
	/**
	 * skips the sleep timer of a slot
	 * @param cid profile id
	 * @param slot
	 */
	public static void skipSleep(String cid, int slot) {
		profiles.get(cid).skip(slot);
	}
	
	/**
	 * marks/unmarks a slot to be changed
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchSlotChangeMarker(String cid, int slot) {
		profiles.get(cid).change(slot);
	}
	
	/**
	 * applies the fav value to the captain
	 * @param cid profile
	 * @param slot
	 * @param val value
	 */
	public static void favCaptain(String cid, int slot, int val) {
		profiles.get(cid).fav(slot, val);
	}
	
	/**
	 * @param cid profile id
	 * @param slot
	 * @return the url to the captains stream or null if slot empty
	 */
	public static String getTwitchCaptainLink(String cid, int slot) {
		return profiles.get(cid).getTwitchLink(slot);
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
								ct.notify();
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
		for(final String key : profiles.keySet())
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
					profiles.get(cid).updateFrame();
				} catch (NoConnectionException | NotAuthorizedException e1) {
					Debug.printException("Manager -> updateProfile: err=failed to update frame", e1, Debug.general, Debug.error, ConfigsV2.getPStr(cid, ConfigsV2.pname), null, true);
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
		for(String key : profiles.keySet()) {
			for(int i=0; i<5; i++) {
				Run r = profiles.get(key);
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
		if(profiles.size() == 0)
			return null;
		return profiles.elements().nextElement().getBackEndHandler().getServerTime();
	}
	
	
	/**
	 * @return a list with all (SR) user ids
	 */
	public static List<String> getUserIds() {
		List<String> ret = new ArrayList<>(profiles.size());
		for(String key : profiles.keySet())
			ret.add(profiles.get(key).getBackEndHandler().getUserId());
		return ret;
	}
	
}