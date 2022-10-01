package srlib.units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import otherlib.Options;
import srlib.Time;
import srlib.skins.Skin;
import srlib.souls.Soul;
import srlib.souls.SoulType;

public class Unit implements Comparable<Unit> {
	
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
		return uTypes.getAsJsonObject(type).get("canFly").getAsBoolean();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append("{").append(type)
				.append(" ").append(level);
		
		if(soulType != null)
			sb.append(" ").append(soulType.toString());
		
		return sb.append("}").toString();
	}
	
	@Override
	public int compareTo(Unit u) {
		int t = this.rarity.rank - u.rarity.rank;
		if(t != 0)
			return t;
		
		t = this.type.compareTo(u.type);
		if(t != 0)
			return t;
		
		return u.level - this.level;
	}
	
	
	public final UnitRarity rarity;
	public final String type, specializationUid, specializationDisName;
	public final Set<String> ptags;
	public final int unitId, level;
	public final boolean dupe;
	
	private final long cool;

	private String skin;
	private SoulType soulType;
	private int soulId;
	
	
	public Unit(JsonObject unit) {
		this.unitId = unit.get("unitId").getAsInt();
		this.type = unit.get("unitType").getAsString();
		this.level = unit.get("level").getAsInt();
		
		rarity = UnitRarity.parseType(type);
		
		this.dupe = false;
		
		JsonElement je = unit.get("cooldownTime");
		this.cool = je.isJsonPrimitive() ? Time.parse(je.getAsString()) + 5 : 0;
		je = unit.get("soulId");
		this.soulId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		je = unit.get("soulType");
		this.soulType = je.isJsonPrimitive() ? SoulType.parseUID(je.getAsString()) : null;
		je = unit.get("skin");
		this.skin = je.isJsonPrimitive() ? je.getAsString() : null;
		
		je = unit.get("specializationUid");
		if(je.isJsonPrimitive()) {
			this.specializationUid = je.getAsString();
			JsonArray spc = uTypes.getAsJsonObject(type).getAsJsonArray("specs");
			String tmp = null;
			for(int i=0; i<spc.size(); i++) {
				JsonObject sp = spc.get(i).getAsJsonObject();
				if(specializationUid.equals(sp.get("uid").getAsString())) {
					tmp = sp.get("name").getAsString();
					break;
				}
			}
			this.specializationDisName = tmp;
		} else {
			this.specializationUid = null;
			this.specializationDisName = null;
		}
		
		
		JsonArray ptagsJArr = uTypes.getAsJsonObject(type).getAsJsonArray("roles");
		HashSet<String> ptags_tmp = new HashSet<>();
		for(int i=0; i<ptagsJArr.size(); i++)
			ptags_tmp.add(ptagsJArr.get(i).getAsString());
		
		this.ptags = Collections.unmodifiableSet(ptags_tmp);
	}
	
	private Unit(String unitType, boolean dupe) {
		this.type = unitType;
		this.rarity = UnitRarity.parseType(unitType);
		this.dupe = dupe;
		
		JsonArray ptagsJArr = uTypes.getAsJsonObject(unitType).getAsJsonArray("roles");
		HashSet<String> ptags_tmp = new HashSet<>();
		for(int i=0; i<ptagsJArr.size(); i++)
			ptags_tmp.add(ptagsJArr.get(i).getAsString());
		
		ptags = Collections.unmodifiableSet(ptags_tmp);

		this.soulType = null;
		this.skin = null;
		this.specializationUid = null;
		this.specializationDisName = null;
		this.level = -1;
		this.unitId = -1;
		this.soulId = -1;
		this.cool = -1;
	}
	
	
	public static Unit getTypeOnly(String unitType, boolean dupe) {
		return new Unit(unitType, dupe);
	}

	
	public boolean isAvailable() {
		return Time.isBeforeServerTime(cool);
	}
	
	public void setSoul(Soul soul) {
		this.soulType = soul != null ? soul.type : null;
		this.soulId = soul != null ? soul.soulId : -1;
	}
	
	public SoulType getSoulType() {
		return soulType;
	}
	
	public int getSoulId() {
		return soulId;
	}
	
	public void setSkin(Skin skin) {
		this.skin = skin==null?null:skin.uid;
	}
	
	public String getSkin() {
		return skin;
	}
	
	public String getDisName() {
		return skin != null 
				//	TODO optimize, should not parse whole skins object for just one skin name
				? Json.parseObj(Options.get("skins")).getAsJsonObject(skin).get("DisplayName").getAsString()
				: (specializationDisName != null
							? specializationDisName
							: uTypes.getAsJsonObject(type).get("name").getAsString());
	}
	
}
