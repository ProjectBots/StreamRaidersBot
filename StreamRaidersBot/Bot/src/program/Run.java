package program;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Time;
import include.Heatmap;
import include.Json;
import include.Maths;
import include.Pathfinding;
import program.MapConv.NoFinException;
import program.QuestEventRewards.Quest;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;
import program.SRRHelper.DungeonException;
import program.SRRHelper.PvPException;
import program.Store.Item;

public class Run {

	private String cookies = "";
	
	private String name = "";
	private LocalDateTime started = null;
	
	public LocalDateTime getDateTime() {
		return started;
	}
	
	public void resetDateTime() {
		started = LocalDateTime.now();
		rews = new JsonObject();
	}
	
	
	public Run(String name, String cookies) {
		this.cookies = cookies;
		this.name = name;
	}
	
	
	public static interface FrontEndHandler {
		public default void onStart(String name) {}
		public default void onReload(String name) {}
		public default void onFail(String name) {}
		public default void onNotAuthorized(String name) {}
		public default void onUpdateTimer(String name, String time) {}
		public default void onSlotEmpty(String name, int slot) {}
		public default void onSlotLocked(String name, int slot, boolean l) {}
		public default void onSlotBlocked(String name, int slot, boolean b) {}
		public default void onUpdateSlot(String name, int slot, Raid raid) {}
	}
	
	private FrontEndHandler feh = new FrontEndHandler() {};
	
	public void setFrontEndHandler(FrontEndHandler feh) {
		this.feh = feh;
	}
	
	
	private SRRHelper srrh = null;
	
