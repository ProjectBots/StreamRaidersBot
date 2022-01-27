package program;

import java.util.HashSet;
import java.util.Hashtable;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Time;
import include.Http.NoConnectionException;

public class Store {

	public static final String[] nLevelCost = Options.get("nlevelcost").split("\\|");
	public static final String[] lLevelCost = Options.get("llevelcost").split("\\|");
	
	private JsonArray shopItems = new JsonArray();
	private Hashtable<String, Integer> currencies = new Hashtable<>();
	
	private int storeRefreshCount = 0;
	
	public int getStoreRefreshCount() {
		return storeRefreshCount;
	}
	
	public static class C {
		private String con = null;
		public C(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	public static final C potions = new C("potions");
	public static final C gold = new C("gold");
	public static final C keys = new C("keys");
	public static final C eventcurrency = new C("eventcurrency");
	public static final C bones = new C("bones");
	public static final C meat = new C("meat");
	
	
	private static final HashSet<String> currencyTypes = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
		JsonObject uts = Unit.getTypes();
		for(String ut : uts.keySet())
			add(ut.replace("allies", ""));
		add(potions.get());
		add(gold.get());
		add(keys.get());
		add(eventcurrency.get());
		add(bones.get());
		add(meat.get());
	}};
	
	public int getCurrency(C con) {
		return getCurrency(con.get());
	}
	
	public int getCurrency(String type) {
		type = type.replace("allies", "");
		if(currencies.containsKey(type))
			return currencies.get(type);
		else
			return 0;
	}
	
	public void decreaseCurrency(C con, int amount) {
		decreaseCurrency(con.get(), amount);
	}
	
	public void decreaseCurrency(String type, int amount) {
		if(currencies.containsKey(type))
			setCurrency(type, currencies.get(type) - amount);
		else
			setCurrency(type, -amount);
	}
	
	public void addCurrency(C con, int amount) {
		addCurrency(con.get(), amount);
	}
	
	public void addCurrency(String type, int amount) {
		if(currencyTypes.contains(type)) {
			if(currencies.containsKey(type))
				setCurrency(type, currencies.get(type) + amount);
			else
				setCurrency(type, amount);
		}
	}
	
	public void setCurrency(C con, int amount) {
		setCurrency(con.get(), amount);
	}
	
	public void setCurrency(String type, int amount) {
		if(type.equals(potions.get()) && amount > 60)
			amount = 60;
		currencies.put(type, amount);
	}
	
	public Hashtable<String, Integer> getCurrencies() {
		return currencies;
	}
	
	public Store(SRR req) throws NoConnectionException {
		refreshCurrency(req);
		getCurrentStoreItems(req);
	}
	
	public Store(JsonObject user, JsonArray availableCurrencies, JsonArray currentStoreItems) {
		currencies = new Hashtable<>();
		for(int i=0; i<availableCurrencies.size(); i++) {
			JsonObject c = availableCurrencies.get(i).getAsJsonObject();
			currencies.put(c.getAsJsonPrimitive("currencyId").getAsString().replace("cooldown", meat.get()).replace("snowflake", eventcurrency.get()), Integer.parseInt(c.getAsJsonPrimitive("quantity").getAsString()));
		}
		int potion = user.get("epicProgression").getAsInt();
		currencies.put(potions.get(), potion > 60 ? 60 : potion);
		storeRefreshCount = user.get("storeRefreshCount").getAsInt();
		shopItems = currentStoreItems;
	}
	
	public void refreshCurrency(SRR req) throws NoConnectionException {
		currencies = new Hashtable<>();
		String r = req.getAvailableCurrencies();
		JsonArray cs = Json.parseObj(r).getAsJsonArray("data");
		for(int i=0; i<cs.size(); i++) {
			JsonObject c = cs.get(i).getAsJsonObject();
			currencies.put(c.getAsJsonPrimitive("currencyId").getAsString().replace("cooldown", meat.get()).replace("snowflake", eventcurrency.get()), Integer.parseInt(c.getAsJsonPrimitive("quantity").getAsString()));
		}
		JsonObject user = Json.parseObj(req.getUser()).getAsJsonObject("data");
		int potion = user.get("epicProgression").getAsInt();
		currencies.put(potions.get(), potion > 60 ? 60 : potion);
		storeRefreshCount = user.get("storeRefreshCount").getAsInt();
	}
	
