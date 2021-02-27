package program;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import include.Http;

public class SRR {
	private static boolean ver_err = false;
	
	private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0";
	private String cookies = "";
	
	private String userId = "";
	private String isCaptain = "";
	private String gameDataVersion = "";
	private String clientVersion = "";
	private String clientPlatform = "WebGL";
	
	public String getUserId() {
		return userId;
	}
	
	public SRR(String cookies, String clientVersion) {
		this.cookies = cookies;
		this.clientVersion = clientVersion;
		
		JsonObject raw = json(getUser());
		String ver = raw.getAsJsonObject("info").getAsJsonPrimitive("version").getAsString();
		if(!ver.equals(clientVersion)) {
			if(!ver_err) {
				ver_err = true;
				System.err.println("Client version is outdated " + clientVersion + " -> " + ver);
				System.err.println("not critical but can cause issues");
			}
			this.clientVersion = ver;
			raw = json(getUser());
			constructor(raw);
		} else {
			constructor(raw);
		}
	}
	
	private void constructor(JsonObject getUser) {
		this.gameDataVersion = getUser.getAsJsonObject("info").getAsJsonPrimitive("dataVersion").getAsString();
		getUser = getUser.getAsJsonObject("data");
		this.userId = getUser.getAsJsonPrimitive("userId").getAsString();
		this.isCaptain = getUser.getAsJsonPrimitive("isCaptain").getAsString();
	}
	
	private static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	public Http getPost(String cn) {
		Http post = new Http();
		
		post.addHeader("User-Agent", userAgent);
		post.addHeader("Cookie", cookies);
		
		post.setUrl("https://www.streamraiders.com/api/game/");
		post.addUrlArg("cn", cn);
		
		return post;
	}
	
	public String getUser() {
		Http post = getPost("getUser");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"gameDataVersion", "",
				"command", "getUser",
				"skipDateCheck", "true",
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String updateFavoriteCaptains(String captainId, boolean fav) {
		
		Http post = getPost("updateFavoriteCaptains");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "updateFavoriteCaptains",
				"isFavorited", (fav ? "True" : "False"),
				"captainId", captainId,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String addPlayerToRaid(/*String raidId,*/ String captainId , String userSortIndex) {
		
		Http post = getPost("addPlayerToRaid");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "addPlayerToRaid",
				//"raidId", raidId,
				"userSortIndex", userSortIndex,
				"captainId", captainId,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String leaveCaptain(String captainId) {
		
		Http post = getPost("leaveCaptain");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "leaveCaptain",
				"captainId", captainId,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String getCaptainsForSearch(int page, int resultsPerPage, boolean fav, boolean live, boolean pve, int code, String name) {
		
		Http post = getPost("getCaptainsForSearch");
		
		String text = null;
		
		JsonObject filter = new JsonObject();
		filter.addProperty("favorite", (fav ? "true" : "false"));
		if(name != null) filter.addProperty("twitchUserName", name);
		if(live) filter.addProperty("isLive", "1");
		filter.addProperty("mode", (pve ? "pve" : "pvp"));
		if(code == 1) filter.addProperty("roomCodes", "false");
		if(code == 2) filter.addProperty("roomCodes", "true");
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getCaptainsForSearch",
				"page", ""+page,
				"resultsPerPage", ""+resultsPerPage,
				"filters", filter.toString(),
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}

	
	public String getRaidPlan(String raidId) {
		
		Http post = getPost("getRaidPlan");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getRaidPlan",
				"raidId", raidId,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String getCurrentTime() {
		Http post = getPost("getCurrentTime");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getCurrentTime",
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}

	public String getRaid(String raidId) {
		
		Http post = getPost("getRaid");
		
		String text = null;
		
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getRaid",
				"raidId", raidId,
				"maybeSendNotifs", "False",
				"placementStartIndex", "0",
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	
	
	public String getActiveRaidsByUser() {
		
		Http post = getPost("getActiveRaidsByUser");
		
		String text = null;
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getActiveRaidsByUser",
				"placementStartIndices", "{}",
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
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
		
		String text = null;
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getRaidStatsByUser",
				"raidId", raidId,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public String addToRaid(String raidId, String placementData) {

		Http post = getPost("addToRaid");
		
		String text = null;
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "addToRaid",
				"raidId", raidId,
				"placementData", placementData,
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	
	
	
	public String getUserUnits() {

		Http post = getPost("getUserUnits");
		
		String text = null;
		try {
			text = post.sendUrlEncoded(
				"userId", userId,
				"isCaptain", isCaptain,
				"gameDataVersion", gameDataVersion,
				"command", "getUserUnits",
				"clientVersion", clientVersion,
				"clientPlatform", clientPlatform
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
}
