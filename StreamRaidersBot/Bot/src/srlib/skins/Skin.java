package srlib.skins;

import com.google.gson.JsonObject;

import srlib.units.UnitType;

public class Skin {
	@Override
	public String toString() {
		return "{uid="+uid+", captainId="+captainId+", disname="+disname+", unit="+unitType.uid+", type="+type+"}";
	}
	public final String uid;
	public final UnitType unitType;
	public final String disname;
	public final SkinType type;
	public final String captainId;
	Skin(JsonObject pack) {
		uid = pack.get("Uid").getAsString();
		unitType = UnitType.getType(pack.get("BaseUnitType").getAsString());
		disname = pack.get("DisplayName").getAsString();
		type = SkinType.parseString(pack.get("Type").getAsString());
		captainId = pack.get("StreamerId").getAsString();
	}
}