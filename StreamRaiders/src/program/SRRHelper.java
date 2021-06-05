package program;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.JsonParser;
import program.QuestEventRewards.Quest;
import program.Run.SilentException;
import program.SRR.NoInternetException;
import program.SRR.NotAuthorizedException;
import program.SRR.OutdatedDataException;
import program.Store.C;

public class SRRHelper {

	public String getRaidPlan(int index) throws NoInternetException {
		return req.getRaidPlan(req.getRaidPlan(raids[index].get(SRC.Raid.raidId)));
	}
	
	
	private SRR req = null;
	private Unit[] units = new Unit[0];
	private Raid[] raids = new Raid[0];
	private Store store = null;
	private Map map = null;
	private QuestEventRewards qer = new QuestEventRewards();
	
	public SRRHelper(String cookies, String clientVersion) throws Exception {
		try {
			req = new SRR(cookies, clientVersion);
			updateUnits();
			return;
		} catch (OutdatedDataException e1) {
			updateDataPath(e1.getDataPath(), req);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			req = new SRR(cookies, clientVersion);
			updateUnits();
		}
	}
	
	public String reload() throws Exception {
		String ret = null;
		try {
			ret = req.reload();
			updateUnits();
		} catch (OutdatedDataException e1) {
			updateDataPath(e1.getDataPath(), req);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			ret = req.reload();
			updateUnits();
		}
		return ret;
	}
	
	synchronized private static void updateDataPath(String dataPath, SRR req) throws NoInternetException, NotAuthorizedException {
		if(!StreamRaiders.get("data").equals(dataPath)) {
			JsonObject data = JsonParser.parseObj(SRR.getData(dataPath)).getAsJsonObject("sheets");
			StreamRaiders.set("obstacles", data.getAsJsonObject("Obstacles").toString());
			StreamRaiders.set("quests", data.getAsJsonObject("Quests").toString());
			StreamRaiders.set("mapNodes", data.getAsJsonObject("MapNodes").toString());
			StreamRaiders.set("events", data.getAsJsonObject("Events").toString());
			StreamRaiders.set("specsRaw", data.getAsJsonObject("Specialization").toString());
			StreamRaiders.set("store", data.getAsJsonObject("Store").toString());
			GuideContent.saveChestRewards(data);
			GuideContent.gainStats(data.getAsJsonObject("Units"));
			StreamRaiders.set("data", dataPath);
			StreamRaiders.save();
		}
		try {
			if(req != null)
				req.reload();
		} catch (OutdatedDataException e) {
			StreamRaiders.log("SRRHelper -> updateDataPath: err=failed to update data path", e);
		}
	}

	public SRR getSRR() {
		return req;
	}
	
	
	public void updateQuests() throws NoInternetException {
		qer.updateQuests(req);
	}
	
	public Quest[] getClaimableQuests() {
		return qer.getClaimableQuests();
	}
	
	public String claimQuest(Quest quest) throws NoInternetException {
		return quest.claim(req);
	}
	
	public String[] getNeededUnitTypesForQuests() {
		return qer.getNeededUnitTypesForQuests();
	}
	
	public void updateEvent() throws NoInternetException {
		qer.updateEvent(req);
	}
	
	public boolean isEvent() {
		return qer.isEvent();
	}
	
	public int getEventTier() {
		return qer.getEventTier();
	}
	
	public boolean hasBattlePass() {
		return qer.hasBattlePass();
	}
	
	public String collectEvent(int p, boolean battlePass) throws NoInternetException {
		return qer.collectEvent(p, battlePass, req);
	}
	
	public String placeUnit(Raid raid, Unit unit, boolean epic, int[] pos, boolean onPlanIcon) throws NoInternetException {
		String atr = req.addToRaid(raid.get(SRC.Raid.raidId),
				createPlacementData(unit, epic, map.getAsSRCoords(epic, pos), onPlanIcon));
		
		try {
			JsonElement je = JsonParser.parseObj(atr).get(SRC.errorMessage);
			if(je.isJsonPrimitive()) return je.getAsString();
		} catch (NullPointerException e) {
			StreamRaiders.log("SRRHelper -> placeUnit: err=failed to place Unit, atr=" + atr == null ? "null" : atr, e);
		}
		
		return null;
	}
	
