package run;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.LinkedList;
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

import include.Json;
import include.Maths;
import include.Pathfinding;
import include.Time;
import program.Configs;
import program.Remaper;
import program.SRC;
import program.SRR;
import program.Store;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;

import program.Store.C;
import program.Store.Item;
import program.viewer.CaptainData;
import program.viewer.Raid;
import program.viewer.Raid.Reward;
import run.AbstractBackEnd.UpdateEventListener;
import program.Unit;
import program.Configs.ListType;
import program.Configs.StorePrioType;
import program.MapConv.NoFinException;
import program.Logger;
import program.Heatmap;
import program.Map;
import program.MapConv;
import program.Options;
import program.Quests.Quest;

public class Viewer extends AbstractProfile<Viewer.ViewerBackEndRunnable,ViewerBackEnd> {
	
	public static interface ViewerBackEndRunnable extends AbstractProfile.BackEndRunnable<ViewerBackEnd> {}
	
	private boolean[] isRunning = new boolean[5];
	private boolean[] isActiveRunning = new boolean[5];
	private boolean[] change = new boolean[4];
	private int[] sleep = new int[5];
	
	private JsonObject rews = null;
	
	private static final String[] rew_sources = "chests bought event".split(" ");
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestboostedscroll chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "dungeonchests eventchests".split(" ");
	private static String[] rew_types;
	
	private static String[] genRewTypes() {
		ArrayList<String> ret = new ArrayList<String>(Arrays.asList("gold potions token eventcurrency keys meat bones skin".split(" ")));
		ArrayList<String> utypes = Unit.getTypesList();
		for(int i=0; i<utypes.size(); i++)
			utypes.set(i, "scroll"+utypes.get(i).replace("allies", ""));
		ret.addAll(utypes);
		return ret.toArray(new String[ret.size()]);
	}
	
	public void updateRews() {
		rew_types = genRewTypes();
		if(rews == null)
			rews = new JsonObject();
		for(String s : rew_sources) {
			if(!rews.has(s))
				rews.add(s, new JsonObject());
			JsonObject source = rews.getAsJsonObject(s);
			switch(s) {
			case "chests":
				for(String t : rew_chests_chests)
					if(!source.has(t))
						source.addProperty(t, 0);
				break;
			case "bought":
				for(String t : rew_bought_chests)
					if(!source.has(t))
						source.addProperty(t, 0);
				break;
			}
			for(String t : rew_types)
				if(!source.has(t))
					source.addProperty(t, 0);
		}
	}
	
	synchronized public void addRew(ViewerBackEnd beh, String con, String type, int amount) {
		type = Remaper.map(type).replace(Options.get("currentEventCurrency"), Store.eventcurrency.get());
		try {
			JsonObject r = rews.getAsJsonObject(con);
			r.addProperty(type, r.get(type).getAsInt() + amount);
			beh.addCurrency(type, amount);
		} catch (NullPointerException e) {
			Logger.printException("Run -> addRew: err=failed to add reward, con=" + con + ", type=" + type + ", amount=" + amount, e, Logger.runerr, Logger.error, cid, null, true);
		}
	}
	
	public JsonObject getRews() {
		return rews;
	}
	
