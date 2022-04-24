package run;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import program.SRC;
import program.SRR;
import program.SRR.NotAuthorizedException;

public abstract class BackEnd {
	
	final String cid;
	final SRR req;
	
	public SRR getSRR() {
		return req;
	}
	
	public BackEnd(String cid, SRR req) {
		this.req = req;
		this.cid = cid;
	}
	
	
	public static interface UpdateEventListener {
		public default void afterUpdate(String obj) {};
	}
	
	UpdateEventListener uelis = new UpdateEventListener() {};
	
	public void setUpdateEventListener(UpdateEventListener uelis) {
		this.uelis = uelis;
	}
	
	boolean testUpdate(JsonObject jo) throws NoConnectionException, NotAuthorizedException {
		JsonElement je = jo.get(SRC.errorMessage);
		Manager.updateSecsOff(jo.getAsJsonObject("info").get("serverTime").getAsString());
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
