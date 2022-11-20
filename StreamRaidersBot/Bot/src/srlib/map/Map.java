package srlib.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import otherlib.Logger;
import otherlib.Options;
import srlib.units.UnitType;
import srlib.viewer.Raid;

public class Map {

	private final static HashSet<String> seenPlanTypes = new HashSet<>();
	
	private static void addPT(String pt) {
		if(!seenPlanTypes.contains(pt))
			seenPlanTypes.add(pt);
	}
	
	public static ArrayList<String> getSeenPlanTypes() {
		return new ArrayList<String>(seenPlanTypes);
	}
	
	public final int width;
	public final int length;
	public final int mapPower;
	
	private int playerPower = 0;
	
	public int getPlayerPower() {
		return playerPower;
	}
	
	private final Place[][] map;
	
	public final String name;
	public final int raidId;
	private final String cid;
	private final int slot;
	
	private int lastIndex;
	
	public int getLastIndex() {
		return lastIndex;
	}
	
	
	private Place getCreate(int x, int y) {
		if(isOutOfRange(x, y))
			return null;
		if(map[x][y] == null)
			map[x][y] = new Place();
		return map[x][y];
	}
	
	public Place get(int x, int y) {
		if(isOutOfRange(x, y))
			return null;
		return map[x][y];
	}
	
	
	private int hw() {
		return (int) Math.floor(width/2);
	}
	
	private int hl() {
		return (int) Math.floor(length/2);
	}
	
	public Map(JsonObject mapData, Raid raid, String name, List<String> userIds, String cid, int slot) {
		if(mapData == null)
			new run.StreamRaidersException("Map -> const: err=mapData is null, mapName="+name, cid, slot);
		
		this.cid = cid;
		this.slot = slot;
		this.name = name;
		this.raidId = raid.raidId;
		float mapScale = mapData.get("MapScale").getAsFloat();
		if(mapScale < 0) {
			width = (int) (mapData.get("GridWidth").getAsInt());
			length = (int) (mapData.get("GridLength").getAsInt());
		} else {
			//	found by trial and error
			width = (int) Math.round(41 * mapScale);
			length = (int) Math.round(29 * mapScale);
		}
		
		this.mapPower = mapData.get("MapPower").getAsInt();
		
		map = new Place[width][length];
		
		updateMap(mapData, raid, userIds);
		
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
	
	public void updateMap(JsonObject mapData, Raid raid, List<String> userIds) {
		updateMap(mapData, Json.parseArr(raid.placementsSerialized), Json.parseArr(raid.users), userIds);
	}
	
	
	private void updateMap(JsonObject mapData, JsonArray placements, JsonArray users, List<String> userIds) {
		if(mapData != null) {
			for(PlacementRectType prt : PlacementRectType.values())
				addRects(mapData.getAsJsonArray(prt.name), prt);
			
			addEntities(mapData.getAsJsonArray("PlacementData"), null, userIds);
			addObstacles(mapData.getAsJsonArray("ObstaclePlacementData"));
		}
		
		//	TODO rem unitPower in opt.txt
		//JsonObject unitsData = Json.parseObj(Options.get("unitPower"));
		
		addEntities(placements, users, userIds);
		
	}
	
	public void updateRaidPlan(JsonObject plan) {
		addPlan(plan);
		genUsablePlanTypesSets();
	}
	
	private void addPlan(JsonObject plan) {
		for(String key : plan.keySet()) {
			JsonArray pd = plan.getAsJsonArray(key);
			key = key.toLowerCase();
			addPT(key);
			for(int i=0; i<pd.size(); i+=2)
				getCreate(pd.get(i).getAsInt(), pd.get(i+1).getAsInt()).plan = key;
		}
	}

	private void addObstacles(JsonArray places) {
		JsonObject obstData = Json.parseObj(Options.get("obstacles"));
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			int x = (int) Math.round(place.get("X").getAsDouble() / 0.8 + hw());
			int y = (int) Math.round((place.get("Y").getAsDouble() / 0.8 - hl()) * -1);
			
			Place p = getCreate(x, y);
			if(p == null)
				continue;
			
			JsonObject obst = obstData.getAsJsonObject(place.get("ObstacleName").getAsString());
			
			p.isObstacle = true;
			p.canWalkOver = obst.get("CanWalkOver").getAsBoolean();
			p.canFlyOver = obst.get("CanFlyOver").getAsBoolean();
		}
	}

