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
import otherlib.Options;
import run.AbstractBackEnd;
import srlib.EventsAndEventRewards;
import srlib.RaidType;
import srlib.Reward;
import srlib.SRC;
import srlib.SRR;
import srlib.Quest;
import srlib.SRR.NotAuthorizedException;
import srlib.map.Map;
import srlib.skins.Skin;
import srlib.souls.SoulType;
import srlib.store.BuyableUnit;
import srlib.store.Store;
import srlib.units.Unit;
import srlib.units.UnitType;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;

public class ViewerBackEnd extends AbstractBackEnd<ViewerBackEnd> {

	private Raid[] raids = {null, null, null, null};
	private Map[] maps = {null, null, null, null};
	private CaptainData[][] caps = new CaptainData[RaidType.highestTypeInt][0];
	private Quest[] quests = {};
	private EventsAndEventRewards event = new EventsAndEventRewards();
	private HashMap<String, Long> rts = new HashMap<>();
	/**
	 * 0 caps<br>
	 * 1 raids<br>
	 * 2 maps<br>
	 * 3 eventrewards<br>
	 * 4 quests<br>
	 */
	private int[] updateTimes = {10, 1, 5, 15, 15};
	
	
	public ViewerBackEnd(String cid, SRR req, UpdateEventListener<ViewerBackEnd> uelis) throws NoConnectionException, NotAuthorizedException {
		super(cid, req, uelis);
		ini();
	}
	
	protected void ini() throws NoConnectionException, NotAuthorizedException {
		super.ini();
		updateRaids(true);
		updateEventRewards(true);
		updateQuests(true);
	}
	
	
	public void setUpdateTimes(int units, int skins, int souls, int caps, int raids, int plans, int store, int eventRewards, int quests) {
		super.setUpdateTimes(units, skins, souls, store);
		updateTimes[0] = caps;
		updateTimes[1] = raids;
		updateTimes[2] = plans;
		updateTimes[3] = eventRewards;
		updateTimes[4] = quests;
	}
	
	
	synchronized public void updateRaids(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("raids");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		
		int[] psi = new int[raids.length*2];
		int i = 0;
		int ind = 0;
		for(;i<raids.length; i++) {
			if(maps[i] != null) {
				psi[ind++] = maps[i].raidId;
				psi[ind++] = maps[i].getLastIndex();
			}
		}
		//	we do not want the array to be empty at its end
		int[] tmp;
		if(ind != psi.length) {
			tmp = new int[ind];
			System.arraycopy(psi, 0, tmp, 0, ind);
		} else
			tmp = psi;
		
		
		JsonObject jo = Json.parseObj(req.getActiveRaidsByUser(tmp));
		if(testUpdate(jo))
			jo = Json.parseObj(req.getActiveRaidsByUser(tmp));
		
		
		boolean[] got = new boolean[4];
		JsonArray rs = jo.getAsJsonArray("data");
		for(i=0; i<rs.size(); i++) {
			final int index = rs.get(i).getAsJsonObject().get("userSortIndex").getAsInt();
			raids[index] = new Raid(rs.get(i).getAsJsonObject(), cid, index);
			got[index] = true;
		}
		
		for(i=0; i<got.length; i++) {
			if(!got[i]) {
				raids[i] = null;
				maps[i] = null;
			} else
				updateMap(i);
		}
		
		rts.put("raids", now + updateTimes[1]*60*1000);
		uelis.afterUpdate("raids", this);
	}
	
	synchronized public void updateRaidPlan(int slot, boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("plan::"+slot);
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		//	updates the maps too
		updateRaids(force);
		
		if(maps[slot] != null) {
			JsonObject raidplan = Json.parseObj(req.getRaidPlan(""+raids[slot].raidId));
			if(testUpdate(raidplan))
				raidplan = Json.parseObj(req.getRaidPlan(""+raids[slot].raidId));

			JsonElement je = raidplan.get("data");
			if(je.isJsonObject())
				maps[slot].updateRaidPlan(je.getAsJsonObject().getAsJsonObject("planData"));
		}
		
		rts.put("plan::"+slot, now + updateTimes[2]*60*1000);
		uelis.afterUpdate("plan::"+slot, this);
	}
	
	private void updateMap(int slot) throws NoConnectionException {
		ArrayList<String> userIds = SRR.getAllUserIds();
		userIds.add(0, req.getViewerUserId());
		if(maps[slot] != null && maps[slot].raidId == raids[slot].raidId) {
			//	just update existing map
			maps[slot].updateMap(null, raids[slot], userIds);
		} else {
			//	new map -> new Object
			String mapName = raids[slot].battleground;
			maps[slot] = new Map(Json.parseObj(req.getMapData(mapName)),
					raids[slot], mapName, userIds, cid, slot);
		}
	}
	
