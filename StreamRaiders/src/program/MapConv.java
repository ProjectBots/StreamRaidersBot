package program;


import java.awt.Color;
import include.GUI;
import include.JsonParser;
import include.GUI.Label;

public class MapConv {
	
	public static Map load(String map) {
		return new Map(JsonParser.jsonArr(map));
	}
	
	public static void save(String path, Map map) {
		NEF.save(path, JsonParser.prettyJson(map.getMap()));
	}
	
	
	public static void asGui(Map map) {
		
		GUI gui = new GUI("Map", 600, 500);
		
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
