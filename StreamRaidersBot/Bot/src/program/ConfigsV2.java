package program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import include.Json;
import include.Maths;
import include.NEF;
import program.ConfigsV2.Exportable.Profile;
import program.ConfigsV2.Exportable.Profile.Layer;

public class ConfigsV2 {

	private static final String path = "data/configsV2.json";
	private static final String bpath = "data/configsV2_.json";
	

	private static JsonObject configs = null;
	
	
	public static JsonObject getProfile(String cid) {
		return configs.getAsJsonObject(cid);
	}
	
	public static void remProfile(String cid) {
		configs.remove(cid);
		for(String c : getCids())
			if(getPStr(c, synced).equals(cid))
				sync(c, null);
	}
	
	private static class All {
		private String con = null;
		public All(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
		@Override
		public boolean equals(Object obj) {
			return obj instanceof All 
				? ((All) obj).get().equals(con)
				: false;
		}
	}
	
	private static class GStr extends All {
		public GStr(String con) {
			super(con);
		}
	}

	public static final GStr fontFile = new GStr("fontFile");
	public static final GStr blocked_errors = new GStr("blocked_errors");
	
	public static String getGStr(GStr con) {
		return configs.getAsJsonObject("Global").get(con.get()).getAsString();
	}
	
	public static void setGStr(GStr con, String str) {
		configs.getAsJsonObject("Global").addProperty(con.get(), str);
	}
	
	
	
	private static class GBoo extends All {
		public GBoo(String con) {
			super(con);
		}
	}

	public static final GBoo useMemoryReleaser = new GBoo("useMemoryReleaser");
	public static final GBoo needCloseConfirm = new GBoo("needCloseConfirm");
	
	public static boolean getGBoo(GBoo con) {
		return configs.getAsJsonObject("Global").get(con.get()).getAsBoolean();
	}
	
	public static void setGBoo(GBoo con, boolean b) {
		configs.getAsJsonObject("Global").addProperty(con.get(), b);
	}
	
	
	private static class GInt extends All {
		public GInt(String con) {
			super(con);
		}
	}

	public static final GInt maxProfileActions = new GInt("maxProfileActions");
	
	public static int getGInt(GInt con) {
		return configs.getAsJsonObject("Global").get(con.get()).getAsInt();
	}
	
	public static void setGInt(GInt con, int x) {
		configs.getAsJsonObject("Global").addProperty(con.get(), x);
	}
	
	
	
	public static class PStr extends All {
		public PStr(String con) {
			super(con);
		}
	}
	
	public static final PStr cookies = new PStr("cookies");
	public static final PStr pname = new PStr("name");
	public static final PStr synced = new PStr("synced");
	
	public static String getPStr(String cid, PStr con) {
		return configs.getAsJsonObject(cid).get(con.get()).getAsString();
	}
	
	public static void setPStr(String cid, PStr con, String val) {
		configs.getAsJsonObject(cid).addProperty(con.get(), val);
	}
	
	
	
	public static class PObj extends All {
		public PObj(String con) {
			super(con);
		}
	}
	
	public static final PObj stats = new PObj("stats");
	public static final PObj ptimes = new PObj("times");
	
	public static JsonObject getPObj(String cid, PObj con) {
		return configs.getAsJsonObject(cid).getAsJsonObject(con.get());
	}
	
	public static void setPObj(String cid, PObj con, JsonObject val) {
		configs.getAsJsonObject(cid).add(con.get(), val);
	}
	
	
	
	public static String[] getLayerIds(String cid) {
		
		Set<String> set = configs.getAsJsonObject(cid)
				.getAsJsonObject("layers")
				.keySet();
		
		return set.toArray(new String[set.size()]);
	}
	
	private static JsonObject getLayer(String cid, String lay) {
		return configs.getAsJsonObject(cid)
				.getAsJsonObject("layers")
				.getAsJsonObject(lay);
	}
	
	public static void addLayer(String cid, String lay, String name, String clay) {
		configs.getAsJsonObject(cid)
				.getAsJsonObject("layers")
				.add(lay, getLayer(cid, clay)
					.deepCopy());
		setStr(cid, lay, lname, name);
	}
	
	public static void remLayer(String cid, String lay) {
		configs.getAsJsonObject(cid)
				.getAsJsonObject("layers")
				.remove(lay);
	}
	
	
	
	public static class Str extends All {
		public Str(String con) {
			super(con);
		}
	}
	
	public static final Str dungeonSlot = new Str("dungeonSlot");
	public static final Str lname = new Str("name");
	//public static final Str canBuyChest = new Str("canBuyChest");
	//public static final Str canBuyEventChest = new Str("canBuyEventChest");
	public static final Str userAgent = new Str("userAgent");
	public static final Str proxyDomain = new Str("proxyDomain");
	public static final Str proxyUser = new Str("proxyUser");
	public static final Str proxyPass = new Str("proxyPass");
	
