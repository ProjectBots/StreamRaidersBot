package program;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.JsonParser;
import include.Time;
import program.SRR.NoInternetException;

public class Store {

	private static final String[] nLevelCost = StreamRaiders.get("nlevelcost").split("\\|");
	private static final String[] lLevelCost = StreamRaiders.get("llevelcost").split("\\|");
	
	private JsonArray shopItems = new JsonArray();
	private Hashtable<String, Integer> currency = new Hashtable<>();
	
	public Store(SRR req) throws URISyntaxException, IOException, NoInternetException {
		refreshCurrency(req);
		refreshStoreItems(req);
	}
	
	public void refreshCurrency(SRR req) throws URISyntaxException, IOException, NoInternetException {
		currency = new Hashtable<>();
		JsonArray cs = JsonParser.parseObj(req.getAvailableCurrencies()).getAsJsonArray("data");
		for(int i=0; i<cs.size(); i++) {
			JsonObject c = cs.get(i).getAsJsonObject();
			currency.put(c.getAsJsonPrimitive("currencyId").getAsString(), Integer.parseInt(c.getAsJsonPrimitive("quantity").getAsString()));
		}
	}
	
	public void refreshStoreItems(SRR req) throws URISyntaxException, IOException, NoInternetException {
		shopItems = JsonParser.parseObj(req.getCurrentStoreItems()).getAsJsonArray("data");
	}
	
	public boolean canUpgradeUnit(Unit unit) {
		int lvl = Integer.parseInt(unit.get(SRC.Unit.level));
		String type = unit.get(SRC.Unit.unitType);
		String[] cost = (Unit.isLegendary(type) ? lLevelCost[lvl] : nLevelCost[lvl]).split(",");
		
		if(currency.get("gold") < Integer.parseInt(cost[0])) return false;
		if(currency.get(type.replace("allies", "")) < Integer.parseInt(cost[1])) return false;
		
		return true;
	}
	
	public boolean canUnlockUnit(String type, boolean dupe) {
		String[] cost;
		if(dupe) {
			cost = "1000 300".split(" ");
		} else {
			cost = (Unit.isLegendary(type) ? lLevelCost[0] : nLevelCost[0]).split(",");
		}
		if(currency.get("gold") < Integer.parseInt(cost[0])) return false;
		if(currency.get(type.replace("allies", "")) < Integer.parseInt(cost[1])) return false;
		
		return true;
	}
	
	
	public Unit[] getUnlockableUnits(Unit[] units) {
		
		JsonObject allTypes = Unit.getTypes();
		
		String[] gotTypes = new String[0];
		
		for(int i=0; i<units.length; i++) {
			gotTypes = add(gotTypes, units[i].get(SRC.Unit.unitType));
		}
		
		List<String> lGotTypes = Arrays.asList(gotTypes);
		
		Unit[] ret = new Unit[0];
		
		for(String type : allTypes.keySet()) {
			if(!lGotTypes.contains(type)) {
				int scrolls = Integer.parseInt((Unit.isLegendary(type) ? lLevelCost : nLevelCost)[0].split(",")[1]);
				
				Integer gotScrolls = currency.get(type.replace("allies", ""));
				if(gotScrolls == null) continue;
				if(scrolls <= gotScrolls.intValue()) {
					ret = add(ret, Unit.createTypeOnly(type, false));
				}
			} else {
				
			}
		}
		
		JsonArray ndupes = new JsonArray();
		
		for(Unit unit : units) {
			String type = unit.get(SRC.Unit.unitType);
			if(ndupes.contains(new JsonPrimitive(type))) {
				ndupes.remove(new JsonPrimitive(type));
			} else {
				ndupes.add(type);
			}
		}
		
		for(Unit unit : units) {
			String type = unit.get(SRC.Unit.unitType);
			if(!ndupes.contains(new JsonPrimitive(type))) continue;
			if(unit.get(SRC.Unit.level).equals("30")) {
				Integer scrolls = currency.get(type.replace("allies", ""));
				if(scrolls >= 300) {
					ret = add(ret, Unit.createTypeOnly(type, true));
				}
			}
		}
		return ret;
	}
	
	
	public String upgradeUnit(Unit unit, SRR req, String specUID) throws URISyntaxException, IOException, NoInternetException {
		String lvl = unit.get(SRC.Unit.level);
		JsonObject ret;
		int cost = Integer.parseInt(
				(Unit.isLegendary(unit.get(SRC.Unit.unitType))
					? lLevelCost[Integer.parseInt(lvl)] 
					: nLevelCost[Integer.parseInt(lvl)]
				).split(",")[0]);
		
		if(lvl.equals("19") || lvl.equals("29")) {
			if(specUID == null) return "no specUID";
			if(specUID.equals("null")) return "no specUID";
			ret = JsonParser.parseObj(req.specializeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId), specUID));
		} else {
			ret = JsonParser.parseObj(req.upgradeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId)));
		}
		
		JsonElement err = ret.get(SRC.errorMessage);
		if(err.isJsonNull()) {
			currency.put("gold", currency.get("gold") - cost);
			return null;
		} else {
			return err.getAsString();
		}
	}
	
	
	public String unlockUnit(String type, boolean dupe, SRR req) throws URISyntaxException, IOException, NoInternetException {
		
		int price = 0;
		if(!canUnlockUnit(type, dupe)) return "not enough gold";

		String text = req.unlockUnit(type);
		if(text == null) return "critical request error";
		
		JsonObject res = JsonParser.parseObj(text);
		
		JsonElement gc = res.getAsJsonObject("data").get("goldCharged");
		if(!gc.isJsonPrimitive()) return res.getAsJsonPrimitive(SRC.errorMessage).getAsString();
		
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
			int cost = Integer.parseInt((Unit.isLegendary(type) ? lLevelCost[lvl] : nLevelCost[lvl]).split(",")[1]);
			
			if(currency.containsKey(type.replace("allies", ""))) {
				int got = currency.get(type.replace("allies", ""));
				if(cost <= got) ret = add(ret, units[i]);
			}
		}
		return ret;
	}
	
	
	public String buyItem(JsonObject item, SRR req) throws URISyntaxException, IOException, NoInternetException {
		if(item.getAsJsonPrimitive("purchased").getAsInt() == 1) return "already purchased";
		
		int price = item.getAsJsonPrimitive("price").getAsInt();
		int gold = 0;
		try {
			gold = currency.get("gold");
		} catch (NullPointerException e) {}
		if(price > gold) return "not enough gold";
		
		currency.put("gold", gold - price);
		JsonObject text = JsonParser.parseObj(req.purchaseStoreItem(item.getAsJsonPrimitive("itemId").getAsString()));
		if(!text.getAsJsonPrimitive("status").getAsString().equals("success")) return text.getAsJsonPrimitive(SRC.errorMessage).getAsString();
		
		shopItems = text.getAsJsonArray("data");
		return null;
	}
	
	public String buyDungeonChest(String serverTime, SRR req) throws URISyntaxException, IOException, NoInternetException {
		if(Time.isAfter(serverTime, "2021-06-15 19:00:00"))
			return "after end";
		
		int keys = currency.get("keys");
		
		if(keys < 10)
			return "not enough keys";
		
		JsonElement err = JsonParser.parseObj(req.purchaseChestItem("dungeonchest")).get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		currency.put("keys", keys - 10);
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
