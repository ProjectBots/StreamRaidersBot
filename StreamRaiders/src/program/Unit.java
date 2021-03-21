package program;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Unit {

	private JsonObject unit = null;
	private Date cool = null;
	private int rank = 0;
	
	public static final String[] common = StreamRaiders.get("common").split(",");
	public static final String[] uncommon = StreamRaiders.get("uncommon").split(",");
	public static final String[] rare = StreamRaiders.get("rare").split(",");
	public static final String[] legendary = StreamRaiders.get("legendary").split(",");
	public static final String[] flying = StreamRaiders.get("flying").split(",");
	
	public static String[][] getTypes() {
		return new String[][] {common, uncommon, rare, legendary};
	}

	public boolean canFly() {
		return Arrays.asList(flying).indexOf(unit.getAsJsonPrimitive(SRC.Unit.unitType).getAsString()) != -1;
	}
	
	public Unit(JsonObject unit) throws ClassCastException {
		this.unit = unit;
		JsonElement jcool = unit.get("cooldownTime");
		if(jcool.isJsonPrimitive()) {
			setDate(unit.getAsJsonPrimitive("cooldownTime").getAsString());
		}
		String unitType = unit.getAsJsonPrimitive(SRC.Unit.unitType).getAsString();
		
		String[][] types = getTypes();
		for(int i=0; i<types.length; i++) {
			if(Arrays.asList(types[i]).indexOf(unitType) != -1) {
				rank = i+1;
				return;
			}
		}
		System.err.println("Invalid Unit Type: " + unitType);
	}
	
	private Unit(String unitType) {
		JsonObject unit = new JsonObject();
		unit.addProperty(SRC.Unit.unitType, unitType);
		this.unit = unit;
	}
	
	public static Unit createTypeOnly(String unitType) {
		return new Unit(unitType);
	}

	public String get(String con) {
		if(con.equals(SRC.Unit.rank)) return ""+rank;
		
		JsonElement el = unit.get(con);
		if(el == null) return null;
		if(!el.isJsonPrimitive()) return null;
		return el.getAsString();
	}
	
	public boolean isAvailable(String serverTime) {
		if(cool == null) return true;
		try {
			return SRC.dateParse(serverTime).after(cool);
		} catch (ParseException e) {
			StreamRaiders.log("Unit->isAvailable:" + serverTime, e);
		}
		return false;
	}
	
	public void setDate(String date) {
		try {
			this.cool = SRC.dateParse(date);
		} catch (ParseException e) {
			StreamRaiders.log("Unit->setDate:" + date, e);
		}
	}


	
	
}
