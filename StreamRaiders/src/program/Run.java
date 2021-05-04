package program;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.HttpHostConnectException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Time;
import include.GUI;
import include.GUI.Image;
import include.Heatmap;
import include.Pathfinding;
import program.QuestEventRewards.Quest;
import program.SRR.NoInternetException;
import program.SRRHelper.PvPException;

public class Run {

	private String cookies = "";
	private static String clientVersion = StreamRaiders.get("clientVersion");
	
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
	
	private SRRHelper srrh = null;
	
	public SRRHelper getSRRH() {
		return srrh;
	}
	
	
	public void showMap(int index) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if(srrh == null) return;
				try {
					Raid[] raids = srrh.getRaids();
					if(index < raids.length) {
						srrh.loadMap(srrh.getRaids()[index]);
						Map map = srrh.getMap();
						MapConv.asGui(map);
					}
				} catch (PvPException e) {
				} catch (Exception e) {
					StreamRaiders.log(name + ": Run -> showMap", e);
				} 
			}
		});
		t.start();
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
						srrh = new SRRHelper(cookies, clientVersion);
						isReloading = false;
						runs();
					} catch (NoInternetException e) {
						StreamRaiders.log(name + ": Run -> Maybe your internet connection failed", e, true);
						GUI.setBackground(name+"::start", Color.red);
						setRunning(false);
					} catch (Exception e) {
						StreamRaiders.log(name + ": Run -> setRunning", e);
						GUI.setBackground(name+"::start", Color.red);
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
		
		
		JsonObject jo = new JsonObject();
		jo.addProperty("time", sec);
		jo.add("rewards", rews.deepCopy());

		JsonArray stats = Configs.getArr(name, Configs.stats);
		stats.add(jo);
		
		resetDateTime();
	}

	private Thread t = null;
	
	public void interrupt() {
		time = 0;
	}

	
	public void runs() {
		String part = "null";
		try {
			if(!isRunning()) return;
			
			part = "chests";
			if(chests()) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
			}
			
			part = "captains";
			captains();

			part = "raids";
			while(raids()) {
				part = "captains 2";
				captains();
				part = "raids 2";
			}
			
			part = "collectEvent";
			collectEvent();
			
			part = "claimQuests";
			claimQuests();
			
			part = "reloadStore";
			srrh.reloadStore();

			part = "store";
			store();

			part = "unlock";
			unlock();
			
			part = "upgradeUnits";
			upgradeUnits();
			
			sleep((int) Math.round(Math.random()*620) + 100);
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
		GUI.setBackground(name+"::start", Color.yellow);
		
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
				
				GUI.setText(name + "::counter", "Reload in " + smin + ssec);
				
				if(time <= 0) {
					t.cancel();
					System.out.println("started reload for " + name);
					
					try {
						String ver = srrh.reload();
						
						System.out.println("completed reloading srrh for " + name);
						if(ver == null) 
							StreamRaiders.log("critical error happened for " + name + " at \"" + part + "\" -> skipped this round", e);
						
						GUI.setBackground(name+"::start", Color.green);
						isReloading = false;
						resetDateTime();
						sleep(10);
					} catch (NoInternetException | HttpHostConnectException e2) {
						StreamRaiders.log(name + ": Run -> Maybe your internet connection failed", e2, true);

						setReloading(part, e);
					} catch (Exception e1) {
						StreamRaiders.log("failed to reload srrh for " + name, e1);
						
						setReloading(part, e);
					}
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	
	private void setReloading(String part, Exception e) {
		if(isReloading) {
			GUI.setBackground(name+"::start", Color.red);
			setRunning(false);
		} else {
			StreamRaiders.log("critical error happened for " + name + " at \"" + part + "\" -> try to reload again", e);
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
				
				GUI.setText(name + "::counter", smin + ssec);
				
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

	private void claimQuests() throws URISyntaxException, IOException, NoInternetException {
		srrh.updateQuests();
		
		neededUnits = srrh.getNeededUnitTypesForQuests();
		
		Quest[] quests = srrh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			String err = srrh.claimQuest(quests[i]);
			if(err != null) StreamRaiders.log(name + ": Run -> claimQuests: err=" + err, null);
		}
	}
	
	private List<String> potionsTiers = Arrays.asList("5,11,14,22,29".split(","));
	
	private void collectEvent() throws URISyntaxException, IOException, NoInternetException {
		srrh.updateEvent();
		
		if(!srrh.isEvent()) return;
		
		boolean bp = srrh.hasBattlePass();
		int tier = srrh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(potionsTiers.contains(""+i)) continue;
			String err = srrh.collectEvent(i, false);
			if(err != null && !err.equals("cant collect")) {
				StreamRaiders.log(name + ": Run -> collectEvent -> basic: err=" + err, null);
			}
			if(!bp) continue;
			
			err = srrh.collectEvent(i, true);
			if(err != null && !err.equals("cant collect")) {
				StreamRaiders.log(name + ": Run -> collectEvent -> pass: err=" + err, null);
			}
		}
	}
	
	private void unlock() throws URISyntaxException, IOException, NoInternetException {
		Unit[] unlockable = srrh.getUnits(SRC.Helper.canUnlockUnit);
		for(int i=0; i<unlockable.length; i++) {
			if(!Configs.getUnitBoolean(name, unlockable[i].get(SRC.Unit.unitType), unlockable[i].isDupe() ? Configs.dupe : Configs.unlock))
				continue;
			String err = srrh.unlockUnit(unlockable[i]);
			if(err != null && !err.equals("not enough gold"))
				StreamRaiders.log(name + ": Run -> unlock: type=" + unlockable[i].get(SRC.Unit.unitType) + ", err=" + err, null);
		}
	}
	
	
	private void store() throws URISyntaxException, IOException, NoInternetException {
		JsonArray items = srrh.getStoreItems(SRC.Store.notPurchased);
		for(int i=0; i<items.size(); i++) {
			String type = items.get(i).getAsJsonObject()
					.getAsJsonPrimitive("itemId")
					.getAsString()
					.split("pack")[0]
					.replace("scroll", "")
					.replace("paladin", "alliespaladin");
			try {
				if(!Configs.getUnitBoolean(name, type, Configs.buy))
					continue;
				String err = srrh.buyItem(items.get(i).getAsJsonObject());
				if(err != null && !err.equals("not enough gold"))
					StreamRaiders.log(name + ": Run -> store: item=" + items.get(i) + ", err=" + err, null);
			} catch (NullPointerException e) {
				StreamRaiders.log(name + ": Run -> store: item=" + items.get(i).getAsJsonObject().getAsJsonPrimitive("itemId").getAsString(), e);	
			}
		}
	}
	
	
	private void upgradeUnits() throws URISyntaxException, IOException, NoInternetException {
		Unit[] us = srrh.getUnits(SRC.Helper.canUpgradeUnit);
		for(int i=0; i<us.length; i++) {
			if(!Configs.getUnitBoolean(name, us[i].get(SRC.Unit.unitType), Configs.upgrade))
				continue;
			String err = srrh.upgradeUnit(us[i], Configs.getUnitString(name, us[i].get(SRC.Unit.unitType), Configs.spec));
			if(err != null) {
				if(!(err.equals("no specUID") || err.equals("cant upgrade unit"))) {
					StreamRaiders.log(name + ": Run -> upgradeUnits: type=" + us[i].get(SRC.Unit.unitType) + " err=" + err, null);
					break;
				}
			}
		}
	}
	
	public static final String[] pveloy = "? bronze silver gold".split(" ");
	
	private boolean raids() throws URISyntaxException, IOException, NoInternetException {
		boolean ret = false;
		
		JsonArray locked = Configs.getArr(name, Configs.locked);
		JsonArray favs = Configs.getArr(name, Configs.favs);
		
		Unit[] units = srrh.getUnits(SRC.Helper.canPlaceUnit);
		Raid[] plra = srrh.getRaids(SRC.Helper.canPlaceUnit);
		Raid[] all = srrh.getRaids();

		for(int i=0; i<4; i++) {
			if(i<all.length) {
				if(Configs.isSlotBlocked(name, all[i].get(SRC.Raid.userSortIndex))) {
					GUI.setText(name+"::name::"+i, "Blocked Raid!");
					GUI.setForeground(name+"::name::"+i, Color.red);
					Image img = new Image("data/ChestPics/nochest.png");
					img.setSquare(30);
					GUI.setImage(name+"::chest::"+i, img);
					GUI.setText(name+"::lockBut::"+i, "\uD83D\uDD13");
					GUI.setBackground(name+"::lockBut::"+i, GUI.getDefButCol());
					GUI.setEnabled(name+"::lockBut::"+i, false);
					GUI.setText(name+"::favBut::"+i, "\uD83D\uDC94");
					GUI.setForeground(name+"::favBut::"+i, Color.black);
					GUI.setEnabled(name+"::favBut::"+i, false);
					GUI.setEnabled(name+"::map::"+i, false);
				} else {
					int wins = Integer.parseInt(all[i].get(SRC.Raid.pveWins));
					int lvl = Integer.parseInt(all[i].get(SRC.Raid.pveLoyaltyLevel));
					String disName = all[i].get(SRC.Raid.twitchDisplayName);
					GUI.setText(name+"::name::"+i, disName + " - " + wins + "|" + pveloy[lvl]);
					GUI.setForeground(name+"::name::"+i, Color.black);
					String ct = all[i].getFromNode(SRC.MapNode.chestType);
					if(ct == null) ct = "nochest";
					Image img = new Image("data/ChestPics/" + ct + ".png");
					img.setSquare(30);
					GUI.setImage(name+"::chest::"+i, img);
					GUI.setEnabled(name+"::lockBut::"+i, true);
					if(locked.contains(new JsonPrimitive(disName))) {
						GUI.setText(name+"::lockBut::"+i, "\uD83D\uDD12");
						GUI.setBackground(name+"::lockBut::"+i, Color.green);
					} else {
						GUI.setText(name+"::lockBut::"+i, "\uD83D\uDD13");
						GUI.setBackground(name+"::lockBut::"+i, GUI.getDefButCol());
					}
					GUI.setEnabled(name+"::favBut::"+i, true);
					if(favs.contains(new JsonPrimitive(disName))) {
						GUI.setText(name+"::favBut::"+i, "\u2764");
						GUI.setForeground(name+"::favBut::"+i, new Color(227,27,35));
					} else {
						GUI.setText(name+"::favBut::"+i, "\uD83D\uDC94");
						GUI.setForeground(name+"::favBut::"+i, Color.black);
					}
					GUI.setEnabled(name+"::map::"+i, true);
				}
			} else {
				GUI.setText(name+"::name::"+i, "");
				Image img = new Image("data/ChestPics/nochest.png");
				img.setSquare(30);
				GUI.setImage(name+"::chest::"+i, img);
			}
		}
		
		if(plra.length != 0) {
			for(int i=0; i<plra.length; i++) {
				if(Configs.isSlotBlocked(name, plra[i].get(SRC.Raid.userSortIndex)))
					continue;
				try {
					if(units.length == 0) {
						break;
					}
					
					srrh.loadMap(plra[i]);
					Map map = srrh.getMap();
					
					JsonArray ppt = map.getPresentPlanTypes();
					
					boolean apt = !(ppt.size() == 0);
					
					int[] maxheat = Heatmap.getMaxHeat(map, 5);
					int[][] banned = new int[0][0];
					
					loop:
					while(true) {
						fpt = null;
						Unit unit = findUnit(units, apt, ppt);
						if(unit == null) unit = findUnit(units, false, ppt);
						if(unit == null) break;
						JsonArray allowedPlanTypes = new JsonArray();
						allowedPlanTypes.add(fpt);
						
						while(true) {
							int[] pos = Pathfinding.search(MapConv.asField(map, unit.canFly(), allowedPlanTypes, maxheat, banned));
							
							if(pos == null) {
								if(fpt == null) break loop;
								ppt.remove(new JsonPrimitive(fpt));
								continue loop;
							}
							
							String err = srrh.placeUnit(plra[i], unit, false, pos, fpt != null);
							
							if(err == null) {
								units = remove(units, unit);
								break loop;
							}
							
							if(err.equals("PERIOD_ENDED") || err.equals("AT_POPULATION_LIMIT")) break loop;
							
							if(banned.length >= 5) {
								StreamRaiders.log(name + ": Run -> raids -> unitPlacement: tdn=" + plra[i].get(SRC.Raid.twitchDisplayName) + ", err=" + err, null);
								break loop;
							}
							
							banned = add(banned, pos);
						}
					}
				} catch (PvPException e) {
					switchRaid(plra[i].get(SRC.Raid.userSortIndex), true, srrh.search(1, 240, false, true, false, null));
					ret = true;
				}
			}
		}
		return ret;
	}
	
	private String fpt = null;
	
	private Unit findUnit(Unit[] units, boolean apt, JsonArray ppt) {
		Unit unit = null;
		for(int j=0; j<units.length; j++) {
			String tfpt = null;
			if(apt) {
				JsonArray pts = units[j].getPlanTypes();
				for(int k=0; k<pts.size(); k++) {
					String pt = pts.get(k).getAsString();
					if(ppt.contains(new JsonPrimitive(pt))) tfpt = pt;
				}
				if(tfpt == null) continue;
			}
			
			if(Arrays.asList(neededUnits).contains(units[j].get(SRC.Unit.unitType))) {
				unit = units[j];
				break;
			}
			if(!Configs.getUnitBoolean(name, units[j].get(SRC.Unit.unitType), Configs.place)) continue;
			if(unit == null) {
				unit = units[j];
				fpt = tfpt;
			} else {
				int rank = Integer.parseInt(units[j].get(SRC.Unit.rank));
				if(rank > Integer.parseInt(unit.get(SRC.Unit.rank))) {
					unit = units[j];
					fpt = tfpt;
				}
			}
		}
		return unit;
	}
	
	private JsonObject rews = new JsonObject();
	
	public JsonObject getRews() {
		return rews;
	}
	
	
	private boolean chests() throws URISyntaxException, IOException, NoInternetException {
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
	
	private boolean captains() throws URISyntaxException, IOException, NoInternetException {
		int uCount = srrh.getUnits(SRC.Helper.all).length;
		Raid[] raids = srrh.getRaids(SRC.Helper.all);
		Set<String> set = bannedCaps.deepCopy().keySet();
		for(String cap : set) 
			if(Time.isAfter(bannedCaps.getAsJsonPrimitive(cap).getAsString(), srrh.getServerTime()))
				bannedCaps.remove(cap);
		
		JsonArray locked = Configs.getArr(name, Configs.locked);
		
		boolean changed = false;
		JsonArray caps = new JsonArray();
		
		boolean ctNull = true;
		while(true) {
			boolean breakout = true;
			boolean[] got = new boolean[] {false, uCount < 5, uCount < 8, !srrh.hasBattlePass()};
			for(int i=0; i<raids.length; i++) {
				if(Configs.isSlotBlocked(name, raids[i].get(SRC.Raid.userSortIndex)))
					continue;
				got[Integer.parseInt(raids[i].get(SRC.Raid.userSortIndex))] = true;
				if(locked.contains(new JsonPrimitive(raids[i].get(SRC.Raid.twitchDisplayName)))) continue;
				if(!raids[i].isSwitchable(srrh.getServerTime(), true, 10)) continue;
				String ct = raids[i].getFromNode(SRC.MapNode.chestType);
				if(ct == null) {
					if(ctNull)
						StreamRaiders.log(name + ": Run -> captains: ct=null", null);
					getCaps(caps);
					ctNull = false;
					bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
					switchRaid(raids[i].get(SRC.Raid.userSortIndex), true, caps);
					changed = true;
					breakout = false;
					continue;
				}
				int loy = Integer.parseInt(raids[i].get(SRC.Raid.pveLoyaltyLevel));
				if((raids[i].isOffline(srrh.getServerTime(), true, 10)) ||
						(ct.contains("bone") || !Configs.getChestBoolean(name, ct)) ||
						((ct.contains("boosted") || ct.contains("super"))
								? loy < Configs.getChestInt(name, Configs.loyChestLoyMin)
								: loy > Configs.getChestInt(name, Configs.normChestLoyMax))) {
					getCaps(caps);
					bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
					switchRaid(raids[i].get(SRC.Raid.userSortIndex), true, caps);
					changed = true;
					breakout = false;
				}
			}
			
			for(int i=0; i<got.length; i++) {
				if(!got[i]) {
					if(Configs.isSlotBlocked(name, ""+i))
						continue;
					getCaps(caps);
					switchRaid(""+i, false, caps);
					changed = true;
					breakout = false;
				}
			}
			
			if(breakout) break;
			
			raids = srrh.getRaids(SRC.Helper.all);
		}
		return changed;
	}
	
	private void getCaps(JsonArray empty) throws URISyntaxException, IOException, NoInternetException {
		if(empty.size() != 0) return;
		int pages = 3;
		for(int i=1; i<pages && i<=30; i++) {
			empty.addAll(srrh.search(i, 6, false, true, false, null));
			pages = srrh.getPagesFromLastSearch();
		}
	}
	
	private void switchRaid(String sortIndex, boolean rem, JsonArray caps) throws URISyntaxException, IOException, NoInternetException {
		JsonObject cap = null;
		int oldLoy = -1;

		JsonArray favs = Configs.getArr(name, Configs.favs);
		boolean fav = false;
		
		for(int i=0; i<caps.size(); i++) {
			JsonObject icap = caps.get(i).getAsJsonObject();
			
			if(bannedCaps.has(icap.getAsJsonPrimitive(SRC.Raid.captainId).getAsString()))
				continue;
			
			if(favs.contains(icap.getAsJsonPrimitive(SRC.Raid.twitchDisplayName))) {
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
			StreamRaiders.log(name+": Run -> switchRaid: err=No captain matches", null);
			return;
		}
		
		if(rem) {
			srrh.switchRaid(cap, sortIndex);
		} else {
			srrh.addRaid(cap, sortIndex);
		}
		caps.remove(cap);
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
