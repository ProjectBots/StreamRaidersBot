package program;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.JsonParser;

public class QuestEventRewards {
	
	private boolean hasBattlePass = false;
	private JsonObject collected = new JsonObject();
	private int tier = 0;
	private String currentEvent = null;
	
	public int getEventTier() {
		return tier;
	}
	
	public boolean hasBattlePass() {
		return hasBattlePass;
	}
	
	
	public void updateEvent(SRR req) {
		JsonObject raw = JsonParser.json(req.getUserEventProgression()).getAsJsonArray("data").get(0).getAsJsonObject();
		
		currentEvent = raw.getAsJsonPrimitive("eventUid").getAsString();
		hasBattlePass = raw.getAsJsonPrimitive("hasBattlePass").getAsInt() == 1;
		tier = raw.getAsJsonPrimitive("currentTier").getAsInt();
		
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
		
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) {
		if(p >= tier || p <= 0) return false;
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
		if(!e.isJsonPrimitive()) return true;
		return !e.getAsBoolean();
	}
	
	public String collectEvent(int p, boolean battlePass, SRR req) {
		if(!canCollectEvent(p, battlePass)) return "cant collect";
		
		JsonElement err = JsonParser.json(req.grantEventReward(currentEvent, ""+p, battlePass)).get("errorMessage");
		
		if(err.isJsonPrimitive()) return err.getAsString();
		
		return null;
	}
	
}