	@Override
	public void saveStats() {
		JsonObject astats = Configs.getUObj(cid, Configs.statsViewer);
		for(String s : rews.keySet()) {
			JsonObject stats = astats.getAsJsonObject(s);
			JsonObject rew = rews.getAsJsonObject(s);
			for(String v : rew.keySet()) {
				try {
					stats.addProperty(v, stats.get(v).getAsInt() + rew.get(v).getAsInt());
				} catch (NullPointerException e) {
					Logger.print("Viewer -> saveStats: err=unknown stat, v="+v+", s="+s, Logger.general, Logger.error, cid, null, true);
					stats.addProperty(v, rew.get(v).getAsInt());
				}
				rew.addProperty(v, 0);
			}
		}
	}
	
	
	public Viewer(String cid, SRR req) throws Exception {
		super(cid, new ViewerBackEnd(cid, req), ProfileType.VIEWER);
		uelis = new UpdateEventListener<ViewerBackEnd>() {
			@Override
			public void afterUpdate(String obj, ViewerBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				boolean dungeon = false;
				switch(obj) {
				case "caps::true":
					dungeon = true;
					//$FALL-THROUGH$
				case "caps::false":
					try {
						CaptainData[] caps;
						caps = vbe.getCaps(dungeon);
						HashSet<String> got = new HashSet<>();
						for(CaptainData c : caps)
							got.add(c.get(SRC.Captain.twitchDisplayName));
						
						HashSet<String> favs = Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign);
						for(String tdn : favs) {
							if(got.contains(tdn) || !Configs.getCapBoo(cid, currentLayer, tdn, dungeon ? Configs.dungeon : Configs.campaign, Configs.il))
								continue;
							
							JsonArray results = vbe.searchCap(1, null, false, false, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, true, tdn);
							if(results.size() == 0)
								continue;
							
							CaptainData n = new CaptainData(results.get(0).getAsJsonObject());
							
							if(n.get(SRC.Captain.isPlaying).equals("1"))
								caps = add(caps, n);
						}
						vbe.setCaps(caps, dungeon);
					} catch(Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve caps", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				case "units":
					try {
						Unit[] units = vbe.getUnits(SRC.BackEndHandler.all, false);
						for(Unit u : units)
							Configs.addUnitId(cid, ProfileType.VIEWER, u.unitId, u.unitType, Integer.parseInt(u.get(SRC.Unit.level)));
					} catch (Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve units", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				}
			}
		};
		useBackEnd(vbe -> {
			vbe.setUpdateEventListener(uelis);
			vbe.ini();
			raids = vbe.getRaids(SRC.BackEndHandler.all);
			curs = vbe.getCurrencies();
		});
		updateRews();
	}

	
	private List<List<Boolean>> queue = Collections.synchronizedList(new LinkedList<List<Boolean>>() {
		private static final long serialVersionUID = 1L;
		{
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
		}
	});
	
	private boolean[] setRun = new boolean[5];
	
	@Override
	public void setRunningAll(boolean b) {
		for(int i=0; i<5; i++)
			setRunning(b, i);
	}
	
	@Override
	public void setRunning(boolean bb, int slot) {
		if(Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.syncSlotViewer) != -1)
			bb = false;
		final boolean b_ = bb;
		new Thread(() -> {
			List<Boolean> q = queue.get(slot);
			q.add(b_);
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
		).start();
	}
	
	@Override
	public boolean isRunning(int slot) {
		return isRunning[slot];
	}
	
	@Override
	public boolean hasStopped() {
		for(boolean b : isActiveRunning)
			if(b) return false;
		return true;
	}

	@Override
	public void skip(int slot) {
		sleep[slot] = 0;
	}
	
	@Override
	public void skipAll() {
		for(int i=0; i<5; i++)
			skip(i);
	}
	
	public void change(int slot) {
		change[slot] = !change[slot];
		try {
			updateFrame(null);
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException(cid+": Run -> change: slot="+slot+", err=failed to update Frame", e, Logger.runerr, Logger.error, cid, slot, true);
		}
	}
	
	public void fav(int slot, int val) {
		String cap = cnames[slot+4];
		if(cap == null)
			return;
		
		Integer v = Configs.getCapInt(cid, "(all)", cap, cnames[slot+8].equals("d") ? Configs.dungeon : Configs.campaign, Configs.fav);
		if(v == null 
				|| v == Integer.MAX_VALUE-1
				|| v == Integer.MIN_VALUE+1
				|| Math.signum(val)*Math.signum(v) <= 0)
			Configs.favCap(cid, "(all)", cap, Configs.all, val);
		else 
			Configs.favCap(cid, "(all)", cap, Configs.all, null);
	}
	
	
	synchronized private void slotSequence(int slot) {

		updateLayer();
		updateSlotSync();
		if(!isRunning(slot))
			return;
		
		Logger.print("requesting action", Logger.general, Logger.info, cid, slot);
		try {
			Manager.requestAction();
		} catch (InterruptedException e1) {
			Logger.print("action rejected", Logger.general, Logger.info, cid, slot);
			return;
		}
		
		
		try {
			useBackEnd(vbe -> {
				updateBeh(vbe);
				doSlot(vbe, slot);
				
				for(int i=0; i<5; i++)
					if(Configs.getSleepInt(cid, currentLayer, ""+i, Configs.syncSlotViewer) == slot)
						doSlot(vbe, i);

				Logger.print("updateFrame", Logger.general, Logger.info, cid, slot);
				updateFrame(vbe);
			});
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("Run -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Logger.runerr, Logger.fatal, cid, slot, true);
		} catch (StreamRaidersException | NoCapMatchesException e) {
		} catch (Exception e) {
			Logger.printException("Run -> slotSequence: slot=" + slot + " err=unknown", e, Logger.runerr, Logger.fatal, cid, slot, true);
		}
		
		
		Logger.print("releasing action", Logger.general, Logger.info, cid, slot);
		Manager.releaseAction();
		
		LocalDateTime now_ldt = LocalDateTime.now();
		// current time in layer-units
		int now = ((now_ldt.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now_ldt.getHour() * 12) 
				+ (now_ldt.getMinute() / 5);
		
		
		// --- calculate time to sleep ---
		
		JsonObject ptimes = Configs.getUObj(cid, Configs.ptimesViewer);
		int end = Integer.parseInt(currentLayerId.split("-")[1]);
		
		int min = Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.minViewer);
		int max = Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.maxViewer);
		
		int w = -1;
		
		
		// test if sleep is not possible before next layer
		if_clause:
		if(min < 0 || max < 0 || now+(min/300) > end) {
			// change layer
			//	loop multiple times to be sure that it finds the next layer
			for(int i=0; i<ptimes.size(); i++) {
				// loop until first layer after current which is not disabled
				for(String t : ptimes.keySet()) {
					int start = Integer.parseInt(t.split("-")[0]);
					if(start != (end == 2015 ? 0 : end+1))
						continue;
					
					if(Configs.getSleepInt(cid, ptimes.get(t).getAsString(), ""+slot, Configs.minViewer) < 0 ||
							Configs.getSleepInt(cid, ptimes.get(t).getAsString(), ""+slot, Configs.maxViewer) < 0) {
						end = Integer.parseInt(t.split("-")[1]);
						continue;
					}

					// shift start if before now
					if(start < now)
						start += 2016;
					
					// calculate time until next layer which is not disabled
					w = (start-now)*300;
					break if_clause;
				}
			}
		} else {
			// test if max is still in same layer or else set max to end time of layer
			if(now+(max/300) >= end)
				max = (end-now)*300;
			// generate random sleep-time
			w = Maths.ranInt(min, max);
		}
		
		if(w > -1) {
			Logger.print("sleeping "+w+" sec", Logger.general, Logger.info, cid, slot);
			sleep(w, slot);
		} else {
			Logger.print("Run -> slotSequence: err=couldn't find wait time", Logger.runerr, Logger.fatal, cid, slot, true);
			Manager.blis.onProfileChangedRunning(cid, slot, false);
			isActiveRunning[slot] = false;
			isRunning[slot] = false;
		}
		
		Logger.print("before MemoryReleaser", Logger.general, Logger.info, cid, slot);
		synchronized (gclock) {
			if(Configs.getGBoo(Configs.useMemoryReleaser) && now_ldt.isAfter(gcwait)) {
				System.gc();
				gcwait = now_ldt.plusSeconds(30);
			}
		}
		Logger.print("after MemoryReleaser", Logger.general, Logger.info, cid, slot);
		
	}
	
	private void doSlot(ViewerBackEnd beh, int slot) throws Exception {
		if_clause:
		if(slot == 4) {
			Logger.print("event", Logger.general, Logger.info, cid, slot);
			event(beh);

			Logger.print("quest", Logger.general, Logger.info, cid, slot);
			quest(beh);

			Logger.print("store", Logger.general, Logger.info, cid, slot);
			store(beh);

			Logger.print("unlock", Logger.general, Logger.info, cid, slot);
			unlock(beh);

			Logger.print("upgrade", Logger.general, Logger.info, cid, slot);
			upgrade(beh);
			
			Logger.print("grantExtraRewards", Logger.general, Logger.info, cid, slot);
			beh.grantTeamReward();
			beh.grantEventQuestMilestoneReward();
			
		} else {
			
			if(!canUseSlot(beh, slot))
				break if_clause;

			Logger.print("chest", Logger.general, Logger.info, cid, slot);
			chest(beh, slot);

			Logger.print("captain", Logger.general, Logger.info, cid, slot);
			captain(beh, slot);

			Logger.print("place", Logger.general, Logger.info, cid, slot);
			place(beh, slot);
			
		}
	}
	
	private static LocalDateTime gcwait = LocalDateTime.now();
	private static final Object gclock = new Object();
	
	
	public boolean canUseSlot(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException {
		int uCount = beh.getUnits(SRC.BackEndHandler.all, false).length;
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
				for(int i=0; i<5; i++)
					if(Configs.getSleepInt(cid, currentLayer, ""+i, Configs.syncSlotViewer) == slot)
						Manager.blis.onProfileTimerUpdate(cid, i, ms);
				
				sleep[slot]--;
				
				if(sleep[slot] < 0) {
					t.cancel();
					slotSequence(slot);
				}
			}
		}, 0, 1000);
	}

