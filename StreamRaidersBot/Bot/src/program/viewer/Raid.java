package program.viewer;

import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Time;
import program.Debug;
import program.Options;
import program.SRC;
import program.Store;

public class Raid {
	
	@Override
	public String toString() {
		return get(SRC.Raid.twitchDisplayName);
	}

	private final JsonObject raid;
	private final JsonObject node;
	
	private final String cid;
	private final int slot;
	
	
	public JsonObject getRaidJsonObject() {
		return raid;
	}
	
	public String getFromNode(String con) {
		if(node == null) return null;
		return node.get(con).getAsString();
	}
	
	
	public Raid(JsonObject raid, String cid, int slot) {
		this.raid = raid;
		this.slot = slot;
		this.node = Json.parseObj(Options.get("mapNodes")).getAsJsonObject(get(SRC.Raid.nodeId));
		this.cid = cid;
	}
	
	public boolean isDungeon() {
		JsonElement node = raid.get(SRC.Raid.nodeId);
		if(node == null || !node.isJsonPrimitive())
			return false;
		return node.getAsString().contains("dungeon");
	}
	
	public boolean isVersus() {
		JsonElement node = raid.get(SRC.Raid.nodeId);
		if(node == null || !node.isJsonPrimitive())
			return false;
		String nodeId = node.getAsString();
		return nodeId.contains("pvp") || nodeId.contains("calibration");
	}
	
	public String get(String con) {
		try {
			if(con.equals(SRC.Raid.users)) 
				return raid.getAsJsonArray(con).toString();
			return raid.get(con).getAsString();
		} catch (ClassCastException | NullPointerException | UnsupportedOperationException e) {
			return null;
		}
	}
	
	public boolean isSwitchable(String serverTime, int treshold) {
		JsonElement je = raid.get(SRC.Raid.lastUnitPlacedTime);
		if(je.isJsonNull()) return true;
		return isOffline(serverTime, false, treshold);
	}
	
	public boolean isOffline(String serverTime, boolean whenNotLive, int treshold) {
		String hvr = get(SRC.Raid.hasViewedResults);
		String hrr = get(SRC.Raid.hasRecievedRewards);
		String ip = get(SRC.Raid.isPlaying);
		String il = get(SRC.Raid.isLive);
		ifc:
		if(hvr != null && hrr != null && ip != null && il != null) {
			if(!hvr.contains("1")) break ifc;
			if(!hrr.contains("1")) break ifc;
			if(!(ip.contains("0") || (whenNotLive && il.contains("0")))) break ifc;
			return true;
		}
		if(treshold == -1) return false;
		
		return Time.isAfter(serverTime, Time.plusMinutes(get(SRC.Raid.creationDate), (get(SRC.Raid.nodeId).contains("dungeon") ? 6 : 30) + treshold));
		
	}
	
	public boolean isReward() {
		JsonElement pbc = raid.get("postBattleComplete");
		JsonElement hrr = raid.get("hasRecievedRewards");
		if(pbc == null || !pbc.isJsonPrimitive() || hrr == null || !hrr.isJsonPrimitive()) return false;
		
		if(!pbc.getAsString().contains("1")) return false;
		if(!hrr.getAsString().contains("0")) return false;
		
		return true;
	}
	
	private static final String[][] typChestRews = new String[][] {
		{Store.gold.get(), "goldAwarded"},
		{"token", "eventTokensReceived"},
		{Store.potions.get(), "potionsAwarded"},
		{Store.keys.get(), "keysAwarded"},
		{Store.bones.get(), "bonesAwarded"}
	};
	
	public static final Hashtable<String, String> typViewChestRews = new Hashtable<String, String>() {
		private static final long serialVersionUID = 1L;
		{
		put("goldbag", Store.gold.get());
		put("eventtoken", "token");
		put("cooldown", Store.meat.get());
		put("potion", Store.potions.get());
		put("epicpotion", Store.potions.get());
	}};
	
	
	public JsonObject getChest(JsonObject raidStats) {
		JsonObject ret = new JsonObject();
		
		try {
			if(!raidStats.get("battleResult").getAsBoolean())
				return ret;
			
			JsonElement chest = raidStats.get("chestAwarded");
			if(chest != null && chest.isJsonPrimitive())
				ret.addProperty(chest.getAsString(), 1);
			else
				ret.addProperty("chestsalvage", 1);
			
			
			for(int i=0; i<typChestRews.length; i++) {
				int ityp = raidStats.get(typChestRews[i][1]).getAsInt();
				if(ityp != 0) 
					ret.addProperty(typChestRews[i][0], ityp);
			}
			
			updateChestRews();
			
			JsonElement rawRews = raidStats.get("viewerChestRewards");
			if(rawRews != null && rawRews.isJsonArray()) {
				JsonArray rews = rawRews.getAsJsonArray();
				JsonElement bonus = raidStats.get("bonusItemReceived");
				if(bonus != null && bonus.isJsonPrimitive() && !bonus.getAsString().equals(""))
					rews.add(bonus);
				
				for(int i=0; i<rews.size(); i++) {
					Reward r = new Reward(rews.get(i).getAsString(), cid, slot);
					
					if(ret.has(r.name))
						ret.addProperty(r.name, ret.get(r.name).getAsInt() + r.quantity);
					else {
						ret.addProperty(r.name, r.quantity);
					}
				}
			}
		} catch (UnsupportedOperationException e) {
			Debug.printException("Raid -> getChest: err=failed to get chest, rawData="+raidStats.toString(), e, Debug.runerr, Debug.error, cid, slot, true);
		}
		return ret;
	}

	private static JsonObject chest_rews = null;
	
	public static void updateChestRews() {
		chest_rews = Json.parseObj(Options.get("rewards"));
	}
	
	public static class Reward {
		public final String name;
		public final int quantity;
		public Reward(String reward, String cid, int slot) {
			String[] rew = reward.split("\\|");
			quantity = chest_rews.getAsJsonObject(rew[0]).get("Quantity").getAsInt();
			
			String frew = rew[0].split("_")[0];
			if(typViewChestRews.containsKey(frew)) {
				name = typViewChestRews.get(frew);
			} else if(rew.length == 2) {
				name = rew[1];
			} else if(rew[0].contains("skin")) {
				name = "skin";
			} else {
				Debug.print("Raid -> Reward -> const.: err=failed to determine reward, reward=" + reward, Debug.runerr, Debug.error, cid, slot, true);
				name = "unknown";
			}
		}
	}
	
	
	public boolean canPlaceUnit(String serverTime) {
		if(raid.get(SRC.Raid.endTime).isJsonPrimitive()) return false;
		if(raid.get(SRC.Raid.startTime).isJsonPrimitive()) return false;

		JsonElement date = raid.get(SRC.Raid.lastUnitPlacedTime);
		String node = raid.get(SRC.Raid.nodeId).getAsString();
		if(date.isJsonPrimitive()) 
			if(Time.isAfter(Time.plusMinutes(date.getAsString(), node.contains("dungeon") ? 2 : 5), serverTime)) 
				return false;

		if(Time.isAfter(serverTime, Time.plusMinutes(raid.get(SRC.Raid.creationDate).getAsString(), node.contains("dungeon") ? 6 : 30))) 
			return false;

		if(node.contains("dungeon") && Time.isAfter(Time.plusSeconds(raid.get(SRC.Raid.creationDate).getAsString(), 60), serverTime))
			return false;
		
		return true;
	}
	

}
