package include;

import program.Debug;

public class Pathfinding {

	
	public static class Field {
		
		private boolean isExplored = false;
		
		private boolean isFinish = false;
		private boolean isObstacle = false;
		
		private int cost = Integer.MAX_VALUE;
		
		public void setHome() {
			cost = 0;
		}
		
		public void setFinish(boolean isFinish) {
			this.isFinish = isFinish;
		}
		
		public void setObstacle(boolean isObstacle) {
			this.isObstacle = isObstacle;
		}
		
		public boolean isExplored() {
			return isExplored;
		}
		
		public boolean isFinish() {
			return isFinish;
		}
		
		public boolean isObstacle() {
			return isObstacle;
		}
		
		public boolean setCost(int cost) {
			if(this.cost > cost && !isObstacle) {
				this.cost = cost;
				isExplored = false;
			}
			return isFinish;
		}
		
		public void explore() {
			isExplored = true;
		}
		
		public int getCost() {
			return cost;
		}
	}
	
	private Field[][] map;
	
	public int[] search(Field[][] map, String name, boolean big) {
		
		this.map = map;
		
		int x, y, min, ox, oy, omin;
		
		int[] last = null;
		
		int c = 1;
		
		while(true) {
			
			if(++c % 500 == 0) {
				Debug.print("[" + name + "] Pathfinding search " + c, Debug.loop);
			}
			
			boolean stuck = true;
			
			x = -1;
			y = -1;
			min = Integer.MAX_VALUE;
			
			ox = -1;
			oy = -1;
			omin = Integer.MAX_VALUE;
			
			for(int i=0; i<map.length; i++) {
				for(int j=0; j<map[i].length; j++) {
					if(map[i][j].isExplored) continue;
					
					if(map[i][j].isObstacle)
						map[i][j].explore();
					
					stuck = false;
					
					int cost = map[i][j].getCost();
					
					if(map[i][j].isObstacle && min == Integer.MAX_VALUE && omin > cost) {
						ox = i;
						oy = j;
						omin = cost;
					} else if(min > cost) {
						x = i;
						y = j;
						min = cost;
					}
				}
			}
			
			if(stuck) return last;
			
			if(min == Integer.MAX_VALUE) {
				x = ox;
				y = oy;
				min = omin;
			}
			
			if(x == -1 || y == -1)
				return last;
			
			map[x][y].explore();
			
			for(int i=-1; i<2; i++) {
				for(int j=-1; j<2; j++) {
					if(i==0 && j==0) continue;
					
					if(check(x+i, y+j)) {
						int ran;
						if(i==0 || j==0) 
							ran = ranInt(7, 12);
						else 
							ran = ranInt(10, 15);
						
						if(map[x+i][y+j].setCost(min+ran)) {
							if(!big || (
									(check(x+i-1, y+j) && map[x+i-1][y+j].isFinish()) &&
									(check(x+i-1, y+j+1) && map[x+i-1][y+j+1].isFinish()) &&
									(check(x+i, y+j+1) && map[x+i][y+j+1].isFinish())
									)) {
								last = new int[] {x+i, y+j};
								if(ranInt(0, big ? 5 : 20) == 0)
									return last;
							}
							
						}
					}
				}
			}
			
		}
	}
	
	private static int ranInt(int min, int max) {
		return (int) Math.round((Math.random()*(max-min))+min);
	}
	
	private boolean check(int x, int y) {
		if(x<0 || x>=map.length) return false;
		if(y<0 || y>=map[x].length) return false;
		return true;
	}
}
