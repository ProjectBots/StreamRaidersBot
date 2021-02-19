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
	
	
	public static final String[] types = new String[] {
			"archer", 		"tank", 		"warrior", 	"flagbearer", 	"rogue",
			"buster", 		"bomber", 		"healer", 	"barbarian", 	"alliespaladin",
			"musketeer", 	"centurion", 	"monk", 	"berserker", 	"flyingarcher",
			"mage", 		"warbeast", 	"templar", 	"orcslayer", 	"alliesballoonbuster"	
	};
	
	public Unit(JsonObject unit) {
		this.unit = unit;
		try {
			setDate(unit.getAsJsonPrimitive("cooldownTime").getAsString());
		} catch (Exception e) {}
		
		String unitType = unit.getAsJsonPrimitive("unitType").getAsString();
		
		int index = Arrays.asList(types).indexOf(unitType);
		
		if(index == -1) {
			System.err.println("Invalid Unit Type: " + unitType);
		}
		
		rank = (int) Math.floor(index / 5) + 1;
		
		/*
		switch(unit.getAsJsonPrimitive("unitType").getAsString()) {
		case "archer":
		case "tank":
		case "warrior":
		case "flagbearer":
		case "rogue":
			rank = 1;
			break;
		case "buster":
		case "bomber":
		case "healer":
		case "barbarian":
		case "alliespaladin":
			rank = 2;
			break;
		case "musketeer":
		case "centurion":
		case "monk":
		case "berserker":
		case "flyingarcher":
			rank = 3;
			break;
		case "mage":
		case "warbeast":
		case "templar":
		case "alliesballoonbuster":
		case "orcslayer":
			rank = 4;
			break;
		default:
			System.err.println("Invalid Unit Type: " + unit.getAsJsonPrimitive("unitType").getAsString());
		}
		*/
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
