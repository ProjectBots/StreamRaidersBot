package srlib;

public class SRC {
	
	public static class Store {
		public static final String dungeon = "Dungeon";
		public static final String event = "Event";
		public static final String bones = "Bones";
	}
	
	
	public static class Search {
		public static final String all = null;
		public static final String campaign = "campaign";
		public static final String dungeon = "dungeons";
		public static final String versus = "versus";
	}
	
	public static class BackEnd {
		public static final int all = 0;
		public static final int isRaidReward = 1;
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
