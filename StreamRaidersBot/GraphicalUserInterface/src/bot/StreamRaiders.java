package bot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.WinLis;
import userInterface.WaitScreen;
import program.Configs;
import program.ConfigsV2;
import program.Debug;
import program.Options;
import program.Remaper;
import program.SRRHelper;
import program.Debug.DebugEventHandler;
import program.Debug.Scope;
import program.Debug.Type;
import program.SRRHelper.DataPathEventListener;
import run.BackEndHandler;

public class StreamRaiders {
	
	private static int error_count = 0;
	private static GUI err = null;
	public static final String pre = "StreamRaiders::";
	
	
	synchronized private static void log(String text) {
		final String errmsg;
		if_clause:
		if(Options.is("beta_frame")) {
			int ind1 = text.indexOf("err=");
			if(ind1 == -1) {
				errmsg = null;
				break if_clause;
			}
			int ind2 = text.indexOf(",", ind1);
			if(ind2 == -1)
				ind2 = text.length();
			errmsg = text.substring(ind1+4, ind2);
			
			if(ConfigsV2.getGStr(ConfigsV2.blocked_errors).contains(errmsg))
				return;
		} else
			errmsg = null;
		if(error_count == 0) {
			err = new GUI("Error occured", 400, 200, Options.is("beta_frame") ? userInterface.MainFrame.getGUI() : bot.MainFrame.getGUI(), null);
			err.addWinLis(new WinLis() {
				@Override
				public void onIconfied(WindowEvent e) {}
				@Override
				public void onFocusLost(WindowEvent e) {}
				@Override
				public void onFocusGained(WindowEvent e) {}
				@Override
				public void onDeIconfied(WindowEvent e) {}
				@Override
				public void onClose(WindowEvent e) {
					error_count = 0;
				}
			});
			err.addImage(new Image("data/Other/error.png"));
			Label l = new Label();
			l.setPos(1, error_count++);
			l.setText("<html>see logs.txt for more informations</html>");
			l.setInsets(2, 10, 2, 2);
			err.addLabel(l);
			log(text);
			err.refresh();
		} else {
			TextArea ta = new TextArea();
			ta.setPos(1, error_count);
			ta.setText(text);
			ta.setEditable(false);
			ta.setInsets(2, 10, 2, 2);
			err.addTextArea(ta);
			
			if(errmsg != null) {
				Button block = new Button();
				block.setPos(2, error_count);
				block.setText("block");
				block.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String blocked = ConfigsV2.getGStr(ConfigsV2.blocked_errors);
						if(blocked.contains(errmsg))
							return;
						if(!blocked.equals(""))
							blocked += "|";
						blocked += errmsg;
						ConfigsV2.setGStr(ConfigsV2.blocked_errors, blocked);
					}
				});
				err.addBut(block);
			}
			err.refresh();
			error_count++;
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
			Options.load();
		} catch (IOException | NullPointerException fnf) {
			System.out.println("Couldnt load options");
			return;
		}
		
		System.out.println("by ProjectBots https://github.com/ProjectBots/StreamRaiderBot\r\n"
				+ "Version: " + Options.get("botVersion") + "\r\n");
		
		/*	TODO make better / test if bug has been resolved over time
		if(!System.getProperty("java.version").startsWith("16")) {
			System.err.println("Incompatible java Version\nRestarting with new Version\n");
			
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<args.length; i++)
				sb.append(" " + args[i]);
			try {
				Runtime.getRuntime().exec("cmd.exe /c jdk-16\\bin\\java.exe -jar StreamRaidersBot.jar" + sb.toString());
			} catch (IOException e) {
				System.out.println("StreamRaiders -> main: err=failed to restart with jdk-16");
			}
			
			return;
		}
		*/
		Debug.setDebugEventHandler(new DebugEventHandler() {
			@Override
			public void onPrintLine(String pre, String msg, Scope scope, Type type, String pn, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((pn == null ? "" : "["+pn+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				DebugEventHandler.super.onPrintLine(pre, msg, scope, type, pn, slot, forced);
			}
			@Override
			public void onWriteLine(String path, String pre, String msg, Scope scope, Type type, String pn, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((pn == null ? "" : "["+pn+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Debug.getSevertyOf(type) > 1 && !msg.contains("couldnt set image"))
					System.err.println(pre+msg);
				DebugEventHandler.super.onWriteLine(path, pre, msg, scope, type, pn, slot, forced);
			}
			@Override
			public void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, String pn, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((pn == null ? "" : "["+pn+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				DebugEventHandler.super.onPrintException(pre, msg, e, scope, type, pn, slot, forced);
			}
			@Override
			public void onWriteException(String path, String pre, String msg, Exception e, Scope scope, Type type, String pn, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((pn == null ? "" : "["+pn+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Debug.getSevertyOf(type) > 1) {
					System.err.println(pre+msg);
					e.printStackTrace();
				}
				DebugEventHandler.super.onWriteException(path, pre, msg, e, scope, type, pn, slot, forced);
			}
		});
		
		
		Debug.print("started", Debug.general, Debug.info, null, null);
		
		WaitScreen.open("Initialize Bot");
		
		WaitScreen.setText("Initialize Remaper");
		Remaper.load();
		
		
		WaitScreen.setText("Initialize Browser");
		if(!Options.is("no_browser")) {
			//	TODO auto disable if it loads infinitely
			try {
				Browser.create();
			} catch (IOException | RuntimeException e) {
				Debug.printException("err=Couldnt initialize embeded Browser", e, Debug.runerr, Debug.error, null, null, true);
				WaitScreen.close();
				return;
			}
		} else {
			WaitScreen.setText("Browser disabled");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
		
		WaitScreen.setText("Initialize Config");
		try {
			if(Options.is("beta_frame")) 
				ConfigsV2.load();
			else
				Configs.load();
		} catch (IOException e) {
			Debug.printException("err=failed to load configs", e, Debug.runerr, Debug.error, null, null, true);
			if(GUI.showConfirmationBoxStatic("Loading Configs", "config file is corrupted\r\nreset?")) {
				try {
					if(Options.is("beta_frame")) 
						ConfigsV2.load(true);
					else
						Configs.load(true);
				} catch (IOException e1) {
					Debug.printException("err=failed to reset config", e, Debug.runerr, Debug.error, null, null, true);
				}
			} else {
				return;
			}	
		}
		
		
		WaitScreen.setText("Initialize MainFrame"); 
		if(Options.is("beta_frame")) {
			BackEndHandler.setDataPathEventListener(new run.BackEndHandler.DataPathEventListener() {
			@Override
			public void onUpdate(String dataPath, String serverTime, JsonObject data) {
				run.BackEndHandler.DataPathEventListener.super.onUpdate(dataPath, serverTime, data);
				GuideContent.saveChestRewards(data);
				GuideContent.gainStats(data.getAsJsonObject("Units"));
			}
			});
			userInterface.MainFrame.open(true);
		} else {
			startMemReleaser();
			
			SRRHelper.setDataPathEventListener(new DataPathEventListener() {
				@Override
				public void onUpdate(String dataPath, JsonObject data) {
					DataPathEventListener.super.onUpdate(dataPath, data);
					GuideContent.saveChestRewards(data);
					GuideContent.gainStats(data.getAsJsonObject("Units"));
				}
			});
			bot.MainFrame.open();
			WaitScreen.close();
		}
	}
	
	
	private static Thread t;
	private static boolean memRelRunning = false;
	
	public static void startMemReleaser() {
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				memRelRunning = true;
				while(memRelRunning) {
					long mem = Runtime.getRuntime().totalMemory();
					
					if(mem > 300000000)
						System.gc();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
				}
			}
		});
		t.start();
	}
	
	public static void stopMemReleaser() {
		memRelRunning = false;
	}
	
	public static boolean isMemRelRunning() {
		return memRelRunning;
	}
	
	
}
