package include;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonParser {

	public static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	public static JsonObject json(String json, JsonObject def, boolean nested) {
		JsonObject jo = json(json);
		for(String key : def.keySet()) 
			if(!jo.has(key))
				jo.add(key, def.get(key));
		
		if(!nested) return jo;
		
		for(String key : def.keySet()) {
			JsonElement je = def.get(key);
			if(je.isJsonObject()) 
				jo.add(key, json(jo.get(key).toString(), def.getAsJsonObject(key), true));
		}
		return jo;
	}
	
	public static JsonArray jsonArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
	}
	
	public static String prettyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
	
}
