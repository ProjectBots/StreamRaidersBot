package srlib.units;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import include.Json;
import otherlib.Options;
import srlib.Time;
import srlib.skins.Skin;
import srlib.souls.Soul;
import srlib.souls.SoulType;

public class Unit implements Comparable<Unit> {
	
	
	
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
		int t = this.type.compareTo(u.type);
		if(t != 0)
			return t;
		
		return u.level - this.level;
	}
	
	
	public final UnitType type;
	public final String specializationUid, specializationDisName;
	public final int unitId, level;
	public final boolean dupe;
	
	private final long cool;

	private String skin;
	private SoulType soulType;
	private int soulId;
	
	
	public Unit(JsonObject unit) {
		this.unitId = unit.get("unitId").getAsInt();
		this.type = UnitType.types.get(unit.get("unitType").getAsString());
		this.level = unit.get("level").getAsInt();
		
		this.dupe = false;
		
		JsonElement je = unit.get("cooldownTime");
		this.cool = je.isJsonPrimitive() ? Time.parse(je.getAsString()) + 10 : 0;
		je = unit.get("soulId");
		this.soulId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		je = unit.get("soulType");
		this.soulType = je.isJsonPrimitive() ? SoulType.parseUID(je.getAsString()) : null;
		je = unit.get("skin");
		this.skin = je.isJsonPrimitive() ? je.getAsString() : null;
		
		je = unit.get("specializationUid");
		if(je.isJsonPrimitive()) {
			this.specializationUid = je.getAsString();
			String tmp = null;
			for(int i=0; i<3; i++) {
				if(specializationUid.equals(type.getSpecUid(i))) {
					tmp = type.getSpecName(i);
					break;
				}
			}
			this.specializationDisName = tmp;
		} else {
			this.specializationUid = null;
			this.specializationDisName = null;
		}
		
	}
	
	private Unit(UnitType unitType, boolean dupe) {
		this.type = unitType;
		this.dupe = dupe;
		
		this.soulType = null;
		this.skin = null;
		this.specializationUid = null;
		this.specializationDisName = null;
		this.level = -1;
		this.unitId = -1;
		this.soulId = -1;
		this.cool = -1;
	}
	
	
	public static Unit getTypeOnly(UnitType unitType, boolean dupe) {
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
							: type.name);
	}
	
}
