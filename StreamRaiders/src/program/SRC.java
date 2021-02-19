package program;

import java.text.SimpleDateFormat;

public class SRC {
	
	public static final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static class Raid {
		public static final String raidId = "raidId";
		public static final String captainId = "captainId";
		public static final String lastUnitPlacedTime = "uslastUnitPlacedTimeerId";
		public static final String hasViewedResults = "hasViewedResults";
		public static final String userSortIndex = "userSortIndex";
		public static final String chestAwarded = "chestAwarded";
		public static final String twitchDisplayName = "twitchDisplayName";
		public static final String isPlaying = "isPlaying";
		public static final String isLive = "isLive";
		public static final String creationDate = "creationDate";
		public static final String startTime = "startTime";
		public static final String battleground = "battleground";
		public static final String endTime = "endTime";
		public static final String battleResult = "battleResult";
		public static final String goldAwarded = "goldAwarded";
		public static final String rewards = "rewards";
		public static final String postBattleComplete = "postBattleComplete";
		public static final String placementEndTime = "placementEndTime";
		public static final String hasRecievedRewards = "hasRecievedRewards";
		public static final String placementCount = "placementCount";
		public static final String placementsSerialized = "placementsSerialized";
		public static final String pveWins = "pveWins";
		public static final String pveLoyaltyLevel = "pveLoyaltyLevel";
	}
	
	public static class Helper {
		public static final int canPlaceUnit = 0;
		public static final int isReward = 1;
		public static final int isOffline = 2;
		public static final int all = 3;
	}
	
	public static class Unit {
		public static final String unitType = "unitType";
		public static final String level = "level";
		public static final String skin = "skin";
		public static final String cooldownTime = "cooldownTime";
		public static final String unitId = "unitId";
		public static final String specializationUid = "specializationUid";
		public static final String rank = "rank";
	}
	

}	
