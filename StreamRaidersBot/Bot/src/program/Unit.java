package program;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Json;
import include.Time;

public class Unit {
	
	@Override
	public String toString() {
		return get(SRC.Unit.unitType);
	}
	
	private JsonObject unit = null;
	private String cool = null;
	private int rank = 0;
	private JsonArray ptags = null;
	
	
	public static final JsonObject uTypes = Json.parseObj(Options.get("unitTypes"));
	public static final JsonObject specs = Json.parseObj(Options.get("specUIDs"));
	
	public static JsonArray getAllPlanTypes() {
		return uTypes.getAsJsonArray("allTypes").deepCopy();
	}
	
	public static JsonObject getTypes() {
		JsonObject ret = uTypes.deepCopy();
		ret.remove("allTypes");
		return ret;
	}
	
	public boolean canFly() {
		return uTypes.getAsJsonObject(this.get(SRC.Unit.unitType)).getAsJsonPrimitive("canFly").getAsBoolean();
	}
	
	
	public static boolean isLegendary(String type) {
		return uTypes.getAsJsonObject(type).getAsJsonPrimitive("rank").getAsInt() == 4;
	}
	
	public Unit(JsonObject unit) throws ClassCastException {
		this.unit = unit;
		JsonElement jcool = unit.get("cooldownTime");
		if(jcool.isJsonPrimitive()) {
			setCooldown(unit.getAsJsonPrimitive("cooldownTime").getAsString());
		}
		
		JsonObject uType = uTypes.getAsJsonObject(unit.getAsJsonPrimitive(SRC.Unit.unitType).getAsString());
		
		rank = uType.getAsJsonPrimitive("rank").getAsInt();
		ptags = uType.getAsJsonArray("role");
	}
	
	private Unit(String unitType, boolean dupe) {
		JsonObject unit = new JsonObject();
		unit.addProperty(SRC.Unit.unitType, unitType);
		this.dupe = dupe;
		this.unit = unit;
	}
	
	private boolean dupe = false;
	
	public boolean isDupe() {
		return dupe;
	}
	
	public static Unit createTypeOnly(String unitType, boolean dupe) {
		return new Unit(unitType, dupe);
	}

	public String get(String con) {
		switch(con) {
		case SRC.Unit.rank:
			return ""+rank;
		case SRC.Unit.disName:
			String spec = get(SRC.Unit.specializationUid);
			if(spec == null) 
				return uTypes.getAsJsonObject(get(SRC.Unit.unitType))
						.get("name").getAsString();
			
			JsonArray spc = specs.getAsJsonArray(get(SRC.Unit.unitType));
			for(int i=0; i<spc.size(); i++) {
				JsonObject sp = spc.get(i).getAsJsonObject();
				if(spec.equals(sp.get("uid").getAsString()))
					return sp.get("name").getAsString();
			}
			return null;
		default:
			JsonElement el = unit.get(con);
			if(el == null || !el.isJsonPrimitive()) 
				return null;
			return el.getAsString();
		}
	}
	
	public boolean isAvailable(String serverTime) {
		if(cool == null) 
			return true;
		return Time.isAfter(serverTime, cool);
	}
	
	public boolean hasPlanType(String tag) {
		return ptags.contains(new JsonPrimitive(tag));
	}
	
	public JsonArray getPlanTypes() {
		return ptags;
	}
	
	public void setCooldown(String date) {
		cool = date;
	}


	
	
}
