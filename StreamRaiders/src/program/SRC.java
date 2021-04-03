package program;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SRC {
	
	private static final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	synchronized public static Date dateParse(String in) throws ParseException {
		return date.parse(in);
	}
	
	public static class Map {
		public static final String isPlayerRect = "isPlayerRect";
		public static final String isEnemyRect = "isEnemyRect";
		public static final String isHoldRect = "isHoldRect";
		public static final String isEnemy = "isEnemy";
		public static final String isAllied = "isAllied";
		public static final String isObstacle = "isObstacle";
		public static final String canFlyOver = "canFlyOver";
		public static final String canWalkOver = "canWalkOver";
		public static final String isEmpty = "isEmpty";
		public static final String isOccupied = "isOccupied";
		public static final String isEpic = "isEpic";
		public static final String isPlayer = "isPlayer";
	}

	public static class Store {
		public static final int all = 0;
		public static final int notPurchased = 1;
	}
	
	public static class QuestSlotIds {
		public static final String daily = "questslot_viewer_daily";
		public static final String event = "questslot_viewer_event_daily";
		public static final String ftue = "questslot_viewer_ftue";
		public static final String weekly = "questslot_viewer_weekly";
	}
	
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
		public static final String nodeId = "nodeId";
	}
	
	public static class Helper {
		public static final int canPlaceUnit = 0;
		public static final int canUpgradeUnit = 4;
		public static final int canUnlockUnit = 5;
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
