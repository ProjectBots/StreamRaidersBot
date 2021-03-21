package program;


import java.awt.Color;

import include.GUI;
import include.JsonParser;
import include.Pathfinding.Field;
import include.GUI.Label;

public class MapConv {
	
	public static Field[][] asField(Map map, boolean canFly, int[] h, int[][] banned) {
		
		int length = map.length();
		int width = map.width();
		
		Field[][] ret = new Field[width][length];
		
		for(int x=0; x<width; x++) {
			loop:
			for(int y=0; y<length; y++) {
				ret[x][y] = new Field();
				if(map.is(x, y, SRC.Map.isObstacle)) {
					if(map.is(x, y, SRC.Map.canWalkOver)) continue;
					if(canFly && map.is(x, y, SRC.Map.canFlyOver)) continue;
					ret[x][y].setObstacle(true);
				} else if(map.is(x, y, SRC.Map.isPlayerRect)) {
					if(!map.is(x, y, SRC.Map.isEmpty)) continue;
					for(int i=-1; i<2; i++) {
						for(int j=-1; j<2; j++) {
							if(map.is(x+i, y+j, SRC.Map.isEnemy)) continue loop;
						}
					}
					for(int i=0; i< banned.length; i++) {
						if(banned[i][0] == x && banned[i][1] == y) continue loop;
					}
					ret[x][y].setFinish(true);
				}
			}
		}
		
		ret[h[0]][h[1]].setHome();
		
		return ret;
	}
	
	
	public static Map load(String map) {
		return new Map(JsonParser.jsonArr(map));
	}
	
	public static void save(String path, Map map) {
		NEF.save(path, JsonParser.prettyJson(map.getMap()));
	}
	
	
	public static void asGui(Map map) {
		
		GUI gui = new GUI("Map", 1000, 800);
		
		for(int x=0; x<map.width(); x++) {
			for(int y=0; y<map.length(); y++) {
				
				Label l = new Label();
				l.setText("");
				l.setSize(5, 5);
				l.setInsets(0, 0, 0, 0);
				l.setOpaque(true);
				l.setPos(x, y);
				l.setBackground(Color.white);
				
				if(map.is(x, y, SRC.Map.isOccupied)) continue;
				
				if(map.is(x, y, SRC.Map.isEnemyRect)) l.setBackground(new Color(255, 143, 143));
				if(map.is(x, y, SRC.Map.isPlayerRect)) l.setBackground(new Color(0, 204, 255));
				if(map.is(x, y, SRC.Map.isHoldRect)) l.setBackground(new Color(153, 0, 153));
				
				if(map.is(x, y, SRC.Map.isObstacle)) {
					l.setBackground(new Color(51, 51, 51));
					if(map.is(x, y, SRC.Map.canFlyOver)) l.setBackground(new Color(94, 94, 94));
					if(map.is(x, y, SRC.Map.canWalkOver)) l.setBackground(new Color(145, 145, 145));
				}

				if(map.is(x, y, SRC.Map.isEnemy)) {
					l.setBackground(Color.red);
					l.setBorder(Color.black, 1);
				}

				if(map.is(x, y, SRC.Map.isAllied)) {
					if(map.is(x, y, SRC.Map.isEpic)) {
						l.setPos(x-1, y);
						l.setSpan(2, 2);
						l.setSize(10, 10);
					}
					l.setBackground(Color.green);
					l.setBorder(Color.black, 1);
				}
				
				gui.addLabel(l);
				
			}
		}
		
		gui.refresh();
	}
	
}
