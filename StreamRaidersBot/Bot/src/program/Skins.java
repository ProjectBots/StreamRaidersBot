package program;

import java.util.ArrayList;
import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.Json;

public class Skins {
	
	public static class Skin {
		public final String uid;
		public final String unit;
		public final String disname;
		public final String type;
		public Skin(JsonObject pack) {
			uid = pack.get("Uid").getAsString();
			unit = pack.get("BaseUnitType").getAsString();
			disname = pack.get("DisplayName").getAsString();
			type = pack.get("Type").getAsString();
		}
		@Override
		public String toString() {
			return "{uid="+uid+", disname="+disname+", unit="+unit+", type="+type+"}";
		}
	}

	private Hashtable<String, Skin> skins = new Hashtable<>();
	
	public Skins(JsonArray skins) {
		if(skins != null) {
			JsonObject packs = Json.parseObj(Options.get("skins"));
			for(int i=0; i<skins.size(); i++) {
				String uid = skins.get(i).getAsJsonObject().get("productId").getAsString();
				this.skins.put(uid, new Skin(packs.getAsJsonObject(uid)));
			}
		}
	}
	
	public boolean hasSkin(String uid) {
		return skins.containsKey(uid);
	}
	
	public void addSkin(String uid) {
		skins.put(uid, new Skin(Json.parseObj(Options.get("skins")).getAsJsonObject(uid)));
	}
	
	public ArrayList<String> getSkinUids() {
		return new ArrayList<>(skins.keySet());
	}
	
	public Skin getSkin(String skinUid) {
		return skins.get(skinUid);
	}
	
}
