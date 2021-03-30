package program;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.JsonParser;
import program.QuestEventRewards.Quest;
import program.SRR.NoInternetException;
import program.SRR.OutdatedDataException;

public class SRRHelper {

	
	
	private SRR req = null;
	private Unit[] units = new Unit[0];
	private Raid[] raids = new Raid[0];
	private Store store = null;
	private Map map = null;
	private QuestEventRewards qer = new QuestEventRewards();
	
	public SRRHelper(String cookies, String clientVersion) throws OutdatedDataException, NoInternetException {
		try {
			req = new SRR(cookies, clientVersion);
			updateUnits();
			return;
		} catch (OutdatedDataException e1) {
			updateDataPath(e1.getDataPath());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			req = new SRR(cookies, clientVersion);
			updateUnits();
		}
	}
	
	public String reload() throws OutdatedDataException, NoInternetException {
		String ret = null;
		try {
			ret = req.reload();
			updateUnits();
		} catch (OutdatedDataException e1) {
			updateDataPath(e1.getDataPath());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			ret = req.reload();
			updateUnits();
		}
		return ret;
	}
	
	private void updateDataPath(String dataPath) {
		JsonObject data = JsonParser.json(SRR.getData(dataPath)).getAsJsonObject("sheets");
		StreamRaiders.set("obstacles", data.getAsJsonObject("Obstacles").toString());
		StreamRaiders.set("quests", data.getAsJsonObject("Quests").toString());
		StreamRaiders.set("data", dataPath);
		StreamRaiders.save();
	}

	public SRR getSRR() {
		return req;
	}
	
	
	public void updateQuests() {
		qer.updateQuests(req);
	}
	
	public Quest[] getClaimableQuests() {
		return qer.getClaimableQuests();
	}
	
	public String claimQuest(Quest quest) {
		return quest.claim(req);
	}
	
	public String[] getNeededUnitTypesForQuests() {
		return qer.getNeededUnitTypesForQuests();
	}
	
	public void updateEvent() {
		qer.updateEvent(req);
	}
	
	public int getEventTier() {
		return qer.getEventTier();
	}
	
	public boolean hasBattlePass() {
		return qer.hasBattlePass();
	}
	
	public String collectEvent(int p, boolean battlePass) {
		return qer.collectEvent(p, battlePass, req);
	}
	
