package run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import include.Json;
import otherlib.Logger;
import srlib.SRC;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skin;
import srlib.skins.Skins;
import srlib.souls.Soul;
import srlib.store.Item;
import srlib.store.Store;
import srlib.store.Store.C;
import srlib.units.Unit;
import srlib.Time;

public abstract class AbstractBackEnd<B extends AbstractBackEnd<B>> {
	
	protected final String cid;
	protected final SRR req;
	protected final UpdateEventListener<B> uelis;
	protected Unit[] units;
	protected Soul[] souls;
	protected Skins skins;
	protected Store store;
	

	private HashMap<String, Long> rts = new HashMap<>();
	private int[] updateTimes = new int[] {10, 60, 60, 30};
	
	public void setUpdateTimes(int units, int skins, int souls, int store) {
		updateTimes[0] = units;
		updateTimes[1] = skins;
		updateTimes[2] = souls;
		updateTimes[3] = store;
	}
	
	public SRR getSRR() {
		return req;
	}
	
	public AbstractBackEnd(String cid, SRR req, UpdateEventListener<B> uelis) {
		this.req = req;
		this.cid = cid;
		this.uelis = uelis;
	}

	protected void ini() throws NoConnectionException, NotAuthorizedException {
		updateUnits(true);
		updateStore(true);
		updateSkins(true);
	}
	
	public static interface UpdateEventListener<B extends AbstractBackEnd<B>> {
		public default void afterUpdate(String obj, B be) {};
	}
	
	public String getViewerUserId() {
		return req.getViewerUserId();
	}
	
