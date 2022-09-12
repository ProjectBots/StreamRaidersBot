package srlib.skins;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.Json;
import otherlib.Logger;
import otherlib.Options;

public class Skins {
	
	private Hashtable<String, Skin> skins = new Hashtable<>();
	
	public Skins(JsonArray skins) {
		if(skins != null) {
			JsonObject packs = Json.parseObj(Options.get("skins"));
			for(int i=0; i<skins.size(); i++) {
				String uid = skins.get(i).getAsJsonObject().get("productId").getAsString();
				try {
					this.skins.put(uid, new Skin(packs.getAsJsonObject(uid)));
				} catch (NullPointerException e) {
					Logger.printException("Skins -> Skins(): err=unable to ini skin, uid="+uid, e, Logger.runerr, Logger.error, null, null, true);
				}
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
	
	public ArrayList<Skin> searchSkins(String captainId, String unit, SkinType... exclude) {
		ArrayList<Skin> ret = new ArrayList<>();
		for(Skin skin : skins.values())
			if((captainId == null || skin.captainId.equals(captainId))
					&& (unit == null || skin.unit.equals(unit))
					&& !ArrayUtils.contains(exclude, skin.type))
				ret.add(skin);
		return ret;
	}
	
}
