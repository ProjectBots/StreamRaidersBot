package include;

import program.Map;
import program.SRC;

public class Heatmap {

	public static int[] getMaxHeat(Map map) {
		double[][] hmap = new double[map.length()][map.width()];
		for(int x=0; x<hmap.length; x++) {
			for(int y=0; y<hmap[x].length; y++) {
				if(map.is(x, y, SRC.Map.isObstacle)) continue;
				if(map.is(x, y, SRC.Map.isEnemy)) {
					for(int i=0; i<hmap.length; i++) {
						for(int j=0; j<hmap[i].length; j++) {
							hmap[i][j] += (double) 1 / Vector2.dis(x, y, i, j);
						}
					}
				}
			}
		}

		int[] maxheat = new int[] {0, 0};
		double heat = -1;
		for(int i=0; i<hmap.length; i++) {
			for(int j=0; j<hmap[i].length; j++) {
				if(hmap[i][j] > heat) {
					maxheat = new int[] {i, j};
					heat = hmap[i][j];
				}
			}
		}
		return maxheat;
	}
	
	
}
