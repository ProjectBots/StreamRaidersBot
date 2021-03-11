package program;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import include.GUI;
import include.JsonParser;
import include.GUI.Label;

public class MapConv {
	
	public static JsonObject[][] load(String path) {
		File file = new File(path);
		
		FileReader r = null;
		BufferedReader br = null;
		
		JsonObject[][] ret = null;
		
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			String d = br.readLine();
			int x = Integer.parseInt(d.split("#")[0]);
			int y = Integer.parseInt(d.split("#")[1]);
			
			
			br.readLine();
			
			ret = new JsonObject[x][y];
			
			for(int i=0; i<x; i++) {
				for(int j=0; j<y; j++) {
					String line = br.readLine();
					String js = "";
					while(!line.equals("#begin#")) {
						js += line;
						line = br.readLine();
						if(line == null) break;
					}
					ret[i][j] = JsonParser.json(js);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				r.close();
			} catch (Exception e1) {}
		}
		
		return ret;
	}
	
	public static void save(String path, JsonObject[][] map) {
		System.out.println("saving...");
		String text = "" + map.length + "#" + map[0].length;

		Gson gb = new GsonBuilder().setPrettyPrinting().create();
		
		int z = 20;
		for(int i=0; i<map.length; i++) {
			for(int j=0; j<map[i].length; j++) {
				text += "\n#begin#\n";
				
				text += gb.toJson(map[i][j]);
				
			}
			double p = 100 * i / map.length;
			if(p >= z) {
				System.out.println((int) Math.floor(p) + "%");
				z += 20;
			}
			
		}
		System.out.println("100%");
		File file = new File(path);
		
		FileWriter w = null;
		BufferedWriter bw = null;
		
		try {
			try {
				String dirPath = path.substring(0, path.lastIndexOf("/"));
				
				File dir = new File(dirPath);
				
				if(!dir.exists()) {
					dir.mkdirs();
				}
			} catch(Exception e) {}
			
			
			file.createNewFile();
			w = new FileWriter(file);
			bw = new BufferedWriter(w);
			
			w.write(text);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				w.close();
			} catch (IOException e) {}
		}
		
		System.out.println("finished");
		
	}
	
	
	
	public static void asGui(JsonObject[][] map) {
		
		GUI gui = new GUI("Map", 600, 500);
		
		for(int i=0; i<map.length; i++) {
			for(int j=0; j<map[i].length; j++) {
				
				Label l = new Label();
				l.setText("");
				l.setSize(5, 5);
				l.setInsets(0, 0, 0, 0);
				l.setOpaque(true);
				
				l.setPos(i, j);
				
				if(map[i][j].getAsJsonPrimitive("occupied").getAsBoolean()) continue;
				
				switch(map[i][j].getAsJsonPrimitive("unit").getAsInt()) {
				case 0:
					l.setBackground(Color.white);
					if(map[i][j].getAsJsonPrimitive("playerRect").getAsBoolean()) {
						l.setBackground(new Color(0, 204, 255));
					}
					if(map[i][j].getAsJsonPrimitive("holdRect").getAsBoolean()) {
						l.setBackground(new Color(153, 0, 153));
					}
					break;
				case 1:
					String ct = map[i][j].getAsJsonObject("data").getAsJsonPrimitive("CharacterType").getAsString();
					if(ct.contains("epic") || ct.contains("captain")) {
						l.setPos(i-1, j);
						l.setSpan(2, 2);
						l.setSize(10, 10);
					}
					l.setBackground(Color.green);
					l.setBorder(Color.black, 1);
					break;
				case 2:
					l.setBackground(Color.red);
					l.setBorder(Color.black, 1);
					break;
				case 3:
					l.setBackground(Color.lightGray);
					break;
				}
				
				
				
				gui.addLabel(l);
				
				
				
			}
		}
		
		gui.refresh();
	}
	
	public static String asString(JsonObject[][] map) {
		String ret = "";
		for(int i=0; i<map[0].length; i++) {
			for(int j=0; j<map.length; j++) {
				String p = " ";
				if(map[j][i].getAsJsonPrimitive("playerRect").getAsBoolean()) p = "p";
				if(map[j][i].getAsJsonPrimitive("holdRect").getAsBoolean()) p = "h";
				
				switch(map[j][i].getAsJsonPrimitive("unit").getAsInt()) {
				case 1:
					p = "a";
					break;
				case 2:
					p = "f";
					break;
				case 3:
					p = "o";
				}
				
				if(map[j][i].getAsJsonPrimitive("occupied").getAsBoolean()) p = "b";
				ret += p + " ";
			}
			ret += "\n";
		}
		return ret;
	}
	
	public static String[][] asString2D(JsonObject[][] map) {
		String[][] ret = new String[map.length][map[0].length];
		for(int i=0; i<map[0].length; i++) {
			for(int j=0; j<map.length; j++) {
				String p = " ";
				if(map[j][i].getAsJsonPrimitive("playerRect").getAsBoolean()) p = "p";
				//if(map[j][i].getAsJsonPrimitive("enemyRect").getAsBoolean()) p = "e";
				if(map[j][i].getAsJsonPrimitive("holdRect").getAsBoolean()) p = "h";
				if(map[j][i].getAsJsonPrimitive("occupied").getAsBoolean()) p = "b";
				
				switch(map[j][i].getAsJsonPrimitive("unit").getAsInt()) {
				case 1:
					p = "a";
					break;
				case 2:
					p = "f";
					break;
				case 3:
					p = "o";
				}
				ret[j][i] = p;
			}
		}
		return ret;
	}
	
	
	
}