	protected boolean testUpdate(JsonObject jo) throws NoConnectionException, NotAuthorizedException {
		Time.updateSecsOff(jo.getAsJsonObject("info").get("serverTime").getAsString());
		JsonElement je = jo.get(SRC.errorMessage);
		if(!je.isJsonPrimitive()) 
			return false;
		String err = je.getAsString();
		switch(err) {
		case "Game data mismatch.":
		case "Client lower.":
		case "Account type mismatch.":
			Manager.updateSRData(jo.getAsJsonObject("info").get("dataPath").getAsString(), req);
			return true;
		default:
			throw new StreamRaidersException("BackEnd -> testUpdate: err="+je.getAsString()+", jo="+jo.toString(), cid, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void afterUpdate(String obj) {
		uelis.afterUpdate(obj, (B) this);
	}
	
	synchronized public void updateUnits(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("units");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject units = Json.parseObj(req.getUserUnits());
		if(testUpdate(units))
			units = Json.parseObj(req.getUserUnits());
		
		JsonArray us = units.getAsJsonArray("data");
		this.units = new Unit[us.size()];
		for(int i=0; i<us.size(); i++)
			this.units[i] = new Unit(us.get(i).getAsJsonObject());
		
		rts.put("units", now + updateTimes[0]*60*1000);
		afterUpdate("units");
	}
	
	
	synchronized public void updateSkins(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("skins");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now > wt))
			return;
		
		JsonObject skins = Json.parseObj(req.getUserItems());
		if(testUpdate(skins))
			skins = Json.parseObj(req.getUserItems());
		
		JsonElement data = skins.get("data");
		this.skins = new Skins(data.isJsonArray() ? data.getAsJsonArray() : null);
		
		rts.put("skins", now + updateTimes[1]*60*1000);
		afterUpdate("skins");
	}
	
	synchronized public void updateSouls(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("souls");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject souls = Json.parseObj(req.getUserSouls());
		if(testUpdate(souls))
			souls = Json.parseObj(req.getUserSouls());

		JsonArray ss = souls.getAsJsonArray("data");
		this.souls = new Soul[ss.size()];
		for(int i=0; i<ss.size(); i++)
			this.souls[i] = new Soul(ss.get(i).getAsJsonObject());
		
		
		rts.put("souls", now + updateTimes[2]*60*1000);
		afterUpdate("souls");
	}
	
	synchronized public void updateStore(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("store");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject user = Json.parseObj(req.getUser());
		if(testUpdate(user))
			user = Json.parseObj(req.getUser());
		
		JsonObject cur = Json.parseObj(req.getAvailableCurrencies());
		JsonElement err = cur.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Logger.print("BackEndHandler -> updateStore: cur, err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);

		JsonObject items = Json.parseObj(req.getCurrentStoreItems());
		err = items.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Logger.print("BackEndHandler -> updateStore: items, err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
		
		JsonObject openedChests = Json.parseObj(req.getOpenCountTrackedChests());
		err = openedChests.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			Logger.print("BackEndHandler -> updateStore: openedChests, err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
		
		
		store = new Store(user.getAsJsonObject("data"),
				cur.getAsJsonArray("data"),
				items.getAsJsonArray("data"),
				openedChests.getAsJsonArray("data"));
		
		rts.put("store", now + updateTimes[3]*60*1000);
		afterUpdate("store");
	}
	
	
	public String extractSoul(Unit u) throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		
		if(store.getCurrency(Store.gold) < 2000)
			return "not enough gold";
		
		if(store.getCurrency(Store.soulvessel) < 1)
			return "not enough soulvessel";
		
		JsonObject jo = Json.parseObj(req.extractSoul(""+u.unitId));
		JsonElement err = jo.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		JsonObject data = jo.getAsJsonObject("data");
		
		units = ArrayUtils.remove(units, ArrayUtils.indexOf(units, u));
		souls = ArrayUtils.add(souls, new Soul(data));
		
		store.decreaseCurrency(Store.soulvessel, 1);
		store.decreaseCurrency(Store.gold, 2000);
		
		return null;
	}
	
	/**
	 * equips the soul to the unit (also unequips the soul if it is equiped to another unit beforhand)<br>
	 * 
	 * unequips the soul if either u or s are null
	 * @param u unit
	 * @param s soul
	 * @return error or null
	 * @throws NoConnectionException
	 */
	public String equipSoul(Unit u, Soul s) throws NoConnectionException {
		if(u == null || s == null) {
			unequipSoul(u, s);
			return null;
		}
		
		if(s.getUnitId() == u.unitId)
			return "already equiped";
		
		if(s.getUnitId() != -1)
			unequipSoul(null, s);
		
		if(u.getSoulId() != -1)
			unequipSoul(u, null);
		
		
		
		JsonElement err = Json.parseObj(req.equipSoul(""+s.soulId, ""+u.unitId)).get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		s.setUnit(u);
		u.setSoul(s);
		return null;
	}
	
	private String unequipSoul(Unit u, Soul s) throws NoConnectionException {
		if(u == null) {
			JsonElement err = Json.parseObj(req.unequipSoul(""+s.soulId)).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				return err.getAsString();
			
			for(int i=0; i<units.length; i++) {
				if(units[i].unitId == s.getUnitId()) {
					units[i].setSoul(null);
					break;
				}
			}
			
			s.setUnit(null);
		} else if(s == null) {
			JsonElement err = Json.parseObj(req.unequipSoul(""+u.getSoulId())).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				return err.getAsString();
			
			for(int i=0; i<souls.length; i++) {
				if(souls[i].getUnitId() == u.unitId) {
					souls[i].setUnit(null);
					break;
				}
			}
			
			u.setSoul(null);
		} 
		/*
		 * Would be dead code atm
		 * 
		else {
			if(u.getSoulId() != s.soulId)
				return "u.soulId and s.soulId do not match";
			
			JsonElement err = Json.parseObj(req.unequipSoul(""+s.soulId)).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				return err.getAsString();
			
			u.setSoul(null);
			s.setUnit(null);
		}*/
		return null;
	}
	
	
	public Soul[] getSouls(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateSouls(false);
		return souls;
	}
	
	
	public boolean redeemProductCode(String code) throws NoConnectionException {
		JsonObject rpc = Json.parseObj(req.redeemProductCode(code));
		JsonElement err = rpc.get(SRC.errorMessage);
		if(err == null || !err.isJsonPrimitive()) {
			String skin = rpc.getAsJsonObject("data").get("productId").getAsString();
			try {
				skins.addSkin(skin);
			} catch (Exception e) {
				Logger.printException("AbstractBackEnd -> redeemProductCode: err=failed to add skin, skin="+skin, e, Logger.runerr, Logger.error, cid, null, true);
			}
			return true;
		}
		return false;
	}
	
	public int getCurrency(C con, boolean force) throws NotAuthorizedException, NoConnectionException {
		updateStore(false);
		return store.getCurrency(con);
	}
	
	public void decreaseCurrency(C con, int amount) {
		store.decreaseCurrency(con, amount);
	}
	
	public void addCurrency(C con, int amount) {
		store.addCurrency(con, amount);
	}
	
	public void addCurrency(String type, int amount) {
		store.addCurrency(type, amount);
	}
	
	public void setCurrency(C con, int amount) {
		store.setCurrency(con, amount);
	}
	
	public Hashtable<String, Integer> getCurrencies() throws NotAuthorizedException, NoConnectionException {
		updateStore(false);
		return store.getCurrencies();
	}
	
	
	public JsonObject buyItem(Item item) throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.buyItem(item, req, skins);
	}
	
	public ArrayList<Item> getPurchasableScrolls() throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.getPurchasableScrolls();
	}
	
	public ArrayList<Item> getAvailableEventStoreItems(String section, boolean includePurchased) throws NoConnectionException, NotAuthorizedException {
		updateSkins(false);
		updateStore(false);
		return store.getAvailableEventStoreItems(section, includePurchased, skins);
	}
	
	public Item getDaily() throws NoConnectionException, NotAuthorizedException {
		updateStore(false);
		return store.getDaily();
	}
	
	public String refreshStore() throws NoConnectionException, NotAuthorizedException {
		JsonObject rdata = Json.parseObj(req.purchaseStoreRefresh());
		if(testUpdate(rdata))
			rdata = Json.parseObj(req.purchaseStoreRefresh());
		
		JsonElement err = rdata.get(SRC.errorMessage);
		if(err.isJsonPrimitive())
			return err.getAsString();
		
		store.refreshStore(rdata.getAsJsonArray("data"));
		return null;
	}
	
	public int getStoreRefreshCount() {
		return store.getStoreRefreshCount();
	}
	
	public Skins getSkins(boolean force) throws NoConnectionException, NotAuthorizedException {
		updateSkins(force);
		return skins;
	}
	
	public String equipSkin(Unit unit, Skin skin) throws NoConnectionException {
		JsonObject resp = Json.parseObj(req.equipSkin(""+unit.unitId,
					skin == null ? unit.getSkin() : skin.uid,
					skin == null ? "0" : "1"));
		JsonElement err = resp.get(SRC.errorMessage);
		
		if(err == null || !err.isJsonPrimitive()) {
			unit.setSkin(skin);
			return null;
		} else
			return err.getAsString();
	}
	
	public void setProxyAndUserAgent(String proxyDomain, int proxyPort, String username, String password, String userAgent, boolean mandatory) {
		req.setProxy(proxyDomain, proxyPort, username, password, mandatory);
		req.setUserAgent(userAgent);
	}
}
