package program;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.GUI.TextArea;
import include.JsonParser;

public class Map {

	private int width = 0;
	private int length = 0;
	
	public int width() {
		return width;
	}
	
	public int length() {
		return length;
	}
	
	private int hw() {
		return (int) Math.floor(width/2);
	}
	
	private int hl() {
		return (int) Math.floor(length/2);
	}
	
	private JsonArray map = null;
	
	public JsonArray getMap() {
		return map;
	}
	
	public JsonObject get(int x, int y) {
		if(x >= width || y >= length || x < 0 || y < 0) return null;
		return map.get(x).getAsJsonArray().get(y).getAsJsonObject();
	}
	
	private void set(int x, int y, String key, String value) {
		if(x >= width || y >= length || x < 0 || y < 0) return;
		map.get(x).getAsJsonArray().get(y).getAsJsonObject().addProperty(key, value);
	}
	
	private void set(int x, int y, String key, boolean value) {
		if(x >= width || y >= length || x < 0 || y < 0) return;
		map.get(x).getAsJsonArray().get(y).getAsJsonObject().addProperty(key, value);
	}
	
	public Map(JsonArray completeMap) {
		map = completeMap;
		width = map.size();
		length = map.get(0).getAsJsonArray().size();
	}
	
	private String name = "";
	
	public String getName() {
		return name;
	}
	
	public Map(JsonObject mapData, JsonArray placements, JsonArray users, JsonObject plan, String name, String userId) {
		this.name = name;
		float mapScale = mapData.getAsJsonPrimitive("MapScale").getAsFloat();
		if(mapScale < 0) {
			width = (int) (mapData.getAsJsonPrimitive("GridWidth").getAsInt());
			length = (int) (mapData.getAsJsonPrimitive("GridLength").getAsInt());
		} else {
			width = (int) Math.round(41 * mapScale);
			length = (int) Math.round(29 * mapScale);
		}
		
		map = new JsonArray();
		
		for(int i=0; i<width; i++) {
			map.add(new JsonArray());
			for(int j=0; j<length; j++) {
				JsonObject n = new JsonObject();
				n.addProperty(SRC.Map.isEmpty, true);
				map.get(i).getAsJsonArray().add(n);
			}
		}
		
		updateMap(mapData, placements, users, plan, userId);
	}
	
	public boolean testPos(boolean epic, int[] coords) {
		if(!testField(coords[0], coords[1])) return false;
		if(epic) {
			if(!testField(coords[0]-1, coords[1])) return false;
			if(!testField(coords[0]-1, coords[1]+1)) return false;
			if(!testField(coords[0], coords[1]+1)) return false;
		}
		return true;
	}
	
	private boolean testField(int x, int y)  {
		JsonObject field = map.get(x).getAsJsonArray().get(y).getAsJsonObject();
		if(!field.has(SRC.Map.isEmpty)) return false;
		if(!field.get(SRC.Map.isEmpty).getAsBoolean()) return false;
		if(!field.has(SRC.Map.isPlayerRect)) return false;
		if(!field.get(SRC.Map.isPlayerRect).getAsBoolean()) return false;
		
		return true;
	}
	

	public double[] getAsSRCoords(boolean epic, int[] coords) {
		double x = ((double) coords[0] - hw()) * 0.8;
		double y = ((double) coords[1] - hl()) * -0.8;
		
		if(epic) {
			x -= 0.4;
			y += 0.4;
		}
		
		return new double[] {x, y};
	}
	
	public void updateMap(JsonObject mapData, JsonArray placements, JsonArray users, JsonObject plan, String userId) {
		map = addRects(map, mapData.getAsJsonArray("PlayerPlacementRects"), SRC.Map.isPlayerRect);
		map = addRects(map, mapData.getAsJsonArray("EnemyPlacementRects"), SRC.Map.isEnemyRect);
		map = addRects(map, mapData.getAsJsonArray("HoldingZoneRects"), SRC.Map.isHoldRect);
		
		map = addEntity(map, placements, users, userId);
		map = addEntity(map, mapData.getAsJsonArray("PlacementData"), null, userId);
		map = addObstacle(map, mapData.getAsJsonArray("ObstaclePlacementData"));
		
		if(plan == null) return;
		map = addPlan(map, plan);
	}
	
	private static JsonArray planTypes = new JsonArray();
	
	synchronized private static void addPT(String pt) {
		if(!planTypes.contains(new JsonPrimitive(pt)))
			planTypes.add(pt);
	}
	
