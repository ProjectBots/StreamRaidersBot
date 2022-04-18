package program;

import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Time;
import program.Skins.Skin;

public class Unit {
	
	private static JsonObject uTypes = Json.parseObj(Options.get("unitTypes"));
	
	public static void setUnitTypes(JsonObject data) {
		uTypes = new JsonObject();
		JsonObject us = data.getAsJsonObject("Units");
		for(String key : us.keySet()) {
			JsonObject u = us.getAsJsonObject(key);
			
			if(!u.get("PlacementType").getAsString().equals("viewer"))
				continue;
			
			String type = u.get("UnitType").getAsString();
			
			if(uTypes.has(type))
				continue;
			
			JsonObject ut = new JsonObject();
			for(String s : "canFly IsFlying  name DisplayName".split("  ")) {
				String[] ss = s.split(" ");
				ut.add(ss[0], u.get(ss[1]));
			}
			
			ut.addProperty("rarity", u.get("Rarity").getAsString().toLowerCase());
			
			JsonArray roles = new JsonArray();
			roles.add(u.get("Role").getAsString().toLowerCase());
			//	i really hate to do this, but there is no other reliable way for it
			//	add specific roles (that are not declared in the data)
			switch(type) {
			case "flagbearer":
				roles.add("supportFlag");
				break;
			case "rogue":
				roles.add("assassinDagger");
				break;
			case "buster":
				roles.add("assassinExplosive");
				break;
			case "healer":
				roles.add("supportHealer");
				break;
			case "flyingarcher":
				roles.add("assassinFlyingDagger");
				break;
			case "alliesballoonbuster":
				roles.add("assassinFlyingExplosive");
				break;
			}
			roles.add("vibe");
			
			ut.add("roles", roles);
			
			ut.add("specs", new JsonArray());
			
			uTypes.add(type, ut);
		}
		
		JsonObject sps = data.getAsJsonObject("Specialization");
		for(String key : sps.keySet()) {										//	 ¯\_(ツ)_/¯
			if(key.startsWith("epic") || key.startsWith("captain") || key.startsWith("cpatain"))
				continue;
			
			JsonObject sp = sps.getAsJsonObject(key);
			
			JsonObject spec = new JsonObject();
			spec.add("name", sp.get("DisplayName"));
			spec.addProperty("uid", key);
			
			uTypes.getAsJsonObject(sp.get("UnitType").getAsString())
				.getAsJsonArray("specs")
				.add(spec);
			
		}
	}
	
	public static JsonObject getTypes() {
		return uTypes.deepCopy();
	}
	
	public static JsonArray getSpecs(String type) {
		return uTypes.getAsJsonObject(type).getAsJsonArray("specs");
	}
	
	public static boolean isLegendary(String type) {
		return uTypes.getAsJsonObject(type).get("rarity").getAsString().equals("legendary");
	}
	
	public static SRC.UnitRarity getRarity(String type) {
		return SRC.UnitRarity.valueOf(uTypes.getAsJsonObject(type).get("rarity").getAsString());
	}
	
	public boolean canFly() {
		return uTypes.getAsJsonObject(get(SRC.Unit.unitType)).get("canFly").getAsBoolean();
	}
	
	
	@Override
	public String toString() {
		return get(SRC.Unit.unitType);
	}
	
	private final JsonObject unit;
	private String cool = null;
	private final HashSet<String> ptags = new HashSet<>();
	
	public final boolean dupe;
	
	public Unit(JsonObject unit) throws ClassCastException {
		this.unit = unit;
		JsonElement jcool = unit.get(SRC.Unit.cooldownTime);
		if(jcool.isJsonPrimitive())
			setCooldown(unit.get(SRC.Unit.cooldownTime).getAsString());
		
		
		JsonObject uType = uTypes.getAsJsonObject(unit.get(SRC.Unit.unitType).getAsString());
		
		dupe = false;
		
		JsonArray ptagsJArr = uType.getAsJsonArray("roles");
		for(int i=0; i<ptagsJArr.size(); i++)
			ptags.add(ptagsJArr.get(i).getAsString());
	}
	
	private Unit(String unitType, boolean dupe) {
		JsonObject unit = new JsonObject();
		unit.addProperty(SRC.Unit.unitType, unitType);
		this.dupe = dupe;
		this.unit = unit;
	}
	
	
	public static Unit createTypeOnly(String unitType, boolean dupe) {
		return new Unit(unitType, dupe);
	}

	public String get(String con) {
		switch(con) {
		case SRC.Unit.specializationDisName:
			String spec = get(SRC.Unit.specializationUid);
			if(spec != null) {
				JsonArray spc = uTypes.getAsJsonObject(get(SRC.Unit.unitType)).getAsJsonArray("specs");	// specs.getAsJsonArray(get(SRC.Unit.unitType));
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
