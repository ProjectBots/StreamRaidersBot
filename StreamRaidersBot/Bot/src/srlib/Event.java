package srlib;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import otherlib.Logger;
import otherlib.Options;
import srlib.store.Store;

public class Event {

	private static String currentEvent = null;
	
	public static JsonArray genTiersFromData(JsonObject data) {
		updateCurrentEvent(data.getAsJsonObject("Events"));
		String event = currentEvent;
		if(event == null)
			event = getNextEvent(data, data.getAsJsonObject("Events"));
		
		JsonArray currentTiers = new JsonArray();
		if(event == null)
			return currentTiers;
		
		currentTiers.add(0);
		outer:
		{
			JsonObject tiers = data.getAsJsonObject("EventTiers");

			//	sr seems to not have a constant naming convention here
			//	therefore we get the prefix by searching
			for(String key : tiers.keySet()) {
				JsonObject tier = tiers.getAsJsonObject(key);
				if(tier.get("Streamer").getAsBoolean())
					continue;
				
				if(!tier.get("EventUid").getAsString().equals(event))
					continue;
				
				//	remove the last numbers
				event = key.replaceFirst("[0-9]+$", "");
				
				int count = 0;
				for(String n : tiers.keySet())
					if(n.matches(Pattern.quote(event)+"\\d+"))
						count++;
				
				for(int i=1; i<=count; i++)
					currentTiers.add(tiers.get(event+i));
				
				break outer;
			}
			Logger.print("Event -> genTiersFromData: err=failed to find tiers, event="+event, Logger.runerr, Logger.error, null, null, true);
		}
		return currentTiers;
	}
	
	public static String genEventBadgesFromData(JsonObject data) {
		StringBuffer ret = new StringBuffer();
		for(String b : data.getAsJsonObject("EventBadges").keySet())
			ret.append(b).append(",");
		return ret.deleteCharAt(ret.length()-1).toString();
	}
	
	public static int getEventTierSize() {
		return Json.parseArr(Options.get("eventTiers")).size();
	}
	
	

	public static String getCurrentEvent() {
		return currentEvent;
	}
	
	public static boolean isEvent() {
		return currentEvent != null;
	}
	
	
	private boolean hasBattlePass = false;
	private boolean[] collectedBasic = new boolean[0];
	private boolean[] collectedPass = new boolean[0];
	
	private int tier = 0;
	
	public int getEventTier() {
		return tier;
	}
	
	private static void updateCurrentEvent(JsonObject events) {
		for(String key : events.keySet()) {
			JsonObject event = events.getAsJsonObject(key);
			
			if(Time.isBeforeServerTime(event.get("EndTime").getAsString()) || Time.isAfterServerTime(event.get("StartTime").getAsString())) 
				continue;

			currentEvent = key;
			return;
		}
		currentEvent = null;
	}
	
	private static String getNextEvent(JsonObject data, JsonObject events) {
		for(String key : events.keySet()) {
			JsonObject event = events.getAsJsonObject(key);
			
			if(Time.isBeforeServerTime(event.get("EndTime").getAsString())) 
				continue;

			return key;
		}
		return null;
	}
	
	public boolean hasBattlePass() {
		return hasBattlePass;
	}
	
	
	public void updateEventProgression(JsonArray userEventProgression) {
		
		int size = Integer.parseInt(Options.get("eventTiersSize"));
		if(size != collectedBasic.length) {
			collectedBasic = new boolean[size];
			collectedPass = new boolean[size];
		}
		
		updateCurrentEvent(Json.parseObj(Options.get("events")));
		for(int i=0; i<userEventProgression.size(); i++) {
			JsonObject raw = userEventProgression.get(i).getAsJsonObject();
			
			if(!raw.get("eventUid").getAsString().equals(currentEvent)) 
				continue;

			hasBattlePass = raw.get("hasBattlePass").getAsInt() == 1;
			tier = raw.get("currentTier").getAsInt();
			
			JsonElement rbc = raw.get("basicRewardsCollected");
			if(rbc.isJsonPrimitive()) {
				String[] bc = rbc.getAsString().split(",");
				for(int j=0; j<bc.length; j++)
					collectedBasic[Integer.parseInt(bc[j])] = true;
			}
			
			JsonElement rpc = raw.get("battlePassRewardsCollected");
			if(rpc.isJsonPrimitive()) {
				String[] pc = rpc.getAsString().split(",");
				for(int j=0; j<pc.length; j++)
					collectedPass[Integer.parseInt(pc[j])] = true;
			}
			
			return;
		}
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) {
		if(p >= tier || p <= 0)
			return false;
		
		if(battlePass && !hasBattlePass)
			return false;
		
		return battlePass ? !collectedPass[p] : !collectedBasic[p];
	}
	
	
	private static final Hashtable<String, String> rewardRenames = new Hashtable<String, String>() {
		private static final long serialVersionUID = 8966433027339581222L;
		{
		put("epicpotion", Store.potions.get());
		put("goldpiecebag", Store.gold.get());
		put("cooldown", Store.meat.get());
	}};
	
	private static final HashSet<String> rewardUnknown = new HashSet<String>() {
		private static final long serialVersionUID = -8138638718616026037L;
		{
		add("commonscroll");
		add("uncommonscroll");
		add("rarescroll");
	}};
	
	public JsonObject collectEvent(int p, boolean battlePass, JsonObject grantEventReward) {
		JsonObject ret = new JsonObject();
		JsonElement err = grantEventReward.get(SRC.errorMessage);
		
		if(err.isJsonPrimitive()) {
			ret.add(SRC.errorMessage, err);
			return ret;
		}
		
		if(battlePass)
			collectedPass[p] = true;
		else
			collectedBasic[p] = true;
		
		
		JsonArray tiers = Json.parseArr(Options.get("eventTiers"));
		JsonObject tier = tiers.get(p).getAsJsonObject();
		String rew;
		int qty;
		if(battlePass) {
			rew = tier.get("BattlePassRewards").getAsString();
			qty = tier.get("BattlePassAmount").getAsInt();
		} else {
			rew = tier.get("BasicRewards").getAsString();
			qty = tier.get("BasicAmount").getAsInt();
		}
		
		if(rew.equals("")) {
			rew = "badges";
			qty = 1;
		} else if(rew.startsWith("skin")) {
			rew = "skin";
		} else if(rewardUnknown.contains(rew)) {
			rew = grantEventReward.get("data").getAsString();
		} else if(rewardRenames.containsKey(rew)) {
			rew = rewardRenames.get(rew);
		}
		
		ret.addProperty("reward", rew);
		ret.addProperty("quantity", qty);
		
		return ret;
	}
	
	
	
}
