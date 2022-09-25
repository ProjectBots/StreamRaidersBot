package srlib.viewer;

public enum RaidType {
	CAMPAIGN(1800, 0, 300), DUNGEON(3600, 60, 100), VERSUS(420, 60, 120);
	
	public final int raidDuration, planningPeriodDuration, placementCooldownDuration;
	private RaidType(int raidDuration, int planningPeriodDuration, int placementCooldownDuration) {
		this.raidDuration = raidDuration;
		this.planningPeriodDuration = planningPeriodDuration;
		this.placementCooldownDuration = placementCooldownDuration;
	}
}
