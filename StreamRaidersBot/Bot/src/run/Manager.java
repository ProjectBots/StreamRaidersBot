package run;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Remaper;
import otherlib.Resources;
import otherlib.Resources.Resource;
import otherlib.Resources.ResourceReader;
import otherlib.Resources.ResourceTemplate;
import otherlib.Configs.IllegalConfigTypeException;
import otherlib.Configs.IllegalConfigVersionException;
import otherlib.Configs.PStr;
import otherlib.Configs.SleInt;
import run.captain.Captain;
import run.viewer.Viewer;
import srlib.EventsAndEventRewards;
import srlib.Reward;
import srlib.SRC;
import srlib.SRR;
import srlib.Time;
import srlib.SRR.NotAuthorizedException;
import srlib.SRR.OutdatedDataException;
import srlib.store.Store;
import srlib.units.UnitType;
import include.Json;

public class Manager {
	
	/*	TODO
	 * 	add tooltips (everywhere)
	 *	make epic slot dependent
	 * 	get Donators from github source
	 * 	split beh updates into parts (ex.: only update currencies instead of whole shop)
	 * 	when creating chest rewards for guide: exclude chest which aren't obtainable, compare to chests in Store
	 * 	option to disable the loading images
	 * 	optimize SkinSettings
	 * 	when stopping wait for slots to finish + option to force close
	 * 	better resource management (WIP)
	 *  - opt.txt to new system (eliminate as many JsonObjects as possible) 
	 * 	better config (WIP)
	 * 	- unit sync
	 * 
	 * 	rem unitPower from opt.txt
	 * 
	 */
	
	
	private static Hashtable<String, AbstractProfile<?>> profiles = new Hashtable<>();
	
	private static BotListener blis;
	
	public static BotListener blis() {
		return blis;
	}
	
