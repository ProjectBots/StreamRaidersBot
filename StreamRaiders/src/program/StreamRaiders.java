package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Hashtable;

import include.GUI;
import include.GUI.Image;
import include.GUI.Label;
import include.Http;
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
		
		
		Http get = new Http();
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
		get.setUrl("https://github.com/ProjectBots/StreamRaidersBot/blob/master/StreamRaiders/data/opt.app");
		
		try {
			StringBuilder ret = new StringBuilder(get.sendGet());
			
			int index = ret.indexOf("botVersion=");
			
			String ver = ret.substring(index+11, ret.indexOf("</td>", index));
			
			if(!get("botVersion").equals(ver)) {
				System.err.println("This Bot Version is outdated.\n"
						+ "Not problematic, but consider updating.\n"
						+ "Newest Version: " + ver);
				
				GUI verg = new GUI("Outdated Bot Version", 500, 250);
				Label l = new Label();
				l.setText("<html>This Bot Version is outdated.<br>Not problematic, but consider updating.<br>Newest Version: " + ver + "</html>");
				verg.addLabel(l);
				verg.refresh();
			}
		} catch (URISyntaxException | IOException e2) {
			log("StreamRaiders -> main: err=Couldnt get current version", e2);
		}
		
		
		
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
