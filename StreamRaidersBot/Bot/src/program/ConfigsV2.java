package program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import include.Json;
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
	
	public static boolean getGBoo(GBoo con) {
		return configs.getAsJsonObject("Global").get(con.get()).getAsBoolean();
	}
	
	public static void setGBoo(GBoo con, boolean b) {
		configs.getAsJsonObject("Global").addProperty(con.get(), b);
	}
	
	
	
	private static class PStr extends All {
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
	
	/*
	private static class Arr extends All {
		public Arr(String con) {
			super(con);
		}
	}
	
	
	
	public static JsonArray getArr(String cid, String lay, Arr con) {
		return getLayer(cid, lay).getAsJsonArray(con.get());
	}
	
	public static void setArr(String cid, String lay, Arr con, JsonArray val) {
		getLayer(cid, lay).add(con.get(), val);
	}
	*/
	
	/*
	public static class Obj extends All {
		public Obj(String con) {
			super(con);
		}
	}
	
	public static final Obj caps = new Obj("caps");
	
	public static JsonObject getObj(String cid, String lay, Obj con) {
		if(lay.equals("(all)")) {
			JsonObject ret = new JsonObject();
			String[] lays = getLayers(cid);
			for(String l : lays)
				JsonParser.check(ret, getObj(cid, l, con));
			
			return ret;
		}
		return getLayer(cid, lay).getAsJsonObject(con.get());
	}
	
	public static void setObj(String cid, String lay, Obj con, JsonObject val) {
		if(lay.equals("(all)")) {
			String[] lays = getLayers(cid);
			for(String l : lays) 
				setObj(cid, l, con, val.deepCopy());
			
			return;
		}
		getLayer(cid, lay).add(con.get(), val);
	}
	*/
	
	
	public static class Str extends All {
		public Str(String con) {
			super(con);
		}
	}
	
	public static final Str dungeonSlot = new Str("dungeonSlot");
	public static final Str lname = new Str("name");
	public static final Str canBuyChest = new Str("canBuyChest");
	public static final Str canBuyEventChest = new Str("canBuyEventChest");
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
	public static final Int maxTimeLeft = new Int("maxTimeLeft");
	public static final Int minTimeLeft = new Int("minTimeLeft");
	public static final Int color = new Int("color");
	public static final Int unitUpdate = new Int("unitUpdate");
	public static final Int raidUpdate = new Int("raidUpdate");
	public static final Int mapUpdate = new Int("mapUpdate");
	public static final Int storeUpdate = new Int("storeUpdate");
	public static final Int questEventRewardsUpdate = new Int("questEventRewardsUpdate");
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
	public static final Boo placeMarkerOnly = new Boo("placeMarkerOnly");
	public static final Boo preferRoguesOnTreasureMaps = new Boo("preferRoguesOnTreasureMaps");
	public static final Boo allowPlaceFirst = new Boo("allowPlaceFirst");
	public static final Boo proxyMandatory = new Boo("proxyMandatory");
	public static final Boo campaignEpicPlaceFavOnly = new Boo("campaignEpicPlaceFavOnly");
	public static final Boo dungeonEpicPlaceFavOnly = new Boo("dungeonEpicPlaceFavOnly");
	public static final Boo campaignFavOnly = new Boo("campaignFavOnly");
	public static final Boo dungeonFavOnly = new Boo("dungeonFavOnly");
	
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
	public static final UniInt difmin = new UniInt("difmin");
	public static final UniInt difmax = new UniInt("difmax");
	public static final UniInt epicdifmin = new UniInt("epicdifmin");
	public static final UniInt epicdifmax = new UniInt("epicdifmax");
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
	
	
	private static class UniStr extends All {
		public UniStr(String con) {
			super(con);
		}
	}
	
	public static final UniStr spec = new UniStr("spec");
	
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
	
	public static final CheInt minc = new CheInt("min");
	public static final CheInt maxc = new CheInt("max");
	
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
	
	public static JsonArray getFavCaps(String cid, String lay, ListType list) {
		JsonArray ret = new JsonArray();
		if(lay.equals("(all)")) {
			String[] lays = getLayerIds(cid);
			for(String l : lays) {
				JsonArray caps = getFavCaps(cid, l, list);
				for(int i=0; i<caps.size(); i++)
					if(!ret.contains(caps.get(i)))
						ret.add(caps.get(i));
			}
				
			return ret;
		}
		if(list.equals(all)) {
			ret = getFavCaps(cid, lay, campaign);
			JsonArray caps2 = getFavCaps(cid, lay, dungeon);
			for(int i=0; i<caps2.size(); i++)
				if(!ret.contains(caps2.get(i)))
					ret.add(caps2.get(i));
			
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
	
	
	
	public static List<String> getCids() {
		List<String> cids = new ArrayList<>(Arrays.asList(configs.keySet().toArray(new String[configs.size()])));
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
			
			JsonObject global = Json.parseObj(Options.get("cglobal"));
			
			Json.check(configs, global);
			
			for(String key : getCids()) 
				check(key);
			
			for(String key : getCids()) {
				String sync = getPStr(key, synced);
				if(sync.equals("(none)"))
					continue;
				sync(key, sync);
			}
		}
	}
	
	public static void add(String name, String access_info) {
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", "ACCESS_INFO="+access_info);
		jo.addProperty("name", name);
		String key = ""+LocalDateTime.now().toString().hashCode();
		configs.add(key, jo);
		check(key);
	}
	
	
	public static boolean isPNameTaken(String name) {
		List<String> taken = new ArrayList<String>();
		for(String cid : getCids())
			taken.add(getPStr(cid, pname));
		taken.add("Global");
		taken.add("(none)");
		taken.add("(all)");
		return taken.contains(name);
	}
	
	public static boolean isLNameTaken(String cid, String name) {
		List<String> taken = new ArrayList<String>();
		for(String lid : getLayerIds(cid))
			taken.add(getStr(cid, lid, lname));
		taken.add("(none)");
		taken.add("(all)");
		return taken.contains(name);
	}
	
	private static JsonObject cmain = Json.parseObj(Options.get("cmain"));
	private static JsonObject clayer = Json.parseObj(Options.get("clayer"));
	private static JsonObject ccap = Json.parseObj(Options.get("ccap"));
	
	public static void check(String cid) {
		JsonObject main = configs.getAsJsonObject(cid);
		Json.check(main, cmain.deepCopy());
		JsonObject layers = main.getAsJsonObject("layers");
		for(String lay : layers.keySet()) {
			JsonObject layer = layers.getAsJsonObject(lay);
			Json.check(layer, clayer.deepCopy());
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
		boolean[] got = new boolean[2016];
		Integer s;
		int e;
		for(String key : times.keySet()) {
			String[] time = key.split("-");
			s = Integer.parseInt(time[0]);
			e = Integer.parseInt(time[1]);
			for(; s<=e; s++)
				got[s] = true;
		}
		s = null;
		for(int i=0; i<2016; i++) {
			if(!got[i]) {
				if(s == null)
					s = i;
			} else if(s != null) {
				times.addProperty(s+"-"+i, "(default)");
				s = null;
			}
		}
		if(s != null)
			times.addProperty(s+"-"+2015, "(default)");
	}
	
	//	TODO
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
		private static enum ConfigClasses {
			GStr, GBoo, PStr, PObj, Str, Int, Boo, UniInt, UniStr, CheInt, CheBoo, SleInt, UPDInt, ListType, CapInt, CapBoo
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
						case GStr:
						case GBoo:
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
				public Layer(String lid) {
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
				i.add("name");
				i.add("color");
				for(String s : i)
					Json.set(res, pl+s, Json.get(configs, pl+s));
				
				List<String> u = lconfs.get("Unit");
				for(String ut : Unit.getTypes().keySet()) {
					String pu = pl+"units "+ut+" ";
					for(String s : u) {
						Json.set(res, pu+s, Json.get(configs, pu+s));
					}
				}
				
				List<String> c = lconfs.get("Chest");
				JsonArray chests = Json.parseArr(Options.get("chests").replace("chestsalvage", "dungeonchest"));
				for(int j=0; j<chests.size(); j++) {
					String pc = pl+"chests "+chests.get(j).getAsString()+" ";
					for(String s : c)
						Json.set(res, pc+s, Json.get(configs, pc+s));
				}
				
				List<String> t = lconfs.get("Sleep");
				for(int j=0; j<5; j++) {
					String ps = pl+"sleep "+j+" ";
					for(String s : t)
						Json.set(res, ps+s, Json.get(configs, ps+s));
				}
				
				List<String> d = lconfs.get("PlaceDelay");
				String pd = pl+"unitPlaceDelay ";
				for(String s : d)
					Json.set(res, pd+s, Json.get(configs, pd+s));
				
				List<String> a = lconfs.get("Captain");
				List<String> cls = lconfs.get("CaptainList");
				for(String cl : cls) {
					String pcl = pl+"caps "+cl+" ";
					JsonArray caps = getFavCaps(p.getCid(), l.getLid(), new ListType(cl));
					for(int j=0; j<caps.size(); j++) {
						String pc = pcl+caps.get(j).getAsString()+" ";
						for(String s : a)
							Json.set(res, pc+s, Json.get(configs, pc+s));
					}
				}
				
			}
			
		}
		
		NEF.save(ex.getPath(), Json.prettyJson(res));
		
	}
	
	
	public static class Importable {
		//TODO importable
	}
	
	public static void importConfig(Importable im) {
		//TODO import
	}
	
	

	
	private static int sign(int in) {
		return (int) Math.signum(in);
	}
	
}