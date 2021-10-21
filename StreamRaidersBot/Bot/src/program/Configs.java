package program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.NEF;

public class Configs {

	private static final String path = "data/configs.json";
	private static final String bpath = "data/configs_.json";
	

	private static JsonObject configs = null;
	
	public static JsonObject getConfigs() {
		return configs;
	}
	
	
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
	public static final Str dungeonSlot = new Str("dungeonSlot");
	public static final Str canBuyChest = new Str("canBuyChest");
	public static final Str buyEventChest = new Str("buyEventChest");
	
	public static String getStr(String name, Str con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsString();
	}
	
	public static void setStr(String name, Str con, String val) {
		configs.getAsJsonObject(name).addProperty(con.get(), val);
	}
	
	
	private static class Int extends All {
		public Int(String con) {
			super(con);
		}
	}
	
	public static final Int maxPage = new Int("maxPage");
	public static final Int unitPlaceDelay = new Int("unitPlaceDelay");
	
	public static int getInt(String name, Int con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsInt();
	}
	
	public static void setInt(String name, Int con, int val) {
		configs.getAsJsonObject(name).addProperty(con.get(), val);
	}
	
	
	private static class Boo extends All {
		public Boo(String con) {
			super(con);
		}
	}
	
	public static final Boo canBuyScrolls = new Boo("canBuyScrolls");
	
	public static boolean getBoolean(String name, Boo con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsBoolean();
	}
	
	public static void setBoolean(String name, Boo con, boolean val) {
		configs.getAsJsonObject(name).addProperty(con.get(), val);
	}
	
	
	public static class Obj extends All {
		public Obj(String con) {
			super(con);
		}
	}
	
	public static final Obj favs = new Obj("favs");
	public static final Obj stats = new Obj("stats");
	public static final Obj caps = new Obj("caps");
	
	public static JsonObject getObj(String name, Obj con) {
		return configs.getAsJsonObject(name).getAsJsonObject(con.get());
	}
	
	public static void setObj(String name, Obj con, JsonObject val) {
		configs.getAsJsonObject(name).add(con.get(), val);
	}
	
	
	
	public static class B extends All {
		public B(String con) {
			super(con);
		}
	}
	
	public static final B place = new B("place");
	public static final B epic = new B("epic");
	public static final B upgrade = new B("upgrade");
	public static final B unlock = new B("unlock");
	public static final B dupe = new B("dupe");
	public static final B buy = new B("buy");
	
	public static int getUnitInt(String name, String uType, B con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.getAsJsonPrimitive(con.get())
				.getAsInt();
	}
	
