package include;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class NEF {
	
	public static Hashtable<String, String> getOpt(String path) throws IOException {
		
		File file = new File(path);
		
		FileReader r = null;
		BufferedReader br = null;
		
		Hashtable<String, String> tab = new Hashtable<>();
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			StringBuilder order = new StringBuilder();
			
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				if(order.length() != 0) order.append("~~~");
				if(line.equals("") || line.startsWith("##")) {
					order.append(line);
					continue;
				}
				
				String[] params = line.split("=", 2);
				tab.put(params[0], params[1]);
				order.append("{" + params[0] + "}");
			}
			
			tab.put("~order", order.toString());
		} finally {
			if(br != null)
				br.close();
			if(r != null)
				r.close();
		}
		return tab;
	}
	
	public static void saveOpt(String path, Hashtable<String, String> tab) throws IOException {
		
		Hashtable<String, String> table = new Hashtable<>();
		
		for(String key : tab.keySet()) {
			table.put(key, tab.get(key));
		}
		
		
		createDir(path);
		
		File file = new File(path);
		
		FileWriter w = null;
		
		try {
			file.createNewFile();
			w = new FileWriter(file);
			
			
			String[] order = new String[0];
			
			try {
				order = table.get("~order").split("~~~");
				table.remove("~order");
			} catch (NullPointerException e) {}
			
			
			StringBuilder text = new StringBuilder();
			
			for(int i=0; i<order.length; i++) {
				if(order[i].startsWith("{") && order[i].endsWith("}")) {
					try {
						order[i] = order[i].substring(1, order[i].length() - 1);
						text.append(order[i] + "=" + table.get(order[i]) + "\n");
						table.remove(order[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					text.append(order[i] + "\n");
				}
			}
			
			for(String key : table.keySet()) {
				text.append(key + "=" + table.get(key) + "\n");
			}
			
			w.write(text.substring(0, text.length() - 1));
		} finally {
			if(w != null)
				w.close();
		}
	}
	

	public static void save(String path, String text) throws IOException {
		save(path, text, false);
	}
	
	public static void save(String path, String text, boolean append) throws IOException {
		
		createDir(path);
		
		File file = new File(path);
		
		FileWriter w = null;
		
		try {
			file.createNewFile();
			w = new FileWriter(file, append);
			
			w.write(text);
		} finally {
			if(w != null)
				w.close();
		}
	}
	
	public static String read(String path) throws IOException {
		FileReader r = null;
		StringBuilder text = new StringBuilder();
		try {
			r = new FileReader(new File(path));
			for(int c = r.read(); c != -1; c = r.read())
				text.append((char) c);
		} finally {
			if(r != null)
				r.close();
		}
		return text.toString().replaceAll("(\r\n|\r)", "\n");
	}
	
	public static String[] readLines(String path) throws IOException {
		File file = new File(path);
		
		FileReader r = null;
		BufferedReader br = null;
		
		String[] lines = null;
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			lines = br.lines().toArray(size -> new String[size]);
		} finally {
			if(br != null)
				br.close();
			if(r != null)
				r.close();
		}
		return lines;
	}
	
	private static void createDir(String path) {
		int index = path.lastIndexOf("/");
		if(index == -1)
			index = path.lastIndexOf("\\");
		try {
			path = path.substring(0, index);
		} catch (StringIndexOutOfBoundsException e) {
			return;
		}
		
		File dir = new File(path);
		
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	
}