	public void getCurrentStoreItems(SRR req) throws NoConnectionException {
		shopItems = Json.parseObj(req.getCurrentStoreItems()).getAsJsonArray("data");
	}
	
	public String refreshStore(SRR req) throws NoConnectionException {
		JsonObject raw = Json.parseObj(req.purchaseStoreRefresh());
		
		JsonElement err = raw.get(SRC.errorMessage);
		
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		
		shopItems = raw.getAsJsonArray("data");
		storeRefreshCount++;
		if(storeRefreshCount > 3) {
			currencies.put("gold", currencies.get("gold") - 400);
		} else {
			currencies.put("gold", currencies.get("gold") - (storeRefreshCount * 100));
		}
		
		return null;
	}
	
	public void refreshStore(JsonArray shopItems) throws NoConnectionException {
		this.shopItems = shopItems;
		if(++storeRefreshCount > 3) {
			currencies.put("gold", currencies.get("gold") - 400);
		} else {
			currencies.put("gold", currencies.get("gold") - (storeRefreshCount * 100));
		}
	}
	
	
	private static int[] getCost(String type, int lvl, boolean dupe) {
		if(dupe) 
			return new int[] {1000, Unit.isLegendary(type) ? 30 : 300};
		
		String[] cost = (Unit.isLegendary(type) ? lLevelCost[lvl] : nLevelCost[lvl]).split(",");
		return new int[] {Integer.parseInt(cost[0]), Integer.parseInt(cost[1])};
	}
	
	public boolean canUpgradeUnit(Unit unit) {
		int lvl = Integer.parseInt(unit.get(SRC.Unit.level));
		String type = unit.get(SRC.Unit.unitType);
		int[] cost = getCost(type, lvl, false);
		
		if(getCurrency(gold) < cost[0] || getCurrency(type) < cost[1])
			return false;
		
		return true;
	}
	

	public Unit[] getUpgradeableUnits(Unit[] units) {
		Unit[] ret = new Unit[0];
		for(int i=0; i<units.length; i++) {
			int lvl = Integer.parseInt(units[i].get(SRC.Unit.level));
			String type = units[i].get(SRC.Unit.unitType);
			if(lvl == 30) 
				continue;
			
			if(getCost(type, lvl, false)[1] <= getCurrency(type))
				ret = add(ret, units[i]);
		}
		return ret;
	}
	
