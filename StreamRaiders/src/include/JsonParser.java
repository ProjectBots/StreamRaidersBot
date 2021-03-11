package include;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonParser {

	public static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	public static JsonArray jsonArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
	}
	
	public static String prettyJson(JsonObject jo) {
		Gson gb = new GsonBuilder().setPrettyPrinting().create();
		return gb.toJson(jo);
	}
	
	public static String prettyJson(JsonArray ja) {
		Gson gb = new GsonBuilder().setPrettyPrinting().create();
		return gb.toJson(ja);
	}
	
	public static JsonObject jsonFromFile(String path) throws IOException {
		FileReader r = null;
		BufferedReader br = null;
		
		JsonObject jo = null;
		try {
			r = new FileReader(new File(path));
			br = new BufferedReader(r);
			
			StringBuilder sb = new StringBuilder();
			
			for(String line = br.readLine(); line != null; line = br.readLine()) sb.append(line);
			
			jo = json(sb.toString());
		} finally {
			if(br != null) {
				br.close();
				r.close();
			}
		}
		return jo;
	}
}