	public static AbstractProfile<?> getProfile(String cid) {
		return profiles.get(cid);
	}
	
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
		return getProfile(cid).pos;
	}
	
	/**
	 * @return the ProfileType of the specified profile
	 */
	public static ProfileType getProfileType(String cid) {
		return profiles.get(cid).getType();
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
				+ "\t   _____ ____     ____        __ \r\n"
				+ "\t  / ___// __ \\   / __ )____  / /_\r\n"
				+ "\t  \\__ \\/ /_/ /  / __  / __ \\/ __/\r\n"
				+ "\t ___/ / _, _/  / /_/ / /_/ / /_  \r\n"
				+ "\t/____/_/ |_|  /_____/\\____/\\__/  \r\n"
				+ "\r\n");
		
		Resources.setFolder("data");
		
		Resources.addCategory(JsonObject.class, new ResourceReader<JsonObject>() {
			@Override
			public ResourceTemplate<JsonObject> read(String folder, String path) {
				//	just for me
				if(path.equals("defConfig.json") || path.equals("remaps.json"))
					return null;
					
				FileReader r = null;
				try {
					r = new FileReader(new File(folder+path));
					return new ResourceTemplate<JsonObject>(Json.parseObj(r), -1);
				} catch (Exception e) {
					throw new RuntimeException("failed to get "+folder+path);
				} finally {
					if(r != null)
						try {
							r.close();
						} catch (IOException e) {
							Logger.printException("Manager -> ini -> addCategory: failed to close reader", e, Logger.runerr, Logger.error, null, null, true);
						}
				}
			}
			@Override
			public void save(String folder, Resource<JsonObject> data) {
				FileWriter w = null;
				try {
					w = new FileWriter(new File(folder+data.name));
				} catch (Exception e) {
					throw new RuntimeException("failed to save "+folder+data.name);
				} finally {
					if(w != null)
						try {
							w.close();
						} catch (IOException e) {
							Logger.printException("Manager -> ini -> addCategory: failed to close writer", e, Logger.runerr, Logger.error, null, null, true);
						}
				}
			}
		}, "json");
		
		ArrayList<String> files = Resources.loadAllFilesInFolder();
		for(String file : files)
			Logger.print("Manager -> ini: unknown extension for "+file, Logger.lowerr, Logger.warn, null, null, true);
		
		try {
			Options.load();
		} catch (IOException | NullPointerException fnf) {
			throw new IniCanceledException("Couldnt load options");
		}
		
		System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaidersBot\r\n"
				+ "Version: " + Options.get("botVersion") + "\r\n");
		
		Remaper.load();
		
		UnitType.ini();
		
		try {
			Configs.load();
		} catch (IOException | IllegalConfigTypeException | IllegalConfigVersionException e) {
			Logger.printException("err=failed to load config", e, Logger.runerr, Logger.error, null, null, true);
			if(Manager.blis.configNotReadable()) {
				try {
					Configs.load(true);
				} catch (IOException | IllegalConfigTypeException | IllegalConfigVersionException e1) {
					Logger.printException("err=failed to reset config", e, Logger.runerr, Logger.error, null, null, true);
					throw new IniCanceledException("config not resetable");
				}
			} else {
				throw new IniCanceledException("config not readable");
			}
		}
		setMaxConcurrentActions(Configs.getGInt(Configs.maxProfileActions));
		setClockRunning(true);
		
	}
	
	
	/**
	 * adds and loads a new profile
	 * @param name profile name
	 * @param access_info cookie which is used to log into the account
	 * @return the profile id assigned
	 */
	public static String addProfile(String name, String access_info) {
		String cid = Configs.addProfile(name, access_info);
		Configs.saveb();
		countProfiles++;
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
		Configs.remProfile(cid);
		Configs.saveb();
		blis.onProfileRemoved(cid);
		failedProfiles.remove(cid);
	}
	
	private static int countProfiles = -1;
	private static HashSet<String> loadedProfiles = new HashSet<>();
	private static HashSet<String> failedProfiles = new HashSet<>();
	private static final Object config_load_status_update_sync_lock = new Object();
	
	/**
	 * checks the config for new profiles and loads them
	 */
	public static void loadAllNewProfiles() {
		ArrayList<String> cids = Configs.getConfigIds();
		countProfiles = cids.size();
		cids.removeAll(profiles.keySet());
		for(final String cid : cids)
			loadProfile(cid);
		synchronized(config_load_status_update_sync_lock) {
			blis.onConfigLoadStatusUpdate(loadedProfiles.size(), failedProfiles.size(), countProfiles);
		}
	}
	
	/**
	 * loads a specific profile
	 * @param cid profile id
	 */
	private static void loadProfile(final String cid) {
		if(profiles.containsKey(cid))
			return;
		new Thread(() -> {
			try {
				requestAction();
			} catch (InterruptedException e1) {
				return;
			}
			failedProfiles.remove(cid);
			blis.onProfileStartedLoading(cid);
			AbstractProfile<?> p = null;
			int pos = Configs.getConfigIds().indexOf(cid);
			try {
				SRR req = null;
				try {
					req = new SRR(cid, Options.get("clientVersion"));
				} catch (OutdatedDataException e) {
					Time.updateSecsOff(e.getServerTime());
					updateSRData(e.getDataPath(), req);
					req = new SRR(cid, Options.get("clientVersion"));
				}
				if(req.playsAsCaptain()) {
					p = new Captain(cid, req, pos);
					if(!Options.is("captain_beta"))
						p = p.switchProfileType();
				} else
					p = new Viewer(cid, req, pos);
				profiles.put(cid, p);
				
				boolean before = Configs.getPBoo(cid, Configs.canCaptain);
				if(!before) {
					boolean now = req.canPlayAsCaptain();
					if(now) {
						Configs.setPBoo(cid, Configs.canCaptain, now);
						Configs.check(cid);
					}
				}
				
				blis.onProfileLoadComplete(cid, pos, p.getType());
				p.setReady(true);
			} catch (Exception e) {
				blis.onProfileLoadError(cid, pos, e);
			}
			synchronized(config_load_status_update_sync_lock) {
				(p == null
					?failedProfiles
					:loadedProfiles
					).add(cid);
				blis.onConfigLoadStatusUpdate(loadedProfiles.size(), failedProfiles.size(), countProfiles);
			}
			releaseAction();
		}).start();
	}
	
	public static void retryLoadProfile(final String cid) {
		if(!failedProfiles.contains(cid))
			return;
		loadProfile(cid);
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
	private static void unloadProfile(String cid) {
		if(!profiles.containsKey(cid))
			throw new IllegalArgumentException("No Profile with cid="+cid+" loaded");
		setRunningAll(cid, false);
		AbstractProfile<?> p = profiles.remove(cid);
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
			profiles.get(key).saveRews();
		for(String cid : getLoadedProfiles())
			unloadProfile(cid);
		Configs.save();
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
		profiles.get(cid).setRunning(slot, b);
	}
	
	/**
	 * switches the running state of a slot in the specified profile
	 * @param cid profile id
	 * @param slot
	 */
	public static void switchRunning(String cid, int slot) {
		AbstractProfile<?> p = profiles.get(cid);
		p.setRunning(slot, !p.isRunning(slot));
	}
	
	/**
	 * switches the profile type
	 * this should not be called with a profile, which is viewer only
	 * @param cid profile id
	 * @throws Exception
	 */
	public static void switchProfileType(String cid) throws Exception {
		AbstractProfile<?> p = profiles.get(cid);
		p = p.switchProfileType();
		if(p == null)
			throw new Exception("Failed to switch, p is null");
		profiles.put(cid, p);
		blis.onProfileSwitchedAccountType(cid, p.getType());
		p.setReady(true);
	}
	
	
	/**
	 * syncs a slot to another slot.<br>
	 * a synced slot will be managed by the slot it's synced to
	 * @param cid profile id
	 * @param lay layer id
	 * @param slot the slot that relinquishes control of itself
	 * @param syncTo the slot that rules from now on, use -1 to unsync
	 */
	public static void syncSlots(String cid, String lay, int slot, int syncTo) {
		ProfileType pt = profiles.get(cid).getType();
		
		SleInt con = pt == ProfileType.VIEWER ? Configs.syncSlotViewer : Configs.syncSlotCaptain;
		Configs.setSleepInt(cid, lay, ""+slot, con, syncTo);
		if(syncTo != -1)
			Configs.setSleepInt(cid, lay, ""+syncTo, con, -1);
		
		PStr syncProfile = pt == ProfileType.VIEWER ? Configs.syncedViewer : Configs.syncedCaptain;
		
		String configSyncTo = Configs.getPStr(cid, syncProfile);
		if(!configSyncTo.equals("(none)"))
			cid = configSyncTo;
		
		for(String cid_ : getLoadedProfiles()) {
			AbstractProfile<?> p = profiles.get(cid);
			if(p.getType() == pt && (Configs.getPStr(cid_, syncProfile).equals(cid) || cid_.equals(cid)))
				p.updateSlotSync();
		}
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
	public static void switchSlotFriendly(String cid, int slot) {
		Viewer v = profiles.get(cid).getAsViewer();
		if(v != null)
			v.switchChange(slot);
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
	
	
	private static int redeemCodesAllRemaining;
	private static Object redeemCodesAllRemainingLock = new Object();
	
	/**
	 * redeems one or multiple codes<br>
	 * this function is <b>NOT</b> multi thread safe<br>
	 * do not use any other function while this one is running<br>
	 * codes have to be in this format: XXXX-XXXX-XXXX
	 * @param codes
	 */
	public static void redeemCodesAll(String... codes) {
		synchronized(redeemCodesAllRemainingLock) {
			redeemCodesAllRemaining = profiles.size();
		}
		for(String cid : profiles.keySet()) {
			new Thread(() -> {
				AbstractProfile<?> p = getProfile(cid);
				try {
					redeemCodes(p, codes);
					if(Configs.getPBoo(cid, Configs.canCaptain))
						redeemCodesSwitchProfileType(p, codes);
				} catch (Exception e) {
					Logger.printException("Manager -> redeemCodeAll: err=failed to redeem code", e, Logger.runerr, Logger.error, p.cid, null, true);
				}
				synchronized(redeemCodesAllRemainingLock) {
					if(--redeemCodesAllRemaining == 0)
						blis.redeemCodesFinished();
				}
			}).start();
		}
	}
	
	private static void redeemCodes(AbstractProfile<?> p, String[] codes) throws Exception {
		AbstractBackEnd<?> be = p.getBackEnd();
		for(int i=0; i<codes.length; i++)
			if(!be.redeemProductCode(codes[i]))
				Logger.print("Manager -> redeemCodes: err=failed to redeem, code="+codes[i], Logger.runerr, Logger.error, p.cid, null, true);
	}
	
	private static void redeemCodesSwitchProfileType(AbstractProfile<?> p, String[] codes) throws Exception {
		boolean[] wereRunning = new boolean[p.slots.length];
		for(int i=0; i<p.slots.length; i++)
			wereRunning[i] = p.isRunning(i);
		
		switchProfileType(p.cid);
		p = getProfile(p.cid);
		
		redeemCodes(p, codes);
		
		switchProfileType(p.cid);
		p = getProfile(p.cid);
		
		for(int i=0; i<p.slots.length; i++)
			p.setRunning(i, wereRunning[i]);
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
		new Thread(() -> {
			try {
				profiles.get(cid).updateFrame();
			} catch (NoConnectionException | NotAuthorizedException e1) {
				Logger.printException("Manager -> updateProfile: err=failed to update frame", e1, Logger.general, Logger.error, cid, null, true);
			}
		}).start();
	}
	
	/**
	 * does an action for all profiles<br>
	 * @param con see {@link srlib.SRC.Manager} for constants
	 * @param delay time between starting each action
	 */
	public static void doAll(int con, int delay) {
		AbstractProfile<?>[] psorted = new AbstractProfile[profiles.size()];

		int i = 0;
		for(AbstractProfile<?> p : profiles.values())
			psorted[i++] = p;
		
		Arrays.sort(psorted);
		
		for(i=0; i<psorted.length; i++) {
			switch(con) {
			case SRC.Manager.start:
				psorted[i].setRunningAll(true);
				break;
			case SRC.Manager.skip:
				psorted[i].skipAll();
				break;
			case SRC.Manager.stop:
				psorted[i].setRunningAll(false);
				break;
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {}
		}
	}
	
	private static final String[] catsToUpdateData = "obstacles Obstacles quests Quests mapNodes MapNodes store Store events Events skins Skins".split(" ");
	
	/**
	 * retrieves the StreamRaiders data from the data path url which the bot uses
	 * @param dataPathUrl
	 * @param req (will be reloaded, null to disable)
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	synchronized static void updateSRData(String dataPathUrl, SRR req) throws NoConnectionException, NotAuthorizedException {
		if(!Options.get("data").equals(dataPathUrl)) {
			JsonObject data = Json.parseObj(SRR.getData(dataPathUrl)).getAsJsonObject("sheets");
			for(int i=0; i<catsToUpdateData.length; i+=2)
				Options.set(catsToUpdateData[i], data.get(catsToUpdateData[i+1]).toString());
			
			JsonArray ets = EventsAndEventRewards.genTiersFromData(data);
			Options.set("eventTiers", ets.toString());
			Options.set("eventTiersSize", ""+ets.size());
			Options.set("eventBadges", EventsAndEventRewards.genEventBadgesFromData(data));
			Options.set("currentEventCurrency", data.getAsJsonObject("Items").getAsJsonObject("eventcurrency").get("CurrencyTypeAwarded").getAsString());
			Options.set("unitCosts", Store.genUnitCostsFromData(data).toString());
			Options.set("unitTypes", UnitType.genUnitTypesFromData(data).toString());
			Options.set("unitPower", UnitType.genUnitPowerFromData(data).toString());
			Options.set("rewards", Reward.updateChestRews(data).toString());
			Options.set("data", dataPathUrl);
			Options.save();
			
			for(String key : profiles.keySet())
				profiles.get(key).updateRews();
			
			Manager.blis.onSRDataUpdate(dataPathUrl, data);
		}

		if(req != null)
			try {
				req.reload();
			} catch (OutdatedDataException e) {
				Logger.printException("BackEndHandler -> updateDataPath: err=failed to update data path",  e, Logger.runerr, Logger.fatal, null, null, true);
			}
	}
	
	private static int currentActions = 0;
	private static int maxConcurrentActions;
	
	/**
	 * sets the max of concurrent (defined) actions of the bot
	 * @param x the integer value (-1 to disable)
	 */
	public static void setMaxConcurrentActions(int x) {
		maxConcurrentActions = x;
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
						if(actions.size() > 0 && (currentActions < maxConcurrentActions || maxConcurrentActions < 0)) {
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
	

	private static final Object gclock = new Object();
	private static long gcwait = System.currentTimeMillis();
	public static void gc() {
		synchronized (gclock) {
			long now = System.currentTimeMillis();
			if(Configs.getGBoo(Configs.useMemoryReleaser) && now > gcwait) {
				System.gc();
				gcwait = now + 30000;
			}
		}
	}
	
}
