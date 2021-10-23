package program;

import java.awt.event.WindowEvent;
import java.io.IOException;

import com.google.gson.JsonObject;

import include.ArgSplitter;
import include.ArgSplitter.Arg;
import include.GUI;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.WinLis;
import userInterface.WaitScreen;
import include.NEF;
import program.Debug.DebugEventHandler;
import program.Debug.Scope;
import program.Debug.Type;
import program.SRRHelper.DataPathEventListener;
import run.BackEndHandler;

public class StreamRaiders {
	
	
	private static int error_count = 0;
	private static GUI err = null;
	
	synchronized private static void log(String text, Exception e) {
		if(error_count == 0) {
			err = new GUI("Error occured", 400, 200, isBetaFrame() ? userInterface.MainFrame.getGUI() : program.MainFrame.getGUI(), null);
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
			TextArea ta = new TextArea();
			ta.setPos(1, error_count++);
			ta.setText(text);
			ta.setInsets(2, 10, 2, 2);
			err.addTextArea(ta);
			err.refresh();
		} else {
			TextArea ta = new TextArea();
			ta.setPos(1, error_count++);
			ta.setText(text);
			ta.setInsets(2, 10, 2, 2);
			err.addTextArea(ta);
			err.refresh();
		}
	}
	
	private static boolean betaFrame = false;
	
	public static boolean isBetaFrame() {
		return betaFrame;
	}
	
	private static boolean browserEnabled = true;
	
	public static boolean isBrowserEnabled() {
		return browserEnabled;
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
			System.out.println("Couldnt load \"opt.txt\"");
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
			public void onPrintLine(String pre, String msg, Scope scope, Type type, boolean forced) {
				if(scope.equals(Debug.runerr))
					log(msg, null);
				DebugEventHandler.super.onPrintLine(pre, msg, scope, type, forced);
			}
			@Override
			public void onWriteLine(String path, String pre, String msg, Scope scope, Type type, boolean forced) {
				if(scope.equals(Debug.runerr))
					log(msg, null);
				if(Debug.getSevertyOf(type) > 1 && !msg.contains("couldnt set image"))
					System.err.println(pre+msg);
				DebugEventHandler.super.onWriteLine(path, pre, msg, scope, type, forced);
			}
			@Override
			public void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
				if(scope.equals(Debug.runerr))
					log(msg, e);
				DebugEventHandler.super.onPrintException(pre, msg, e, scope, type, forced);
			}
			@Override
			public void onWriteException(String path, String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
				if(scope.equals(Debug.runerr))
					log(msg, e);
				if(Debug.getSevertyOf(type) > 1) {
					System.err.println(pre+msg);
					e.printStackTrace();
				}
				DebugEventHandler.super.onWriteException(path, pre, msg, e, scope, type, forced);
			}
		});
		
		ArgSplitter as = new ArgSplitter();
		as.registerArg("debug", new Arg() {
			@Override
			public void run(String[] args) {
				int i = 0;
				if(args[0].startsWith("=")) {
					String s = args[0].substring(1);
					Debug.setOutputDirectory(s);
					i++;
				}
				
				if(i == args.length) 
					Debug.addScope("general");
				else 
					for(; i<args.length; i++)
						Debug.addScope(args[i]);
			}
		});
		as.registerArg("beta_frame", new Arg() {
			@Override
			public void run(String[] args) {
				betaFrame = true;
			}
		});
		as.registerArg("no_browser", new Arg() {
			@Override
			public void run(String[] args) {
				browserEnabled = false;
			}
		});
		
		try {
			as.check(NEF.read("data/args.txt").split("(\n| )"));
		} catch (IOException e2) {
			System.out.println("StreamRaiders -> main: err=cant read args.txt");
		}
		
		

		Debug.print("started", Debug.general, Debug.info);
		
		WaitScreen.open("Initialize Bot");
		
		WaitScreen.setText("Initialize Remaper");
		Remaper.load();
		
		WaitScreen.setText("Initialize Browser");
		if(browserEnabled) {
			try {
				Browser.create();
			} catch (IOException | RuntimeException e) {
				Debug.printException("Couldnt initialize embeded Browser", e, Debug.runerr, Debug.error, true);
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
			if(isBetaFrame()) 
				ConfigsV2.load();
			else
				Configs.load();
		} catch (IOException e) {
			Debug.printException("load configs", e, Debug.runerr, Debug.error, true);
			if(GUI.showConfirmationBoxStatic("Loading Configs", "config file is corrupted\r\nreset?")) {
				try {
					if(isBetaFrame()) 
						ConfigsV2.load(true);
					else
						Configs.load(true);
				} catch (IOException e1) {
					Debug.printException("failed", e, Debug.runerr, Debug.error, true);
				}
			} else {
				return;
			}	
		}
		
		
		WaitScreen.setText("Initialize MainFrame"); 
		if(isBetaFrame()) {
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
			MainFrame.open();
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
