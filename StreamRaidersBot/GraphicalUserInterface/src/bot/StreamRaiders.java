package bot;

import java.awt.Color;
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
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import userInterface.MainFrame;
import userInterface.WaitScreen;
import program.ConfigsV2;
import program.Debug;
import program.Options;
import program.Raid;
import program.Debug.DebugEventHandler;
import program.Debug.Scope;
import program.Debug.Type;
import run.Manager;
import run.BotListener;
import run.Manager.IniCanceledException;

public class StreamRaiders {
	
	private static int error_count = 0;
	private static GUI err = null;
	public static final String pre = "StreamRaiders::";
	
	
	synchronized private static void log(String text) {
		final String errmsg;
		checkBlocked: {
			int ind1 = text.indexOf("err=");
			if(ind1 == -1) {
				errmsg = null;
				break checkBlocked;
			}
			int ind2 = text.indexOf(",", ind1);
			if(ind2 == -1)
				ind2 = text.length();
			errmsg = text.substring(ind1+4, ind2);
			
			if(ConfigsV2.getGStr(ConfigsV2.blocked_errors).contains(errmsg))
				return;
		} 
		if(error_count == 0) {
			err = new GUI("Error occured", 400, 200, MainFrame.getGUI(), null);
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
			public void onPrintLine(String pre, String msg, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((cid == null ? "" : "["+ConfigsV2.getPStr(cid, ConfigsV2.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				DebugEventHandler.super.onPrintLine(pre, msg, scope, type, cid, slot, forced);
			}
			@Override
			public void onWriteLine(String path, String pre, String msg, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((cid == null ? "" : "["+ConfigsV2.getPStr(cid, ConfigsV2.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Debug.getSevertyOf(type) > 1 && !msg.contains("couldnt set image"))
					System.err.println(pre+msg);
				DebugEventHandler.super.onWriteLine(path, pre, msg, scope, type, cid, slot, forced);
			}
			@Override
			public void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((cid == null ? "" : "["+ConfigsV2.getPStr(cid, ConfigsV2.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				DebugEventHandler.super.onPrintException(pre, msg, e, scope, type, cid, slot, forced);
			}
			@Override
			public void onWriteException(String path, String pre, String msg, Exception e, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Debug.runerr))
					log((cid == null ? "" : "["+ConfigsV2.getPStr(cid, ConfigsV2.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Debug.getSevertyOf(type) > 1) {
					System.err.println(pre+msg);
					e.printStackTrace();
				}
				DebugEventHandler.super.onWriteException(path, pre, msg, e, scope, type, cid, slot, forced);
			}
		});
		
		
		Debug.print("started", Debug.general, Debug.info, null, null);
		
		WaitScreen.setText("Initialize Bot"); 
		try {
			Manager.ini(new BotListener() {
				@Override
				public boolean configNotReadable() {
					return GUI.showConfirmationBoxStatic("Loading Configs", "config file is corrupted\r\nreset?");
				}
				@Override
				public void onDataPathUpdate(String dataPath, String serverTime, JsonObject data) {
					GuideContent.saveChestRewards(data);
					GuideContent.gainStats(data.getAsJsonObject("Units"));
				}
				@Override
				public void onConfigLoadStatusUpdate(int loaded, int failed, int total) {
					MainFrame.updateLoadStatus(loaded, failed, total);
				}
				@Override
				public void onProfileLoadComplete(String cid, int pos) {
					MainFrame.addLoadedProfile(cid, pos);
				}
				@Override
				public void onProfileLoadError(String cid, int pos, Exception e) {
					MainFrame.addFailedProfile(cid, pos, e);
				}
				@Override
				public void onProfileUnloaded(String cid) {
					MainFrame.remProfile(cid);
				}
				@Override
				public void onProfileChangedRunning(String cid, int slot, boolean run) {
					MainFrame.updateSlotRunning(cid, slot, run);
				}
				@Override
				public void onProfileTimerUpdate(String cid, int slot, String time) {
					MainFrame.updateTimer(cid, slot, time);
				}
				@Override
				public void onProfileUpdateCurrency(String cid, String type, int amount) {
					MainFrame.updateCurrency(cid, type, amount);
				}
				@Override
				public void onProfileUpdateGeneral(String cid, String pn, String ln, Color lc) {
					MainFrame.updateGeneral(cid, pn, ln, lc);
				}
				@Override
				public void onProfileUpdateSlot(String cid, int slot, Raid raid, boolean locked, boolean change) {
					MainFrame.updateSlot(cid, slot, raid, change);
				}
				@Override
				public void onProfileUpdateSlotSync(String cid, int slot, int slotSyncedTo) {
					MainFrame.updateSlotSync(cid, slot, slotSyncedTo != -1);
				}
			});
		} catch (IniCanceledException e1) {
			//	exit bot
			return;
		}
		
		if(!Options.is("no_browser")) {
			WaitScreen.setText("Initialize Browser");
			try {
				Browser.create();
			} catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
				Debug.printException("err=Couldnt initialize embeded Browser", e, Debug.runerr, Debug.error, null, null, true);
				WaitScreen.close();
				return;
			}
		}
		
		WaitScreen.setText("Initialize MainFrame"); 
		MainFrame.open();
		Manager.loadAllNewProfiles();
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
