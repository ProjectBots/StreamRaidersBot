package srlib.units;

import java.util.ArrayList;
import java.util.Hashtable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import otherlib.Options;

public class UnitType implements Comparable<UnitType> {
	
	@Override
	public int compareTo(UnitType ut) {
		int t = this.rarity.rank - ut.rarity.rank;
		if(t != 0)
			return t;
		
		t = this.name.compareTo(ut.name);
		if(t != 0)
			return t;
		
		
		return this.uid.compareTo(ut.uid);
	}
	
	/**
	 * do not modify outside of UnitType
	 */
	public static Hashtable<String, UnitType> types;
	
	/**
	 * do not modify outside of UnitType
	 */
	public static ArrayList<String> typeUids;
	
	public static void ini() {
		JsonObject jo = Json.parseObj(Options.get("unitTypes"));
		UnitType.types = new Hashtable<>();
		
		for(String uid : jo.keySet()) {
			JsonObject u = jo.getAsJsonObject(uid);
			types.put(uid, new UnitType(uid,
					u.get("name").getAsString(),
					u.get("canFly").getAsBoolean(),
					UnitRole.valueOf(u.get("role").getAsString()),
					UnitRarity.parseString(u.get("rarity").getAsString())));
			
		}
		
		UnitType.typeUids = new ArrayList<>(types.keySet());
	}
	
	public static JsonElement genUnitTypesFromData(JsonObject data) {
		JsonObject us = data.getAsJsonObject("Units");
		for(String key : us.keySet()) {
			JsonObject u = us.getAsJsonObject(key);
			
			if(!u.get("PlacementType").getAsString().equals("viewer"))
				continue;
			
			String type = u.get("UnitType").getAsString();
			
			if(types.containsKey(type))
				continue;
			
			types.put(type, new UnitType(type,
						u.get("DisplayName").getAsString(),
						u.get("IsFlying").getAsBoolean(),
						UnitRole.parseUID(u.get("Role").getAsString().toLowerCase()),
						UnitRarity.parseString(u.get("Rarity").getAsString().toUpperCase())));
			
			typeUids.add(type);
		}
		
		
		
		JsonObject sps = data.getAsJsonObject("Specialization");
		for(String uid : sps.keySet()) {									//	 ¯\_(ツ)_/¯ typo by sr
			if(uid.startsWith("epic") || uid.startsWith("captain") || uid.startsWith("cpatain"))
				continue;
			
			JsonObject sp = sps.getAsJsonObject(uid);
			
			types.get(sp.get("UnitType").getAsString())
					.addSpec(uid, sp.get("DisplayName").getAsString());
			
		}
		
		return Json.fromObj(types);
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
		return types.containsKey(ret) ? ret : null;
	}
	
	
	public final String uid, name, ptag;
	public final UnitRarity rarity;
	public final boolean canFly;
	public final UnitRole role;
	
	private final String[] specUids = new String[3];
	private final String[] specNames = new String[3];
	
	public UnitType(String uid, String name, boolean canFly, UnitRole role, UnitRarity rarity) {
		this.uid = uid;
		this.name = name;
		this.rarity = rarity;
		this.canFly = canFly;
		this.role = role;
		this.ptag = name.toLowerCase().replace(" ", "");
	}
	
	public boolean hasPTag(String ptag) {
		return role.uid.equals(ptag) || uid.equals(ptag) || "vibe".equals(ptag);
	}
	
	public String getSpecUid(int p) {
		return specUids[p];
	}
	
	public String getSpecName(int p) {
		return specNames[p];
	}
	
	private void addSpec(String uid, String name) {
		for(int i=0; i<3; i++) {
			if(specUids[i] == null) {
				specUids[i] = uid;
				specNames[i] = name;
				return;
			}
		}
	}
}
