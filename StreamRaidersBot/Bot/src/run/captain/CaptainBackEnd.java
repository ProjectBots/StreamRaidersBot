package run.captain;

import java.util.HashMap;

import com.google.gson.JsonObject;

import include.Json;
import include.Http.NoConnectionException;
import run.AbstractBackEnd;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skins;

public class CaptainBackEnd extends AbstractBackEnd<CaptainBackEnd> {
	

	private HashMap<String, Long> rts = new HashMap<>();
	private int[] updateTimes = new int[] {60};

	public CaptainBackEnd(String cid, SRR req) {
		super(cid, req);
	}
	
	@Override
	protected void ini() throws NoConnectionException, NotAuthorizedException {
		updateSkins(true);
	}
	
	public void setUpdateTimes(int skins) {
		updateTimes[0] = skins;
	}
	
	synchronized public void updateSkins(boolean force) throws NoConnectionException, NotAuthorizedException {
		Long wt = rts.get("skins");
		long now = System.currentTimeMillis();
		if(!force && !(wt == null || now - wt > 0))
			return;
		
		JsonObject skins = Json.parseObj(req.getUserItems());
		if(testUpdate(skins))
			skins = Json.parseObj(req.getUserItems());
		
		this.skins = new Skins(skins.get("data").isJsonArray() ? skins.getAsJsonArray("data") : null);
		
		rts.put("skins", now + updateTimes[0]*60*1000);
		uelis.afterUpdate("skins", this);
	}

}
