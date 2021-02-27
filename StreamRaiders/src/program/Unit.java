package program;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.JsonObject;

public class Unit {

	//private static JsonObject stats = JsonParser.json(NEF.read("data/unitStats.app"));
	
	JsonObject unit = null;
	Date cool = null;
	int rank = 0;
	
	public static final String[] common = new String[] {"archer", "tank", "warrior", "flagbearer", "rogue"};
	public static final String[] uncommon = new String[] {"buster", "bomber", "healer", "barbarian", "alliespaladin"};
	public static final String[] rare = new String[] {"musketeer", "centurion", "monk", "berserker", "flyingarcher"};
	public static final String[] legendary = new String[] {"mage", "warbeast", "templar", "orcslayer", "alliesballoonbuster"};
	
	public static String[][] getTypes() {
		return new String[][] {common, uncommon, rare, legendary};
	}

	
	public Unit(JsonObject unit) {
		this.unit = unit;
		try {
			setDate(unit.getAsJsonPrimitive("cooldownTime").getAsString());
		} catch (Exception e) {}
		
		String unitType = unit.getAsJsonPrimitive("unitType").getAsString();
		
		if(Arrays.asList(common).indexOf(unitType) != -1) {
			rank = 1;
		} else if(Arrays.asList(uncommon).indexOf(unitType) != -1) {
			rank = 2;
		} else if(Arrays.asList(rare).indexOf(unitType) != -1) {
			rank = 3;
		} else if(Arrays.asList(legendary).indexOf(unitType) != -1) {
			rank = 4;
		} else {
			System.err.println("Invalid Unit Type: " + unitType);
		}
	}
	
	public String get(String con) {
		if(con.equals("rank")) return ""+rank;
		
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
