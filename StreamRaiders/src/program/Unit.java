package program;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.JsonObject;

public class Unit {

	private JsonObject unit = null;
	private Date cool = null;
	private int rank = 0;
	
	public static final String[] common = StreamRaiders.get("common").split(",");
	public static final String[] uncommon = StreamRaiders.get("uncommon").split(",");
	public static final String[] rare = StreamRaiders.get("rare").split(",");
	public static final String[] legendary = StreamRaiders.get("legendary").split(",");
	
	public static String[][] getTypes() {
		return new String[][] {common, uncommon, rare, legendary};
	}

	public Unit(JsonObject unit) {
		this.unit = unit;
		try {
			setDate(unit.getAsJsonPrimitive("cooldownTime").getAsString());
		} catch (Exception e) {}
		
		String unitType = unit.getAsJsonPrimitive("unitType").getAsString();
		
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
		
		try {
			return unit.getAsJsonPrimitive(con).getAsString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean isAvailable(String serverTime) {
		if(cool == null) return true;
		try {
			return SRC.date.parse(serverTime).after(cool);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setDate(String date) {
		try {
			this.cool = SRC.date.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


	
	
}
