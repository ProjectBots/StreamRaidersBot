package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

import include.GUI;
import include.GUI.Image;
import include.GUI.Label;
import include.NEF;

public class StreamRaiders {
	
	private static Hashtable<String, String> opt = null;
	
	public static String get(String key) {
		return opt.get(key);
	}
	
	synchronized public static void set(String key, String value) {
		opt.put(key, value);
	}
	
	synchronized public static void save() {
		try {
			NEF.saveOpt("data/opt.app", opt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String text, Exception e) {
		log(text, e, false);
	}
	
	synchronized public static void log(String text, Exception e, boolean silent) {
		try {
			String out = "";
			if(e != null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				
				if(text != null) {
					out = text;
				}
				
				if(!silent) out += "\n" + sw.toString();
			} else {
				if(text == null) {
					out = "critical error happend";
				} else {
					out = text;
				}
			}
			GUI err = new GUI("Error occured", 400, 200, MainFrame.getGUI(), null);
			Image img = new Image("data/Other/error.png");
			err.addImage(img);
			Label l = new Label();
			l.setPos(1, 0);
			l.setText("<html>" + out.replace("\n", "<br>") + "</html>");
			l.setInsets(2, 10, 2, 2);
			err.addLabel(l);
			err.refresh();
			System.err.println(out);
			NEF.log("logs.app", out);
		} catch (IOException e1) {
			e1.printStackTrace();
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
		} catch (IOException fnf) {
			log("Couldnt load \"opt.app\"", fnf);
			return;
		}
		
		System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaiderBot\r\n"
				+ "Version: " + get("botVersion") + "\r\n");
		

		if(args.length != 0) {
			String[] argss = args[0].split("=");
			if(argss.length == 2 && argss[0].equals("-debug")) {
				args[0] = argss[0];
				try {
					NEF.save(argss[1], "");
				} catch (IOException e) {
					log("StreamRaiders -> main: err=failed to create debug file, path=" + argss[1], e);
				}
				Debug.setOutputFile(argss[1]);
			}
			if(args[0].toLowerCase().equals("-debug")) 
				if(args.length == 1) 
					Debug.addScope("general");
				else 
					for(int i=1; i<args.length; i++)
						Debug.addScope(args[i]);
			
		}
		
		
		Debug.print("started", Debug.general);
		
		Raid.loadTypViewChestRews();
		try {
			Browser.create();
		} catch (IOException | RuntimeException e) {
			log("Couldnt initialize embeded Browser", e);
			return;
		}
		try {
			Configs.load();
		} catch (IOException e) {
			log("load configs", e);
			if(GUI.showConfirmationBoxStatic("Loading Configs", "config file is corrupted\r\nreset?")) {
				try {
					Configs.load(true);
				} catch (IOException e1) {
					log("failed", e1);
				}
			} else {
				return;
			}	
		}
		MainFrame.open();
	}
}
