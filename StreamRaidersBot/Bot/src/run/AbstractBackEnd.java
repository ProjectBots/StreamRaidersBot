package run;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import include.Json;
import otherlib.Logger;
import srlib.SRC;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skins;
import srlib.Time;

public abstract class AbstractBackEnd<B extends AbstractBackEnd<B>> {
	
	protected final String cid;
	protected final SRR req;
	protected Skins skins;
	
	public SRR getSRR() {
		return req;
	}
	
	public AbstractBackEnd(String cid, SRR req) {
		this.req = req;
		this.cid = cid;
	}

	protected abstract void ini() throws NoConnectionException, NotAuthorizedException;
	
	public static interface UpdateEventListener<B extends AbstractBackEnd<B>> {
		public default void afterUpdate(String obj, B be) {};
	}
	
	protected UpdateEventListener<B> uelis = null;
	
	public void setUpdateEventListener(UpdateEventListener<B> uelis) {
		this.uelis = uelis;
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
	
	public String getViewerUserId() {
		return req.getViewerUserId();
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
	
	public void setProxyAndUserAgent(String proxyDomain, int proxyPort, String username, String password, String userAgent, boolean mandatory) {
		req.setProxy(proxyDomain, proxyPort, username, password, mandatory);
		req.setUserAgent(userAgent);
	}
}
