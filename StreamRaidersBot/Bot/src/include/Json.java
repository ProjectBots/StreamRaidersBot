package include;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Json {

	public static JsonObject parseObj(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	public static JsonArray parseArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
	}

	public static JsonElement fromObj(Object in) {
		return new Gson().toJsonTree(in);
	}
	
	public static <T>T toObj(JsonElement in, Class<T> c) {
		return new Gson().fromJson(in, c);
	}
	
	public static JsonObject check(JsonObject json, JsonObject def) {
		return check(json, def, false, null);
	}
	
	public static JsonObject check(JsonObject json, JsonObject def, boolean rem, JsonObject spare) {
		
		JsonObject objs = spare == null ? null : spare.getAsJsonObject("Objects");
		JsonArray items = spare == null ? null : spare.getAsJsonArray("Items");
		JsonObject values = spare == null ? null : spare.getAsJsonObject("Values");
		
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
				check(json.getAsJsonObject(key), def.getAsJsonObject(key), rem && !(items != null && items.contains(new JsonPrimitive(key))), objs == null ? null : objs.getAsJsonObject(key));
			}
		}
		
		if(rem) {
			for(String key : json.keySet().toArray(new String[json.size()])) {
				if((items == null || !items.contains(new JsonPrimitive(key))) && !def.has(key))
					json.remove(key);
				
				if(values != null && values.has(key)) {
					JsonElement tmp = json.get(key);
					if(!(tmp.isJsonPrimitive() ? tmp.getAsString() : tmp.toString()).matches(values.get(key).getAsString()))
						json.add(key, def.get(key));
				}
			}
		}
		
		return json;
	}
	
	public static JsonObject treeAddItem(JsonObject tree, String path) {
		return treeAddItem(tree, new ArrayList<>(Arrays.asList(path.split(" "))));
	}
	
	public static JsonObject treeAddItem(JsonObject tree, List<String> path) {
		JsonObject tmp = tree;
		while(path.size() > 1) {
			String s = path.remove(0);
			if(!tmp.has("Objects"))
				tmp.add("Objects", new JsonObject());
			tmp = tmp.getAsJsonObject("Objects");
			if(!tmp.has(s))
				tmp.add(s, new JsonObject());
			tmp = tmp.getAsJsonObject(s);
		}
		if(!tmp.has("Items"))
			tmp.add("Items", new JsonArray());
		tmp.getAsJsonArray("Items").add(path.remove(0));
		return tree;
	}
	
	public static JsonObject treeAddValue(JsonObject tree, String path, String regex) {
		return treeAddValue(tree, new ArrayList<>(Arrays.asList(path.split(" "))), regex);
	}
	
	public static JsonObject treeAddValue(JsonObject tree, List<String> path, String regex) {
		JsonObject tmp = tree;
		while(path.size() > 1) {
			String s = path.remove(0);
			if(!tmp.has("Objects"))
				tmp.add("Objects", new JsonObject());
			tmp = tmp.getAsJsonObject("Objects");
			if(!tmp.has(s))
				tmp.add(s, new JsonObject());
			tmp = tmp.getAsJsonObject(s);
		}
		if(!tmp.has("Values"))
			tmp.add("Values", new JsonObject());
		tmp.getAsJsonObject("Values").addProperty(path.remove(0), regex);
		return tree;
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