	private boolean goMultiPlace;
	private LocalDateTime placeTime = LocalDateTime.now();
	
	private void place(final ViewerBackEnd vbe, final int slot) throws NoConnectionException, NotAuthorizedException {
		//	Unit place delay
		long tdif = ChronoUnit.MILLIS.between(LocalDateTime.now(), placeTime);
		if(tdif > 0) {
			try {
				Thread.sleep(tdif);
			} catch (InterruptedException e) {}
		}
		
		Raid r = vbe.getRaid(slot, true);
		if(r == null)
			return;

		if(r.isVersus())
			return;
		
		String placeSer = r.get(SRC.Raid.placementsSerialized);
		if(!r.canPlaceUnit(Manager.getServerTime())
			|| Configs.getInt(cid, currentLayer, Configs.maxUnitPerRaidViewer) < (placeSer == null 
																					? 0 
																					: placeSer.split(vbe.getViewerUserId()).length-1))
			return;
		
		
		final boolean dungeon = r.isDungeon();
		
		//	check place settings (eg.: min loy/room) before placement (bcs of locked slots)
		
		String tdn = r.get(SRC.Raid.twitchDisplayName);
		Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, dungeon ? Configs.dungeon : Configs.campaign, Configs.ic);
		if(ic == null)
			ic = false;
		
		String ct = r.getFromNode(SRC.MapNode.chestType);
		if(ct == null)
			return;
		ct = Remaper.map(ct);
		
		//	check if epic is available
		boolean epic;
		int dunLvl = -1;
		int loy;
		int length;
		if(dungeon) {
			vbe.addUserDungeonInfo(r);
			JsonObject udi = r.getUserDungeonInfo();
			epic = udi.get("epicChargesUsed").getAsInt() == 0;
			loy = udi.get("completedLevels").getAsInt() + 1;
			if(epic)
				dunLvl = loy % 3;
			
			length = 360;
		} else {
			Integer potionsc = vbe.getCurrency(Store.potions, true);
			epic = potionsc == null ? false : (potionsc >= 45);
			
			loy = Integer.parseInt(r.get(SRC.Raid.pveWins));
			
			length = 1800;
		}

		boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		
		int maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		int minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
		
		int maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		int minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		maxTimeLeft = length - maxTimeLeft;
		
