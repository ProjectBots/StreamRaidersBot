package srlib.souls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Soul {

	
	public final SoulType type;
	public final int soulId;
	public final int unitId;
	public Soul(JsonObject soul) {
		this.soulId = soul.get("soulId").getAsInt();
		JsonElement je = soul.get("unitId");
		this.unitId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		this.type = SoulType.parseUID(soul.get("soulType").getAsString());
	}
	
}
