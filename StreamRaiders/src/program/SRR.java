package program;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http;
import include.JsonParser;
import program.Run.SilentException;

public class SRR {
	private static boolean ver_error = true;
	
	synchronized private static void printVerError(String ver) {
		StreamRaiders.set("clientVersion", ver);
		StreamRaiders.save();
		if(ver_error) {
			ver_error = false;
			System.out.println("new Client Version: " + ver);
		}
	}
	
	private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/0.0";
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
		public NoInternetException(Exception e) {
			super(e);
		}
	}
	
	public static class NotAuthorizedException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	
	public SRR(String cookies, String clientVersion) throws NoInternetException, OutdatedDataException, SilentException, NotAuthorizedException {
		this.cookies = cookies;
		this.clientVersion = clientVersion;
		reload();
	}
	
	public String reload() throws NoInternetException, OutdatedDataException, SilentException, NotAuthorizedException {
		userId = null;
		gameDataVersion = "";
		isCaptain = "";
		JsonObject raw = JsonParser.parseObj(getUser());
		String data = raw.getAsJsonObject("info").getAsJsonPrimitive("dataPath").getAsString();
		if(!data.equals(StreamRaiders.get("data"))) throw new OutdatedDataException(data);
		
		String ver = raw.getAsJsonObject("info").getAsJsonPrimitive("version").getAsString();
		if(!ver.equals(clientVersion)) {
			printVerError(ver);
			this.clientVersion = ver;
			raw = JsonParser.parseObj(getUser());
			constructor(raw);
			return ver;
		} else {
			constructor(raw);
			return null;
		}
	}
	
	private void constructor(JsonObject getUser) throws SilentException, NotAuthorizedException {
		this.gameDataVersion = getUser.getAsJsonObject("info").getAsJsonPrimitive("dataVersion").getAsString();
		try {
			JsonObject data = getUser.getAsJsonObject("data");
			this.isCaptain = "0";
			this.userId = data.getAsJsonPrimitive("userId").getAsString();
			if(userId.endsWith("c"))
				userId = data.getAsJsonPrimitive("otherUserId").getAsString();
		} catch (ClassCastException e) {
			JsonElement err = getUser.get(SRC.errorMessage);
			if(err.isJsonPrimitive() && err.getAsString().equals("User is not authorized.")) {
				throw new NotAuthorizedException();
			} else {
				StreamRaiders.log("SRR -> constructor: getUser=" + getUser, e);
				throw new Run.SilentException();
			}
		}
	}
	
	
	
	public Http getPost(String cn) {
		return getPost(cn, true);
	}

	private Http getPost(String cn, boolean addUser) {
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
	
	private String sendPost(Http post) throws NoInternetException {
		Exception e = null;
		for(int i=0; i<3; i++) {
			try {
				String p = post.sendUrlEncoded();
				
				Debug.print(post.getUrlArg("cn") + "\n" + post.getLastEntity() + "\n" + p, Debug.srlog);
				
				if(p.equals(""))
					continue;
				
				return p;
			} catch (Exception e1) {
				e = e1;
			}
		}
		throw new NoInternetException(e);
	}
	
	private String getUser() throws NoInternetException {
		Http post = getPost("getUser");
		post.addEncArg("skipDateCheck", "true");
		return sendPost(post);
	}
	
	
	public String unlockUnit(String unitType) throws NoInternetException {
		Http post = getPost("unlockUnit");
		post.addEncArg("unitType", unitType);
		return sendPost(post);
	}
	
	
	public String upgradeUnit(String unitType, String unitLevel, String unitId) throws NoInternetException {
		Http post = getPost("upgradeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		return sendPost(post);
	}
	
	
	public String specializeUnit(String unitType, String unitLevel, String unitId, String specializationUid) throws NoInternetException {
		Http post = getPost("specializeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		post.addEncArg("specializationUid", specializationUid);
		return sendPost(post);
	}
	
	
	public String getAvailableCurrencies() throws NoInternetException {
		return sendPost(getPost("getAvailableCurrencies"));
	}
	
	
	public String collectQuestReward(String slotId) throws NoInternetException {
		Http post = getPost("collectQuestReward");
		post.addEncArg("slotId", slotId);
		post.addEncArg("autoComplete", "False");
		return sendPost(post);
	}
	
	
	public String getUserQuests() throws NoInternetException {
		return sendPost(getPost("getUserQuests"));
	}
	
	
	public String getCurrentStoreItems() throws NoInternetException {
		return sendPost(getPost("getCurrentStoreItems"));
	}
	
	
	public String purchaseStoreItem(String itemId) throws NoInternetException {
		Http post = getPost("purchaseStoreItem");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	
	public String grantEventReward(String eventId, String rewardTier, boolean collectBattlePass) throws NoInternetException {
		Http post = getPost("grantEventReward");
		post.addEncArg("eventId", eventId);
		post.addEncArg("rewardTier", rewardTier);
		post.addEncArg("collectBattlePass", (collectBattlePass ? "True" : "False"));
		return sendPost(post);
	}
	
	
	public String getUserEventProgression() throws NoInternetException {
		Http post = getPost("getUserEventProgression", false);
		post.addEncArg("userId", "");
		post.addEncArg("isCaptain", isCaptain);
		return sendPost(post);
	}
	
	
	public String updateFavoriteCaptains(String captainId, boolean fav) throws NoInternetException {
		Http post = getPost("updateFavoriteCaptains");
		post.addEncArg("isFavorited", (fav ? "True" : "False"));
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String addPlayerToRaid(String captainId, String userSortIndex) throws NoInternetException {
		Http post = getPost("addPlayerToRaid");
		post.addEncArg("userSortIndex", userSortIndex);
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String leaveCaptain(String captainId) throws NoInternetException {
		Http post = getPost("leaveCaptain");
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	
	public String getCaptainsForSearch(int page, int resultsPerPage, boolean fav, boolean live, String mode, boolean searchForCaptain, String name) throws NoInternetException {
		JsonObject filter = new JsonObject();
		filter.addProperty("favorite", (fav ? "true" : "false"));
		if(name != null) filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if(live) filter.addProperty("isLive", "1");
		if(!mode.equals(SRC.Search.all))
			filter.addProperty("mode", mode);
		
		Http post = getPost("getCaptainsForSearch");
		post.addEncArg("page", ""+page);
		post.addEncArg("resultsPerPage", ""+resultsPerPage);
		post.addEncArg("filters", filter.toString());
		
		return sendPost(post);
	}

	
	public String getRaidPlan(String raidId) throws NoInternetException {
		Http post = getPost("getRaidPlan");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String purchaseChestItem(String itemId) throws NoInternetException {
		Http post = getPost("purchaseChestItem");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	
	public String getUserDungeonInfoForRaid(String raidId) throws NoInternetException {
		Http post = getPost("getUserDungeonInfoForRaid");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String getCurrentTime() throws NoInternetException {
		return sendPost(getPost("getCurrentTime"));
	}

	
	public String getRaid(String raidId) throws NoInternetException {
		Http post = getPost("getRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("maybeSendNotifs", "False");
		post.addEncArg("placementStartIndex", "0");
		return sendPost(post);
	}
	
	
	public String getActiveRaidsByUser() throws NoInternetException {
		Http post = getPost("getActiveRaidsByUser");
		post.addEncArg("placementStartIndices", "{}");
		return sendPost(post);
	}
	
	
	public String getMapData(String map) throws NoInternetException {
		Http get = new Http();
		get.setUrl("https://d2k2g0zg1te1mr.cloudfront.net/maps/" + map + ".txt");
		get.addHeader("User-Agent", userAgent);
		
		Exception e = null;
		for(int i=0; i<3; i++) {
			try {
				String ret = get.sendGet();
				
				if(ret.equals(""))
					continue;
				
				return ret;
			} catch (Exception e1) {
				e = e1;
			}
		}
		throw new NoInternetException(e);
	}
	
	
	public String getRaidStatsByUser(String raidId) throws NoInternetException {
		Http post = getPost("getRaidStatsByUser");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String addToRaid(String raidId, String placementData) throws NoInternetException {
		Http post = getPost("addToRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("placementData", placementData);
		return sendPost(post);
	}
	
	
	public String getUserUnits() throws NoInternetException {
		return sendPost(getPost("getUserUnits"));
	}
}
