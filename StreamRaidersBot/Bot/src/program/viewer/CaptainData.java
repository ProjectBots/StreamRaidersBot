package program.viewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import program.SRC;

public class CaptainData {

	private final JsonObject cap;
	
	public CaptainData(JsonObject cap) {
		this.cap = cap;
	}
	
	public String get(String con) {
		if(!cap.has(con))
			return null;
		JsonElement je = cap.get(con);
		if(!je.isJsonPrimitive())
			return null;
		return je.getAsString();
	}
	
	@Override
	public String toString() {
		return get(SRC.Captain.twitchDisplayName);
	}
	
}
