package program;

import java.awt.Color;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Time;
import include.GUI;
import include.GUI.Image;
import include.GUI.Label;
import include.Heatmap;
import include.JsonParser;
import include.Pathfinding;
import program.QuestEventRewards.Quest;
import program.SRR.NoInternetException;
import program.SRR.NotAuthorizedException;
import program.SRRHelper.DungeonException;
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
						try {
							srrh.loadMap(srrh.getRaids()[index]);
						} catch (DungeonException e) {}
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
					} catch (SilentException e) {
					} catch (NotAuthorizedException e3) {
							GUI err = new GUI("User not authorized", 500, 200, MainFrame.getGUI(), null);
							Label l = new Label();
							l.setText("<html>Your Account is not authorized.<br>Please remove and add it again</html>");
							err.addLabel(l);
							err.refresh();
							setRunning(false);
							GUI.setBackground(name+"::start", Color.red);
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
		String part = Debug.print(name+" started round", Debug.run);;
		try {
			if(!isRunning()) return;
			
			part = Debug.print(name+" chests", Debug.run);
			if(chests()) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
			}
			
			part = Debug.print(name+" captains", Debug.run);
			captains();

			int c = 1;
			part = Debug.print(name+" raids", Debug.run);
			while(raids()) {
				Debug.print("[" + name + "] Run runs " + c++, Debug.loop);
				part = Debug.print(name+" captains 2", Debug.run);
				captains();
				part = Debug.print(name+" raids 2", Debug.run);
			}
			
			part = Debug.print(name+" collectEvent", Debug.run);
			collectEvent();
			
			part = Debug.print(name+" claimQuests", Debug.run);
			claimQuests();
			
			part = Debug.print(name+" reloadStore", Debug.run);
			srrh.reloadStore();

			part = Debug.print(name+" store", Debug.run);
			store();

			part = Debug.print(name+" unlock", Debug.run);
			unlock();
			
			part = Debug.print(name+" upgradeUnits", Debug.run);
			upgradeUnits();
			
			int min = Configs.getTime(name, Configs.min);
			int max = Configs.getTime(name, Configs.max);
			
			part = Debug.print(name+" finished round", Debug.run);
			
			sleep((int) Math.round(Math.random()*(max-min)) + min);
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
							if(!e.getClass().equals(SilentException.class))
								StreamRaiders.log("critical error happened for " + name + " at \"" + part + "\" -> skipped this round", e);
						
						GUI.setBackground(name+"::start", Color.green);
						isReloading = false;
						resetDateTime();
						sleep(10);
					} catch (NoInternetException | HttpHostConnectException e2) {
						StreamRaiders.log(name + ": Run -> Maybe your internet connection failed", e2, true);
						setReloading(part, e);
					} catch (NotAuthorizedException e3) {
							GUI err = new GUI("User not authorized", 500, 200, MainFrame.getGUI(), null);
							Label l = new Label();
							l.setText("<html>Your Account is not authorized.<br>Please remove and add it again</html>");
							err.addLabel(l);
							err.refresh();
							setRunning(false);
							GUI.setBackground(name+"::start", Color.red);
					} catch (Exception e1) {
						if(!e1.getClass().equals(SilentException.class))
							StreamRaiders.log("failed to reload srrh for " + name, e1);
						setReloading(part, e);
					}
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	
	public static class SilentException extends Exception {
		private static final long serialVersionUID = 6180078222808617728L;
		public SilentException() {
			super("this is a silent exception");
		}
	}
	
	private void setReloading(String part, Exception e) {
		if(isReloading) {
			GUI.setBackground(name+"::start", Color.red);
			setRunning(false);
		} else {
			if(!e.getClass().equals(SilentException.class))
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

	private void claimQuests() throws NoInternetException {
		srrh.updateQuests();
		
		neededUnits = srrh.getNeededUnitTypesForQuests();
		
		Quest[] quests = srrh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			String err = srrh.claimQuest(quests[i]);
			if(err != null) StreamRaiders.log(name + ": Run -> claimQuests: err=" + err, null);
		}
	}
	
	private List<String> potionsTiers = Arrays.asList("5,11,14,22,29".split(","));
	
	private void collectEvent() throws NoInternetException {
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
	
	private void unlock() throws NoInternetException, SilentException {
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
				StreamRaiders.log(name + ": Run -> unlock: type=" + unlockable[ind].get(SRC.Unit.unitType) + ", err=" + err, null);
			
			ps[ind] = -1;
		}
	}
	
	private JsonObject bought = new JsonObject();
	
	public JsonObject getBoughtItems() {
		return bought;
	}
	
	private void store() throws NoInternetException {
		
		try {
			String err = srrh.buyDungeonChest();
			if(err != null && !(err.equals("after end") || err.equals("not enough keys")))
				StreamRaiders.log(name+" -> Run -> store -> buyDungeonChest: err="+err, null);
			if(err == null) {
				if(bought.has("dungeonchest"))
					bought.addProperty("dungeonchest", bought.get("dungeonchest").getAsInt() + 1);
				else 
					bought.addProperty("dungeonchest", 1);
				
			}
		} catch (NullPointerException e) {}
		
		JsonArray items = srrh.getStoreItems(SRC.Store.notPurchased);
		if(items.size() == 0)
			return;
		
		int[] ps = new int[items.size()];
		String[] types = new String[items.size()];
		for(int i=0; i<items.size(); i++) {
			try {
				types[i] = items.get(i).getAsJsonObject()
						.getAsJsonPrimitive("itemId")
						.getAsString()
						.split("pack")[0]
						.replace("scroll", "")
						.replace("paladin", "alliespaladin");
			
				ps[i] = Configs.getUnitInt(name, types[i], Configs.buy);
				
			} catch (NullPointerException e) {
				JsonElement itemId = items.get(i).getAsJsonObject().getAsJsonPrimitive("itemId");
				
				if(itemId != null) {
					StreamRaiders.log(name + ": Run -> store: item=" + items.get(i).getAsJsonObject().getAsJsonPrimitive("itemId").getAsString() + ", err=item is not correct", e);
				} else {
					StreamRaiders.log(name + ": Run -> store: itemObj=" + items.get(i).getAsJsonObject().toString() + ", err=item dont exist? (Unknown err +10 Points for finding)", e);
				}
				ps[i] = -1;
			}
		}
		
		JsonObject packs = JsonParser.parseObj(StreamRaiders.get("store"));
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = srrh.buyItem(items.get(ind).getAsJsonObject());
			if(err != null && !err.equals("not enough gold"))
				StreamRaiders.log(name + ": Run -> store: item=" + items.get(ind) + ", err=" + err, null);
			
			int amount = packs.get(items.get(ind).getAsJsonObject().get("itemId").getAsString())
					.getAsJsonObject().get("Quantity").getAsInt();
			
			if(bought.has(types[ind]))
				bought.addProperty(types[ind], bought.get(types[ind]).getAsInt() + amount);
			else 
				bought.addProperty(types[ind], amount);
			
			
			ps[ind] = -1;
		}
		
	}
	
	
	private void upgradeUnits() throws NoInternetException, SilentException {
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
					StreamRaiders.log(name + ": Run -> upgradeUnits: type=" + us[ind].get(SRC.Unit.unitType) + " err=" + err, null);
					break;
				}
			}
			
			ps[ind] = -1;
		}
	}
	
	public static final String[] pveloy = "? bronze silver gold".split(" ");
	
	private boolean raids() throws NoInternetException, SilentException, NotAuthorizedException {
		boolean ret = false;
		
		JsonObject favs = Configs.getObj(name, Configs.favs);
		
		
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
					if(Configs.isSlotLocked(name, all[i].get(SRC.Raid.userSortIndex))) {
						GUI.setText(name+"::lockBut::"+i, "\uD83D\uDD12");
						GUI.setBackground(name+"::lockBut::"+i, Color.green);
					} else {
						GUI.setText(name+"::lockBut::"+i, "\uD83D\uDD13");
						GUI.setBackground(name+"::lockBut::"+i, GUI.getDefButCol());
					}
					GUI.setEnabled(name+"::favBut::"+i, true);
					if(favs.has(disName)) {
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
					if(units.length == 0) {
						break;
					}
					Unit[] dunUnits = null;
					try {
						srrh.loadMap(plra[i]);
					} catch (DungeonException e) {
						if(!plra[i].get(SRC.Raid.userSortIndex).equals(Configs.getStr(name, Configs.dungeonSlot)))
							throw e;
						dunUnits = srrh.getUnitsDungeons(plra[i]);
					}
					Map map = srrh.getMap();
					
					JsonArray ppt = map.getPresentPlanTypes();
					
					boolean apt = !(ppt.size() == 0);
					
					int[] maxheat = Heatmap.getMaxHeat(map);
					int[][] banned = new int[0][0];
					
					loop:
					while(true) {
						fpt = null;
						Unit unit = findUnit(dunUnits == null ? units : dunUnits, apt, ppt);
						if(unit == null) unit = findUnit(dunUnits == null ? units : dunUnits, false, ppt);
						if(unit == null) break;
						JsonArray allowedPlanTypes = new JsonArray();
						allowedPlanTypes.add(fpt);
						
						while(true) {
							
							Debug.print("[" + name + "] Run raids " + c++, Debug.loop);
							
							int[] pos = new Pathfinding().search(MapConv.asField(map, unit.canFly(), allowedPlanTypes, maxheat, banned), name);
							
							if(pos == null) {
								if(fpt == null) break loop;
								ppt.remove(new JsonPrimitive(fpt));
								continue loop;
							}
							
							String err = srrh.placeUnit(plra[i], unit, false, pos, fpt != null);
							
							if(err == null) {
								if(dunUnits == null)
									units = remove(units, unit);
								break loop;
							}
							
							if(err.equals("PERIOD_ENDED") || err.equals("AT_POPULATION_LIMIT")) 
								break loop;
							
							if(banned.length >= 5) {
								StreamRaiders.log(name + ": Run -> raids -> unitPlacement: tdn=" + plra[i].get(SRC.Raid.twitchDisplayName) + ", err=" + err, null);
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
							StreamRaiders.log(name + " -> Run -> raids: err=No Captain matches", e1);
						}
					}
				}
			}
			resCaps();
		}
		return ret;
	}
	
	private String fpt = null;
	
	private Unit findUnit(Unit[] units, boolean apt, JsonArray ppt) {
		Unit unit = null;
		
		int[] ps = new int[units.length];
		String[] sps = new String[units.length];
		
		for(int j=0; j<units.length; j++) {
			String tfpt = null;
			if(apt) {
				JsonArray pts = units[j].getPlanTypes();
				for(int k=0; k<pts.size(); k++) {
					String pt = pts.get(k).getAsString();
					if(ppt.contains(new JsonPrimitive(pt)))
						tfpt = pt;
				}
				if(tfpt == null)
					continue;
				
				sps[j] = tfpt;
			}
			
			if(Arrays.asList(neededUnits).contains(units[j].get(SRC.Unit.unitType))) {
				unit = units[j];
				break;
			}
			ps[j] = Configs.getUnitInt(name, units[j].get(SRC.Unit.unitType), Configs.place);
		}
		
		if(unit == null) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind])
					ind = i;
			
			if(ps[ind] > -1) {
				unit = units[ind];
				fpt = sps[ind];
			}
		}
		
		return unit;
	}
	
	private JsonObject rews = new JsonObject();
	
	public JsonObject getRews() {
		return rews;
	}
	
	
	private boolean chests() throws NoInternetException, SilentException, NotAuthorizedException {
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
	
	private boolean captains() throws NoInternetException, SilentException, NotAuthorizedException {
		int uCount = srrh.getUnits(SRC.Helper.all).length;
		Raid[] raids = srrh.getRaids(SRC.Helper.all);
		Set<String> set = bannedCaps.deepCopy().keySet();
		for(String cap : set) 
			if(Time.isAfter(bannedCaps.getAsJsonPrimitive(cap).getAsString(), srrh.getServerTime()))
				bannedCaps.remove(cap);
		
		boolean changed = false;
		
		JsonObject favs = Configs.getObj(name, Configs.favs);
		
		String di = Configs.getStr(name, Configs.dungeonSlot);
		
		boolean ctNull = true;
		int c = 1;
		try {
			while(true) {
				Debug.print("[" + name + "] Run captains " + c++, Debug.loop);
				boolean breakout = true;
				boolean[] got = new boolean[] {false, uCount < 5, uCount < 8, !srrh.hasBattlePass()};
				for(int i=0; i<raids.length; i++) {
					got[Integer.parseInt(raids[i].get(SRC.Raid.userSortIndex))] = true;
					if(Configs.isSlotBlocked(name, raids[i].get(SRC.Raid.userSortIndex)))
						continue;
					if(Configs.isSlotLocked(name, raids[i].get(SRC.Raid.userSortIndex)))
						continue;
					if(favs.has(raids[i].get(SRC.Raid.twitchDisplayName)))
						if(favs.getAsJsonPrimitive(raids[i].get(SRC.Raid.twitchDisplayName)).getAsBoolean())
							if(!raids[i].isOffline(srrh.getServerTime(), false, 10))
								continue;
					if(!raids[i].isSwitchable(srrh.getServerTime(), 10)) 
						continue;
					String ct = raids[i].getFromNode(SRC.MapNode.chestType);
					if(ct == null) {
						if(ctNull)
							StreamRaiders.log(name + ": Run -> captains: ct=null", null);
						ctNull = false;
						bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
						switchRaid(raids[i].get(SRC.Raid.userSortIndex), true, getCaps(raids[i]));
						changed = true;
						breakout = false;
						continue;
					}
					if(raids[i].get(SRC.Raid.userSortIndex).equals(di) && !raids[i].isOffline(srrh.getServerTime(), true, 10) && ct.contains("dungeon"))
						continue;
						
					int loy = Integer.parseInt(raids[i].get(SRC.Raid.pveWins));
					int max = 0;
					try {
						max = Configs.getChestInt(name, ct, Configs.maxc);
					} catch (NullPointerException e) {}
					
					if(		raids[i].isOffline(srrh.getServerTime(), true, 10) ||
							ct.contains("bone") ||
							ct.contains("dungeon") ||
							!Configs.getChestBoolean(name, ct, Configs.enabled) ||
							loy < Configs.getChestInt(name, ct, Configs.minc) ||
							loy > (max < 0 ? Integer.MAX_VALUE : max) ||
							(raids[i].get(SRC.Raid.userSortIndex).equals(di) && !ct.contains("dungeons"))
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
						switchRaid(""+i, false, getCaps(""+i));
						changed = true;
						breakout = false;
					}
				}
				
				if(breakout) break;
				
				raids = srrh.getRaids(SRC.Helper.all);
			}
		} catch (NoCapMatchException e) {
			StreamRaiders.log(name + ": Run -> captains: err=" + e.getMessage(), e);
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
	
	
	private JsonArray getCaps(Raid raid) throws NoInternetException, NoCapMatchException {
		return getCaps(raid.get(SRC.Raid.userSortIndex));
	}
	
	private JsonArray getCaps(String usi) throws NoInternetException, NoCapMatchException {
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
	
	private void searchCaps(JsonArray caps, String mode) throws NoInternetException {
		int pages = 3;
		for(int i=1; i<pages && i<=Configs.getInt(name, Configs.maxPage); i++) {
			caps.addAll(srrh.search(i, 6, false, true, mode, false, null));
			pages = srrh.getPagesFromLastSearch();
		}
	}
	
	private boolean switchRaid(String sortIndex, boolean rem, JsonArray caps) throws NoInternetException, SilentException, NotAuthorizedException {
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
			StreamRaiders.log(name+": Run -> switchRaid: err=No captain matches", null);
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
