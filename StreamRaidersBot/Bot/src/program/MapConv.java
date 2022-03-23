package program;


import java.util.HashSet;

import include.Pathfinding.Field;

public class MapConv {
	
	private boolean found = false;
	
	public static class NoFinException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public Field[][] asField(Map map, boolean canFly, HashSet<String> pts, int[] h, HashSet<String> banned) throws NoFinException {
		
		int length = map.length;
		int width = map.width;
		
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
				} else if(pts != null && pts.contains(pt)) {
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
	
	
	
}
