package srlib;

import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Json;
import otherlib.Logger;
import otherlib.Options;
import srlib.viewer.Raid;

public class Map {

	public final int width;
	public final int length;
	public final int mapPower;
	
	private int playerPower = 0;
	
	public int getPlayerPower() {
		return playerPower;
	}
	
	private final JsonArray map;
	
	public final String name;
	private final String cid;
	private final int slot;
	
	public JsonObject get(int x, int y) {
		if(isOutOfRange(x, y)) return null;
		return map.get(x).getAsJsonArray().get(y).getAsJsonObject();
	}
	
	private void set(int x, int y, String key, String value) {
		if(isOutOfRange(x, y)) return;
		map.get(x).getAsJsonArray().get(y).getAsJsonObject().addProperty(key, value);
	}
	
	private void set(int x, int y, String key, boolean value) {
		if(isOutOfRange(x, y)) return;
		map.get(x).getAsJsonArray().get(y).getAsJsonObject().addProperty(key, value);
	}
	
	private int hw() {
		return (int) Math.floor(width/2);
	}
	
	private int hl() {
		return (int) Math.floor(length/2);
	}
	
	public Map(JsonObject mapData, Raid raid, JsonObject plan, String name, List<String> userIds, String cid, int slot) {
		if(mapData == null)
			new run.StreamRaidersException("Map -> const: err=mapData is null, mapName="+name, cid, slot);
		this.cid = cid;
		this.slot = slot;
		this.name = name;
		float mapScale = mapData.get("MapScale").getAsFloat();
		if(mapScale < 0) {
			width = (int) (mapData.get("GridWidth").getAsInt());
			length = (int) (mapData.get("GridLength").getAsInt());
		} else {
			width = (int) Math.round(41 * mapScale);
			length = (int) Math.round(29 * mapScale);
		}
		
		this.mapPower = mapData.get("MapPower").getAsInt();
		map = new JsonArray();
		
		for(int i=0; i<width; i++) {
			map.add(new JsonArray());
			for(int j=0; j<length; j++) {
				JsonObject n = new JsonObject();
				n.addProperty(SRC.Map.isEmpty, true);
				map.get(i).getAsJsonArray().add(n);
			}
		}
		
		updateMap(mapData, Json.parseArr(raid.placementsSerialized), Json.parseArr(raid.users), plan, userIds);
	}
	
	public double[] getAsSRCoords(boolean epic, int[] coords) {
		double x = ((double) coords[0] - hw()) * 0.8;
		double y = ((double) coords[1] - hl()) * -0.8;
		
		if(epic) {
			x -= 0.4;
			y -= 0.4;
		}
		
		return new double[] {x, y};
	}
	
	private void updateMap(JsonObject mapData, JsonArray placements, JsonArray users, JsonObject plan, List<String> userIds) {
		addRects(mapData.getAsJsonArray("PlayerPlacementRects"), SRC.Map.isPlayerRect);
		addRects(mapData.getAsJsonArray("EnemyPlacementRects"), SRC.Map.isEnemyRect);
		addRects(mapData.getAsJsonArray("HoldingZoneRects"), SRC.Map.isHoldRect);
		
		JsonObject unitsData = Json.parseObj(Options.get("unitPower"));
		
		addEntities(placements, users, userIds, unitsData);
		addEntities(mapData.getAsJsonArray("PlacementData"), null, userIds, null);
		addObstacles(mapData.getAsJsonArray("ObstaclePlacementData"));
		
		if(plan != null) {
			addPlan(plan);
			genUsablePlanTypesSets();
		}
		
		
	}
	
	private final static JsonArray seenPlanTypes = new JsonArray();
	
	synchronized private static void addPT(String pt) {
		if(!seenPlanTypes.contains(new JsonPrimitive(pt)))
			seenPlanTypes.add(pt);
	}
	
