package program;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import include.Time;
import program.Skins.Skin;

public class Unit {
	
	private static JsonObject uTypes = Json.parseObj(Options.get("unitTypes"));
	
	public static JsonObject genUnitTypesFromData(JsonObject data) {
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
			
			ut.addProperty("rarity", u.get("Rarity").getAsString().toUpperCase());
			
			JsonArray roles = new JsonArray();
			roles.add(u.get("Role").getAsString().toLowerCase());
			roles.add(u.get("DisplayName").getAsString().toLowerCase().replace(" ", ""));
			roles.add("vibe");
			
			ut.add("roles", roles);
			
			ut.add("specs", new JsonArray());
			
			uTypes.add(type, ut);
		}
		
		JsonObject sps = data.getAsJsonObject("Specialization");
		for(String key : sps.keySet()) {										//	 ¯\_(ツ)_/¯ typo by sr
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
		
		return uTypes;
	}
	
	public static JsonObject genUnitPowerFromData(JsonObject data) {
		JsonObject units_raw = data.getAsJsonObject("Units");
		JsonObject units = new JsonObject();
		for(String key : units_raw.keySet()) {
			JsonObject u = units_raw.getAsJsonObject(key);
			if(u.get("CanBePlaced").getAsBoolean())
				units.addProperty(key, u.get("Power").getAsInt());
		}
		return units;
	}
	
	public static JsonObject getTypes() {
		return uTypes.deepCopy();
	}
	
	public static ArrayList<String> getTypesList() {
		return new ArrayList<>(uTypes.keySet());
	}
	
	/**
	 * parses a character type (ex: epicbomber30) into its type (ex: bomber)
	 * @param ct
	 * @return the unit type
	 */
	public static String getUnitTypeFromCharacterType(String ct) {
		String ret = ct.replaceAll("\\d+$|^captain|^epic", "");
		switch (ret) {
		case "paladin":
			ret = "alliespaladin";
			break;
		case "balloonbuster":
			ret = "alliesballoonbuster";
			break;
		}
		return uTypes.has(ret) ? ret : null;
	}
	
	public static JsonArray getSpecs(String type) {
		return uTypes.getAsJsonObject(type).getAsJsonArray("specs");
	}
	
	public static boolean isLegendary(String type) {
		return uTypes.getAsJsonObject(type).get("rarity").getAsString().equals(UnitRarity.LEGENDARY.toString());
	}
	
	public static String getName(String type) {
		return uTypes.getAsJsonObject(type).get("name").getAsString();
	}
	
	
	public boolean canFly() {
		return uTypes.getAsJsonObject(unitType).get("canFly").getAsBoolean();
	}
	
	
	@Override
	public String toString() {
		return new StringBuilder().append("{").append(unitType)
						.append(" ").append(get(SRC.Unit.level))
						.append("}").toString();
	}
	
	private final JsonObject unit;
	public final String unitId, unitType;
	private String cool = null;
	private final HashSet<String> ptags = new HashSet<>();
	
	public final boolean dupe;
	
	public Unit(JsonObject unit, String cid) throws ClassCastException {
		this.unit = unit;
		JsonElement jcool = unit.get(SRC.Unit.cooldownTime);
		if(jcool.isJsonPrimitive())
			setCooldown(unit.get(SRC.Unit.cooldownTime).getAsString());
		
		unitType = unit.remove("unitType").getAsString();
		unitId = unit.get("unitId").getAsString();
		
		JsonObject uType = uTypes.getAsJsonObject(unitType);
		
		dupe = false;
		
		JsonArray ptagsJArr = uType.getAsJsonArray("roles");
		for(int i=0; i<ptagsJArr.size(); i++)
			ptags.add(ptagsJArr.get(i).getAsString());
	}
	
	private Unit(String unitType, boolean dupe) {
		this.dupe = dupe;
		unit = null;
		unitId = null;
		this.unitType = unitType;
	}
	
	
	public static Unit createTypeOnly(String unitType, boolean dupe) {
		return new Unit(unitType, dupe);
	}

	public String get(String con) {
		switch(con) {
		case SRC.Unit.specializationDisName:
			String spec = get(SRC.Unit.specializationUid);
			if(spec != null) {
				JsonArray spc = uTypes.getAsJsonObject(unitType).getAsJsonArray("specs");
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
					? uTypes.getAsJsonObject(unitType)
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
		unit.addProperty(SRC.Unit.skin, skin==null?null:skin.uid);
	}

	
}
