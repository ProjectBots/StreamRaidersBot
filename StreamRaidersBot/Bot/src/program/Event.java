package program;

import java.util.HashSet;
import java.util.Hashtable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Time;

public class Event {

	private static String currentEvent = null;
	
	public static String getCurrentEvent() {
		return currentEvent;
	}
	
	public static boolean isEvent() {
		return currentEvent == null;
	}
	
	
	private boolean hasBattlePass = false;
	private JsonObject collected = new JsonObject();
	private int tier = 0;
	
	
	public int getEventTier() {
		return tier;
	}
	
	synchronized public static void updateCurrentEvent(String serverTime) {
		JsonObject events = Json.parseObj(Options.get("events"));
		for(String key : events.keySet()) {
			JsonObject event = events.getAsJsonObject(key);
			
			if(Time.isAfter(serverTime, event.get("EndTime").getAsString())) 
				continue;

			currentEvent = key;
			return;
		}
		currentEvent = null;
	}
	
	public boolean hasBattlePass() {
		return hasBattlePass;
	}
	
	
	public void updateEvent(String serverTime, JsonArray userEventProgression) {
		updateCurrentEvent(serverTime);
		for(int i=0; i<userEventProgression.size(); i++) {
			JsonObject raw = userEventProgression.get(i).getAsJsonObject();
			
			if(!raw.get("eventUid").getAsString().equals(currentEvent)) 
				continue;

			hasBattlePass = raw.get("hasBattlePass").getAsInt() == 1;
			tier = raw.get("currentTier").getAsInt();
			
			JsonElement rbc = raw.get("basicRewardsCollected");
			if(rbc.isJsonPrimitive()) {
				String[] bc = rbc.getAsString().split(",");
				for(String t : bc) {
					JsonObject c = new JsonObject();
					c.addProperty("basic", true);
					collected.add(t, c);
				}
			}
			
			JsonElement rpc = raw.get("battlePassRewardsCollected");
			if(rpc.isJsonPrimitive()) {
				String[] pc = rpc.getAsString().split(",");
				for(String t : pc) {
					JsonElement je = collected.get(t);
					if(je == null) {
						JsonObject c = new JsonObject();
						c.addProperty("pass", true);
						collected.add(t, c);
					} else {
						je.getAsJsonObject().addProperty("pass", true);
					}
				}
			}
			
			return;
		}
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) {
		if(p >= tier || p <= 0)
			return false;
		JsonElement je = collected.get(""+p);
		if(je == null) return true;
		JsonObject jo = je.getAsJsonObject();
		JsonElement e;
		if(battlePass) {
			if(!hasBattlePass) return false;
			e = jo.get("pass");
		} else {
			e = jo.get("basic");
		}
		if(e == null || !e.isJsonPrimitive()) 
			return true;
		return !e.getAsBoolean();
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
		
		if(collected.has(""+p)) {
			collected.getAsJsonObject(""+p).addProperty(battlePass ? "pass" : "basic", true);
		} else {
			JsonObject tmp = new JsonObject();
			tmp.addProperty(battlePass ? "pass" : "basic", true);
			collected.add(""+p, tmp);
		}
		
		JsonObject tiers = Json.parseObj(Options.get("eventTiers"));
		for(String key : tiers.keySet()) {
			if(!key.matches("^[A-z]+"+p+"$"))
				continue;
			
			String rew;
			int qty;
			if(battlePass) {
				rew = tiers.getAsJsonObject(key).get("BattlePassRewards").getAsString();
				qty = tiers.getAsJsonObject(key).get("BattlePassAmount").getAsInt();
			} else {
				rew = tiers.getAsJsonObject(key).get("BasicRewards").getAsString();
				qty = tiers.getAsJsonObject(key).get("BasicAmount").getAsInt();
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
			
			break;
		}
		
		
		return ret;
	}
	
	
	
}
