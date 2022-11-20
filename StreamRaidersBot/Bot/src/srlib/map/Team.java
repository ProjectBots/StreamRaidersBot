package srlib.map;

import java.util.HashMap;

public enum Team {
	//	Player is not an actual team, but not all allies are players and i want to distinguish them
	PLAYER(""), ALLY("Ally"), NEUTRAL("Neutral"), ENEMY("Enemy");
	
	public final String name;
	private Team(String name) {
		this.name = name;
	}
	
	private final static HashMap<String, Team> from_string;
	static {
		from_string = new HashMap<>();
		for(Team value : values()) {
			from_string.put(value.toString(), value);
			from_string.put(value.name, value);
		}

	}

	public static Team parseString(String arg) {
		return from_string.get(arg);
	}
}
