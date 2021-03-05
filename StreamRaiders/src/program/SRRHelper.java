package program;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SRRHelper {

	
	
	private SRR req = null;
	private Unit[] units = new Unit[0];
	private Raid[] raids = new Raid[0];
	private Map map = null;
	
	
	public SRRHelper(String cookies, String clientVersion) {
		req = new SRR(cookies, clientVersion);
		updateUnits();
	}
	
	public SRR getSRR() {
		return req;
	}
	
	public String placeUnit(Raid raid, Unit unit, boolean epic, int x, int y) {
		JsonObject res = json(req.addToRaid(raid.get(SRC.Raid.raidId), createPlacementData(unit, epic, map.getAsSRCoords(new int[] {x, y}))));
		try {
			return res.getAsJsonPrimitive("errorMessage").getAsString();
		} catch (Exception e) {}
		return null;
	}
	
	public JsonObject[][] getMap() {
		return map.getMap();
	}
	
	public void loadMap(Raid raid) {
		map = new Map(json(req.getMapData(raid.get(SRC.Raid.battleground))), jsonArr(raid.get(SRC.Raid.placementsSerialized)));
		/*
		for(int i=0; i<max_attempts; i++) {
			try {
				
				break;
			} catch (Exception e) {}
		}
		*/
	}
	
	public boolean testPos(boolean epic, int x, int y) {
		return map.testPos(epic, new int[] {x, y});
	}
	
	public double[] getSRPos(int x, int y) {
		return map.getAsSRCoords(new int[] {x, y});
	}
	
	public JsonArray getPlayerRects() {
		return map.getPlayerRects();
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
		req.addPlayerToRaid(/*captain.getAsJsonPrimitive("raidId").getAsString(),*/ captain.getAsJsonPrimitive("userId").getAsString(), userSortIndex);
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
		JsonObject jo = json(req.getActiveRaidsByUser());
		JsonArray rs = jo.getAsJsonArray("data");
		raids = new Raid[0];
		for(int i=0; i<rs.size(); i++) {
			raids = add(raids, new Raid(rs.get(i).getAsJsonObject()));
		}
		return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
	}
	
	
	public JsonArray search(int page, int resultsPerPage, boolean fav, boolean live, boolean searchForCaptain, String name) {
		JsonObject raw = json(req.getCaptainsForSearch(page, resultsPerPage, fav, live, searchForCaptain, name)).getAsJsonObject("data");
		
		JsonArray loyalty = raw.getAsJsonArray("pveLoyalty");
		JsonArray captains = raw.getAsJsonArray("captains");
		
		for(int i=0; i<loyalty.size(); i++) {
			captains.get(i).getAsJsonObject().addProperty("pveWins", loyalty.get(i).getAsJsonObject().getAsJsonPrimitive("pveWins").getAsInt());
			captains.get(i).getAsJsonObject().addProperty("pveLoyaltyLevel", loyalty.get(i).getAsJsonObject().getAsJsonPrimitive("pveLoyaltyLevel").getAsInt());
		}
		
		return captains;
	}
	
	public boolean setFavorite(JsonObject captain, boolean b) {
		return json(req.updateFavoriteCaptains(captain.getAsJsonPrimitive("userId").getAsString(), b)).getAsJsonPrimitive("status").getAsString().contains("success");
	}
	
	public String updateUnits() {
		JsonObject jo = json(req.getUserUnits());
		JsonArray u = jo.getAsJsonArray("data");
		units = new Unit[0];
		for(int i=0; i<u.size(); i++) {
			units = add(units, new Unit(u.get(i).getAsJsonObject()));
		}
		return jo.getAsJsonObject("info").getAsJsonPrimitive("serverTime").getAsString();
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
			}
		}
		return ret;
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
	
	private static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	private static JsonArray jsonArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
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