	public static void setUnitInt(String name, String uType, B con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), val);
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
	
	
	private static class CB extends All {
		public CB(String con) {
			super(con);
		}
	}
	
	public static final CB enabled = new CB("enabled");
	
	public static boolean getChestBoolean(String name, String cType, CB con) {
		try {
			return configs.getAsJsonObject(name)
					.getAsJsonObject("chests")
					.getAsJsonObject(cType)
					.getAsJsonPrimitive(con.get())
					.getAsBoolean();
		} catch (NullPointerException e) {
			Debug.printException("Configs -> getChestBoolean: err=NullPointer cType=" + cType, e, Debug.runerr, Debug.error);
			throw new Run.SilentException();
		}
		
	}
	
	public static void setChestBoolean(String name, String cType, CB con, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), b);
	}
	
	
	private static class CI extends All {
		public CI(String con) {
			super(con);
		}
	}
	
	public static final CI minc = new CI("min");
	public static final CI maxc = new CI("max");
	
	public static int getChestInt(String name, String cType, CI con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.getAsJsonPrimitive(con.get())
				.getAsInt();
	}
	
	public static void setChestInt(String name, String cType, CI con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), val);
	}
	
	
	private static class T extends All {
		public T(String con) {
			super(con);
		}
	}
	
	public static final T max = new T("max");
	public static final T min = new T("min");
	
	public static int getTime(String name, T con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("time")
				.getAsJsonPrimitive(con.get()).getAsInt();
	}
	
	public static void setTime(String name, T con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("time")
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
	
	
	public static boolean isSlotLocked(String name, String slot) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("lockedSlots")
				.getAsJsonPrimitive(""+slot)
				.getAsBoolean();
	}
	
	public static void setSlotLocked(String name, String slot, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("lockedSlots")
				.addProperty(""+slot, b);
	}
	

	public static int getStoreRefreshInt(String name, int ind) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("storeRefresh")
				.getAsJsonPrimitive(""+ind)
				.getAsInt();
	}
	
	public static void setStoreRefreshInt(String name, int ind, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("storeRefresh")
				.addProperty(""+ind, val);
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
			JsonObject def = Json.parseObj(Options.get("defConfig"));
			try {
				configs = Json.parseObj(NEF.read(bpath));
				for(String key : configs.keySet())
					Json.check(configs.getAsJsonObject(key), def);
			} catch (IOException e) {
				try {
					configs = Json.parseObj(NEF.read(path));
					for(String key : configs.keySet())
						Json.check(configs.getAsJsonObject(key), def);
				} catch (FileNotFoundException e1) {
					configs = new JsonObject();
				}
			}
		}
	}
	
	private static final List<String> cookiesa = Arrays.asList("ACCESS_INFO scsession".split(" "));
	
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
			int index = 0;
			while(true) {
				index = sb.indexOf("=", index)+1;
				if(index == 0)
					break;
				sb.replace(index, sb.indexOf(";", index), "{hidden}");
			}
			Debug.print("Not enough cookies, got: "+sb.toString(), Debug.runerr, Debug.error, true);
			return;
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", sb.toString().substring(0, sb.length()-2));
		configs.add(name, Json.check(jo, Json.parseObj(Options.get("defConfig"))));
	}
	
	public static void add(String name, String cookies) {
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", cookies);
		configs.add(name, Json.check(jo, Json.parseObj(Options.get("defConfig"))));
	}
	
	
	public static void save() {
		try {
			NEF.save(path, Json.prettyJson(configs));
			File bc = new File(bpath);
			if(bc.exists())
				bc.delete();
		} catch (IOException e) {
			Debug.printException("Failed to save configs", e, Debug.runerr, Debug.error);
		}
	}
	
	public static void saveb() {
		try {
			NEF.save(bpath, Json.prettyJson(configs));
		} catch (IOException e) {
			Debug.printException("Failed to save configs", e, Debug.runerr, Debug.error);
		}
	}
	
	
	
	
	public static void conMerge(String name, JsonObject imp) {
		
		B[] bs = new B[] {buy, epic, dupe, unlock, upgrade, place};
		List<String> sbs = Arrays.asList("buy1 epic1 dupe1 unlock1 upgrade1 place1".split(" "));
		
		for(String key : imp.keySet()) {
			String[] keys = key.split("_");
			
			switch(keys[0]) {
			case "cookies":
				configs.getAsJsonObject(name).add("cookies", imp.get(key));
				break;
			case "unit":
				if(keys[2].equals("spec")) {
					setUnitString(name, keys[1], spec, imp.getAsJsonPrimitive(key).getAsString());
				} else if(keys[2].endsWith("1")) {
					setUnitInt(name, keys[1], bs[sbs.indexOf(keys[2])], imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "chests":
				if(keys[2].equals("enabled")) {
					setChestBoolean(name, keys[1], enabled, imp.getAsJsonPrimitive(key).getAsBoolean());
				} else {
					setChestInt(name, keys[1], keys[2].equals("min") ? minc : maxc, imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "blockedSlots":
				setSlotBlocked(name, keys[1], imp.getAsJsonPrimitive(key).getAsBoolean());
				break;
			case "lockedSlots":
				setSlotLocked(name, keys[1], imp.getAsJsonPrimitive(key).getAsBoolean());
				break;
			case "dungeonSlot":
				setStr(name, dungeonSlot, imp.getAsJsonPrimitive(key).getAsString());
				break;
			case "time":
				if(keys[1].equals("max")) {
					setTime(name, max, imp.getAsJsonPrimitive(key).getAsInt());
				} else {
					setTime(name, min, imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "maxPage":
				setInt(name, maxPage, imp.getAsJsonPrimitive(key).getAsInt());
				break;
			case "unitPlaceDelay":
				setInt(name, unitPlaceDelay, imp.getAsJsonPrimitive(key).getAsInt());
				break;
			case "canBuyChest":
				setStr(name, canBuyChest, imp.getAsJsonPrimitive(key).getAsString());
				break;
			case "canBuyScrolls":
				setBoolean(name, canBuyScrolls, imp.getAsJsonPrimitive(key).getAsBoolean());
				break;
			case "storeRefresh":
				setObj(name, new Obj("storeRefresh"), Json.parseObj(imp.getAsJsonPrimitive(key).getAsString()));
				break;
			case "favs":
				setObj(name, favs, Json.parseObj(imp.getAsJsonPrimitive(key).getAsString()));
				break;
			case "stats1":
				JsonObject stat = getObj(name, stats);
				JsonObject ostat = Json.parseObj(imp.getAsJsonPrimitive(key).getAsString());
				stat.addProperty("time", (long) stat.getAsJsonPrimitive("time").getAsLong() + ostat.getAsJsonPrimitive("time").getAsLong());
				
				JsonObject rews = stat.getAsJsonObject("rewards");
				JsonObject orews = ostat.getAsJsonObject("rewards");
				
				for(String rew : orews.keySet()) {
					JsonElement je = rews.get(rew);
					if(je != null) {
						rews.addProperty(rew, je.getAsInt() + orews.get(rew).getAsInt());
					} else {
						rews.add(rew, orews.get(rew).deepCopy());
					}
				}
				break;
			}
		}
		
		
		
	}
	
}
