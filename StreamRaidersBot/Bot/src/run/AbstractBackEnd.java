package run;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import srlib.SRC;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

public abstract class AbstractBackEnd<B extends AbstractBackEnd<B>> {
	
	protected final String cid;
	protected final SRR req;
	
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
		Manager.updateSecsOff(jo.getAsJsonObject("info").get("serverTime").getAsString());
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
}
