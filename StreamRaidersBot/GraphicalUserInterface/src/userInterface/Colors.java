package userInterface;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI.Gradient;
import include.Json;
import include.NEF;
import program.Configs;

public class Colors {
	
	private static final String data = "data/Colors/";

	private static JsonObject def;
	private static JsonObject user_specified;
	
	public static void ini() {
		try {
			def = Json.parseObj(NEF.read(data+"default.json"));
			user_specified = Json.parseObj(NEF.read(data+Configs.getGStr(Configs.fontFile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Gradient getGradient(String id) {
		JsonObject tmp = getObj(id);
		JsonArray col1 = tmp.getAsJsonArray("col1");
		JsonArray col2 = tmp.getAsJsonArray("col2");
		JsonArray p1 = tmp.getAsJsonArray("p1");
		JsonArray p2 = tmp.getAsJsonArray("p2");
		return new Gradient(new Color(col1.get(0).getAsInt(), col1.get(1).getAsInt(), col1.get(2).getAsInt()),
							new Color(col2.get(0).getAsInt(), col2.get(1).getAsInt(), col2.get(2).getAsInt()),
							new Point(p1.get(0).getAsInt(), p1.get(1).getAsInt()),
							new Point(p2.get(0).getAsInt(), p2.get(1).getAsInt())
						);
	}
	
	public static Color getColor(String id) {
		JsonObject tmp = getObj(id);
		JsonArray col = tmp.getAsJsonArray("color");
		return new Color(col.get(0).getAsInt(), col.get(1).getAsInt(), col.get(2).getAsInt());
	}
	
	private static JsonObject getObj(String id) {
		String[] path = id.split(" ");
		JsonObject obj = user_specified;
		try {
			for(String p : path) {
				obj = obj.getAsJsonObject(p);
			}
		} catch (NullPointerException e) {
			obj = def;
			for(String p : path) {
				try {
					obj = obj.getAsJsonObject(p);
				} catch (NullPointerException e1) {
					throw new run.StreamRaidersException("Fonts -> getObj: err=couldn't get obj, id="+id + ", stuck="+p, e1, null, null);
				}
			}
		}
		return obj;
	}
	
	
	
}
