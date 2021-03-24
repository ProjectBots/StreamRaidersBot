package program;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.JsonParser;

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
	
	public boolean canUnlockUnit(String type) {
		String[] cost = (Arrays.asList(legendary).indexOf(type) == -1 ? nLevelCost[0] : lLevelCost[0]).split(",");
		
		if(currency.get("gold") < Integer.parseInt(cost[0])) return false;
		if(currency.get(type.replace("allies", "")) < Integer.parseInt(cost[1])) return false;
		
		return true;
	}
	
	
	public Unit[] getUnlockableUnits(Unit[] units) {
		
		String[][] allTypes = Unit.getTypes();
		
		String[] gotTypes = new String[0];
		
		for(int i=0; i<units.length; i++) {
			gotTypes = add(gotTypes, units[i].get(SRC.Unit.unitType));
		}
		
		List<String> lGotTypes = Arrays.asList(gotTypes);
		
		Unit[] ret = new Unit[0];
		
		for(int i=0; i<allTypes.length; i++) {
			for(int j=0; j<allTypes[i].length; j++) {
				String type = allTypes[i][j];
				if(lGotTypes.indexOf(type) == -1) {
					int scrolls = Integer.parseInt((Arrays.asList(legendary).indexOf(type) == -1 
							? nLevelCost
							: lLevelCost)[0].split(",")[1]);
					
					Integer gotScrolls = currency.get(type.replace("allies", ""));
					if(gotScrolls == null) continue;
					if(scrolls <= gotScrolls.intValue()) {
						ret = add(ret, Unit.createTypeOnly(type));
					}
				}
			}
		}
		return ret;
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
			if(specUID.equals("null")) return "no specUID";
			ret = JsonParser.json(req.specializeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId), specUID));
		} else {
			ret = JsonParser.json(req.upgradeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId)));
		}
		
		JsonElement err = ret.get("errorMessage");
		if(err.isJsonNull()) {
			currency.put("gold", currency.get("gold") - cost);
			return null;
		} else {
			return err.getAsString();
		}
	}
	
	
	public String unlockUnit(String type, SRR req) {
		
		int price = 0;
		if(!canUnlockUnit(type)) return "not enough gold";

		String text = req.unlockUnit(type);
		if(text == null) return "critical request error";
		
		JsonObject res = JsonParser.json(text);
		
		JsonElement gc = res.getAsJsonObject("data").get("goldCharged");
		if(!gc.isJsonPrimitive()) return res.getAsJsonPrimitive("errorMessage").getAsString();
		
		price = gc.getAsInt();
		
		currency.put("gold", currency.get("gold") - price);
		return null;
		
		
	}
	
	public Unit[] getUpgradeableUnits(Unit[] units) {
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			String slvl = units[i].get(SRC.Unit.level);
			if(slvl == null) continue;
			
			int lvl = Integer.parseInt(slvl);
			if(lvl == 30) continue;
			
			String type = units[i].get(SRC.Unit.unitType);
			if(type == null) continue;
			int cost = Integer.parseInt((Arrays.asList(legendary).indexOf(type) == -1 
					? nLevelCost[lvl] 
					: lLevelCost[lvl])
				.split(",")[1]);
			
			int got = currency.get(type.replace("allies", ""));
			
			if(cost <= got) ret = add(ret, units[i]);
		}
		return ret;
	}
	
	
	public String buyItem(JsonObject item, SRR req) {
		if(item.getAsJsonPrimitive("purchased").getAsInt() == 1) return "already purchased";
		
		int price = item.getAsJsonPrimitive("price").getAsInt();
		int gold = 0;
		try {
			gold = currency.get("gold");
		} catch (NullPointerException e) {}
		if(price > gold) return "not enough gold";
		
		currency.put("gold", gold - price);
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
	
	private String[] add(String[] arr, String item) {
		String[] arr2 = new String[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
}
