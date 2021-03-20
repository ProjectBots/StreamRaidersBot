package program;

import java.util.Arrays;

public class Heatmap {

	public static int[] getMaxHeat(Map map, int r) {
		int[] maxheat = new int[] {0, 0, -100000};
		int ec = 0;
		for(int x=0; x<map.width(); x++) {
			for(int y=0; y<map.length(); y++) {
				int heat = 0;
				if(map.is(x, y, SRC.Map.isEnemy)) ec++;
				for(int i=x-r; i<x+r; i++) {
					for(int j=y-r; j<y+r; j++) {
						if(Vector2.dis(x, y, i, j) > r+0.001) continue;
						if(map.is(i, j, SRC.Map.isEnemy)) heat++;
					}
				}
				if(heat > maxheat[2]) maxheat = new int[] {x, y, heat};
			}
		}
		return (maxheat[2] <= 2 && ec >= 3 ? getMaxHeat(map, r+1) : maxheat);
	}
	
	public static int[] getNearest(Map map, int[] pos, int[][] banned) {
		double[] nearest = new double[] {-1, -1, 1000000};
		
		for(int x=0; x<map.width(); x++) {
			loop:
			for(int y=0; y<map.length(); y++) {
				for(int i=0; i<banned.length; i++) {
					if(banned[i][0] == x && banned[i][1] == y) continue loop;
				}
				if(map.is(x, y, SRC.Map.isPlayerRect) && map.is(x, y, SRC.Map.isEmpty)) {
					double dis = Vector2.dis(x, y, pos[0], pos[1]);
					if(dis < nearest[2] && dis > 2-0.001) nearest = new double[] {x, y, dis};
				}
			}
		}
		return new int[] {(int) Math.round(nearest[0]), (int) Math.round(nearest[1])};
	}
	
	
}
