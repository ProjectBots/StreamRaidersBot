package program;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.JsonParser;
import program.QuestEventRewards.Quest.NoQuestException;

public class QuestEventRewards {
	
	private boolean hasBattlePass = false;
	private JsonObject collected = new JsonObject();
	private int tier = 0;
	private String currentEvent = null;
	
	private JsonObject questTypes = null;
	private Quest[] quests = null;
	
	public int getEventTier() {
		return tier;
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
			if(!id.isJsonPrimitive()) throw new NoQuestException();
			
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
		
		public String claim(SRR req) {
			JsonElement err = JsonParser.json(req.collectQuestReward(slot)).get("errorMessage");
			if(err.isJsonPrimitive()) return err.getAsString();
			return null;
		}
		
		public String neededUnit() {
			String u = quest.getAsJsonPrimitive("UnitTypeRequirement").getAsString();
			if(!u.equals("")) return u;
			return null;
		}
	}
	
	public String[] getNeededUnitTypesForQuests() {
		String[] ret = new String[0];
		for(int i=0; i<quests.length; i++) {
			String type = quests[i].neededUnit();
			if(type != null) ret = add(ret, type);
		}
		return ret;
	}
	
	
	public void updateQuests(SRR req) {
		questTypes = JsonParser.json(StreamRaiders.get("quests"));
		
		JsonArray raw = JsonParser.json(req.getUserQuests()).getAsJsonArray("data");
		quests = new Quest[0];
		
		for(int i=0; i<raw.size(); i++) {
			try {
				quests = add(quests, new Quest(raw.get(i).getAsJsonObject()));
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
	
	private Quest[] add(Quest[] arr, Quest item) {
		Quest[] arr2 = new Quest[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	private String[] add(String[] arr, String item) {
		String[] arr2 = new String[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
}
