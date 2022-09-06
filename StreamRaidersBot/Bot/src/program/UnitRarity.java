package program;

import java.util.HashMap;

import com.google.gson.JsonObject;

public enum UnitRarity {
	COMMON(0), UNCOMMON(1), RARE(2), LEGENDARY(3);
	
	
	public final int rank;
	private UnitRarity(int rank) {
		this.rank = rank;
	}
	
	
	private final static HashMap<String, UnitRarity> from_string;
	static {
	    from_string = new HashMap<>();
	    for (UnitRarity values : values())
	        from_string.put(values.toString(), values);
	    
	}
	private final static HashMap<String, UnitRarity> from_type;
	static {
		from_type = new HashMap<>();
		JsonObject types = Unit.getTypes();
		for(String type : types.keySet())
			from_type.put(type, parseString(types.getAsJsonObject(type).get("rarity").getAsString()));
	}
	
	
	
	//	faster than .valueOf
	public static UnitRarity parseString(String arg) {
		return from_string.get(arg);
	}
	
	public static UnitRarity parseType(String type) {
		return from_type.get(type);
	}
}