	public Map getMap() {
		return map;
	}
	
	
	public static class PvPException extends Exception {
		private static final long serialVersionUID = 1L;
		public PvPException () {
			super("This is a pvp raid");
		}
	}
	
	public static class DungeonException extends Exception {
		private static final long serialVersionUID = 1L;
		public DungeonException () {
			super("This is a dungeon raid");
		}
	}
	
	public void loadMap(Raid raid) throws PvPException, NoInternetException, DungeonException {
		String node = raid.get(SRC.Raid.nodeId);
		if(node.contains("pvp")) throw new PvPException();
		String raidplan = req.getRaidPlan(raid.get(SRC.Raid.raidId));
		try {
			JsonElement je = JsonParser.parseObj(raidplan).get("data");
			String mapName = raid.get(SRC.Raid.battleground);
			map = new Map(JsonParser.parseObj(req.getMapData(mapName)),
					JsonParser.parseArr(raid.get(SRC.Raid.placementsSerialized)),
					JsonParser.parseArr(raid.get(SRC.Raid.users)),
					(je.isJsonObject() ? je.getAsJsonObject().getAsJsonObject("planData") : null), mapName);
		} catch (NullPointerException e) {
			StreamRaiders.log("SRRHelper -> loadMap: raidplan=" + raidplan, e);
			throw new SilentException();
		}
		if(node.contains("dungeon")) throw new DungeonException();
	}
	
	public boolean testPos(boolean epic, int x, int y) {
		return map.testPos(epic, new int[] {x, y});
	}
	
	public Raid getRaid(String con, String arg) {
		for(int i=0; i<raids.length; i++) 
			if(raids[i].get(con).equals(arg)) 
				return raids[i];
		
		return null;
	}
	
	private boolean whenNotLive = true;
	private int treshold = 10;
	
	public void setOfflineOptions(boolean whenNotLive, int treshold) {
		this.whenNotLive = whenNotLive;
		this.treshold = treshold;
	}
	
	public Raid[] getRaids() {
		return raids;
	}
	
	private String serverTime = null;
	
	public String getServerTime() {
		return serverTime;
	}
	
	public Raid[] getRaids(int con) throws NoInternetException, NotAuthorizedException {
		serverTime = updateRaids();
		Raid[] ret = new Raid[0];
		for(int i=0; i<raids.length; i++) {
			switch(con) {
			case SRC.Helper.canPlaceUnit:
				if(raids[i].canPlaceUnit(serverTime)) ret = add(ret, raids[i]);
				break;
			case SRC.Helper.isReward:
				if(raids[i].isReward()) ret = add(ret, raids[i]);
				break;
			case SRC.Helper.isOffline:
				if(raids[i].isOffline(serverTime, whenNotLive, treshold)) ret = add(ret, raids[i]);
				break;
			case SRC.Helper.all:
				ret = add(ret, raids[i]);
				break;
			}
		}
		return ret;
	}
	
	public void remRaid(String captainId) throws NoInternetException {
		req.leaveCaptain(captainId);
	}
	
	public void addRaid(JsonObject captain, String userSortIndex) throws NoInternetException {
		req.addPlayerToRaid(captain.getAsJsonPrimitive("userId").getAsString(), userSortIndex);
	}
	
	public void switchRaid(JsonObject captain, String userSortIndex) throws NoInternetException, NotAuthorizedException {
		updateRaids();
		for(int i=0; i<raids.length; i++) {
			if(raids[i].get(SRC.Raid.userSortIndex).equals(userSortIndex)) {
				remRaid(raids[i].get(SRC.Raid.captainId));
				addRaid(captain, userSortIndex);
			}
		}
	}
	
