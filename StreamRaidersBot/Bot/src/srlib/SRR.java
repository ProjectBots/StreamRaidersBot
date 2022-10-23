package srlib;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import include.Json;

public class SRR {
	
	private String proxyDomain = null;
	private int proxyPort = 0;
	private boolean proxyMandatory;
	private String proxyUser;
	private String proxyPass;
	
	private final String cid;
	
	private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/0.0";
	
	public void setProxy(String domain, int port, String username, String password, boolean mandatory) {
		proxyDomain = domain;
		proxyPort = port;
		proxyUser = username;
		proxyPass = password;
		proxyMandatory = mandatory;
	}
	
	public void setUserAgent(String ua) {
		userAgent = ua;
	}
	
	private final String cookies;
	private String viewerUserId = null;
	private String captainUserId = null;
	private boolean playsAsCaptain;
	private boolean canPlayAsCaptain;
	private String gameDataVersion = "";
	private String clientVersion = "";
	private static final String clientPlatform = "WebGL";
	
	public String getViewerUserId() {
		return viewerUserId;
	}
	
	public String getCaptainUserId() {
		return captainUserId;
	}
	
	public boolean canPlayAsCaptain() {
		return canPlayAsCaptain;
	}
	
	public boolean playsAsCaptain() {
		return playsAsCaptain;
	}
	
	private static ArrayList<String> allUserIds = new ArrayList<String>();
	
	public static ArrayList<String> getAllUserIds() {
		return allUserIds;
	}
	
	synchronized public static void addUserId(String uid) {
		if(!allUserIds.contains(uid))
			allUserIds.add(uid);
	}
	
	public static String getData(String dataPath) {
		Http get = new Http();
		get.setUrl(dataPath);
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/0.0");
		
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
		private String dataPath;
		private String serverTime;
		public OutdatedDataException(String newDataPath, String serverTime) {
			super("the datapath is outdated");
			dataPath = newDataPath;
			this.serverTime = serverTime;
		}
		public String getDataPath() {
			return dataPath;
		}
		public String getServerTime() {
			return serverTime;
		}
	}
	
	public static class NotAuthorizedException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	
	public SRR(String cid, String clientVersion) throws NoConnectionException, OutdatedDataException, NotAuthorizedException {
		this.cid = cid;
		this.cookies = Configs.getPStr(cid, Configs.cookies);
		this.clientVersion = clientVersion;
		reload();
		addUserId(viewerUserId);
		if(captainUserId != null);
			addUserId(captainUserId);
	}
	
	public String reload() throws NoConnectionException, OutdatedDataException, NotAuthorizedException {
		viewerUserId = null;
		gameDataVersion = "";
		JsonObject raw = Json.parseObj(getUser());
		JsonObject info = raw.getAsJsonObject("info");
		String datapath = info.get("dataPath").getAsString();
		
		if(!datapath.equals(Options.get("data")))
			throw new OutdatedDataException(datapath, info.get("serverTime").getAsString());
		
		String ver = info.get("version").getAsString();
		
		if(!ver.equals(clientVersion)) {
			this.clientVersion = ver;
			raw = Json.parseObj(getUser());
			Options.set("clientVersion", ver);
			Options.save();
		} else
			ver = null;
		
		constructor(raw);
		return ver;
	}
	
	private void constructor(JsonObject getUser) throws NotAuthorizedException {
		this.gameDataVersion = getUser.getAsJsonObject("info").get("dataVersion").getAsString();
		try {
			JsonObject data = getUser.getAsJsonObject("data");
			canPlayAsCaptain = data.get("hasCaptainPrivileges").getAsBoolean();
			if(!canPlayAsCaptain) {
				this.viewerUserId = data.get("userId").getAsString();
				playsAsCaptain = false;
			} else {
				String uid = data.get("userId").getAsString();
				String oid = data.get("otherUserId").getAsString();
				if(data.get("isCaptain").getAsInt() == 0) {
					playsAsCaptain = false;
					captainUserId = oid;
					viewerUserId = uid;
				} else {
					playsAsCaptain = true;
					captainUserId = uid;
					viewerUserId = oid;
				}
			}
		} catch (ClassCastException e) {
			JsonElement err = getUser.get(SRC.errorMessage);
			if(err.isJsonPrimitive() && err.getAsString().equals("User is not authorized.")) {
				throw new NotAuthorizedException();
			} else {
				Logger.print("SRR -> constructor: err=failed to get User, getUser=" + getUser, Logger.runerr, Logger.fatal, cid, null, true);
			}
		}
	}
	
	
	public Http getPost(String cn) {
		return getPost(cn, true);
	}

	
	private Http getPost(String cn, boolean addUser) {
		Http post = getPurePost(cn);
		
		if(viewerUserId != null && addUser) {
			post.addEncArg("userId", playsAsCaptain ? captainUserId : viewerUserId);
			post.addEncArg("isCaptain", playsAsCaptain ? "1" : "0");
		}
		post.addEncArg("gameDataVersion", gameDataVersion);
		post.addEncArg("clientVersion", clientVersion);
		post.addEncArg("clientPlatform", clientPlatform);
		
		return post;
	}
	
