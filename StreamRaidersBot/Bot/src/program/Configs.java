package program;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Maths;
import include.NEF;
import run.ProfileType;


public class Configs {
	
	public static class NotACaptainException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	
	private static final String path = "data/configs.json";
	private static final String bpath = "data/temp/configs_.json";
	
	private static final int configVersion = 3;

	private static JsonObject config = null;
	
	
	public static JsonObject getProfile(String cid) {
		return config.getAsJsonObject(cid);
	}
	
	private static class All {
		public final String con;
		public final ProfileType pt;
		public All(String con, ProfileType pt) {
			this.con = con;
			this.pt = pt;
		}
		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof All))
				return false;
			All other = (All) obj;
			return con.equals(other.con)
					&& pt == other.pt;
		}
	}
	
	private static class GStr extends All {
		public GStr(String con) {
			super(con, null);
		}
	}

	public static final GStr fontFile = new GStr("fontFile");
	public static final GStr blocked_errors = new GStr("blocked_errors");
	
	public static String getGStr(GStr con) {
		return config.getAsJsonObject("Global").get(con.con).getAsString();
	}
	
	public static void setGStr(GStr con, String str) {
		config.getAsJsonObject("Global").addProperty(con.con, str);
	}
	
	
	
	private static class GBoo extends All {
		public GBoo(String con) {
			super(con, null);
		}
	}

	public static final GBoo useMemoryReleaser = new GBoo("useMemoryReleaser");
	public static final GBoo needCloseConfirm = new GBoo("needCloseConfirm");
	public static final GBoo freeUpMemoryByUsingDrive = new GBoo("freeUpMemoryByUsingDrive");
	
	public static boolean getGBoo(GBoo con) {
		return config.getAsJsonObject("Global").get(con.con).getAsBoolean();
	}
	
	public static void setGBoo(GBoo con, boolean b) {
		config.getAsJsonObject("Global").addProperty(con.con, b);
	}
	
	
	private static class GInt extends All {
		public GInt(String con) {
			super(con, null);
		}
	}

	public static final GInt maxProfileActions = new GInt("maxProfileActions");
	
	public static int getGInt(GInt con) {
		return config.getAsJsonObject("Global").get(con.con).getAsInt();
	}
	
	public static void setGInt(GInt con, int x) {
		config.getAsJsonObject("Global").addProperty(con.con, x);
	}
	
	
	public static ArrayList<String> getConfigIds() {
		ArrayList<String> cids = new ArrayList<>(config.keySet());
		cids.removeAll(Arrays.asList("Global version type".split(" ")));
		return cids;
	}
	
	public static class PStr extends All {
		public PStr(String con) {
			super(con, null);
		}
	}
	
	public static final PStr cookies = new PStr("cookies");
	public static final PStr pname = new PStr("name");
	public static final PStr syncedViewer = new PStr("syncedViewer");
	public static final PStr syncedCaptain = new PStr("syncedCaptain");
	
	public static String getPStr(String cid, PStr con) {
		return config.getAsJsonObject(cid).get(con.con).getAsString();
	}
	
	public static void setPStr(String cid, PStr con, String val) {
		config.getAsJsonObject(cid).addProperty(con.con, val);
	}
	
	
	
	public static class PBoo extends All {
		public PBoo(String con) {
			super(con, null);
		}
	}
	
	public static final PBoo canCaptain = new PBoo("canCaptain");
	
	public static boolean getPBoo(String cid, PBoo con) {
		return config.getAsJsonObject(cid).get(con.con).getAsBoolean();
	}
	
	public static void setPBoo(String cid, PBoo con, boolean val) {
		config.getAsJsonObject(cid).addProperty(con.con, val);
	}
	
	
	
	public static class UObj extends All {
		public UObj(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final UObj statsViewer = new UObj("stats", ProfileType.VIEWER);
	public static final UObj ptimesViewer = new UObj("times", ProfileType.VIEWER);
	public static final UObj statsCaptain = new UObj("stats", ProfileType.CAPTAIN);
	public static final UObj ptimesCaptain = new UObj("times", ProfileType.CAPTAIN);
	
	public static JsonObject getUObj(String cid, UObj con) {
		if(con.pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		return config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.getAsJsonObject(con.con);
	}
	
	public static void setUObj(String cid, UObj con, JsonObject val) {
		if(con.pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.add(con.con, val);
	}
	
	
	public static ArrayList<String> getLayerIds(String cid, ProfileType pt) {
		if(pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		return new ArrayList<String>(config.getAsJsonObject(cid)
				.getAsJsonObject(pt.toString())
				.getAsJsonObject("layers")
				.keySet());
	}
	
	private static JsonObject getLayer(String cid, ProfileType pt, String lay) {
		if(pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		return config.getAsJsonObject(cid)
				.getAsJsonObject(pt.toString())
				.getAsJsonObject("layers")
				.getAsJsonObject(lay);
	}
	
	public static String addLayer(String cid, ProfileType pt, String name, String clay) {
		if(pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		String lid = ranUUIDLayer(cid, pt);
		config.getAsJsonObject(cid)
				.getAsJsonObject(pt.toString())
				.getAsJsonObject("layers")
				.add(lid, getLayer(cid, pt, clay)
					.deepCopy());
		setStr(cid, lid, lnameViewer, name);
		return lid;
	}
	
	public static void remLayer(String cid, ProfileType pt, String lay) {
		if(pt == ProfileType.CAPTAIN && !getPBoo(cid, canCaptain))
			throw new NotACaptainException();
		config.getAsJsonObject(cid)
				.getAsJsonObject(pt.toString())
				.getAsJsonObject("layers")
				.remove(lay);
	}
	
	public static class Str extends All {
		public Str(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final Str dungeonSlotViewer = new Str("dungeonSlot", ProfileType.VIEWER);
	public static final Str lnameViewer = new Str("name", ProfileType.VIEWER);
	public static final Str userAgentViewer = new Str("userAgent", ProfileType.VIEWER);
	public static final Str proxyDomainViewer = new Str("proxyDomain", ProfileType.VIEWER);
	public static final Str proxyUserViewer = new Str("proxyUser", ProfileType.VIEWER);
	public static final Str proxyPassViewer = new Str("proxyPass", ProfileType.VIEWER);
	public static final Str captainTeamViewer = new Str("captainTeam", ProfileType.VIEWER);
	
	public static final Str lnameCaptain = new Str("name", ProfileType.CAPTAIN);
	public static final Str userAgentCaptain = new Str("userAgent", ProfileType.CAPTAIN);
	public static final Str proxyDomainCaptain = new Str("proxyDomain", ProfileType.CAPTAIN);
	public static final Str proxyUserCaptain = new Str("proxyUser", ProfileType.CAPTAIN);
	public static final Str proxyPassCaptain = new Str("proxyPass", ProfileType.CAPTAIN);
	
	public static String getStr(String cid, String lid, Str con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			String sel = getStr(cid, lays.remove(0), con);
			for(String lay : lays)
				if(!sel.equals(getStr(cid, lay, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, con.pt, lid)
				.get(con.con)
				.getAsString();
	}
	
	public static void setStr(String cid, String lid, Str con, String val) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, con.pt))
				setStr(cid, lay, con, val);
			return;
		}
		getLayer(cid, con.pt, lid)
				.addProperty(con.con, val);
	}
	
	
	
	public static class Int extends All {
		public Int(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final Int unitPlaceRetriesViewer = new Int("unitPlaceRetries", ProfileType.VIEWER);
	public static final Int mapReloadAfterXRetriesViewer = new Int("mapReloadAfterXRetries", ProfileType.VIEWER);
	public static final Int maxUnitPerRaidViewer = new Int("maxUnitPerRaid", ProfileType.VIEWER);
	public static final Int capInactiveTresholdViewer = new Int("capInactiveTreshold", ProfileType.VIEWER);
	public static final Int storeMinKeysViewer = new Int("storeMinKeys", ProfileType.VIEWER);
	public static final Int storeMinBonesViewer = new Int("storeMinBones", ProfileType.VIEWER);
	public static final Int storeMinEventcurrencyViewer = new Int("storeMinEventcurrency", ProfileType.VIEWER);
	public static final Int storeMinGoldViewer = new Int("storeMinGold", ProfileType.VIEWER);
	public static final Int upgradeMinGoldViewer = new Int("upgradeMinGold", ProfileType.VIEWER);
	public static final Int unlockMinGoldViewer = new Int("unlockMinGold", ProfileType.VIEWER);
	public static final Int colorViewer = new Int("color", ProfileType.VIEWER);
	public static final Int unitUpdateViewer = new Int("unitUpdate", ProfileType.VIEWER);
	public static final Int raidUpdateViewer = new Int("raidUpdate", ProfileType.VIEWER);
	public static final Int mapUpdateViewer = new Int("mapUpdate", ProfileType.VIEWER);
	public static final Int storeUpdateViewer = new Int("storeUpdate", ProfileType.VIEWER);
	public static final Int skinUpdateViewer = new Int("skinUpdate", ProfileType.VIEWER);
	public static final Int questEventRewardsUpdateViewer = new Int("questEventRewardsUpdate", ProfileType.VIEWER);
	public static final Int capsUpdateViewer = new Int("capsUpdate", ProfileType.VIEWER);
	public static final Int proxyPortViewer = new Int("proxyPort", ProfileType.VIEWER);
	public static final Int unitPlaceDelayMinViewer = new Int("unitPlaceDelayMin", ProfileType.VIEWER);
	public static final Int unitPlaceDelayMaxViewer = new Int("unitPlaceDelayMax", ProfileType.VIEWER);
	
	public static final Int unitPlaceRetriesCaptain = new Int("unitPlaceRetries", ProfileType.CAPTAIN);
	public static final Int mapReloadAfterXRetriesCaptain = new Int("mapReloadAfterXRetries", ProfileType.CAPTAIN);
	public static final Int storeMinKeysCaptain = new Int("storeMinKeys", ProfileType.CAPTAIN);
	public static final Int storeMinBonesCaptain = new Int("storeMinBones", ProfileType.CAPTAIN);
	public static final Int storeMinEventcurrencyCaptain = new Int("storeMinEventcurrency", ProfileType.CAPTAIN);
	public static final Int storeMinGoldCaptain = new Int("storeMinGold", ProfileType.CAPTAIN);
	public static final Int upgradeMinGoldCaptain = new Int("upgradeMinGold", ProfileType.CAPTAIN);
	public static final Int unlockMinGoldCaptain = new Int("unlockMinGold", ProfileType.CAPTAIN);
	public static final Int colorCaptain = new Int("color", ProfileType.CAPTAIN);
	public static final Int unitUpdateCaptain = new Int("unitUpdate", ProfileType.CAPTAIN);
	public static final Int raidUpdateCaptain = new Int("raidUpdate", ProfileType.CAPTAIN);
	public static final Int mapUpdateCaptain = new Int("mapUpdate", ProfileType.CAPTAIN);
	public static final Int storeUpdateCaptain = new Int("storeUpdate", ProfileType.CAPTAIN);
	public static final Int skinUpdateCaptain = new Int("skinUpdate", ProfileType.CAPTAIN);
	public static final Int questEventRewardsUpdateCaptain = new Int("questEventRewardsUpdate", ProfileType.CAPTAIN);
	public static final Int capsUpdateCaptain = new Int("capsUpdate", ProfileType.CAPTAIN);
	public static final Int proxyPortCaptain = new Int("proxyPort", ProfileType.CAPTAIN);
	
	
	public static Integer getInt(String cid, String lid, Int con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			int sel = getInt(cid, lays.remove(0), con);
			for(String lay : lays)
				if(!(sel == getInt(cid, lay, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, con.pt, lid)
				.get(con.con)
				.getAsInt();
	}
	
	public static void setInt(String cid, String lid, Int con, int val) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, con.pt))
				setInt(cid, lay, con, val);
			return;
		}
		getLayer(cid, con.pt, lid).addProperty(con.con, val);
	}
	
	
	
	public static class Boo extends All {
		public Boo(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	

	public static final Boo useMultiPlaceExploitViewer = new Boo("useMultiPlaceExploit", ProfileType.VIEWER);
	public static final Boo useMultiQuestExploitViewer = new Boo("useMultiQuestExploit", ProfileType.VIEWER);
	public static final Boo useMultiUnitExploitViewer = new Boo("useMultiUnitExploit", ProfileType.VIEWER);
	public static final Boo useMultiChestExploitViewer = new Boo("useMultiChestExploit", ProfileType.VIEWER);
	public static final Boo useMultiEventExploitViewer = new Boo("useMultiEventExploit", ProfileType.VIEWER);
	public static final Boo preferRoguesOnTreasureMapsViewer = new Boo("preferRoguesOnTreasureMaps", ProfileType.VIEWER);
	public static final Boo allowPlaceFirstViewer = new Boo("allowPlaceFirst", ProfileType.VIEWER);
	public static final Boo proxyMandatoryViewer = new Boo("proxyMandatory", ProfileType.VIEWER);
	
	public static final Boo preferRoguesOnTreasureMapsCaptain = new Boo("preferRoguesOnTreasureMaps", ProfileType.CAPTAIN);
	public static final Boo proxyMandatoryCaptain = new Boo("proxyMandatory", ProfileType.CAPTAIN);
	
	public static Boolean getBoolean(String cid, String lid, Boo con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			boolean sel = getBoolean(cid, lays.remove(0), con);
			for(String lay : lays)
				if(!(sel == getBoolean(cid, lay, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, con.pt, lid).get(con.con).getAsBoolean();
	}
	
	public static void setBoolean(String cid, String lid, Boo con, boolean val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, con.pt))
				setBoolean(cid, l, con, val);
			return;
		}
		getLayer(cid, con.pt, lid).addProperty(con.con, val);
	}
	
	
	/**
	 * @param cid profile id
	 * @param pt profile type
	 * @param includeTypes 
	 * @return a ArrayList with all unit ids for the profile in the specified account type
	 */
	public static ArrayList<String> getUnitIds(String cid, ProfileType pt, boolean includeTypes) {
		ArrayList<String> ret = new ArrayList<>(config.getAsJsonObject(cid)
									.getAsJsonObject(pt.toString())
									.getAsJsonObject("unitInfo")
									.keySet());
		if(includeTypes)
			ret.addAll(Unit.getTypesList());
		
		return ret;
	}
	
	/**
	 * @param cid profile id
	 * @param pt profile type
	 * @return a String Array with all unit ids for the profile in the specified account type
	 */
	public static String[] getUnitIdsArray(String cid, ProfileType pt) {
		Set<String> ui = config.getAsJsonObject(cid)
							.getAsJsonObject(pt.toString())
							.getAsJsonObject("unitInfo")
							.keySet();
		
		return ui.toArray(new String[ui.size()]);
	}
	
	
	
	public static void addUnitId(String cid, ProfileType pt, String unitId, String unitType, int unitLevel) {
		
		JsonObject unitInfo = config.getAsJsonObject(cid)
								.getAsJsonObject(pt.toString())
								.getAsJsonObject("unitInfo");
		
		JsonObject info = new JsonObject();
		info.addProperty(levelViewer.con, unitLevel);
		info.addProperty(typeViewer.con, unitType);
		info.addProperty(fromViewer.con, cid);
		
		unitInfo.add(unitId, info);
		
		checkUnit(cid, pt, unitId, true);
		
	}
	
	private static void remUnitId(String cid, ProfileType pt, String unitId) {
		for(String lid : getLayerIds(cid, pt)) {
			JsonObject units = getLayer(cid, pt, lid).getAsJsonObject("units");
			for(String u : units.keySet()) {
				String sync = units.getAsJsonObject(u).get("sync").getAsString();
				if(sync.equals(unitId))
					syncUnit(cid, pt, lid, u, null);
			}
			units.remove(unitId);
		}
		config.getAsJsonObject(cid)
			.getAsJsonObject(pt.toString())
			.getAsJsonObject("unitInfo")
			.remove(unitId);
	}
	
	
	private static void checkUnit(String cid, ProfileType pt, String unitId, boolean checkSync) {
		//	all ids are numbers, if it is not a number its probably a type which isn't tracked in unit info
		if(!unitId.matches("\\d+"))
			return;
		
		String type = getUnitInfoStr(cid, unitId, new UniInfoStr("type", pt));
		
		for(String lid : getLayerIds(cid, pt)) {
			JsonObject units = getLayer(cid, pt, lid).getAsJsonObject("units");
			
			if(units.has(unitId))
				continue;
			
			JsonObject defUnit = units.getAsJsonObject(type);
			units.add(unitId, defUnit.deepCopy());
			
			if(checkSync) {
				String sync = defUnit.get("sync").getAsString();
				if(!sync.equals("(none)"))
					syncUnit(cid, pt, lid, unitId, sync);
			}
		}
		
	}

	public static class UniInfoInt extends All {
		public UniInfoInt(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final UniInfoInt levelViewer = new UniInfoInt("level", ProfileType.VIEWER);

	public static final UniInfoInt levelCaptain = new UniInfoInt("level", ProfileType.CAPTAIN);
	
	public static int getUnitInfoInt(String cid, String unitId, UniInfoInt con) {
		return config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.getAsJsonObject("unitInfo")
				.getAsJsonObject(unitId)
				.get(con.con).getAsInt();
	}
	
	public static void setUnitInfoInt(String cid, String unitId, UniInfoInt con, int val) {
		config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.getAsJsonObject("unitInfo")
				.getAsJsonObject(unitId)
				.addProperty(con.con, val);
	}
	
	
	public static class UniInfoStr extends All {
		public UniInfoStr(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final UniInfoStr typeViewer = new UniInfoStr("type", ProfileType.VIEWER);
	public static final UniInfoStr fromViewer = new UniInfoStr("from", ProfileType.VIEWER);

	public static final UniInfoStr typeCaptain = new UniInfoStr("type", ProfileType.CAPTAIN);
	public static final UniInfoStr fromCaptain = new UniInfoStr("from", ProfileType.CAPTAIN);
	
	
	public static String getUnitInfoStr(String cid, String unitId, UniInfoStr con) {
		return config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.getAsJsonObject("unitInfo")
				.getAsJsonObject(unitId)
				.get(con.con).getAsString();
	}
	
	public static void setUnitInfoStr(String cid, String unitId, UniInfoStr con, String val) {
		config.getAsJsonObject(cid)
				.getAsJsonObject(con.pt.toString())
				.getAsJsonObject("unitInfo")
				.getAsJsonObject(unitId)
				.addProperty(con.con, val);
	}
	
	/**
	 * syncs the unit settings to another unit
	 * @param cid profile id
	 * @param pt profile type
	 * @param lid layer id
	 * @param unitId unit that will be synced
	 * @param defUnitId donator unit, null to unsync
	 */
	public static void syncUnit(String cid, ProfileType pt, String lid, String unitId, String defUnitId) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, pt))
				syncUnit(cid, pt, lay, unitId, defUnitId);
			return;
		}
		
		JsonObject units = getLayer(cid, pt, lid)
				.getAsJsonObject("units");

		JsonObject u = units.getAsJsonObject(unitId);
		if(defUnitId == null || defUnitId.equals("(none)")) {
			u.add("opt", u.get("opt").deepCopy());
			u.addProperty("sync", "(none)");
		} else {
			u.add("opt", units.getAsJsonObject(defUnitId).get("opt"));
			u.addProperty("sync", defUnitId);
		}
	}
	
	public static String getUnitSync(String cid, ProfileType pt, String lid, String unitId) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, pt);
			String sel = getUnitSync(cid, pt, lays.remove(0), unitId);
			StringBuilder ret = null;
			for(String lay : lays) {
				String str = getUnitSync(cid, pt, lay, unitId);
				if(ret == null) {
					if(!sel.equals(str))
						ret = new StringBuilder(sel).append("::").append(str);
				} else {
					if(ret.indexOf(str) == -1)
						ret.append("::").append(str);
				}
			}
			return ret == null ? sel : ret.toString();
		}
		return getLayer(cid, pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.get("sync").getAsString();
	}
	
	
	public static String getUnitSpec(String cid, ProfileType pt, String lid, String unitId) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, pt);
			String sel = getUnitSpec(cid, pt, lays.remove(0), unitId);
			StringBuilder ret = null;
			for(String lay : lays) {
				String str = getUnitSpec(cid, pt, lay, unitId);
				if(ret == null) {
					if(!sel.equals(str))
						ret = new StringBuilder(sel).append("::").append(str);
				} else {
					if(ret.indexOf(str) == -1)
						ret.append("::").append(str);
				}
			}
			return ret == null ? sel : ret.toString();
		}
		return getLayer(cid, pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.get("spec").getAsString();
	}
	
	public static void setUnitSpec(String cid, ProfileType pt, String lid, String unitId, String spec) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, pt))
				setUnitSpec(cid, pt, l, unitId, spec);
			return;
		}
		getLayer(cid, pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.addProperty("spec", spec);
	}
	
	public static class UniInt extends All {
		public UniInt(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final UniInt placeViewer = new UniInt("place", ProfileType.VIEWER);
	public static final UniInt epicViewer = new UniInt("epic", ProfileType.VIEWER);
	public static final UniInt placedunViewer = new UniInt("placedun", ProfileType.VIEWER);
	public static final UniInt epicdunViewer = new UniInt("epicdun", ProfileType.VIEWER);
	public static final UniInt upgradeViewer = new UniInt("upgrade", ProfileType.VIEWER);
	public static final UniInt unlockViewer = new UniInt("unlock", ProfileType.VIEWER);
	public static final UniInt dupeViewer = new UniInt("dupe", ProfileType.VIEWER);
	public static final UniInt buyViewer = new UniInt("buy", ProfileType.VIEWER);
	
	public static final UniInt placeCaptain = new UniInt("place", ProfileType.CAPTAIN);
	public static final UniInt placedunCaptain = new UniInt("placedun", ProfileType.CAPTAIN);
	public static final UniInt upgradeCaptain = new UniInt("upgrade", ProfileType.CAPTAIN);
	public static final UniInt unlockCaptain = new UniInt("unlock", ProfileType.CAPTAIN);
	public static final UniInt dupeCaptain = new UniInt("dupe", ProfileType.CAPTAIN);
	public static final UniInt buyCaptain = new UniInt("buy", ProfileType.CAPTAIN);
	
	public static Integer getUnitInt(String cid, String lid, String unitId, UniInt con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			int sel = getUnitInt(cid, lays.remove(0), unitId, con);
			for(String lay : lays)
				if(!(sel == getUnitInt(cid, lay, unitId, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, con.pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.getAsJsonObject("opt")
				.get(con.con).getAsInt();
	}
	
	public static void setUnitInt(String cid, String lid, String unitId, UniInt con, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, con.pt))
				setUnitInt(cid, l, unitId, con, val);
			return;
		}
		getLayer(cid, con.pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.getAsJsonObject("opt")
				.addProperty(con.con, val);
	}
	
	
	public static class UniStr extends All {
		public UniStr(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final UniStr chestsViewer = new UniStr("chests", ProfileType.VIEWER);
	public static final UniStr favOnlyViewer = new UniStr("favOnly", ProfileType.VIEWER);
	public static final UniStr markerOnlyViewer = new UniStr("markerOnly", ProfileType.VIEWER);
	public static final UniStr canVibeViewer = new UniStr("canVibe", ProfileType.VIEWER);
	public static final UniStr dunEpicModeViewer = new UniStr("dunEpicMode", ProfileType.VIEWER);
	
	public static final UniStr chestsCaptain = new UniStr("chests", ProfileType.CAPTAIN);
	
	public static String getUnitString(String cid, String lid, String unitId, UniStr con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			String sel = getUnitString(cid, lays.remove(0), unitId, con);
			StringBuilder ret = null;
			for(String lay : lays) {
				String str = getUnitString(cid, lay, unitId, con);
				if(ret == null) {
					if(!sel.equals(str))
						ret = new StringBuilder(sel).append("::").append(str);
				} else {
					if(ret.indexOf(str) == -1)
						ret.append("::").append(str);
				}
			}
			return ret == null ? sel : ret.toString();
		}
		return getLayer(cid, con.pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.getAsJsonObject("opt")
				.get(con.con).getAsString();
	}
	
	public static void setUnitString(String cid, String lid, String unitId, UniStr con, String str) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, con.pt))
				setUnitString(cid, l, unitId, con, str);
			return;
		}
		getLayer(cid, con.pt, lid)
				.getAsJsonObject("units")
				.getAsJsonObject(unitId)
				.getAsJsonObject("opt")
				.addProperty(con.con, str);
	}
	
	
	
	private static class CheBoo extends All {
		public CheBoo(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final CheBoo enabledViewer = new CheBoo("enabled", ProfileType.VIEWER);
	
	public static Boolean getChestBoolean(String cid, String lid, String cType, CheBoo con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			boolean sel = getChestBoolean(cid, lays.remove(0), cType, con);
			for(String lay : lays)
				if(!(sel == getChestBoolean(cid, lay, cType, con)))
					return null;
			
			return sel;
		}
		try {
			return getLayer(cid, con.pt, lid)
					.getAsJsonObject("chests")
					.getAsJsonObject(cType)
					.get(con.con).getAsBoolean();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static void setChestBoolean(String cid, String lid, String cType, CheBoo con, boolean b) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, con.pt))
				setChestBoolean(cid, lay, cType, con, b);
			return;
		}
		getLayer(cid, con.pt, lid)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.con, b);
	}
	
	
	private static class CheInt extends All {
		public CheInt(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final CheInt minLoyViewer = new CheInt("minLoy", ProfileType.VIEWER);
	public static final CheInt maxLoyViewer = new CheInt("maxLoy", ProfileType.VIEWER);
	public static final CheInt minTimeViewer = new CheInt("minTime", ProfileType.VIEWER);
	public static final CheInt maxTimeViewer = new CheInt("maxTime", ProfileType.VIEWER);
	
	public static final CheInt weightCaptain = new CheInt("weight", ProfileType.CAPTAIN);
	
	public static Integer getChestInt(String cid, String lid, String cType, CheInt con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			int sel = getChestInt(cid, lays.remove(0), cType, con);
			for(String lay : lays)
				if(!(sel == getChestInt(cid, lay, cType, con)))
					return null;
			
			return sel;
		}
		try {
			return getLayer(cid, con.pt, lid)
					.getAsJsonObject("chests")
					.getAsJsonObject(cType)
					.get(con.con).getAsInt();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static void setChestInt(String cid, String lid, String cType, CheInt con, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, con.pt))
				setChestInt(cid, l, cType, con, val);
			return;
		}
		getLayer(cid, con.pt, lid)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.con, val);
	}
	
	
	public static class SleInt extends All {
		public SleInt(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final SleInt maxViewer = new SleInt("max", ProfileType.VIEWER);
	public static final SleInt minViewer = new SleInt("min", ProfileType.VIEWER);
	public static final SleInt syncSlotViewer = new SleInt("sync", ProfileType.VIEWER);
	
	public static final SleInt maxCaptain = new SleInt("max", ProfileType.CAPTAIN);
	public static final SleInt minCaptain = new SleInt("min", ProfileType.CAPTAIN);
	public static final SleInt syncSlotCaptain = new SleInt("sync", ProfileType.CAPTAIN);
	
	public static Integer getSleepInt(String cid, String lid, String slot, SleInt con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			int sel = getSleepInt(cid, lays.remove(0), slot, con);
			for(String lay : lays)
				if(!(sel == getSleepInt(cid, lay, slot, con)))
					return null;
			
			return sel;
		}
		return getLayer(cid, con.pt, lid)
				.getAsJsonObject("sleep")
				.getAsJsonObject(slot)
				.get(con.con).getAsInt();
	}
	
	public static void setSleepInt(String cid, String lid, String slot, SleInt con, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, con.pt))
				setSleepInt(cid, l, slot, con, val);
			return;
		}
		getLayer(cid, con.pt, lid)
				.getAsJsonObject("sleep")
				.getAsJsonObject(slot)
				.addProperty(con.con, val);
	}
	
	public static class ListType extends All {
		public ListType(String con) {
			super(con, ProfileType.VIEWER);
		}
	}
	
	public static final ListType campaign = new ListType("campaign");
	public static final ListType dungeon = new ListType("dungeon");
	public static final ListType all = new ListType("all");
	
	private static JsonObject getList(String cid, String lay, ListType con) {
		return getLayer(cid, con.pt, lay)
				.getAsJsonObject("caps")
				.getAsJsonObject(con.con);
	}
	
	
	/*
	 * >0					: fav
	 * <0					: block
	 * null					: remove
	 */
	public static void favCap(String cid, String lid, String cap, ListType list, Integer val) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, list.pt))
				favCap(cid, lay, cap, list, val);
			return;
		}
		if(list.equals(all)) {
			favCap(cid, lid, cap, campaign, val);
			favCap(cid, lid, cap, dungeon, val);
			return;
		} 
		
		JsonObject caps = getList(cid, lid, list);
		if(val == null) {
			caps.remove(cap);
		} else {
			if(caps.has(cap) && sign(getCapInt(cid, lid, cap, list, fav)) * sign(val) > 0)
				return;
			JsonObject jo = ccap.deepCopy();
			jo.addProperty("fav", val);
			caps.add(cap, jo);
		}
		
	}
	
	public static HashSet<String> getFavCaps(String cid, String lid, ListType list) {
		HashSet<String> ret = new HashSet<>();
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, list.pt)) {
				HashSet<String> caps = getFavCaps(cid, lay, list);
				for(String c : caps)
					ret.add(c);
			}
				
			return ret;
		}
		if(list.equals(all)) {
			ret = getFavCaps(cid, lid, campaign);
			HashSet<String> caps = getFavCaps(cid, lid, dungeon);
			for(String c : caps)
				ret.add(c);
			
			return ret;
		}
		JsonObject caps = getList(cid, lid, list);
		for(String key : caps.keySet())
			ret.add(key);
		
		return ret;
	}
	
	
	public static class CapBoo extends All {
		public CapBoo(String con) {
			super(con, ProfileType.VIEWER);
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
	public static Integer getCapBooTend(String cid, String lid, String cap, ListType list, CapBoo con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, con.pt);
			Integer sel = getCapBooTend(cid, lays.remove(0), cap, list, con);
			boolean same = true;
			for(String lay : lays) {
				Integer val = getCapBooTend(cid, lay, cap, list, con);
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
			Integer b1 = getCapBooTend(cid, lid, cap, campaign, con);
			Integer b2 = getCapBooTend(cid, lid, cap, dungeon, con);
			if(b1 == b2)
				return b1;
			if(b1 == null)
				return ((b2*b2)-2)/b2;
			if(b2 == null)
				return ((b1*b1)-2)/b1;
			
			return 0;
		}
		JsonObject caps = getList(cid, lid, list);
		if(!caps.has(cap))
			return null;
		return caps.getAsJsonObject(cap)
			.get(con.con)
			.getAsBoolean()
				? 2
				: -2;
	}
	
	public static void setCapBoo(String cid, String lid, String cap, ListType list, CapBoo con, boolean val) {
		if(lid.equals("(all)")) {
			for(String lay : getLayerIds(cid, list.pt))
				setCapBoo(cid, lay, cap, list, con, val);
			return;
		}
		if(list.equals(all)) {
			setCapBoo(cid, lid, cap, campaign, con, val);
			setCapBoo(cid, lid, cap, dungeon, con, val);
			return;
		}
		JsonObject caps = getList(cid, lid, list);
		if(!caps.has(cap))
			favCap(cid, lid, cap, list, 1);
		caps.getAsJsonObject(cap)
			.addProperty(con.con, val);
	}
	
	
	private static class CapInt extends All {
		public CapInt(String con) {
			super(con, ProfileType.VIEWER);
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
	public static Integer getCapInt(String cid, String lid, String cap, ListType list, CapInt con) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, list.pt);
			Integer sel = getCapInt(cid, lays.remove(0), cap, list, con);
			boolean same = true;
			boolean conNull = false;
			for(String lay : lays) {
				Integer c = getCapInt(cid, lay, cap, list, con);
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
			Integer i1 = getCapInt(cid, lid, cap, campaign, con);
			Integer i2 = getCapInt(cid, lid, cap, dungeon, con);
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
		JsonObject caps = getList(cid, lid, list);
		if(!caps.has(cap))
			return null;
		try {
			return caps.getAsJsonObject(cap)
					.get(con.con)
					.getAsInt();
		} catch (UnsupportedOperationException | NullPointerException e) {
			return null;
		}
		
	}
	
	public static void setCapInt(String cid, String lid, String cap, ListType list, CapInt con, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, list.pt))
				setCapInt(cid, l, cap, list, con, val);
			return;
		}
		if(list.equals(all)) {
			setCapInt(cid, lid, cap, campaign, con, val);
			setCapInt(cid, lid, cap, dungeon, con, val);
			return;
		}
		JsonObject caps = getList(cid, lid, list);
		if(!caps.has(cap))
			favCap(cid, lid, cap, list, val);
		else
			caps.getAsJsonObject(cap)
				.addProperty(con.con, val);
	}
	
	
	
	public static Boolean isSlotLocked(String cid, String lid, String slot) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, ProfileType.VIEWER);
			boolean sel = isSlotLocked(cid, lays.remove(0), slot);
			for(String lay : lays)
				if(!(sel == isSlotLocked(cid, lay, slot)))
					return null;
			
			return sel;
		}
		return getLayer(cid, ProfileType.VIEWER, lid)
				.getAsJsonObject("lockedSlots")
				.get(""+slot).getAsBoolean();
	}
	
	public static void setSlotLocked(String cid, String lid, String slot, boolean b) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, ProfileType.VIEWER))
				setSlotLocked(cid, l, slot, b);
			return;
		}
		getLayer(cid, ProfileType.VIEWER, lid)
				.getAsJsonObject("lockedSlots")
				.addProperty(""+slot, b);
	}
	

	public static Integer getStoreRefreshInt(String cid, ProfileType pt, String lid, int ind) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, pt);
			int sel = getStoreRefreshInt(cid, pt, lays.remove(0), ind);
			for(String lay : lays)
				if(!(sel == getStoreRefreshInt(cid, pt, lay, ind)))
					return null;
			
			return sel;
		}
		return getLayer(cid, pt, lid)
				.getAsJsonObject("storeRefresh")
				.get(""+ind).getAsInt();
	}
	
	public static void setStoreRefreshInt(String cid, ProfileType pt, String lid, int ind, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, pt))
				setStoreRefreshInt(cid, pt, l, ind, val);
			return;
		}
		getLayer(cid, pt, lid)
				.getAsJsonObject("storeRefresh")
				.addProperty(""+ind, val);
	}
	
	
	public static class StorePrioType extends All {
		public StorePrioType(String con, ProfileType pt) {
			super(con, pt);
		}
	}
	
	public static final StorePrioType keysViewer = new StorePrioType("Key", ProfileType.VIEWER);
	public static final StorePrioType bonesViewer = new StorePrioType("Bone", ProfileType.VIEWER);
	public static final StorePrioType eventViewer = new StorePrioType("Event", ProfileType.VIEWER);
	
	public static final StorePrioType keysCaptain = new StorePrioType("Key", ProfileType.CAPTAIN);
	public static final StorePrioType bonesCaptain = new StorePrioType("Bone", ProfileType.CAPTAIN);
	public static final StorePrioType eventCaptain = new StorePrioType("Event", ProfileType.CAPTAIN);

	
	public static HashSet<String> getStorePrioList(String cid, String lid, StorePrioType spt) {
		HashSet<String> ret = new HashSet<>();
		if(lid.equals("(all)"))
			for(String lay : getLayerIds(cid, spt.pt))
				ret.addAll(getStorePrioList(cid, lay, spt));
		else 
			ret.addAll(getLayer(cid, spt.pt, lid)
							.getAsJsonObject("store"+spt.con+"Prios")
							.keySet());
		
		return ret;
	}
	
	public static Integer getStorePrioInt(String cid, String lid, StorePrioType spt, String type) {
		if(lid.equals("(all)")) {
			ArrayList<String> lays = getLayerIds(cid, spt.pt);
			int sel = getStorePrioInt(cid, lays.remove(0), spt, type);
			for(String lay : lays)
				if(!(sel == getStorePrioInt(cid, lay, spt, type)))
					return null;
			return sel;
		}
		JsonObject sp = getLayer(cid, spt.pt, lid)
				.getAsJsonObject("store"+spt.con+"Prios");
		
		return sp.has(type)
				? sp.get(type).getAsInt()
				: -1;
	}
	
	public static void setStorePrioInt(String cid, String lid, StorePrioType spt, String type, int val) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, spt.pt))
				setStorePrioInt(cid, l, spt, type, val);
			return;
		}
		getLayer(cid, spt.pt, lid)
				.getAsJsonObject("store"+spt.con+"Prios")
				.addProperty(type, val);
	}
	
	public static void remStorePrioInt(String cid, String lid, StorePrioType spt, String type) {
		if(lid.equals("(all)")) {
			for(String l : getLayerIds(cid, spt.pt))
				remStorePrioInt(cid, l, spt, type);
			return;
		}
		getLayer(cid, spt.pt, lid)
				.getAsJsonObject("store"+spt.con+"Prios")
				.remove(type);
	}
	
	
	public static class IllegalConfigVersionException extends Exception {
		private static final long serialVersionUID = 1L;
		public final List<Integer> required; 
		public final int version;
		public IllegalConfigVersionException(int version, Integer... required) {
			super("Config Version not Compatible, should be one of "+Arrays.toString(required)+", but is \""+version+"\"");
			this.required = Arrays.asList(required);
			this.version = version;
		}
	}
	
	public static class IllegalConfigTypeException extends Exception {
		private static final long serialVersionUID = 1L;
		public final List<String> required;
		public final String type;
		public IllegalConfigTypeException(String type, String... required) {
			super("Config Type not Compatible, should be one of "+Arrays.toString(required)+", but is \""+type+"\"");
			this.type = type;
			this.required = Arrays.asList(required);
		}
	}
	
	
	public static void load() throws IOException, IllegalConfigTypeException, IllegalConfigVersionException {
		load(false);
	}
	
	public static void load(boolean create) throws IOException, IllegalConfigTypeException, IllegalConfigVersionException {
		if(create) {
			config = new JsonObject();
			config.addProperty("type", "config");
			config.addProperty("version", configVersion);
		} else {
			try {
				config = Json.parseObj(NEF.read(bpath));
			} catch (FileNotFoundException e) {
				try {
					config = Json.parseObj(NEF.read(path));
				} catch (FileNotFoundException e1) {
					load(true);
					return;
				}
			}
			JsonElement ct = config.get("type");
			if(ct == null)
				throw new IllegalConfigTypeException("null", "config");
			
			if(!ct.getAsString().equals("config"))
				throw new IllegalConfigTypeException(ct.getAsString(), "config");
			
			JsonElement cv = config.get("version");
			if(cv == null)
				throw new IllegalConfigVersionException(-1, configVersion);
			
			if(cv.getAsInt() != configVersion)
				throw new IllegalConfigVersionException(cv.getAsInt(), configVersion);
			
		}
		
		checkAll();
		
		//	sync profiles
		for(String cid : getConfigIds()) {
			String sync = getPStr(cid, syncedViewer);
			if(!sync.equals("(none)"))
				syncProfile(cid, sync, ProfileType.VIEWER);
			

			if(getPBoo(cid, canCaptain)) {
				sync = getPStr(cid, syncedCaptain);
				if(!sync.equals("(none)"))
					syncProfile(cid, sync, ProfileType.CAPTAIN);
			}
		}
		
		//	sync units
		//	TODO canCaptain
		for(String cid : getConfigIds()) {
			
			String psync = getPStr(cid, syncedViewer);
			if(psync.equals("(none)"))
				syncUnits(cid, ProfileType.VIEWER);
			

			if(getPBoo(cid, canCaptain)) {
				psync = getPStr(cid, syncedCaptain);
				if(psync.equals("(none)"))
					syncUnits(cid, ProfileType.CAPTAIN);
			}
		}
		
	}
	
	private static void syncUnits(String cid, ProfileType pt) {
		for(String lid : getLayerIds(cid, pt)) {
			JsonObject units = getLayer(cid, pt, lid).getAsJsonObject("units");
			for(String key : units.keySet()) {
				String sync = units.getAsJsonObject(key).get("sync").getAsString();
				if(sync.equals("(none)"))
					continue;
				syncUnit(cid, pt, lid, key, sync);
			}
		}
	}
	
	public static String addProfile(String name, String access_info) {
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", "ACCESS_INFO="+access_info);
		jo.addProperty("name", name);
		String cid = ranUUIDProfile();
		config.add(cid, jo);
		check(cid);
		return cid;
	}
	
	public static void remProfile(String cid) {
		boolean canCaptain = getPBoo(cid, Configs.canCaptain);
		
		for(String c : getConfigIds()) {
			if(getPStr(c, syncedViewer).equals(cid))
				syncProfile(c, null, ProfileType.VIEWER);

			if(canCaptain && getPStr(c, syncedCaptain).equals(cid))
				syncProfile(c, null, ProfileType.VIEWER);
		}
		config.remove(cid);
	}
	
	public static boolean isPNameTaken(String name) {
		if(name.startsWith("(") && name.endsWith(")"))
			return true;
		ArrayList<String> taken = new ArrayList<>();
		for(String cid : getConfigIds())
			taken.add(getPStr(cid, pname));
		taken.add("Global");
		taken.add("version");
		taken.add("type");
		return taken.contains(name);
	}
	
	public static boolean isLNameTaken(String cid, ProfileType pt, String name) {
		if(name.startsWith("(") && name.endsWith(")"))
			return true;
		List<String> taken = new ArrayList<String>();
		Str con = pt == ProfileType.VIEWER ? lnameViewer : lnameCaptain;
		for(String lid : getLayerIds(cid, pt))
			taken.add(getStr(cid, lid, con));
		return taken.contains(name);
	}
	

	private static JsonObject cglobal = Json.parseObj(Options.get("cglobal"));
	private static JsonObject cprofile = Json.parseObj(Options.get("cprofile"));
	private static JsonObject cprofilespare = Json.parseObj(Options.get("cprofilespare"));
	private static JsonObject ccaptain = Json.parseObj(Options.get("ccaptain"));
	private static JsonObject clayerviewer = Json.parseObj(Options.get("clayerviewer"));
	private static JsonObject clayercaptain = Json.parseObj(Options.get("clayercaptain"));
	private static JsonObject clayerviewerspare = Json.parseObj(Options.get("clayerviewerspare"));
	private static JsonObject clayercaptainspare = Json.parseObj(Options.get("clayercaptainspare"));
	private static JsonObject cunitinfo = Json.parseObj(Options.get("cunitinfo"));
	private static JsonObject cunitviewer = Json.parseObj(Options.get("cunitviewer"));
	private static JsonObject cunitcaptain = Json.parseObj(Options.get("cunitcaptain"));
	private static JsonObject ccap = Json.parseObj(Options.get("ccap"));
	
	
	public static void checkAll() {
		if(config.has("Global"))
			Json.check(config.getAsJsonObject("Global"), cglobal.deepCopy(), true, null);
		else
			config.add("Global", cglobal.deepCopy());
		
		for(String key : getConfigIds()) 
			check(key);
	}
	
	public static void check(String cid) {
		JsonObject profile = config.getAsJsonObject(cid);
		Json.check(profile, cprofile.deepCopy(), true, cprofilespare.deepCopy());
		
		JsonObject unitTypes = Unit.getTypes();
		
		JsonObject acc = profile.getAsJsonObject(ProfileType.VIEWER.toString());
		
		JsonObject unitInfos = acc.getAsJsonObject("unitInfo");
		for(String u : unitInfos.keySet())
			Json.check(unitInfos.getAsJsonObject(u), cunitinfo.deepCopy(), true, null);
		
		JsonObject layersviewer = acc.getAsJsonObject("layers");
		for(String lay : layersviewer.keySet()) {
			JsonObject layer = layersviewer.getAsJsonObject(lay);
			Json.check(layer, clayerviewer.deepCopy(), true, clayerviewerspare.deepCopy());

			//	make sure all unit types are present
			JsonObject units = layer.getAsJsonObject("units");
			for(String type : unitTypes.keySet())
				if(!units.has(type))
					units.add(type, units.get(unitTypes.getAsJsonObject(type).get("rarity").getAsString()).deepCopy());
			
			for(String u : units.keySet())
				Json.check(units.getAsJsonObject(u), cunitviewer.deepCopy(), true, null);
			
			JsonObject lists = layer.getAsJsonObject("caps");
			for(String l : lists.keySet()) {
				JsonObject caps = lists.getAsJsonObject(l);
				for(String c : caps.keySet()) {
					JsonObject cap = caps.getAsJsonObject(c);
					Json.check(cap, ccap.deepCopy());
				}
			}
		}
		
		//	check if all layers have all known units
		for(String u : unitInfos.keySet())
			checkUnit(cid, ProfileType.VIEWER, u, false);
		
		//	TODO remove "dead" units if present
		
		checkPTimes(ProfileType.VIEWER, cid);
		
		//	only add captain side of configs if acc can play as captain
		if(profile.get("canCaptain").getAsBoolean()) {
			if(!profile.has(ProfileType.CAPTAIN.toString()))
				profile.add(ProfileType.CAPTAIN.toString(), ccaptain.deepCopy());
			else
				Json.check(profile.getAsJsonObject(ProfileType.CAPTAIN.toString()), ccaptain.deepCopy());
			
			JsonObject layerscaptain = profile.getAsJsonObject(ProfileType.CAPTAIN.toString()).getAsJsonObject("layers");
			for(String lay : layerscaptain.keySet()) {
				JsonObject layer = layerscaptain.getAsJsonObject(lay);
				Json.check(layer, clayercaptain.deepCopy(), true, clayercaptainspare.deepCopy());
				
				//	TODO units captain
				JsonObject units = layer.getAsJsonObject("units");
				for(String u : units.keySet())
					Json.check(units.getAsJsonObject(u), cunitcaptain.deepCopy(), true, null);
				
			}
			
			//	TODO unit info captain
			//	TODO remove "dead" units if present
			
			checkPTimes(ProfileType.CAPTAIN, cid);
		}
	}
	
	private static void checkPTimes(ProfileType pt, String cid) {
		UObj con = pt == ProfileType.VIEWER ? ptimesViewer : ptimesCaptain;
		
		JsonObject times = getUObj(cid, con);
		String[] nts = new String[2016];
		Integer s;
		int e;
		ArrayList<String> lids = getLayerIds(cid, pt);
		for(String key : times.keySet()) {
			String lid = times.get(key).getAsString();
			if(!lids.contains(lid))
				continue;
			String[] time = key.split("-");
			s = Integer.parseInt(time[0]);
			e = Integer.parseInt(time[1]);
			while(s <= e)
				nts[s++] = lid;
		}
		
		times = new JsonObject();
		
		for(int i=0; i<2016; i++)
			if(nts[i] == null)
				nts[i] = "(default)";
		
		for(int i=0; i<2016; i++) {
			s = i;
			String lid = nts[i];
			for(i++; i<2016; i++)
				if(!lid.equals(nts[i]))
					break;
			
			times.addProperty(s+"-"+--i, lid);
		}
		
		setUObj(cid, con, times);
	}
	
	
	/**
	 * syncs the specified profile to the other profile
	 * unsyncs if defCid is null
	 * @param cid profile id
	 * @param defCid other profile id
	 */
	public static void syncProfile(String cid, String defCid, ProfileType pt) {
		if(pt == ProfileType.CAPTAIN && !(getPBoo(cid, canCaptain) && (defCid == null || getPBoo(defCid, canCaptain))))
			throw new NotACaptainException();
		
		PStr syncProfile = pt == ProfileType.VIEWER ? syncedViewer : syncedCaptain;
		
		JsonObject profile = getProfile(cid);
		if(defCid == null) {
			defCid = Configs.getPStr(cid, syncProfile);
			
			//	unsync
			setPStr(cid, syncProfile, "(none)");
			JsonObject profilePT = profile.getAsJsonObject(pt.toString());
			
			profilePT.add("layers", profilePT.get("layers").deepCopy());
			profilePT.add("times", profilePT.get("times").deepCopy());
			profilePT.add("unitInfo", profilePT.get("unitInfo").deepCopy());
			
			UniInfoStr fromProfile = pt == ProfileType.VIEWER ? fromViewer : fromCaptain;
			
			//	remove units from other profiles
			for(String uId : getUnitIds(cid, pt, false))
				if(!getUnitInfoStr(cid, uId, fromProfile).equals(cid))
					remUnitId(cid, pt, uId);
			
			//	remove units of the unsynced profile from the other profile
			for(String uId : getUnitIds(defCid, pt, false))
				if(getUnitInfoStr(defCid, uId, fromProfile).equals(cid))
					remUnitId(defCid, pt, uId);
		} else {
			//	sync
			JsonObject def = getProfile(defCid);
			setPStr(cid, syncProfile, defCid);
			
			JsonObject toSyncPT = profile.getAsJsonObject(pt.toString());
			JsonObject defPT = def.getAsJsonObject(pt.toString());
			
			JsonObject toSyncLays = toSyncPT.getAsJsonObject("layers");
			JsonObject defLays = defPT.getAsJsonObject("layers");

			//	merge units and unit info
			for(String lid : defLays.keySet())
				Json.check(defLays.getAsJsonObject(lid).getAsJsonObject("units"),
						toSyncLays.getAsJsonObject(toSyncLays.has(lid) ? lid : "(default)").getAsJsonObject("units"));
			
			JsonObject defUnitInfo = defPT.getAsJsonObject("unitInfo");
			Json.check(defUnitInfo, toSyncPT.getAsJsonObject("unitInfo"));
			
			//	actually syncing
			toSyncPT.add("layers", defLays);
			toSyncPT.add("times", defPT.get("times"));
			toSyncPT.add("unitInfo", defUnitInfo);
		}
	}
	
	public static void save() {
		try {
			NEF.save(path, Json.prettyJson(config));
			File bc = new File(bpath);
			if(bc.exists())
				bc.delete();
		} catch (IOException e) {
			Logger.print("Failed to save configs", Logger.runerr, Logger.error, null, null, true);
		}
	}
	
	public static void saveb() {
		try {
			NEF.save(bpath, Json.prettyJson(config));
		} catch (IOException e) {
			Logger.print("Failed to save configs", Logger.runerr, Logger.error, null, null, true);
		}
	}
	
	
	
	public static class Exportable {
		
		public static class Category {
			@Override
			public String toString() {
				StringBuilder ret = new StringBuilder("{\n");
				for(String key : childs.keySet()) {
					ret.append("   "+key+": ");
					String c = childs.get(key).toString();
					if(c.equals("{\n}"))
						ret.append("{}\n");
					else
						ret.append(c.replace("\n", "\n   ")+"\n");
				}
				for(String key : items.keySet())
					ret.append("   "+key+" = "+items.get(key).add+"\n");
				return ret.append("}").toString();
			}
			public final String name;
			//	null none, false custom, true all
			private Boolean exm = null;
			private final boolean hidden;
			private final Hashtable<String, Category> childs = new Hashtable<>();
			private final Hashtable<String, Category> childs_hidden = new Hashtable<>();
			private final Hashtable<String, Item> items = new Hashtable<>();
			private final Hashtable<String, Item> items_hidden = new Hashtable<>();
			//private final ArrayList<Category> dependencies_cats = new ArrayList<>();
			private final ArrayList<Item> dependencies_items = new ArrayList<>();
			
			
			private Category(String name) {
				this.name = name;
				this.hidden = false;
			}
			
			private Category(String name, boolean hidden) {
				this.name = name;
				this.hidden = hidden;
			}
			
			private void addCategory(Category child) {
				if(child.hidden)
					childs_hidden.put(child.name, child);
				else
					childs.put(child.name, child);
			}
			
			private void addItem(String name, Item item) {
				if(item.hidden)
					items_hidden.put(name, item);
				else
					items.put(name, item);
			}
			
			/*
			private void addDependency(Category cat) {
				if(hidden)
					throw new UnsupportedOperationException("Hidden Categories can not have dependencies");
				if(!cat.hidden)
					throw new IllegalArgumentException("Only a hidden Category can be set as a dependency");
				dependencies_cats.add(cat);
			}
			*/
			
			private void addDependency(Item item) {
				if(hidden)
					throw new UnsupportedOperationException("Hidden Categories can not have dependencies");
				if(!item.hidden)
					throw new IllegalArgumentException("Only a hidden Item can be set as a dependency");
				dependencies_items.add(item);
			}
			
			/**
			 * @return a Hashtable containing all sub categories
			 */
			public Hashtable<String, Category> getSubcategories() {
				return childs;
			}
			
			/**
			 * @return a ArrayList containing all Item Names
			 */
			public ArrayList<String> getItemNames() {
				return new ArrayList<>(items.keySet());
			}
			
			/**
			 * sets if a item should be exported
			 * @param itemName
			 * @param add value
			 */
			public void setExportItem(String itemName, boolean add) {
				items.get(itemName).add = add;
			}
			
			/** 
			 * returns the export mode of this category<br>
			 * if true all items and subcategories will be force added<br>
			 * if false all items and subcategories will be added according to their add value<br>
			 * if null no items or subcategories will be added
			 * @return Boolean
			 */
			public Boolean getExportMode() {
				return exm;
			}
			
			/**
			 * @param itemName the name of the item
			 * @return true if the item will be exported
			 */
			public boolean getItemExportMode(String itemName) {
				return items.get(itemName).add;
			}
			
			/**
			 * sets the export mode<br>
			 * if true all items and subcategories will be force added<br>
			 * if false all items and subcategories will be added according to their add value<br>
			 * if null no items or subcategories will be added
			 * @param b
			 */
			public void setExportCategory(Boolean b) {
				this.exm = b;
			}
			
			/**
			 * checks if anything will be exported in this category
			 * does not take in account parent category
			 */
			private boolean willAnythingBeExported() {
				if(exm == null)
					return false;
				
				if(exm)
					return true;
				
				//	exm must be false (which means custom selection) at this point
				//	does not take into account whether a parent category forces the export of this category
				for(String key : items.keySet())
					if(items.get(key).add)
						return true;
				
				for(String key : childs.keySet())
					if(childs.get(key).willAnythingBeExported())
						return true;
				
				return false;
			}
			
			private void checkForDependencies(boolean force) {
				//System.out.println(name);
				if(exm == null && !force)
					return;
				
				if(exm != null)
					force |= exm;

				if(force || willAnythingBeExported()) {
					for(Item item : dependencies_items)
						item.add = true;
					/*
					for(Category cat : dependencies_cats)
						cat.setExportCategory(true);
					*/
				}
				
				for(String key : childs.keySet())
					childs.get(key).checkForDependencies(force);
			}
			
			private JsonObject getJson(boolean force) {
				if(exm == null && !force)
					return getHiddenJson();
				
				if(exm != null)
					force |= exm;
				
				
				JsonObject ret = new JsonObject();
				
				for(String key : childs.keySet()) {
					JsonObject jo = childs.get(key).getJson(force);
					if(jo != null)
						ret.add(key, jo);
				}
				
				for(String key : items.keySet()) {
					JsonElement je = items.get(key).getJson(force);
					if(je != null)
						ret.add(key, je);
				}
				
				addHiddenJson(ret);
				
				
				return ret.size() == 0 ? null : ret;
			}
			
			private JsonObject getHiddenJson() {

				JsonObject ret = new JsonObject();
				
				for(String key : childs.keySet()) {
					JsonObject jo = childs.get(key).getHiddenJson();
					if(jo != null)
						ret.add(key, jo);
				}
				
				addHiddenJson(ret);
				
				return ret.size() == 0 ? null : ret;
			}
			
			private JsonObject addHiddenJson(JsonObject json) {
				for(String key : childs_hidden.keySet()) {
					JsonObject jo = childs_hidden.get(key).getJson(false);
					if(jo != null)
						json.add(key, jo);
				}
				
				for(String key : items_hidden.keySet()) {
					JsonElement je = items_hidden.get(key).getJson(false);
					if(je != null)
						json.add(key, je);
				}
				
				return json;
			}
			
		}
		
		private static class Item {
			private boolean add = false;
			private final JsonElement value;
			private final boolean hidden;
			private Item(JsonElement value) {
				this.value = value;
				this.hidden = false;
			}
			private Item(JsonElement value, boolean hidden) {
				this.value = value;
				this.hidden = hidden;
			}
			private JsonElement getJson(boolean force) {
				return add || force ? value : null;
			}
		}
		
		private static final HashSet<String> globalConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("needCloseConfirm "
									+ "useMemoryReleaser "
									+ "freeUpMemoryByUsingDrive "
									+ "maxProfileActions"
									).split(" ")));
			}
		};
		private static final HashSet<String> profileConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("cookies"
						).split(" ")));
			}
		};
		private static final HashSet<String> userConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("stats "
						+ "times"
						).split(" ")));
			}
		};
		private static final HashSet<String> layerAllConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("color "
						+ "storeKeyPrios "
						+ "storeEventPrios "
						+ "storeRefresh "
						+ "sleep "
						+ "unitPlaceRetries "
						+ "storeMinGold "
						+ "storeMinKeys "
						+ "storeMinEventcurrency "
						+ "upgradeMinGold "
						+ "unlockMinGold "
						+ "preferRoguesOnTreasureMaps "
						+ "userAgent "
						+ "proxyDomain "
						+ "proxyPort "
						+ "proxyUser "
						+ "proxyPass "
						+ "proxyMandatory "
						+ "unitUpdate "
						+ "raidUpdate "
						+ "mapUpdate "
						+ "storeUpdate "
						+ "skinUpdate "
						+ "questEventRewardsUpdate"
						).split(" ")));
			}
		};
		private static final HashSet<String> layerViewerConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("lockedSlots "
						+ "unitPlaceDelay "
						+ "captainTeam "
						+ "dungeonSlot "
						+ "mapReloadAfterXRetries "
						+ "maxUnitPerRaidMin "
						+ "maxUnitPerRaidMax "
						+ "capInactiveTreshold "
						+ "allowPlaceFirst "
						+ "capsUpdate "
						+ "caps"
						).split(" ")));
			}
		};
		private static final HashSet<String> layerCaptainConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList((""
						).split(" ")));
			}
		};
		private static final HashSet<String> unitAllConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("place "
						+ "placedun "
						+ "upgrade "
						+ "unlock "
						+ "dupe "
						+ "buy "
						+ "chests"
						).split(" ")));
			}
		};
		private static final HashSet<String> unitViewerConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("epic "
						+ "epicdun "
						+ "favOnly "
						+ "markerOnly "
						+ "canVibe "
						+ "dunEpicMode"
						).split(" ")));
			}
		};
		private static final HashSet<String> unitCaptainConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList((""
						).split(" ")));
			}
		};
		private static final HashSet<String> chestViewerConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("minLoy "
						+ "maxLoy "
						+ "enabled "
						+ "minTime "
						+ "maxTime"
						).split(" ")));
			}
		};
		private static final HashSet<String> chestCaptainConfs = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				addAll(Arrays.asList(("weight"
						).split(" ")));
			}
		};
		
		public final Category root = new Category("root");
		
		public Exportable() {
			//	easiest way to copy and unsync
			JsonObject source = Json.parseObj(config.toString());
			
			HashSet<String> unitTypes = new HashSet<>(Unit.getTypesList());
			
			for(String k1 : source.keySet()) {
				if(k1.equals("type") || k1.equals("version"))
					continue;
				if(k1.equals("Global")) {
					Category c1 = new Category(k1);
					JsonObject g = source.getAsJsonObject(k1);
					for(String k2 : g.keySet())
						if(globalConfs.contains(k2))
							c1.addItem(k2, new Item(g.get(k2)));
					root.addCategory(c1);
				} else {
					JsonObject pro = source.getAsJsonObject(k1);
					final String pname = pro.get(Configs.pname.con).getAsString();
					Category c1 = new Category(pname);
					for(String k2 : pro.keySet())
						if(profileConfs.contains(k2))
							c1.addItem(k2, new Item(pro.get(k2)));
					
					addAcc(pro, ProfileType.VIEWER, c1, k1, unitTypes);
					
					if(getPBoo(k1, canCaptain))
						addAcc(pro, ProfileType.CAPTAIN, c1, k1, unitTypes);
					
					root.addCategory(c1);
				}
			}
			//	set default export to custom (false) for root
			root.setExportCategory(false);
		}
		
		private void addAcc(JsonObject pro, ProfileType pt, Category c1, String k1, HashSet<String> unitTypes) {
			Category c2 = new Category(pt.toString());
			JsonObject user = pro.getAsJsonObject(pt.toString());
			for(String k3 : user.keySet())
				if(userConfs.contains(k3))
					c2.addItem(k3, new Item(user.get(k3)));
			
			JsonObject unitInfo = user.getAsJsonObject("unitInfo");
			Category cuinfos = new Category("unitInfo", true);
			for(String key : unitInfo.keySet()) {
				JsonObject ui = unitInfo.getAsJsonObject(key);
				//	filter out potential units from other profiles
				//	happens if the profiles were synced beforehand
				if(!ui.get(fromViewer.con).getAsString().equals(k1))
					continue;
				ui.remove(fromViewer.con);
				cuinfos.addItem(key, new Item(ui, true));
			}
			c2.addCategory(cuinfos);
			
			Category c3 = new Category("layers");
			JsonObject lays = user.getAsJsonObject("layers");
			
			for(String k3 : lays.keySet()) {
				JsonObject lay = lays.getAsJsonObject(k3);
				final String lname = lay.get(lnameViewer.con).getAsString();
				Category c4 = new Category(lname);
				for(String k4 : lay.keySet())
					if(layerAllConfs.contains(k4) || (pt == ProfileType.VIEWER ? layerViewerConfs : layerCaptainConfs).contains(k4))
						c4.addItem(k4, new Item(lay.get(k4)));
				
				Category c5 = new Category("units");
				JsonObject units = lay.getAsJsonObject("units");
				for(String k4 : units.keySet()) {
					boolean isType = unitTypes.contains(k4);
					
					if(!isType && !cuinfos.items_hidden.containsKey(k4))
						continue;
					
					Category c6;
					//	if the unit gets exported, the unit id for it has to be exported too
					if(isType) {
						c6 = new Category(k4);
					} else {
						c6 = new Category(k4+"  "+unitInfo.getAsJsonObject(k4).get(typeViewer.con).getAsString());
						c6.addDependency(cuinfos.items_hidden.get(k4));
					}
					JsonObject u = units.getAsJsonObject(k4);
					
					c6.addItem("spec", new Item(u.get("spec")));
					
					Category c7 = new Category("opt");
					JsonObject opt = u.getAsJsonObject("opt");
					for(String k5 : opt.keySet())
						if(unitAllConfs.contains(k5) || (pt == ProfileType.VIEWER ? unitViewerConfs : unitCaptainConfs).contains(k5))
							c7.addItem(k5, new Item(opt.get(k5)));
					
					
					c6.addCategory(c7);
					c5.addCategory(c6);
				}
				c4.addCategory(c5);
				
				Category c6 = new Category("chests");
				JsonObject chests = lay.getAsJsonObject("chests");
				for(String k4 : chests.keySet()) {
					Category c7 = new Category(k4);
					JsonObject c = chests.getAsJsonObject(k4);
					for(String k5 : c.keySet())
						if((pt == ProfileType.VIEWER ? chestViewerConfs : chestCaptainConfs).contains(k5))
							c7.addItem(k5, new Item(c.get(k5)));
					c6.addCategory(c7);
				}
				c4.addCategory(c6);
				
				c3.addCategory(c4);
			}
			c2.addCategory(c3);
			
			c1.addCategory(c2);
		}
		
		public void export(String path) throws IOException {
			root.checkForDependencies(false);
			JsonObject ex = root.getJson(false);
			ex.addProperty("version", configVersion);
			ex.addProperty("type", "export");
			NEF.save(path, Json.prettyJson(ex));
		}
	}
	
	
	
	public static class Importable {
		public static class NoCookiesException extends Exception {
			private static final long serialVersionUID = 1L;
			public NoCookiesException() {
				super("Profile has no Cookies");
			}
		}
		
		public class Profile {
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder(name);
				for(ProfileType pt : ProfileType.values())
					sb.append(" | "+pt.toString()+" lids="+(aLids.get(pt).toString().replaceFirst(Pattern.quote(" - "+name)+"$", "")));
				return sb.toString();
			}
			
			private Hashtable<ProfileType, LinkedList<String>> aLids = new Hashtable<>(2);
			private JsonObject pro;
			public final String name;
			private boolean add = false;
			private String new_name;
			public Profile(JsonObject pro, final String name) throws NoCookiesException {
				this.pro = pro;
				this.name = name;
				this.new_name = name;
				for(ProfileType pt : ProfileType.values()) {
					JsonElement u_ = pro.get(pt.toString());
					if(u_ == null)
						continue;
					
					JsonElement lays_ = u_.getAsJsonObject().remove("layers");
					if(lays_ == null)
						continue;

					//	add individual layers of profile to lays 
					LinkedList<String> lids = new LinkedList<>();
					JsonObject layers = lays_.getAsJsonObject();
					for(String key : layers.keySet()) {
						final String ilid = key + " - " + name;
						lids.add(ilid);
						lays.get(pt).put(ilid, new Layer(layers.getAsJsonObject(key), key, pt));
					}
					aLids.put(pt, lids);
				}
				
				//	if profile does not have cookies it can't be added as profile
				if(!pro.has("cookies"))
					throw new NoCookiesException();
			}
			
			public JsonObject getContent(String ncid) {
				for(ProfileType pt : ProfileType.values()) {
					LinkedList<String> lids = aLids.get(pt);
					if(lids == null || lids.size() == 0)
						continue;
					
					Hashtable<String, Layer> uLays = lays.get(pt);
					JsonObject layers = new JsonObject();
					
					while(lids.size() > 0)
						layers.add(UUID.randomUUID().toString(), convertUnitObject(pt, uLays.get(lids.pop()).getContentFull(false, null, null), null));
					
					JsonObject acc = pro.getAsJsonObject(pt.toString());
					acc.add("layers", layers);
					//	adjust from value in unit info to new cid
					if(acc.has("unitInfo")) {
						JsonObject unitInfo = acc.getAsJsonObject("unitInfo");
						for(String u : unitInfo.keySet())
							unitInfo.getAsJsonObject(u).addProperty(fromViewer.con, ncid);
					}
					
					pro.add(pt.toString(), acc);
				}
				String name = new_name == null || new_name.equals("") ? this.name : new_name;
				
				while(isPNameTaken(name))
					name += "_"+Maths.ranString(3);
				
				pro.addProperty("name", name);
				return pro.deepCopy();
			}
		}
		
		public static class Layer {
			public static interface MergeConsumer {
				public void merge(String cid, String lid, JsonObject content);
			}
			public static interface AddConsumer {
				public void add(String cid, JsonObject content);
			}
			private final JsonObject content;
			public final String name;
			public final ProfileType pt;
			private HashSet<String> mergeTo = new HashSet<>();
			private Hashtable<String, HashSet<String>> addTo = new Hashtable<>();
			public Layer(JsonObject content, String name, ProfileType pt) {
				this.name = name;
				this.content = content;
				this.pt = pt;
			}
			public JsonObject getContent() {
				return content.deepCopy();
			}
			public JsonObject getContentFull(boolean checkDuplicateName, String cid, String new_name) {
				JsonObject c = getContent();
				String name = new_name == null || new_name.equals("") ? this.name : new_name;
				while(checkDuplicateName && isLNameTaken(cid, pt, name))
					name += "_"+Maths.ranString(3);
				
				c.addProperty("name", name);
				
				if(!c.has("color")) {
					Random r = new Random();
					c.addProperty("color", new Color(r.nextInt(0, 256), r.nextInt(0, 256), r.nextInt(0, 256)).getRGB());
				}
				return c;
			}
			public boolean setMergeTo(String cid, String lid, boolean b) {
				final String id = cid + ">>" + lid;
				if(b == mergeTo.contains(id))
					return false;
				if(b)
					mergeTo.add(id);
				else
					mergeTo.remove(id);
				return true;
			}
			public void forEachMerge(MergeConsumer mc) {
				mergeTo.forEach(s -> {
					String[] ss = s.split(">>");
					mc.merge(ss[0], ss[1], convertUnitObject(pt, getContent(), ss[0]));
				});
			}
			public boolean setAddTo(String cid, boolean b, String new_name) {
				HashSet<String> set = addTo.get(cid);
				if(set == null) {
					set = new HashSet<>();
					addTo.put(cid, set);
				}
				
				if(b == set.contains(new_name))
					return false;
				
				if(b)
					set.add(new_name);
				else
					set.remove(new_name);
				return true;
			}
			public void forEachAdd(AddConsumer ac) {
				addTo.forEach((cid, new_names) ->
								new_names.forEach(new_name ->
										ac.add(cid, convertUnitObject(pt, getContentFull(true, cid, new_name), cid))));
			}
		}
		
		private static JsonObject convertUnitObject(ProfileType pt, JsonObject lay, String cid) {
			JsonObject units = lay.getAsJsonObject("units");
			ArrayList<String> unitIds = cid == null ? new ArrayList<>() : getUnitIds(cid, pt, true);
			for(String key : new ArrayList<>(units.keySet())) {
				JsonObject u = units.remove(key).getAsJsonObject();
				if(!unitIds.contains(key))
					continue;
				units.add(key.split("  ")[0], u);
			}
			return lay;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("{\n  layIds: {");
			for(ProfileType pt : ProfileType.values()) {
				sb.append("\n    "+pt.toString()+": {");
				Hashtable<String, Layer> ht = lays.get(pt);
				for(String key : ht.keySet())
					sb.append("\n      "+key);
				sb.append("\n    }");
			}
			sb.append("\n  }\n  profiles: {");
			for(String key : pros.keySet())
				sb.append("\n    "+pros.get(key).toString());
			sb.append("\n  }\n  Global: "+(glob == null ? "null" : glob.keySet().toString()));
			return sb.append("\n}").toString();
		}
		
		//	global options or sauce if old config
		private JsonObject glob;
		//	profiles
		private Hashtable<String, Profile> pros = new Hashtable<>();
		//	layers
		private Hashtable<ProfileType, Hashtable<String, Layer>> lays = new Hashtable<>();
		
		public final boolean isCompatibleOldConfig;
		
		private boolean mergeGlobal = false;
		
		/**
		 * @param path path to the file
		 * @throws IOException
		 * @throws IllegalConfigTypeException
		 * @throws IllegalConfigVersionException
		 */
		public Importable(String path) throws IOException, IllegalConfigTypeException, IllegalConfigVersionException {
			JsonObject source = Json.parseObj(NEF.read(path));
			
			JsonElement cv = source.remove("version");
			if(cv == null)
				throw new IllegalConfigVersionException(-1, configVersion);
			
			final int version = cv.getAsInt();
			isCompatibleOldConfig = version != configVersion;
			if(isCompatibleOldConfig) {
				if(version != configVersion-1)
					glob = source;
				else
					throw new IllegalConfigVersionException(version, configVersion);
				return;
			}
			
			JsonElement ct = source.remove("type");
			if(ct == null)
				throw new IllegalConfigTypeException("null", "config");
			
			if(!ct.getAsString().equals("export"))
				throw new IllegalConfigTypeException(ct.getAsString(), "export");

			
			JsonElement jeglob = source.remove("Global");
			glob = jeglob == null ? null : jeglob.getAsJsonObject();
			for(ProfileType pt : ProfileType.values())
				lays.put(pt, new Hashtable<>());
			
			for(String key : source.keySet())
				try {
					pros.put(key, new Profile(source.getAsJsonObject(key), key));
				} catch (NoCookiesException e) {}
		}
		
		/**
		 * @return true if global options can be imported
		 */
		public boolean hasGlobalOptions() {
			return glob != null && glob.size() > 0;
		}
		
		
		/**
		 * @return an ArrayList with all Profile names that can be imported
		 */
		public ArrayList<String> getProfileNames() {
			return new ArrayList<>(pros.keySet());
		}
		
		/**
		 * @return an String Array with all Profile names that can be imported
		 */
		public String[] getProfileNamesArray() {
			return pros.keySet().toArray(new String[pros.size()]);
		}
		
		/**
		 * @return the amount of Profiles that can be imported
		 */
		public int getProfileCount() {
			return pros.size();
		}
		
		/**
		 * a ArrayList with all layer names that can be imported
		 * @param pt
		 * @return
		 */
		public ArrayList<String> getLayerNames(ProfileType pt) {
			return new ArrayList<>(lays.get(pt).keySet());
		}
		
		/**
		 * sets if global options should be merged
		 * @param b
		 */
		public void setMergeGlobal(boolean b) {
			mergeGlobal = b;
		}
		
		/**
		 * sets if a profile should be imported
		 * @param pname profile name
		 * @param b
		 */
		public void setAddProfile(String pname, boolean b, String new_name) {
			Profile pro = pros.get(pname);
			pro.add = b;
			pro.new_name  = new_name;
		}
		
		/**
		 * sets if a layer should be merged to an already existing layer
		 * @param pt ProfileType
		 * @param lname layer name to import
		 * @param cid profile id from existing profile
		 * @param lid layer id from existing layer
		 * @param b
		 * @return true if state changed
		 */
		public boolean setMergeLayer(ProfileType pt, String lname, String cid, String lid, boolean b) {
			return lays.get(pt).get(lname).setMergeTo(cid, lid, b);
		}
		
		/**
		 * sets if a layer should be added to a already existing profile
		 * @param pt ProfileType
		 * @param lname layer name to import
		 * @param cid profile id from existing profile
		 * @param b
		 * @param new_name the new name for this layer
		 * @return true if state changed
		 */
		public boolean setAddLayer(ProfileType pt, String lname, String cid, boolean b, String new_name) {
			return lays.get(pt).get(lname).setAddTo(cid, b, new_name);
		}
		
		/**
		 * imports and merges everything set.
		 * The Importable instance should not be used anymore after this call.
		 */
		public void importAndMerge() {
			if(isCompatibleOldConfig)
				importOld();
			else {
				if(glob != null && mergeGlobal)
					Json.override(config.getAsJsonObject("Global"), glob);
				
				for(String key : pros.keySet()) {
					Profile p = pros.get(key);
					if(p.add) {
						String uuid = ranUUIDProfile();
						config.add(uuid, p.getContent(uuid));
					}
					
				}
				
				for(ProfileType pt : ProfileType.values()) {
					Hashtable<String, Layer> ls = lays.get(pt);
					ls.forEach((key, l) -> {
						l.forEachMerge((cid, lid, content) -> Json.override(Json.get(config, new ArrayList<>(Arrays.asList(cid, pt.toString(), "layers", lid))).getAsJsonObject(), content));
						l.forEachAdd((cid, content) -> Json.get(config, new ArrayList<>(Arrays.asList(cid, pt.toString(), "layers"))).getAsJsonObject().add(ranUUIDLayer(cid, pt), content));
					});
				}
			}
			
			checkAll();
		}
		
		private void importOld() {
			//	merge global
			if(glob.has("Global"))
				config.add("Global", glob.remove("Global"));
			
			for(String k1 : glob.keySet()) {
				JsonObject pro_ = glob.getAsJsonObject(k1);
				//	no cookies => no profile
				if(!pro_.has("cookies"))
					continue;
				
				JsonObject pro = new JsonObject();
				
				//	add stuff with different path
				JsonObject vie = new JsonObject();
				if(pro_.has("stats"))
					vie.add("stats", pro_.remove("stats"));
				if(pro_.has("times"))
					vie.add("times", pro_.remove("times"));
				if(pro_.has("layers")) {
					JsonObject layers = pro_.remove("layers").getAsJsonObject();
					for(String lay : layers.keySet()) {
						JsonObject layer = layers.getAsJsonObject(lay);
						if(layer.has("units")) {
							JsonObject units = layer.getAsJsonObject("units");
							for(String us : units.keySet()) {
								JsonObject u_new = new JsonObject();
								u_new.add("opt", units.getAsJsonObject(us));
								units.add(us, u_new);
							}
						}
						if(layer.has("unitPlaceDelay")) {
							JsonObject upd = layer.getAsJsonObject("unitPlaceDelay");
							for(String mm : upd.keySet())
								layer.add("unitPlaceDelayM"+mm.substring(1), upd.get(mm));
						}
					}
					
					vie.add("layers", layers);
				}
				pro.add(ProfileType.VIEWER.toString(), vie);
				
				//	add everything else
				for(String k2 : pro_.keySet())
					pro.add(k2, pro_.get(k2));
				
				//	prevent duplicate names
				final String name = pro.get("name").getAsString();
				String nn = name;
				while(isPNameTaken(nn))
					nn = name + "_" + Maths.ranString(3);
				pro.addProperty("name", nn);
				
				//	add profile to config
				config.add(ranUUIDProfile(), pro);
			}
		}
	}
	
	
	private static int sign(int in) {
		return (int) Math.signum(in);
	}
	
	private static String ranUUIDProfile() {
		String uuid;
		do
			uuid = UUID.randomUUID().toString();
		while(config.has(uuid));
		return uuid;
	}
	
	private static String ranUUIDLayer(String cid, ProfileType pt) {
		JsonObject lays = config.getAsJsonObject(cid)
								.getAsJsonObject(pt.toString())
								.getAsJsonObject("layers");
		String uuid;
		do
			uuid = UUID.randomUUID().toString();
		while(lays.has(uuid));
		return uuid;
	}
	
}