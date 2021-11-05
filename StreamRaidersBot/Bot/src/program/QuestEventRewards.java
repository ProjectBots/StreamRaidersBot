package program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Time;
import program.QuestEventRewards.Quest.NoQuestException;
import include.Http.NoConnectionException;

public class QuestEventRewards {
	
	private boolean hasBattlePass = false;
	private JsonObject collected = new JsonObject();
	private int tier = 0;
	private static String currentEvent = null;
	private static boolean isEvent = false;
	
	public boolean isEvent() {
		return isEvent;
	}
	
	private JsonObject questTypes = null;
	private Quest[] quests = null;
	
	public int getEventTier() {
		return tier;
	}
	
	public static String getCurrentEvent() {
		return currentEvent;
	}
	
	synchronized public static void updateCurrentEvent(String serverTime) {
		JsonObject events = Json.parseObj(Options.get("events"));
		for(String key : events.keySet()) {
			JsonObject event = events.getAsJsonObject(key);
			
			if(Time.isAfter(serverTime, event.get("EndTime").getAsString())) 
				continue;

			currentEvent = key;
			isEvent = true;
			return;
		}
		currentEvent = null;
		isEvent = false;
	}
	
	public boolean hasBattlePass() {
		return hasBattlePass;
	}
	
	
	public class Quest {
		
		public class NoQuestException extends Exception {
			private static final long serialVersionUID = 1L;
		}
		
		private JsonObject quest = null;
		private int progress = 0;
		private String slot = null;
		
		public Quest(JsonObject rQuest) throws NoQuestException {
			JsonElement id = rQuest.get("currentQuestId");
			if(!id.isJsonPrimitive())
				throw new NoQuestException();
			
			quest = questTypes.getAsJsonObject(id.getAsString());
			progress = rQuest.getAsJsonPrimitive("currentProgress").getAsInt();
			slot = rQuest.getAsJsonPrimitive("questSlotId").getAsString();
		}
		
		public boolean canClaim() {
			return progress >= quest.getAsJsonPrimitive("GoalAmount").getAsInt();
		}
		
		public String getSlot() {
			return slot;
		}
		
		public String claim(SRR req) throws NoConnectionException {
			JsonElement err = Json.parseObj(req.collectQuestReward(slot)).get(SRC.errorMessage);
			if(err.isJsonPrimitive()) return err.getAsString();
			return null;
		}
		
		public String neededUnit() {
			String type = quest.getAsJsonPrimitive("Type").getAsString();
			if(type.equals("PlaceUnitOfType")) 
				return quest.getAsJsonPrimitive("Objective").getAsString();
			return null;
		}
	}
	
	public String[] getNeededUnitTypesForQuests_old() {
		String[] ret = new String[0];
		for(int i=0; i<quests.length; i++) {
			String type = quests[i].neededUnit();
			if(type != null) ret = add(ret, type);
		}
		return ret;
	}
	
	public List<String> getNeededUnitTypesForQuests() {
		List<String> ret = new ArrayList<>();
		for(int i=0; i<quests.length; i++) {
			String type = quests[i].neededUnit();
			if(type != null) 
				ret.add(type);
		}
		return ret;
	}
	
	
	public void updateQuests(SRR req) throws NoConnectionException {
		questTypes = Json.parseObj(Options.get("quests"));
		
		JsonArray raw = Json.parseObj(req.getUserQuests()).getAsJsonArray("data");
		quests = new Quest[0];
		
		for(int i=0; i<raw.size(); i++) {
			try {
				quests = add(quests, new Quest(raw.get(i).getAsJsonObject()));
			} catch (NoQuestException e) {}
		}
	}
	
	public void updateQuests(JsonArray userQuests) {
		questTypes = Json.parseObj(Options.get("quests"));
		
		quests = new Quest[0];
		
		for(int i=0; i<userQuests.size(); i++) {
			try {
				quests = add(quests, new Quest(userQuests.get(i).getAsJsonObject()));
			} catch (NoQuestException e) {}
		}
	}
	
	public Quest[] getClaimableQuests() {
		Quest[] ret = new Quest[0];
		for(int i=0; i<quests.length; i++) 
			if(quests[i].canClaim()) 
				ret = add(ret, quests[i]);
		return ret;
	}
	
	
	
	public void updateEvent(SRR req) throws NoConnectionException {
		JsonObject fullRaw = Json.parseObj(req.getUserEventProgression());
		String st = fullRaw.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
		JsonArray data = fullRaw.getAsJsonArray("data");
		
		for(int i=0; i<data.size(); i++) {
			JsonObject raw = data.get(i).getAsJsonObject();
			currentEvent = raw.getAsJsonPrimitive("eventUid").getAsString();
			
			if(Time.isAfter(st, Json.parseObj(
					Options.get("events"))
						.getAsJsonObject(currentEvent)
						.getAsJsonPrimitive("EndTime")
						.getAsString())) 
				continue;
			
			
			isEvent = true;
			
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
		isEvent = false;
	}
	
	
	public void updateEvent(String serverTime, JsonArray userEventProgression) {
		updateCurrentEvent(serverTime);
		for(int i=0; i<userEventProgression.size(); i++) {
			JsonObject raw = userEventProgression.get(i).getAsJsonObject();
			
			if(!raw.getAsJsonPrimitive("eventUid").getAsString().equals(currentEvent)) 
				continue;

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
	
	public String collectEvent(int p, boolean battlePass, SRR req) throws NoConnectionException {
		if(!canCollectEvent(p, battlePass)) return "cant collect";
		JsonObject raw = Json.parseObj(req.grantEventReward(currentEvent, ""+p, battlePass));
		JsonElement err = raw.get(SRC.errorMessage);
		return err.isJsonPrimitive() ? err.getAsString() : null;
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
				rew = "skins";
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
	
	
	private <T> T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
	
}
