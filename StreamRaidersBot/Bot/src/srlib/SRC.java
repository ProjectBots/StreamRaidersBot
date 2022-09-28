package srlib;

public class SRC {
	
	
	public static class Map {
		public static final String isPlayerRect = "isPlayerRect";
		public static final String isEnemyRect = "isEnemyRect";
		public static final String isHoldRect = "isHoldRect";
		public static final String isEnemy = "isEnemy";
		public static final String isAllied = "isAllied";
		public static final String isNeutral = "isNeutral";
		public static final String isObstacle = "isObstacle";
		public static final String canFlyOver = "canFlyOver";
		public static final String canWalkOver = "canWalkOver";
		public static final String isEmpty = "isEmpty";
		public static final String isOccupied = "isOccupied";
		public static final String isEpic = "isEpic";
		public static final String isPlayer = "isPlayer";
		public static final String isCaptain = "isCaptain";
		public static final String isSelf = "isSelf";
		public static final String isOther = "isOther";
	}
	
	public static class Store {
		public static final String dungeon = "Dungeon";
		public static final String event = "Event";
		public static final String bones = "Bones";
	}
	
	public static class Helper {
		public static final int canPlaceUnit = 0;
		public static final int canUpgradeUnit = 4;
		public static final int canUnlockUnit = 5;
		public static final int isReward = 1;
		public static final int isOffline = 2;
		public static final int all = 3;
	}
	
	public static class Search {
		public static final String all = "";
		public static final String campaign = "campaign";
		public static final String dungeons = "dungeons";
	}
	
	public static class Unit {
		public static final String level = "level";
		public static final String skin = "skin";
		public static final String cooldownTime = "cooldownTime";
		public static final String unitId = "unitId";
		public static final String specializationUid = "specializationUid";
		public static final String specializationDisName = "specializationDisName";
		public static final String disName = "disName";
	}
	
	public static class BackEndHandler {
		public static final int all = 0;
		public static final int isRaidReward = 1;
		public static final int isUnitPlaceable = 3;
		public static final int isUnitUpgradeable = 4;
		public static final int isUnitUnlockable = 5;
	}
	
	public static class Run {
		public static final String chests = "chests";
		public static final String bought = "bought";
		public static final String event = "event";
		
		public static final int exploitThreadCount = 50;
	}
	
	public static class Manager {
		public static final int start = 0;
		public static final int skip = 1;
		public static final int stop = 3;
	}
	
	public static final String errorMessage = "errorMessage";

}	