	public static JsonArray getSeenPlanTypes() {
		return seenPlanTypes.deepCopy();
	}
	
	
	private void addPlan(JsonObject plan) {
		for(String key : plan.keySet()) {
			JsonArray pd = plan.getAsJsonArray(key);
			key = key.toLowerCase();
			addPT(key);
			for(int i=0; i<pd.size(); i+=2)
				set(pd.get(i).getAsInt(), pd.get(i+1).getAsInt(), "plan", key);
			
		}
	}

	private void addObstacles(JsonArray places) {
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			int x = (int) Math.round(place.getAsJsonPrimitive("X").getAsDouble() / 0.8 + hw());
			int y = (int) Math.round((place.getAsJsonPrimitive("Y").getAsDouble() / 0.8 - hl()) * -1);
			
			String name = place.getAsJsonPrimitive("ObstacleName").getAsString();
			
			JsonObject obst = Json.parseObj(Options.get("obstacles")).getAsJsonObject(name);
			
			boolean canWalkOver = obst.getAsJsonPrimitive("CanWalkOver").getAsBoolean();
			
			set(x, y, SRC.Map.isObstacle, true);
			set(x, y, "name", name);
			set(x, y, SRC.Map.canFlyOver, obst.getAsJsonPrimitive("CanFlyOver").getAsBoolean());
			set(x, y, SRC.Map.canWalkOver, canWalkOver);
			set(x, y, "ContactBuff", obst.getAsJsonPrimitive("ContactBuff").getAsBoolean());
			set(x, y, SRC.Map.isEmpty, is(x, y, SRC.Map.isEmpty) && canWalkOver);
		}
	}

	private void addEntities(JsonArray places, JsonArray users, List<String> uids, JsonObject unitsData) {
		if(places == null) 
			return;
		
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			double rx = place.get("X").getAsDouble();
			double ry = place.get("Y").getAsDouble();
			
			try {
				String chaType = place.get("CharacterType").getAsString();
				if(chaType.contains("epic") || place.getAsJsonPrimitive("CharacterType").getAsString().contains("captain")) {
					rx += 0.4;
					ry += 0.4;
				}
			} catch (Exception e) {}
			
			int x = (int) Math.round(rx / 0.8 + hw());
			int y = (int) Math.round(ry / -0.8 + hl());
			
			//	TODO optimize by getting the Jsonobject for x y and adding directly instead of using set(x, y, key, val)
			
			JsonElement jteam = place.get("team");
			
			String team = "Enemy";
			
			if(jteam != null && jteam.isJsonPrimitive())
				team = jteam.getAsString();
			
			switch(team) {
			case "Ally":
				String chaType = place.get("CharacterType").getAsString();
				try {
					if(unitsData != null)
						playerPower += unitsData.get(chaType).getAsInt();
				} catch(Exception e) {
					Logger.printException("Map -> addEntities: err=unknown unit, chaType="+chaType, e, Logger.runerr, Logger.error, cid, slot, true);
				}
				
				
				if(chaType.contains("epic") || place.get("CharacterType").getAsString().contains("captain")) {
					set(x-1, y+1, SRC.Map.isOccupied, true);
					set(x-1, y, SRC.Map.isOccupied, true);
					set(x, y+1, SRC.Map.isOccupied, true);
					set(x-1, y+1, SRC.Map.isEmpty, false);
					set(x-1, y, SRC.Map.isEmpty, false);
					set(x, y+1, SRC.Map.isEmpty, false);
					set(x, y, SRC.Map.isEpic, true);
				}
				set(x, y, SRC.Map.isAllied, true);
				set(x, y, "spec", place.get("specializationUid").getAsString());
				if(chaType.contains("captain")) {
					set(x, y, SRC.Map.isCaptain, true);
					set(x-1, y, SRC.Map.isCaptain, true);
					set(x, y+1, SRC.Map.isCaptain, true);
					set(x-1, y+1, SRC.Map.isCaptain, true);
				}

				String userId = place.get("userId").getAsString();
				if(!userId.equals("") && users != null) {
					set(x, y, "userId", userId);
					int index = uids.indexOf(userId);
					if(index == 0)
						set(x, y, SRC.Map.isSelf, true);
					else if(index > 0)
						set(x, y, SRC.Map.isOther, true);
					for(int u=0; u<users.size(); u++) {
						JsonObject user = users.get(u).getAsJsonObject();
						if(userId.equals(user.get("userId").getAsString()))
							set(x, y, "twitchDisplayName", user.get("twitchDisplayName").getAsString());
					}
					String type = Unit.getUnitTypeFromCharacterType(chaType);
					if(type != null)
						set(x, y, "unitType", type);
				}
				break;
			case "Enemy":
				set(x, y, SRC.Map.isAllied, false);
				break;
			case "Neutral":
				set(x, y, SRC.Map.isNeutral, true);
				break;
			default:
				Logger.print("Map -> addEntity: err=failed to determine team, team=" + place.get("team").getAsString(), Logger.runerr, Logger.error, cid, slot, true);
			}
			set(x, y, "isEntity", true);
			set(x, y, "isEmpty", false);
			set(x, y, "type", place.getAsJsonPrimitive("CharacterType").getAsString().replaceAll("[0-9]", ""));
			set(x, y, "lvl", place.getAsJsonPrimitive("CharacterType").getAsString().replaceAll("[a-zA-Z]", ""));
			
		}
	}

	private void addRects(JsonArray rects, String prop) {
		if(rects == null) return;
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
	}
	
	private boolean isOutOfRange(int x, int y) {
		return x < 0 
			|| y < 0 
			|| x >= width 
			|| y >= length;
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
			break;
		case SRC.Map.isNeutral:
			break;
		case SRC.Map.isEmpty:
			break;
		case SRC.Map.isEnemy:
			if(!place.has(SRC.Map.isAllied)) return false;
			return !place.get(SRC.Map.isAllied).getAsBoolean();
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
			return !place.get("userId").getAsString().equals("");
		case SRC.Map.isCaptain:
			break;
		case SRC.Map.isSelf:
			break;
		case SRC.Map.isOther:
			break;
		default:
			return false;
		}
		return (!place.has(con)) 
				? false 
				: place.get(con).getAsBoolean();
	}
	
	public String getPlanType(int x, int y) {
		JsonObject obj = get(x, y);
		if(obj == null)
			return null;
		JsonElement je = obj.get("plan");
		return je == null || !je.isJsonPrimitive()
				? null
				: je.getAsString();
	}
	
	
	private static final HashSet<String> nupts = new HashSet<>();
	private static final HashSet<String> eupts = new HashSet<>();
	
	private void genUsablePlanTypesSets() {
		String[][] mpts = new String[width][length];
		for(int x=0; x<width; x++) 
			for(int y=0; y<length; y++) 
				if(is(x, y, SRC.Map.isEmpty)) {
					JsonElement je = get(x, y).get("plan");
					if(je == null || !je.isJsonPrimitive())
						continue;
					String pt = je.getAsString();
					if(pt.equals("noPlacement"))
						continue;
					mpts[x][y] = pt;
					nupts.add(mpts[x][y]);
				}
					
			
		for(int x=0; x<mpts.length-1; x++) {
			for(int y=0; y<mpts[x].length-1; y++) {
				String pt = mpts[x][y];
				if(pt == null)
					continue;
				if(pt.equals(mpts[x+1][y])
					&& pt.equals(mpts[x][y+1])
					&& pt.equals(mpts[x+1][y+1]))
					eupts.add(pt);
			}
		}
	}
	
	public HashSet<String> getUsablePlanTypes(boolean epic) {
		return new HashSet<>(epic ? eupts : nupts);
	}
	
	public String getDisplayName(int x, int y) {
		JsonElement n = get(x, y).get("twitchDisplayName");
		return n == null || !n.isJsonPrimitive() 
				? null 
				: n.getAsString();
	}
}
