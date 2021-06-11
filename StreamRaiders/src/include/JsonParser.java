package include;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class JsonParser {

	public static JsonObject parseObj(String json) {
		try {
			return new Gson().fromJson(json, JsonObject.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static JsonArray parseArr(String json) {
		try {
			return new Gson().fromJson(json, JsonArray.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static JsonObject check(JsonObject json, JsonObject def) {
		if(def == null)
			return json;
		if(json == null) {
			json = def.deepCopy();
			return json;
		}
		for(String key : def.keySet()) {
			JsonElement je = json.get(key);
			JsonElement de = def.get(key);
			if(je == null) {
				json.add(key, de);
			} else if(!((je.isJsonObject() && de.isJsonObject()) ||
						(je.isJsonArray() && de.isJsonArray()) ||
						(je.isJsonPrimitive() && de.isJsonPrimitive())
					)) {
				json.add(key, de);
			} else if(de.isJsonPrimitive()) {
				JsonPrimitive dp = de.getAsJsonPrimitive();
				JsonPrimitive jp = je.getAsJsonPrimitive();
				if(	!(	(dp.isBoolean() && jp.isBoolean()) ||
						(dp.isNumber() && jp.isNumber()) ||
						(dp.isString() && jp.isString())
						)) {
					json.add(key, de);
				}
			} else if(je.isJsonObject()) {
				check(json.getAsJsonObject(key), def.getAsJsonObject(key));
			}
		}
		return json;
	}
	
	public static String prettyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
	
}
