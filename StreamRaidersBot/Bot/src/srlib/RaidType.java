package srlib;

import java.util.HashMap;

public enum RaidType {
	CAMPAIGN(1800, 0, 300, 1), DUNGEON(3600, 60, 100, 3 /*TODO find type*/), VERSUS(420, 60, 120, 2);
	
	public final int raidDuration, planningPeriodDuration, placementCooldownDuration, typeInt;
	private RaidType(int raidDuration, int planningPeriodDuration, int placementCooldownDuration, int typeInt) {
		this.raidDuration = raidDuration;
		this.planningPeriodDuration = planningPeriodDuration;
		this.placementCooldownDuration = placementCooldownDuration;
		this.typeInt = typeInt;
	}
	
	private static HashMap<Integer, RaidType> from_int = new HashMap<>();
	static {
		for(RaidType rt : values())
			from_int.put(rt.typeInt, rt);
	}
	
	public static RaidType parseInt(int type) {
		return from_int.get(type);
	}
	
}
