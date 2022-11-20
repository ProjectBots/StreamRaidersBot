package otherlib;


import java.util.Set;

import include.Maths.PointN;
import include.Pathfinding.Field;
import srlib.map.Map;
import srlib.map.Place;
import srlib.map.PlacementRectType;
import srlib.map.Team;

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
				Place p = map.get(x, y);
				if(p == null)
					continue;
				String pt = p.getPlanType();
				if(pt == null)
					pt = "Programmed by ProjectBots";
				if(!p.canWalkOver()) {
					if(canFly && p.canFlyOver()) 
						continue;
					fields[x][y].setObstacle(true);
				} else if(pts == null && p.getPlacementRectType() == PlacementRectType.PLAYER) {
					setFin(fields, p, x, y, banned);
				} else if(pts != null && pts.contains(pt)) {
					setFin(fields, p, x, y, banned);
				}
			}
		}
		
		if(!found)
			throw new NoFinException();
		
		fields[h[0]][h[1]].setHome();
		
		return this;
	}
	
	private void setFin(Field[][] ret, Place p, int x, int y, Set<String> banned) {
		if(banned.contains(x+"-"+y))
			return;
		
		if(!p.isEmpty()) 
			return;
		
		for(int i=-2; i<3; i++) 
			for(int j=-2; j<3; j++) 
				if(p.getTeam() == Team.ENEMY) 
					return;
		
		String plan = p.getPlanType();
		
		ret[x][y].setFinish((short) (plan != null && (plan.equals("noplacement") || plan.equals("vibe")) ? 0 : 1));
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
					Place p = map.get(x, y);
					if(p == null)
						continue;
				
					if(p.getTeam() == (k==0 ? Team.PLAYER : Team.ENEMY) || p.isOccupied()) {
						pc++;
						PointN pn = new PointN(x, y);
						double mul = p.isCaptain() ? 2 : 1;
						for(int i=0; i<hmap.length; i++) {
							for(int j=0; j<hmap[i].length; j++) {
								double dis = pn.dis(new PointN(i, j));
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
