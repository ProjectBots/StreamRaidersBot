package srlib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Json;
import include.Http.NoConnectionException;
import otherlib.Options;
import srlib.skins.Skins;
import srlib.units.Unit;
import srlib.units.UnitRarity;

public class Store {
	
	private static JsonObject unitCosts = Json.parseObj(Options.get("unitCosts"));
	
	public static JsonObject genUnitCostsFromData(JsonObject data) {
		unitCosts = data.getAsJsonObject("UnitCosts");
		return unitCosts;
	}

	public static int[] getCost(UnitRarity ur, int lvl, boolean dupe) {
		lvl++;
		String id = ur.toString().toLowerCase()+"_";
		if(dupe) 
			id += "dupe1";
		else if(lvl == 1)
			id += "unlock1";
		else
			id += "upgrade"+lvl;
		
		JsonObject cost = unitCosts.getAsJsonObject(id);
		return new int[] {cost.get("GoldCostViewer").getAsInt(), cost.get("CurrencyCostViewer").getAsInt()};
	}
	
	
	private JsonArray shopItems = new JsonArray();
	private Hashtable<String, Integer> currencies = new Hashtable<>();
	private HashSet<String> openedChests = new HashSet<>();
	
	private int storeRefreshCount = 0;
	
	public int getStoreRefreshCount() {
		return storeRefreshCount;
	}
	
	
	public Store(JsonObject user, JsonArray availableCurrencies, JsonArray currentStoreItems, JsonArray openedChests) {
		
		currencies = new Hashtable<>();
		for(int i=0; i<availableCurrencies.size(); i++) {
			JsonObject c = availableCurrencies.get(i).getAsJsonObject();
			currencies.put(c.get("currencyId").getAsString().replace("cooldown", meat.get()).replace(Options.get("currentEventCurrency"), eventcurrency.get()), Integer.parseInt(c.get("quantity").getAsString()));
		}
		int potion = user.get("epicProgression").getAsInt();
		currencies.put(potions.get(), potion > 100 ? 100 : potion);
		storeRefreshCount = user.get("storeRefreshCount").getAsInt();
		shopItems = currentStoreItems;
		
		for(int i=0; i<openedChests.size(); i++) {
			JsonObject c = openedChests.get(i).getAsJsonObject();
			if(c.get("purchasedQuantity").getAsString().equals("1"))
				this.openedChests.add(c.get("chestId").getAsString());
		}
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
		for(String ut : Unit.getTypesList())
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
		type = type.replace("scroll", "").replace("cooldown", meat.get());
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
		if(type.equals(potions.get()) && amount > 100)
			amount = 100;
		currencies.put(type, amount);
	}
	
	public Hashtable<String, Integer> getCurrencies() {
		return currencies;
	}
	
	public void refreshStore(JsonArray shopItems) throws NoConnectionException {
		this.shopItems = shopItems;
		storeRefreshCount++;
		currencies.put(gold.get(), currencies.get(gold.get()) 
				- (storeRefreshCount > 3 ? 400 : storeRefreshCount * 100));
	}
	
	public boolean canUpgradeUnit(Unit unit) {
		int[] cost = getCost(unit.rarity, unit.level, false);
		
		if(getCurrency(gold) < cost[0] || getCurrency(unit.type) < cost[1])
			return false;
		
		return true;
	}
	

	public Unit[] getUpgradeableUnits(Unit[] units) {
		LinkedList<Unit> ret = new LinkedList<>();
		for(int i=0; i<units.length; i++) {
			int lvl = units[i].level;
			String type = units[i].type;
			if(lvl == 30) 
				continue;
			
			if(getCost(units[i].rarity, lvl, false)[1] <= getCurrency(type))
				ret.add(units[i]);
		}
		return ret.toArray(new Unit[ret.size()]);
	}
	
	public String upgradeUnit(Unit unit, SRR req, String specUID) throws NoConnectionException {
		if(!canUpgradeUnit(unit))
			return "not enough gold";
		
		JsonObject ret;
		if(unit.level == 19) {
			if(specUID == null || specUID.equals("null"))
				return "no specUID";
			ret = Json.parseObj(req.specializeUnit(unit.type, ""+unit.level, ""+unit.unitId, specUID));
		} else {
			ret = Json.parseObj(req.upgradeUnit(unit.type, ""+unit.level, ""+unit.unitId));
		}
		
		JsonElement err = ret.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		int[] cost = getCost(unit.rarity, unit.level, false);
		decreaseCurrency(gold, cost[0]);
		decreaseCurrency(unit.type, cost[1]);
		return null;
	}
	
