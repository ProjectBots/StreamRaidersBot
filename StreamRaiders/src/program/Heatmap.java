package program;


import com.google.gson.JsonObject;

public class Heatmap {

	/*
	public static int[] get(JsonObject[][] map, int r) {
		
		int[] maxheat = getMaxHeat(map, r);
		
		int[] nearest = getNearest(map, maxheat, null);
		
		return nearest;
	}
	*/
	
	public static int[] getMaxHeat(JsonObject[][] map, int r) {
		int[] maxheat = new int[] {0, 0, -100000};
		int ec = 0;
		for(int x=0; x<map.length; x++) {
			for(int y=0; y<map[y].length; y++) {
				int heat = 0;
				if(map[x][y].getAsJsonPrimitive("unit").getAsInt() == 2) ec++;
				for(int i=x-r; i<x+r; i++) {
					if(i < 0 || i >= map.length) continue;
					for(int j=y-r; j<y+r; j++) {
						if(j < 0 || j >= map[i].length || Vector2.dis(x, y, i, j) > r + 0.01) continue;
						if(map[i][j].getAsJsonPrimitive("unit").getAsInt() == 2) heat++;
					}
				}
				if(heat > maxheat[2]) maxheat = new int[] {x, y, heat};
			}
		}
		return (maxheat[2] <= 2 && ec >= 3 ? getMaxHeat(map, r+1) : maxheat);
	}
	
	public static int[] getNearest(JsonObject[][] map, int[] pos, int[][] banned) {
		double[] nearest = new double[] {-1, -1, 1000000};
		
		for(int x=0; x<map.length; x++) {
			loop:
			for(int y=0; y<map[x].length; y++) {
				for(int i=0; i<banned.length; i++) {
					if(banned[i][0] == x && banned[i][1] == y) continue loop;
				}
				if(map[x][y].getAsJsonPrimitive("playerRect").getAsBoolean() 
						&& map[x][y].getAsJsonPrimitive("unit").getAsInt() == 0
						&& !map[x][y].getAsJsonPrimitive("occupied").getAsBoolean()) {
					double dis = Vector2.dis(x, y, pos[0], pos[1]);
					if(dis < nearest[2]) nearest = new double[] {x, y, dis};
				}
			}
		}
		
		return new int[] {(int) Math.round(nearest[0]), (int) Math.round(nearest[1])};
	}
	
	
}
