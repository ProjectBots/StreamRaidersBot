package program;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;

public class Quests {
	
	public static class NoQuestException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public static class Quest {
		
		private final JsonObject quest;
		public final int progress;
		public final String slot;
		
		public Quest(JsonObject rQuest) throws NoQuestException {
			JsonElement id = rQuest.get("currentQuestId");
			if(!id.isJsonPrimitive())
				throw new NoQuestException();
			
			quest = questTypes.getAsJsonObject(id.getAsString());
			progress = rQuest.get("currentProgress").getAsInt();
			slot = rQuest.get("questSlotId").getAsString();
		}
		
		public boolean canClaim() {
			return progress >= quest.get("GoalAmount").getAsInt();
		}
		
		public String neededUnit() {
			if(quest.get("Type").getAsString().equals("PlaceUnitOfType")) 
				return quest.get("Objective").getAsString();
			return null;
		}
	}
	
	private static JsonObject questTypes = null;
	
	private ArrayList<Quest> quests = null;
	
	public ArrayList<String> getNeededUnitTypesForQuests() {
		ArrayList<String> ret = new ArrayList<>();
		for(Quest q : quests) {
			String type = q.neededUnit();
			if(type != null) 
				ret.add(type);
		}
		return ret;
	}
	
	
	public void updateQuests(JsonArray userQuests) {
		questTypes = Json.parseObj(Options.get("quests"));
		
		quests = new ArrayList<Quest>();
		
		for(int i=0; i<userQuests.size(); i++) {
			try {
				quests.add(new Quest(userQuests.get(i).getAsJsonObject()));
			} catch(NoQuestException e) {}
		}
	}
	
	public ArrayList<Quest> getClaimableQuests() {
		ArrayList<Quest> ret = new ArrayList<Quest>();
		for(Quest q : quests)
			if(q.canClaim()) 
				ret.add(q);
		return ret;
	}
}
