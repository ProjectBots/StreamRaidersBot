package srlib.souls;

import java.util.HashMap;

import srlib.units.UnitRarity;

public enum SoulType {

	ArcherSoul(UnitRarity.COMMON, "Archer Soul", "archer_soul", "archer"),
	FlagBearerSoul(UnitRarity.COMMON, "Flag Bearer Soul", "flagbearer_soul", "flagbearer"),
	RogueSoul(UnitRarity.COMMON, "Rogue Soul", "rogue_soul", "rogue"),
	TankSoul(UnitRarity.COMMON, "Tank Soul", "tank_soul", "tank"),
	WarriorSoul(UnitRarity.COMMON, "Warrior Soul", "warrior_soul", "warrior")

	;

	public final UnitRarity quality;
	public final String title, uid, unitCurrencyType;

	private SoulType(UnitRarity quality, String title, String uid, String unitCurrencyType) {
		this.quality = quality;
		this.title = title;
		this.uid = uid;
		this.unitCurrencyType = unitCurrencyType;
	}

	private final static HashMap<String, SoulType> from_uid;
	static {
		from_uid = new HashMap<>();
		for (SoulType value : values())
			from_uid.put(value.toString(), value);

	}

	public static SoulType parseUID(String uid) {
		return from_uid.get(uid);
	}

}
