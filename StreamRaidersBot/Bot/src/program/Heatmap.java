package program;

import include.Maths.PointN;

public class Heatmap {
	
	private double[][] hmap = null;
	
	public double[][] getHMap() {
		return hmap;
	}
	
	public int[] getMaxHeat(Map map) {
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
