package srlib.map;

public enum PlacementRectType {
	ENEMY("EnemyPlacementRects"),
	PLAYER("PlayerPlacementRects"),
	HOLDING("HoldingZoneRects");
	
	public final String name;
	private PlacementRectType(String name) {
		this.name = name;
	}
}
