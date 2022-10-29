package srlib.viewer;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import otherlib.Logger;
import otherlib.Options;
import srlib.RaidType;
import srlib.Reward;
import srlib.Time;
import srlib.store.Store;

public class Raid {
	
	@Override
	public String toString() {
		return twitchDisplayName;
	}
	
	public final String raidId, captainId, nodeType, chestType, twitchDisplayName, battleground, nodeId,
						twitchUserImage, twitchUserName, allyBoons, placementsSerialized, users;
	public final long creationDate, nextUnitPlaceTime;
	public final boolean ended, battleResult, hasViewedResults, isPlaying, isLive, postBattleComplete, hasRecievedRewards, placementEnded, placedUnit, isCodeLocked;
	public final int pveWins, pveLoyaltyLevel, userSortIndex, dungeonStreak;
	
	
	public Raid(JsonObject raid, String cid, int slot) {
		this.cid = cid;
		this.slot = slot;
		
		JsonElement je;
		
		type = RaidType.parseInt(raid.get("type").getAsInt());
		
		this.raidId = raid.get("raidId").getAsString();
		this.captainId = raid.get("captainId").getAsString();
		this.twitchDisplayName = raid.get("twitchDisplayName").getAsString();
		this.battleground = raid.get("battleground").getAsString();
		this.twitchUserImage = raid.get("twitchUserImage").getAsString();
		this.twitchUserName = raid.get("twitchUserName").getAsString();
		je = raid.get("allyBoons");
		this.allyBoons = je.isJsonPrimitive() ? je.getAsString() : null;
		je = raid.get("placementsSerialized");
		this.placementsSerialized = je != null && je.isJsonPrimitive() ? je.getAsString() : null;
		je = raid.get("users");
		this.users = je != null && je.isJsonArray() ? je.toString() : null;
		
		this.creationDate = Time.parse(raid.get("creationDate").getAsString());
		je = raid.get("lastUnitPlacedTime");
		this.placedUnit = je != null && je.isJsonPrimitive();
		this.nextUnitPlaceTime = placedUnit ? Time.plus(je.getAsString(), type.placementCooldownDuration+10) : 0;
		
		
		this.hasViewedResults = raid.get("hasViewedResults").getAsInt() == 1;
		this.isPlaying = raid.get("isPlaying").getAsInt() == 1;
		this.isLive = raid.get("isLive").getAsInt() == 1;
		this.postBattleComplete = raid.get("postBattleComplete").getAsInt() == 1;
		je = raid.get("battleResult");
		this.battleResult = je.isJsonPrimitive() && je.getAsBoolean();

		je = raid.get("hasRecievedRewards");
		this.hasRecievedRewards = je.isJsonPrimitive() && je.getAsInt() == 1;
		this.placementEnded = raid.get("placementEndTime").isJsonPrimitive() || Time.isBeforeServerTime(creationDate + type.raidDuration);
		this.ended = raid.get("endTime").isJsonPrimitive();
		this.isCodeLocked = raid.get("isCodeLocked").getAsBoolean();
		
		this.pveWins = raid.get("pveWins").getAsInt();
		this.pveLoyaltyLevel = raid.get("pveLoyaltyLevel").getAsInt();
		this.userSortIndex = raid.get("userSortIndex").getAsInt();
		je = raid.get("dungeonStreak");
		this.dungeonStreak = je != null && je.isJsonPrimitive() ? je.getAsInt() : -1;

		je = raid.get("nodeId");
		if(je.isJsonPrimitive()) {
			this.nodeId = je.getAsString();
			JsonObject node = Json.parseObj(Options.get("mapNodes")).getAsJsonObject(nodeId);
			this.nodeType = node.get("NodeType").getAsString();
			this.chestType = node.get("ChestType").getAsString();
		} else {
			this.nodeId = null;
			this.nodeType = null;
			this.chestType = "nochest";
		}
		
	}
	
	private final String cid;
	private final int slot;
	
	private JsonObject userDungeonInfo = null;
	
	public final RaidType type;
	
	public boolean hasUserDungeonInfo() {
		return userDungeonInfo == null;
	}
	
	public void addUserDungeonInfo(JsonObject data) {
		userDungeonInfo = data;
	}
	
	public JsonObject getUserDungeonInfo() {
		return userDungeonInfo.deepCopy();
	}
	
	public boolean isSwitchable(int treshold) {
		if(!placedUnit)
			return true;
		return isOffline(false, treshold);
	}
	
	public boolean isOffline(boolean whenNotLive, int treshold) {
		if(hasRecievedRewards && (!isPlaying || (whenNotLive && !isLive)))
			return true;
		
		if(treshold == -1) 
			return false;
		
		return Time.isBeforeServerTime(creationDate + type.raidDuration + treshold);
	}
	
	public boolean isReward() {
		return ended && !hasRecievedRewards;
	}
	
	private static final String[][] AWARDED_REWARDS = new String[][] {
		{Store.gold.get(), "goldAwarded"},
		{"token", "eventTokensReceived"},
		{Store.potions.get(), "potionsAwarded"},
		{Store.keys.get(), "keysAwarded"},
		{Store.bones.get(), "bonesAwarded"}
	};
	
	private final static Reward SALVAGE_CHEST_REWARD = new Reward("chestsalvage", 1);
	
	public ArrayList<Reward> getChest(JsonObject raidStats) {
		
		ArrayList<Reward> ret = new ArrayList<>();
		
		try {
			if(!raidStats.get("battleResult").getAsBoolean())
				return ret;
			
			JsonElement chest = raidStats.get("chestAwarded");
			if(type != RaidType.DUNGEON)
				ret.add(chest != null && chest.isJsonPrimitive() && !chest.getAsString().equals("") ? new Reward(chest.getAsString(), 1) : SALVAGE_CHEST_REWARD);
			
			
			for(int i=0; i<AWARDED_REWARDS.length; i++) {
				int ityp = raidStats.get(AWARDED_REWARDS[i][1]).getAsInt();
				if(ityp != 0) 
					ret.add(new Reward(AWARDED_REWARDS[i][0], ityp));
			}
			
			JsonElement rawRews = raidStats.get("viewerChestRewards");
			if(rawRews != null && rawRews.isJsonArray()) {
				JsonArray rews = rawRews.getAsJsonArray();
				JsonElement bonus = raidStats.get("bonusItemReceived");
				if(bonus != null && bonus.isJsonPrimitive() && !bonus.getAsString().equals(""))
					rews.add(bonus);
				
				for(int i=0; i<rews.size(); i++)
					ret.add(Reward.genChestReward(rews.get(i).getAsString(), cid, slot));
			}
		} catch (UnsupportedOperationException e) {
			Logger.printException("Raid -> getChest: err=failed to get chest, rawData="+raidStats.toString(), e, Logger.runerr, Logger.error, cid, slot, true);
		}
		return ret;
	}

	
	
	public boolean canPlaceUnit() {
		return !placementEnded
				&& Time.isBeforeServerTime(nextUnitPlaceTime)
				&& Time.isAfterServerTime(creationDate + type.raidDuration)
				&& Time.isBeforeServerTime(creationDate + type.planningPeriodDuration);
	}
	

}