	public boolean canUnlockUnit(String type, boolean dupe) {
		int[] cost = getCost(UnitRarity.parseType(type), 0, dupe);
		
		if(getCurrency(gold) < cost[0] || getCurrency(type) < cost[1])
			return false;
		
		return true;
	}
	
	
	public Unit[] getUnlockableUnits(Unit[] units) {
		
		ArrayList<String> allTypes = Unit.getTypesList();
		
		Hashtable<String, Boolean> gotTypes = new Hashtable<>();
		for(int i=0; i<units.length; i++) {
			String type = units[i].type;
			if(gotTypes.contains(type))
				gotTypes.put(type, gotTypes.get(type) && units[i].level == 30);
			else
				gotTypes.put(type, units[i].level == 30);
		}
		
		LinkedList<Unit> ret = new LinkedList<>();
		
		//	normal unlock
		for(String type : allTypes) {
			if(gotTypes.containsKey(type))
				continue;
			
			if(getCost(UnitRarity.parseType(type), 0, false)[1] <= getCurrency(type))
				ret.add(Unit.getTypeOnly(type, false));
		}
		
		//	dupe unlock
		for(String type : gotTypes.keySet()) {
			//	only !true if every unit of this type is lvl 30
			if(!gotTypes.get(type))
				continue;
			
			if(getCost(UnitRarity.parseType(type), 0, true)[1] <= getCurrency(type))
				ret.add(Unit.getTypeOnly(type, true));
		}
		return ret.toArray(new Unit[ret.size()]);
	}
	