	public static void showPlanTypes(GUI parent) {
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<planTypes.size(); i++) {
			sb.append(planTypes.get(i).getAsString() + "\n");
		}
		
		if(sb.length() == 0) {
			return;
		}
		
		GUI gui = new GUI("Plan Types", 400, 500, parent, null);
		
		
		TextArea ta = new TextArea();
		ta.setEditable(false);
		ta.setText(sb.substring(0, sb.length()-1));
		gui.addTextArea(ta);
		
		gui.refresh();
		
	}
	
	
	private JsonArray addPlan(JsonArray map, JsonObject plan) {
		for(String key : plan.keySet()) {
			addPT(key);
			JsonArray pd = plan.getAsJsonArray(key);
			for(int i=0; i<pd.size(); i+=2) {
				set(pd.get(i).getAsInt(), pd.get(i+1).getAsInt(), "plan", key);
			}
		}
		return map;
	}

	private JsonArray addObstacle(JsonArray map, JsonArray places) {
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			int x = (int) Math.round(place.getAsJsonPrimitive("X").getAsDouble() / 0.8 + hw());
			int y = (int) Math.round((place.getAsJsonPrimitive("Y").getAsDouble() / 0.8 - hl()) * -1);
			
			String name = place.getAsJsonPrimitive("ObstacleName").getAsString();
			
			JsonObject obst = JsonParser.parseObj(StreamRaiders.get("obstacles")).getAsJsonObject(name);
			
			set(x, y, SRC.Map.isObstacle, true);
			set(x, y, "name", name);
			set(x, y, SRC.Map.canFlyOver, obst.getAsJsonPrimitive("CanFlyOver").getAsBoolean());
			set(x, y, SRC.Map.canWalkOver, obst.getAsJsonPrimitive("CanWalkOver").getAsBoolean());
			set(x, y, "ContactBuff", obst.getAsJsonPrimitive("ContactBuff").getAsBoolean());
			set(x, y, SRC.Map.isEmpty, false);
			}
		return map;
	}

	private JsonArray addEntity(JsonArray map, JsonArray places, JsonArray users, String uid) {
		if(places == null) return map;
		
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			double rx = place.getAsJsonPrimitive("X").getAsDouble();
			double ry = place.getAsJsonPrimitive("Y").getAsDouble();
			
			try {
				if(place.getAsJsonPrimitive("CharacterType").getAsString().contains("epic") || place.getAsJsonPrimitive("CharacterType").getAsString().contains("captain")) {
					rx += 0.4;
					ry += 0.4;
				}
			} catch (Exception e) {}
			
			
			int x = (int) Math.round(rx / 0.8 + hw());
			int y = (int) Math.round((ry / 0.8 - hl()) * -1);
			
			JsonElement jteam = place.get("team");
			
			String team = "Enemy";
			
			if(jteam != null && jteam.isJsonPrimitive()) team = jteam.getAsString();
			
			switch(team) {
			case "Ally":
				if(place.getAsJsonPrimitive("CharacterType").getAsString().contains("epic") || place.getAsJsonPrimitive("CharacterType").getAsString().contains("captain")) {
					set(x-1, y+1, SRC.Map.isOccupied, true);
					set(x-1, y, SRC.Map.isOccupied, true);
					set(x, y+1, SRC.Map.isOccupied, true);
					set(x-1, y+1, SRC.Map.isEmpty, false);
					set(x-1, y, SRC.Map.isEmpty, false);
					set(x, y+1, SRC.Map.isEmpty, false);
					set(x, y, SRC.Map.isEpic, true);
				}
				set(x, y, SRC.Map.isAllied, true);
				set(x, y, "spec", place.getAsJsonPrimitive("specializationUid").getAsString());
				String userId = place.getAsJsonPrimitive("userId").getAsString();
				if(place.getAsJsonPrimitive("CharacterType").getAsString().contains("captain")) {
					set(x, y, SRC.Map.isCaptain, true);
					set(x-1, y, SRC.Map.isCaptain, true);
					set(x, y+1, SRC.Map.isCaptain, true);
					set(x-1, y+1, SRC.Map.isCaptain, true);
				}
				
				if(!userId.equals("") && users != null) {
					set(x, y, "userId", userId);
					if(userId.equals(uid))
						set(x, y, SRC.Map.isSelf, true);
					for(int u=0; u<users.size(); u++) {
						if(userId.equals(users.get(u).getAsJsonObject().getAsJsonPrimitive("userId").getAsString())) {
							set(x, y, "twitchUserName", users.get(u).getAsJsonObject().getAsJsonPrimitive("twitchUserName").getAsString());
						}
					}
				}
				break;
			case "Enemy":
				set(x, y, SRC.Map.isAllied, false);
				break;
			default:
				StreamRaiders.log("Map -> addEntity: team=" + place.getAsJsonPrimitive("team").getAsString(), null);
			}
			set(x, y, "isEntity", true);
			set(x, y, "isEmpty", false);
			set(x, y, "type", place.getAsJsonPrimitive("CharacterType").getAsString().replaceAll("[0-9]", ""));
			set(x, y, "lvl", place.getAsJsonPrimitive("CharacterType").getAsString().replaceAll("[a-zA-Z]", ""));
			
		}
		return map;
	}

	private JsonArray addRects(JsonArray map, JsonArray rects, String prop) {
		if(rects == null) return map;
		for(int i=0; i<rects.size(); i++) {
			JsonObject rect = rects.get(i).getAsJsonObject();
			
			int x = (int) Math.round(rect.getAsJsonPrimitive("x").getAsDouble() / 0.8 + hw() + 0.5);
			int y = (int) Math.round((rect.getAsJsonPrimitive("y").getAsDouble() / 0.8 - hl()) * -1 - 0.5);
			int w = (int) Math.round(rect.getAsJsonPrimitive("width").getAsDouble() / 0.8 - 1);
			int h = (int) Math.round(rect.getAsJsonPrimitive("height").getAsDouble() / 0.8 - 1);
			
			for(int j=x; j<=x+w; j++) {
				for(int k=y-h; k<=y; k++) {
					set(j, k, prop, true);
				}
			}
		}
		return map;
	}
	
	private boolean isOutOfRange(int x, int y) {
		if(x < 0 || y < 0) return true;
		if(x >= width || y >= length) return true;
		return false;
	}
	
	public boolean is(int x, int y, String con) {
		if(isOutOfRange(x, y)) return false;
		JsonObject place = get(x, y);
		switch (con) {
		case SRC.Map.canFlyOver:
			if(!is(x, y, SRC.Map.isObstacle)) return false;
			break;
		case SRC.Map.canWalkOver:
			if(!is(x, y, SRC.Map.isObstacle)) return false;
			break;
		case SRC.Map.isAllied:
			if(!place.has("isEntity")) return false;
			if(!place.getAsJsonPrimitive("isEntity").getAsBoolean()) return false;
			break;
		case SRC.Map.isEmpty:
			break;
		case SRC.Map.isEnemy:
			if(!place.has("isEntity")) return false;
			if(!place.getAsJsonPrimitive("isEntity").getAsBoolean()) return false;
			return !place.getAsJsonPrimitive(SRC.Map.isAllied).getAsBoolean();
		case SRC.Map.isEnemyRect:
			break;
		case SRC.Map.isHoldRect:
			break;
		case SRC.Map.isObstacle:
			break;
		case SRC.Map.isPlayerRect:
			break;
		case SRC.Map.isOccupied:
			break;
		case SRC.Map.isEpic:
			break;
		case SRC.Map.isPlayer:
			if(!place.has("userId")) return false;
			return !place.getAsJsonPrimitive("userId").getAsString().equals("");
		case SRC.Map.isCaptain:
			if(!place.has(SRC.Map.isCaptain)) return false;
			break;
		case SRC.Map.isSelf:
			break;
		default:
			return false;
		}
		if(!place.has(con)) return false;
		return place.getAsJsonPrimitive(con).getAsBoolean();
	}
	
	public String getPlanType(int x, int y) {
		JsonElement je = get(x, y).get("plan");
		if(je != null && je.isJsonPrimitive()) {
			return je.getAsString();
		} else {
			return null;
		}
	}
	
	public JsonArray getPresentPlanTypes() {
		JsonArray ret = new JsonArray();
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				JsonElement je = get(x, y).get("plan");
				if(je != null && je.isJsonPrimitive())
					ret.add(je.getAsString());
				
			}
		}
		return ret;
	}
	
	public String getUserName(int x, int y) {
		JsonObject f = get(x, y);
		if(f.has("twitchUserName"))
			return f.getAsJsonPrimitive("twitchUserName").getAsString();
		return null;
	}
}