	private Http getPurePost(String cn) {
		Http post = new Http();
		if(proxyDomain != null)
			post.setProxy(proxyDomain, proxyPort, proxyUser, proxyPass);
		
		post.addHeader("User-Agent", userAgent);
		post.addHeader("Cookie", cookies);
		
		post.setUrl("https://www.streamraiders.com/api/game/");
		post.addUrlArg("cn", cn);
		
		post.addEncArg("command", cn);
		
		return post;
	}
	
	
	private String sendPost(Http post) throws NoConnectionException {
		String p;
		try {
			try {
				p = post.sendUrlEncoded();
			} catch (URISyntaxException e) {
				throw new NoConnectionException(e);
			}
		} catch (NoConnectionException e) {
			if(proxyMandatory || !post.isProxyEnabled())
				throw e;
			post.setProxy(null, 0, null, null);
			try {
				p = post.sendUrlEncoded();
			} catch (URISyntaxException e1) {
				throw new NoConnectionException(e);
			}
		}
		
		if(p.contains("\"errorMessage\":\""))
			Logger.print(post.getUrlArg("cn") + "\n" + post.getPayloadAsString().replace("&", ", ") + "\n" + p, Logger.srerr, Logger.warn, cid, null);
		else
			Logger.print(post.getUrlArg("cn") + "\n" + post.getPayloadAsString().replace("&", ", ") + "\n" + p, Logger.srlog, Logger.info, cid, null);
		
		return p;
	}
	
	public String getUser() throws NoConnectionException {
		Http post = getPost("getUser", false);
		post.addEncArg("skipDateCheck", "true");
		return sendPost(post);
	}
	
