package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import srlib.skins.Skin;
import srlib.souls.SoulType;
import srlib.store.BuyableUnit;
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
	
	
	public ViewerBackEnd(String cid, SRR req, UpdateEventListener<ViewerBackEnd> uelis) throws NoConnectionException, NotAuthorizedException {
		super(cid, req, uelis);
		ini();
	}
	
	protected void ini() throws NoConnectionException, NotAuthorizedException {
		super.ini();
		updateRaids(true);
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
		
		setCaps(searchCaptains(false, true, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, false, null, 100), dungeon);
		
		rts.put("caps::"+dungeon, now + updateTimes[0]*60*1000);
		uelis.afterUpdate("caps::"+dungeon, this);
	}
	
	public void setCaps(CaptainData[] caps, boolean dungeon) {
		if(dungeon)
			this.dunCaps = caps;
		else
			this.caps = caps;
	}
	
	public static class SeedAndLastPage {
		public String seed = "0";
		public int lastPage = 10;
		public SeedAndLastPage() {}
		public SeedAndLastPage(String seed) {
			this.seed = seed;
		}
	}
	
	public CaptainData[] searchCaptains(boolean fav, boolean live, String mode, boolean searchForCaptain, String name, int maxPage) throws NoConnectionException, NotAuthorizedException {
		JsonArray rawCaps = new JsonArray();
		
		SeedAndLastPage sap = new SeedAndLastPage();
		
		for(int i=1; i<=sap.lastPage && i<=maxPage; i++) {
			JsonObject raw = Json.parseObj(req.getCaptainsForSearch(""+i, "24", sap.seed, fav, live, mode, searchForCaptain, name));
			if(testUpdate(raw))
				raw = Json.parseObj(req.getCaptainsForSearch(""+i, "24", sap.seed, fav, live, mode, searchForCaptain, name));
			
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
					cap.getAsJsonObject().add("pveWins", loyalty.get(j).getAsJsonObject().get("pveWins"));
					cap.getAsJsonObject().add("pveLoyaltyLevel", loyalty.get(j).getAsJsonObject().get("pveLoyaltyLevel"));
				}
			}
			rawCaps.addAll(captains);
		}
		
		
		CaptainData[] ret = new CaptainData[rawCaps.size()];
		for(int i=0; i<ret.length; i++)
			ret[i] = new CaptainData(rawCaps.get(i).getAsJsonObject());
		
		return ret;
	}
	
	
	public CaptainData[] getCaps(boolean dungeon) throws NoConnectionException, NotAuthorizedException {
		updateCaps(false, dungeon);
		return dungeon 
				? dunCaps
				: caps;
	}
	
	public String updateFavoriteCaptain(CaptainData cap, boolean fav) throws NoConnectionException {
		JsonElement err = Json.parseObj(req.updateFavoriteCaptains(cap.captainId, fav)).get(SRC.errorMessage);
		if(err == null != !err.isJsonPrimitive())
			return null;
		
		return err.getAsString();
	}
	
	
	public Raid[] getRaids(int con) throws NoConnectionException, NotAuthorizedException {
		updateRaids(false);
		Raid[] ret = new Raid[0];
		for(int i=0; i<raids.length; i++) {
			switch(con) {
			case SRC.BackEnd.all:
				return raids.clone();
			case SRC.BackEnd.isRaidReward:
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
	
	public String remRaid(String captainId) throws NoConnectionException {
		JsonElement err = Json.parseObj(req.leaveCaptain(captainId)).get(SRC.errorMessage);
		return err.isJsonPrimitive() ? err.getAsString() : null;
	}
	
	public String addRaid(CaptainData captain, String slot) throws NoConnectionException {
		JsonElement err = Json.parseObj(req.addPlayerToRaid(captain.captainId, slot)).get(SRC.errorMessage);
		return err.isJsonPrimitive() ? err.getAsString() : null;
	}
	
	public String switchRaid(CaptainData captain, int slot) throws NoConnectionException, NotAuthorizedException {
		if(raids[slot] != null) {
			String err = remRaid(raids[slot].captainId);
			if(err != null)
				return err;
		}
		return addRaid(captain, ""+slot);
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
	
	public Unit[] getUnits(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateUnits(force);
		return units;
	}
	
	public Unit[] getPlaceableUnits(boolean force) {
		ArrayList<Unit> ret = new ArrayList<>(units.length);
		for(int i=0; i<units.length; i++)
			if(units[i].isAvailable())
				ret.add(units[i]);
		return ret.toArray(new Unit[ret.size()]);
	}
	
	public BuyableUnit[] getUnlockableUnits(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateUnits(force);
		updateStore(force);
		return store.getUnlockableUnits(units);
	}
	
	public BuyableUnit[] getUpgradeableUnits(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateUnits(force);
		updateStore(force);
		return store.getUpgradeableUnits(units);
	}
	
	public String upgradeUnit(Unit unit, String specUID) throws NoConnectionException {
		return store.canUpgradeUnit(unit) ? store.upgradeUnit(unit, req, specUID) : "cant upgrade unit";
	}
	
	
	public String unlockUnit(BuyableUnit unit) throws NoConnectionException {
		return store.unlockUnit(req.unlockUnit(unit.type.uid), unit.type, unit.dupe);
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
		ret.addProperty("CharacterType", (epic?"epic":"")+unit.type.uid+unit.level);
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
