package program;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.JsonParser;
import include.NEF;

public class Configs {

	private static final String path = "data\\configs.json";
	

	private static JsonObject configs = null;
	
	
	public static JsonObject getProfile(String name) {
		return configs.getAsJsonObject(name);
	}
	
	public static void remProfile(String name) {
		configs.remove(name);
	}
	
	public static class Str {
		private String con = null;
		public Str(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	public static final Str cookies = new Str("cookies");
	
	public static String getStr(String name, Str con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsString();
	}
	
	
	public static class Obj {
		private String con = null;
		public Obj(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	public static final Obj units = new Obj("units");
	public static final Obj specs = new Obj("specs");
	public static final Obj chests = new Obj("chests");
	public static final Obj clmm = new Obj("clmm");
	
	public static JsonObject getObj(String name, Obj con) {
		return configs.getAsJsonObject(name).getAsJsonObject(con.get());
	}
	
	
	public static class Arr {
		private String con = null;
		public Arr(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	public static final Arr locked = new Arr("locked");
	public static final Arr favs = new Arr("favs");
	public static final Arr stats = new Arr("stats");
	
	public static JsonArray getArr(String name, Arr con) {
		return configs.getAsJsonObject(name).getAsJsonArray(con.get());
	}
	
	
	public static Set<String> keySet() {
		return configs.keySet();
	}
	
	public static void load() throws IOException {
		load(false);
	}
	
	public static void load(boolean create) throws IOException {
		if(create) {
			configs = new JsonObject();
		} else {
			try {
				configs = JsonParser.parseObj(NEF.read(path));
				JsonObject def = JsonParser.parseObj(StreamRaiders.get("defConfig"));
				for(String key : configs.keySet())
					JsonParser.check(configs.getAsJsonObject(key), def);
			} catch (FileNotFoundException e) {
				configs = new JsonObject();
			}
		}
	}
	
	private static final List<String> cookiesa = Arrays.asList("ACCESS_INFO _ga scsession _gid".split(" "));
	
	public static void add(String name, JsonObject cookies) {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for(String key : cookies.keySet()) {
			if(Configs.cookiesa.contains(key)) {
				sb.append(key + "=" + cookies.getAsJsonPrimitive(key).getAsString() + "; ");
				c++;
			}
		}
		if(c != Configs.cookiesa.size()) {
			StreamRaiders.log("Not enough cookies, got: "+sb.toString(), null);
			return;
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", sb.toString().substring(0, sb.length()-2));
		configs.add(name, JsonParser.check(jo, JsonParser.parseObj(StreamRaiders.get("defConfig"))));
	}
	
	
	public static void save() {
		try {
			NEF.save(path, JsonParser.prettyJson(configs));
		} catch (IOException e) {
			StreamRaiders.log("Failed to save configs", e);
		}
	}
	
}
