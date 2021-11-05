package program;

import java.util.Hashtable;

import com.google.gson.JsonObject;

import include.Json;

public class Remaper {

	//TODO include more stuff here
	
	private static Hashtable<String, String> maps = new Hashtable<>();
	
	public static void load() {
		JsonObject all = Json.parseObj(Options.get("remaps"));
		for(String key : all.keySet())
			maps.put(key, all.get(key).getAsString());
	}
	
	public static String map(String s) {
		if(s == null) return s;
		String ret = maps.get(s);
		return ret == null ? s : ret;
	}
	
	
}