	synchronized public void updateEventRewards(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("eventRewards");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject userEventProgression = Json.parseObj(req.getUserEventProgression());
		if(testUpdate(userEventProgression))
			userEventProgression = Json.parseObj(req.getUserEventProgression());
		
		event.updateEventProgression(userEventProgression.getAsJsonArray("data"));
		
		rts.put("eventRewards", now + updateTimes[3]*60*1000);
		uelis.afterUpdate("eventRewards", this);
	}
	
	synchronized public void updateQuests(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("quests");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject request = Json.parseObj(req.getUserQuests());
		if(testUpdate(request))
			request = Json.parseObj(req.getUserQuests());
		
		JsonArray userQuests = request.getAsJsonArray("data");
		JsonObject questTypes = Json.parseObj(Options.get("quests"));
		
		Quest[] buffer = new Quest[userQuests.size()];
		int c = 0;
		
		for(int i=0; i<userQuests.size(); i++) {
			JsonObject rQuest = userQuests.get(i).getAsJsonObject();
			
			JsonElement id = rQuest.get("currentQuestId");
			if(!id.isJsonPrimitive())
				continue;
			
			String questId = id.getAsString();
			
			JsonObject quest = questTypes.getAsJsonObject(questId);
			
			String qtype = quest.get("Type").getAsString();
			
			buffer[c++] = new Quest(questId,
									rQuest.get("currentProgress").getAsInt(),
									quest.get("GoalAmount").getAsInt(),
									rQuest.get("questSlotId").getAsString(),
									qtype,
									qtype.equals("PlaceUnitOfType") ? UnitType.getType(quest.get("UnitTypeRequirement").getAsString()) : null);
			
		}
		
		quests = new Quest[c];
		System.arraycopy(buffer, 0, quests, 0, c);
		
		rts.put("quests", now + updateTimes[4]*60*1000);
		uelis.afterUpdate("quests", this);
	}
	
	synchronized public void updateCaps(boolean force, final RaidType rt) throws NoConnectionException, NotAuthorizedException, ErrorRetrievingCaptainsException {
		Long wt = rts.get("caps::"+rt.toString());
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		setCaps(searchCaptains(false, true, false, rt, false, null, 100), rt);
		
		rts.put("caps::"+rt.toString(), now + updateTimes[0]*60*1000);
		uelis.afterUpdate("caps::"+rt.toString(), this);
	}
	
	public void setCaps(CaptainData[] caps, RaidType rt) {
		this.caps[rt.typeInt-1] = caps;
	}
	
	public CaptainData[] getCaps(RaidType rt) throws NoConnectionException, NotAuthorizedException, ErrorRetrievingCaptainsException {
		updateCaps(false, rt);
		return caps[rt.typeInt-1];
	}
	
	public static class SeedAndLastPage {
		public String seed = "0";
		public int lastPage = 10;
		public SeedAndLastPage() {}
		public SeedAndLastPage(String seed) {
			this.seed = seed;
		}
	}
	
	public static class ErrorRetrievingCaptainsException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public CaptainData[] searchCaptains(boolean fav, boolean live, Boolean roomCodes, RaidType rt, boolean searchForCaptain, String name, int maxPage) throws NoConnectionException, NotAuthorizedException, ErrorRetrievingCaptainsException {
		JsonArray rawCaps = new JsonArray();
		
		SeedAndLastPage sap = new SeedAndLastPage();
		
		final String mode;
		if(rt == null)
			mode = null;
		else {
			mode = switch(rt) {
			case CAMPAIGN -> SRC.Search.campaign;
			case DUNGEON -> SRC.Search.dungeon;
			case VERSUS -> SRC.Search.versus;
			default -> throw new IllegalArgumentException("Unexpected value: " + rt);
			};
		}
		
