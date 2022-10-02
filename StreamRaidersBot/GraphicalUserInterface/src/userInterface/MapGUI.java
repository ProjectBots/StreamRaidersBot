package userInterface;

import include.GUI;
import include.GUI.Label;
import include.GUI.TextArea;
import include.Maths.Scaler;
import otherlib.Logger;
import otherlib.MapConv;
import run.viewer.Viewer;
import srlib.Map;
import srlib.SRC;
import srlib.units.UnitType;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

import javax.swing.SwingConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

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
	
	public static void showHeatMap(GUI parrent, MapConv heatMap, String name) {
		
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
		
		int[] h = heatMap.getMaxHeat();
		
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
	
	private static final Hashtable<String, String> shortenedUnitAndPlanTypes = new Hashtable<String, String>() {
		private static final long serialVersionUID = 1L; {
			//	general
			put("noplacement", "N");
			put("vibe", "V");
			put("assassin", "D");
			put("support", "S");
			put("armored", "A");
			put("melee", "m");
			put("ranged", "R");
			
			//	common
			put("archer", "a");
			put("rogue", "r");
			put("warrior", "w");
			put("tank", "t");
			put("flagbearer", "f");
			
			//	uncommon
			put("buster", "k");
			put("barbarian", "B");
			put("paladin", "p");
			put("alliespaladin", "p");
			put("healer", "h");
			put("vampire", "v");
			put("saint", "s");
			put("bomber", "b");
			
			//	rare
			put("centurion", "c");
			put("flyingrogue", "F");
			put("flyingarcher", "F");
			put("monk", "H");
			put("musketeer", "u");
			put("berserker", "z");
			put("shinobi", "E");
			
			//	legendary
			put("warbeast", "W");
			put("necromancer", "n");
			put("artillery", "Y");
			put("templar", "T");
			put("orcslayer", "O");
			put("mage", "G");
			put("balloonbuster", "K");
			put("alliesballoonbuster", "K");
		}
	};
	
	public static String getShortenedUnitAndPlanType(String type) {
		return shortenedUnitAndPlanTypes.containsKey(type) ? shortenedUnitAndPlanTypes.get(type) : " ";
	}
	
	public static void asGui(GUI parrent, Viewer run, int slot) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if(run == null)
					return;
				try {
					run.useBackEnd(vbe -> {
						if(!Viewer.canUseSlot(vbe, slot))
							return;
						
						Map map = vbe.getMap(slot, false);
						if(map == null)
							return;
						
						GUI gui = new GUI("MapName: " + map.name + ", Power: "+map.getPlayerPower()+"::"+map.mapPower, 1000, 800, parrent, null);
						gui.setFullScreen(true);
						
						gui.setGlobalKeyLis(new KeyListener() {
							@Override
							public void keyTyped(KeyEvent e) {}
							@Override
							public void keyReleased(KeyEvent e) {}
							@Override
							public void keyPressed(KeyEvent e) {
								if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) > 0) {
									MapConv hm = new MapConv().createHeatMap(map);
									showHeatMap(gui, hm, map.name);
								} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
									switch(e.getKeyCode()) {
									}
								} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
									switch(e.getKeyCode()) {
									case KeyEvent.VK_R:
										gui.close();

										try {
											run.useBackEnd(beh -> {
												beh.updateMap(slot, true);
												asGui(gui, run, slot);
											});
										} catch (Exception e1) {
											Logger.printException("MapGUI -> asGUI -> reload: err=failed to update Map", e1, Logger.runerr, Logger.error, run.cid, slot, true);
										}
										break;
									}
								} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
									switch(e.getKeyCode()) {
									}
								}
							}
						});
						
						
						for(int x=0; x<map.width; x++) {
							for(int y=0; y<map.length; y++) {

								if(map.is(x, y, SRC.Map.isOccupied))
									continue;
								
								int s = 10;
								
								Label l = new Label();
								l.setText("");
								l.setSize(s, s);
								l.setInsets(0, 0, 0, 0);
								l.setOpaque(true);
								l.setPos(x, y);
								l.setHalign(SwingConstants.CENTER);
								l.setValign(SwingConstants.CENTER);
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
								//	TODO switch with enums
								if(map.is(x, y, SRC.Map.isEnemy)) {
									l.setBackground(Color.red);
									l.setBorder(Color.black, 1);
								} else if(map.is(x, y, SRC.Map.isNeutral)) {
									l.setBackground(new Color(244, 242, 170));
									l.setBorder(Color.black, 1);
								} else if(map.is(x, y, SRC.Map.isAllied)) {
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
									} else {
										JsonElement uType = map.get(x, y).get("unitType");
										String ut = null;
										if(uType != null)
											l.setText(getShortenedUnitAndPlanType(ut = uType.getAsString()));
										l.setTooltip(map.getDisplayName(x, y) + (ut==null?"":" - "+UnitType.types.get(ut).name));
									}
									l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
								}
								
								String plan = map.getPlanType(x, y);
								if(plan != null && map.is(x, y, SRC.Map.isEmpty)) {
									l.setText(getShortenedUnitAndPlanType(plan));
									l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
								}
								
								gui.addLabel(l);
							}
						}
						
						gui.refresh();
					});
				} catch (Exception e) {
					Logger.printException("MapGUI -> asGui: err=failed to load", e, Logger.runerr, Logger.error, run.cid, slot, true);
				}
				
				
			}
		});
		t.start();
	}
	
}