	public String placeUnit(Raid raid, Unit unit, boolean epic, int x, int y) {
		JsonElement je = JsonParser.json(req.addToRaid(raid.get(SRC.Raid.raidId),
					createPlacementData(unit, epic, map.getAsSRCoords(new int[] {x, y}))))
				.get("errorMessage");
		
		if(je.isJsonPrimitive()) return je.getAsString();
		
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
	
	public void loadMap(Raid raid) throws PvPException {
		if(raid.get(SRC.Raid.nodeId).contains("pvp")) throw new PvPException();
		map = new Map(JsonParser.json(req.getMapData(raid.get(SRC.Raid.battleground))), JsonParser.jsonArr(raid.get(SRC.Raid.placementsSerialized)));
	}
	
	public boolean testPos(boolean epic, int x, int y) {
		return map.testPos(epic, new int[] {x, y});
	}
	
	public double[] getSRPos(int x, int y) {
		return map.getAsSRCoords(new int[] {x, y});
	}
	
	
	public Raid getRaid(String con, String arg) {
		for(int i=0; i<raids.length; i++) {
			if(raids[i].get(con).equals(arg)) {
				return raids[i];
			}
		}
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
	
	public Raid[] getRaids(int arg) {
		String serverTime = updateRaids();
		Raid[] ret = new Raid[0];
		for(int i=0; i<raids.length; i++) {
			switch(arg) {
			case SRC.Helper.canPlaceUnit:
				if(raids[i].canPlaceUnit(serverTime)) {
					ret = add(ret, raids[i]);
				}
				break;
			case SRC.Helper.isReward:
				if(raids[i].isReward()) {
					ret = add(ret, raids[i]);
				}
				break;
			case SRC.Helper.isOffline:
				if(raids[i].isOffline(serverTime, whenNotLive, treshold)) {
					ret = add(ret, raids[i]);
				}
				break;
			case SRC.Helper.all:
				ret = add(ret, raids[i]);
				break;
			}
		}
		return ret;
	}
	
	public void remRaid(String captainId) {
		req.leaveCaptain(captainId);
	}
	
	public void addRaid(JsonObject captain, String userSortIndex) {
		req.addPlayerToRaid(captain.getAsJsonPrimitive("userId").getAsString(), userSortIndex);
	}
	
	public void switchRaid(JsonObject captain, String userSortIndex) {
		updateRaids();
		for(int i=0; i<raids.length; i++) {
			if(raids[i].get(SRC.Raid.userSortIndex).equals(userSortIndex)) {
				remRaid(raids[i].get(SRC.Raid.captainId));
				addRaid(captain, userSortIndex);
			}
		}
	}
	
	public String updateRaids() {
		JsonObject jo = JsonParser.json(req.getActiveRaidsByUser());
		JsonArray rs = jo.getAsJsonArray("data");
		raids = new Raid[0];
		for(int i=0; i<rs.size(); i++) {
			raids = add(raids, new Raid(rs.get(i).getAsJsonObject()));
		}
		return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
	}
	
	
	public JsonArray search(int page, int resultsPerPage, boolean fav, boolean live, boolean searchForCaptain, String name) {
		JsonObject raw = JsonParser.json(req.getCaptainsForSearch(page, resultsPerPage, fav, live, searchForCaptain, name)).getAsJsonObject("data");
		
		JsonArray loyalty = raw.getAsJsonArray("pveLoyalty");
		JsonArray captains = raw.getAsJsonArray("captains");
		
		for(int i=0; i<loyalty.size(); i++) {
			captains.get(i).getAsJsonObject().addProperty("pveWins", loyalty.get(i).getAsJsonObject().getAsJsonPrimitive("pveWins").getAsInt());
			captains.get(i).getAsJsonObject().addProperty("pveLoyaltyLevel", loyalty.get(i).getAsJsonObject().getAsJsonPrimitive("pveLoyaltyLevel").getAsInt());
		}
		
		return captains;
	}
	
	public boolean setFavorite(JsonObject captain, boolean b) {
		return JsonParser.json(req.updateFavoriteCaptains(captain.getAsJsonPrimitive("userId").getAsString(), b)).getAsJsonPrimitive("status").getAsString().contains("success");
	}
	
	public String updateUnits() {
		JsonObject jo = JsonParser.json(req.getUserUnits());
		JsonArray u = jo.getAsJsonArray("data");
		units = new Unit[0];
		for(int i=0; i<u.size(); i++) {
			units = add(units, new Unit(u.get(i).getAsJsonObject()));
		}
		return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
	}
	
	public void reloadStore() {
		store = new Store(req);
	}
	
	public JsonArray getStoreItems(int con) {
		return store.getStoreItems(con);
	}
	
	public String buyItem(JsonObject item) {
		return store.buyItem(item, req);
	}
	
	public Unit[] getUnits(int con) {
		String serverTime = updateUnits();
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			switch(con) {
			case SRC.Helper.canPlaceUnit:
				if(units[i].isAvailable(serverTime)) {
					ret = add(ret, units[i]);
				}
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
	
	public String unlockUnit(Unit unit) {
		return store.unlockUnit(unit.get(SRC.Unit.unitType), req);
	}
	
	public String upgradeUnit(Unit unit, String specUID) {
		if(store.canUpgradeUnit(unit)) {
			return store.upgradeUnit(unit, req, specUID);
		}
		return "cant upgrade unit";
	}
	
	public Unit[] getUnits(String con, String arg) {
		updateUnits();
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			if(units[i].get(con).equals(arg)) {
				ret = add(ret, units[i]);
			}
		}
		return ret;
	}
	
	
	public String createPlacementData(Unit unit, boolean epic, double[] pos) {
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
		ret.addProperty("onPlanIcon", false);
		ret.addProperty("isSpell", false);
		ret.addProperty("stackRaidPlacementsId", "0");
		
		
		return ret.toString();
	}
	
	
	private static Unit[] add(Unit[] arr, Unit item) {
		Unit[] arr2 = new Unit[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	private static Raid[] add(Raid[] arr, Raid item) {
		Raid[] arr2 = new Raid[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	
}
