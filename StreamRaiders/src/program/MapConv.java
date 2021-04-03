package program;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.JsonParser;
import include.NEF;
import include.Pathfinding.Field;
import include.GUI.Label;

public class MapConv {
	
	public static Field[][] asField(Map map, boolean canFly, JsonArray allowedPlanTypes,  int[] h, int[][] banned) {
		
		int length = map.length();
		int width = map.width();
		
		Field[][] ret = new Field[width][length];
		
		boolean found = false;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<length; y++) {
				ret[x][y] = new Field();
				String pt = map.getPlanType(x, y);
				if(pt == null) pt = "Programmed by ProjectBots";
				if(map.is(x, y, SRC.Map.isObstacle)) {
					if(map.is(x, y, SRC.Map.canWalkOver)) continue;
					if(canFly && map.is(x, y, SRC.Map.canFlyOver)) continue;
					ret[x][y].setObstacle(true);
				} else if(allowedPlanTypes.contains(new JsonPrimitive(pt))) {
					setFin(ret, map, x, y, banned);
					found = true;
				}
			}
		}
		
		if(!found) 
			for(int x=0; x<width; x++) 
				for(int y=0; y<length; y++) 
					if(map.is(x, y, SRC.Map.isPlayerRect)) 
						setFin(ret, map, x, y, banned);
		
		ret[h[0]][h[1]].setHome();
		
		return ret;
	}
	
	private static void setFin(Field[][] ret, Map map, int x, int y, int[][] banned) {
		
		for(int i=0; i< banned.length; i++) 
			if(banned[i][0] == x && banned[i][1] == y) return;
		
		if(!map.is(x, y, SRC.Map.isEmpty)) return;
		
		for(int i=-1; i<2; i++) 
			for(int j=-1; j<2; j++) 
				if(map.is(x+i, y+j, SRC.Map.isEnemy)) return;
		
		ret[x][y].setFinish(true);
		
		return;
	}
	
	
	public static Map load(String map) {
		return new Map(JsonParser.jsonArr(map));
	}
	
	public static void save(String path, Map map) {
		NEF.save(path, JsonParser.prettyJson(map.getMap()));
	}
	
	
	public static void asGui(Map map) {
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		GUI gui = new GUI("Map", (int) Math.round(size.getWidth()), (int) Math.round(size.getHeight()));
		
		for(int x=0; x<map.width(); x++) {
			for(int y=0; y<map.length(); y++) {
				
				int s = 10;
				
				Label l = new Label();
				l.setCenter(true);
				l.setText("");
				l.setSize(s, s);
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
						l.setSize(s*2, s*2);
					}
					l.setBackground(Color.green);
					l.setBorder(Color.black, 1);
					if(!map.is(x, y, SRC.Map.isPlayer)) {
						l.setText("X");
						l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
					}
				}
				
				String plan = map.getPlanType(x, y);
				if(plan != null && map.is(x, y, SRC.Map.isEmpty)) {
					String c1 = plan.replace("assassin", "").toUpperCase().substring(0, 1);
					l.setText(c1);
					l.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
				}
				
				gui.addLabel(l);
			}
		}
		
		gui.refresh();
	}
	
}
