package program;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

public class StreamRaiders {
	
	private static Hashtable<String, String> opt = null;
	
	public static String get(String key) {
		return opt.get(key);
	}
	
	synchronized public static void set(String key, String value) {
		opt.put(key, value);
	}
	
	synchronized public static void save() {
		NEF.saveOpt("data/opt.app", opt);
	}
	
	synchronized public static void log(String text, Exception e) {
		if(e != null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			
			if(text != null) {
				System.err.println(text);
				NEF.log("logs.app", text + "\n" + sw.toString());
			} else {
				NEF.log("logs.app", sw.toString());
			}
			
			System.err.println(sw.toString());
		} else {
			System.err.println(text);
			NEF.log("logs.app", text);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("\r\n"
				+ "\u0009███████╗██████╗     ██████╗  ██████╗ ████████╗\r\n"
				+ "\u0009██╔════╝██╔══██╗    ██╔══██╗██╔═══██╗╚══██╔══╝\r\n"
				+ "\u0009███████╗██████╔╝    ██████╔╝██║   ██║   ██║   \r\n"
				+ "\u0009╚════██║██╔══██╗    ██╔══██╗██║   ██║   ██║   \r\n"
				+ "\u0009███████║██║  ██║    ██████╔╝╚██████╔╝   ██║   \r\n"
				+ "\u0009╚══════╝╚═╝  ╚═╝    ╚═════╝  ╚═════╝    ╚═╝   \r\n"
				+ "\r\n");
		
		try {
			opt = NEF.getOpt("data/opt.app");
		} catch (FileNotFoundException fnf) {
			System.err.println("Couldnt load \"opt.app\"");
			return;
		}
		
		System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaiderBot\r\n"
				+ "Version: " + get("botVersion") + "\r\n");
		
		Raid.loadTypViewChestRews();
		MainFrame.open();
	}
}
