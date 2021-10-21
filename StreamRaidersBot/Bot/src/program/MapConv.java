package program;


import java.io.IOException;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.Json;
import include.NEF;
import include.Pathfinding.Field;

public class MapConv {
	
	private boolean found = false;
	
	public static class NoFinException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	
	
	public Field[][] asField_old(Map map, boolean canFly, String planType, int[] h, int[][] banned) throws NoFinException {
		
		int length = map.length();
		int width = map.width();
		
		Field[][] ret = new Field[width][length];
		
		found = false;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				ret[x][y] = new Field();
				String pt = map.getPlanType(x, y);
				if(pt == null) pt = "Programmed by ProjectBots";
				if(map.is(x, y, SRC.Map.isObstacle) && !map.is(x, y, SRC.Map.canWalkOver)) {
					if(canFly && map.is(x, y, SRC.Map.canFlyOver)) 
						continue;
					ret[x][y].setObstacle(true);
				} else if(planType == null && map.is(x, y, SRC.Map.isPlayerRect)) {
					setFin_old(ret, map, x, y, banned);
				} else if(pt.equals(planType)) {
					setFin_old(ret, map, x, y, banned);
				}
			}
		}
		
		if(!found)
			throw new NoFinException();
		
		ret[h[0]][h[1]].setHome();
		
		return ret;
	}
	
	private void setFin_old(Field[][] ret, Map map, int x, int y, int[][] banned) {
		
		for(int i=0; i< banned.length; i++)
			if(banned[i][0] == x && banned[i][1] == y) 
				return;
		
		if(!map.is(x, y, SRC.Map.isEmpty)) 
			return;
		
		for(int i=-2; i<3; i++) 
			for(int j=-2; j<3; j++) 
				if(map.is(x+i, y+j, SRC.Map.isEnemy)) 
					return;
			
		
		
		ret[x][y].setFinish(true);
		found = true;
		
		return;
	}
	
	public Field[][] asField(Map map, boolean canFly, JsonArray pts, int[] h, HashSet<String> banned) throws NoFinException {
		
		int length = map.length();
		int width = map.width();
		
		Field[][] ret = new Field[width][length];
		
		found = false;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				ret[x][y] = new Field();
				String pt = map.getPlanType(x, y);
				if(pt == null) pt = "Programmed by ProjectBots";
				if(map.is(x, y, SRC.Map.isObstacle) && !map.is(x, y, SRC.Map.canWalkOver)) {
					if(canFly && map.is(x, y, SRC.Map.canFlyOver)) 
						continue;
					ret[x][y].setObstacle(true);
				} else if(pts == null && map.is(x, y, SRC.Map.isPlayerRect)) {
					setFin(ret, map, x, y, banned);
				} else if(pts != null && pts.contains(new JsonPrimitive(pt))) {
					setFin(ret, map, x, y, banned);
				}
			}
		}
		
		if(!found)
			throw new NoFinException();
		
		ret[h[0]][h[1]].setHome();
		
		return ret;
	}
	
	private void setFin(Field[][] ret, Map map, int x, int y, HashSet<String> banned) {
		if(banned.contains(x+"-"+y))
			return;
		
		if(!map.is(x, y, SRC.Map.isEmpty)) 
			return;
		
		for(int i=-2; i<3; i++) 
			for(int j=-2; j<3; j++) 
				if(map.is(x+i, y+j, SRC.Map.isEnemy)) 
					return;
		
		ret[x][y].setFinish(true);
		found = true;
	}
	
	
	public static Map load(String map) {
		return new Map(Json.parseArr(map));
	}
	
	public static void save(String path, Map map) throws IOException {
		NEF.save(path, Json.prettyJson(map.getMap()));
	}
	
	
	
	
}
