package otherlib;


import java.util.Set;

import include.Maths.PointN;
import include.Pathfinding.Field;
import srlib.Map;
import srlib.SRC;

public class MapConv {
	
	public static class NoFinException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	private boolean found = false;
	
	private Field[][] fields = null;
	
	public Field[][] getField2DArray() {
		return fields;
	}
	
	public MapConv createField2DArray(Map map, boolean canFly, Set<String> pts, int[] h, Set<String> banned) throws NoFinException {
		
		int length = map.length;
		int width = map.width;
		
		fields = new Field[width][length];
		found = false;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				fields[x][y] = new Field();
				String pt = map.getPlanType(x, y);
				if(pt == null)
					pt = "Programmed by ProjectBots";
				if(map.is(x, y, SRC.Map.isObstacle) && !map.is(x, y, SRC.Map.canWalkOver)) {
					if(canFly && map.is(x, y, SRC.Map.canFlyOver)) 
						continue;
					fields[x][y].setObstacle(true);
				} else if(pts == null && map.is(x, y, SRC.Map.isPlayerRect)) {
					setFin(fields, map, x, y, banned);
				} else if(pts != null && pts.contains(pt)) {
					setFin(fields, map, x, y, banned);
				}
			}
		}
		
		if(!found)
			throw new NoFinException();
		
		fields[h[0]][h[1]].setHome();
		
		return this;
	}
	
	private void setFin(Field[][] ret, Map map, int x, int y, Set<String> banned) {
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

	
	private double[][] hmap = null;
	private int[] maxHeat = null;
	
	public double[][] getHMap() {
		return hmap;
	}
	
	public int[] getMaxHeat() {
		return maxHeat;
	}
	
	public MapConv createHeatMap(Map map) {
		hmap = new double[map.width][map.length];
		int pc = 0;
		for(int k=0; k<2; k++) {
			for(int x=0; x<hmap.length; x++) {
				for(int y=0; y<hmap[x].length; y++) {
					if(map.is(x, y, k==0 ? SRC.Map.isPlayer : SRC.Map.isEnemy) || map.is(x, y, SRC.Map.isOccupied)) {
						pc++;
						PointN p = new PointN(x, y);
						double mul = map.is(x, y, SRC.Map.isCaptain) ? 2 : 1;
						for(int i=0; i<hmap.length; i++) {
							for(int j=0; j<hmap[i].length; j++) {
								double dis = p.dis(new PointN(i, j));
								if(dis < 0.00001) {
									hmap[i][j] += mul;
									continue;
								}
								double c = mul / dis;
								if(Double.isFinite(c))
									hmap[i][j] += c;
							}
						}
					}
				}
			}
			if(pc > 0)
				break;
		}
		

		maxHeat = new int[] {0, 0};
		double heat = -1;
		for(int i=0; i<hmap.length; i++) {
			for(int j=0; j<hmap[i].length; j++) {
				if(hmap[i][j] > heat) {
					maxHeat = new int[] {i, j};
					heat = hmap[i][j];
				}
			}
		}
		
		return this;
	}
	
	
}
