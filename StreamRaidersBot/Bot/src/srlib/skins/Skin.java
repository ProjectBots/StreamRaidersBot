package srlib.skins;

import com.google.gson.JsonObject;

public class Skin {
	public final String uid;
	public final String unit;
	public final String disname;
	public final SkinType type;
	public final String captainId;
	public Skin(JsonObject pack) {
		uid = pack.get("Uid").getAsString();
		unit = pack.get("BaseUnitType").getAsString();
		disname = pack.get("DisplayName").getAsString();
		type = SkinType.parseString(pack.get("Type").getAsString());
		captainId = pack.get("StreamerId").getAsString();
	}
	@Override
	public String toString() {
		return "{uid="+uid+", captainId="+captainId+", disname="+disname+", unit="+unit+", type="+type+"}";
	}
}