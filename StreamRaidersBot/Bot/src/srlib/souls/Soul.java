package srlib.souls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import srlib.units.Unit;

public class Soul {
	
	@Override
	public String toString() {
		return new StringBuffer("{")
				.append(type.toString()).append(" ")
				.append(soulId).append("}").toString();
	}

	public final SoulType type;
	public final int soulId;
	
	private int unitId;
	
	public Soul(JsonObject soul) {
		this.soulId = soul.get("soulId").getAsInt();
		JsonElement je = soul.get("unitId");
		this.unitId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		this.type = SoulType.parseUID(soul.get("soulType").getAsString());
	}
	
	public void setUnit(Unit u) {
		this.unitId = u != null ? u.unitId : -1;
	}
	
	public int getUnitId() {
		return unitId;
	}

	
	
}
