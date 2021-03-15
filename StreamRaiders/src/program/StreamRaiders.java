package program;

import java.io.FileNotFoundException;
import java.util.Hashtable;

public class StreamRaiders {
	
	private static Hashtable<String, String> opt = null;
	
	public static String get(String key) {
		return opt.get(key);
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
