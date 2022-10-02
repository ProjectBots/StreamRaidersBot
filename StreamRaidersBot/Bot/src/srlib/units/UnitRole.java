package srlib.units;

import java.util.HashMap;

public enum UnitRole {
	ASSASSIN("assassin"),
	ARMORED("armored"),
	RANGED("ranged"),
	MELEE("melee"),
	SUPPORT("support")
	;
	
	public final String uid;
	private UnitRole(String role) {
		this.uid = role;
	}
	
	private final static HashMap<String, UnitRole> from_role;
	static {
		from_role = new HashMap<>();
		for (UnitRole value : values())
			from_role.put(value.uid, value);

	}

	public static UnitRole parseUID(String uid) {
		return from_role.get(uid);
	}
}