	public String switchUserAccountType() throws NoConnectionException {
		return sendPost(getPurePost("switchUserAccountType"));
	}
	
	
	public String unlockUnit(String unitType) throws NoConnectionException {
		Http post = getPost("unlockUnit");
		post.addEncArg("unitType", unitType);
		return sendPost(post);
	}
	
	
	public String upgradeUnit(String unitType, String unitLevel, String unitId) throws NoConnectionException {
		Http post = getPost("upgradeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		return sendPost(post);
	}
	
	
	public String specializeUnit(String unitType, String unitLevel, String unitId, String specializationUid) throws NoConnectionException {
		Http post = getPost("specializeUnit");
		post.addEncArg("unitType", unitType);
		post.addEncArg("unitLevel", unitLevel);
		post.addEncArg("unitId", unitId);
		post.addEncArg("specializationUid", specializationUid);
		return sendPost(post);
	}
	
	
	public String getAvailableCurrencies() throws NoConnectionException {
		return sendPost(getPost("getAvailableCurrencies"));
	}
	
	
	public String collectQuestReward(String slotId) throws NoConnectionException {
		Http post = getPost("collectQuestReward");
		post.addEncArg("slotId", slotId);
		post.addEncArg("autoComplete", "False");
		return sendPost(post);
	}
	
	
	public String getUserQuests() throws NoConnectionException {
		return sendPost(getPost("getUserQuests"));
	}
	
	
	public String getCurrentStoreItems() throws NoConnectionException {
		return sendPost(getPost("getCurrentStoreItems"));
	}
	
	
	public String purchaseStoreItem(String itemId) throws NoConnectionException {
		Http post = getPost("purchaseStoreItem");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	public String grantDailyDrop() throws NoConnectionException {
		Http post = getPost("grantDailyDrop");
		post.addEncArg("storeUid", "dailydrop");
		return sendPost(post);
	}
	
	
	public String grantEventReward(String eventId, String rewardTier, boolean collectBattlePass) throws NoConnectionException {
		Http post = getPost("grantEventReward");
		post.addEncArg("eventId", eventId);
		post.addEncArg("rewardTier", rewardTier);
		post.addEncArg("collectBattlePass", (collectBattlePass ? "True" : "False"));
		return sendPost(post);
	}
	
	
	public String getUserEventProgression() throws NoConnectionException {
		Http post = getPost("getUserEventProgression", false);
		post.addEncArg("userId", "");
		post.addEncArg("isCaptain", playsAsCaptain ? "1" : "0");
		return sendPost(post);
	}
	
	
	public String updateFavoriteCaptains(String captainId, boolean fav) throws NoConnectionException {
		Http post = getPost("updateFavoriteCaptains");
		post.addEncArg("isFavorited", (fav ? "True" : "False"));
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String addPlayerToRaid(String captainId, String userSortIndex) throws NoConnectionException {
		Http post = getPost("addPlayerToRaid");
		post.addEncArg("userSortIndex", userSortIndex);
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	public String leaveCaptain(String captainId) throws NoConnectionException {
		Http post = getPost("leaveCaptain");
		post.addEncArg("captainId", captainId);
		return sendPost(post);
	}
	
	
	
	public String getCaptainsForSearch(String page, String resultsPerPage, String seed, boolean fav, boolean live, Boolean roomCodes, String mode, boolean searchForCaptain, String name) throws NoConnectionException {
		JsonObject filter = new JsonObject();
		filter.addProperty("ambassadors", "false");
		if(fav)
			filter.addProperty("favorite", "true");
		if(name != null) 
			filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if(live) 
			filter.addProperty("isLive", "1");
		if(roomCodes != null)
			filter.addProperty("roomCodes", ""+roomCodes);
		if(mode != null)
			filter.addProperty("mode", mode);
		
		Http post = getPost("getCaptainsForSearch");
		post.addEncArg("page", page);
		post.addEncArg("resultsPerPage", resultsPerPage);
		post.addEncArg("filters", filter.toString());
		post.addEncArg("seed", seed==null?"0":seed);
		
		return sendPost(post);
	}

	
	public String getRaidPlan(String raidId) throws NoConnectionException {
		Http post = getPost("getRaidPlan");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String purchaseChestItem(String itemId) throws NoConnectionException {
		Http post = getPost("purchaseChestItem");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	
	public String purchaseStoreSkin(String itemId) throws NoConnectionException {
		Http post = getPost("purchaseStoreSkin");
		post.addEncArg("itemId", itemId);
		return sendPost(post);
	}
	
	
	public String equipSkin(String unitId, String skinUid, String isEquipped) throws NoConnectionException {
		Http post = getPost("equipSkin");
		post.addEncArg("unitId", unitId);
		post.addEncArg("skinUid", skinUid);
		post.addEncArg("isEquipped", isEquipped);
		return sendPost(post);
	}
	
	
	public String purchaseStoreRefresh() throws NoConnectionException {
		Http post = getPost("purchaseStoreRefresh");
		return sendPost(post);
	}
	
	
	public String getUserDungeonInfoForRaid(String raidId) throws NoConnectionException {
		Http post = getPost("getUserDungeonInfoForRaid");
		post.addEncArg("raidId", raidId);
		return sendPost(post);
	}
	
	
	public String getCurrentTime() throws NoConnectionException {
		return sendPost(getPost("getCurrentTime"));
	}

	
	public String getRaid(String raidId) throws NoConnectionException {
		Http post = getPost("getRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("maybeSendNotifs", "False");
		post.addEncArg("placementStartIndex", "0");
		return sendPost(post);
	}
	
	
	public String getActiveRaidsByUser() throws NoConnectionException {
		Http post = getPost("getActiveRaidsByUser");
		post.addEncArg("placementStartIndices", "{}");
		return sendPost(post);
	}
	
	private static final Set<String> mapPaths = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("https://d1vngzyege2qd5.cloudfront.net/prod1/");
			add("https://d2k2g0zg1te1mr.cloudfront.net/maps/");
		}
	});
	
	public String getMapData(String map) throws NoConnectionException {
		String ret;
		for(String path : mapPaths) {
			ret = getMapData(map, path);
			if(ret != null && !ret.contains("AccessDenied"))
				return ret;
		}
		return null;
	}
	
	private String getMapData(String map, String path) throws NoConnectionException {
		Http get = new Http();
		get.setUrl(path + map + ".txt");
		get.addHeader("User-Agent", userAgent);
		try {
			return get.sendGet();
		} catch (URISyntaxException e) {
			throw new NoConnectionException(e);
		}
	}
	
	
	public String getRaidStatsByUser(String raidId) throws NoConnectionException {
		Http post = getPost("getRaidStatsByUser");
		post.addEncArg("raidId", raidId);
		String ret = sendPost(post);
		return ret;
	}
	
	
	public String addToRaid(String raidId, String placementData) throws NoConnectionException {
		Http post = getPost("addToRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("placementData", placementData);
		return sendPost(post);
	}
	
	
	public String getUserUnits() throws NoConnectionException {
		return sendPost(getPost("getUserUnits"));
	}
	
	public String getUserItems() throws NoConnectionException {
		return sendPost(getPost("getUserItems"));
	}
	
	public String grantTeamReward() throws NoConnectionException {
		return sendPost(getPost("grantTeamReward"));
	}
	
	public String grantEventQuestMilestoneReward() throws NoConnectionException {
		return sendPost(getPost("grantEventQuestMilestoneReward"));
	}
	
	public String getOpenCountTrackedChests() throws NoConnectionException {
		return sendPost(getPost("getOpenCountTrackedChests"));
	}
	
	public String redeemProductCode(String code) throws NoConnectionException {
		Http post = getPost("redeemProductCode");
		post.addEncArg("actionSource", "store_cta");
		post.addEncArg("code", code);
		return sendPost(post);
	}
	
	public String extractSoul(String unitId) throws NoConnectionException {
		Http post = getPost("extractSoul");
		post.addEncArg("unitId", unitId);
		return sendPost(post);
	}
	
	public String getUserSouls() throws NoConnectionException {
		return sendPost(getPost("getUserSouls"));
	}
	
	public String equipSoul(String soulId, String unitId) throws NoConnectionException {
		Http post = getPost("equipSoul");
		post.addEncArg("soulId", soulId);
		post.addEncArg("unitId", unitId);
		return sendPost(post);
	}
	
	public String unequipSoul(String soulId) throws NoConnectionException {
		Http post = getPost("unequipSoul");
		post.addEncArg("soulId", soulId);
		return sendPost(post);
	}
}
