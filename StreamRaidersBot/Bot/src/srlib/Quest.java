package srlib;

import srlib.units.UnitType;

public class Quest {
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Quest))
			return false;
		
		return questId.equals(((Quest) obj).questId);
	}
	
	public final String questId;
	
	public final int progress;
	public final int goal;
	public final boolean canClaim;
	
	public final String qslot;
	public final String qtype;
	public final UnitType neededUnit;
	
	public Quest(String questId, int progress, int goal, String qslot, String qtype, UnitType neededUnit) {
		this.questId = questId;
		this.progress = progress;
		this.goal = goal;
		this.canClaim = progress >= goal;
		this.qslot = qslot;
		this.qtype = qtype;
		this.neededUnit = neededUnit;
	}
}