	public String upgradeUnit(Unit unit, SRR req, String specUID) throws NoConnectionException {
		if(!canUpgradeUnit(unit))
			return "not enough gold";
		
		String lvl = unit.get(SRC.Unit.level);
		JsonObject ret;
		if(lvl.equals("19")) {
			if(specUID == null || specUID.equals("null"))
				return "no specUID";
			ret = Json.parseObj(req.specializeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId), specUID));
		} else {
			ret = Json.parseObj(req.upgradeUnit(unit.get(SRC.Unit.unitType), lvl, unit.get(SRC.Unit.unitId)));
		}
		
		JsonElement err = ret.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		String ut = unit.get(SRC.Unit.unitType);
		int[] cost = getCost(ut, Integer.parseInt(lvl), false);
		decreaseCurrency(gold, cost[0]);
		decreaseCurrency(ut, cost[1]);
		return null;
	}
	
	public boolean canUnlockUnit(String type, boolean dupe) {
		int[] cost = getCost(type, 0, dupe);
		
		if(getCurrency(gold) < cost[0] || getCurrency(type) < cost[1])
			return false;
		
		return true;
	}
	
	
	public Unit[] getUnlockableUnits(Unit[] units) {
		
		JsonObject allTypes = Unit.getTypes();
		
		Hashtable<String, Boolean> gotTypes = new Hashtable<>();
		for(int i=0; i<units.length; i++) {
			String type = units[i].get(SRC.Unit.unitType);
			int lvl = Integer.parseInt(units[i].get(SRC.Unit.level));
			if(gotTypes.contains(type))
				gotTypes.put(type, gotTypes.get(type) && lvl == 30);
			else
				gotTypes.put(type, lvl == 30);
		}
		
		Unit[] ret = new Unit[0];
		
		//	normal unlock
		for(String type : allTypes.keySet()) {
			if(gotTypes.containsKey(type))
				continue;
			
			if(getCost(type, 0, false)[1] <= getCurrency(type))
				ret = add(ret, Unit.createTypeOnly(type, false));
		}
		
		//	dupe unlock
		for(String type : gotTypes.keySet()) {
			//	only !true if every unit of this type is lvl 30
			if(!gotTypes.get(type))
				continue;
			
			if(getCost(type, 0, true)[1] <= getCurrency(type))
				ret = add(ret, Unit.createTypeOnly(type, true));
		}
		return ret;
	}
	
	//	TODO handle request in BackEndHandler.java
	public String unlockUnit(String type, boolean dupe, SRR req) throws NoConnectionException {
		
		String text = req.unlockUnit(type);
		
		JsonObject res = Json.parseObj(text);
		JsonElement err = res.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		int[] cost = getCost(type, 0, dupe);
		decreaseCurrency(gold, cost[0]);
		decreaseCurrency(type, cost[1]);
		return null;
	}
	
	
	public String buyItem(JsonObject item, JsonObject pack, SRR req, String serverTime) throws NoConnectionException {
		if(item.get("purchased").getAsInt() == 1) return "already purchased";
		String let = pack.get("LiveEndTime").getAsString();
		if(!let.equals("") && Time.isAfter(serverTime, let)) return "after end";
		
		int price = item.get("price").getAsInt();
		C cur = pack.get("Section").getAsString().equals("Dungeon") ? keys : gold;
		if(price > getCurrency(cur))
			return "not enough " + cur.get();
		
		String itemId = item.get("itemId").getAsString();
		
		if(itemId.equals("dailydrop")) {
			JsonObject resp = Json.parseObj(req.grantDailyDrop());
			JsonElement err = resp.get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				return err.getAsString();
		} else {
			JsonObject resp = Json.parseObj(req.purchaseStoreItem(itemId));
			
			JsonElement err = resp.get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				return err.getAsString();
			
			decreaseCurrency(cur, price);
			shopItems = resp.getAsJsonArray("data");
		}
		
		addCurrency(pack.get("Item").getAsString(), pack.get("Quantity").getAsInt());
		return null;
	}
	
	public JsonObject buyChest(String serverTime, String chest, SRR req) throws NoConnectionException {
		JsonObject ret = new JsonObject();
		if(Time.isAfter(serverTime, Options.get(chest+"date"))) {
			ret.addProperty(SRC.errorMessage, "after end");
			return ret;
		}
		
		int price = Integer.parseInt(Options.get(chest+"price"));
		
		if(getCurrency(keys) < price) {
			ret.addProperty(SRC.errorMessage, "not enough "+keys.get());
			return ret;
		}
		
		ret = Json.parseObj(req.purchaseChestItem(chest));
		
		JsonElement err = ret.get(SRC.errorMessage);
		
		if(!err.isJsonPrimitive())
			decreaseCurrency(keys, price);
		
		return ret;
	}
	
	public JsonArray getStoreItems(int con, String section) {
		switch(con) {
		case SRC.Store.notPurchased:
			JsonArray ret = new JsonArray();
			for(int i=0; i<shopItems.size(); i++) {
				JsonObject item = shopItems.get(i).getAsJsonObject();
				if(item.get("purchased").getAsString().equals("0") && item.get("section").getAsString().equals(section)) {
					ret.add(item.deepCopy());
				}
			}
			return ret;
		default:
			return shopItems.deepCopy();
		}
	}
	
	
	
	private <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
	
	
}
