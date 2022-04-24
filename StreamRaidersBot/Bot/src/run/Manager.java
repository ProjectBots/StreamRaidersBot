package run;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import include.Json;
import include.Time;
import program.ConfigsV2;
import program.Debug;
import program.Event;
import program.Options;
import program.Remaper;
import program.SRC;
import program.SRR;
import program.Store;
import program.Unit;
import program.SRR.NotAuthorizedException;
import program.SRR.OutdatedDataException;

public class Manager {
	
	/*	TODO
	 * 	rename Fonts to CS (ColorScheme)
	 * 	add tooltips (everywhere)
	 * 	fonts manage error blocks (GlobalOptions)
	 * 	config versioning
	 * 	make epic slot dependent
	 * 	get unlock/upgrade cost from datapath (sheets\UnitCosts\...)
	 *	make epic slot dependent
	 *	get unit types from datapath
	 *	get unit costs (unlock/upgrade) from datapath
	 * 	get Donators from github source
	 * 	split beh updates into parts (ex.: only update currencies instead of whole shop)
	 * 	when creating chest rewards for guide: exclude chest which aren't obtainable, compare to chests in Store
	 * 	option to disable loading images (saves ram)
	 * 	ViewerBackEnd change arrays to lists
	 * 
	 * 
	 * 	???:
	 * 	kill (slot) round and restart if it takes more than x min
	 * 	- may not be possible, didn't found a reliable way to "kill" a thread if it's hung up
	 * 	
	 * 
	 */
	
	
	private static Hashtable<String, Profile<?,?>> profiles = new Hashtable<>();
	private static Hashtable<String, Integer> poss = new Hashtable<>();
	
	private static long secsoff = Long.MIN_VALUE;
	protected static BotListener blis;
	
	
	/**
	 * @param cid profile id
	 * @return the Viewer Object of the Profile or null if not loaded or Captain
	 */
	public static Viewer getViewer(String cid) {
		return profiles.get(cid).getAsViewer();
	}
	
	/**
	 * @param cid profile id
	 * @return the Captain Object of the profile or null if not loaded
	 */
	public static Captain getCaptain(String cid) {
		return profiles.get(cid).getAsCaptain();
	}
	
	/**
	 * @param cid profile id
	 * @return a instance unique number assigned to the profile starting from 0 and counting up. Can change when restarted, but keeps the order in which the profiles where added.
	 */
	public static int getProfilePos(String cid) {
		return poss.get(cid);
	}
	
	public static ProfileType getProfileType(String cid) {
		return profiles.get(cid).getType();
	}
	
	/**
	 * updates the value that will be returned by {@link #getSecsOff()}
	 * @param st StreamRaiders server time
	 */
	static void updateSecsOff(String st) {
		secsoff = ChronoUnit.SECONDS.between(Time.parse(st), LocalDateTime.now());
	}
	
	/**
	 * @return the amount off seconds between StreamRaiders's server time and local time
	 */
	public static long getSecsOff() {
		return secsoff;
	}
	