	public String updateRaids() throws NoInternetException, NotAuthorizedException {
		JsonObject jo = JsonParser.parseObj(req.getActiveRaidsByUser());
		try {
			JsonArray rs = jo.getAsJsonArray("data");
			raids = new Raid[0];
			for(int i=0; i<rs.size(); i++) {
				raids = add(raids, new Raid(rs.get(i).getAsJsonObject()));
				raids[i].addNode(raids[i].get(SRC.Raid.nodeId));
			}
			return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
		} catch (ClassCastException e) {
			JsonElement je = jo.get(SRC.errorMessage);
			if(je.isJsonPrimitive() && je.getAsString().equals("Game data mismatch.")) {
				updateDataPath(jo.getAsJsonObject("info").getAsJsonPrimitive("dataPath").getAsString(), req);
				return updateRaids();
			} else {
				StreamRaiders.log("SRRHelper -> updateRaids: jo=" + jo, null);
				throw new Run.SilentException();
			}
		}
		
	}
	
	private int pages = 0;
	
	public int getPagesFromLastSearch() {
		return pages;
	}
	
	public JsonArray search(int page, int resultsPerPage, boolean fav, boolean live, String mode, boolean searchForCaptain, String name) throws NoInternetException {
		JsonObject rawd = JsonParser.parseObj(req.getCaptainsForSearch(page, resultsPerPage, fav, live, mode, searchForCaptain, name));
		if(rawd == null) {
			StreamRaiders.log("SRRHelper -> search: rawd=null", null);
			return new JsonArray();
		}
		
		JsonObject raw = rawd.getAsJsonObject("data");
		
		pages = raw.getAsJsonPrimitive("lastPage").getAsInt();
		
		JsonArray loyalty = raw.getAsJsonArray("pveLoyalty");
		JsonArray captains = raw.getAsJsonArray("captains");
		
		for(int i=0; i<loyalty.size(); i++) {
			captains.get(i).getAsJsonObject().addProperty(SRC.Raid.pveWins, loyalty.get(i).getAsJsonObject().getAsJsonPrimitive(SRC.Raid.pveWins).getAsInt());
			captains.get(i).getAsJsonObject().addProperty(SRC.Raid.pveLoyaltyLevel, loyalty.get(i).getAsJsonObject().getAsJsonPrimitive(SRC.Raid.pveLoyaltyLevel).getAsInt());
			captains.get(i).getAsJsonObject().addProperty(SRC.Raid.captainId, loyalty.get(i).getAsJsonObject().getAsJsonPrimitive(SRC.Raid.captainId).getAsString());
		}
		
		
		return captains;
	}
	
	public String setFavorite(JsonObject captain, boolean b) throws NoInternetException {
		JsonElement err = JsonParser.parseObj(req.updateFavoriteCaptains(captain.getAsJsonPrimitive(SRC.Raid.captainId).getAsString(), b))
				.get(SRC.errorMessage);
		if(err.isJsonPrimitive()) 
			return err.getAsString();
		
		return null;
	}
	
	public String updateUnits() throws NoInternetException {
		String r = req.getUserUnits();
		JsonObject jo = JsonParser.parseObj(r);
		if(jo == null) {
			StreamRaiders.log("SRRHelper -> updateUnits: jo=null, r="+r, null);
			throw new Run.SilentException();
		}
		JsonArray u = jo.getAsJsonArray("data");
		units = new Unit[0];
		for(int i=0; i<u.size(); i++) units = add(units, new Unit(u.get(i).getAsJsonObject()));
		return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
	}
	
	public void reloadStore() throws NoInternetException {
		store = new Store(req);
	}
	
	public JsonArray getStoreItems(int con) {
		return store.getStoreItems(con);
	}
	
	public void refreshStore() throws NoInternetException {
		store.refreshStore(req);
	}
	
	public int getStoreRefreshCount() {
		return store.getStoreRefreshCount();
	}
	
	public Integer getCurrency(C con) {
		return store == null ? null : store.getCurrency(con);
	}
	
	public String buyItem(JsonObject item) throws NoInternetException {
		return store.buyItem(item, req);
	}
	
	public String buyDungeonChest() throws NoInternetException {
		return store.buyDungeonChest(getServerTime(), req);
	}
	
