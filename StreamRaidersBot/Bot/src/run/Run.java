package run;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Heatmap;
import include.Json;
import include.Maths;
import include.Pathfinding;
import include.Time;
import program.Captain;
import program.ConfigsV2;
import program.Raid;
import program.Raid.Reward;
import program.Remaper;
import program.SRC;
import program.Store;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;
import program.SRR.OutdatedDataException;

import program.Store.C;
import program.Store.Item;
import program.Unit;
import run.BackEndHandler.UpdateEventListener;
import program.ConfigsV2.ListType;
import program.ConfigsV2.StorePrioType;
import program.MapConv.NoFinException;
import program.QuestEventRewards.Quest;
import program.Debug;
import program.Map;
import program.MapConv;
import program.Options;

public class Run {
	
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
	 * 
	 * 
	 * 	???:
	 * 	kill (slot) round and restart if it takes more than x min
	 * 	- may not be possible, didn't found a reliable way to "kill" a thread if it's hung up
	 * 	
	 * 
	 */

	private String cid;
	private BackEndHandler beh;
	
	private String currentLayer = "(default)";
	private String currentLayerId = null;
	private boolean[] isRunning = new boolean[5];
	private boolean[] isActiveRunning = new boolean[5];
	private boolean[] change = new boolean[4];
	private int[] sleep = new int[5];
	
	private boolean ready = false;
	public void setReady(boolean b) {
		ready = b;
	}
	
	private JsonObject rews = null;
	
	private static final String[] rew_sources = "chests bought event".split(" ");
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "snowfallcharitychest dungeonchest vampirechest saintchest".split(" ");
	private static final String[] rew_types = "gold potions token eventcurrency keys meat bones skin scrollnecromancer scrollmage scrollwarbeast scrolltemplar scrollorcslayer scrollballoonbuster scrollartillery scrollflyingarcher scrollberserker scrollcenturion scrollmusketeer scrollmonk scrollbuster scrollbomber scrollbarbarian scrollpaladin scrollhealer scrollvampire scrollsaint scrollflagbearer scrollrogue scrollwarrior scrolltank scrollarcher".split(" ");
	
	
	private void iniRews() {
		rews = new JsonObject();
		for(String s : rew_sources) {
			JsonObject source = new JsonObject();
			switch(s) {
			case "chests":
				for(String t : rew_chests_chests)
					source.addProperty(t, 0);
				break;
			case "bought":
				for(String t : rew_bought_chests)
					source.addProperty(t, 0);
				break;
			}
			for(String t : rew_types)
				source.addProperty(t, 0);
			rews.add(s, source);
		}
	}
	
	synchronized public void addRew(String con, String type, int amount) {
		type = Remaper.map(type).replace(Options.get("currentEventCurrency"), Store.eventcurrency.get());
		try {
			JsonObject r = rews.getAsJsonObject(con);
			r.addProperty(type, r.get(type).getAsInt() + amount);
			beh.addCurrency(type, amount);
		} catch (NullPointerException e) {
			Debug.printException("Run -> addRew: err=failed to add reward, con=" + con + ", type=" + type + ", amount=" + amount, e, Debug.runerr, Debug.error, pn, null, true);
		}
	}
	
	public JsonObject getRews() {
		return rews;
	}
	

