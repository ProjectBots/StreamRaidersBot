package program;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.JsonObject;

public class Unit {

	//private static JsonObject stats = JsonParser.json(NEF.read("data/unitStats.app"));
	
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
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(serverTime).after(cool);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setDate(String date) {
		try {
			this.cool = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


	
	
}
