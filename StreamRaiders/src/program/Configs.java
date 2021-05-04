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
	
	private static class All {
		private String con = null;
		public All(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	private static class Str extends All {
		public Str(String con) {
			super(con);
		}
	}
	
	public static final Str cookies = new Str("cookies");
	
	public static String getStr(String name, Str con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsString();
	}
	
	
	public static class Arr extends All {
		public Arr(String con) {
			super(con);
		}
	}
	
	public static final Arr locked = new Arr("locked");
	public static final Arr favs = new Arr("favs");
	public static final Arr stats = new Arr("stats");
	
	public static JsonArray getArr(String name, Arr con) {
		return configs.getAsJsonObject(name).getAsJsonArray(con.get());
	}
	
	
	public static class B extends All {
		public B(String con) {
			super(con);
		}
	}
	
	public static final B place = new B("place");
	public static final B upgrade = new B("upgrade");
	public static final B unlock = new B("unlock");
	public static final B dupe = new B("dupe");
	public static final B buy = new B("buy");
	
	public static boolean getUnitBoolean(String name, String uType, B con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.getAsJsonPrimitive(con.get())
				.getAsBoolean();
	}
	
	public static void setUnitBoolean(String name, String uType, B con, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), b);
	}
	
	
	private static class S extends All {
		public S(String con) {
			super(con);
		}
	}
	
	public static final S spec = new S("spec");
	
	public static String getUnitString(String name, String uType, S con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.getAsJsonPrimitive(con.get())
				.getAsString();
	}
	
	public static void setUnitString(String name, String uType, S con, String str) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), str);
	}
	
	public static boolean getChestBoolean(String name, String cType) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonPrimitive(cType)
				.getAsBoolean();
	}
	
	public static void setChestBoolean(String name, String cType, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.addProperty(cType, b);
	}
	
	
	private static class C extends All {
		public C(String con) {
			super(con);
		}
	}
	
	public static final C normChestLoyMax = new C("normChestLoyMax");
	public static final C loyChestLoyMin = new C("loyChestLoyMin");
	
	public static int getChestInt(String name, C con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonPrimitive(con.get())
				.getAsInt();
	}
	
	public static void setChestInt(String name, C con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.addProperty(con.get(), val);
	}
	
	
	public static boolean isSlotBlocked(String name, String slot) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("blockedSlots")
				.getAsJsonPrimitive(""+slot)
				.getAsBoolean();
	}
	
	public static void setSlotBlocked(String name, String slot, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("blockedSlots")
				.addProperty(""+slot, b);
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