	public void saveStats() {
		JsonObject astats = ConfigsV2.getPObj(cid, ConfigsV2.stats);
		for(String s : rews.keySet()) {
			JsonObject stats = astats.getAsJsonObject(s);
			JsonObject rew = rews.getAsJsonObject(s);
			for(String v : rew.keySet())
				stats.addProperty(v, stats.get(v).getAsInt() + rew.get(v).getAsInt());
			
		}
		iniRews();
	}
	
	
	public static class StreamRaidersException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private Exception cause;
		private String text;
		public StreamRaidersException(String text, Exception cause, boolean silent) {
			this.cause = cause;
			this.text = text;
			Debug.printException(text, cause, Debug.runerr, Debug.fatal, null, null, !silent);
		}
		public StreamRaidersException(String text, Exception cause) {
			this.cause = cause;
			this.text = text;
			Debug.printException(text, cause, Debug.runerr, Debug.fatal, null, null, true);
		}
		public StreamRaidersException(String text) {
			cause = null;
			this.text = text;
			Debug.print(text, Debug.runerr, Debug.fatal, null, null, true);
		}
		public String getText() {
			return text;
		}
		public Exception getCause() {
			return cause;
		}
	}
	
	
	public Run(String cid) throws NotAuthorizedException, NoConnectionException, OutdatedDataException {
		this.cid = cid;
		pn = ConfigsV2.getPStr(cid, ConfigsV2.pname);
		beh = new BackEndHandler(pn, ConfigsV2.getPStr(cid, ConfigsV2.cookies));
		beh.setUpdateEventListener(new UpdateEventListener() {
			@Override
			public void afterUpdate(String obj) {
				Debug.print("updated "+obj, Debug.general, Debug.info, pn, null);
				if(obj.contains("caps::")) {
					boolean dungeon = obj.contains("::true");
					Captain[] caps;
					try {
						caps = beh.getCaps(dungeon);
						HashSet<String> got = new HashSet<>();
						for(Captain c : caps)
							got.add(c.get(SRC.Captain.twitchDisplayName));
						
						HashSet<String> favs = ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign);
						for(String tdn : favs) {
							if(got.contains(tdn) || !ConfigsV2.getCapBoo(cid, currentLayer, tdn, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.il))
								continue;
							
							JsonArray results = beh.searchCap(1, null, false, false, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, true, tdn);
							if(results.size() == 0)
								continue;
							
							Captain n = new Captain(results.get(0).getAsJsonObject());
							
							if(n.get(SRC.Captain.isPlaying).equals("1"))
								caps = add(caps, n);
						}
						beh.setCaps(caps, dungeon);
						
					} catch (NoConnectionException | NotAuthorizedException e) {
						Debug.printException(pn+": Run -> constr.: err=unable to retrieve caps", e, Debug.runerr, Debug.error, pn, null, true);
						return;
					}
				}
			}
		});
		iniRews();
	}
	
	public BackEndHandler getBackEndHandler() {
		return beh;
	}
	
	
	private List<List<Boolean>> queue = Collections.synchronizedList(new ArrayList<List<Boolean>>() {
		private static final long serialVersionUID = 1L;
		{
			add(Collections.synchronizedList(new ArrayList<>()));
			add(Collections.synchronizedList(new ArrayList<>()));
			add(Collections.synchronizedList(new ArrayList<>()));
			add(Collections.synchronizedList(new ArrayList<>()));
			add(Collections.synchronizedList(new ArrayList<>()));
		}
	});
	
	private boolean[] setRun = new boolean[5];
	public void setRunning(boolean b, int slot) {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				List<Boolean> q = queue.get(slot);
				q.add(b);
				if(setRun[slot])
					return;
				setRun[slot] = true;
				while(q.size() > 0) {
					boolean b = q.remove(0);
					Manager.blis.onProfileChangedRunning(cid, slot, b);
					if(isRunning[slot] == b)
						continue;
					isRunning[slot] = b;
					while(isActiveRunning[slot]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					if(b) {
						isActiveRunning[slot] = true;
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								slotSequence(slot);
							}
						});
						t.start();
					}
				}
				setRun[slot] = false;
			}
		});
		th.start();
	}
	
	public boolean isRunning(int slot) {
		return isRunning[slot];
	}

	public void skip(int slot) {
		sleep[slot] = 0;
	}
	
	public void change(int slot) {
		change[slot] = !change[slot];
		try {
			updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Debug.printException(pn+": Run -> change: slot="+slot+", err=failed to update Frame", e, Debug.runerr, Debug.error, pn, slot, true);
		}
	}
	
	public void fav(int slot, int val) {
		String cap = cnames[slot+4];
		if(cap == null)
			return;
		
		Integer v = ConfigsV2.getCapInt(cid, "(all)", cap, cnames[slot+8].equals("d") ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.fav);
		if(v == null 
				|| v == Integer.MAX_VALUE-1
				|| v == Integer.MIN_VALUE+1
				|| Math.signum(val)*Math.signum(v) <= 0)
			ConfigsV2.favCap(cid, "(all)", cap, ConfigsV2.all, val);
		else 
			ConfigsV2.favCap(cid, "(all)", cap, ConfigsV2.all, null);
	}
	
	private String pn = null;
	
	public String getPN() {
		return pn;
	}
	
	synchronized private void slotSequence(int slot) {
		Debug.print("requesting action", Debug.general, Debug.info, pn, slot);
		try {
			Manager.requestAction();
		} catch (InterruptedException e1) {
			Debug.print("action rejected", Debug.general, Debug.info, pn, slot);
			return;
		}
		pn = ConfigsV2.getPStr(cid, ConfigsV2.pname);
		try {
			if_clause:
			if(slot == 4) {
				Debug.print("event", Debug.general, Debug.info, pn, slot);
				event();

				Debug.print("quest", Debug.general, Debug.info, pn, slot);
				quest();

				Debug.print("store", Debug.general, Debug.info, pn, slot);
				store();

				Debug.print("unlock", Debug.general, Debug.info, pn, slot);
				unlock();

				Debug.print("upgrade", Debug.general, Debug.info, pn, slot);
				upgrade();
				
				Debug.print("grantExtraRewards", Debug.general, Debug.info, pn, slot);
				beh.grantTeamReward();
				beh.grantEventQuestMilestoneReward();
				
			} else {
				
				if(!canUseSlot(slot))
					break if_clause;

				Debug.print("chest", Debug.general, Debug.info, pn, slot);
				chest(slot);

				Debug.print("captain", Debug.general, Debug.info, pn, slot);
				captain(slot);

				Debug.print("place", Debug.general, Debug.info, pn, slot);
				place(slot);
				
			}

			Debug.print("updateFrame", Debug.general, Debug.info, pn, slot);
			updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Debug.printException("Run -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Debug.runerr, Debug.fatal, pn, slot, true);
		} catch (StreamRaidersException | NoCapMatchesException e) {
		} catch (Exception e) {
			Debug.printException("Run -> slotSequence: slot=" + slot + " err=unknown", e, Debug.runerr, Debug.fatal, pn, slot, true);
		}
		
		Debug.print("releasing action", Debug.general, Debug.info, pn, slot);
		Manager.releaseAction();
		
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);
		
		
		// --- calculate time to sleep ---
		
		JsonObject ptimes = ConfigsV2.getPObj(cid, ConfigsV2.ptimes);
		int e = Integer.parseInt(currentLayerId.split("-")[1]);
		
		int min = ConfigsV2.getSleep(cid, currentLayer, ""+slot, ConfigsV2.min);
		int max = ConfigsV2.getSleep(cid, currentLayer, ""+slot, ConfigsV2.max);
		
		int w = -1;
		
		
		// test if sleep is not possible before next layer
		if_clause:
		if(min < 0 || max < 0 || n+(min/300) > e) {
			// change layer
			//	loop multiple times to be sure that it finds the next layer
			for(int i=0; i<ptimes.size(); i++) {
				// loop until first layer after current which is not disabled
				for(String t : ptimes.keySet()) {
					int s = Integer.parseInt(t.split("-")[0]);
					if(s != (e == 2015 ? 0 : e+1))
						continue;
					
					if(ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.min) < 0 ||
							ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.max) < 0) {
						e = Integer.parseInt(t.split("-")[1]);
						continue;
					}

					// shift start if before now
					if(s < n)
						s += 2016;
					
					// calculate time until next layer which is not disabled
					w = (s-n)*300;
					break if_clause;
				}
			}
		} else {
			// test if max is still in same layer or else set max to end time of layer
			if(n+(max/300) >= e)
				max = (e-n)*300;
			// generate random sleep-time
			w = Maths.ranInt(min, max);
		}
		
		if(w > -1) {
			Debug.print("sleeping "+w+" sec", Debug.general, Debug.info, pn, slot);
			sleep(w, slot);
		} else {
			Debug.print("Run -> slotSequence: err=couldn't find wait time", Debug.runerr, Debug.fatal, pn, slot, true);
			Manager.blis.onProfileChangedRunning(cid, slot, false);
			isActiveRunning[slot] = false;
			isRunning[slot] = false;
		}
		
		Debug.print("before MemoryReleaser", Debug.general, Debug.info, pn, slot);
		synchronized (gclock) {
			if(ConfigsV2.getGBoo(ConfigsV2.useMemoryReleaser) && now.isAfter(gcwait)) {
				System.gc();
				gcwait = now.plusSeconds(30);
			}
		}
		Debug.print("after MemoryReleaser", Debug.general, Debug.info, pn, slot);
		
	}
	
	private static LocalDateTime gcwait = LocalDateTime.now();
	private static final Object gclock = new Object();
	
	
	public boolean canUseSlot(int slot) throws NoConnectionException, NotAuthorizedException {
		int uCount = beh.getUnits(pn, SRC.BackEndHandler.all, false).length;
		return (slot == 0)
				|| (slot == 1 && uCount > 4)
				|| (slot == 2 && uCount > 7)
				|| (slot == 3 && beh.hasBattlePass());
	}
	
	private void sleep(int sec, int slot) {
		sleep[slot] = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isRunning[slot]) {
					t.cancel();
					isActiveRunning[slot] = false;
				}
				
				int mm = sleep[slot] / 60;
				int ss = sleep[slot] % 60;
				
				String ms = "";
				
				if(mm != 0) {
					ms += mm+":";
					if(ss < 10)
						ms += "0";
				}
				
				ms += ss;
				
				Manager.blis.onProfileTimerUpdate(cid, slot, ms);
				
				sleep[slot]--;
				
				if(sleep[slot] < 0) {
					t.cancel();
					slotSequence(slot);
				}
			}
		}, 0, 1000);
	}

	private HashSet<String> bannedPos = new HashSet<>();
	private boolean goMultiPlace;
	private LocalDateTime placeTime = LocalDateTime.now();
	
	private void place(int slot) throws NoConnectionException, NotAuthorizedException {
		//	Unit place delay
		long tdif = ChronoUnit.MILLIS.between(LocalDateTime.now(), placeTime);
		if(tdif > 0) {
			try {
				Thread.sleep(tdif);
			} catch (InterruptedException e) {}
		}
		
		Raid r = beh.getRaid(slot, true);
		if(r == null)
			return;

		if(r.isVersus())
			return;
		
		String placeSer = r.get(SRC.Raid.placementsSerialized);
		if(!r.canPlaceUnit(beh.getServerTime())
			|| ConfigsV2.getInt(cid, currentLayer, ConfigsV2.maxUnitPerRaid) < (placeSer == null 
																					? 0 
																					: placeSer.split(beh.getUserId()).length-1))
			return;
		
		
		boolean dungeon = r.isDungeon();
		
		//	check place settings (eg.: min loy/room) before placement (bcs of locked slots)
		
		String tdn = r.get(SRC.Raid.twitchDisplayName);
		Boolean ic = ConfigsV2.getCapBoo(cid, currentLayer, tdn, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.ic);
		if(ic == null)
			ic = false;
		
		String ct = r.getFromNode(SRC.MapNode.chestType);
		if(ct == null)
			return;
		ct = Remaper.map(ct);

		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		int maxLoy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxLoy);
		int minLoy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.minLoy);
		boolean enabled = ConfigsV2.getChestBoolean(cid, currentLayer, ct, ConfigsV2.enabled);
		int maxTimeLeft = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxTime);
		int minTimeLeft = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.minTime);
		
		if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
		
		
		int length = dungeon ? 360 : 1800;
		
		maxTimeLeft = length - maxTimeLeft;
		
		if(!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusSeconds(maxTimeLeft),
								beh.getServerTime())) {
			return;
		}
		
		if((dungeon ^ r.isDungeon())
			|| (!ic && Time.isAfter(beh.getServerTime(),
							Time.parse(r.get(SRC.Raid.creationDate))
								.plusSeconds(length - minTimeLeft)))
			|| (!ic && !enabled)
			|| (!ic && (loy < minLoy || loy > maxLoy))
			) {
			return;
		}
		
		
		//	check if epic is available
		Integer potionsc = beh.getCurrency(pn, Store.potions, true);
		boolean epic = potionsc == null ? false : (potionsc >= 45);
		
		final Unit[] units = beh.getPlaceableUnits(slot);
		Debug.print("units="+Arrays.toString(units), Debug.units, Debug.info, pn, slot);
		
		Map map = beh.getMap(pn, slot, true);
		
		HashSet<String> upts = map.getUsablePlanTypes();
		upts.remove("noPlacement");
		
		Debug.print("upts="+upts, Debug.units, Debug.info, pn, slot);
		
		if(!ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.allowPlaceFirst) 
			&& (placeSer == null ? true : Json.parseArr(placeSer).size() == 0))
			return;
		
		
		int[] mh = new Heatmap().getMaxHeat(map);
		
		int re = 0;
		int retries = ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unitPlaceRetries);
		int reload = ConfigsV2.getInt(cid, currentLayer, ConfigsV2.mapReloadAfterXRetries);
		bannedPos = new HashSet<>();
		
		List<String> neededUnits = beh.getNeededUnitTypesForQuests();
		boolean preferRogues = ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.preferRoguesOnTreasureMaps) 
				&& r.getFromNode(SRC.MapNode.nodeType).contains("treasure");
		
		if(preferRogues) {
			neededUnits.add("rogue");
			neededUnits.add("flyingarcher");
		}
		
		Debug.print("neededUnits="+neededUnits, Debug.units, Debug.info, pn, slot);
		
		while(true) {
			Debug.print("place "+re, Debug.loop, Debug.info, pn, slot);
			
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiPlaceExploit)) {
				goMultiPlace = false;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					final Place pla = findPlace(map, mh, upts, neededUnits, units, epic, dungeon, r.getFromNode(SRC.MapNode.chestType),
							ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign).contains(r.get(SRC.Raid.twitchDisplayName)), slot);
					if(pla == null)
						continue;
					bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiPlace) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								beh.placeUnit(pn, slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiPlace = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				break;
			} else {
				String node = Remaper.map(r.getFromNode(SRC.MapNode.chestType));
				boolean fav = ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign).contains(r.get(SRC.Raid.twitchDisplayName));
				final Place pla = findPlace(map, mh, upts, neededUnits, units, epic, dungeon, node, fav, slot);
				if(pla == null) {
					Debug.print("Place=null", Debug.units, Debug.info, pn, slot);
					break;
				}
				Debug.print("Place="+pla.toString(), Debug.units, Debug.info, pn, slot);
				String err = beh.placeUnit(pn, slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
				bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
				
				if(err == null) {
					if(pla.epic)
						beh.decreaseCurrency(Store.potions, 45);
					
					beh.addCurrency(Store.potions, 1);
					String ut = pla.unit.get(SRC.Unit.unitType);
					if(!Unit.isLegendary(ut))
						beh.addCurrency(pla.unit.get(SRC.Unit.unitType), 1);
					placeTime = LocalDateTime.now().plus(Maths.ranInt(ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.minu),
																	ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.maxu)), 
														ChronoUnit.MILLIS);
					break;
				}
				
				if(err.equals("NOT_ENOUGH_POTIONS")) {
					beh.setCurrency(Store.potions, 0);
					epic = false;
				}
				
				if(err.equals("PERIOD_ENDED"))
					break;
				
				if(re++ < retries) {
					if(re % reload == 0) {
						map = beh.getMap(pn, slot, true);
					}
					continue;
				}
				
				Debug.print("Run -> place: tdn="+r.toString()+" err="+err, Debug.lowerr, Debug.error, pn, slot, true);
				break;
			}
			
			
			
			
		}
		
	}
	
	
	private static class Place {
		public final Unit unit;
		public final int[] pos;
		public final boolean epic;
		public final boolean isOnPlan;
		public Place(Unit u, int[] pos, boolean epic, boolean isOnPlan) {
			this.unit = u;
			this.pos = pos;
			this.epic = epic;
			this.isOnPlan = isOnPlan;
		}
		@Override
		public String toString() {
			return unit.get(SRC.Unit.unitType)+"|"+pos[0]+"-"+pos[1]+(epic ? "|epic" : "")+(isOnPlan ? "|plan" : "");
		}
	}
	
	
	
	private static class Prio {
		public final Unit unit;
		public final int[] ps;
		public final boolean[] vs;
		public Prio(Unit unit, int np, int ep, int n, int e, boolean vn, boolean ve) {
			this.unit = unit;
			ps = new int[] {ep, np, e, n};
			vs = new boolean[] {ve, vn};
		}
		@Override
		public String toString() {
			return "{" + unit.get(SRC.Unit.unitType) + "|ps=" + Arrays.toString(ps) + "|vs=" + Arrays.toString(vs) + "}";
		}
	}
	
	private Place findPlace(Map map, int[] mh, HashSet<String> upts, List<String> neededUnits, final Unit[] units, boolean epic, boolean dungeon, String chest, boolean fav, int slot) {
		HashSet<String> epts = new HashSet<>();
		if(epic) {
			String[][] mpts = new String[map.width()][map.length()];
			for(int x=0; x<map.width(); x++) 
				for(int y=0; y<map.length(); y++) 
					if(map.is(x, y, SRC.Map.isEmpty))
						mpts[x][y] = map.getPlanType(x, y);
				
			for(int x=0; x<mpts.length-1; x++) {
				for(int y=0; y<mpts[x].length-1; y++) {
					String pt = mpts[x][y];
					if(pt == null)
						continue;
					if(pt.equals(mpts[x+1][y])
							&& pt.equals(mpts[x][y+1])
							&& pt.equals(mpts[x+1][y+1]))
						epts.add(pt);
				}
			}
		}
		
		Prio[] prios = new Prio[units.length];
		for(int i=0; i<units.length; i++) {
			final String utype = units[i].get(SRC.Unit.unitType);
			
			int n = ConfigsV2.getUnitInt(cid, currentLayer, utype, dungeon ? ConfigsV2.placedun : ConfigsV2.place);
			int e = ConfigsV2.getUnitInt(cid, currentLayer, utype, dungeon ? ConfigsV2.epicdun : ConfigsV2.epic);
			String chests = ConfigsV2.getUnitString(cid, currentLayer, utype, ConfigsV2.chests);
			String favOnly = ConfigsV2.getUnitString(cid, currentLayer, utype, ConfigsV2.favOnly);
			String markerOnly = ConfigsV2.getUnitString(cid, currentLayer, utype, ConfigsV2.markerOnly);
			String canVibe = ConfigsV2.getUnitString(cid, currentLayer, utype, ConfigsV2.canVibe);
			
			final String nx = dungeon ? "nd" : "nc";
			final String ex = dungeon ? "ed" : "ec";
			
			
			if(!fav && favOnly.contains(nx))
				n = -1;
			
			if(!fav && favOnly.contains(ex))
				e = -1;
			
			if(!chests.contains(chest+",")) {
				n = -1;
				e = -1;
			}
			
			if(neededUnits.contains(utype))
				n = Integer.MAX_VALUE;
			
			
			int np = -1;
			int ep = -1;
			
			HashSet<String> pts = units[i].getPlanTypes();
			pts.remove("vibe");
			for(String pt : pts) {
				if(epts.contains(pt))
					ep = e;
				if(upts.contains(pt))
					np = n;
			}
			

			boolean vn = false;
			boolean ve = false;
			

			if(np < 0 && upts.contains("vibe") && canVibe.contains(nx)) {
				np = n;
				vn = true;
			}
			
			if(ep < 0 && epts.contains("vibe") && canVibe.contains(ex)) {
				ep = e;
				ve = true;
			}
			
			if(markerOnly.contains(nx))
				n = -1;
			
			if(markerOnly.contains(ex))
				e = -1;
			
			
			prios[i] = new Prio(units[i], np, ep, n, e, vn, ve);
		}
		
		Debug.print("prios=" + Arrays.toString(prios), Debug.units, Debug.info, pn, slot);
		
		for(int i=0; i<4; i++) {
			if(!epic && i%2 == 0)
				continue;
			
			while(true) {
				int p = 0;
				
				for(int j=1; j<prios.length; j++) 
					if(prios[j].ps[i] > prios[p].ps[i] 
						|| ((prios[j].ps[i] == prios[p].ps[i])
							&& (Integer.parseInt(prios[j].unit.get(SRC.Unit.level)) > Integer.parseInt(prios[p].unit.get(SRC.Unit.level)))))
						p = j;
				
				if(prios[p].ps[i] < 0)
					break;
				
				prios[p].ps[i] = -1;
				
				final Unit u = prios[p].unit;
				
				HashSet<String> pts = null;
				if(i<2) {
					pts = u.getPlanTypes();
					if(!prios[p].vs[i])
						pts.remove("vibe");
				}
				
				int[] pos = null;
				try {
					pos = new Pathfinding().search(new MapConv().asField(map, u.canFly(), pts, mh, bannedPos), pn, slot, i%2==0);
				} catch (NoFinException e) {}
				if(pos == null)
					continue;
				return new Place(u, pos, i%2==0, i<2);
			}
		}
		
		return null;
	}
	
	private void captain(int slot) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		captain(slot, true, false);
	}

	private void captain(int slot, boolean first, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		
		boolean dungeon = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.dungeonSlot).equals(""+slot);
		
		Raid r = beh.getRaid(slot, true);
		
		if(r != null && ConfigsV2.isSlotLocked(cid, currentLayer, ""+slot)) {
			if(r.isDungeon() && !dungeon) {
				Raid[] all = beh.getRaids(SRC.BackEndHandler.all);
				boolean change = true;
				for(int i=0; i<all.length; i++) {
					if(i == slot || all[i] == null)
						continue;
					if(all[i].isDungeon() && ConfigsV2.isSlotLocked(cid, currentLayer, ""+i))
						change = false;
				}
				if(change) {
					ConfigsV2.setStr(cid, currentLayer, ConfigsV2.dungeonSlot, ""+slot);
					dungeon = true;
				}
			} else if(!r.isDungeon() && dungeon) {
				ConfigsV2.setStr(cid, currentLayer, ConfigsV2.dungeonSlot, "(none)");
				dungeon = false;
			}
		}
		
		if(ConfigsV2.isSlotLocked(cid, currentLayer, ""+slot) && !change[slot])
			return;
		
		if(r == null) {
			switchCap(slot, dungeon, null, null, noCap, first, null);
			return;
		}
		
		if(!r.isSwitchable(beh.getServerTime(), ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capInactiveTreshold)))
			return;

		String tdn = r.get(SRC.Raid.twitchDisplayName);
		if(r.isVersus()) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}

		ListType list = dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign;
		
		Boolean ic = ConfigsV2.getCapBoo(cid, currentLayer, tdn, list, ConfigsV2.ic);
		ic = ic == null ? false : ic;
		Boolean il = ConfigsV2.getCapBoo(cid, currentLayer, tdn, list, ConfigsV2.il);
		il = il == null ? false : il;
		Integer fav = ConfigsV2.getCapInt(cid, currentLayer, tdn, list, ConfigsV2.fav);
		fav = fav == null ? 0 : fav;
		String ct = r.getFromNode(SRC.MapNode.chestType);

		if(ct == null) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		ct = Remaper.map(ct);

		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		Integer maxLoy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxLoy);
		Integer minLoy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.minLoy);
		Boolean enabled = ConfigsV2.getChestBoolean(cid, currentLayer, ct, ConfigsV2.enabled);
		Integer maxTimeLeft = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxTime);
		Integer minTimeLeft = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.minTime);
		if(maxLoy == null || minLoy == null || enabled == null || maxTimeLeft == null || minTimeLeft == null) {
			Debug.print("Run -> captain: ct="+ct+", err=failed to get chest config", Debug.runerr, Debug.error, pn, slot, true);
			//	prevents picking the chest
			maxLoy = 5;
			minLoy = 8;
			enabled = false;
			maxTimeLeft = 10;
			minTimeLeft = 20;
		} else if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
			
		int length = dungeon ? 360 : 1800;
		
		maxTimeLeft = length - maxTimeLeft;
		
		if(!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusSeconds(maxTimeLeft),
								beh.getServerTime())) {
			switchCap(slot, dungeon, r, tdn, noCap, first, maxTimeLeft);
			return;
		}
		
		
		JsonArray users = Json.parseArr(r.get(SRC.Raid.users));
		if(users != null) {
			String uid = beh.getUserId();
			for(int i=0; i<users.size(); i++)
				if(users.get(i).getAsJsonObject().get("userId").getAsString().equals(uid))
					minTimeLeft = Integer.MIN_VALUE;
		}
		
		if((dungeon ^ r.isDungeon())
			|| r.isOffline(beh.getServerTime(), il, ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capInactiveTreshold))
			|| (!ic && Time.isAfter(beh.getServerTime(),
							Time.parse(r.get(SRC.Raid.creationDate))
								.plusSeconds(length - minTimeLeft)))
			|| (!ic && !enabled)
			|| (!ic && (loy < minLoy || loy > maxLoy))
			|| fav < 0
			) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		if(change[slot]) {
			if(first)
				switchCap(slot, dungeon, r, tdn, noCap, first, null);
			else
				change[slot] = false;
		}
		
	}
	
	private Hashtable<String, LocalDateTime> banned = new Hashtable<>();
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap, boolean first, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			switchCap(slot, dungeon, r, disname, noCap, overrideBanTime);
		} catch (NoCapMatchesException e) {
			if(!noCap) {
				beh.updateCaps(true, dungeon);
				captain(slot, first, true);
			} else {
				Debug.print("Run -> switchCap: slot="+slot+", err=No Captain Matches Config", Debug.runerr, Debug.error, pn, slot, true);
				throw e;
			}
		}
	}
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {

		LocalDateTime now = Time.parse(beh.getServerTime());
		
		if(!(r == null || disname == null)) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate));
			long plus;
			if(overrideBanTime != null)
				plus = overrideBanTime;
			else if(r.isVersus()) 
				plus = 420;
			else if(r.isDungeon())
				plus = 420;
			else
				plus = 2100;
			if(now.isAfter(start.plusSeconds(plus-120)))
				banned.put(disname, now.plusSeconds(120));
			else
				banned.put(disname, start.plusSeconds(plus));
			Debug.print("blocked " + disname, Debug.caps, Debug.info, pn, slot);
		}
		
		
		HashSet<String> removed = new HashSet<>();
		for(String key : banned.keySet().toArray(new String[banned.size()])) {
			if(now.isAfter(banned.get(key))) {
				removed.add(key);
				banned.remove(key);
			}
		}
		Debug.print("unblocked " + removed.toString(), Debug.caps, Debug.info, pn, slot);

		Captain[] caps = beh.getCaps(dungeon);
		Debug.print("got caps " + Arrays.toString(caps), Debug.caps, Debug.info, pn, slot);
		
		ListType list = dungeon
				? ConfigsV2.dungeon 
				: ConfigsV2.campaign;

		Raid[] all = beh.getRaids(SRC.BackEndHandler.all);
		ArrayList<String> otherCaps = new ArrayList<>();
		for(Raid raid : all) {
			if(raid == null)
				continue;
			otherCaps.add(raid.get(SRC.Raid.twitchDisplayName));
		}
		
		
		Captain best = null;
		int val = -1;
		int loy = 0;
		
		HashSet<String> skipped = new HashSet<>();
		
		for(int i=0; i<caps.length; i++) {
			String tdn = caps[i].get(SRC.Captain.twitchDisplayName);
			Integer fav = ConfigsV2.getCapInt(cid, currentLayer, tdn, list, ConfigsV2.fav);
			fav = fav == null ? 0 : fav;
			if(fav < 0 || banned.containsKey(tdn) || otherCaps.contains(tdn)) {
				skipped.add(tdn);
				continue;
			}
			int nloy = Integer.parseInt(caps[i].get(SRC.Captain.pveWins));
			if(fav > val || (fav == val && nloy > loy)) {
				best = caps[i];
				val = fav;
				loy = nloy;
				continue;
			}
		}
		
		Debug.print("skipped " + skipped.toString(), Debug.caps, Debug.info, pn, slot);
		
		if(best == null) 
			throw new NoCapMatchesException();
		
		beh.switchRaid(best, slot);
		
		Debug.print("switched to " + best.get(SRC.Captain.twitchDisplayName), Debug.caps, Debug.info, pn, slot);
		
		captain(slot, false, noCap);
	}

	
	private boolean goMultiChestClaim;
	
	private void chest(int slot) throws NoConnectionException, NotAuthorizedException {
		if(!beh.isReward(slot))
			return;
		if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiChestExploit)) {
			goMultiChestClaim = false;
			for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						while(!goMultiChestClaim) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							JsonObject rews = beh.getChest(slot);
							if(rews == null)
								return;
							for(String rew : rews.keySet())
								addRew(SRC.Run.chests, rew, rews.get(rew).getAsInt());
						} catch (NoConnectionException | NotAuthorizedException e) {}
					}
				});
				t.start();
			}
			goMultiChestClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			JsonObject rews = beh.getChest(slot);
			if(rews == null)
				return;
			for(String rew : rews.keySet())
				addRew(SRC.Run.chests, rew, rews.get(rew).getAsInt());
		}
		
	}

	private void upgrade() throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(pn, Store.gold, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.upgradeMinGold))
			return;
		
		Unit[] us = beh.getUnits(pn, SRC.BackEndHandler.isUnitUpgradeable, false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, us[i].get(SRC.Unit.unitType), ConfigsV2.upgrade);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = beh.upgradeUnit(us[ind], ConfigsV2.getUnitString(cid, currentLayer, us[ind].get(SRC.Unit.unitType), ConfigsV2.spec));
			if(err != null && (!(err.equals("no specUID") || err.equals("cant upgrade unit")))) {
				Debug.print("Run -> upgradeUnits: type=" + us[ind].get(SRC.Unit.unitType) + " err=" + err, Debug.lowerr, Debug.error, pn, 4, true);
				break;
			}
			
			ps[ind] = -1;
		}
	}

	private boolean goMultiUnit;
	
	private void unlock() throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(pn, Store.gold, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unlockMinGold))
			return;
		
		Unit[] unlockable = beh.getUnits(pn, SRC.BackEndHandler.isUnitUnlockable, true);
		if(unlockable.length == 0)
			return;
		
		int[] ps = new int[unlockable.length];
		for(int i=0; i<unlockable.length; i++)
			ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, unlockable[i].get(SRC.Unit.unitType), unlockable[i].isDupe() ? ConfigsV2.dupe : ConfigsV2.unlock);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind])
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			if(!beh.canUnlockUnit(unlockable[ind])) {
				ps[ind] = -1;
				continue;
			}
			
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiUnitExploit)) {
				goMultiUnit = false;
				final Unit picked = unlockable[ind];
				for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiUnit) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								beh.unlockUnit(picked);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiUnit = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				beh.updateStore(pn, true);
			} else {
				String err = beh.unlockUnit(unlockable[ind]);
				if(err != null && !err.equals("not enough gold")) 
					Debug.print("Run -> unlock: type=" + unlockable[ind].get(SRC.Unit.unitType) + ", err=" + err, Debug.lowerr, Debug.error, pn, 4, true);
			}
			
			ps[ind] = -1;
		}
	}

	private void store() throws NoConnectionException, NotAuthorizedException {
		//TODO better store
		beh.updateStore(pn, false);
		
		//	collecting daily reward if any
		List<Item> daily = beh.getStoreItems(SRC.Store.purchasable, SRC.Store.daily);
		for(Item item : daily) {
			JsonElement err = beh.buyItem(item).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				Debug.print("Run -> store -> daily: err="+err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);
			else
				addRew(SRC.Run.bought, Store.eventcurrency.get(), item.getQuantity());
		}
		
		//	buying from dungeon store if available
		for(int sec : new int[] {0,1}) {
			final String section;
			final StorePrioType spt;
			switch(sec) {
			case 0:
				if(beh.getCurrency(pn, Store.keys, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.storeMinKeys))
					continue;
				section = SRC.Store.dungeon;
				spt = ConfigsV2.keys;
				break;
			case 1:
				section = SRC.Store.Event;
				spt = ConfigsV2.event;
				break;
			default:
				//	not gonna happen but important for compiler
				section = null;
				spt = null;
			}
			
			List<Item> items = beh.getAvailableEventStoreItems(section, false);
			Item best = null;
			int p = -1;
			for(Item item : items) {
				int p_ = ConfigsV2.getStorePrioInt(cid, currentLayer, spt, item.getStr("Uid"));
				if(p_ > p) {
					best = item;
					p = p_;
				}
			}
			if(p < 0)
				continue;
			
			JsonObject resp = beh.buyItem(best);
			
			JsonElement err = resp.get(SRC.errorMessage);
			if(err == null || !err.isJsonPrimitive()) {
				switch(resp.get("buyType").getAsString()) {
				case "item":
					addRew(SRC.Run.bought, best.getItem(), best.getQuantity());
					break;
				case "chest":
					addRew(SRC.Run.bought, "dungeonchest", 1);
					JsonArray data = resp.getAsJsonObject("data").getAsJsonArray("rewards");
					Raid.updateChestRews();
					for(int i=0; i<data.size(); i++) {
						Reward rew = new Reward(data.get(i).getAsString());
						addRew(SRC.Run.bought, rew.name, rew.quantity);
					}
					break;
				case "skin":
					addRew(SRC.Run.bought, "skin", 1);
					break;
				default:
					Debug.print("Run -> store -> buyItem: err=unknown buyType, buyType="+resp.get("buyType").getAsString()+", item="+best.toString(), Debug.runerr, Debug.error, pn, 4, true);
				}
			} else if(!err.getAsString().startsWith("not enough "))
				Debug.print("Run -> store -> buyItem: err="+err.getAsString()+", item="+best.toString(), Debug.runerr, Debug.error, pn, 4, true);
		}
		
		
		
		if(beh.getCurrency(pn, Store.gold, false) >= ConfigsV2.getInt(cid, currentLayer, ConfigsV2.scrollsMinGold)) {
			List<Item> items = beh.getStoreItems(SRC.Store.purchasable, SRC.Store.scrolls);
			if(items.size() != 0) {
				int[] ps = new int[items.size()];
				for(int i=0; i<items.size(); i++) {
					Item item = items.get(i);
					String type = item.getItem().replace("scroll", "");
					
					//	switch if sr decides to add more units with allies
					switch(type) {
					case "paladin":
						type = "allies" + type;
					}
					
					if(type.equals("eventcurrency"))
						ps[i] = Integer.MAX_VALUE;
					else {
						try {
							ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, type, ConfigsV2.buy);
						} catch (NullPointerException e) {
							Debug.printException("Run -> store: err=item is not correct, item=" + item.toStringOneLine(), e, Debug.runerr, Debug.error, pn, 4, true);
							ps[i] = -1;
						}
					}
				}
				
				
				while(true) {
					int ind = 0;
					for(int i=1; i<ps.length; i++)
						if(ps[i] > ps[ind]) 
							ind = i;
					
					if(ps[ind] < 0)
						break;
					
					Item item = items.get(ind);
					
					//TODO make better
					String err = beh.buyStoreItem(item);
					if(err == null)
						addRew(SRC.Run.bought, item.getItem(), item.getQuantity());
					else if(!err.equals("not enough gold"))
						Debug.print("Run -> store: err=" + err + ", item=" + item.toString(), Debug.lowerr, Debug.error, pn, 4, true);
					
					ps[ind] = -1;
				}
			}
			
			Integer gold = beh.getCurrency(pn, Store.gold, false);
			if(gold != null) {
				int src = beh.getStoreRefreshCount();
				int min = ConfigsV2.getStoreRefreshInt(cid, currentLayer, src > 3 ? 3 : src);
				if(min > -1 && min < gold) {
					String err = beh.refreshStore();
					if(err != null)
						Debug.print("Run -> Store: err="+err, Debug.runerr, Debug.error, pn, 4, true);
					store();
				}
			}
			
		}
	}

	private boolean goMultiQuestClaim;
	
	private void quest() throws NoConnectionException, NotAuthorizedException {
		Quest[] quests = beh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiQuestExploit)) {
				goMultiQuestClaim = false;
				final Quest picked = quests[i];
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiQuestClaim) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								beh.claimQuest(picked);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiQuestClaim = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
			} else {
				JsonObject dat = beh.claimQuest(quests[i]);
				JsonElement err = dat.get(SRC.errorMessage);
				if(err.isJsonPrimitive())
					Debug.print("Run -> claimQuests: err=" + err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);
				else {
					dat = dat.getAsJsonObject("data").getAsJsonObject("rewardData");
					String item = dat.get("ItemId").getAsString();
					if(item.equals("goldpiecebag"))
						item = Store.gold.get();
					else if(item.startsWith("skin"))
						item = "skin";
					else if(!item.startsWith("scroll") && !item.equals("eventcurrency")) {
						Debug.print("Run -> claimQuests: err=unknown reward, item="+item, Debug.lowerr, Debug.error, pn, null, true);
						return;
					}
					int a = dat.get("Amount").getAsInt();
					addRew(SRC.Run.event, item, a);
				}
			}
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	private void event() throws NoConnectionException, NotAuthorizedException {
		if(!beh.isEvent())
			return;
		
		boolean bp = beh.hasBattlePass();
		int tier = beh.getEventTier();
		for(int i=1; i<=tier; i++) {
			if(bp)
				collectEvent(i, true);
			
			if(potionsTiers.contains(i) && beh.getCurrency(pn, Store.potions, false) > 10)
				continue;
			
			collectEvent(i, false);
		}
	}
	
	private boolean goMultiEventClaim;
	
	private void collectEvent(int tier, boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!beh.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiEventExploit)) {
			goMultiEventClaim = false;
			for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						while(!goMultiEventClaim) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							JsonObject ce = beh.collectEvent(tier, bp);
							JsonElement err = ce.get(SRC.errorMessage);
							if(err == null || !err.isJsonPrimitive()) {
								String rew = ce.get("reward").getAsString();
								if(!rew.equals("badges"))
									addRew(SRC.Run.event, rew, ce.get("quantity").getAsInt());
							}
						} catch (NoConnectionException e) {
						} catch (NullPointerException e) {
							Debug.print("Run -> event -> collectEvent(exploit): err=failed to collectEvent(exploit), tier="+tier+", bp="+bp, Debug.runerr, Debug.error, pn, 4, true);
						}
						
					}
				});
				t.start();
			}
			goMultiEventClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			JsonObject ce = beh.collectEvent(tier, bp);
			JsonElement err = ce.get(SRC.errorMessage);
			if(err != null && err.isJsonPrimitive()) {
				Debug.print("Run -> event -> collectEvent: tier="+tier+", bp="+bp+", err=" + err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);
			} else {
				String rew = ce.get("reward").getAsString();
				if(!rew.equals("badges"))
					addRew(SRC.Run.event, rew, ce.get("quantity").getAsInt());
			}
		}
		
		
		
	}

	public String getCurrentLayer() {
		return currentLayer;
	}
	
	private static final C[] sc = new C[] {Store.gold, Store.potions, Store.meat, Store.eventcurrency, Store.keys, Store.bones};
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	synchronized public void updateFrame() throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;
		
		updateLayer();
		
		Raid[] raids = beh.getRaids(SRC.BackEndHandler.all);
		
		for(int i=0; i<raids.length; i++) {
			
			if(raids[i] == null) {
				cnames[i] = null;
				cnames[i+4] = null;
				cnames[i+8] = null;
			} else {
				cnames[i] = raids[i].get(SRC.Raid.twitchUserName);
				cnames[i+4] = raids[i].get(SRC.Raid.twitchDisplayName);
				cnames[i+8] = raids[i].isDungeon() ? "d" : "c";
			}

			Manager.blis.onProfileUpdateSlot(cid, i, raids[i], ConfigsV2.isSlotLocked(cid, currentLayer, ""+i), change[i]);
			
		}
		
		for(C key : sc)
			Manager.blis.onProfileUpdateCurrency(cid, key.get(), beh.getCurrency(pn, key, false));
		
	}
	
	public synchronized void updateLayer() {
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units (1 = 5 min)
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);

		// set current layer
		JsonObject ptimes = ConfigsV2.getPObj(cid, ConfigsV2.ptimes);
		for(String key : ptimes.keySet()) {
			String[] sp = key.split("-");
			if(Integer.parseInt(sp[0]) <= n && Integer.parseInt(sp[1]) >= n) {
				if(key.equals(currentLayerId))
					break;
				currentLayer = ptimes.get(key).getAsString();
				currentLayerId = key;
				updateProxySettings();
				break;
			}
		}
		
		Manager.blis.onProfileUpdateGeneral(cid, pn, ConfigsV2.getStr(cid, currentLayer, ConfigsV2.lname), new Color(ConfigsV2.getInt(cid, currentLayer, ConfigsV2.color)));
		
		beh.setUpdateTimes( ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unitUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.raidUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.mapUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.storeUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.questEventRewardsUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capsUpdate),
							ConfigsV2.getInt(cid, currentLayer, ConfigsV2.skinUpdate));
	}
	
	
	private String[] cnames = new String[12];
	
	public String getTwitchLink(int slot) {
		return "https://twitch.tv/"+cnames[slot];
	}
	
	public Map getMap(int slot) throws NoConnectionException, NotAuthorizedException {
		return beh.getMap(pn, slot, false);
	}
	
	public void updateProxySettings() {
		String proxy = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyDomain);
		String user = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyUser);
		beh.setOptions(proxy.equals("") ? null : proxy, 
				ConfigsV2.getInt(cid, currentLayer, ConfigsV2.proxyPort),
				user.equals("") ? null : user,
				ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyPass),
				ConfigsV2.getStr(cid, currentLayer, ConfigsV2.userAgent),
				ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.proxyMandatory));
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