	public Unit[] getUnits(int con) throws NoInternetException {
		String serverTime = updateUnits();
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			switch(con) {
			case SRC.Helper.canPlaceUnit:
				if(units[i].isAvailable(serverTime)) ret = add(ret, units[i]);
				break;
			case SRC.Helper.all:
				ret = add(ret, units[i]);
				break;
			case SRC.Helper.canUpgradeUnit:
				return store.getUpgradeableUnits(units);
			case SRC.Helper.canUnlockUnit:
				return store.getUnlockableUnits(units);
			}
		}
		return ret;
	}
	
	
	private static final String[] dungeons_units_dis = "knockedUnits deadUnits exhaustedUnits".split(" ");
	
	public Unit[] getUnitsDungeons(Raid raid) throws NoInternetException {
		updateUnits();
		Unit[] ret = new Unit[0];
		Unit[] all = getUnits(SRC.Helper.all);
		
		JsonObject data = JsonParser.parseObj(req.getUserDungeonInfoForRaid(raid.get(SRC.Raid.raidId))).getAsJsonObject("data");
		
		String[] bnd = new String[0];
		
		for(String key : dungeons_units_dis) {
			JsonElement ku = data.get(key);
			if(ku != null && ku.isJsonPrimitive()) {
				String sku = ku.getAsString();
				if(!sku.equals(""))
					bnd = merge(bnd, sku.split(","));
			}
		}
		
		JsonArray pmnt = JsonParser.parseArr(raid.get(SRC.Raid.placementsSerialized));
		if(pmnt != null) {
			for(int i=0; i<pmnt.size(); i++) {
				JsonObject jo = pmnt.get(i).getAsJsonObject();
				bnd = add(bnd, jo.getAsJsonPrimitive(SRC.Unit.unitId).getAsString());
			}
		}
		
		
		List<String> aban = Arrays.asList(bnd);
		
		for(int i=0; i<all.length; i++) {
			if(aban.contains(all[i].get(SRC.Unit.unitId)))
				continue;
			
			ret = add(ret, all[i]);
		}
		
		return ret;
	}
	
	public String unlockUnit(Unit unit) throws NoInternetException {
		return store.unlockUnit(unit.get(SRC.Unit.unitType), unit.isDupe(), req);
	}
	
	public String upgradeUnit(Unit unit, String specUID) throws NoInternetException {
		return store.canUpgradeUnit(unit) ? store.upgradeUnit(unit, req, specUID) : "cant upgrade unit";
	}
	
	public Unit[] getUnits(String con, String arg) throws NoInternetException {
		updateUnits();
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) 
			if(units[i].get(con).equals(arg)) 
				ret = add(ret, units[i]);
		
		return ret;
	}
	
	
	public String createPlacementData(Unit unit, boolean epic, double[] pos, boolean onPlanIcon) {
		JsonObject ret = new JsonObject();
		ret.addProperty("raidPlacementsId", "");
		ret.addProperty("userId", req.getUserId());
		if(epic) {
			ret.addProperty("CharacterType", "epic"+unit.get(SRC.Unit.unitType)+unit.get(SRC.Unit.level));
		} else {
			ret.addProperty("CharacterType", unit.get(SRC.Unit.unitType)+unit.get(SRC.Unit.level));
		}
		ret.addProperty("X", pos[0]);
		ret.addProperty("Y", pos[1]);
		
		String skin = unit.get(SRC.Unit.skin);
		if(skin != null) {
			ret.addProperty("skin", skin);
		} else {
			ret.addProperty("skin", "");
		}
		ret.addProperty("unitId", unit.get(SRC.Unit.unitId));
		
		String suid = unit.get(SRC.Unit.specializationUid);
		if(suid != null) {
			ret.addProperty("specializationUid", suid);
		} else {
			ret.addProperty("specializationUid", "");
		}
		
		ret.addProperty("team", "Ally");
		ret.addProperty("onPlanIcon", onPlanIcon);
		ret.addProperty("isSpell", false);
		ret.addProperty("stackRaidPlacementsId", "0");
		
		return ret.toString();
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
	
	private static <T>T[] merge(T[] arr1, T[] arr2) {
		return ArrayUtils.addAll(arr1, arr2);
	}
	
	
}
