package bot;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.WinLis;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Resources;
import otherlib.Resources.Resource;
import otherlib.Resources.ResourceReader;
import otherlib.Resources.ResourceTemplate;
import otherlib.Logger.LoggerEventHandler;
import otherlib.Logger.Scope;
import otherlib.Logger.Type;
import userInterface.MainFrame;
import userInterface.WaitScreen;
import userInterface.globaloptions.RedeemCodes;
import run.Manager;
import run.BotListener;
import run.Manager.IniCanceledException;
import srlib.viewer.Raid;
import run.ProfileType;

public class StreamRaiders {
	
	private static boolean configLoaded = false;
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
			
			if(configLoaded && Configs.getGStr(Configs.blocked_errors).contains(errmsg))
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
			err.addImage(new Image((java.awt.Image) Resources.get("Other/error.png")));
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
						String blocked = Configs.getGStr(Configs.blocked_errors);
						if(blocked.contains(errmsg))
							return;
						if(!blocked.equals(""))
							blocked += "|";
						blocked += errmsg;
						Configs.setGStr(Configs.blocked_errors, blocked);
					}
				});
				err.addBut(block);
			}
			err.refresh();
			error_count++;
		}
	}
	
	public static void main(String[] args) {
		
		Logger.setDebugEventHandler(new LoggerEventHandler() {
			@Override
			public void onPrintLine(String pre, String msg, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Logger.runerr))
					log((cid == null ? "" : "["+Configs.getPStr(cid, Configs.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				LoggerEventHandler.super.onPrintLine(pre, msg, scope, type, cid, slot, forced);
			}
			@Override
			public void onWriteLine(String path, String pre, String msg, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Logger.runerr))
					log((cid == null ? "" : "["+Configs.getPStr(cid, Configs.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Logger.getSevertyOf(type) > 1 && !msg.contains("couldnt set image"))
					System.err.println(pre+msg);
				LoggerEventHandler.super.onWriteLine(path, pre, msg, scope, type, cid, slot, forced);
			}
			@Override
			public void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Logger.runerr))
					log((cid == null ? "" : "["+Configs.getPStr(cid, Configs.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				LoggerEventHandler.super.onPrintException(pre, msg, e, scope, type, cid, slot, forced);
			}
			@Override
			public void onWriteException(String path, String pre, String msg, Exception e, Scope scope, Type type, String cid, Integer slot, boolean forced) {
				if(scope.equals(Logger.runerr))
					log((cid == null ? "" : "["+Configs.getPStr(cid, Configs.pname)+"] ") + (slot == null ? "" : "["+slot+"] ") + msg);
				if(Logger.getSevertyOf(type) > 1) {
					System.err.println(pre+msg);
					e.printStackTrace();
				}
				LoggerEventHandler.super.onWriteException(path, pre, msg, e, scope, type, cid, slot, forced);
			}
		});

		Logger.print("started", Logger.general, Logger.info, null, null);
		
		Resources.addCategory(java.awt.Image.class, new ResourceReader<java.awt.Image>() {
			@Override
			public ResourceTemplate<java.awt.Image> read(String folder, String path) {
				
				if(path.startsWith("http")) {
					try {
						return new ResourceTemplate<java.awt.Image>(ImageIO.read(new URL(path)), 5*60*1000);
					} catch (IOException e) {
						throw new RuntimeException("failed to get image from web", e);
					}
				}
				
				File file = new File(folder+path);
				if(file.exists()) {
					try {
						return new ResourceTemplate<java.awt.Image>(ImageIO.read(file), -1);
					} catch (IOException e) {
						throw new RuntimeException("failed to get image from drive", e);
					}
				}
				
				if(path.startsWith("UnitPics/"))
					return new ResourceTemplate<java.awt.Image>(Resources.get("UnitPics/unknow.png"), -1);
				
				if(path.startsWith("SoulPics/"))
					return new ResourceTemplate<java.awt.Image>(Resources.get("SoulPics/graysoul.png"), -1);
				
				if(path.startsWith("ChestPics/"))
					return new ResourceTemplate<java.awt.Image>(Resources.get("ChestPics/nochest.png"), -1);
				
				throw new RuntimeException("404 Image not found");
			}

			@Override
			public void save(String folder, Resource<java.awt.Image> resource) {
				throw new UnsupportedOperationException();
			}
		}, "png");
		
		WaitScreen.setText("Initialize Bot"); 
		try {
			Manager.ini(new BotListener() {
				@Override
				public boolean configNotReadable() {
					return WaitScreen.getGUI().showConfirmationBox("config file is corrupted\r\nreset?");
				}
				@Override
				public void onSRDataUpdate(String dataPathUrl, JsonObject data) {
					GuideContent.saveChestRewards(data);
					GuideContent.gainStats(data);
				}
				@Override
				public void onConfigLoadStatusUpdate(int loaded, int failed, int total) {
					MainFrame.updateLoadStatus(loaded, failed, total);
				}
				@Override
				public void onProfileLoadComplete(String cid, int pos, ProfileType pt) {
					MainFrame.addLoadedProfile(cid, pos, pt);
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
				public void onProfileSwitchedAccountType(String cid, ProfileType pt) {
					MainFrame.profileSwitchedAccountType(cid, pt);
				}
				@Override
				public void onProfileChangedRunning(String cid, ProfileType pt, int slot, boolean run) {
					MainFrame.updateSlotRunning(cid, slot, run);
				}
				@Override
				public void onProfileTimerUpdate(String cid, ProfileType pt, int slot, String time) {
					MainFrame.updateTimer(cid, slot, time);
				}
				@Override
				public void onProfileUpdateCurrency(String cid, ProfileType pt, String type, int amount) {
					MainFrame.updateCurrency(cid, type, amount);
				}
				@Override
				public void onProfileUpdateGeneral(String cid, ProfileType pt, String pn, String ln, Color lc) {
					MainFrame.updateGeneral(cid, pn, ln, lc);
				}
				@Override
				public void onProfileUpdateSlotViewer(String cid, int slot, Raid raid, boolean locked, boolean change) {
					MainFrame.updateSlot(cid, slot, raid, change);
				}
				@Override
				public void onProfileUpdateSlotSync(String cid, ProfileType pt, int slot, int slotSyncedTo) {
					MainFrame.updateSlotSync(cid, slot, slotSyncedTo != -1);
				}
				@Override
				public void redeemCodesFinished() {
					RedeemCodes.finished();
				}
			});
		} catch (IniCanceledException e1) {
			//	Manager failed to initialize => stopping bot
			System.exit(0);
			return;
		}
		configLoaded = true;
		
		
		if(!Options.is("no_browser")) {
			WaitScreen.setText("Initialize Browser");
			try {
				Browser.create();
			} catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
				Logger.printException("err=Couldnt initialize embeded Browser", e, Logger.runerr, Logger.error, null, null, true);
				WaitScreen.close();
				System.exit(-5);
				return;
			}
		}
		
		WaitScreen.setText("Initialize MainFrame"); 
		MainFrame.open();
		
		if(!Options.is("block_profile_auto_load"))
			Manager.loadAllNewProfiles();
		else
			MainFrame.updateLoadStatus(0, 0, 0);
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
