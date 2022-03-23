package program;

import com.google.gson.JsonObject;

public class Captain {

	private final JsonObject cap;
	
	public Captain(JsonObject cap) {
		this.cap = cap;
	}
	
	public String get(String con) {
		if(!cap.has(con)) return null;
		return cap.get(con).getAsString();
	}
	
	@Override
	public String toString() {
		return get(SRC.Captain.twitchDisplayName);
	}
	
}
