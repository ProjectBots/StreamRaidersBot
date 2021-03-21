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
	
	private static Field[][] lastMap = null;
	
	public static Field[][] getLastMap() {
		return lastMap;
	}
	
	public static int[] search(Field[][] map) {
		
		int width = map.length;
		int height = map[0].length;
		
		while(true) {
			
			int x = -1;
			int y = -1;
			int min = Integer.MAX_VALUE;
			
			for(int i=0; i<width; i++) {
				for(int j=0; j<height; j++) {
					if(map[i][j].isExplored) continue;
					if(map[i][j].isObstacle) continue;
					
					if(min > map[i][j].getCost()) {
						x = i;
						y = j;
						min = map[i][j].getCost();
					}
				}
			}
			
			map[x][y].explore();
			
			
			if(x+1 < width) if(map[x+1][y].setCost(min+10)) return new int[] {x+1, y};
			if(x-1 >= 0) if(map[x-1][y].setCost(min+10)) return new int[] {x-1, y};
			if(y+1 < height) if(map[x][y+1].setCost(min+10)) return new int[] {x, y+1};
			if(y-1 >= 0) if(map[x][y-1].setCost(min+10)) return new int[] {x, y-1};

			if(x+1 < width && y+1 < height) if(map[x+1][y+1].setCost(min+14)) return new int[] {x+1, y+1};
			if(x+1 < width && y-1 >= 0) if(map[x+1][y-1].setCost(min+14)) return new int[] {x+1, y-1};
			if(x-1 >= 0 && y+1 < height) if(map[x-1][y+1].setCost(min+14)) return new int[] {x-1, y+1};
			if(x-1 >= 0 && y-1 >= 0) if(map[x-1][y-1].setCost(min+14)) return new int[] {x-1, y-1};
			
		}
		
	}
	
}