		for(int i=1; i<=sap.lastPage && i<=maxPage; i++) {
			JsonObject raw = Json.parseObj(req.getCaptainsForSearch(""+i, "24", sap.seed, fav, live, roomCodes, mode, searchForCaptain, name));
			JsonElement err = raw.get(SRC.errorMessage);
			if(err != null && err.isJsonPrimitive() && err.getAsString().equals("Error retrieving captains"))
				throw new ErrorRetrievingCaptainsException();
			if(testUpdate(raw))
				raw = Json.parseObj(req.getCaptainsForSearch(""+i, "24", sap.seed, fav, live, roomCodes, mode, searchForCaptain, name));
			
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
	
	public String updateFavoriteCaptain(CaptainData cap, boolean fav) throws NoConnectionException {
		JsonElement err = Json.parseObj(req.updateFavoriteCaptains(cap.captainId+"c", fav)).get(SRC.errorMessage);
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
		
		JsonObject rdata = Json.parseObj(req.getUserDungeonInfoForRaid(""+r.raidId));
		if(testUpdate(rdata))
			rdata = Json.parseObj(req.getUserDungeonInfoForRaid(""+r.raidId));
		
		r.addUserDungeonInfo(rdata.getAsJsonObject("data"));
	}
	
	public String remRaid(String captainId) throws NoConnectionException {
		JsonElement err = Json.parseObj(req.leaveCaptain(captainId)).get(SRC.errorMessage);
		return err.isJsonPrimitive() ? err.getAsString() : null;
	}
	
	public String addRaid(CaptainData captain, String slot) throws NoConnectionException {
		JsonObject resp = Json.parseObj(req.addPlayerToRaid(captain.captainId, slot));
		JsonElement err = resp.get(SRC.errorMessage);
		return err.isJsonPrimitive() ? err.getAsString() : (resp.get("data").getAsBoolean() ? null : "data is false");
	}
	
	public String switchRaid(CaptainData captain, int slot) throws NoConnectionException, NotAuthorizedException {
		if(raids[slot] != null) {
			String err = remRaid(raids[slot].captainId);
			if(err != null)
				return err;
		}
		return addRaid(captain, ""+slot);
	}
	
	
	public ArrayList<Reward> getChest(int slot) throws NoConnectionException, NotAuthorizedException {
		JsonObject resp = Json.parseObj(req.getRaidStatsByUser(""+raids[slot].raidId));
		JsonElement err = resp.get(SRC.errorMessage);
		if(err != null && err.isJsonPrimitive())
			throw new RuntimeException(err.getAsString());
		return raids[slot].getChest(resp.getAsJsonObject("data"));
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
		String atr = req.addToRaid(""+raids[slot].raidId,
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
		updateRaids(force);
		updateRaidPlan(slot, force);
		return maps[slot];
	}
	
	public boolean isEvent() throws NoConnectionException, NotAuthorizedException {
		updateEventRewards(false);
		return EventsAndEventRewards.isEvent();
	}
	
	public int getEventTier() throws NoConnectionException, NotAuthorizedException {
		updateEventRewards(false);
		return event.getEventTier();
	}
	
	public boolean canCollectEvent(int p, boolean battlePass) throws NoConnectionException, NotAuthorizedException {
		updateEventRewards(false);
		return event.canCollectEvent(p, battlePass);
	}
	
	public JsonObject collectEvent(int p, boolean battlePass) throws NoConnectionException {
		return event.collectEvent(p, battlePass, Json.parseObj(req.grantEventReward(EventsAndEventRewards.getCurrentEvent(), ""+p, battlePass)));
	}
	
	public boolean hasBattlePass() throws NoConnectionException, NotAuthorizedException {
		updateEventRewards(false);
		return event.hasBattlePass();
	}
	
	public ArrayList<Quest> getClaimableQuests() throws NoConnectionException, NotAuthorizedException {
		ArrayList<Quest> ret = new ArrayList<Quest>();
		for(Quest q : quests)
			if(q.canClaim)
				ret.add(q);
		return ret;
	}
	
	public static class QuestClaimFailedException extends Exception {
		private static final long serialVersionUID = -7741694457843475140L;
		public QuestClaimFailedException(String err) {
			super(err);
		}
	}
	
	public Reward claimQuest(Quest quest) throws NoConnectionException, QuestClaimFailedException {
		JsonObject dat = Json.parseObj(req.collectQuestReward(quest.qslot));
		JsonElement err = dat.get(SRC.errorMessage);
		boolean isErr = err.isJsonPrimitive();
		
		if(isErr)
			throw new QuestClaimFailedException(err.getAsString());
		
		dat = dat.getAsJsonObject("data").getAsJsonObject("rewardData");
		String item = dat.get("ItemId").getAsString();
		
		if(item.equals("goldpiecebag"))
			item = Store.gold.get();
		else if(item.contains("skin"))
			item = "skin";
		else if(Options.get("eventBadges").contains(item))
			return null;
		
		int a = dat.get("Amount").getAsInt();
		
		quests = ArrayUtils.removeElement(quests, quest);
		
		return new Reward(item, a);
	}
	
	public HashSet<UnitType> getNeededUnitTypesForQuests() {
		HashSet<UnitType> ret = new HashSet<>();
		for(Quest q : quests)
			if(q.neededUnit != null)
				ret.add(q.neededUnit);
		return ret;
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
