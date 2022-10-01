package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Http.NoConnectionException;
import otherlib.Logger;
import run.AbstractBackEnd;
import srlib.Event;
import srlib.Map;
import srlib.Quests;
import srlib.RaidType;
import srlib.SRC;
import srlib.SRR;
import srlib.Quests.Quest;
import srlib.SRR.NotAuthorizedException;
import srlib.Store.C;
import srlib.Store.Item;
import srlib.skins.Skin;
import srlib.skins.Skins;
import srlib.souls.SoulType;
import srlib.units.Unit;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;

public class ViewerBackEnd extends AbstractBackEnd<ViewerBackEnd> {

	private Raid[] raids = new Raid[4];
	private Map[] maps = new Map[4];
	private CaptainData[] caps;
	private CaptainData[] dunCaps;
	private Quests quests = new Quests();
	private Event event = new Event();
	private HashMap<String, Long> rts = new HashMap<>();
	private int[] updateTimes = new int[] {10, 1, 5, 15};
	
	
	public ViewerBackEnd(String cid, SRR req) {
		super(cid, req);
	}
	
	protected void ini() throws NoConnectionException, NotAuthorizedException {
		updateRaids(true);
		updateUnits(true);
		updateStore(true);
		updateSkins(true);
		updateQuestEventRewards(true);
	}
	
	
	public void setUpdateTimes(int units, int skins, int souls, int caps, int raids, int maps, int store, int qer) {
		super.setUpdateTimes(units, skins, souls, store);
		updateTimes[0] = caps;
		updateTimes[1] = raids;
		updateTimes[2] = maps;
		updateTimes[3] = qer;
	}
	
	
	synchronized public void updateRaids(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("raids");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject jo = Json.parseObj(req.getActiveRaidsByUser());
		if(testUpdate(jo))
			jo = Json.parseObj(req.getActiveRaidsByUser());
		
		
		boolean[] got = new boolean[4];
		JsonArray rs = jo.getAsJsonArray("data");
		for(int i=0; i<rs.size(); i++) {
			final int index = rs.get(i).getAsJsonObject().get("userSortIndex").getAsInt();
			raids[index] = new Raid(rs.get(i).getAsJsonObject(), cid, index);
			got[index] = true;
		}
		
		for(int i=0; i<got.length; i++)
			if(!got[i])
				raids[i] = null;
		
		rts.put("raids", now + updateTimes[1]*60*1000);
		uelis.afterUpdate("raids", this);
	}
	
	synchronized public void updateMap(int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("map::"+slot);
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		
		updateRaids(true);
		
		ArrayList<String> userIds = SRR.getAllUserIds();
		userIds.add(0, req.getViewerUserId());
		
			
		JsonObject raidplan = Json.parseObj(req.getRaidPlan(raids[slot].raidId));
		if(testUpdate(raidplan))
			raidplan = Json.parseObj(req.getRaidPlan(raids[slot].raidId));
		
		JsonElement je = raidplan.get("data");
		String mapName = raids[slot].battleground;
		maps[slot] = new Map(Json.parseObj(req.getMapData(mapName)),
				raids[slot],
				(je.isJsonObject() ? je.getAsJsonObject().getAsJsonObject("planData") : null),
				mapName,
				userIds,
				cid, slot);
		
		rts.put("map::"+slot, now + updateTimes[2]*60*1000);
		uelis.afterUpdate("map::"+slot, this);
	}
	
	synchronized public void updateQuestEventRewards(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("qer");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject userEventProgression = Json.parseObj(req.getUserEventProgression());
		if(testUpdate(userEventProgression))
			userEventProgression = Json.parseObj(req.getUserEventProgression());
		
		event.updateEventProgression(userEventProgression.getAsJsonArray("data"));
		
		JsonObject userQuests = Json.parseObj(req.getUserQuests());
		if(testUpdate(userQuests))
			userQuests = Json.parseObj(req.getUserQuests());
		
		//TODO split
		quests.updateQuests(userQuests.getAsJsonArray("data"));
		
		rts.put("qer", now + updateTimes[3]*60*1000);
		uelis.afterUpdate("qer", this);
	}
	
	synchronized public void updateCaps(boolean force, boolean dungeon) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("caps::"+dungeon);
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonArray rawCaps = new JsonArray();
		SeedAndLastPage sap = new SeedAndLastPage();
		for(int i=1; i<=sap.lastPage; i++)
			rawCaps.addAll(searchCap(i, sap, false, true, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, false, null));
		
		CaptainData[] caps = new CaptainData[rawCaps.size()];
		
		for(int i=0; i<caps.length; i++)
			caps[i] = new CaptainData(rawCaps.get(i).getAsJsonObject());
		
		setCaps(caps, dungeon);
		
