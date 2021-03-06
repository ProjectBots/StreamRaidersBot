package program;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI;

public class Run {

	
	private String cookies = "";
	private static String clientVersion = StreamRaiders.get("clientVersion");
	
	private String name = "";
	
	public Run(String name, String cookies) {
		this.cookies = cookies;
		this.name = name;
	}
	
	
	
	private boolean first = true;
	
	public SRRHelper srrh = null;
	
	
	public void showMap(int index) {
		if(srrh == null) return;
		try {
			srrh.loadMap(srrh.getRaids()[index]);
			MapConv.asGui(srrh.getMap());
		} catch (Exception e) {}
	}
	
	
	private boolean running = false;
	
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
		if(running) {
			t= new Thread(new Runnable() {
				@Override
				public void run() {
					srrh = new SRRHelper(cookies, clientVersion);
					runs();
				}
			});
			t.start();
		}
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
				
			part = "raids 1";
			raids();
						
			srrh.reloadStore();
			
			part = "store";
			store();
			
			
			part = "units";
			upgradeUnits();
			
			
			if(first) {
				first = false;
				try {
					part = "sleeping first";
					sleep(10);
					return;
				} catch (Exception e) {}
			}
			
			part = "captains";
			if(captains()) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
				part = "raids 2";
				raids();
			}
			
			sleep((int) Math.round(Math.random()*620) + 100);
		} catch (Exception e) {
			System.err.println("critical error happened for " + name + " at \"" + part + "\" -> skipped this round");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
			System.out.println("reload srrh for " + name);
			try {
				srrh = new SRRHelper(cookies, clientVersion);
				System.out.println("completed reloading srrh for " + name);
				sleep(10);
			} catch (Exception e1) {
				System.err.println("failed to reload srrh for " + name);
				GUI.setBackground(name+"::start", Color.red);
				setRunning(false);
			}
			
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
	
	private void store() {
		JsonArray items = srrh.getStoreItems(SRC.Store.notPurchased);
		for(int i=0; i<items.size(); i++) {
			String err = srrh.buyItem(items.get(i).getAsJsonObject());
			if(err != null) {
				if(!(err.equals("not enough gold")))
				System.err.println(name + " -> couldnt buy " + items.get(i) + " because " + err);
			}
		}
	}
	
	private void upgradeUnits() {
		Unit[] us = srrh.getUnits(SRC.Helper.canUpgradeUnit);
		for(int i=0; i<us.length; i++) {
			String err = srrh.upgradeUnit(us[i], null);
			if(err != null) {
				if(!(err.equals("no specUID") || err.equals("cant upgrade unit"))) {
					System.out.println(name + " -> u lvl err: " + err);
					break;
				}
			}
		}
	}
	
	private String[] pveloy = new String[] {"?", "bronze", "silver", "gold"};
	
	private void raids() {
		Hashtable<String, String> black = MainFrame.getBlacklist(name);
		
		Unit[] units = srrh.getUnits(SRC.Helper.canPlaceUnit);
		
		Raid[] plra = srrh.getRaids(SRC.Helper.canPlaceUnit);
		
		Raid[] all = srrh.getRaids();
		for(int i=0; i<4; i++) {
			if(i<all.length) {
				int wins = Integer.parseInt(all[i].get(SRC.Raid.pveWins));
				int lvl = Integer.parseInt(all[i].get(SRC.Raid.pveLoyaltyLevel));
				if(lvl == 0) lvl = 3;
				GUI.setText(name+"::name::"+i, all[i].get(SRC.Raid.twitchDisplayName) + " - " + wins + "|" + pveloy[lvl]);
			} else {
				GUI.setText(name+"::name::"+i, "");
			}
		}
		
		if(plra.length != 0) {
			
			for(int i=0; i<plra.length; i++) {
				
				if(units.length == 0) {
					break;
				}
				
				Unit unit = null;
				for(int j=0; j<units.length; j++) {
					if(Boolean.parseBoolean(black.get(units[j].get(SRC.Unit.unitType)))) continue;
					if(unit == null) {
						unit = units[j];
					} else {
						int rank = Integer.parseInt(units[j].get(SRC.Unit.rank));
						if(rank > Integer.parseInt(unit.get(SRC.Unit.rank))) {
							unit = units[j];
						}
					}
				}
				if(unit == null) {
					break;
				}
				
				
				srrh.loadMap(plra[i]);
				JsonObject[][] map = srrh.getMap();
				
				
				int[] maxheat = Heatmap.getMaxHeat(map, 5);
				
				int[][] banned = new int[0][0];
				
				while(true) {
					int[] pos = Heatmap.getNearest(map, maxheat, banned);
					
					String err0 = srrh.placeUnit(plra[i], unit, false, pos[0], pos[1]);
					
					if(err0 == null) {
						units = remove(units, unit);
						break;
					}
					try {
						if(err0.equals("OVER_OBSTACLE")) {
							Map.whiteObst(map[pos[0]][pos[1]].getAsJsonObject("data").getAsJsonPrimitive("ObstacleName").getAsString());
						}
					} catch (Exception e) {}
					
					
					if(banned.length >= 5) break;
					
					
					banned = add(banned, pos);
				}
			}
		}
	}
	
	private JsonObject rews = new JsonObject();
	
	public JsonObject getRews() {
		return rews;
	}
	
	private boolean chests() {
		Raid[] rera = srrh.getRaids(SRC.Helper.isReward);
		if(rera.length != 0) {
			
			for(int i=0; i<rera.length; i++) {
				
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
	
	private boolean captains() {
		Raid[] offRaids = srrh.getRaids(SRC.Helper.isOffline);
		if(offRaids.length != 0) {
			for(int i=0; i<offRaids.length; i++) {
				JsonArray caps = srrh.search(1, 20, true, true, false, null);
				JsonObject cap = null;
				if(caps.size() != 0) {
					for(int j=0; j<caps.size(); j++) {
						int loyalty = Integer.parseInt(caps.get(j).getAsJsonObject().getAsJsonPrimitive("pveLoyaltyLevel").getAsString());
						try {
							int oldLoy = Integer.parseInt(cap.getAsJsonPrimitive("pveLoyaltyLevel").getAsString());
							if(loyalty > oldLoy) {
								cap = caps.get(j).getAsJsonObject();
							}
							if(loyalty == 0) {
								cap = caps.get(j).getAsJsonObject();
								break;
							}
						} catch(Exception e) {
							cap = caps.get(j).getAsJsonObject();
						}
					}
				} else {
					cap = srrh.search(1, 6, false, true, false, "stream raiders").get(0).getAsJsonObject();
					srrh.setFavorite(cap, true);
				}
				srrh.switchRaid(cap, offRaids[i].get(SRC.Raid.userSortIndex));
			}
			return true;
		}
		return false;
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
