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
			
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				if(line.equals("")) continue;
				if(line.startsWith("##")) continue;
				
				String[] params = line.split("=", 2);
				tab.put(params[0], params[1]);
			}
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
		BufferedWriter bw = null;
		
		try {
			file.createNewFile();
			w = new FileWriter(file);
			bw = new BufferedWriter(w);
			
			
			String text = "";
			for(String key : table.keySet()) {
				text += key + "=" + table.get(key) + "\n";
			}
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
		
		String text = "";
		try {
			r = new FileReader(file);
			br = new BufferedReader(r);
			
			String line = br.readLine();
			while(line != null) {
				text += line + "\n";
				line = br.readLine();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				r.close();
			} catch (Exception e) {}
		}
		return text;
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
