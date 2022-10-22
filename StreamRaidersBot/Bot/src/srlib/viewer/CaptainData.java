package srlib.viewer;

import com.google.gson.JsonObject;

import srlib.RaidType;

public class CaptainData {
	
	public final boolean isLive, isPlaying;
	public final String twitchDisplayName, twitchUserImage, twitchUserName;
	public final int pveWins, pveLoyaltyLevel, captainId;
	public final RaidType type;

	public CaptainData(JsonObject cap) {
		
		this.isLive = cap.get("isLive").getAsInt() == 1;
		this.isPlaying = cap.get("isPlaying").getAsInt() == 1;
		this.twitchDisplayName = cap.get("twitchDisplayName").getAsString();
		this.twitchUserImage = cap.get("twitchUserImage").getAsString();
		this.twitchUserName = cap.get("twitchUserName").getAsString();
		String capId = cap.get("userId").getAsString();
		this.captainId = Integer.parseInt(capId.substring(0, capId.length()-2));
		this.pveWins = cap.get("pveWins").getAsInt();
		this.pveLoyaltyLevel = cap.get("pveLoyaltyLevel").getAsInt();
		this.type = RaidType.parseInt(cap.get("type").getAsInt());
	}
	
	
	@Override
	public String toString() {
		return new StringBuilder("{")
				.append(twitchDisplayName)
				.append("@")
				.append(captainId)
				.append("c - ")
				.append(isLive ? "live ":"")
				.append(isPlaying ? "playing ":"")
				.append(pveWins)
				.append(" ")
				.append(type.toString())
				.append("}")
				.toString();
	}
	
}
