package userInterface;

import include.GUI;
import include.Heatmap;
import include.GUI.Label;
import include.GUI.TextArea;
import include.Maths.Scaler;
import include.Http.NoConnectionException;
import program.Debug;
import program.Map;
import program.SRC;
import program.SRR.NotAuthorizedException;
import run.Run;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.google.gson.JsonArray;

public class MapGUI {

	public static void showPlanTypes(GUI parent) {
		
		JsonArray pts = Map.getSeenPlanTypes();
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<pts.size(); i++) {
			sb.append(pts.get(i).getAsString() + "\n");
		}
		
		if(sb.length() == 0) {
			return;
		}
		
		GUI gui = new GUI("Plan Types", 400, 500, parent, null);
		
		
		TextArea ta = new TextArea();
		ta.setEditable(false);
		ta.setText(sb.substring(0, sb.length()-1));
		gui.addTextArea(ta);
		
		gui.refresh();
		
	}
	
	public static void showLastHeatMap(GUI parrent, Heatmap heatMap, String name, int[] h) {
		
		double[][] hmap = heatMap.getHMap();
		
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
		
		Scaler sc = new Scaler(min, max, 255, 0).setDecPl(0);
		
		GUI map = new GUI("Heatmap " + name, 1000, 800, parrent, null);
		map.setFullScreen(true);
		
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
				int c = (int) sc.scale(hmap[i][j]);
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
	
	
	public static void asGui(GUI parrent, Run run, int slot) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if(run == null)
					return;
				run.useBackEndHandler(beh -> {
					try {
						if(!run.canUseSlot(beh, slot))
							return;
					} catch (NoConnectionException | NotAuthorizedException e2) {
						Debug.printException("MapGUI -> asGui: err=failed to get canUseSlot", e2, Debug.runerr, Debug.error, run.getPN(), slot, true);
						return;
					}
					Map map;
					try {
						map = run.getMap(beh, slot);
					} catch (NoConnectionException | NotAuthorizedException e1) {
						Debug.printException("MapGUI -> asGui: err=failed to get Map", e1, Debug.runerr, Debug.error, run.getPN(), slot, true);
						return;
					}
					if(map == null)
						return;
					
					GUI gui = new GUI("Map " + map.getName(), 1000, 800, parrent, null);
					gui.setFullScreen(true);
					
					gui.setGlobalKeyLis(new KeyListener() {
						@Override
						public void keyTyped(KeyEvent e) {}
						@Override
						public void keyReleased(KeyEvent e) {}
						@Override
						public void keyPressed(KeyEvent e) {
							if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) > 0) {
								Heatmap hm = new Heatmap();
								showLastHeatMap(gui, hm, map.getName(), hm.getMaxHeat(map));
							} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
								switch(e.getKeyCode()) {
								}
							} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
								switch(e.getKeyCode()) {
								case KeyEvent.VK_R:
									gui.close();
									run.useBackEndHandler(beh -> {
										try {
											beh.updateMap(run.getPN(), slot, true);
										} catch (NoConnectionException | NotAuthorizedException e1) {
											Debug.printException("MapGUI -> asGUI -> reload: err=failed to update Map", e1, Debug.runerr, Debug.error, run.getPN(), slot, true);
											return;
										}
										asGui(gui, run, slot);
									});
									break;
								}
							} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
								switch(e.getKeyCode()) {
								}
							}
						}
					});
					
					
					for(int x=0; x<map.width(); x++) {
						for(int y=0; y<map.length(); y++) {

							if(map.is(x, y, SRC.Map.isOccupied)) continue;
							
							int s = 10;
							
							Label l = new Label();
							l.setText("");
							l.setSize(s, s);
							l.setInsets(0, 0, 0, 0);
							l.setOpaque(true);
							l.setPos(x, y);
							l.setBackground(Color.white);
							
							if(map.is(x, y, SRC.Map.isEnemyRect)) l.setBackground(new Color(255, 143, 143));
							if(map.is(x, y, SRC.Map.isPlayerRect)) l.setBackground(new Color(0, 204, 255));
							if(map.is(x, y, SRC.Map.isHoldRect)) l.setBackground(new Color(153, 0, 153));
							
							if(map.is(x, y, SRC.Map.isObstacle)) {
								if(map.is(x, y, SRC.Map.canWalkOver)) {
									if(!map.is(x, y, SRC.Map.isPlayerRect))
										l.setBackground(new Color(145, 145, 145));
								} else if(map.is(x, y, SRC.Map.canFlyOver)) 
									l.setBackground(new Color(94, 94, 94));
								else
									l.setBackground(new Color(51, 51, 51));
							}

							if(map.is(x, y, SRC.Map.isEnemy)) {
								l.setBackground(Color.red);
								l.setBorder(Color.black, 1);
							}
							
							if(map.is(x, y, SRC.Map.isNeutral)) {
								l.setBackground(new Color(244, 242, 170));
								l.setBorder(Color.black, 1);
							}

							if(map.is(x, y, SRC.Map.isAllied)) {
								if(map.is(x, y, SRC.Map.isEpic)) {
									l.setPos(x-1, y);
									l.setSpan(2, 2);
									l.setSize(s*2, s*2);
								}
								if(map.is(x, y, SRC.Map.isCaptain))
									l.setBackground(new Color(200, 255, 0));
								else if(map.is(x, y, SRC.Map.isSelf))
									l.setBackground(new Color(255, 106, 0));
								else if(map.is(x, y, SRC.Map.isOther))
									l.setBackground(new Color(255, 200, 0));
								else
									l.setBackground(Color.green);
								l.setBorder(Color.black, 1);
								if(!map.is(x, y, SRC.Map.isPlayer)) {
									l.setText("X");
									l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
								} else {
									l.setTooltip(map.getUserName(x, y));
								}
							}
							
							
							String plan = map.getPlanType(x, y);
							if(plan != null && map.is(x, y, SRC.Map.isEmpty)) {
								switch (plan) {
								case "supportHealer":
									l.setText("H");
									break;
								case "supportFlag":
									l.setText("L");
									break;
								case "assassinFlyingExplosive":
									l.setText("B");
									break;
								case "assassin":
									l.setText("I");
									break;
								default:
									l.setText(plan.replace("assassin", "").toUpperCase().substring(0, 1));
									break;
								}
								l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
							}
							
							gui.addLabel(l);
						}
					}
					
					gui.refresh();
				});
				
				
			}
		});
		t.start();
	}
}
