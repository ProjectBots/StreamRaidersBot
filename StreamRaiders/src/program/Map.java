package program;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.JsonParser;

public class Map {

	private int width = 0;
	private int length = 0;
	
	private int hw() {
		return (int) Math.floor(width/2);
	}
	
	private int hl() {
		return (int) Math.floor(length/2);
	}
	
	JsonObject[][] map = null;
	
	public JsonObject[][] getMap() {
		return map;
	}
	
	public Map(JsonObject mapData, JsonArray placements) {
		float mapScale = mapData.getAsJsonPrimitive("MapScale").getAsFloat();
		if(mapScale < 0) {
			width = (int) (mapData.getAsJsonPrimitive("GridWidth").getAsInt()/0.8);
			length = (int) (mapData.getAsJsonPrimitive("GridLength").getAsInt()/0.8);
		} else {
			width = Math.round(50 * mapScale);
			length = Math.round(40 * mapScale);
			//System.err.println("> MapScale: " + mapScale + " w: " + width + " l: " + length);
		}
		
		map = new JsonObject[width][length];
		for(int i=0; i<width; i++) {
			for(int j=0; j<length; j++) {
				map[i][j] = new JsonObject();
				map[i][j].addProperty("playerRect", false);
				map[i][j].addProperty("enemyRect", false);
				map[i][j].addProperty("holdRect", false);
				map[i][j].addProperty("occupied", false);
				map[i][j].addProperty("unit", 0);	//	0=nothing, 1=player/ally, 2=enemy, 3=obstacle
				map[i][j].add("data", new JsonObject());
			}
		}
		
		updateMap(mapData, placements);
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
		if(!map[x][y].getAsJsonPrimitive("playerRect").getAsBoolean()) return false;
		if(map[x][y].getAsJsonPrimitive("unit").getAsInt() != 0) return false;
		if(map[x][y].getAsJsonPrimitive("occupied").getAsBoolean()) return false;
		return true;
	}
	

	public double[] getAsSRCoords(int[] coords) {
		double x = ((double) coords[0] - hw()) * 0.8;
		double y = ((double) coords[1] - 1 - hl()) * -0.8;
		
		return new double[] {x, y};
	}
	
	
	private JsonArray playerRects = new JsonArray();
	
	public JsonArray getPlayerRects() {
		return playerRects;
	}
	
	public void updateMap(JsonObject mapData, JsonArray placements) {
		map = addRects(map, mapData.getAsJsonArray("PlayerPlacementRects"), "playerRect");
		map = addRects(map, mapData.getAsJsonArray("EnemyPlacementRects"), "enemyRect");
		map = addRects(map, mapData.getAsJsonArray("HoldingZoneRects"), "holdRect");
		
		map = addEntity(map, placements);
		map = addEntity(map, mapData.getAsJsonArray("PlacementData"));
		map = addObstacle(map, mapData.getAsJsonArray("ObstaclePlacementData"));
		
	}
	
	private String[] obWhite = new String[] {
			"wall",
			"water",
			"canal",
			"tree",
			"mountains",
			"canyons"
	};
	
	private static JsonArray obsts = JsonParser.jsonArr(NEF.read("data/obstacles.app"));
	
	public static void whiteObst(String name) {
		if(!obsts.contains(new JsonPrimitive(name))) {
			obsts.add(name);
			NEF.save("data/obstacles.app", JsonParser.prettyJson(obsts));
			System.out.println("+added " + name + " to obstacle list");
		}
	}

	private JsonObject[][] addObstacle(JsonObject[][] map, JsonArray places) {
		for(int i=0; i<places.size(); i++) {
			JsonObject place = places.get(i).getAsJsonObject();
			
			
			if(!obsts.contains(place.getAsJsonPrimitive("ObstacleName"))) {
				String obst = place.getAsJsonPrimitive("ObstacleName").getAsString();
				
				bif:
				if(obst.contains("terrain")) {
					for(int j=0; j<obWhite.length; j++) {
						if(obst.contains(obWhite[j])) {
							break bif;
						}
					}
					continue;
				}
				
				whiteObst(obst);
			}
			
			
			int x = (int) Math.round(place.getAsJsonPrimitive("X").getAsDouble() / 0.8 + hw());
			int y = (int) Math.round((place.getAsJsonPrimitive("Y").getAsDouble() / 0.8 - hl()) * -1 + 1);
			
			
			map[x][y].addProperty("unit", 3);
			map[x][y].add("data", place);
		}
		return map;
	}

	private JsonObject[][] addEntity(JsonObject[][] map, JsonArray places) {
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
			int y = (int) Math.round((ry / 0.8 - hl()) * -1 + 1);
			
			try {
				switch(place.getAsJsonPrimitive("team").getAsString()) {
				case "Ally":
					try {
						if(place.getAsJsonPrimitive("CharacterType").getAsString().contains("epic") || place.getAsJsonPrimitive("CharacterType").getAsString().contains("captain")) {
							map[x-1][y+1].addProperty("occupied", true);
							map[x-1][y].addProperty("occupied", true);
							map[x][y+1].addProperty("occupied", true);
						}
						
					} catch (Exception e) {}
					map[x][y].addProperty("unit", 1);
					break;
				case "Enemy":
					map[x][y].addProperty("unit", 2);
					break;
				default:
					System.err.println("Unit team incorrect: " + place.getAsJsonPrimitive("team").getAsString());
					map[x][y].addProperty("unit", 0);
				}
			} catch (Exception e) {
				map[x][y].addProperty("unit", 2);
			}
			
			map[x][y].add("data", place);
			
		}
		return map;
	}

	private JsonObject[][] addRects(JsonObject[][] map, JsonArray rects, String prop) {
		if(rects == null) return map;
		for(int i=0; i<rects.size(); i++) {
			JsonObject rect = rects.get(i).getAsJsonObject();
			
			int x = (int) Math.round(rect.getAsJsonPrimitive("x").getAsDouble() / 0.8 + hw() + 0.5);
			int y = (int) Math.round((rect.getAsJsonPrimitive("y").getAsDouble() / 0.8 - hl()) * -1 + 0.5);
			int w = (int) Math.round(rect.getAsJsonPrimitive("width").getAsDouble() / 0.8 - 1);
			int h = (int) Math.round(rect.getAsJsonPrimitive("height").getAsDouble() / 0.8 - 1);
			
			
			
			if(prop.contains("player")) {
				JsonObject nrect = new JsonObject();
				nrect.addProperty("x", x);
				nrect.addProperty("y", y-h);
				nrect.addProperty("w", w);
				nrect.addProperty("h", h);
				playerRects.add(nrect);
			}
			
			for(int j=x; j<=x+w; j++) {
				for(int k=y-h; k<=y; k++) {
					map[j][k].addProperty(prop, true);
				}
			}
			
		}
		return map;
	}
	
	
	/*
	 * p, e, h: rects
	 * o: obsticale
	 * a: ally
	 * f: foe (enemy)
	 *  : nothing
	 * 
	 * 
	 */
	
	
}