	public static String getStr(String cid, String lay, Str con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			String sel = getStr(cid, lays[0], con);
			for(int i=1; i<lays.length; i++)
				if(!sel.equals(getStr(cid, lays[i], con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.get(con.get())
				.getAsString();
	}
	
	public static void setStr(String cid, String lay, Str con, String val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setStr(cid, l, con, val);
			return;
		}
		getLayer(cid, lay)
				.addProperty(con.get(), val);
	}
	
	
	
	public static class Int extends All {
		public Int(String con) {
			super(con);
		}
	}
	
	public static final Int unitPlaceRetries = new Int("unitPlaceRetries");
	public static final Int mapReloadAfterXRetries = new Int("mapReloadAfterXRetries");
	public static final Int maxUnitPerRaid = new Int("maxUnitPerRaid");
	public static final Int capInactiveTreshold = new Int("capInactiveTreshold");
	public static final Int storeMinKeys = new Int("storeMinKeys");
	public static final Int storeMinBones = new Int("storeMinBones");
	public static final Int scrollsMinGold = new Int("scrollsMinGold");
	public static final Int upgradeMinGold = new Int("upgradeMinGold");
	public static final Int unlockMinGold = new Int("unlockMinGold");
	public static final Int color = new Int("color");
	public static final Int unitUpdate = new Int("unitUpdate");
	public static final Int raidUpdate = new Int("raidUpdate");
	public static final Int mapUpdate = new Int("mapUpdate");
	public static final Int storeUpdate = new Int("storeUpdate");
	public static final Int skinUpdate = new Int("skinUpdate");
	public static final Int questEventRewardsUpdate = new Int("questEventRewardsUpdate");
	public static final Int capsUpdate = new Int("capsUpdate");
	public static final Int proxyPort = new Int("proxyPort");
	
	
	public static Integer getInt(String cid, String lay, Int con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getInt(cid, lays[0], con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getInt(cid, lays[i], con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay).get(con.get()).getAsInt();
	}
	
	public static void setInt(String cid, String lay, Int con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setInt(cid, l, con, val);
			return;
		}
		getLayer(cid, lay).addProperty(con.get(), val);
	}
	
	
	
	public static class Boo extends All {
		public Boo(String con) {
			super(con);
		}
	}
	

	public static final Boo useMultiPlaceExploit = new Boo("useMultiPlaceExploit");
	public static final Boo useMultiQuestExploit = new Boo("useMultiQuestExploit");
	public static final Boo useMultiUnitExploit = new Boo("useMultiUnitExploit");
	public static final Boo useMultiChestExploit = new Boo("useMultiChestExploit");
	public static final Boo useMultiEventExploit = new Boo("useMultiEventExploit");
	public static final Boo preferRoguesOnTreasureMaps = new Boo("preferRoguesOnTreasureMaps");
	public static final Boo allowPlaceFirst = new Boo("allowPlaceFirst");
	public static final Boo proxyMandatory = new Boo("proxyMandatory");
	
	public static Boolean getBoolean(String cid, String lay, Boo con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			boolean sel = getBoolean(cid, lays[0], con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getBoolean(cid, lays[i], con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay).get(con.get()).getAsBoolean();
	}
	
	public static void setBoolean(String cid, String lay, Boo con, boolean val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setBoolean(cid, l, con, val);
			return;
		}
		getLayer(cid, lay).addProperty(con.get(), val);
	}
	
	
	
	public static class UniInt extends All {
		public UniInt(String con) {
			super(con);
		}
	}
	
	public static final UniInt place = new UniInt("place");
	public static final UniInt epic = new UniInt("epic");
	public static final UniInt placedun = new UniInt("placedun");
	public static final UniInt epicdun = new UniInt("epicdun");
	public static final UniInt upgrade = new UniInt("upgrade");
	public static final UniInt unlock = new UniInt("unlock");
	public static final UniInt dupe = new UniInt("dupe");
	public static final UniInt buy = new UniInt("buy");
	
	public static Integer getUnitInt(String cid, String lay, String uType, UniInt con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getUnitInt(cid, lays[0], uType, con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getUnitInt(cid, lays[i], uType, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.get(con.get()).getAsInt();
	}
	
	public static void setUnitInt(String cid, String lay, String uType, UniInt con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setUnitInt(cid, l, uType, con, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), val);
	}
	
	
	public static class UniStr extends All {
		public UniStr(String con) {
			super(con);
		}
	}
	
	public static final UniStr spec = new UniStr("spec");
	public static final UniStr chests = new UniStr("chests");
	public static final UniStr favOnly = new UniStr("favOnly");
	public static final UniStr markerOnly = new UniStr("markerOnly");
	public static final UniStr canVibe = new UniStr("canVibe");
	
	public static String getUnitString(String cid, String lay, String uType, UniStr con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			String sel = getUnitString(cid, lays[0], uType, con);
			String ret = null;
			for(int i=1; i<lays.length; i++) {
				String str = getUnitString(cid, lays[i], uType, con);
				if(ret == null) {
					if(!sel.equals(str))
						ret = sel + "::" + str;
				} else {
					if(!ret.contains(str)) 
						ret += "::" + str;
				}
			}
			return ret == null ? sel : ret;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.get(con.get()).getAsString();
	}
	
	public static void setUnitString(String cid, String lay, String uType, UniStr con, String str) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setUnitString(cid, l, uType, con, str);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), str);
	}
	
	
	
