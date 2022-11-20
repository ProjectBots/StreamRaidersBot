package userInterface;

import include.GUI;
import include.GUI.Label;
import include.GUI.TextArea;
import include.Maths.Scaler;
import otherlib.Logger;
import otherlib.MapConv;
import run.viewer.Viewer;
import run.viewer.ViewerBackEnd;
import srlib.map.Map;
import srlib.map.Place;
import srlib.map.PlacementRectType;
import srlib.map.Team;
import srlib.units.UnitType;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.SwingConstants;

public class MapGUI {

	public static void showPlanTypes(GUI parent) {
		
		ArrayList<String> pts = Map.getSeenPlanTypes();
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<pts.size(); i++) {
			sb.append(pts.get(i) + "\n");
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
	
	private static final int SQUARE_SIZE = 10;
	
	public static void asGui(GUI parrent, Viewer v, int slot) {
		if(v == null)
			return;
		
		try {
			ViewerBackEnd vbe = v.getBackEnd();
			
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
							try {
								vbe.updateRaidPlan(slot, true);
								asGui(gui, v, slot);
								gui.close();
							} catch (Exception e1) {
								Logger.printException("MapGUI -> asGUI -> reload: err=failed to update Map", e1, Logger.runerr, Logger.error, v.cid, slot, true);
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
					
					Place p = map.get(x, y);
					
					Label l = new Label();
					l.setText("");
					l.setSize(SQUARE_SIZE, SQUARE_SIZE);
					l.setInsets(0, 0, 0, 0);
					l.setOpaque(true);
					l.setPos(x, y);
					l.setHalign(SwingConstants.CENTER);
					l.setValign(SwingConstants.CENTER);
					l.setBackground(Color.white);

					if(p == null) {
						gui.addLabel(l);
						continue;
					}
					
					if(p.isOccupied())
						continue;
					
					PlacementRectType prt = p.getPlacementRectType();
					if(prt != null) {
						l.setBackground(switch(prt) {
							case PLAYER -> new Color(0, 204, 255);
							case ENEMY -> new Color(255, 143, 143);
							case HOLDING -> new Color(153, 0, 153);
						});
					}
					
					if(p.isObstacle()) {
						if(p.canWalkOver()) {
							if(prt != PlacementRectType.PLAYER && prt != PlacementRectType.HOLDING)
								l.setBackground(new Color(145, 145, 145));
						} else if(p.canFlyOver())
							l.setBackground(new Color(94, 94, 94));
						else
							l.setBackground(new Color(51, 51, 51));
					}
					
					Team team = p.getTeam();
					if(team != null) {
						l.setBackground(Color.green);
						switch(p.getTeam()) {
						case ENEMY:
							l.setBackground(Color.red);
							l.setBorder(Color.black, 1);
							break;
						case NEUTRAL:
							l.setBackground(new Color(244, 242, 170));
							l.setBorder(Color.black, 1);
							break;
						case PLAYER:
							UnitType uType = p.getUnitType();
							if(uType != null)
								l.setText(getShortenedUnitAndPlanType(uType.uid));
							l.setTooltip(p.getTwitchDisplayName() + (uType==null ? "" : " - "+uType.name));
							
							if(p.isCaptain())
								l.setBackground(new Color(200, 255, 0));
							else if(p.isSelf())
								l.setBackground(new Color(255, 106, 0));
							else if(p.isOther())
								l.setBackground(new Color(255, 200, 0));
							
							//$FALL-THROUGH$
						case ALLY:
							if(p.isEpic()) {
								l.setPos(x-1, y);
								l.setSpan(2, 2);
								l.setSize(SQUARE_SIZE*2, SQUARE_SIZE*2);
							}
							
							l.setBorder(Color.black, 1);
							if(team != Team.PLAYER)
								l.setText("X");
							
							l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
							break;
						}
					}
					
					String plan = p.getPlanType();
					if(plan != null && p.isEmpty()) {
						l.setText(getShortenedUnitAndPlanType(plan));
						l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
					}
					
					gui.addLabel(l);
				}
			}
			
			gui.refresh();
		} catch (Exception e) {
			Logger.printException("MapGUI -> asGui: err=failed to load", e, Logger.runerr, Logger.error, v.cid, slot, true);
		}
	}
	
}