	public SRRHelper getSRRH() {
		return srrh;
	}
	
	
	public Map getMap(int slot) {
		if(srrh == null) 
			return null;
		
		Raid[] raids = srrh.getRaids();
		for(int i=0; i<raids.length; i++) {
			if(raids[i].get(SRC.Raid.userSortIndex).equals(""+slot)) {
				try {
					srrh.loadMap(raids[i], name);
				} catch (DungeonException e) {
				} catch (PvPException | NoConnectionException e) {
					return null;
				}
				return srrh.getMap();
			}
		}
		return null;
	}
	
	
	private boolean running = false;
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
		if(running) {
			resetDateTime();
			t= new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						srrh = new SRRHelper(cookies, Options.get("clientVersion"));
						isReloading = false;
						runs();
					} catch (NoConnectionException e) {
						Debug.printException("Run -> Maybe your internet connection failed", e, Debug.runerr, Debug.fatal, name, null, true);
						feh.onFail(name);
						setRunning(false);
					} catch (SilentException e) {
					} catch (NotAuthorizedException e3) {
						feh.onNotAuthorized(name);
						setRunning(false);
					} catch (Exception e) {
						Debug.printException("Run -> setRunning", e, Debug.runerr, Debug.fatal, name, null, true);
						feh.onFail(name);
						setRunning(false);
					}
				}
			});
			t.start();
		} else {
			saveStats();
		}
	}
	
	public void saveStats() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime s = getDateTime();
		if(s == null) return;
		
		long sec = Duration.between(s, now).toSeconds();
		if(sec < 3600) return;
		
		JsonObject stats = Configs.getObj(name, Configs.stats);
		stats.addProperty("time", (long) stats.get("time").getAsLong() + sec);
		
		JsonObject crews = stats.getAsJsonObject("rewards");
		
		for(String rew : rews.keySet()) {
			if(crews.has(rew)) {
				crews.addProperty(rew, crews.get(rew).getAsInt() + rews.get(rew).getAsInt());
			} else {
				crews.add(rew, rews.get(rew).deepCopy());
			}
		}
		
		resetDateTime();
	}

	private Thread t = null;
	
	public void interrupt() {
		time = 0;
	}

	
	public void runs() {
		String part = Debug.print("started round", Debug.run, Debug.info, name, null);
		try {
			if(!isRunning()) return;
			
			part = Debug.print("chests", Debug.run, Debug.info, name, null);
			if(chests()) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
			}
			
			part = Debug.print("captains", Debug.run, Debug.info, name, null);
			captains();

			int c = 1;
			part = Debug.print("raids", Debug.run, Debug.info, name, null);
			while(raids()) {
				Debug.print("Run runs " + c++, Debug.loop, Debug.info, name, null);
				part = Debug.print("captains 2", Debug.run, Debug.info, name, null);
				captains();
				part = Debug.print("raids 2", Debug.run, Debug.info, name, null);
			}
			
			part = Debug.print("collectEvent", Debug.run, Debug.info, name, null);
			collectEvent();
			
			part = Debug.print("claimQuests", Debug.run, Debug.info, name, null);
			claimQuests();
			
			part = Debug.print("reloadStore", Debug.run, Debug.info, name, null);
			srrh.reloadStore();

			part = Debug.print("store", Debug.run, Debug.info, name, null);
			store();

			part = Debug.print("unlock", Debug.run, Debug.info, name, null);
			unlock();
			
			part = Debug.print("upgradeUnits", Debug.run, Debug.info, name, null);
			upgradeUnits();
			
			part = Debug.print("grantExtraRewards", Debug.run, Debug.info, name, null);
			srrh.getSRR().grantTeamReward();
			srrh.getSRR().grantEventQuestMilestoneReward();
			
			int min = Configs.getTime(name, Configs.min);
			int max = Configs.getTime(name, Configs.max);
			
			part = Debug.print("finished round", Debug.run, Debug.info, name, null);
			
			sleep(Maths.ranInt(min, max));
		} catch (Exception e) {
			saveStats();
			reload(20, part, e);
		}
	}
	
	private boolean isReloading = false;
	
	private void reload(int sec, String part, Exception e) {

		if(isReloading) saveStats();
		
		LocalTime lt = LocalTime.now();
		System.out.println("reload srrh in 20 sec for " + name + " at " + lt.getHour() + ":" + lt.getMinute());
		feh.onReload(name);
		
		time = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				int min = (int) Math.floor(time / 60);
				int sec = time % 60;
				
				String smin = "";
				String ssec = ""+sec;
				if(min != 0) {
					smin = min+":";
					if(sec < 10) {
						ssec = "0"+sec;
					}
				}
				
				feh.onUpdateTimer(name, "Reload in " + smin + ssec);
				
				if(time <= 0) {
					t.cancel();
					System.out.println("started reload for " + name);
					
					try {
						String ver = srrh.reload();
						
						System.out.println("completed reloading srrh for " + name);
						if(ver == null) 
							if(!e.getClass().equals(SilentException.class) && !e.getClass().equals(NoConnectionException.class))
								Debug.printException("critical error happened for " + name + " at \"" + part + "\" -> skipped this round", e, Debug.runerr, Debug.fatal, name, null, true);
						
						feh.onStart(name);
						isReloading = false;
						resetDateTime();
						sleep(10);
					} catch (NoConnectionException e2) {
						setReloading(part, e);
					} catch (NotAuthorizedException e3) {
						feh.onNotAuthorized(name);
						setRunning(false);
					} catch (Exception e1) {
						if(!e1.getClass().equals(SilentException.class))
							Debug.printException("failed to reload srrh for " + name, e, Debug.runerr, Debug.fatal, name, null, true);
						setReloading(part, e);
					}
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	
	public static class SilentException extends RuntimeException {
		private static final long serialVersionUID = 6180078222808617728L;
		public SilentException() {
			super("this is a silent exception");
		}
	}
	
	private void setReloading(String part, Exception e) {
		if(isReloading) {
			if(e.getClass().equals(NoConnectionException.class))
				Debug.printException("Internet connection failed for " + name, e, Debug.runerr, Debug.fatal, name, null, true);
			feh.onFail(name);
			setRunning(false);
		} else {
			if(!e.getClass().equals(SilentException.class) && !e.getClass().equals(NoConnectionException.class))
				Debug.printException("critical error happened for " + name + " at \"" + part + "\" -> try to reload again", e, Debug.runerr, Debug.fatal, name, null, true);
			isReloading = true;
			reload(15*60, part, e);
		}
	}
	

	private int time = 0;
	
	private void sleep(int sec) {
		time = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				int min = (int) Math.floor(time / 60);
				int sec = time % 60;
				
				String smin = "";
				String ssec = ""+sec;
				if(min != 0) {
					smin = min+":";
					if(sec < 10) {
						ssec = "0"+sec;
					}
				}
				
				feh.onUpdateTimer(name, smin + ssec);
				
				if(time <= 0) {
					t.cancel();
					runs();
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	
	private String[] neededUnits = new String[0];

	private void claimQuests() throws NoConnectionException {
		srrh.updateQuests();
		
		neededUnits = srrh.getNeededUnitTypesForQuests();
		
		Quest[] quests = srrh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			String err = srrh.claimQuest(quests[i]);
			if(err != null)
				Debug.print(name + ": Run -> claimQuests: err=" + err, Debug.runerr, Debug.error, name, null, true);
		}
	}
	
	private List<String> potionsTiers = Arrays.asList("5,11,14,22,29".split(","));
	
	private void collectEvent() throws NoConnectionException {
		srrh.updateEvent();
		
		if(!srrh.isEvent()) return;
		
		boolean bp = srrh.hasBattlePass();
		int tier = srrh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(bp) {
				String err = srrh.collectEvent(i, true);
				if(err != null && !err.equals("cant collect")) {
					Debug.print(name + ": Run -> collectEvent -> pass: err=" + err, Debug.runerr, Debug.error, name, null, true);
				}
			}
			
			if(potionsTiers.contains(""+i)) continue;
			String err = srrh.collectEvent(i, false);
			if(err != null && !err.equals("cant collect")) {
				Debug.print(name + ": Run -> collectEvent -> basic: err=" + err, Debug.runerr, Debug.error, name, null, true);
			}
			
		}
	}
	
	private void unlock() throws NoConnectionException {
		Unit[] unlockable = srrh.getUnits(SRC.Helper.canUnlockUnit);
		if(unlockable.length == 0)
			return;
		
		int[] ps = new int[unlockable.length];
		for(int i=0; i<unlockable.length; i++)
			ps[i] = Configs.getUnitInt(name, unlockable[i].get(SRC.Unit.unitType), unlockable[i].isDupe() ? Configs.dupe : Configs.unlock);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = srrh.unlockUnit(unlockable[ind]);
			if(err != null && !err.equals("not enough gold"))
				Debug.print("Run -> unlock: type=" + unlockable[ind].get(SRC.Unit.unitType) + ", err=" + err, Debug.lowerr, Debug.error, name, null, true);
			
			ps[ind] = -1;
		}
	}
	
	private JsonObject bought = new JsonObject();
	
	public JsonObject getBoughtItems() {
		return bought;
	}
	
	private void store() throws NoConnectionException {
		/*	TODO enable when active again
		try {
			String sel = Configs.getStr(name, Configs.canBuyChest);
			String err;
			switch(sel) {
			case "saint":
				err = srrh.buyChest("dungeons5saintchest");
				break;
			case "vampire":
				err = srrh.buyChest("dungeons5vampirechest");
				break;
			default:
				throw new NullPointerException();
			}
			if(err != null && !(err.equals("after end") || err.startsWith("not enough ")))
				Debug.print(name+" -> Run -> store -> buyChest: err="+err+", chest="+sel, Debug.runerr, Debug.error, name, null, true);
			if(err == null) {
				if(bought.has("dungeonchest"))
					bought.addProperty("dungeonchest", bought.get("dungeonchest").getAsInt() + 1);
				else 
					bought.addProperty("dungeonchest", 1);
				
			}
		} catch (NullPointerException e) {}
		*/
		
		ec: {
			String sel = Configs.getStr(name, Configs.buyEventChest);
			//String chest;
			switch(sel) {
			case "Necro Chest":
				String err = srrh.buyChest("dungeons6necrochest");
				if(err != null && !(err.equals("after end") || err.startsWith("not enough ")))
					Debug.print(name+" -> Run -> store -> buyChest: err="+err+", chest="+sel, Debug.runerr, Debug.error, name, null, true);
				if(err == null) {
					if(bought.has("dungeonchest"))
						bought.addProperty("dungeonchest", bought.get("dungeonchest").getAsInt() + 1);
					else 
						bought.addProperty("dungeonchest", 1);
				}
				break;
			case "Necro Scrolls":
				List<Item> items = srrh.getStoreItems(SRC.Store.purchasable, SRC.Store.dungeon);
				
				int[] lowest = {-1, Integer.MAX_VALUE};
				for(int i=0; i<items.size(); i++) {
					int p = items.get(i).getPrice();
					if(p < lowest[1]) {
						lowest[0] = i;
						lowest[1] = p;
					}
				}
				if(lowest[0] < 0) {
					Debug.print("Run -> store -> ec: already bought all necro scrolls", Debug.runerr, Debug.info, name, 4, true);
					break ec;
				}
				
				Item item = items.get(lowest[0]);
				
				String err2 = srrh.buyItem(item);
				
				if(!(err2 == null || err2.startsWith("not enough ") || ArrayUtils.contains("after end  before start".split("  "), err2)))
					Debug.print(name + ": Run -> store: item=" + item + ", err=" + err2, Debug.lowerr, Debug.error, name, null, true);
				
				if(err2 == null) {
					String type = item.getItem();
					int amount = item.getQuantity();
					if(bought.has(type))
						bought.addProperty(type, bought.get(type).getAsInt() + amount);
					else 
						bought.addProperty(type, amount);
				}
				break;
			/*
			case "Toys For Tots":
				chest = "snowfallcharitychest4";
				break;
			*/
			default:
				break ec;
			}
			/*
			String err = srrh.buyChest(chest);
			if(err != null && !(err.equals("after end") || err.startsWith("not enough ")))
				Debug.print(name+" -> Run -> store -> buyChest: err="+err+", chest="+sel, Debug.runerr, Debug.error, name, null, true);
			if(err == null) {
				chest = "snowfallcharitychest";
				if(bought.has(chest))
					bought.addProperty(chest, bought.get(chest).getAsInt() + 1);
				else 
					bought.addProperty(chest, 1);
			}
			*/
		}
		
				
		
		if(Configs.getBoolean(name, Configs.canBuyScrolls)) {
			List<Item> items = srrh.getStoreItems(SRC.Store.purchasable, SRC.Store.scrolls);
			if(items.size() != 0) {
				int[] ps = new int[items.size()];
				for(int i=0; i<items.size(); i++) {
					Item item = items.get(i);
						String type = item.getItem().replace("scroll", "");
						
						//	switch if sr decides to add more units with allias
						switch(type) {
						case "paladin":
							type = "allies" + type;
							break;
						}
					if(type.equals("eventcurrency"))
						ps[i] = Integer.MAX_VALUE;
					else {
						try {
							ps[i] = Configs.getUnitInt(name, type, Configs.buy);
						} catch (NullPointerException e) {
							Debug.printException(name + ": Run -> store: err=item is not correct, item=" + item.toStringOneLine(), e, Debug.runerr, Debug.error, name, null, true);
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
					
					String err = srrh.buyItem(item);
					if(!(err == null || err.startsWith("not enough ") || ArrayUtils.contains("after end  before start".split("  "), err)))
						Debug.print(name + ": Run -> store: err=" + err + ", item=" + item.toString(), Debug.lowerr, Debug.error, name, null, true);
					
					if(err == null) {
						String type = item.getItem();
						int amount = item.getQuantity();
						if(bought.has(type))
							bought.addProperty(type, bought.get(type).getAsInt() + amount);
						else 
							bought.addProperty(type, amount);
					}
					
					ps[ind] = -1;
				}
			}
			
			Integer gold = srrh.getCurrency(Store.gold);
			if(gold != null) {
				int src = srrh.getStoreRefreshCount();
				int min = Configs.getStoreRefreshInt(name, src > 3 ? 3 : src);
				if(min > -1 && min < gold) {
					String err = srrh.refreshStore();
					if(err != null)
						Debug.print(name+" -> Run -> Store: err="+err, Debug.runerr, Debug.error, name, null, true);
					store();
				}
			}
			
		}
		
		
		
	}
	
	
	private void upgradeUnits() throws NoConnectionException {
		Unit[] us = srrh.getUnits(SRC.Helper.canUpgradeUnit);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = Configs.getUnitInt(name, us[i].get(SRC.Unit.unitType), Configs.upgrade);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = srrh.upgradeUnit(us[ind], Configs.getUnitString(name, us[ind].get(SRC.Unit.unitType), Configs.spec));
			if(err != null) {
				if(!(err.equals("no specUID") || err.equals("cant upgrade unit"))) {
					Debug.print(name + ": Run -> upgradeUnits: type=" + us[ind].get(SRC.Unit.unitType) + " err=" + err, Debug.lowerr, Debug.error, name, null, true);
					break;
				}
			}
			
			ps[ind] = -1;
		}
	}
	
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	private boolean raids() throws NoConnectionException, NotAuthorizedException {
		boolean ret = false;
		
		Unit[] units = srrh.getUnits(SRC.Helper.canPlaceUnit);
		Raid[] plra = srrh.getRaids(SRC.Helper.canPlaceUnit);
		Raid[] all = srrh.getRaids();
		
		boolean canEpicn = false;
		for(int i=0; i<units.length; i++) {
			if(Configs.getUnitInt(name, units[i].get(SRC.Unit.unitType), Configs.epic) > -1) {
				canEpicn = true;
				break;
			}
		}
		boolean canEpicd = false;
		
		Integer potionsc = srrh.getCurrency(Store.potions);
		boolean epic = (potionsc == null ? false : (potionsc >= 45 ? true : false));
		
		boolean[] got = new boolean[] {false, false, false, false};
		
		for(int i=0; i<all.length; i++) {
			int usi = Integer.parseInt(all[i].get(SRC.Raid.userSortIndex));
			got[usi] = true;
			feh.onUpdateSlot(name, usi, all[i]);
		}
		
		for(int i=0; i<got.length; i++) {
			
			feh.onSlotBlocked(name, i, Configs.isSlotBlocked(name, ""+i));
			feh.onSlotLocked(name, i, Configs.isSlotLocked(name, ""+i));
			
			
			
			if(!got[i]) {
				feh.onSlotEmpty(name, i);
			}
		}
		
		if(plra.length != 0) {
			
			int c = 1;
			
			for(int i=0; i<plra.length; i++) {
				if(Configs.isSlotBlocked(name, plra[i].get(SRC.Raid.userSortIndex)))
					continue;
				
				if(i!=0) {
					try {
						Thread.sleep(Configs.getInt(name, Configs.unitPlaceDelay));
					} catch (InterruptedException e) {}
				}
				
				try {
					if(units.length == 0)
						break;
					
					Unit[] dunUnits = null;
					try {
						srrh.loadMap(plra[i], name);
					} catch (DungeonException e) {
						if(!plra[i].get(SRC.Raid.userSortIndex).equals(Configs.getStr(name, Configs.dungeonSlot)))
							throw e;
						dunUnits = srrh.getUnitsDungeons(plra[i]);
						for(int k=0; k<dunUnits.length; k++) {
							if(Configs.getUnitInt(name, dunUnits[k].get(SRC.Unit.unitType), Configs.epic) > -1) {
								canEpicd = true;
								break;
							}
						}
					}
					Map map = srrh.getMap();
					
					JsonArray ppt = map.getUsablePlanTypesJArr();
					
					boolean apt = !(ppt.size() == 0);
					
					int[] maxheat = new Heatmap().getMaxHeat(map);
					int[][] banned = new int[0][0];
					
					
					epic = epic && (dunUnits == null ? canEpicn : canEpicd);
					
					loop:
					while(true) {
						fpt = null;
						Unit unit = findUnit(dunUnits == null ? units : dunUnits, apt, ppt, epic);
						if(unit == null) unit = findUnit(dunUnits == null ? units : dunUnits, false, ppt, epic);
						if(unit == null) break;
						
						while(true) {
							Debug.print("Run raids " + c++, Debug.loop, Debug.info, name, null);
							
							int[] pos;
							try {
								pos = new Pathfinding().search(new MapConv().asField_old(map, unit.canFly(), fpt, maxheat, banned), name, Integer.parseInt(plra[i].get(SRC.Raid.userSortIndex)), epic);
							} catch (NoFinException e) {
								if(fpt == null)
									break loop;
								ppt.remove(new JsonPrimitive(fpt));
								break;
							}
							
							if(pos == null) {
								if(fpt == null) 
									break loop;
								ppt.remove(new JsonPrimitive(fpt));
								break;
							}
							String err = srrh.placeUnit(plra[i], unit, epic, pos, fpt != null);
							
							if(err == null) {
								if(dunUnits == null)
									units = remove(units, unit);
								epic = false;
								break loop;
							}
							
							if(err.equals("PERIOD_ENDED") || err.equals("AT_POPULATION_LIMIT")) 
								break loop;
							
							if(banned.length >= 10) {
								Debug.print("Run -> raids -> unitPlacement: tdn=" + plra[i].get(SRC.Raid.twitchDisplayName) + ", epic=" + epic + ", err=" + err, Debug.lowerr, Debug.error, name, null, true);
								break loop;
							}
							
							banned = add(banned, pos);
						}
					}
				} catch (PvPException | DungeonException e) {
					String usi = plra[i].get(SRC.Raid.userSortIndex);
					if(!ret) {
						try {
							ret = switchRaid(usi, true, getCaps(plra[i]));
						} catch (NoCapMatchException e1) {
							Debug.printException("Run -> raids: err=No Captain matches", e1, Debug.runerr, Debug.error, name, null, true);
						}
					}
				}
			}
			resCaps();
		}
		return ret;
	}
	
	private String fpt = null;
	
	private Unit findUnit(Unit[] units, boolean apt, JsonArray ppt, boolean epic) {
		Unit unit = null;
		
		int[] ps = new int[units.length];
		for(int i=0; i<ps.length; i++) {
			ps[i] = -1;
		}
		String[] sps = new String[units.length];
		
		for(int j=0; j<units.length; j++) {
			if(apt) {
				String tfpt = null;
				JsonArray pts = units[j].getPlanTypesJArr();
				for(int k=0; k<pts.size(); k++) {
					String pt = pts.get(k).getAsString();
					if((ppt.contains(new JsonPrimitive(pt)))) {
						if(pt.equals("vibe")) {
							if(tfpt == null)
								tfpt = pt;
						} else {
							tfpt = pt;
						}
					}
				}
				if(tfpt == null)
					continue;
				
				sps[j] = tfpt;
			}
			
			if(Arrays.asList(neededUnits).contains(units[j].get(SRC.Unit.unitType)) && !epic) {
				fpt = sps[j];
				return units[j];
			}
			
			ps[j] = Configs.getUnitInt(name, units[j].get(SRC.Unit.unitType), epic ? Configs.epic : Configs.place);
		}
		
		int ind = 0;
		for(int i=1; i<ps.length; i++)
			if(ps[i] > ps[ind])
				ind = i;
		
		if(ps[ind] > -1) {
			unit = units[ind];
			fpt = sps[ind];
		}
		
		return unit;
	}
	
	private JsonObject rews = new JsonObject();
	
	public JsonObject getRews() {
		return rews;
	}
	
	
	private boolean chests() throws NoConnectionException, NotAuthorizedException {
		Raid[] rera = srrh.getRaids(SRC.Helper.isReward);
		if(rera.length != 0) {
			for(int i=0; i<rera.length; i++) {
				if(Configs.isSlotBlocked(name, rera[i].get(SRC.Raid.userSortIndex)))
					continue;
				
				JsonObject jo = rera[i].getChest(srrh.getSRR());
				
				Set<String> keys = jo.keySet();
				
				for(String key: keys) {
					try {
						rews.addProperty(key, rews.getAsJsonPrimitive(key).getAsInt() + jo.getAsJsonPrimitive(key).getAsInt());
					} catch (Exception e) {
						rews.addProperty(key, jo.getAsJsonPrimitive(key).getAsInt());
					}
				}
			}
			return true;
		}
		return false;
	}
	
	private JsonObject bannedCaps = new JsonObject();
	
	private boolean captains() throws NoConnectionException, NotAuthorizedException {
		int uCount = srrh.getUnits(SRC.Helper.all).length;
		Raid[] raids = srrh.getRaids(SRC.Helper.all);
		Set<String> set = bannedCaps.deepCopy().keySet();
		for(String cap : set) 
			if(Time.isAfter(bannedCaps.getAsJsonPrimitive(cap).getAsString(), srrh.getServerTime()))
				bannedCaps.remove(cap);
		
		boolean changed = false;
		
		JsonObject favs = Configs.getObj(name, Configs.favs);
		JsonArray cts = Json.parseArr(Options.get("chests"));
		
		String di = Configs.getStr(name, Configs.dungeonSlot);
		
		boolean ctNull = true;
		int c = 1;
		try {
			while(true) {
				Debug.print("Run captains " + c++, Debug.loop, Debug.info, name, null);
				boolean breakout = true;
				boolean[] got = new boolean[] {false, uCount < 5, uCount < 8, !srrh.hasBattlePass()};
				for(int i=0; i<raids.length; i++) {
					got[Integer.parseInt(raids[i].get(SRC.Raid.userSortIndex))] = true;
					if(Configs.isSlotBlocked(name, raids[i].get(SRC.Raid.userSortIndex)))
						continue;
					if(Configs.isSlotLocked(name, raids[i].get(SRC.Raid.userSortIndex)))
						continue;
					if(!raids[i].isSwitchable(srrh.getServerTime(), 10)) 
						continue;
					
					boolean ic = false;
					if(favs.has(raids[i].get(SRC.Raid.twitchDisplayName)))
						if(favs.getAsJsonPrimitive(raids[i].get(SRC.Raid.twitchDisplayName)).getAsBoolean())
							if(!raids[i].isOffline(srrh.getServerTime(), false, 10))
								ic = true;
					
					String ct = raids[i].getFromNode(SRC.MapNode.chestType);
					if(ct == null) {
						if(ctNull)
							Debug.print("Run -> captains: err=ct equals null", Debug.runerr, Debug.error, name, null, true);
						ctNull = false;
						bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
						switchRaid(raids[i].get(SRC.Raid.userSortIndex), true, getCaps(raids[i]));
						changed = true;
						breakout = false;
						continue;
					}
					if(!ct.contains("bone") && !ct.contains("dungeon") && !cts.contains(new JsonPrimitive(ct)))
						ct = "chestboostedskin";
					
					if(raids[i].get(SRC.Raid.userSortIndex).equals(di) && !raids[i].isOffline(srrh.getServerTime(), true, 10) && ct.contains("dungeon"))
						continue;
						
					int loy = Integer.parseInt(raids[i].get(SRC.Raid.pveWins));
					int max = 0;
					try {
						max = Configs.getChestInt(name, ct, Configs.maxc);
					} catch (NullPointerException e) {}
					
					if(		(raids[i].isOffline(srrh.getServerTime(), true, 10)) ||
							(ct.contains("bone")) ||
							(ct.contains("dungeon") ? true : raids[i].get(SRC.Raid.userSortIndex).equals(di)) ||
							(!Configs.getChestBoolean(name, ct, Configs.enabled) && !ic) ||
							(loy < Configs.getChestInt(name, ct, Configs.minc) && !ic) ||
							(loy > (max < 0 ? Integer.MAX_VALUE : max) && !ic)
							) {
						bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
						switchRaid(raids[i].get(SRC.Raid.userSortIndex), true, getCaps(raids[i]));
						changed = true;
						breakout = false;
					}
				}
				
				for(int i=0; i<got.length; i++) {
					if(!got[i]) {
						if(Configs.isSlotBlocked(name, ""+i))
							continue;
						if(Configs.isSlotLocked(name, ""+i))
							continue;
						switchRaid(""+i, false, getCaps(""+i));
						changed = true;
						breakout = false;
					}
				}
				
				if(breakout) break;
				
				raids = srrh.getRaids(SRC.Helper.all);
			}
		} catch (NoCapMatchException e) {
			Debug.printException("Run -> captains: err=" + e.getMessage(), e, Debug.runerr, Debug.error, name, null, true);
		}
		resCaps();
		return changed;
	}
	
	
	public static class NoCapMatchException extends Exception {
		private static final long serialVersionUID = 8399974494981759517L;
		public NoCapMatchException() {
			super("No Captain Matches");
		}
	}
	
	private JsonArray ncaps = null;
	private JsonArray dcaps = null;
	
	private void resCaps() {
		ncaps = null;
		dcaps = null;
	}
	
	
	private JsonArray getCaps(Raid raid) throws NoConnectionException, NoCapMatchException {
		return getCaps(raid.get(SRC.Raid.userSortIndex));
	}
	
	private JsonArray getCaps(String usi) throws NoConnectionException, NoCapMatchException {
		if(usi.equals(""+Configs.getStr(name, Configs.dungeonSlot))) {
			if(dcaps == null) {
				dcaps = new JsonArray();
				searchCaps(dcaps, SRC.Search.dungeons);
			}
			if(dcaps.size() == 0)
				throw new NoCapMatchException();
			return dcaps;
		} else {
			if(ncaps == null) {
				ncaps = new JsonArray();
				searchCaps(ncaps, SRC.Search.campaign);
			}
			if(ncaps.size() == 0)
				throw new NoCapMatchException();
			return ncaps;
		}
	}
	
	private void searchCaps(JsonArray caps, String mode) throws NoConnectionException {
		int pages = 3;
		for(int i=1; i<pages && i<=Configs.getInt(name, Configs.maxPage); i++) {
			caps.addAll(srrh.search(i, 6, false, true, mode, false, null));
			pages = srrh.getPagesFromLastSearch();
		}
	}
	
	private boolean switchRaid(String sortIndex, boolean rem, JsonArray caps) throws NoConnectionException, NotAuthorizedException {
		JsonObject cap = null;
		int oldLoy = -1;

		JsonObject favs = Configs.getObj(name, Configs.favs);
		boolean fav = false;
		
		if(Configs.isSlotLocked(name, sortIndex))
			return false;
		
		for(int i=0; i<caps.size(); i++) {
			JsonObject icap = caps.get(i).getAsJsonObject();
			
			if(bannedCaps.has(icap.getAsJsonPrimitive(SRC.Raid.captainId).getAsString()))
				continue;
			
			if(favs.has(icap.getAsJsonPrimitive(SRC.Raid.twitchDisplayName).getAsString())) {
				if(fav) {
					if(Integer.parseInt(icap.getAsJsonPrimitive(SRC.Raid.pveWins).getAsString()) > oldLoy) {
						cap = icap;
						oldLoy = Integer.parseInt(cap.getAsJsonPrimitive(SRC.Raid.pveWins).getAsString());
					}
				} else {
					fav = true;
					cap = icap;
					oldLoy = Integer.parseInt(cap.getAsJsonPrimitive(SRC.Raid.pveWins).getAsString());
				}
			} else {
				if(fav) continue;
				if(Integer.parseInt(icap.getAsJsonPrimitive(SRC.Raid.pveWins).getAsString()) > oldLoy) {
					cap = icap;
					oldLoy = Integer.parseInt(cap.getAsJsonPrimitive(SRC.Raid.pveWins).getAsString());
				}
			}
		}
		
		if(cap == null) {
			Debug.print("Run -> switchRaid: err=No captain matches", Debug.runerr, Debug.error, name, null, true);
			return false;
		}
		
		if(rem) {
			srrh.switchRaid(cap, sortIndex);
		} else {
			srrh.addRaid(cap, sortIndex);
		}
		caps.remove(cap);
		return true;
	}
	
	
	private static Unit[] remove(Unit[] arr, Unit item) {
		int index = -1;
		for(int i=0; i<arr.length; i++) {
			if(arr[i].equals(item)) {
				index = i;
			}
		}
		if(index == -1) return arr;
		
		Unit[] arr2 = new Unit[arr.length - 1];
		System.arraycopy(arr, 0, arr2, 0, index);
		System.arraycopy(arr, index + 1, arr2, index, arr.length-index-1);
		
		return arr2;
	}
	
	private static int[][] add(int[][] arr, int[] item) {
		int[][] arr2 = new int[arr.length + 1][];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	
}
