package program;

import java.net.UnknownHostException;

import com.google.gson.JsonObject;

import include.Http;
import include.JsonParser;

public class SRR {
	private static boolean ver_err = false;
	
	synchronized private static void printVerErr(String clientVersion, String ver) {
		if(!ver_err) {
			ver_err = true;
			System.err.println("Client version is outdated " + clientVersion + " -> " + ver);
			System.err.println("not critical but can cause issues");
		}
	}
	
	private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0";
	private String cookies = "";
	
	private String userId = null;
	private String isCaptain = "";
	private String gameDataVersion = "";
	private String clientVersion = "";
	private String clientPlatform = "WebGL";
	
	public String getUserId() {
		return userId;
	}
	
	
	public static String getData(String dataPath) {
		
		Http get = new Http();
		get.setUrl(dataPath);
		get.addHeader("User-Agent", userAgent);
		
		String ret = null;
		try {
			ret = get.sendGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	public static class OutdatedDataException extends Exception {
		
		private static final long serialVersionUID = 1L;

		private String dataPath = null;
		
		public OutdatedDataException(String newDataPath) {
			super("the datapath is outdated");
			dataPath = newDataPath;
		}
		
		public String getDataPath() {
			return dataPath;
		}
	}
	
	public static class NoInternetException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	
	public SRR(String cookies, String clientVersion) throws OutdatedDataException, NoInternetException {
		this.cookies = cookies;
		this.clientVersion = clientVersion;
		reload();
	}
	
	public String reload() throws OutdatedDataException, NoInternetException {
		userId = null;
		gameDataVersion = "";
		isCaptain = "";
		JsonObject raw = JsonParser.json(getUser());
		String data = raw.getAsJsonObject("info").getAsJsonPrimitive("dataPath").getAsString();
		if(!data.equals(StreamRaiders.get("data"))) throw new OutdatedDataException(data);
		
		String ver = raw.getAsJsonObject("info").getAsJsonPrimitive("version").getAsString();
		if(!ver.equals(clientVersion)) {
			printVerErr(clientVersion, ver);
			this.clientVersion = ver;
			raw = JsonParser.json(getUser());
			constructor(raw);
			return ver;
		} else {
			constructor(raw);
			return null;
		}
	}
	
	private void constructor(JsonObject getUser) {
		this.gameDataVersion = getUser.getAsJsonObject("info").getAsJsonPrimitive("dataVersion").getAsString();
		getUser = getUser.getAsJsonObject("data");
		this.userId = getUser.getAsJsonPrimitive("userId").getAsString();
		this.isCaptain = getUser.getAsJsonPrimitive("isCaptain").getAsString();
	}
	
	
	public Http getPost(String cn) {
		return getPost(cn, true);
	}

	public Http getPost(String cn, boolean addUser) {
		Http post = new Http();
		
		post.addHeader("User-Agent", userAgent);
		post.addHeader("Cookie", cookies);
		
		post.setUrl("https://www.streamraiders.com/api/game/");
		post.addUrlArg("cn", cn);
		
		if(userId != null && addUser) {
			post.addEncArg("userId", userId);
			post.addEncArg("isCaptain", isCaptain);
		}
		post.addEncArg("gameDataVersion", gameDataVersion);
		post.addEncArg("command", cn);
		post.addEncArg("clientVersion", clientVersion);
		post.addEncArg("clientPlatform", clientPlatform);
		
		return post;
	}
	
	
	private String getUser() throws NoInternetException {
		Http post = getPost("getUser");
		post.addEncArg("skipDateCheck", "true");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (UnknownHostException e) {
			throw new NoInternetException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String unlockUnit(String unitType) {
		Http post = getPost("unlockUnit");
		post.addEncArg("unitType", unitType);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String upgradeUnit(String unitType, String unitLevel, String unitId) {
		Http post = getPost("upgradeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String specializeUnit(String unitType, String unitLevel, String unitId, String specializationUid) {
		Http post = getPost("specializeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		post.addEncArg("specializationUid", specializationUid);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getAvailableCurrencies() {
		Http post = getPost("getAvailableCurrencies");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String collectQuestReward(String slotId) {
		Http post = getPost("collectQuestReward");
		post.addEncArg("slotId", slotId);
		post.addEncArg("autoComplete", "False");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserQuests() {
		Http post = getPost("getUserQuests");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getCurrentStoreItems() {
		Http post = getPost("getCurrentStoreItems");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String purchaseStoreItem(String itemId) {
		Http post = getPost("purchaseStoreItem");
		post.addEncArg("itemId", itemId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String grantEventReward(String eventId, String rewardTier, boolean collectBattlePass) {
		Http post = getPost("grantEventReward");
		post.addEncArg("eventId", eventId);
		post.addEncArg("rewardTier", rewardTier);
		post.addEncArg("collectBattlePass", (collectBattlePass ? "True" : "False"));
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserEventProgression() {
		Http post = getPost("getUserEventProgression", false);
		post.addEncArg("userId", "");
		post.addEncArg("isCaptain", isCaptain);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String updateFavoriteCaptains(String captainId, boolean fav) {
		
		Http post = getPost("updateFavoriteCaptains");
		post.addEncArg("isFavorited", (fav ? "True" : "False"));
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String addPlayerToRaid(String captainId, String userSortIndex) {
		
		Http post = getPost("addPlayerToRaid");
		post.addEncArg("userSortIndex", userSortIndex);
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String leaveCaptain(String captainId) {
		
		Http post = getPost("leaveCaptain");
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getCaptainsForSearch(int page, int resultsPerPage, boolean fav, boolean live, boolean searchForCaptain, String name) {
		
		JsonObject filter = new JsonObject();
		filter.addProperty("favorite", (fav ? "true" : "false"));
		if(name != null) filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if(live) filter.addProperty("isLive", "1");
		filter.addProperty("mode", "pve");
		
		Http post = getPost("getCaptainsForSearch");
		post.addEncArg("page", ""+page);
		post.addEncArg("resultsPerPage", ""+resultsPerPage);
		post.addEncArg("filters", filter.toString());
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	
	public String getRaidPlan(String raidId) {
		
		Http post = getPost("getRaidPlan");
		post.addEncArg("raidId", raidId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getCurrentTime() {
		Http post = getPost("getCurrentTime");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	
	public String getRaid(String raidId) {
		
		Http post = getPost("getRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("maybeSendNotifs", "False");
		post.addEncArg("placementStartIndex", "0");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getActiveRaidsByUser() {
		
		Http post = getPost("getActiveRaidsByUser");
		post.addEncArg("placementStartIndices", "{}");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getMapData(String map) {
		
		Http get = new Http();
		get.setUrl("https://d2k2g0zg1te1mr.cloudfront.net/maps/" + map + ".txt");
		get.addHeader("User-Agent", userAgent);
		
		String ret = null;
		try {
			ret = get.sendGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	public String getRaidStatsByUser(String raidId) {

		Http post = getPost("getRaidStatsByUser");
		post.addEncArg("raidId", raidId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String addToRaid(String raidId, String placementData) {

		Http post = getPost("addToRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("placementData", placementData);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserUnits() {

		Http post = getPost("getUserUnits");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
}