	private void addEntities(JsonArray places, JsonArray users, List<String> uids) {
		if(places == null) 
			return;
		
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			double rx = place.get("X").getAsDouble();
			double ry = place.get("Y").getAsDouble();
			
			JsonElement chatype = place.get("CharacterType");
			if(chatype != null && chatype.isJsonPrimitive()) {
				String chaType = place.get("CharacterType").getAsString();
				if(chaType.contains("epic") || chaType.contains("captain")) {
					rx += 0.4;
					ry += 0.4;
				}
			}
			
			int x = (int) Math.round(rx / 0.8 + hw());
			int y = (int) Math.round(ry / -0.8 + hl());
			
			Place p = getCreate(x, y);
			if(p == null)
				continue;
			
			p.isEntity = true;
			p.userId = null;
			
			JsonElement jteam = place.get("team");
			
			p.team = Team.ENEMY;
			
			if(jteam != null && jteam.isJsonPrimitive())
				p.team = Team.parseString(jteam.getAsString());
			
			switch(p.team) {
			case ALLY:
				String chaType = place.get("CharacterType").getAsString();
				
				if(chaType.contains("epic") || chaType.contains("captain")) {
					Place p1 = getCreate(x-1, y+1);
					p1.isOccupied = true;
					Place p2 = getCreate(x-1, y+1);
					p2.isOccupied = true;
					Place p3 = getCreate(x, y+1);
					p3.isOccupied = true;
					if(chaType.contains("captain")) {
						p.isCaptain = true;
						p1.isCaptain = true;
						p2.isCaptain = true;
						p3.isCaptain = true;
					}
					p.isEpic = true;
				} else {
					p.isEpic = false;
					p.isCaptain = false;
				}
				
				if(users == null) {
					p.userId = null;
				} else {
					p.userId = place.get("userId").getAsString();
					if(p.userId.equals("")) {
						Logger.print("Map -> addEntities: err=users not null, but userId is empty", Logger.runerr, Logger.warn, cid, slot, true);
						break;
					}
					//	it has a user id, therefore it is a player
					p.team = Team.PLAYER;
					
					int placeId = place.get("raidPlacementsId").getAsInt();
					if(placeId > lastIndex)
						lastIndex = placeId;
					
					int index = uids.indexOf(p.userId);
					if(index == 0) {
						p.isSelf = true;
						p.isOther = false;
					} else if(index > 0) {
						p.isSelf = false;
						p.isOther = true;
					} else {
						p.isSelf = false;
						p.isOther = false;
					}
					
					p.twitchDisplayName = null;
					for(int u=0; u<users.size(); u++) {
						JsonObject user = users.get(u).getAsJsonObject();
						if(p.userId.equals(user.get("userId").getAsString())) {
							p.twitchDisplayName = user.get("twitchDisplayName").getAsString();
							break;
						}
					}
					
					String type = UnitType.getUnitTypeFromCharacterType(chaType);
					p.type = type != null ? UnitType.getType(type) : null;
				}
				break;
			case ENEMY:
				break;
			case NEUTRAL:
				break;
			case PLAYER:
				//	impossible case
				break;
			}
		}
	}

	private void addRects(JsonArray rects, PlacementRectType prop) {
		if(rects == null)
			return;
		for(int i=0; i<rects.size(); i++) {
			JsonObject rect = rects.get(i).getAsJsonObject();
			
			int x = (int) Math.round(rect.getAsJsonPrimitive("x").getAsDouble() / 0.8 + hw() + 0.5);
			int y = (int) Math.round((rect.getAsJsonPrimitive("y").getAsDouble() / 0.8 - hl()) * -1 - 0.5);
			int w = (int) Math.round(rect.getAsJsonPrimitive("width").getAsDouble() / 0.8 - 1);
			int h = (int) Math.round(rect.getAsJsonPrimitive("height").getAsDouble() / 0.8 - 1);
			
			for(int j=x; j<=x+w; j++)
				for(int k=y-h; k<=y; k++)
					getCreate(j, k).rect = prop;
		}
	}
	
	private boolean isOutOfRange(int x, int y) {
		return x < 0 
			|| y < 0 
			|| x >= width 
			|| y >= length;
	}
	
	
	private static final HashSet<String> nupts = new HashSet<>();
	private static final HashSet<String> eupts = new HashSet<>();
	
	private void genUsablePlanTypesSets() {
		nupts.clear();
		eupts.clear();
		String[][] mpts = new String[width][length];
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				Place p = get(x, y);
				if(p != null && p.isEmpty()) {
					if(p.plan.equals("noPlacement"))
						continue;
					mpts[x][y] = p.plan;
					nupts.add(p.plan);
				}
			}
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
	
	public int countUnitsFrom(String userId) {
		int ret = 0;
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				Place p = get(x, y);
				if(p != null && p.isEntity && p.userId.equals(userId))
					ret++;
			}
		}
		return ret;
	}
}
