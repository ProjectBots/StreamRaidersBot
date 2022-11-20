package srlib.map;

import srlib.units.UnitType;

public class Place {
	
	public boolean isEmpty() {
		return !(isOccupied || isEntity || (isObstacle && !canWalkOver));
	}
	
	//	general
	boolean isOccupied = false;
	String plan = null;
	PlacementRectType rect = null;
	
	public boolean isOccupied() {
		return isOccupied;
	}
	
	public String getPlanType() {
		return plan;
	}
	
	public PlacementRectType getPlacementRectType() {
		return rect;
	}
	
	//	obstacles
	boolean isObstacle = false;
	boolean canWalkOver;
	boolean canFlyOver;
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean canWalkOver() {
		return !isObstacle || canWalkOver;
	}
	
	public boolean canFlyOver() {
		return !isObstacle || canFlyOver;
	}
	
	
	//	units
	boolean isEntity = false;
	Team team;
	String userId;
	String twitchDisplayName;
	UnitType type;
	boolean isEpic;
	boolean isCaptain;
	boolean isSelf;
	boolean isOther;
	
	public Team getTeam() {
		if(!isEntity)
			return null;
		return team;
	}
	
	public String getUserId() {
		if(getTeam() != Team.ALLY)
			return null;
		return userId;
	}
	
	public String getTwitchDisplayName() {
		if(getUserId() == null)
			return null;
		return twitchDisplayName;
	}
	
	public UnitType getUnitType() {
		if(getUserId() == null)
			return null;
		return type;
	}
	
	public boolean isEpic() {
		return isEntity && isEpic;
	}
	
	public boolean isCaptain() {
		return (isOccupied || isEntity) && isCaptain;
	}
	
	public boolean isSelf() {
		return getUserId() != null && isSelf;
	}
	
	public boolean isOther() {
		return getUserId() != null && isOther;
	}
}