	private static class CheBoo extends All {
		public CheBoo(String con) {
			super(con);
		}
	}
	
	public static final CheBoo enabled = new CheBoo("enabled");
	
	public static Boolean getChestBoolean(String cid, String lay, String cType, CheBoo con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			boolean sel = getChestBoolean(cid, lays[0], cType, con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getChestBoolean(cid, lays[i], cType, con)))
					return null;
			
			return sel;
		}
		try {
			return getLayer(cid, lay)
					.getAsJsonObject("chests")
					.getAsJsonObject(cType)
					.get(con.get()).getAsBoolean();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static void setChestBoolean(String cid, String lay, String cType, CheBoo con, boolean b) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setChestBoolean(cid, l, cType, con, b);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), b);
	}
	
	
	private static class CheInt extends All {
		public CheInt(String con) {
			super(con);
		}
	}
	
	public static final CheInt minLoy = new CheInt("minLoy");
	public static final CheInt maxLoy = new CheInt("maxLoy");
	public static final CheInt minTime = new CheInt("minTime");
	public static final CheInt maxTime = new CheInt("maxTime");
	
	public static Integer getChestInt(String cid, String lay, String cType, CheInt con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getChestInt(cid, lays[0], cType, con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getChestInt(cid, lays[i], cType, con)))
					return null;
			
			return sel;
		}
		try {
			return getLayer(cid, lay)
					.getAsJsonObject("chests")
					.getAsJsonObject(cType)
					.get(con.get()).getAsInt();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static void setChestInt(String cid, String lay, String cType, CheInt con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setChestInt(cid, l, cType, con, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), val);
	}
	
	
	public static class SleInt extends All {
		public SleInt(String con) {
			super(con);
		}
	}
	
	public static final SleInt max = new SleInt("max");
	public static final SleInt min = new SleInt("min");
	
	public static Integer getSleep(String cid, String lay, String slot, SleInt con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getSleep(cid, lays[0], slot, con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getSleep(cid, lays[i], slot, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("sleep")
				.getAsJsonObject(slot)
				.get(con.get()).getAsInt();
	}
	
	public static void setSleep(String cid, String lay, String slot, SleInt con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setSleep(cid, l, slot, con, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("sleep")
				.getAsJsonObject(slot)
				.addProperty(con.get(), val);
	}
	
	
	
	private static class UPDInt extends All {
		public UPDInt(String con) {
			super(con);
		}
	}
	
	public static final UPDInt maxu = new UPDInt("max");
	public static final UPDInt minu = new UPDInt("min");
	
	public static Integer getUnitPlaceDelayInt(String cid, String lay, UPDInt con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			Integer sel = getUnitPlaceDelayInt(cid, lays[0], con);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getUnitPlaceDelayInt(cid, lays[i], con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("unitPlaceDelay")
				.get(con.get()).getAsInt();
	}
	
	public static void setUnitPlaceDelayInt(String cid, String lay, UPDInt con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setUnitPlaceDelayInt(cid, l, con, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("unitPlaceDelay")
				.addProperty(con.get(), val);
	}
	
	
	public static class ListType extends All {
		public ListType(String con) {
			super(con);
		}
	}
	
	public static final ListType campaign = new ListType("campaign");
	public static final ListType dungeon = new ListType("dungeon");
	public static final ListType all = new ListType("all");
	
	private static JsonObject getList(String cid, String lay, ListType con) {
		return getLayer(cid, lay)
				.getAsJsonObject("caps")
				.getAsJsonObject(con.get());
	}
	
	
	/*
	 * >0					: fav
	 * <0					: block
	 * null					: remove
	 */
	public static void favCap(String cid, String lay, String cap, ListType list, Integer val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				favCap(cid, l, cap, list, val);
			return;
		}
		if(list.equals(all)) {
			favCap(cid, lay, cap, campaign, val);
			favCap(cid, lay, cap, dungeon, val);
			return;
		} 
		
		JsonObject caps = getList(cid, lay, list);
		if(val == null) {
			caps.remove(cap);
		} else {
			if(caps.has(cap) && sign(getCapInt(cid, lay, cap, list, fav)) * sign(val) > 0)
				return;
			JsonObject jo = ccap.deepCopy();
			jo.addProperty("fav", val);
			caps.add(cap, jo);
		}
		
	}
	
	public static HashSet<String> getFavCaps(String cid, String lay, ListType list) {
		HashSet<String> ret = new HashSet<>();
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays) {
				HashSet<String> caps = getFavCaps(cid, l, list);
				for(String c : caps)
					ret.add(c);
			}
				
			return ret;
		}
		if(list.equals(all)) {
			ret = getFavCaps(cid, lay, campaign);
			HashSet<String> caps = getFavCaps(cid, lay, dungeon);
			for(String c : caps)
				ret.add(c);
			
			return ret;
		}
		JsonObject caps = getList(cid, lay, list);
		for(String key : caps.keySet())
			ret.add(key);
		
		return ret;
	}
	
	
	public static class CapBoo extends All {
		public CapBoo(String con) {
			super(con);
		}
	}
	
	public static final CapBoo ic = new CapBoo("ic");
	public static final CapBoo il = new CapBoo("il");
	
	
	public static Boolean getCapBoo(String cid, String lay, String cap, ListType list, CapBoo con) {
		Integer val = getCapBooTend(cid, lay, cap, list, con);
		return val == null || val*val < 2
				? null
				: val > 0;
	}
	
	/*
	 * -2	: false
	 * -1	: false and null
	 * 0	: true, false and null
	 * 1	: true and null
	 * 2	: true
	 * null	: cap isn't listed
	 */
	public static Integer getCapBooTend(String cid, String lay, String cap, ListType list, CapBoo con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			Integer sel = getCapBooTend(cid, lays[0], cap, list, con);
			boolean same = true;
			for(int i=1; i<lays.length; i++) {
				Integer val = getCapBooTend(cid, lays[i], cap, list, con);
				if(sel == null) {
					sel = val;
					same = false;
					continue;
				}
				if(val == null) {
					same = false;
					continue;
				}
				if(sel * val <= 0)
					return 0;
			}
			if(sel == null)
				return null;
			return same 
					? sel 
					: sel > 0 
						? 1 
						: -1;
		}
		if(list.equals(all)) {
			Integer b1 = getCapBooTend(cid, lay, cap, campaign, con);
			Integer b2 = getCapBooTend(cid, lay, cap, dungeon, con);
			if(b1 == b2)
				return b1;
			if(b1 == null)
				return ((b2*b2)-2)/b2;
			if(b2 == null)
				return ((b1*b1)-2)/b1;
			
			return 0;
		}
		JsonObject caps = getList(cid, lay, list);
		if(!caps.has(cap))
			return null;
		return caps.getAsJsonObject(cap)
			.get(con.get())
			.getAsBoolean()
				? 2
				: -2;
	}
	
	public static void setCapBoo(String cid, String lay, String cap, ListType list, CapBoo con, boolean val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setCapBoo(cid, l, cap, list, con, val);
			return;
		}
		if(list.equals(all)) {
			setCapBoo(cid, lay, cap, campaign, con, val);
			setCapBoo(cid, lay, cap, dungeon, con, val);
			return;
		}
		JsonObject caps = getList(cid, lay, list);
		if(!caps.has(cap))
			favCap(cid, lay, cap, list, 1);
		caps.getAsJsonObject(cap)
			.addProperty(con.get(), val);
	}
	
	
	private static class CapInt extends All {
		public CapInt(String con) {
			super(con);
		}
	}
	
	public static final CapInt fav = new CapInt("fav");
	
	
	/*
	 * Integer.MIN_VALUE	: only negative
	 * Integer.MIN_VALUE+1	: negative and null
	 * 0					: negative, positive and null
	 * Integer.MAX_VALUE-1	: positive and null
	 * Integer.MAX_VALUE	: only positve
	 * null					: no value
	 * any other			: same value
	 */
	public static Integer getCapInt(String cid, String lay, String cap, ListType list, CapInt con) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			Integer sel = getCapInt(cid, lays[0], cap, list, con);
			boolean same = true;
			boolean conNull = false;
			for(int i=1; i<lays.length; i++) {
				Integer c = getCapInt(cid, lays[i], cap, list, con);
				if(sel == null) {
					conNull = true;
					sel = c;
					continue;
				}
				if(c == null) {
					conNull = true;
					continue;
				}
				if(sel == Integer.MAX_VALUE-1 
						|| sel == Integer.MIN_VALUE+1 
						|| c == Integer.MAX_VALUE-1 
						|| c == Integer.MIN_VALUE+1)
					conNull = true;
				if(sign(sel) * sign(c) <= 0)
					return 0;
				if(sel != c)
					same = false;
			}
			
			return sel == null 
					? null
					: conNull
						? sel > 0
							? Integer.MAX_VALUE-1
							: Integer.MIN_VALUE+1
						: same 
							? sel 
							: sel > 0 
								? Integer.MAX_VALUE
								: Integer.MIN_VALUE;
		}
		if(list.equals(all)) {
			Integer i1 = getCapInt(cid, lay, cap, campaign, con);
			Integer i2 = getCapInt(cid, lay, cap, dungeon, con);
			if(i1 == i2)
				return i1;
			if(i1 == null || i2 == null)
				return (i2 == null ? i1 : i2) > 0
							? Integer.MAX_VALUE-1
							: Integer.MIN_VALUE+1;
			return sign(i1)*sign(i2) > 0 
						? i1 > 0 
							? Integer.MAX_VALUE 
							: Integer.MIN_VALUE
						: 0;
		}
		JsonObject caps = getList(cid, lay, list);
		if(!caps.has(cap))
			return null;
		try {
			return caps.getAsJsonObject(cap)
					.get(con.get())
					.getAsInt();
		} catch (UnsupportedOperationException | NullPointerException e) {
			e.printStackTrace();
			System.out.println(Json.prettyJson(caps));
			return null;
		}
		
	}
	
	public static void setCapInt(String cid, String lay, String cap, ListType list, CapInt con, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setCapInt(cid, l, cap, list, con, val);
			return;
		}
		if(list.equals(all)) {
			setCapInt(cid, lay, cap, campaign, con, val);
			setCapInt(cid, lay, cap, dungeon, con, val);
			return;
		}
		JsonObject caps = getList(cid, lay, list);
		if(!caps.has(cap))
			favCap(cid, lay, cap, list, val);
		else
			caps.getAsJsonObject(cap)
				.addProperty(con.get(), val);
	}
	
	
	
	public static Boolean isSlotLocked(String cid, String lay, String slot) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			boolean sel = isSlotLocked(cid, lays[0], slot);
			for(int i=1; i<lays.length; i++)
				if(!(sel == isSlotLocked(cid, lays[i], slot)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("lockedSlots")
				.get(""+slot).getAsBoolean();
	}
	
	public static void setSlotLocked(String cid, String lay, String slot, boolean b) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setSlotLocked(cid, l, slot, b);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("lockedSlots")
				.addProperty(""+slot, b);
	}
	

	public static Integer getStoreRefreshInt(String cid, String lay, int ind) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getStoreRefreshInt(cid, lays[0], ind);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getStoreRefreshInt(cid, lays[i], ind)))
					return null;
			
			return sel;
		}
		return getLayer(cid, lay)
				.getAsJsonObject("storeRefresh")
				.get(""+ind).getAsInt();
	}
	
	public static void setStoreRefreshInt(String cid, String lay, int ind, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setStoreRefreshInt(cid, l, ind, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("storeRefresh")
				.addProperty(""+ind, val);
	}
	
	
	public static class StorePrioType extends All {
		public StorePrioType(String con) {
			super(con);
		}
	}
	
	public static final StorePrioType keys = new StorePrioType("Key");
	public static final StorePrioType bones = new StorePrioType("Bone");
	public static final StorePrioType event = new StorePrioType("Event");

	//TODO
	public static HashSet<String> getStorePrioList(String cid, String lay, StorePrioType spt) {
		HashSet<String> ret = new HashSet<>();
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(int i=0; i<lays.length; i++)
				ret.addAll(getStorePrioList(cid, lays[i], spt));
		} else 
			ret.addAll(getLayer(cid, lay).getAsJsonObject("store"+spt.get()+"Prios").keySet());
		return ret;
	}
	
	public static Integer getStorePrioInt(String cid, String lay, StorePrioType spt, String type) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			int sel = getStorePrioInt(cid, lays[0], spt, type);
			for(int i=1; i<lays.length; i++)
				if(!(sel == getStorePrioInt(cid, lays[i], spt, type)))
					return null;
			return sel;
		}
		JsonObject sp = getLayer(cid, lay)
				.getAsJsonObject("store"+spt.get()+"Prios");
		
		return sp.has(type)
				? sp.get(type).getAsInt()
				: -1;
	}
	
	public static void setStorePrioInt(String cid, String lay, StorePrioType spt, String type, int val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				setStorePrioInt(cid, l, spt, type, val);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("store"+spt.get()+"Prios")
				.addProperty(type, val);
	}
	
	public static void remStorePrioInt(String cid, String lay, StorePrioType spt, String type) {
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays)
				remStorePrioInt(cid, l, spt, type);
			return;
		}
		getLayer(cid, lay)
				.getAsJsonObject("store"+spt.get()+"Prios")
				.remove(type);
	}
	
	
	public static List<String> getCids() {
		List<String> cids = new ArrayList<>(configs.keySet());
		cids.remove("Global");
		return cids;
	}
	
	public static void load() throws IOException {
		load(false);
	}
	
	public static void load(boolean create) throws IOException {
		if(create) {
			configs = Json.parseObj(Options.get("cglobal"));
		} else {
			try {
				configs = Json.parseObj(NEF.read(bpath));
			} catch (IOException e) {
				try {
					configs = Json.parseObj(NEF.read(path));
				} catch (FileNotFoundException e1) {
					configs = new JsonObject();
				}
			}
			
			checkAll();
			
			for(String key : getCids()) {
				String sync = getPStr(key, synced);
				if(sync.equals("(none)"))
					continue;
				sync(key, sync);
			}
		}
	}
	
	public static String add(String name, String access_info) {
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", "ACCESS_INFO="+access_info);
		jo.addProperty("name", name);
		String key = ""+LocalDateTime.now().toString().hashCode();
		configs.add(key, jo);
		check(key);
		return key;
	}
	
	
	public static boolean isPNameTaken(String name) {
		if(name.startsWith("(") && name.endsWith(")"))
			return true;
		List<String> taken = new ArrayList<String>();
		for(String cid : getCids())
			taken.add(getPStr(cid, pname));
		taken.add("Global");
		return taken.contains(name);
	}
	
	public static boolean isLNameTaken(String cid, String name) {
		if(name.startsWith("(") && name.endsWith(")"))
			return true;
		List<String> taken = new ArrayList<String>();
		for(String lid : getLayerIds(cid))
			taken.add(getStr(cid, lid, lname));
		return taken.contains(name);
	}
	
	private static JsonObject cglobal = Json.parseObj(Options.get("cglobal"));
	private static JsonObject cmain = Json.parseObj(Options.get("cmain"));
	private static JsonObject cmainspare = Json.parseObj(Options.get("cmainspare"));
	private static JsonObject clayer = Json.parseObj(Options.get("clayer"));
	private static JsonObject clayerspare = Json.parseObj(Options.get("clayerspare"));
	private static JsonObject ccap = Json.parseObj(Options.get("ccap"));
	
	
	public static void checkAll() {
		if(!configs.has("Global"))
			configs.add("Global", new JsonObject());
		Json.check(configs.getAsJsonObject("Global"), cglobal.deepCopy(), true, null);
		
		for(String key : getCids()) 
			check(key);
	}
	
	public static void check(String cid) {
		JsonObject main = configs.getAsJsonObject(cid);
		Json.check(main, cmain.deepCopy(), true, cmainspare.deepCopy());
		JsonObject layers = main.getAsJsonObject("layers");
		for(String lay : layers.keySet()) {
			JsonObject layer = layers.getAsJsonObject(lay);
			Json.check(layer, clayer.deepCopy(), true, clayerspare.deepCopy());
			JsonObject lists = layer.getAsJsonObject("caps");
			for(String l : lists.keySet()) {
				JsonObject caps = lists.getAsJsonObject(l);
				for(String c : caps.keySet()) {
					JsonObject cap = caps.getAsJsonObject(c);
					Json.check(cap, ccap.deepCopy());
				}
			}
		}
		JsonObject times = main.getAsJsonObject("times");
		String[] nts = new String[2016];
		Integer s;
		int e;
		String[] lids = getLayerIds(cid);
		for(String key : times.keySet()) {
			String lid = times.get(key).getAsString();
			if(!ArrayUtils.contains(lids, lid))
				continue;
			String[] time = key.split("-");
			s = Integer.parseInt(time[0]);
			e = Integer.parseInt(time[1]);
			for(; s<=e; s++)
				nts[s] = lid;
		}
		
		times = new JsonObject();
		
		for(int i=0; i<2016; i++)
			if(nts[i] == null)
				nts[i] = "(default)";
		
		for(int i=0; i<2016; i++) {
			s = i;
			String lid = nts[i];
			for(i++;i<2016;i++)
				if(!lid.equals(nts[i]))
					break;
			
			times.addProperty(s+"-"+--i, lid);
		}
		
		
		main.add("times", times);
	}
	
	
	public static void sync(String cid, String defCid) {
		JsonObject toSync = getProfile(cid);
		if(defCid == null) {
			JsonObject def = getProfile(getPStr(cid, synced));
			setPStr(cid, synced, "(none)");
			toSync.add("layers", def.get("layers").deepCopy());
			toSync.add("times", def.get("times").deepCopy());
		} else {
			JsonObject def = getProfile(defCid);
			setPStr(cid, synced, defCid);
			toSync.add("layers", def.get("layers"));
			toSync.add("times", def.get("times"));
		}
	}
	
	public static void save() {
		try {
			NEF.save(path, Json.prettyJson(configs));
			File bc = new File(bpath);
			if(bc.exists())
				bc.delete();
		} catch (IOException e) {
			Debug.print("Failed to save configs", Debug.runerr, Debug.error, null, null, true);
		}
	}
	
	public static void saveb() {
		try {
			NEF.save(bpath, Json.prettyJson(configs));
		} catch (IOException e) {
			Debug.print("Failed to save configs", Debug.runerr, Debug.error, null, null, true);
		}
	}
	public static class ConfigTypes {
		public static enum ConfigClasses {
			GStr, GBoo, GInt, PStr, PObj, Str, Int, Boo, UniInt, UniStr, CheInt, CheBoo, SleInt, UPDInt, ListType, CapInt, CapBoo, StorePrioType
		}
		public static final Hashtable<String, List<String>> all = new Hashtable<String, List<String>>() {
			private static final long serialVersionUID = 1L;
			{
				//	initialize Lists
				put("Global", new ArrayList<>());
				put("Profile", new ArrayList<>());
				put("Layer", Arrays.asList("Simple Unit Chest Sleep PlaceDelay CaptainList Captain".split(" ")));
				put("Simple", new ArrayList<>());
				put("Unit", new ArrayList<>());
				put("Chest", new ArrayList<>());
				put("Sleep", new ArrayList<>());
				put("PlaceDelay", new ArrayList<>());
				put("CaptainList", new ArrayList<>());
				put("Captain", new ArrayList<>());
				
				//	get configs via reflection and adds them to their list
				Field[] fields = ConfigsV2.class.getFields();
				for(Field f : fields) {
					String classname = f.getType().getSimpleName();
					try {
						All a = (All) f.get(new ConfigsV2());
						String con = a.get();
						if(con.endsWith("Exploit"))
							continue;
						switch(ConfigClasses.valueOf(classname)) {
						case StorePrioType:
							continue;
						case GStr:
						case GBoo:
						case GInt:
							get("Global").add(con);
							continue;
						case PStr:
						case PObj:
							if(ArrayUtils.contains("name synced".split(" "), con))
								continue;
							get("Profile").add(con);
							continue;
						case Boo:
						case Str:
						case Int:
							if(ArrayUtils.contains("color name".split(" "), con))
								continue;
							get("Simple").add(con);
							continue;
						case UniInt:
						case UniStr:
							get("Unit").add(con);
							continue;
						case CheBoo:
						case CheInt:
							get("Chest").add(con);
							continue;
						case SleInt:
							get("Sleep").add(con);
							continue;
						case UPDInt:
							get("PlaceDelay").add(con);
							continue;
						case ListType:
							if(ArrayUtils.contains("all".split(" "), con))
								continue;
							get("CaptainList").add(con);
							continue;
						case CapInt:
						case CapBoo:
							if(ArrayUtils.contains("fav".split(" "), con))
								continue;
							get("Captain").add(con);
							continue;
						}
					} catch(Exception e) {
						Debug.printException("ConfigsV2 -> Exportable -> configsAll: err=didnt catched, class="+classname+", field="+f.getName(), e, Debug.runerr, Debug.error, null, null, true);
					}
				} 
			}
		};
	}
	
	
	public static class Exportable {
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Exportable:");
			if(gconfs.size() > 0) {
				sb.append("\nGlobal:");
				for(String gconf : gconfs)
					sb.append("\n   "+gconf);
			}
			for(Profile p : list)
				p.toSB(sb);
			return sb.toString();
		}
		
		public static class Profile {
			public void toSB(StringBuilder in) {
				in.append("\n"+getPStr(cid, pname)+":");
				if(pconfs.size() > 0) {
					in.append("\n   pconfs:");
					for(String pconf : pconfs)
						in.append("\n      "+pconf);
				}
				for(Layer l : lays)
					l.toSB(cid, in);
			}
			public static class Layer {
				public void toSB(String cid, StringBuilder in) {
					in.append("\n   "+getStr(cid, lid, lname)+":");
					for(String t : items.keySet()) {
						in.append("\n      "+t);
						for(String c : items.get(t))
							in.append("\n         "+c);
					}
						
				}
				private String lid;
				private Hashtable<String, List<String>> items = new Hashtable<>();
				public Layer(String lid) {//TODO
					this.lid = lid;
				}
				public void add(String list, String conf) {
					if(!items.containsKey(list))
						items.put(list, new ArrayList<>());
					items.get(list).add(conf);
				}
				public Hashtable<String, List<String>> getItems() {
					return items;
				}
				public String getLid() {
					return lid;
				}
			}
			private String cid;
			public Profile(String cid) {
				this.cid = cid;
			}
			public String getCid() {
				return cid;
			}
			
			private List<Layer> lays = new ArrayList<>();
			public void add(Layer lay) {
				lays.add(lay);
			}
			public List<Layer> getLayers() {
				return lays;
			}
			
			private List<String> pconfs = new ArrayList<>();
			public void add(String pconf) {
				pconfs.add(pconf);
			}
			public List<String> getPConfs() {
				return pconfs;
			}
		}
		
		private String path;
		public Exportable(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
		
		private List<Profile> list = new ArrayList<>();
		public void add(Profile pro) {
			list.add(pro);
		}
		public List<Profile> getList() {
			return list;
		}
		
		
		private List<String> gconfs = new ArrayList<>();
		public void add(String gconf) {
			gconfs.add(gconf);
		}
		public List<String> getGConfs() {
			return gconfs;
		}
	}
	
	public static void exportConfig(Exportable ex) throws IOException {
		JsonObject res = new JsonObject();
		res.addProperty("version", 1);
		//TODO
		List<String> gconfs = ex.getGConfs();
		for(String c : gconfs)
			Json.set(res, "Global "+c, Json.get(configs, "Global "+c));
		
		List<Profile> pros = ex.getList();
		for(Profile p : pros) {
			String pp = p.getCid()+" ";
			List<String> pconfs = p.getPConfs();
			pconfs.add("name");
			for(String s : pconfs)
				Json.set(res, pp+s, Json.get(configs, pp+s));
			
			List<Layer> lays = p.getLayers();
			for(Layer l : lays) {
				
				
				String pl = pp+"layers "+l.getLid()+" ";
				Hashtable<String, List<String>> lconfs = l.getItems();
				
				List<String> i = lconfs.get("Simple");
				if(i == null)
					i = new ArrayList<>();
				i.add("name");
				i.add("color");
				for(String s : i)
					Json.set(res, pl+s, Json.get(configs, pl+s));
				
				List<String> u = lconfs.get("Unit");
				if(u == null)
					u = new ArrayList<>();
				for(String ut : Unit.getTypes().keySet()) {
					String pu = pl+"units "+ut+" ";
					for(String s : u)
						Json.set(res, pu+s, Json.get(configs, pu+s));
				}
				
				List<String> c = lconfs.get("Chest");
				if(c == null)
					c = new ArrayList<>();
				JsonArray chests = Json.parseArr(Options.get("chests").replace("chestsalvage", "dungeonchest"));
				for(int j=0; j<chests.size(); j++) {
					String pc = pl+"chests "+chests.get(j).getAsString()+" ";
					for(String s : c)
						Json.set(res, pc+s, Json.get(configs, pc+s));
				}
				
				List<String> t = lconfs.get("Sleep");
				if(t == null)
					t = new ArrayList<>();
				for(int j=0; j<5; j++) {
					String ps = pl+"sleep "+j+" ";
					for(String s : t)
						Json.set(res, ps+s, Json.get(configs, ps+s));
				}
				
				List<String> d = lconfs.get("PlaceDelay");
				if(d == null)
					d = new ArrayList<>();
				String pd = pl+"unitPlaceDelay ";
				for(String s : d)
					Json.set(res, pd+s, Json.get(configs, pd+s));
				
				List<String> a = lconfs.get("Captain");
				if(a == null)
					a = new ArrayList<>();
				a.add("fav");
				List<String> cls = lconfs.get("CaptainList");
				if(cls == null)
					cls = new ArrayList<>();
				for(String cl : cls) {
					String pcl = pl+"caps "+cl+" ";
					HashSet<String> caps = getFavCaps(p.getCid(), l.getLid(), new ListType(cl));
					for(String pc : caps) {
						pc = pcl+pc+" ";
						for(String s : a)
							Json.set(res, pc+s, Json.get(configs, pc+s));
					}
				}
				
			}
			
		}
		
		NEF.save(ex.getPath(), Json.prettyJson(res));
		
	}
	
	
	public static class Importable {
		private static class Layer {
			public final String cid;
			public final String overrideLid;
			public final List<String> ptimes;
			public final JsonObject lay;
			public Layer(String cid, String overrideLid, List<String> ptime, JsonObject lay) {
				this.cid = cid;
				this.overrideLid = overrideLid;
				this.ptimes = ptime;
				this.lay = lay;
			}
		}
		
		
		//TODO importable
		private JsonObject g = null;
		public void addGlobal(JsonObject g) {
			this.g = g;
		}
		
		public JsonObject getGlobal() {
			return g;
		}
		
		private List<JsonObject> ps = new ArrayList<>();
		public void addProfile(JsonObject p) {
			ps.add(p);
		}
		
		public List<JsonObject> getProfiles() {
			return ps;
		}
		
		private List<Layer> ls = new ArrayList<>();
		public void addLayer(String cid, String overrideLid, List<String> ptime, JsonObject layer) {
			ls.add(new Layer(cid, overrideLid, ptime, layer));
		}
		
		
	}
	
	public static void importConfig(Importable im) {
		//TODO import
		//applying global options
		if(im.g != null) {
			JsonObject g = configs.get("Global").getAsJsonObject();
			for(String key : im.g.keySet())
				g.add(key, im.g.get(key));
		}
		
		//adding new Profiles
		for(JsonObject p : im.ps) {
			String cid = ""+LocalDateTime.now().toString().hashCode();
			configs.add(cid, p);
			//check bcs profile may not contain everything
			check(cid);
		}
		
		//applying layers to profiles
		for(program.ConfigsV2.Importable.Layer l : im.ls) {
			JsonObject pro = configs.getAsJsonObject(l.cid);
			JsonObject layers = pro.getAsJsonObject("layers");
			JsonObject layer = l.lay;
			String lid;
			if(l.overrideLid != null) {
				lid = l.overrideLid;
				if(layers.has(lid)) {
					layer.remove(lname.get());
					layer = Json.override(layers.getAsJsonObject(lid), layer);
					layers.remove(lid);
				}
			} else
				lid = ""+LocalDateTime.now().toString().hashCode();
			
			String name = layer.get(lname.get()).getAsString();
			if(isLNameTaken(l.cid, name))
				name += "_" + Maths.ranString(3);
			layers.add(lid, layer);
			for(String t : l.ptimes)
				pro.getAsJsonObject("times").addProperty(t, lid);
		}
		
		//basically: repair everything
		checkAll();
	}
	
	

	
	private static int sign(int in) {
		return (int) Math.signum(in);
	}
	
}