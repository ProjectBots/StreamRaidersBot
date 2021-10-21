package program;

import java.io.IOException;
import java.util.Hashtable;

import include.NEF;

public class Options {
	
	private static String op = "data/opt.txt";
	
	private static Hashtable<String, String> opt = null;
	
	public static String get(String key) {
		return opt.get(key);
	}
	
	synchronized public static void set(String key, String value) {
		opt.put(key, value);
	}
	
	synchronized public static void save() {
		try {
			NEF.saveOpt(op, opt);
		} catch (IOException e) {
			Debug.printException("Options -> save: err=failed to save options", e, Debug.runerr, Debug.error, true);
		}
	}
	
	synchronized public static void load() throws IOException {
		opt = NEF.getOpt(op);
	}
	
}