		if((dungeon ^ Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot))
				|| (!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusSeconds(maxTimeLeft), Manager.getServerTime()))
				||(!ic && Time.isAfter(Manager.getServerTime(),
						Time.parse(r.get(SRC.Raid.creationDate))
						.plusSeconds(length - minTimeLeft)))
				|| (!ic && !enabled)
				|| (!ic && (loy < minLoy || loy > maxLoy))
				) {
			return;
		}
		
		Map map = vbe.getMap(slot, true);
		
		if(!Configs.getBoolean(cid, currentLayer, Configs.allowPlaceFirstViewer) 
			&& (placeSer == null ? true : Json.parseArr(placeSer).size() == 0))
			return;
		
		boolean dunNeeded = false;
		if(dungeon) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate)).plusMinutes(1);
			LocalDateTime now = Time.parse(Manager.getServerTime());
			long t = ChronoUnit.SECONDS.between(start, now);
			double tp = (100 * t) / 300.0;
			
			double mp = map.mapPower;
			int pp = map.getPlayerPower();
			
			double pd = (100 * pp) / mp;
			
			dunNeeded = tp > pd + 10;
			Logger.print("t="+t+" tp="+tp+" mp="+mp+" pp="+pp+" pd="+pd, Logger.place, Logger.info, cid, slot);
		}
		
		final Unit[] units = vbe.getPlaceableUnits(r);
		Logger.print("units="+Arrays.toString(units), Logger.units, Logger.info, cid, slot);
		
		int[] mh = new Heatmap().getMaxHeat(map);
		
		int re = 0;
		int retries = Configs.getInt(cid, currentLayer, Configs.unitPlaceRetriesViewer);
		int reload = Configs.getInt(cid, currentLayer, Configs.mapReloadAfterXRetriesViewer);
		HashSet<String> bannedPos = new HashSet<>();
		
		List<String> neededUnits = vbe.getNeededUnitTypesForQuests();
		
		if(Configs.getBoolean(cid, currentLayer, Configs.preferRoguesOnTreasureMapsViewer) 
			&& r.getFromNode(SRC.MapNode.nodeType).contains("treasure")
			) {
			neededUnits.add("rogue");
			neededUnits.add("flyingarcher");
		}
		
		Logger.print("neededUnits="+neededUnits, Logger.units, Logger.info, cid, slot);
		
		while(true) {
			Logger.print("place "+re, Logger.loop, Logger.info, cid, slot);
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiPlaceExploitViewer)) {
				goMultiPlace = false;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					final Place pla = findPlace(map, mh, bannedPos, neededUnits, units, epic, dungeon, dunLvl, dunNeeded, r.getFromNode(SRC.MapNode.chestType),
							Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign).contains(r.get(SRC.Raid.twitchDisplayName)), slot);
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
								vbe.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
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
				boolean fav = Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign).contains(r.get(SRC.Raid.twitchDisplayName));
				final Place pla = findPlace(map, mh, bannedPos, neededUnits, units, epic, dungeon, dunLvl, dunNeeded, node, fav, slot);
				if(pla == null) {
					Logger.print("Place=null", Logger.units, Logger.info, cid, slot);
					System.out.println("place=null, "+Configs.getPStr(cid, Configs.pname)+" - "+slot);
					break;
				}
				Logger.print("Place="+pla.toString(), Logger.place, Logger.info, cid, slot);
				String err = vbe.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
				bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
				
				if(err == null) {
					if(pla.epic && !dungeon)
						vbe.decreaseCurrency(Store.potions, 45);
					
					vbe.addCurrency(Store.potions, 1);
					String ut = pla.unit.unitType;
					if(!Unit.isLegendary(ut))
						vbe.addCurrency(pla.unit.unitType, 1);
					placeTime = LocalDateTime.now().plus(Maths.ranInt(
															Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMinViewer),
															Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMaxViewer)), 
														ChronoUnit.MILLIS);
					break;
				}
				
				if(err.equals("NOT_ENOUGH_POTIONS")) {
					vbe.setCurrency(Store.potions, 0);
					epic = false;
				}
				
				if(err.equals("PERIOD_ENDED"))
					break;
				
				if(re++ < retries) {
					if(re % reload == 0) {
						map = vbe.getMap(slot, true);
					}
					continue;
				}
				
				Logger.print("Run -> place: tdn="+r.toString()+" err="+err, Logger.lowerr, Logger.error, cid, slot, true);
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
			return new StringBuilder(unit.unitType)
					.append("|").append(pos[0]).append("-")
					.append(pos[1]).append(epic ? "|epic" : "").append(isOnPlan ? "|plan" : "")
					.toString();
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
			return "{" + unit.unitType + "|ps=" + Arrays.toString(ps) + "|vs=" + Arrays.toString(vs) + "}";
		}
	}
	
	private Place findPlace(Map map, int[] mh, HashSet<String> bannedPos, List<String> neededUnits, final Unit[] units, final boolean epic, final boolean dungeon, final int dunLvl, final boolean dunNeeded, final String chest, final boolean fav, final int slot) {
		HashSet<String> nupts = map.getUsablePlanTypes(false);
		HashSet<String> eupts = map.getUsablePlanTypes(true);
		
		Prio[] prios = new Prio[units.length];
		for(int i=0; i<units.length; i++) {
			final String uType = units[i].unitType;
			final String uId = units[i].unitId;
			
			int n = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.placedunViewer : Configs.placeViewer);
			int e = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.epicdunViewer : Configs.epicViewer);
			final String chests = Configs.getUnitString(cid, currentLayer, uId, Configs.chestsViewer);
			final String favOnly = Configs.getUnitString(cid, currentLayer, uId, Configs.favOnlyViewer);
			final String markerOnly = Configs.getUnitString(cid, currentLayer, uId, Configs.markerOnlyViewer);
			final String canVibe = Configs.getUnitString(cid, currentLayer, uId, Configs.canVibeViewer);
			
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
			
			if(neededUnits.contains(uType))
				n = Integer.MAX_VALUE;
			
			
			int np = -1;
			int ep = -1;
			
			HashSet<String> pts = units[i].getPlanTypes();
			pts.remove("vibe");
			for(String pt : pts) {
				if(eupts.contains(pt))
					ep = e;
				if(nupts.contains(pt))
					np = n;
			}
			

			boolean vn = false;
			boolean ve = false;
			

			if(np < 0 && nupts.contains("vibe") && canVibe.contains(nx)) {
				np = n;
				vn = true;
			}
			
			if(ep < 0 && eupts.contains("vibe") && canVibe.contains(ex)) {
				ep = e;
				ve = true;
			}
			
			if(markerOnly.contains(nx))
				n = -1;
			
			if(markerOnly.contains(ex))
				e = -1;
			
			if(dungeon) {
				String dem = Configs.getUnitString(cid, currentLayer, uId, Configs.dunEpicModeViewer);
				boolean ce = true;
				inner: {
					int nl;
					switch(dem) {
					case "ifNeeded":
						ce = dunNeeded;
						break inner;
					case "boss":
						nl = 0;
						break;
					case "first":
						nl = 1;
						break;
					case "second":
						nl = 2;
						break;
					default:
						nl = -1;
					}
					ce = nl == dunLvl;
				}
				if(!ce) {
					ep = -1;
					e = -1;
				}
			}
			
			prios[i] = new Prio(units[i], np, ep, n, e, vn, ve);
		}
		
		Logger.print("prios=" + Arrays.toString(prios), Logger.units, Logger.info, cid, slot);
		
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
					pos = new Pathfinding().search(new MapConv().asField(map, u.canFly(), pts, mh, bannedPos), cid, slot, i%2==0);
				} catch (NoFinException e) {}
				if(pos == null)
					continue;
				return new Place(u, pos, i%2==0, i<2);
			}
		}
		
		return null;
	}
	
	private void captain(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		captain(beh, slot, true, false);
	}

	private void captain(ViewerBackEnd vbe, int slot, boolean first, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		
		boolean dungeon = Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot);
		
		Raid r = vbe.getRaid(slot, true);
		
		if(r != null && Configs.isSlotLocked(cid, currentLayer, ""+slot)) {
			if(r.isDungeon() && !dungeon) {
				Raid[] all = vbe.getRaids(SRC.BackEndHandler.all);
				boolean change = true;
				for(int i=0; i<all.length; i++) {
					if(i == slot || all[i] == null)
						continue;
					if(all[i].isDungeon() && Configs.isSlotLocked(cid, currentLayer, ""+i))
						change = false;
				}
				if(change) {
					Configs.setStr(cid, currentLayer, Configs.dungeonSlotViewer, ""+slot);
					dungeon = true;
				}
			} else if(!r.isDungeon() && dungeon) {
				Configs.setStr(cid, currentLayer, Configs.dungeonSlotViewer, "(none)");
				dungeon = false;
			}
		}
		
		if(Configs.isSlotLocked(cid, currentLayer, ""+slot) && !change[slot])
			return;
		
		if(r == null) {
			switchCap(vbe, slot, dungeon, null, null, noCap, first, null);
			return;
		}
		
		if(!r.isSwitchable(Manager.getServerTime(), Configs.getInt(cid, currentLayer, Configs.capInactiveTresholdViewer)))
			return;

		String tdn = r.get(SRC.Raid.twitchDisplayName);
		if(r.isVersus()) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		String ct = r.getFromNode(SRC.MapNode.chestType);
		if(ct == null) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		ct = Remaper.map(ct);

		ListType list = dungeon ? Configs.dungeon : Configs.campaign;
		
		Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.ic);
		ic = ic == null ? false : ic;
		Boolean il = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.il);
		il = il == null ? false : il;
		Integer fav = Configs.getCapInt(cid, currentLayer, tdn, list, Configs.fav);
		fav = fav == null ? 0 : fav;
		
		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		Integer maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		Integer minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		Boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		Integer maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		Integer minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		if(maxLoy == null || minLoy == null || enabled == null || maxTimeLeft == null || minTimeLeft == null) {
			Logger.print("Run -> captain: ct="+ct+", err=failed to get chest config", Logger.runerr, Logger.error, cid, slot, true);
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
								Manager.getServerTime())) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, maxTimeLeft);
			return;
		}
		
		
		JsonArray users = Json.parseArr(r.get(SRC.Raid.users));
		if(users != null) {
			String uid = vbe.getViewerUserId();
			for(int i=0; i<users.size(); i++)
				if(users.get(i).getAsJsonObject().get("userId").getAsString().equals(uid))
					minTimeLeft = Integer.MIN_VALUE;
		}
		
		String capTeam = Configs.getStr(cid, currentLayer, Configs.captainTeamViewer); //TODO rem after event
		
		if((dungeon ^ r.isDungeon())
			|| !(ic || capTeam.equals("(none)")			//TODO rem after event
				|| capTeam.equals(r.get("teamUid")))	//TODO rem after event
			|| r.isOffline(Manager.getServerTime(), il, Configs.getInt(cid, currentLayer, Configs.capInactiveTresholdViewer))
			|| (!ic && Time.isAfter(Manager.getServerTime(),
							Time.parse(r.get(SRC.Raid.creationDate))
								.plusSeconds(length - minTimeLeft)))
			|| (!ic && !enabled)
			|| (!ic && (loy < minLoy || loy > maxLoy))
			|| fav < 0
			) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		if(change[slot]) {
			if(first)
				switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			else
				change[slot] = false;
		}
		
	}
	
	private Hashtable<String, LocalDateTime> banned = new Hashtable<>();
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(ViewerBackEnd beh, int slot, boolean dungeon, Raid r, String disname, boolean noCap, boolean first, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			switchCap(beh, slot, dungeon, r, disname, noCap, overrideBanTime);
		} catch (NoCapMatchesException e) {
			if(!noCap) {
				beh.updateCaps(true, dungeon);
				captain(beh, slot, first, true);
			} else {
				Logger.print("Run -> switchCap: slot="+slot+", err=No Captain Matches Config", Logger.runerr, Logger.error, cid, slot, true);
				throw e;
			}
		}
	}
	
	private void switchCap(ViewerBackEnd beh, int slot, boolean dungeon, Raid r, String disname, boolean noCap, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {

		LocalDateTime now = Time.parse(Manager.getServerTime());
		
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
			Logger.print("blocked " + disname, Logger.caps, Logger.info, cid, slot);
		}
		
		
		HashSet<String> removed = new HashSet<>();
		for(String key : banned.keySet().toArray(new String[banned.size()])) {
			if(now.isAfter(banned.get(key))) {
				removed.add(key);
				banned.remove(key);
			}
		}
		Logger.print("unblocked " + removed.toString(), Logger.caps, Logger.info, cid, slot);

		CaptainData[] caps = beh.getCaps(dungeon);
		Logger.print("got caps " + Arrays.toString(caps), Logger.caps, Logger.info, cid, slot);
		
		ListType list = dungeon
				? Configs.dungeon 
				: Configs.campaign;

		Raid[] all = beh.getRaids(SRC.BackEndHandler.all);
		ArrayList<String> otherCaps = new ArrayList<>();
		for(Raid raid : all) {
			if(raid == null)
				continue;
			otherCaps.add(raid.get(SRC.Raid.twitchDisplayName));
		}
		
		
		CaptainData best = null;
		int val = -1;
		int loy = 0;
		
		HashSet<String> skipped = new HashSet<>();
		
		String capTeam = Configs.getStr(cid, currentLayer, Configs.captainTeamViewer);	//TODO rem after event
		
		for(int i=0; i<caps.length; i++) {
			String tdn = caps[i].get(SRC.Captain.twitchDisplayName);

			Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.ic);	//TODO rem after event
			ic = ic == null ? false : ic;													//TODO rem after event
			
			Integer fav = Configs.getCapInt(cid, currentLayer, tdn, list, Configs.fav);
			fav = fav == null ? 0 : fav;
			if(fav < 0 
				|| banned.containsKey(tdn) 
				|| otherCaps.contains(tdn)
				|| !(ic || capTeam.equals("(none)")				//TODO rem after event
					|| capTeam.equals(caps[i].get("teamUid")))	//TODO rem after event
				) {
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
		
		Logger.print("skipped " + skipped.toString(), Logger.caps, Logger.info, cid, slot);
		
		if(best == null) 
			throw new NoCapMatchesException();
		
		beh.switchRaid(best, slot);
		
		Logger.print("switched to " + best.get(SRC.Captain.twitchDisplayName), Logger.caps, Logger.info, cid, slot);
		
		captain(beh, slot, false, noCap);
	}

	
	private boolean goMultiChestClaim;
	
	private void chest(ViewerBackEnd vbe, int slot) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.isReward(slot))
			return;
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiChestExploitViewer)) {
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
							JsonObject rews = vbe.getChest(slot);
							if(rews == null)
								return;
							for(String rew : rews.keySet())
								addRew(vbe, SRC.Run.chests, rew, rews.get(rew).getAsInt());
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
			JsonObject rews = vbe.getChest(slot);
			if(rews == null)
				return;
			for(String rew : rews.keySet())
				addRew(vbe, SRC.Run.chests, rew, rews.get(rew).getAsInt());
		}
		
	}

	private void upgrade(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < Configs.getInt(cid, currentLayer, Configs.upgradeMinGoldViewer))
			return;
		
		Unit[] us = beh.getUnits(SRC.BackEndHandler.isUnitUpgradeable, false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = Configs.getUnitInt(cid, currentLayer, us[i].unitId, Configs.upgradeViewer);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = beh.upgradeUnit(us[ind], Configs.getUnitSpec(cid, ProfileType.VIEWER, currentLayer, us[ind].unitId));
			if(err != null && (!(err.equals("no specUID") || err.equals("cant upgrade unit")))) {
				Logger.print("Run -> upgradeUnits: type=" + us[ind].unitType + " err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
				break;
			}
			
			ps[ind] = -1;
		}
	}

	private boolean goMultiUnit;
	
	private void unlock(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < Configs.getInt(cid, currentLayer, Configs.unlockMinGoldViewer))
			return;
		
		Unit[] unlockable = beh.getUnits(SRC.BackEndHandler.isUnitUnlockable, true);
		if(unlockable.length == 0)
			return;
		
		int[] ps = new int[unlockable.length];
		for(int i=0; i<unlockable.length; i++)
			ps[i] = Configs.getUnitInt(cid, currentLayer, unlockable[i].unitType, unlockable[i].dupe ? Configs.dupeViewer : Configs.unlockViewer);
		
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
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiUnitExploitViewer)) {
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
				beh.updateStore(true);
			} else {
				String err = beh.unlockUnit(unlockable[ind]);
				if(err != null && !err.equals("not enough gold")) 
					Logger.print("Run -> unlock: type=" + unlockable[ind].unitType + ", err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
			}
			
			ps[ind] = -1;
		}
	}

	private void store(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		beh.updateStore(false);
		
		//	collecting daily reward if any
		Item daily = beh.getDaily();
		if(daily != null) {
			JsonElement err = beh.buyItem(daily).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				Logger.print("Run -> store -> daily: err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
			else
				addRew(beh, SRC.Run.bought, Store.eventcurrency.get(), daily.quantity);
		}
		
		//	buying from dungeon(0) and event(1) store if available
		for(final int sec : new int[] {0,1}) {
			final String section;
			final StorePrioType spt;
			switch(sec) {
			case 0:
				if(beh.getCurrency(Store.keys, false) < Configs.getInt(cid, currentLayer, Configs.storeMinKeysViewer))
					continue;
				section = SRC.Store.dungeon;
				spt = Configs.keysViewer;
				break;
			case 1:
				if(beh.getCurrency(Store.eventcurrency, false) < Configs.getInt(cid, currentLayer, Configs.storeMinEventcurrencyViewer))
					continue;
				section = SRC.Store.event;
				spt = Configs.eventViewer;
				break;
			default:
				//	not gonna happen but important for compiler
				section = null;
				spt = null;
			}
			
			ArrayList<Item> items = beh.getAvailableEventStoreItems(section, false);
			Item best = null;
			int p = -1;
			for(Item item : items) {
				int p_ = Configs.getStorePrioInt(cid, currentLayer, spt, item.uid);
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
					addRew(beh, SRC.Run.bought, best.name, best.quantity);
					break;
				case "chest":
					addRew(beh, SRC.Run.bought, sec==0?"dungeonchests":"eventchests", 1);
					JsonArray data = resp.getAsJsonObject("data").getAsJsonArray("rewards");
					for(int i=0; i<data.size(); i++) {
						Reward rew = new Reward(data.get(i).getAsString(), cid, 4);
						addRew(beh, SRC.Run.bought, rew.name, rew.quantity);
					}
					break;
				case "skin":
					addRew(beh, SRC.Run.bought, "skin", 1);
					break;
				default:
					Logger.print("Run -> store -> buyItem: err=unknown buyType, buyType="+resp.get("buyType").getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
				}
			} else if(!err.getAsString().startsWith("not enough "))
				Logger.print("Run -> store -> buyItem: err="+err.getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
		}
		
		
		//	buying scrolls
		if(beh.getCurrency(Store.gold, false) >= Configs.getInt(cid, currentLayer, Configs.storeMinGoldViewer)) {
			ArrayList<Item> items = beh.getPurchasableScrolls();
			if(items.size() != 0) {
				int[] ps = new int[items.size()];
				for(int i=0; i<items.size(); i++) {
					Item item = items.get(i);
					String type = item.name.replace("scroll", "");
					
					//	switch if sr decides to add more units with allies
					switch(type) {
					case "paladin":
						type = "allies" + type;
						break;
					}
					
					try {
						ps[i] = Configs.getUnitInt(cid, currentLayer, type, Configs.buyViewer);
					} catch (NullPointerException e) {
						Logger.printException("Run -> store: err=item is not correct, item=" + item.toString(), e, Logger.runerr, Logger.error, cid, 4, true);
						ps[i] = -1;
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
					
					JsonElement err = beh.buyItem(item).get(SRC.errorMessage);
					if(err != null && err.isJsonPrimitive()) {
						if(!err.getAsString().startsWith("not enough"))
							Logger.print("Run -> store: err=" + err.getAsString() + ", item=" + item.toString(), Logger.lowerr, Logger.error, cid, 4, true);
					} else
						addRew(beh, SRC.Run.bought, item.name, item.quantity);
					
					ps[ind] = -1;
				}
			}
			
			Integer gold = beh.getCurrency(Store.gold, false);
			if(gold != null) {
				int src = beh.getStoreRefreshCount();
				int min = Configs.getStoreRefreshInt(cid, ProfileType.VIEWER, currentLayer, src > 3 ? 3 : src);
				if(min > -1 && min < gold) {
					String err = beh.refreshStore();
					if(err != null)
						Logger.print("Run -> Store: err="+err, Logger.runerr, Logger.error, cid, 4, true);
					store(beh);
				}
			}
			
		}
	}

	private boolean goMultiQuestClaim;
	
	private void quest(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		ArrayList<Quest> quests = beh.getClaimableQuests();
		
		for(Quest q : quests) {
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiQuestExploitViewer)) {
				goMultiQuestClaim = false;
				final Quest picked = q;
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
				JsonObject dat = beh.claimQuest(q);
				JsonElement err = dat.get(SRC.errorMessage);
				if(err.isJsonPrimitive())
					Logger.print("Run -> claimQuests: err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
				else {
					dat = dat.getAsJsonObject("data").getAsJsonObject("rewardData");
					String item = dat.get("ItemId").getAsString();
					if(item.equals("goldpiecebag"))
						item = Store.gold.get();
					else if(item.startsWith("skin"))
						item = "skin";
					else if(!item.startsWith("scroll") && !item.equals("eventcurrency")) {
						Logger.print("Run -> claimQuests: err=unknown reward, item="+item, Logger.lowerr, Logger.error, cid, 4, true);
						return;
					}
					int a = dat.get("Amount").getAsInt();
					addRew(beh, SRC.Run.event, item, a);
				}
			}
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	private void event(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		if(!beh.isEvent())
			return;
		
		boolean bp = beh.hasBattlePass();
		int tier = beh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(bp)
				collectEvent(beh, i, true);
			
			if(potionsTiers.contains(i) && beh.getCurrency(Store.potions, false) > 10)
				continue;
			
			collectEvent(beh, i, false);
		}
	}
	
	private boolean goMultiEventClaim;
	
	private void collectEvent(ViewerBackEnd beh, int tier, boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!beh.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiEventExploitViewer)) {
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
									addRew(beh, SRC.Run.event, rew, ce.get("quantity").getAsInt());
							}
						} catch (NoConnectionException e) {
						} catch (NullPointerException e) {
							Logger.print("Run -> event -> collectEvent(exploit): err=failed to collectEvent(exploit), tier="+tier+", bp="+bp, Logger.runerr, Logger.error, cid, 4, true);
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
				Logger.print("Run -> event -> collectEvent: tier="+tier+", bp="+bp+", err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
			} else {
				String rew = ce.get("reward").getAsString();
				if(!rew.equals("badges"))
					addRew(beh, SRC.Run.event, rew, ce.get("quantity").getAsInt());
			}
		}
		
	}

	private static final C[] sc = new C[] {Store.gold, Store.potions, Store.meat, Store.eventcurrency, Store.keys, Store.bones};
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	private Raid[] raids = null;
	private Hashtable<String, Integer> curs = null;
	
	@Override
	synchronized public void updateSlotSync() {
		for(int slot=0; slot<5; slot++) {
			int sync = Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.syncSlotViewer);
			Manager.blis.onProfileUpdateSlotSync(cid, slot, sync);
			if(sync == -1)
				continue;
			if(isRunning(slot)) {
				setRunning(false, slot);
				setRunning(true, sync);
			}
		}
	}
	
	@Override
	synchronized public void updateFrame(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;
		
		updateSlotSync();
		
		updateLayer();
		
		if(vbe != null)
			raids = vbe.getRaids(SRC.BackEndHandler.all);
		
		for(int i=0; i<4; i++) {
			if(raids[i] == null) {
				cnames[i] = null;
				cnames[i+4] = null;
				cnames[i+8] = null;
			} else {
				cnames[i] = raids[i].get(SRC.Raid.twitchUserName);
				cnames[i+4] = raids[i].get(SRC.Raid.twitchDisplayName);
				cnames[i+8] = raids[i].isDungeon() ? "d" : "c";
			}
			Manager.blis.onProfileUpdateSlot(cid, i, raids[i], Configs.isSlotLocked(cid, currentLayer, ""+i), change[i]);
		}
		
		if(vbe != null)
			curs = vbe.getCurrencies();
			
		for(C key : sc) {
			String k = key.get();
			Manager.blis.onProfileUpdateCurrency(cid, k, curs.containsKey(k) ? curs.get(k) : 0);
		}
		
			
	}
	
	@Override
	public synchronized void updateLayer() {
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units (1 = 5 min)
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);

		// set current layer
		JsonObject ptimes = Configs.getUObj(cid, Configs.ptimesViewer);
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
		
		Manager.blis.onProfileUpdateGeneral(cid, Configs.getPStr(cid, Configs.pname), Configs.getStr(cid, currentLayer, Configs.lnameViewer), new Color(Configs.getInt(cid, currentLayer, Configs.colorViewer)));
	}
	
	public void updateBeh(ViewerBackEnd beh) {
		updateProxySettings(beh);
		
		beh.setUpdateTimes( Configs.getInt(cid, currentLayer, Configs.unitUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.raidUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.mapUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.storeUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.questEventRewardsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.capsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.skinUpdateViewer));
	}
	
	private String[] cnames = new String[12];
	
	public String getTwitchLink(int slot) {
		return "https://twitch.tv/"+cnames[slot];
	}
	
	public Map getMap(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException {
		return beh.getMap(slot, false);
	}
	
	public void updateProxySettings(ViewerBackEnd beh) {
		String proxy = Configs.getStr(cid, currentLayer, Configs.proxyDomainViewer);
		String user = Configs.getStr(cid, currentLayer, Configs.proxyUserViewer);
		beh.setOptions(proxy.equals("") ? null : proxy, 
				Configs.getInt(cid, currentLayer, Configs.proxyPortViewer),
				user.equals("") ? null : user,
				Configs.getStr(cid, currentLayer, Configs.proxyPassViewer),
				Configs.getStr(cid, currentLayer, Configs.userAgentViewer),
				Configs.getBoolean(cid, currentLayer, Configs.proxyMandatoryViewer));
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
