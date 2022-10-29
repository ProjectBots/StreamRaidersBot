package srlib.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import otherlib.Options;

public class UnitType implements Comparable<UnitType> {
	
	@Override
	public String toString() {
		return uid;
	}
	
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(!(obj instanceof UnitType))
			return false;
		
		return uid.equals(((UnitType) obj).uid);
	}
	
	private static Hashtable<String, UnitType> types;
	private static List<String> typeUids;
	
	
	public static String typeStringRemap(String in) {
		switch(in) {
		case "paladin": return "alliespaladin";
		case "balloonbuster": return "alliesballoonbuster";
		case "flyingrogue": return "flyingarcher";
		default: return in;
		}
	}
	
	public static UnitType getType(String type) {
		type = typeStringRemap(type);
		return types.get(type);
	}
	
	public static Collection<UnitType> getTypes() {
		return types.values();
	}
	
	/**
	 * @return a unmodifiable list<br>
	 * (see {@link Collections#unmodifiableList(List)})
	 */
	public static List<String> getTypeUids() {
		return typeUids;
	}
	
	public static void ini() {
		JsonObject jo = Json.parseObj(Options.get("unitTypes"));
		UnitType.types = new Hashtable<>();
		
		for(String uid : jo.keySet()) {
			JsonObject u = jo.getAsJsonObject(uid);
			UnitType ut = new UnitType(uid,
					u.get("name").getAsString(),
					u.get("canFly").getAsBoolean(),
					UnitRole.valueOf(u.get("role").getAsString()),
					UnitRarity.parseString(u.get("rarity").getAsString()));
			
			JsonArray specUids = u.getAsJsonArray("specUids");
			JsonArray specNames = u.getAsJsonArray("specNames");
			for(int i=0; i<3; i++)
				ut.addSpec(specUids.get(i).getAsString(), specNames.get(i).getAsString());
			
			types.put(uid, ut);
		}
		
		UnitType.typeUids =	Collections.unmodifiableList(new ArrayList<>(types.keySet()));
	}
	
	public static JsonElement genUnitTypesFromData(JsonObject data) {
		JsonObject us = data.getAsJsonObject("Units");
		boolean addedType = false;
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
			
			addedType = true;
		}
		
		if(addedType)
			UnitType.typeUids =	Collections.unmodifiableList(new ArrayList<>(types.keySet()));
		
		
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
