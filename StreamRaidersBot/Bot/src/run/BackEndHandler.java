package run;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Time;
import program.Captain;
import program.Debug;
import program.Map;
import program.QuestEventRewards;
import program.Raid;
import program.SRC;
import program.SRR;
import program.Skins;
import program.Skins.Skin;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;
import program.SRR.OutdatedDataException;
import program.Store;
import program.Store.C;
import program.Store.Item;
import program.Options;
import program.Unit;
import program.QuestEventRewards.Quest;

public class BackEndHandler {

	private SRR req;
	
	private long secoff;
	
	private Raid[] raids = new Raid[4];
	private Map[] maps = new Map[4];
	private Unit[] units;
	private Store store;
	private Skins skins;
	private Captain[] caps;
	private Captain[] dunCaps;
	private QuestEventRewards qer = new QuestEventRewards();
	private Hashtable<String, String> rts = new Hashtable<>();
	private int[] updateTimes = new int[] {10, 1, 5, 30, 15, 10, 60};
	
	
	public BackEndHandler(String pn, String cookies) throws NoConnectionException, NotAuthorizedException, OutdatedDataException {
		try {
			req = new SRR(cookies, Options.get("clientVersion"));
		} catch (OutdatedDataException e) {
			updateDataPath(e.getDataPath(), e.getServerTime(), req);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
			req = new SRR(cookies, Options.get("clientVersion"));
		}
		secoff = ChronoUnit.SECONDS.between(Time.parse(Json.parseObj(req.getCurrentTime()).get("data").getAsString()), LocalDateTime.now());
		updateRaids(true);
		updateUnits(true);
		updateStore(pn, true);
		updateSkins(true);
		updateQuestEventRewards(true);
	}
	
	public void setOptions(String proxyDomain, int proxyPort, String username, String password, String userAgent, boolean mandatory) {
		req.setProxy(proxyDomain, proxyPort, username, password, mandatory);
		req.setUserAgent(userAgent);
	}
	
	public void setUpdateTimes(int units, int raids, int maps, int store, int qer, int caps, int skins) {
		updateTimes[0] = units;
		updateTimes[1] = raids;
		updateTimes[2] = maps;
		updateTimes[3] = store;
		updateTimes[4] = qer;
		updateTimes[5] = caps;
		updateTimes[6] = skins;
	}
	
	
	public static interface DataPathEventListener {
		public default void onUpdate(String dataPath, String serverTime, JsonObject data) {}
	}
	
	private static DataPathEventListener dpelis = new DataPathEventListener() {};
	
	public static void setDataPathEventListener(DataPathEventListener dpelis) {
		BackEndHandler.dpelis = dpelis;
	}
	
	public static interface UpdateEventListener {
		public default void afterUpdate(String obj) {};
	}
	
	private UpdateEventListener uelis = new UpdateEventListener() {};
	
	public void setUpdateEventListener(UpdateEventListener uelis) {
		this.uelis = uelis;
	}
	
	
	synchronized private static void updateDataPath(String dataPath, String serverTime, SRR req) throws NoConnectionException, NotAuthorizedException {
		if(!Options.get("data").equals(dataPath)) {
			JsonObject data = Json.parseObj(SRR.getData(dataPath)).getAsJsonObject("sheets");
			for(String s : "obstacles Obstacles  quests Quests  mapNodes MapNodes  specsRaw Specialization  store Store  rewards ChestRewards  events Events  skins Skins  mapDifficulty MapNodeDifficulty".split("  ")) {
				String[] ss = s.split(" ");
				Options.set(ss[0], data.getAsJsonObject(ss[1]).toString());
			}
			QuestEventRewards.updateCurrentEvent(serverTime);
			String currentEventUid = QuestEventRewards.getCurrentEvent();
			if(currentEventUid != null) {
				currentEventUid = currentEventUid.split("_")[0];
				JsonObject tiers = data.getAsJsonObject("EventTiers");
				JsonObject currentTiers = new JsonObject();
				for(String key : tiers.keySet())
					if(key.matches("^"+currentEventUid+"[0-9]+$"))
						currentTiers.add(key, tiers.get(key));
				Options.set("eventTiers", currentTiers.toString());
			} else {
				Options.set("eventTiers", "{}");
			}
			Options.set("currentEventCurrency", data.getAsJsonObject("Items").getAsJsonObject("eventcurrency").get("CurrencyTypeAwarded").getAsString());
			Options.set("data", dataPath);
			Options.save();
			dpelis.onUpdate(dataPath, serverTime, data);
		}
		try {
			if(req != null)
				req.reload();
		} catch (OutdatedDataException e) {
			Debug.printException("BackEndHandler -> updateDataPath: err=failed to update data path",  e, Debug.runerr, Debug.fatal, null, null, true);
		}
	}
	
