package include;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import include.GUI.Label;
import program.Map;
import program.SRC;

public class Heatmap {
	
	private double[][] hmap = null;
	
	public int[] getMaxHeat(Map map) {
		hmap = new double[map.width()][map.length()];
		int pc = 0;
		for(int k=0; k<2; k++) {
			for(int x=0; x<hmap.length; x++) {
				for(int y=0; y<hmap[x].length; y++) {
					if(map.is(x, y, SRC.Map.isObstacle)) continue;
					if(map.is(x, y, k==0 ? SRC.Map.isPlayer : SRC.Map.isEnemy)) {
						pc++;
						for(int i=0; i<hmap.length; i++) {
							for(int j=0; j<hmap[i].length; j++) {
								double dis = Vector2.dis(x, y, i, j);
								if(dis < 0.00001) {
									hmap[i][j] += 1;
									continue;
								}
								double c = 1.0 / dis;
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
	
	
	public void showLastHeatMap(String name, int[] h) {
		double min = Double.MAX_VALUE, max = -1;
		for(int i=0; i<hmap.length; i++) {
			for(int j=0; j<hmap[i].length; j++) {
				double m = hmap[i][j];
				if(m < min)
					min = m;
				if(m > max)
					max = m;
			}
		}
		max -= min;
		double conv = (double) 255 / max;
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		GUI map = new GUI("Heatmap " + name, (int) Math.round(size.getWidth()), (int) Math.round(size.getHeight()));
		
		map.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if(!((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0)) return;
				if(!((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) > 0)) return;
				map.close();
			}
		});
		
		for(int i=0; i<hmap.length; i++) {
			for(int j=0; j<hmap[i].length; j++) {
				int c = 255 - (int) Math.round((hmap[i][j] - min) * conv);
				Label l = new Label();
				l.setPos(i, j);
				l.setText("");
				if(i == h[0] && j == h[1])
					l.setBackground(Color.red);
				else
					l.setBackground(new Color(c, c, c));
				l.setSize(10, 10);
				l.setOpaque(true);
				l.setInsets(0, 0, 0, 0);
				map.addLabel(l);
			}
		}
		
		map.refresh();
	}
	
	
}
