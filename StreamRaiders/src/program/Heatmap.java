package program;

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
	
	
}
