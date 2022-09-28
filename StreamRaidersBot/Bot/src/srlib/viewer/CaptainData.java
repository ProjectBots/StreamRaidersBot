package srlib.viewer;

import com.google.gson.JsonObject;

import srlib.RaidType;

public class CaptainData {
	
	public final boolean isLive, isPlaying;
	public final String twitchDisplayName, twitchUserImage, twitchUserName, captainId;
	public final int pveWins, pveLoyaltyLevel;
	public final RaidType type;

	public CaptainData(JsonObject cap) {
		
		this.isLive = cap.get("isLive").getAsInt() == 1;
		this.isPlaying = cap.get("isPlaying").getAsInt() == 1;
		this.twitchDisplayName = cap.get("twitchDisplayName").getAsString();
		this.twitchUserImage = cap.get("twitchUserImage").getAsString();
		this.twitchUserName = cap.get("twitchUserName").getAsString();
		this.captainId = cap.get("userId").getAsString();
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
				.append(" - ")
				.append(isLive ? "live ":"")
				.append(isPlaying ? "playing ":"")
				.append(pveWins)
				.append(" ")
				.append(type.toString())
				.toString();
	}
	
}