	/**
	 * @return the current StreamRaiders server time or null if not updated
	 */
	public static String getServerTime() {
		if(secsoff == Long.MIN_VALUE)
			return null;
		return Time.parse(LocalDateTime.now().minusSeconds(secsoff));
	}
	
	
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
	 * @param blis BotListener
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
		if(profiles.containsKey(cid))
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
		cids.removeAll(profiles.keySet());
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
	public static void loadProfile(final String cid) {
		if(profiles.containsKey(cid))
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
				Profile<?,?> p = null;
				try {
					SRR req = null;
					try {
						req = new SRR(cid, Options.get("clientVersion"));
					} catch (OutdatedDataException e) {
						updateSecsOff(e.getServerTime());
						updateSRData(e.getDataPath(), req);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {}
						req = new SRR(cid, Options.get("clientVersion"));
					}
					if(req.playsAsCaptain()) {
						p = new Captain(cid, req);
						profiles.put(cid, p);
					} else {
						p = new Viewer(cid, req);
						profiles.put(cid, p);
					}
					blis.onProfileLoadComplete(cid, poss.get(cid), p.getType());
					p.setReady(true);
				} catch (Exception e) {
					blis.onProfileLoadError(cid, poss.get(cid), e);
				}
				synchronized(config_load_status_update_sync_lock) {
					(p == null
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
		if(!profiles.containsKey(cid))
			throw new IllegalArgumentException("No Profile with cid="+cid+" loaded");
		setRunningAll(cid, false);
		Profile<?,?> p = profiles.remove(cid);
		blis.onProfileUnloaded(cid);
		loadedProfiles.remove(cid);
		
		new Thread(() ->{
			while(!p.hasStopped()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			new File("data/temp/"+cid+".srb.json").delete();
		}).start();
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
	 * starts or stops all slots for the specified profile
	 * @param cid profile id
	 * @param b true => start, false => stop
	 */
	public static void setRunningAll(String cid, boolean b) {
		profiles.get(cid).setRunningAll(b);
	}
	
	/**
	 * starts or stops the slot for the specified profile
	 * @param cid profile id
	 * @param slot [0-4]
	 * @param b true => start, false => stop
	 */
	public static void setRunning(String cid, int slot, boolean b) {
		profiles.get(cid).setRunning(b, slot);
	}
	
	/**
	 * switches the running state of a slot in the specified profile
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchRunning(String cid, int slot) {
		Profile<?,?> p = profiles.get(cid);
		p.setRunning(!p.isRunning(slot), slot);
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
		Viewer v = profiles.get(cid).getAsViewer();
		if(v != null)
			v.change(slot);
	}
	
	/**
	 * applies the fav value to the captain
	 * @param cid profile
	 * @param slot
	 * @param val value
	 */
	public static void favCaptain(String cid, int slot, int val) {
		Viewer v = profiles.get(cid).getAsViewer();
		if(v != null)
			v.fav(slot, val);
	}
	
	/**
	 * @param cid profile id
	 * @param slot
	 * @return the url to the captains stream or null if slot empty
	 */
	public static String getTwitchCaptainLink(String cid, int slot) {
		Viewer v = profiles.get(cid).getAsViewer();
		return v != null ? v.getTwitchLink(slot) : null;
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
	 * updates every Profile
	 */
	public static void updateAllProfiles() {
		for(final String key : profiles.keySet())
			updateProfile(key);
	}
	
	/**
	 * updates the Profile
	 * @param cid profile id
	 */
	public static void updateProfile(String cid) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					profiles.get(cid).updateFrame(null);
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
		for(String key : profiles.keySet()) {
			Profile<?,?> p = profiles.get(key);
			switch(con) {
			case SRC.Manager.start:
				p.setRunningAll(true);
				break;
			case SRC.Manager.skip:
				p.skipAll();
				break;
			case SRC.Manager.stop:
				p.setRunningAll(false);
				break;
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {}
		}
	}
	
	
	/**
	 * retrieves StreamRaiders data from the data path url
	 * @param dataPathUrl
	 * @param req
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	synchronized static void updateSRData(String dataPathUrl, SRR req) throws NoConnectionException, NotAuthorizedException {
		if(!Options.get("data").equals(dataPathUrl)) {
			JsonObject data = Json.parseObj(SRR.getData(dataPathUrl)).getAsJsonObject("sheets");
			for(String s : "obstacles Obstacles  quests Quests  mapNodes MapNodes  specsRaw Specialization  store Store  rewards ChestRewards  events Events  skins Skins".split("  ")) {
				String[] ss = s.split(" ");
				Options.set(ss[0], data.getAsJsonObject(ss[1]).toString());
			}
			Event.updateCurrentEvent(getServerTime());
			String currentEventUid = Event.getCurrentEvent();
			if(currentEventUid != null) {
				currentEventUid = currentEventUid.split("_")[0];
				JsonObject tiers = data.getAsJsonObject("EventTiers");
				JsonObject currentTiers = new JsonObject();
				for(String key : tiers.keySet())
					if(key.matches("^"+currentEventUid+"[0-9]+$"))
						currentTiers.add(key, tiers.get(key));
				Options.set("eventTiers", currentTiers.toString());
			} else {
				Options.set("eventTiers", "{}");
			}
			Options.set("currentEventCurrency", data.getAsJsonObject("Items").getAsJsonObject("eventcurrency").get("CurrencyTypeAwarded").getAsString());
			JsonObject unitCosts = data.getAsJsonObject("UnitCosts");
			Options.set("unitCosts", unitCosts.toString());
			Store.setUnitCosts(unitCosts);
			Unit.setUnitTypes(data);
			Options.set("unitTypes", Unit.getTypes().toString());
			
			JsonObject units_raw = data.getAsJsonObject("Units");
			JsonObject units = new JsonObject();
			for(String key : units_raw.keySet()) {
				JsonObject u = units_raw.getAsJsonObject(key);
				if(u.get("CanBePlaced").getAsBoolean())
					units.add(key, u);
			}
			Options.set("units", units.toString());
			
			Options.set("data", dataPathUrl);
			Options.save();
			Manager.blis.onSRDataUpdate(dataPathUrl, data);
		}
		try {
			if(req != null)
				req.reload();
		} catch (OutdatedDataException e) {
			Debug.printException("BackEndHandler -> updateDataPath: err=failed to update data path",  e, Debug.runerr, Debug.fatal, null, null, true);
		}
	}
	
}
