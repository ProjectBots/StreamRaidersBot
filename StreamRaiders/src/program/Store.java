package program;

import java.util.Arrays;
import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Store {

	private static final String[] nLevelCost = StreamRaiders.get("nlevelcost").split("|");
	private static final String[] lLevelCost = StreamRaiders.get("llevelcost").split("|");
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
	
	//TODO upgradeUnit
	public void upgradeUnit() {
		
	}
	
	//TODO buyItem
	public boolean buyItem(JsonObject item, SRR req) {
		if(item.getAsJsonPrimitive("purchased").getAsInt() == 1) return false;
		
		int price = item.getAsJsonPrimitive("price").getAsInt();
		if(!(price <= currency.get("gold"))) return false;
		
		currency.put("gold", currency.get("gold") - price);
		JsonObject text = JsonParser.json(req.purchaseStoreItem(item.getAsJsonPrimitive("itemId").getAsString()));
		if(!text.getAsJsonPrimitive("status").getAsString().equals("success")) return false;
		
		shopItems = text.getAsJsonArray("data");
		return true;
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
	
}