	public String getServerTime() {
		return Time.parse(LocalDateTime.now().minusSeconds(secoff));
	}
	
	private boolean testUpdate(JsonObject jo) throws NoConnectionException, NotAuthorizedException {
		JsonElement je = jo.get(SRC.errorMessage);
		if(!je.isJsonPrimitive()) 
			return false;
		String err = je.getAsString();
		switch(err) {
		case "Game data mismatch.":
		case "Client lower.":
		case "Account type mismatch.":
			updateDataPath(jo.getAsJsonObject("info").getAsJsonPrimitive("dataPath").getAsString(), getServerTime(), req);
			return true;
		default:
			throw new Run.StreamRaidersException("BackEndHandler -> testUpdate: err="+je.getAsString()+", jo="+jo.toString());
		}
	}
	
	synchronized public void updateUnits(boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("units");
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[0]))) && !force)
			return;
		
		JsonObject jo = Json.parseObj(req.getUserUnits());
		if(testUpdate(jo))
			jo = Json.parseObj(req.getUserUnits());
		
		JsonArray u = jo.getAsJsonArray("data");
		units = new Unit[u.size()];
		for(int i=0; i<u.size(); i++)
			units[i] = new Unit(u.get(i).getAsJsonObject());
		
		rts.put("units", Time.parse(now));
		uelis.afterUpdate("units");
	}
	
	synchronized public void updateRaids(boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("raids");
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[1]))) && !force)
			return;
		
		JsonObject jo = Json.parseObj(req.getActiveRaidsByUser());
		if(testUpdate(jo))
			jo = Json.parseObj(req.getActiveRaidsByUser());
		
		
		boolean[] got = new boolean[4];
		JsonArray rs = jo.getAsJsonArray("data");
		for(int i=0; i<rs.size(); i++) {
			int index = rs.get(i).getAsJsonObject().get(SRC.Raid.userSortIndex).getAsInt();
			raids[index] = new Raid(rs.get(i).getAsJsonObject());
			raids[index].addNode(raids[index].get(SRC.Raid.nodeId));
			got[index] = true;
		}
		
		for(int i=0; i<got.length; i++)
			if(!got[i])
				raids[i] = null;
		
		rts.put("raids", Time.parse(now));
		uelis.afterUpdate("raids");
	}
	
	synchronized public void updateMap(String pn, int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("map::"+slot);
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[2]))) && !force)
			return;
		
		updateRaids(true);
		
		List<String> userIds = SRR.getUserIds();
		userIds.add(0, getUserId());
		
			
		JsonObject raidplan = Json.parseObj(req.getRaidPlan(raids[slot].get(SRC.Raid.raidId)));
		if(testUpdate(raidplan))
			raidplan = Json.parseObj(req.getRaidPlan(raids[slot].get(SRC.Raid.raidId)));
		
		JsonElement je = raidplan.get("data");
		String mapName = raids[slot].get(SRC.Raid.battleground);
		maps[slot] = new Map(Json.parseObj(req.getMapData(mapName)),
				raids[slot],
				(je.isJsonObject() ? je.getAsJsonObject().getAsJsonObject("planData") : null),
				mapName,
				userIds,
				pn, slot);
		
		rts.put("map::"+slot, Time.parse(now));
		uelis.afterUpdate("map::"+slot);
	}
	
	synchronized public void updateStore(String pn, boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("store");
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[3]))) && !force)
			return;
		
		JsonObject user = Json.parseObj(req.getUser());
		if(testUpdate(user))
			user = Json.parseObj(req.getUser());
		
		JsonObject cur = Json.parseObj(req.getAvailableCurrencies());
		JsonElement err = cur.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Debug.print("BackEndHandler -> updateStore: cur, err="+err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);

		JsonObject items = Json.parseObj(req.getCurrentStoreItems());
		err = items.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Debug.print("BackEndHandler -> updateStore: items, err="+err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);
		
		JsonObject skins = Json.parseObj(req.getUserItems());
		err = skins.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Debug.print("BackEndHandler -> updateStore: skins, err="+err.getAsString(), Debug.runerr, Debug.error, pn, 4, true);
		
		
		store = new Store(user.getAsJsonObject("data"),
				cur.getAsJsonArray("data"),
				items.getAsJsonArray("data"));
		

		this.skins = new Skins(skins.get("data").isJsonArray() ? skins.getAsJsonArray("data") : null);
		
		rts.put("store", Time.parse(now));
		uelis.afterUpdate("store");
	}
	
	synchronized public void updateSkins(boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("skins");
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[6]))) && !force)
			return;
		
		JsonObject skins = Json.parseObj(req.getUserItems());
		if(testUpdate(skins))
			skins = Json.parseObj(req.getUserItems());
		
		this.skins = new Skins(skins.get("data").isJsonArray() ? skins.getAsJsonArray("data") : null);
		
		rts.put("skins", Time.parse(now));
		uelis.afterUpdate("skins");
	}
	
	synchronized public void updateQuestEventRewards(boolean force) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("qer");
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[4]))) && !force)
			return;
		
		JsonObject userEventProgression = Json.parseObj(req.getUserEventProgression());
		if(testUpdate(userEventProgression))
			userEventProgression = Json.parseObj(req.getUserEventProgression());
		qer.updateEvent(Time.parse(LocalDateTime.now().minusSeconds(secoff)), userEventProgression.getAsJsonArray("data"));
		
		JsonObject userQuests = Json.parseObj(req.getUserQuests());
		if(testUpdate(userQuests))
			userQuests = Json.parseObj(req.getUserQuests());
		qer.updateQuests(userQuests.getAsJsonArray("data"));
		
		rts.put("qer", Time.parse(now));
		uelis.afterUpdate("qer");
	}
	
	synchronized public void updateCaps(boolean force, boolean dungeon) throws NoConnectionException, NotAuthorizedException {
		String rt = rts.get("caps::"+dungeon);
		LocalDateTime now = LocalDateTime.now();
		if(!(rt == null || now.isAfter(Time.parse(rt).plusMinutes(updateTimes[5]))) && !force)
			return;
		
		JsonArray rawCaps = new JsonArray();
		SeedAndLastPage sap = new SeedAndLastPage();
		for(int i=1; i<=sap.lastPage; i++)
			rawCaps.addAll(searchCap(i, sap, false, true, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, false, null));
		
		Captain[] caps = new Captain[rawCaps.size()];
		
		for(int i=0; i<caps.length; i++)
			caps[i] = new Captain(rawCaps.get(i).getAsJsonObject());
		
		setCaps(caps, dungeon);
		
		rts.put("caps::"+dungeon, Time.parse(now));
		uelis.afterUpdate("caps::"+dungeon);
	}
	
	synchronized public void setCaps(Captain[] caps, boolean dungeon) {
		if(dungeon)
			dunCaps = caps;
		else
			this.caps = caps;
	}
	
	private static class SeedAndLastPage {
		public String seed = "0";
		public int lastPage = 10;
		public SeedAndLastPage() {}
		public SeedAndLastPage(String seed) {
			this.seed = seed;
		}
	}
	
	private JsonArray searchCap(int page, SeedAndLastPage sap, boolean fav, boolean live, String mode, boolean searchForCaptain, String name) throws NoConnectionException, NotAuthorizedException {
		JsonObject raw = Json.parseObj(req.getCaptainsForSearch(page, sap.seed, fav, live, mode, searchForCaptain, name));
		if(testUpdate(raw))
			raw = Json.parseObj(req.getCaptainsForSearch(page, sap.seed, fav, live, mode, searchForCaptain, name));
		
		JsonObject data = raw.getAsJsonObject("data");
		
		sap.seed = data.get("seed").getAsString();
		sap.lastPage = data.get("lastPage").getAsInt();
		
		JsonArray loyalty = data.getAsJsonArray("pveLoyalty");
		JsonArray captains = data.getAsJsonArray("captains");
		
		for(int j=0; j<loyalty.size(); j++) {
			JsonElement cap = captains.get(j);
			if(!cap.isJsonObject()) {
				captains.remove(j);
				loyalty.remove(j);
				j--;
			} else {
				cap.getAsJsonObject().add(SRC.Raid.pveWins, loyalty.get(j).getAsJsonObject().get(SRC.Raid.pveWins));
				cap.getAsJsonObject().add(SRC.Raid.pveLoyaltyLevel, loyalty.get(j).getAsJsonObject().get(SRC.Raid.pveLoyaltyLevel));
			}
		}
		
		return captains;
	}
	
	public JsonArray searchCap(int page, String seed, boolean fav, boolean live, String mode, boolean searchForCaptain, String name) throws NoConnectionException, NotAuthorizedException {
		return searchCap(page, new SeedAndLastPage(seed), fav, live, mode, searchForCaptain, name);
	}
	
	public Captain[] getCaps(boolean dungeon) throws NoConnectionException, NotAuthorizedException {
		updateCaps(false, dungeon);
		return dungeon 
				? dunCaps
				: caps;
	}
	
	
	public Raid[] getRaids(int con) throws NoConnectionException, NotAuthorizedException {
		updateRaids(false);
		Raid[] ret = new Raid[0];
		for(int i=0; i<raids.length; i++) {
			switch(con) {
			case SRC.BackEndHandler.all:
				return raids.clone();
			case SRC.BackEndHandler.isRaidReward:
				if(raids[i].isReward())
					ret = add(ret, raids[i]);
				break;
			}
		}
		return ret;
	}
	
	public Raid getRaid(int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		updateRaids(force);
		return raids[slot];
	}
	
	public Raid[] getOfflineRaids(boolean il, int treshold) throws NoConnectionException, NotAuthorizedException {
		updateRaids(false);
		Raid[] ret = new Raid[0];
		for(int i=0; i<raids.length; i++)
			if(raids[i].isOffline(getServerTime(), il, treshold))
				ret = add(ret, raids[i]);
		return ret;
	}
	
	public void remRaid(String captainId) throws NoConnectionException {
		req.leaveCaptain(captainId);
	}
	
	public void addRaid(Captain captain, String slot) throws NoConnectionException {
		req.addPlayerToRaid(captain.get(SRC.Captain.captainId), slot);
	}
	
	public void switchRaid(Captain captain, int slot) throws NoConnectionException, NotAuthorizedException {
		if(raids[slot] != null)
			remRaid(raids[slot].get(SRC.Raid.captainId));
		addRaid(captain, ""+slot);
	}
	
	
	public JsonObject getChest(int slot) throws NoConnectionException, NotAuthorizedException {
		JsonElement data = Json.parseObj(req.getRaidStatsByUser(raids[slot].get(SRC.Raid.raidId))).get("data");
		if(data == null || !data.isJsonObject())
			return null;
		return raids[slot].getChest(data.getAsJsonObject());
	}
	
	public boolean isReward(int slot) throws NoConnectionException, NotAuthorizedException {
		updateRaids(false);
		return raids[slot] != null && raids[slot].isReward();
			
	}
	
	public Unit[] getUnits(String pn, int con, boolean force) throws NoConnectionException, NotAuthorizedException {
		updateUnits(force);
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			switch(con) {
			case SRC.BackEndHandler.all:
				ret = add(ret, units[i]);
				break;
			case SRC.BackEndHandler.isUnitPlaceable:
				if(units[i].isAvailable(getServerTime()))
					ret = add(ret, units[i]);
				break;
			case SRC.BackEndHandler.isUnitUnlockable:
				updateStore(pn, true);
				return store.getUnlockableUnits(units);
			case SRC.BackEndHandler.isUnitUpgradeable:
				updateStore(pn, true);
				return store.getUpgradeableUnits(units);
			}
		}
		return ret;
	}
	
	public String upgradeUnit(Unit unit, String specUID) throws NoConnectionException {
		return store.canUpgradeUnit(unit) ? store.upgradeUnit(unit, req, specUID) : "cant upgrade unit";
	}
	
	public boolean canUnlockUnit(Unit unit) {
		return store.canUnlockUnit(unit.get(SRC.Unit.unitType), unit.isDupe());
	}
	
	public String unlockUnit(Unit unit) throws NoConnectionException {
		return store.unlockUnit(unit.get(SRC.Unit.unitType), unit.isDupe(), req);
	}
	
	
	private static final String[] dungeons_units_dis = "knockedUnits deadUnits exhaustedUnits".split(" ");
	
	public Unit[] getPlaceableUnits(int slot) throws NoConnectionException, NotAuthorizedException {
		updateUnits(true);
		Unit[] ret = new Unit[0];
		
		if(raids[slot].isDungeon()) {
			JsonObject rdata = Json.parseObj(req.getUserDungeonInfoForRaid(raids[slot].get(SRC.Raid.raidId)));
			if(testUpdate(rdata))
				rdata = Json.parseObj(req.getUserDungeonInfoForRaid(raids[slot].get(SRC.Raid.raidId)));
			
			JsonObject data = rdata.getAsJsonObject("data");
			
			HashSet<String> bnd = new HashSet<>();
			
			for(String key : dungeons_units_dis) {
				JsonElement ku = data.get(key);
				if(ku != null && ku.isJsonPrimitive()) {
					String sku = ku.getAsString();
					if(!sku.equals(""))
						bnd.addAll(Arrays.asList(sku.split(",")));
				}
			}
			
			JsonArray pmnt = Json.parseArr(raids[slot].get(SRC.Raid.placementsSerialized));
			if(pmnt != null) 
				for(int i=0; i<pmnt.size(); i++) 
					bnd.add(pmnt.get(i).getAsJsonObject().getAsJsonPrimitive(SRC.Unit.unitId).getAsString());
				
			
			for(int i=0; i<units.length; i++) {
				if(bnd.contains(units[i].get(SRC.Unit.unitId)))
					continue;
				
				ret = add(ret, units[i]);
			}
		} else {
			for(Unit u : units)
				if(u.isAvailable(getServerTime()))
					ret = add(ret, u);
		}
		
		return ret;
	}
	
	public String placeUnit(String pn, int slot, Unit unit, boolean epic, int[] pos, boolean onPlanIcon) throws NoConnectionException {
		String atr = req.addToRaid(raids[slot].get(SRC.Raid.raidId),
				createPlacementData(unit, epic, maps[slot].getAsSRCoords(epic, pos), onPlanIcon));
		
		try {
			JsonElement je = Json.parseObj(atr).get(SRC.errorMessage);
			if(je.isJsonPrimitive()) 
				return je.getAsString();
		} catch (NullPointerException e) {
			Debug.printException("BackEndHandler -> placeUnit: err=failed to place Unit, atr=" + atr == null ? "null" : atr, e, Debug.runerr, Debug.error, pn, slot, true);
		}
		
		return null;
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
	
	public Map getMap(String pn, int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		updateMap(pn, slot, force);
		return maps[slot];
	}
	
	
	public String getUserId() {
		return req.getUserId();
	}
	
	
	public int getCurrency(String pn, C con, boolean force) throws NotAuthorizedException, NoConnectionException {
		updateStore(pn, false);
		return store.getCurrency(con);
	}
	
	public void decreaseCurrency(C con, int amount) {
		store.decreaseCurrency(con, amount);
	}
	
	public void addCurrency(C con, int amount) {
		store.addCurrency(con, amount);
	}
	
	public void addCurrency(String type, int amount) {
		store.addCurrency(type, amount);
	}
	
	public void setCurrency(C con, int amount) {
		store.setCurrency(con, amount);
	}
	
	public Hashtable<String, Integer> getCurrencies(String pn) throws NotAuthorizedException, NoConnectionException {
		updateStore(pn, false);
		return store.getCurrencies();
	}
	
	
	public JsonObject buyItem(Item item) throws NoConnectionException {
		return store.buyItem(item, req, skins);
	}
	
	public List<Item> getStoreItems(int con, String section) {
		return store.getStoreItems(con, section, getServerTime());
	}
	
	public List<Item> getAvailableEventStoreItems(String section, boolean includePurchased) throws NoConnectionException, NotAuthorizedException {
		updateSkins(false);
		return store.getAvailableEventStoreItems(section, getServerTime(), includePurchased, skins);
	}
	
	public String refreshStore() throws NoConnectionException, NotAuthorizedException {
		JsonObject rdata = Json.parseObj(req.purchaseStoreRefresh());
		if(testUpdate(rdata))
			rdata = Json.parseObj(req.purchaseStoreRefresh());
		
		JsonElement err = rdata.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		store.refreshStore(rdata.getAsJsonArray("data"));
		return null;
	}
	
	public int getStoreRefreshCount() {
		return store.getStoreRefreshCount();
	}
	
	public Skins getSkins() throws NoConnectionException, NotAuthorizedException {
		updateSkins(false);
		return skins;
	}
	
	public String equipSkin(Unit unit, Skin skin) throws NoConnectionException {
		JsonObject resp = Json.parseObj(req.equipSkin(unit.get(SRC.Unit.unitId),
					skin == null ? unit.get(SRC.Unit.skin) : skin.uid,
					skin == null ? "0" : "1"));
		JsonElement err = resp.get(SRC.errorMessage);
		
		if(err == null || !err.isJsonPrimitive()) {
			unit.setSkin(skin);
			return null;
		} else
			return err.getAsString();
	}
	
	public boolean isEvent() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return qer.isEvent();
	}
	
	public int getEventTier() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return qer.getEventTier();
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return qer.canCollectEvent(p, battlePass);
	}
	
	public JsonObject collectEvent(int p, boolean battlePass) throws NoConnectionException {
		return qer.collectEvent(p, battlePass, Json.parseObj(req.grantEventReward(QuestEventRewards.getCurrentEvent(), ""+p, battlePass)));
	}
	
	public boolean hasBattlePass() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return qer.hasBattlePass();
	}
	
	public Quest[] getClaimableQuests() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(true);
		return qer.getClaimableQuests();
	}
	
	public JsonObject claimQuest(Quest quest) throws NoConnectionException {
		return Json.parseObj(req.collectQuestReward(quest.getSlot()));
	}
	
	public List<String> getNeededUnitTypesForQuests() {
		return qer.getNeededUnitTypesForQuests();
	}
	
	public String grantTeamReward() throws NoConnectionException {
		return req.grantTeamReward();
	}
	
	public String grantEventQuestMilestoneReward() throws NoConnectionException {
		return req.grantEventQuestMilestoneReward();
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
	
	
	
}
