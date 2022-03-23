package program;

import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Time;
import program.Skins.Skin;

public class Unit {
	
	@Override
	public String toString() {
		return get(SRC.Unit.unitType);
	}
	
	private final JsonObject unit;
	private String cool = null;
	private final HashSet<String> ptags = new HashSet<>();
	
	public final boolean dupe;
	public final int rank;
	
	
	private static final JsonObject uTypes = Json.parseObj(Options.get("unitTypes"));
	private static final JsonObject specs = Json.parseObj(Options.get("specUIDs"));
	
	public static JsonObject getTypes() {
		JsonObject ret = uTypes.deepCopy();
		ret.remove("allTypes");
		return ret;
	}
	
	public boolean canFly() {
		return uTypes.getAsJsonObject(get(SRC.Unit.unitType)).get("canFly").getAsBoolean();
	}
	
	
	public static boolean isLegendary(String type) {
		return uTypes.getAsJsonObject(type).get("rank").getAsInt() == 4;
	}
	
	public Unit(JsonObject unit) throws ClassCastException {
		this.unit = unit;
		JsonElement jcool = unit.get(SRC.Unit.cooldownTime);
		if(jcool.isJsonPrimitive())
			setCooldown(unit.get(SRC.Unit.cooldownTime).getAsString());
		
		
		JsonObject uType = uTypes.getAsJsonObject(unit.get(SRC.Unit.unitType).getAsString());
		
		rank = uType.get("rank").getAsInt();
		dupe = false;
		
		JsonArray ptagsJArr = uType.getAsJsonArray("role");
		for(int i=0; i<ptagsJArr.size(); i++)
			ptags.add(ptagsJArr.get(i).getAsString());
	}
	
	private Unit(String unitType, boolean dupe) {
		JsonObject unit = new JsonObject();
		unit.addProperty(SRC.Unit.unitType, unitType);
		this.dupe = dupe;
		this.unit = unit;
		this.rank = 0;
	}
	
	
	public static Unit createTypeOnly(String unitType, boolean dupe) {
		return new Unit(unitType, dupe);
	}

	public String get(String con) {
		switch(con) {
		case SRC.Unit.rank:
			return ""+rank;
		case SRC.Unit.specializationDisName:
			String spec = get(SRC.Unit.specializationUid);
			if(spec != null) {
				JsonArray spc = specs.getAsJsonArray(get(SRC.Unit.unitType));
				for(int i=0; i<spc.size(); i++) {
					JsonObject sp = spc.get(i).getAsJsonObject();
					if(spec.equals(sp.get("uid").getAsString()))
						return sp.get("name").getAsString();
				}
			}
			return null;
		case SRC.Unit.disName:
			String skin = get(SRC.Unit.skin);
			if(skin != null)
				return Json.parseObj(Options.get("skins")).getAsJsonObject(skin).get("DisplayName").getAsString();
			
			String specname = get(SRC.Unit.specializationDisName);
			return specname == null
					? uTypes.getAsJsonObject(get(SRC.Unit.unitType))
						.get("name").getAsString()
					: specname;
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
		return ptags.contains(tag);
	}
	
	public HashSet<String> getPlanTypes() {
		return new HashSet<>(ptags);
	}
	
	private void setCooldown(String date) {
		cool = date;
	}
	
	public void setSkin(Skin skin) {
		unit.addProperty(SRC.Unit.disName, skin==null?uTypes.getAsJsonObject(get(SRC.Unit.unitType)).get("name").getAsString():skin.disname);
		unit.addProperty(SRC.Unit.skin, skin==null?null:skin.uid);
	}


	
	
}
