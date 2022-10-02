package srlib.units;

import java.util.HashMap;

public enum UnitRarity {
	COMMON(0), UNCOMMON(1), RARE(2), LEGENDARY(3);

	public final int rank;
	
	private UnitRarity(int rank) {
		this.rank = rank;
	}

	private final static HashMap<String, UnitRarity> from_string;
	static {
		from_string = new HashMap<>();
		for (UnitRarity value : values())
			from_string.put(value.toString(), value);

	}

	public static UnitRarity parseString(String arg) {
		return from_string.get(arg);
	}

	
}