		rts.put("caps::"+dungeon, now + updateTimes[0]*60*1000);
		uelis.afterUpdate("caps::"+dungeon, this);
	}
	
	synchronized public void setCaps(CaptainData[] caps, boolean dungeon) {
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
		JsonObject raw = Json.parseObj(req.getCaptainsForSearch(""+page, "24", sap.seed, fav, live, mode, searchForCaptain, name));
		if(testUpdate(raw))
			raw = Json.parseObj(req.getCaptainsForSearch(""+page, "24", sap.seed, fav, live, mode, searchForCaptain, name));
		
		JsonObject data = raw.getAsJsonObject("data");
		
		sap.seed = data.get("seed").getAsString();
		sap.lastPage = (int) Math.ceil(data.get("total").getAsInt() / 24);
		
		JsonArray loyalty = data.getAsJsonArray("pveLoyalty");
		JsonArray captains = data.getAsJsonArray("captains");
		
		for(int j=0; j<loyalty.size(); j++) {
			JsonElement cap = captains.get(j);
			if(!cap.isJsonObject()) {
				captains.remove(j);
				loyalty.remove(j);
				j--;
			} else {
				cap.getAsJsonObject().add("pveWins", loyalty.get(j).getAsJsonObject().get("pveWins"));
				cap.getAsJsonObject().add("pveLoyaltyLevel", loyalty.get(j).getAsJsonObject().get("pveLoyaltyLevel"));
			}
		}
		
		return captains;
	}
	
	public JsonArray searchCap(int page, String seed, boolean fav, boolean live, String mode, boolean searchForCaptain, String name) throws NoConnectionException, NotAuthorizedException {
		return searchCap(page, new SeedAndLastPage(seed), fav, live, mode, searchForCaptain, name);
	}
	
	public CaptainData[] getCaps(boolean dungeon) throws NoConnectionException, NotAuthorizedException {
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
			if(raids[i].isOffline(il, treshold))
				ret = add(ret, raids[i]);
		return ret;
	}
	
	public void addUserDungeonInfo(Raid r) throws NoConnectionException, NotAuthorizedException {
		if(r.type != RaidType.DUNGEON)
			return;
		
		JsonObject rdata = Json.parseObj(req.getUserDungeonInfoForRaid(r.raidId));
		if(testUpdate(rdata))
			rdata = Json.parseObj(req.getUserDungeonInfoForRaid(r.raidId));
		
		r.addUserDungeonInfo(rdata.getAsJsonObject("data"));
	}
	
	public void remRaid(String captainId) throws NoConnectionException {
		req.leaveCaptain(captainId);
	}
	
	public void addRaid(CaptainData captain, String slot) throws NoConnectionException {
		req.addPlayerToRaid(captain.captainId, slot);
	}
	
	public void switchRaid(CaptainData captain, int slot) throws NoConnectionException, NotAuthorizedException {
		if(raids[slot] != null)
			remRaid(raids[slot].captainId);
		addRaid(captain, ""+slot);
	}
	
	
	public JsonObject getChest(int slot) throws NoConnectionException, NotAuthorizedException {
		JsonElement data = Json.parseObj(req.getRaidStatsByUser(raids[slot].raidId)).get("data");
		if(data == null || !data.isJsonObject())
			return null;
		return raids[slot].getChest(data.getAsJsonObject());
	}
	
	public boolean isReward(int slot) throws NoConnectionException, NotAuthorizedException {
		updateRaids(false);
		return raids[slot] != null && raids[slot].isReward();
			
	}
	
	public Unit[] getUnits(int con, boolean force) throws NoConnectionException, NotAuthorizedException {
		updateUnits(force);
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			switch(con) {
			case SRC.BackEndHandler.all:
				ret = add(ret, units[i]);
				break;
			case SRC.BackEndHandler.isUnitPlaceable:
				if(units[i].isAvailable())
					ret = add(ret, units[i]);
				break;
			case SRC.BackEndHandler.isUnitUnlockable:
				updateStore(true);
				return store.getUnlockableUnits(units);
			case SRC.BackEndHandler.isUnitUpgradeable:
				updateStore(true);
				return store.getUpgradeableUnits(units);
			}
		}
		return ret;
	}
	
	public String upgradeUnit(Unit unit, String specUID) throws NoConnectionException {
		return store.canUpgradeUnit(unit) ? store.upgradeUnit(unit, req, specUID) : "cant upgrade unit";
	}
	
	public boolean canUnlockUnit(Unit unit) {
		return store.canUnlockUnit(unit.type, unit.dupe);
	}
	
	public String unlockUnit(Unit unit) throws NoConnectionException {
		return store.unlockUnit(req.unlockUnit(unit.type), unit, unit.dupe, req);
	}
	
	
	private static final String[] dungeons_units_dis = "knockedUnits deadUnits exhaustedUnits".split(" ");
	
	public Unit[] getPlaceableUnits(Raid r) throws NoConnectionException, NotAuthorizedException {
		updateUnits(true);
		Unit[] buffer = new Unit[units.length];
		int index = 0;
		
		switch(r.type) {
		case CAMPAIGN:
			for(int i=0; i<units.length; i++)
				if(units[i].isAvailable())
					buffer[index++] = units[i];
			break;
		case DUNGEON:
			JsonObject data = r.getUserDungeonInfo();
			
			HashSet<String> bnd = new HashSet<>();
			
			for(String key : dungeons_units_dis) {
				JsonElement ku = data.get(key);
				if(ku != null && ku.isJsonPrimitive()) {
					String sku = ku.getAsString();
					if(!sku.equals(""))
						bnd.addAll(Arrays.asList(sku.split(",")));
				}
			}
			
			JsonArray pmnt = Json.parseArr(r.placementsSerialized);
			if(pmnt != null)
				for(int i=0; i<pmnt.size(); i++) 
					bnd.add(pmnt.get(i).getAsJsonObject().get("unitId").getAsString());
			
			
			for(int i=0; i<units.length; i++) {
				if(bnd.contains(""+units[i].unitId))
					continue;
				
				buffer[index++] = units[i];
			}
			break;
		case VERSUS:
			//	TODO versus
			break;
		}
		
		Unit[] ret = new Unit[index];
		System.arraycopy(buffer, 0, ret, 0, index);
		return ret;
	}
	
	public String placeUnit(int slot, Unit unit, boolean epic, int[] pos, boolean onPlanIcon, Skin overrideSkin) throws NoConnectionException {
		String atr = req.addToRaid(raids[slot].raidId,
				createPlacementData(unit, epic, maps[slot].getAsSRCoords(epic, pos), onPlanIcon, overrideSkin, unit.getSoulType()));
		
		try {
			JsonElement je = Json.parseObj(atr).get(SRC.errorMessage);
			if(je.isJsonPrimitive()) 
				return je.getAsString();
		} catch (NullPointerException e) {
			Logger.printException("BackEndHandler -> placeUnit: err=failed to place Unit, atr=" + atr == null ? "null" : atr, e, Logger.runerr, Logger.error, cid, slot, true);
		}
		
		return null;
	}
	
	public String createPlacementData(Unit unit, boolean epic, double[] pos, boolean onPlanIcon, Skin overrideSkin, SoulType st) {
		JsonObject ret = new JsonObject();
		ret.addProperty("raidPlacementsId", "");
		ret.addProperty("userId", req.getViewerUserId());
		ret.addProperty("CharacterType", (epic?"epic":"")+unit.type+unit.level);
		ret.addProperty("X", pos[0]);
		ret.addProperty("Y", pos[1]);
		String skin = overrideSkin == null ? unit.getSkin() : overrideSkin.uid;
		ret.addProperty("skin", skin == null ? "" : skin);
		ret.addProperty("unitId", ""+unit.unitId);
		String suid = unit.specializationUid;
		ret.addProperty("specializationUid", suid == null ? "" : suid);
		ret.addProperty("team", "Ally");
		ret.addProperty("onPlanIcon", onPlanIcon);
		ret.addProperty("isSpell", false);
		ret.addProperty("stackRaidPlacementsId", "0");
		ret.addProperty("SoulType", st == null ? "" : st.uid);
		return ret.toString();
	}
	
	public Map getMap(int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		updateMap(slot, force);
		return maps[slot];
	}
	
	public int getCurrency(C con, boolean force) throws NotAuthorizedException, NoConnectionException {
		updateStore(false);
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
	
	public Hashtable<String, Integer> getCurrencies() throws NotAuthorizedException, NoConnectionException {
		updateStore(false);
		return store.getCurrencies();
	}
	
	
	public JsonObject buyItem(Item item) throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.buyItem(item, req, skins);
	}
	
	public ArrayList<Item> getPurchasableScrolls() throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.getPurchasableScrolls();
	}
	
	public ArrayList<Item> getAvailableEventStoreItems(String section, boolean includePurchased) throws NoConnectionException, NotAuthorizedException {
		updateSkins(false);
		updateStore(false);
		return store.getAvailableEventStoreItems(section, includePurchased, skins);
	}
	
	public Item getDaily() throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.getDaily();
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
	
	public Skins getSkins(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateSkins(force);
		return skins;
	}
	
	public String equipSkin(Unit unit, Skin skin) throws NoConnectionException {
		JsonObject resp = Json.parseObj(req.equipSkin(""+unit.unitId,
					skin == null ? unit.getSkin() : skin.uid,
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
		return Event.isEvent();
	}
	
	public int getEventTier() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return event.getEventTier();
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return event.canCollectEvent(p, battlePass);
	}
	
	public JsonObject collectEvent(int p, boolean battlePass) throws NoConnectionException {
		return event.collectEvent(p, battlePass, Json.parseObj(req.grantEventReward(Event.getCurrentEvent(), ""+p, battlePass)));
	}
	
	public boolean hasBattlePass() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(false);
		return event.hasBattlePass();
	}
	
	public ArrayList<Quest> getClaimableQuests() throws NoConnectionException, NotAuthorizedException {
		updateQuestEventRewards(true);
		return quests.getClaimableQuests();
	}
	
	public JsonObject claimQuest(Quest quest) throws NoConnectionException {
		return Json.parseObj(req.collectQuestReward(quest.slot));
	}
	
	public ArrayList<String> getNeededUnitTypesForQuests() {
		return quests.getNeededUnitTypesForQuests();
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
