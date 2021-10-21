package include;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class Json {

	public static JsonObject parseObj(String json) {
		try {
			return new Gson().fromJson(json, JsonObject.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static JsonArray parseArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
	}
	
	public static JsonObject check(JsonObject json, JsonObject def) {
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
		return new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(json);
	}
	
	public static void set(JsonObject json, String path, JsonElement el) {
		set(json, path.split(" "), el);
	}
	public static void set(JsonObject json, String[] path, JsonElement el) {
		for(int i=0; i<path.length-1; i++) {
			JsonElement tmp = json.get(path[i]);
			if(tmp == null || !tmp.isJsonObject())
				json.add(path[i], new JsonObject());
			json = json.getAsJsonObject(path[i]);
		}
		json.add(path[path.length-1], el);
	}
	
	public static JsonElement get(JsonObject json, String path) {
		return get(json, path.split(" "));
	}
	public static JsonElement get(JsonObject json, String[] path) {
		for(int i=0; i<path.length-1; i++) {
			JsonElement tmp = json.get(path[i]);
			if(tmp == null || !tmp.isJsonObject())
				return null;
			json = tmp.getAsJsonObject();
		}
		return json.get(path[path.length-1]);
	}
	
	
	
}
