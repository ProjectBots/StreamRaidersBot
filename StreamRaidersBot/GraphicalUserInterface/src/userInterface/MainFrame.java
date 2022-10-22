package userInterface;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import javax.swing.WindowConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import bot.Browser;
import include.GUI;
import include.Json;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Menu;
import include.GUI.TrayMenu;
import include.GUI.WinLis;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Remaper;
import otherlib.Ressources;
import include.Guide;
import include.Http;
import run.AbstractProfile;
import run.Manager;
import run.ProfileType;
import run.viewer.Viewer;
import srlib.RaidType;
import srlib.SRC;
import srlib.Time;
import srlib.viewer.Raid;
import userInterface.captain.CaptainProfileSection;
import userInterface.globaloptions.GlobalOptions;
import userInterface.viewer.ViewerProfileSection;

public class MainFrame {
	
	public static final String pre = "MainFrame::";
	public static final String pspre = "ProfileSection::";
	
	private static GUI gui = null;
	private static boolean recieveMainFrameUpdates = false;
	
	public static GUI getGUI() {
		return gui;
	}
	
	
	
	public static void open() {
		
		Colors.ini();
		
		GUI.setDefIcon("data/Other/icon.png");
		
		String bver = Options.get("botVersion");
		
		gui = new GUI("StreamRaider Bot v" + bver, 1000, 700);
		gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		gui.removeDefaultCloseListener();
		
		gui.setBackgroundGradient(Colors.getGradient("main background"));
		
		gui.addWinLis(new WinLis() {
			@Override
			public void onIconfied(WindowEvent e) {}
			@Override
			public void onFocusLost(WindowEvent e) {}
			@Override
			public void onFocusGained(WindowEvent e) {
				Manager.updateAllProfiles();
			}
			@Override
			public void onDeIconfied(WindowEvent e) {}
			@Override
			public void onClose(WindowEvent e) {
				if(Configs.getGBoo(Configs.needCloseConfirm) && !gui.showConfirmationBox("Exit?"))
					return;
				close();
			}
		});
		
		gui.setGlobalKeyLis(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_A:
						new ActionsDelayed().open(gui, true);
						break;
					case KeyEvent.VK_I:
						new ActionsDelayed().open(gui, false);
						break;
					case KeyEvent.VK_R:
						Colors.ini();
						close(false);
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_P:
						MapGUI.showPlanTypes(gui);
						break;
					case KeyEvent.VK_A:
						Manager.doAll(SRC.Manager.start, 0);
						break;
					case KeyEvent.VK_O:
						Manager.doAll(SRC.Manager.stop, 0);
						break;
					case KeyEvent.VK_I:
						Manager.doAll(SRC.Manager.skip, 0);
						break;
					case KeyEvent.VK_T:
						String st = Time.format(Time.getServerTime());
						gui.msg("ServerTime", st, GUI.MsgConst.INFO);
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					
					}
				}
			}
		});
		
		TrayMenu tray = new TrayMenu("Stream Raiders Bot", "data/Other/icon.png");
		tray.addItem("show", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.show();
			}
		});
		tray.addItem("exit", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		gui.setMenuBarGradient(Colors.getGradient("main menubar"));

		int m = 0;
		Menu bot = new Menu("Bot", "Hide Window  Add a Profile  start all  start all delayed  stop all  skip time all  skip time all delayed".split("  "));
		bot.setFont(new Font(null, Font.BOLD, 25));
		bot.setForeground(Colors.getColor("main menubar"));
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!gui.hide(tray))
					Logger.print("MainFrame -> open: err=Couldn't add to tray", Logger.general, Logger.error, null, null, true);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddProfile.open(gui, null);
			}
		});
		bot.setSep(m);
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Manager.doAll(SRC.Manager.start, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ActionsDelayed().open(gui, true);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Manager.doAll(SRC.Manager.stop, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Manager.doAll(SRC.Manager.skip, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ActionsDelayed().open(gui, false);
			}
		});
		gui.addMenu(bot);
		
		
		Menu sep = new Menu(" ", new String[0]);
		sep.setFont(new Font(null, Font.BOLD, 25));
		gui.addMenu(sep);
		
		m = 0;
		Menu config = new Menu("Config", "export  import  Settings".split("  "));
		config.setFont(new Font(null, Font.BOLD, 25));
		config.setForeground(Colors.getColor("main menubar"));
		config.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigsExport.open(gui);
			}
		});
		config.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigsImport.open(gui);
			}
		});
		config.setSep(m);
		config.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 new GlobalOptions().open();
			}
		});
		gui.addMenu(config);
		
		gui.addMenu(sep);
		
		m = 0;
		Menu help = new Menu("Help", "Guide  About  Donators".split("  "));
		help.setFont(new Font(null, Font.BOLD, 25));
		help.setForeground(Colors.getColor("main menubar"));
		help.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Guide("data\\Guide").show(gui, "Home");
				} catch (Exception e1) {
					Logger.printException("MainFrame -> openGuide: err=sth went wrong", e1, Logger.runerr, Logger.error, null, null, true);
				}
			}
		});
		help.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				About.open(gui);
			}
		});
		help.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Donators.open(gui);
			}
		});
		gui.addMenu(help);
	}
	
	public static void updateLoadStatus(int loaded, int failed, int total) {
		WaitScreen.setText("<html><center>Loading Profiles</center><br><center>Ready: " + (loaded+failed) + "/" + total + "</center><br><center>Failed: " + failed + "</center></html>");
		
		if(loaded+failed == total)
			frameReady();
	}
	
	private static boolean verWarnShown = false;
	
	private static void frameReady() {
		WaitScreen.setText("Refreshing Frame");
		recieveMainFrameUpdates = true;
		gui.refresh();
		
		if(!verWarnShown) {
			verWarnShown = true;
			String bver = Options.get("botVersion");
			if(bver.contains("beta")) {
				GUI beta = new GUI("Beta warn", 400, 200, gui, null);
				Label l = new Label();
				l.setText("This version is a beta version!");
				beta.addLabel(l);
				beta.refresh();
			} else if(bver.contains("debug")) {
				GUI debug = new GUI("Debug warn", 400, 200, gui, null);
				Label l = new Label();
				l.setText("This version is a debug version!");
				debug.addLabel(l);
				debug.refresh();
			}
		}
		
		WaitScreen.close();
	}
	
	synchronized public static void addLoadedProfile(String cid, int pos, ProfileType type) {
		Container c = null;
		switch(type) {
		case VIEWER:
			c = new ViewerProfileSection(cid).create();
			break;
		case CAPTAIN:
			c = new CaptainProfileSection(cid).create();
			break;
		}
		c.setPos(0, pos);
		gui.addContainer(c, pre+cid+"::profile");
	}
	
	public static void addFailedProfile(String cid, int pos, Exception e) {
		if(e != null)
			Logger.printException("Profile failed to load: err=" + e.getClass().getSimpleName(), e, Logger.runerr, Logger.error, cid, null, true);
		createFailedContainer(cid, pos);
	}
	
	private static void createFailedContainer(String cid, int pos) {
		if(!recieveMainFrameUpdates)
			return;
		Container c = new Container();
		c.setPos(0, pos);
		c.setBorder(Color.gray, 2, 25);
		c.setInsets(5, 2, 5, 2);
		
			Label name = new Label();
			name.setPos(0, 0);
			name.setText(Configs.getPStr(cid, Configs.pname));
			name.setForeground(Colors.getColor("main labels"));
			c.addLabel(name);
		
			Button retry = new Button();
			retry.setPos(1, 0);
			retry.setText("\u27F2 retry");
			retry.setForeground(Colors.getColor("main buttons def"));
			retry.setGradient(Colors.getGradient("main buttons def"));
			retry.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gui.remove(pre+cid+"::profile");
					Manager.retryLoadProfile(cid);
				}
			});
			c.addBut(retry);
			
			Button reAdd = new Button();
			reAdd.setPos(2, 0);
			reAdd.setText("update cookies");
			reAdd.setForeground(Colors.getColor("main buttons def"));
			reAdd.setGradient(Colors.getGradient("main buttons def"));
			reAdd.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gui.remove(pre+cid+"::profile");
					AddProfile.open(gui, cid);
				}
			});
			c.addBut(reAdd);
			
			Button rem = new Button();
			rem.setPos(3, 0);
			rem.setText("X");
			rem.setForeground(Colors.getColor("main buttons def"));
			rem.setGradient(Colors.getGradient("main buttons def"));
			rem.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Manager.remProfile(cid);
					gui.remove(pre+cid+"::profile");
				}
			});
			c.addBut(rem);
			
			
			
		gui.addContainer(c, pre+cid+"::profile");
	}
	
	public static void remProfile(String cid) {
		if(!recieveMainFrameUpdates)
			return;
		gui.remove(pre+cid+"::profile");
		gui.refresh();
	}
	
	public static void profileSwitchedAccountType(String cid, ProfileType type) {
		if(!recieveMainFrameUpdates)
			return;
		remProfile(cid);
		addLoadedProfile(cid, Manager.getProfilePos(cid), type);
		gui.refresh();
		Manager.updateProfile(cid);
	}
	
	public static void updateSlotRunning(String cid, int slot, boolean run) {
		if(!recieveMainFrameUpdates)
			return;
		GUI.setGradient(pspre+cid+"::"+slot+"::run", Colors.getGradient("main buttons " + (run?"on":"def")));
		GUI.setForeground(pspre+cid+"::"+slot+"::run", Colors.getColor("main buttons " + (run?"on":"def")));
	}
	
	public static void updateTimer(String cid, int slot, String time) {
		if(!recieveMainFrameUpdates)
			return;
		GUI.setText(pspre+cid+"::"+slot+"::time", time);
	}
	
	public static void updateSlot(String cid, int slot, Raid raid, boolean change) {
		if(!recieveMainFrameUpdates)
			return;
		
		GUI.setForeground(pspre+cid+"::"+slot+"::change", Colors.getColor("main buttons " + (change ? "on" : "def")));
		GUI.setGradient(pspre+cid+"::"+slot+"::change", Colors.getGradient("main buttons " + (change ? "on" : "def")));
		
		Boolean locked = Configs.isSlotLocked(cid, "(all)", ""+slot);
		String fid = "main buttons " +(locked == null
										? "cat"
										: locked
											? "on"
											: "def");
		GUI.setGradient(pspre+cid+"::"+slot+"::lock", Colors.getGradient(fid));
		GUI.setForeground(pspre+cid+"::"+slot+"::lock", Colors.getColor(fid));

		if(raid == null) {
			GUI.setText(pspre+cid+"::"+slot+"::capname", "????????");
			try {
				setImage("Other/icon", 100, pspre+cid+"::"+slot+"::img");
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: raid=null, err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
			GUI.setText(pspre+cid+"::"+slot+"::wins", "??");
			try {
				setImage("LoyaltyPics/noloy", 20, pspre+cid+"::"+slot+"::loy");
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: raid=null, err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
			GUI.setGradient(pspre+cid+"::"+slot+"::block", Colors.getGradient("main buttons def"));
			GUI.setForeground(pspre+cid+"::"+slot+"::block", Colors.getColor("main buttons def"));
			GUI.setGradient(pspre+cid+"::"+slot+"::fav", Colors.getGradient("main buttons def"));
			GUI.setForeground(pspre+cid+"::"+slot+"::fav", Colors.getColor("main buttons def"));
			try {
				setImage("ChestPics/nochest", 25, pspre+cid+"::"+slot+"::chest");
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: raid=null, err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
		} else {
			GUI.setText(pspre+cid+"::"+slot+"::capname", raid.twitchDisplayName);
			
			try {
				setImage(raid.twitchUserImage, 100, pspre+cid+"::"+slot+"::img");
			} catch (IOException | RuntimeException e) {
				Logger.print("MainFrame -> onUpdateSlot: err=couldnt set profile image, tdn="+raid.twitchDisplayName+", url="+raid.twitchUserImage, Logger.general, Logger.warn, cid, slot, true);

				//	extracting the image without streamraiders help
				String link = extractTwitchImage(raid.twitchUserName);
				for(int i=0; link == null && i<5; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {}
					link = extractTwitchImage(raid.twitchUserName);
				}
				
				try {
					setImage(link, 100, pspre+cid+"::"+slot+"::img");
				} catch (IOException | RuntimeException e1) {
					Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set profile image 2, tdn="+raid.twitchDisplayName+", url1="+raid.twitchUserImage+", url2="+link, e1, Logger.general, Logger.warn, cid, slot, true);
					try {
						setImage("Other/icon", 100, pspre+cid+"::"+slot+"::img");
					} catch (IOException e2) {
						Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set default profile image", e2, Logger.general, Logger.error, cid, slot, true);
					}
				}
			}
			switch(raid.type) {
			case CAMPAIGN:
				GUI.setText(pspre+cid+"::"+slot+"::wins", ""+raid.pveWins);
				try {
					setImage("LoyaltyPics/" + Viewer.pveloy[raid.pveLoyaltyLevel], 20, pspre+cid+"::"+slot+"::loy");
				} catch (IOException e) {
					Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set loy image", e, Logger.general, Logger.error, cid, slot, true);
				}
				break;
			case DUNGEON:
				int keys = raid.dungeonStreak + 2;
				if(keys > 10)
					keys = 10;
				GUI.setText(pspre+cid+"::"+slot+"::wins", ""+keys);
				try {
					setImage("CurrencyPics/key", 20, pspre+cid+"::"+slot+"::loy");
				} catch (IOException e) {
					Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set key image", e, Logger.general, Logger.error, cid, slot, true);
				}
				break;
			case VERSUS:
				//	TODO
				GUI.setText(pspre+cid+"::"+slot+"::wins", "¯\\_(ツ)_/¯");
				try {
					setImage("CurrencyPics/bone", 20, pspre+cid+"::"+slot+"::loy");
				} catch (IOException e) {
					Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set bone image", e, Logger.general, Logger.error, cid, slot, true);
				}
				break;
			}
			
			Integer val = Configs.getCapInt(cid, "(all)", raid.twitchUserName, raid.type == RaidType.DUNGEON ? Configs.dungeon : Configs.campaign, Configs.fav);
			String favPath;
			String blockPath;
			if(val == null) {
				favPath = "main buttons def";
				blockPath = "main buttons def";
			} else if(val == Integer.MAX_VALUE-1) {
				favPath = "main buttons fav_cat";
				blockPath = "main buttons def";
			} else if(val == Integer.MIN_VALUE+1) {
				favPath = "main buttons def";
				blockPath = "main buttons cat";
			} else if(val == 0) {
				favPath = "main buttons fav_cat";
				blockPath = "main buttons cat";
			} else if(val > 0) {
				favPath = "main buttons fav_on";
				blockPath = "main buttons def";
			} else {
				favPath = "main buttons def";
				blockPath = "main buttons on";
			}
			GUI.setGradient(pspre+cid+"::"+slot+"::fav", Colors.getGradient(favPath));
			GUI.setForeground(pspre+cid+"::"+slot+"::fav", Colors.getColor(favPath));
			GUI.setGradient(pspre+cid+"::"+slot+"::block", Colors.getGradient(blockPath));
			GUI.setForeground(pspre+cid+"::"+slot+"::block", Colors.getColor(blockPath));
			JsonArray cts = Json.parseArr(Options.get("chests"));
			cts.add("bonechest");
			cts.add("dungeonchest");
			cts.add("nochest");
			String ct = Remaper.map(raid.chestType);
			if(ct == null || !cts.contains(new JsonPrimitive(ct))) {
				Logger.print("MainFrame -> updateSlot: err=couldnt set chest img, ct="+ct, Logger.lowerr, Logger.error, cid, slot, true);
				ct = "nochest";
			}
			try {
				setImage("ChestPics/"+ct, 25, pspre+cid+"::"+slot+"::chest");
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
		}
	}
	
	
	public static void updateSlotSync(String cid, int slot, boolean synced) {
		if(!recieveMainFrameUpdates)
			return;
		GUI.setEnabled(pspre+cid+"::"+slot+"::run", !synced);
		GUI.setEnabled(pspre+cid+"::"+slot+"::skip", !synced);
	}
	
	public static void updateCurrency(String cid, String type, int amount) {
		if(!recieveMainFrameUpdates)
			return;
		GUI.setText(pspre+cid+"::"+type, ""+amount);
	}
	
	public static void updateGeneral(String cid, String pn, String ln, Color lc) {
		if(!recieveMainFrameUpdates)
			return;
		GUI.setText(pspre+cid+"::pname", pn);
		GUI.setText(pspre+cid+"::layer", ln);
		GUI.setBackground(pspre+cid+"::laycol", lc);
	}
	
	
	private static void close() {
		close(true);
	}
	
	private static void close(boolean dispose) {
		recieveMainFrameUpdates = false;
		try {
			gui.close(false);
		} catch (Exception e) {}
		
		gui = null;
		
		if(dispose) {
			Browser.dispose();
			
			Manager.stop();
			
			Logger.print("System exit", Logger.general, Logger.info, null, null);
			System.exit(0);
		} else {
			
			WaitScreen.setText("reloading...");
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					open();
					HashSet<String> loaded = Manager.getLoadedProfiles();
					for(String cid : Configs.getConfigIds()) {
						if(loaded.contains(cid)) {
							addLoadedProfile(cid, Manager.getProfilePos(cid), Manager.getProfileType(cid));
							AbstractProfile<?> p = Manager.getProfile(cid);
							for(int i=0; i<p.getSlotSize(); i++)
								updateSlotRunning(cid, i, p.isRunning(i));
						} else {
							addFailedProfile(cid, Manager.getProfilePos(cid), null);
						}
					}
					Manager.updateAllProfiles();
					
					recieveMainFrameUpdates = true;
					WaitScreen.close();
				}
			});
			t.start();
		}
	}
	
	public static void openDesktopBrowser(String link) {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(link));
			} catch (IOException | URISyntaxException e) {
				Logger.printException("MainFrame -> openBrowser: err=can't open DesktopBrowser", e, Logger.runerr, Logger.error, null, null, true);
			}
		} else {
			Logger.print("MainFrame -> openBrowser: err=desktop not supported", Logger.runerr, Logger.error, null, null, true);
		}
	}
	
	private static void setImage(String path, int size, String id) throws IOException {
		Image img = new Image(Ressources.get(path, java.awt.Image.class));
		img.setSquare(size);
		GUI.setImage(id, img);
	}
	
	
	private static String extractTwitchImage(String tun) {
		try {
			Http get = new Http();
			get.setUrl("https://www.twitch.tv/"+tun);
			
			String page = get.sendGet();
			
			int s = page.indexOf("https://static-cdn.jtvnw.net/jtv_user_pictures/");
			int e = page.indexOf("\"", s);
			
			String link = page.substring(s, e);
			
			return link;
		} catch (Exception e) {
			return null;
		}
	}
}
