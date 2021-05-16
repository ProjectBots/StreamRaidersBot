package include;

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
			if(this.cost > cost) {
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
	
	
	private static int width; 
	private static int height;
	
	public static int[] search(Field[][] map) {
		
		width = map.length;
		height = map[0].length;
		
		int x, y, min, ox, oy, omin;
		
		while(true) {
			
			boolean stuck = true;
			
			x = -1;
			y = -1;
			min = Integer.MAX_VALUE;
			
			ox = -1;
			oy = -1;
			omin = Integer.MAX_VALUE;
			
			for(int i=0; i<width; i++) {
				for(int j=0; j<height; j++) {
					if(map[i][j].isExplored) continue;
					
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
			
			if(stuck) return null;
			
			if(min == Integer.MAX_VALUE) {
				x = ox;
				y = oy;
				min = omin;
			}
			
			map[x][y].explore();
			
			
			for(int i=-1; i<2; i++) {
				for(int j=-1; j<2; j++) {
					if(i==0 && j==0) continue;
					
					if(check(x+i, y+j)) {
						if(i==0 || j==0) {
							if(map[x+i][y+j].setCost(min+ranInt(7, 12))) return new int[] {x+i, y+j};
						} else {
							if(map[x+i][y+j].setCost(min+ranInt(10, 15))) return new int[] {x+i, y+j};
						}
					}
				}
			}
			
		}
	}
	
	private static int ranInt(int min, int max) {
		return (int) Math.round((Math.random()*(max-min))+min);
	}
	
	private static boolean check(int x, int y) {
		if(x<0 || y<0 || x>=width || y>=height) return false;
		return true;
	}
}
