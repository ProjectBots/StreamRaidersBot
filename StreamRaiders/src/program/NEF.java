package program;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;

public class NEF {
	
	public static Hashtable<String, String> getOpt(String path) throws FileNotFoundException {
		
		File file = new File(path);
		
		FileReader r = null;
		BufferedReader br = null;
		
		Hashtable<String, String> tab = new Hashtable<>();
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			StringBuilder order = new StringBuilder();
			
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				if(order.length() != 0) order.append("|");
				if(line.equals("") || line.startsWith("##")) {
					order.append(line);
					continue;
				}
				
				String[] params = line.split("=", 2);
				tab.put(params[0], params[1]);
				order.append("{" + params[0] + "}");
			}
			
			tab.put("~order", order.toString());
		} catch (FileNotFoundException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				r.close();
			} catch (Exception e) {}
		}
		return tab;
	}
	
	public static void saveOpt(String path, Hashtable<String, String> table) {
		
		createDir(path);
		
		File file = new File(path);
		
		FileWriter w = null;
		
		try {
			file.createNewFile();
			w = new FileWriter(file);
			
			
			String[] order = new String[0];
			try {
				order = table.get("~order").split("\\|");
				table.remove("~order");
			} catch (Exception e) {}
			
			
			StringBuilder text = new StringBuilder();
			
			for(int i=0; i<order.length; i++) {
				if(order[i].startsWith("{") && order[i].endsWith("}")) {
					try {
						order[i] = order[i].substring(1, order[i].length() - 1);
						text.append(order[i] + "=" + table.get(order[i]) + "\n");
						table.remove(order[i]);
					} catch (Exception e) {}
				} else {
					text.append(order[i]);
				}
			}
			
			for(String key : table.keySet()) {
				text.append(key + "=" + table.get(key) + "\n");
			}
			
			w.write(text.substring(0, text.length() - 1));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				w.close();
			} catch (Exception e) {}
		}
	}
	

	public static void save(String path, String text) {
		
		createDir(path);
		
		File file = new File(path);
		
		FileWriter w = null;
		BufferedWriter bw = null;
		
		try {
			file.createNewFile();
			w = new FileWriter(file);
			bw = new BufferedWriter(w);
			
			bw.write(text);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				w.close();
			} catch (Exception e) {}
		}
	}
	
	public static String read(String path) {
		
		File file = new File(path);
		
		FileReader r = null;
		BufferedReader br = null;
		
		StringBuilder text = new StringBuilder();
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				text.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				r.close();
			} catch (Exception e) {}
		}
		return text.toString();
	}
	
	private static void createDir(String path) {
		
		try {
			path = path.substring(0, path.lastIndexOf("/"));
			
			File dir = new File(path);
			
			if(!dir.exists()) {
				dir.mkdirs();
			}
		} catch (Exception e) {}
	}
	
	
}
