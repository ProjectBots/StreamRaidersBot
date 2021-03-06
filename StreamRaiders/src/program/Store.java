package program;

import java.util.Arrays;
import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Store {

	private static final String[] nLevelCost = StreamRaiders.get("nlevelcost").split("\\|");
	private static final String[] lLevelCost = StreamRaiders.get("llevelcost").split("\\|");
	private static final String[] legendary = StreamRaiders.get("legendary").split(",");
	
	private JsonArray shopItems = new JsonArray();
	private Hashtable<String, Integer> currency = new Hashtable<>();
	
	public Store(SRR req) {
		refreshCurrency(req);
		refreshStoreItems(req);
	}
	
	public void refreshCurrency(SRR req) {
		currency = new Hashtable<>();
		JsonArray cs = JsonParser.json(req.getAvailableCurrencies()).getAsJsonArray("data");
		for(int i=0; i<cs.size(); i++) {
			JsonObject c = cs.get(i).getAsJsonObject();
			currency.put(c.getAsJsonPrimitive("currencyId").getAsString(), Integer.parseInt(c.getAsJsonPrimitive("quantity").getAsString()));
		}
	}
	
	public void refreshStoreItems(SRR req) {
		shopItems = JsonParser.json(req.getCurrentStoreItems()).getAsJsonArray("data");
	}
	
	public boolean canUpgradeUnit(Unit unit) {
		int lvl = Integer.parseInt(unit.get(SRC.Unit.level));
		
		String type = unit.get(SRC.Unit.unitType);
		
		String[] cost = (Arrays.asList(legendary).indexOf(type) == -1 ? nLevelCost[lvl] : lLevelCost[lvl]).split(",");
		
		if(currency.get("gold") < Integer.parseInt(cost[0])) return false;
		if(currency.get(type.replace("allies", "")) < Integer.parseInt(cost[1])) return false;
		
		return true;
	}
	
	
	public String upgradeUnit(Unit unit, SRR req, String specUID) {
		String lvl = unit.get(SRC.Unit.level);
		JsonObject ret;
		int cost = Integer.parseInt(
				(Arrays.asList(legendary).indexOf(unit.get(SRC.Unit.unitType)) == -1 
					? nLevelCost[Integer.parseInt(lvl)] 
					: lLevelCost[Integer.parseInt(lvl)]
				).split(",")[0]);
		
		if(lvl.equals("19") || lvl.equals("29")) {
			if(specUID == null) return "no specUID";
			ret = JsonParser.json(req.specializeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId), specUID));
		} else {
			ret = JsonParser.json(req.upgradeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId)));
		}
		try {
			return ret.getAsJsonPrimitive("errorMessage").getAsString();
		} catch (Exception e) {
			currency.put("gold", currency.get("gold") - cost);
			return null;
		}
	}
	
	
	public Unit[] getUpgradeableUnits(Unit[] units) {
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			int lvl = Integer.parseInt(units[i].get(SRC.Unit.level));
			try {
				String type = units[i].get(SRC.Unit.unitType);
				int cost;
				if(Arrays.asList(legendary).indexOf(type) == -1) {
					cost = Integer.parseInt(nLevelCost[lvl].split(",")[1]);
				} else {
					cost = Integer.parseInt(lLevelCost[lvl].split(",")[1]);
				}
				
				int got = currency.get(type.replace("allies", ""));
				
				if(cost <= got) {
					ret = add(ret, units[i]);
				}
			} catch (Exception e) {}
		}
		return ret;
	}
	
	
	public String buyItem(JsonObject item, SRR req) {
		if(item.getAsJsonPrimitive("purchased").getAsInt() == 1) return "already purchased";
		
		int price = item.getAsJsonPrimitive("price").getAsInt();
		if(!(price <= currency.get("gold"))) return "not enough gold";
		
		currency.put("gold", currency.get("gold") - price);
		JsonObject text = JsonParser.json(req.purchaseStoreItem(item.getAsJsonPrimitive("itemId").getAsString()));
		if(!text.getAsJsonPrimitive("status").getAsString().equals("success")) return text.getAsJsonPrimitive("errorMessage").getAsString();
		
		shopItems = text.getAsJsonArray("data");
		return null;
	}
	
	public JsonArray getStoreItems(int con) {
		switch(con) {
		case SRC.Store.notPurchased:
			JsonArray ret = new JsonArray();
			for(int i=0; i<shopItems.size(); i++) {
				JsonObject item = shopItems.get(i).getAsJsonObject();
				if(item.getAsJsonPrimitive("purchased").getAsString().equals("0")) {
					ret.add(item);
				}
			}
			return ret;
		default:
			return shopItems;
		}
	}
	
	private Unit[] add(Unit[] arr, Unit item) {
		Unit[] arr2 = new Unit[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
}
