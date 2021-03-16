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
	
	synchronized public static void log(String text, Exception e) {
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		if(text != null) {
			System.out.println(text);
			NEF.log("logs.app", text + "\n" + sw.toString());
		} else {
			NEF.log("logs.app", sw.toString());
		}
		
		System.out.println(sw.toString());
		
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
			System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaiderBot\r\n"
					+ "Version: " + get("botVersion") + "\r\n");
			MainFrame.open();
		} catch (FileNotFoundException fnf) {
			System.err.println("Couldnt load \"opt.app\"");
		}
	}
}