	public String unlockUnit(String text, Unit type, boolean dupe, SRR req) {
		
		JsonObject res = Json.parseObj(text);
		JsonElement err = res.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		int[] cost = getCost(type.rarity, 0, dupe);
		decreaseCurrency(gold, cost[0]);
		decreaseCurrency(type.type, cost[1]);
		return null;
	}
	
	
	public static class Item {
		@Override
		public String toString() {
			return new StringBuffer(uid)
					.append(":{")
					.append(name)
					.append(" ")
					.append(quantity)
					.append("@")
					.append(price)
					.append("")
					.append(purchased ? "purchased " : "")
					.append(section)
					.append("}")
					.toString();
		}
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Item))
				return false;
			return ((Item) obj).uid.equals(uid);
		}
		
		public final String name;
		public final int price;
		public final int quantity;
		public final boolean purchased;
		public final String section;
		public final String uid;
		//public final String startTime;
		//public final String endTime;
		
		
		public Item(JsonObject pack, boolean purchased) {

			name = pack.get("Item").getAsString();
			price = pack.get("BasePrice").getAsInt();
			quantity = pack.get("Quantity").getAsInt();
			this.purchased = purchased;
			section = pack.get("Section").getAsString();
			uid = pack.get("Uid").getAsString();
			
			/*
			String lt = pack.get("LiveStartTime").getAsString();
			if(lt.equals(""))
				lt = pack.get("BonesStartTime").getAsString();
			startTime = lt.equals("")
							? Time.parse(LocalDateTime.now().plusYears(-100))
							: lt;
			
			lt = pack.get("LiveEndTime").getAsString();
			if(lt.equals(""))
				lt = pack.get("BonesEndTime").getAsString();
			endTime = lt.equals("")
							? Time.parse(LocalDateTime.now().plusYears(100))
							: lt;
			*/
		}
	}
	
	public ArrayList<Item> getPurchasableScrolls() {
		JsonObject packs = Json.parseObj(Options.get("store"));
		ArrayList<Item> ret = new ArrayList<>();
		for(int i=0; i<shopItems.size(); i++) {
			JsonObject item = shopItems.get(i).getAsJsonObject();
			JsonObject pack = packs.get(item.get("itemId").getAsString()).getAsJsonObject();
			String let = pack.get("LiveEndTime").getAsString();
			if(item.get("purchased").getAsString().equals("0")
				&& item.get("section").getAsString().equals("Scrolls")
				&& (let.equals("") || Time.isAfterServerTime(let)))
				ret.add(new Item(pack, false));
		}
		return ret;
	}
	
	public Item getDaily() {
		JsonObject daily = Json.parseObj(Options.get("store")).getAsJsonObject("dailydrop");
		
		String let = daily.get("LiveEndTime").getAsString();
		if(Time.isBeforeServerTime(let))
			return null;
		
		String lst = daily.get("LiveStartTime").getAsString();
		if(Time.isAfterServerTime(lst))
			return null;
		
		for(int i=0; i<shopItems.size(); i++) {
			JsonObject item = shopItems.get(i).getAsJsonObject();
			if(!item.get("itemId").getAsString().equals("dailydrop"))
				continue;
			
			return item.get("purchased").getAsString().equals("1") ? null : new Item(daily, false);
		}
		return null;
	}
	
	public ArrayList<Item> getAvailableEventStoreItems(String section, boolean includePurchased, Skins skins) {
		JsonObject packs = Json.parseObj(Options.get("store"));
		
		//	check for already purchased items
		HashSet<String> purchasedItems = new HashSet<>();
		for(int i=0; i<shopItems.size(); i++) {
			JsonObject item = shopItems.get(i).getAsJsonObject();
			if(item.get("purchased").getAsString().equals("1"))
				purchasedItems.add(item.get("itemId").getAsString());
		}
		
		ArrayList<Item> ret = new ArrayList<>();
		for(String key : packs.keySet()) {
			JsonObject pack = packs.getAsJsonObject(key);
			
			//filter out different sections
			if(!pack.get("Section").getAsString().equals(section))
				continue;
			
			//filter out other stuff
			if(!filter(pack))
				continue;
			
			//check if purchased
			boolean purchased = openedChests.contains(key) || purchasedItems.contains(key) || skins.hasSkin(key);
			
			if(!includePurchased && purchased)
				continue;
			
			//item passed all filters
			ret.add(new Item(pack, purchased));
		}
		
		return ret;
	}
	
	public static ArrayList<String> getCurrentEventChests() {
		ArrayList<String> ret = new ArrayList<>();
		JsonObject store = Json.parseObj(Options.get("store"));
		
		for(String key : store.keySet()) {
			if(!key.contains("chest"))
				continue;
			
			JsonObject pack = store.getAsJsonObject(key);
			
			if(!filter(pack))
				continue;
			
			ret.add(key);
		}
		
		return ret;
	}
	
	private static boolean filter(JsonObject pack) {
		
		//filtering out stuff for real money
		if(pack.get("BasePrice").getAsInt() == -1)
			return false;
		
		//filtering out stuff which is not available to account type
		String at = pack.get("AvailableTo").getAsString();
		if(!(at.equals("All") || at.equals("Viewer")))
			return false;
		
		//filtering out stuff which isn't available atm 
		String let = pack.get("LiveEndTime").getAsString();
		if(let.equals(""))
			let = pack.get("BonesEndTime").getAsString();
		if(!let.equals("") && Time.isBeforeServerTime(let))
			return false;
		
		String lst = pack.get("LiveStartTime").getAsString();
		if(lst.equals(""))
			lst = pack.get("BonesStartTime").getAsString();
		if(!lst.equals("") && Time.isAfterServerTime(lst))
			return false;
		
		return true;
	}
	
	public static enum BuyType {
		CHEST, SKIN, DAILY, ITEM
	}
	
	
	public JsonObject buyItem(Item item, SRR req, Skins skins) throws NoConnectionException {
		JsonObject ret = new JsonObject();
		C cur;
		switch(item.section) {
		case SRC.Store.dungeon:
			cur = keys;
			break;
		case SRC.Store.event:
			cur = eventcurrency;
			break;
		case SRC.Store.bones:
			cur = bones;
			break;
		default:
			cur = gold;
			break;
		}
		
		int price = item.price;
		
		if(price > getCurrency(cur)) {
			ret.addProperty(SRC.errorMessage, "not enough "+cur.get());
			return ret;
		}
		
		String itype = item.name.replace("scroll", "").replace("cooldown", meat.get());
		String itemId = item.uid;
		switch(itype) {
		case "chest":
			ret.addProperty("buyType", BuyType.CHEST.toString());
			
			Json.override(ret, Json.parseObj(req.purchaseChestItem(itemId)));
			
			if(ret.get(SRC.errorMessage).isJsonPrimitive())
				return ret;
			break;
		case "skin":
			ret.addProperty("buyType", BuyType.SKIN.toString());
			
			Json.override(ret, Json.parseObj(req.purchaseStoreSkin(itemId)));
			
			if(ret.get(SRC.errorMessage).isJsonPrimitive())
				return ret;
			
			skins.addSkin(item.uid);
			break;
		default:
			if(itemId.equals("dailydrop")) {
				ret.addProperty("buyType", BuyType.DAILY.toString());
				Json.override(ret, Json.parseObj(req.grantDailyDrop()));
			} else {
				ret.addProperty("buyType", BuyType.ITEM.toString());
				
				Json.override(ret, Json.parseObj(req.purchaseStoreItem(itemId)));
				
				if(ret.get(SRC.errorMessage).isJsonPrimitive())
					return ret;
				
				shopItems = ret.getAsJsonArray("data").deepCopy();
			}
			break;
		}
		
		decreaseCurrency(cur, price);
		
		return ret;
	}
	
	
}
