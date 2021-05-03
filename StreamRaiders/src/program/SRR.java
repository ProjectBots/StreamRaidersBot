package program;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import com.google.gson.JsonObject;

import include.Http;
import include.JsonParser;

public class SRR {
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
	
	
	public SRR(String cookies, String clientVersion) throws Exception {
		this.cookies = cookies;
		this.clientVersion = clientVersion;
		reload();
	}
	
	public String reload() throws URISyntaxException, IOException, NoInternetException, OutdatedDataException {
		userId = null;
		gameDataVersion = "";
		isCaptain = "";
		JsonObject raw = JsonParser.parseObj(getUser());
		String data = raw.getAsJsonObject("info").getAsJsonPrimitive("dataPath").getAsString();
		if(!data.equals(StreamRaiders.get("data"))) throw new OutdatedDataException(data);
		
		String ver = raw.getAsJsonObject("info").getAsJsonPrimitive("version").getAsString();
		if(!ver.equals(clientVersion)) {
			this.clientVersion = ver;
			raw = JsonParser.parseObj(getUser());
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

	private Http getPost(String cn, boolean addUser) {
		Http post = new Http();
		
		post.addHeader("User-Agent", userAgent);
		post.addHeader("Cookie", cookies);
		
		post.setUrl("https://www.streamraiders.com/api/game/");
		post.addUrlArg("cn", cn);
		
		if(userId != null && addUser) {
			post.addEncArg("userId", userId);
			post.addEncArg("isCaptain", "0");
		}
		post.addEncArg("gameDataVersion", gameDataVersion);
		post.addEncArg("command", cn);
		post.addEncArg("clientVersion", clientVersion);
		post.addEncArg("clientPlatform", clientPlatform);
		
		return post;
	}
	
	private String sendPost(Http post) throws IOException, NoInternetException, URISyntaxException  {
		try {
			
			String p = post.sendUrlEncoded();
			/*
			String f = "";
			try {
				f = NEF.read("out.txt");
			} catch (IOException e) {}
			NEF.save("out.txt", f + "\n\n\n" + post.getUrlArg("cn") + "\n" + post.getLastEntity() + "\n" + p);
			*/
			return p;
		} catch (UnknownHostException e) {
			throw new NoInternetException();
		}
	}
	
	private String getUser() throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getUser");
		post.addEncArg("skipDateCheck", "true");
		return sendPost(post);
	}
	
	
	public String unlockUnit(String unitType) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("unlockUnit");
		post.addEncArg("unitType", unitType);
		return sendPost(post);
	}
	
	
	public String upgradeUnit(String unitType, String unitLevel, String unitId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("upgradeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		return sendPost(post);
	}
	
	
	public String specializeUnit(String unitType, String unitLevel, String unitId, String specializationUid) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("specializeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		post.addEncArg("specializationUid", specializationUid);
		return sendPost(post);
	}
	
	
	public String getAvailableCurrencies() throws URISyntaxException, IOException, NoInternetException {
		return sendPost(getPost("getAvailableCurrencies"));
	}
	
	
	public String collectQuestReward(String slotId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("collectQuestReward");
		post.addEncArg("slotId", slotId);
		post.addEncArg("autoComplete", "False");
		return sendPost(post);
	}
	
	
	public String getUserQuests() throws URISyntaxException, IOException, NoInternetException {
		return sendPost(getPost("getUserQuests"));
	}
	
	
	public String getCurrentStoreItems() throws URISyntaxException, IOException, NoInternetException {
		return sendPost(getPost("getCurrentStoreItems"));
	}
	
	
	public String purchaseStoreItem(String itemId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("purchaseStoreItem");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	
	public String grantEventReward(String eventId, String rewardTier, boolean collectBattlePass) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("grantEventReward");
		post.addEncArg("eventId", eventId);
		post.addEncArg("rewardTier", rewardTier);
		post.addEncArg("collectBattlePass", (collectBattlePass ? "True" : "False"));
		return sendPost(post);
	}
	
	
	public String getUserEventProgression() throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getUserEventProgression", false);
		post.addEncArg("userId", "");
		post.addEncArg("isCaptain", isCaptain);
		return sendPost(post);
	}
	
	
	public String updateFavoriteCaptains(String captainId, boolean fav) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("updateFavoriteCaptains");
		post.addEncArg("isFavorited", (fav ? "True" : "False"));
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String addPlayerToRaid(String captainId, String userSortIndex) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("addPlayerToRaid");
		post.addEncArg("userSortIndex", userSortIndex);
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String leaveCaptain(String captainId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("leaveCaptain");
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String getCaptainsForSearch(int page, int resultsPerPage, boolean fav, boolean live, boolean searchForCaptain, String name) throws URISyntaxException, IOException, NoInternetException {
		JsonObject filter = new JsonObject();
		filter.addProperty("favorite", (fav ? "true" : "false"));
		if(name != null) filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if(live) filter.addProperty("isLive", "1");
		filter.addProperty("mode", "pve");
		
		Http post = getPost("getCaptainsForSearch");
		post.addEncArg("page", ""+page);
		post.addEncArg("resultsPerPage", ""+resultsPerPage);
		post.addEncArg("filters", filter.toString());
		
		return sendPost(post);
	}

	
	public String getRaidPlan(String raidId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getRaidPlan");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String getCurrentTime() throws URISyntaxException, IOException, NoInternetException {
		return sendPost(getPost("getCurrentTime"));
	}

	
	public String getRaid(String raidId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("maybeSendNotifs", "False");
		post.addEncArg("placementStartIndex", "0");
		return sendPost(post);
	}
	
	
	public String getActiveRaidsByUser() throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getActiveRaidsByUser");
		post.addEncArg("placementStartIndices", "{}");
		return sendPost(post);
	}
	
	
	public String getMapData(String map) throws URISyntaxException, IOException, NoInternetException {
		Http get = new Http();
		get.setUrl("https://d2k2g0zg1te1mr.cloudfront.net/maps/" + map + ".txt");
		get.addHeader("User-Agent", userAgent);
		
		try {
			return get.sendGet();
		} catch (UnknownHostException e) {
			throw new NoInternetException();
		}
	}
	
	
	public String getRaidStatsByUser(String raidId) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("getRaidStatsByUser");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String addToRaid(String raidId, String placementData) throws URISyntaxException, IOException, NoInternetException {
		Http post = getPost("addToRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("placementData", placementData);
		return sendPost(post);
	}
	
	
	public String getUserUnits() throws URISyntaxException, IOException, NoInternetException {
		return sendPost(getPost("getUserUnits"));
	}
}
