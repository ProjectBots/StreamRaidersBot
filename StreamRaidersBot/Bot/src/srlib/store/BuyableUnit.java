package srlib.store;

import srlib.units.Unit;
import srlib.units.UnitType;

public class BuyableUnit {

	public final Unit unit;
	public final UnitType type;
	public final int price;
	public final boolean dupe;
	
	public BuyableUnit(Unit u, int price) {
		this.unit = u;
		this.price = price;
		
		this.type = null;
		this.dupe = false;
	}
	
	public BuyableUnit(UnitType type, int price, boolean dupe) {
		this.type = type;
		this.price = price;
		this.dupe = dupe;
		
		this.unit = null;
	}
	
}
