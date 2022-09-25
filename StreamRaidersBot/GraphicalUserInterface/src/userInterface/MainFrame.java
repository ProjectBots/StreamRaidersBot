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
import include.Guide;
import run.Manager;
import run.ProfileType;
import run.viewer.Viewer;
import srlib.RaidType;
import srlib.SRC;
import srlib.Time;
import srlib.viewer.Raid;
import userInterface.captain.CaptainProfileSection;
import userInterface.viewer.ViewerProfileSection;

public class MainFrame {
	
	public static final String pre = "MainFrame::";
	public static final String pspre = "ProfileSection::";
	
	private static GUI gui = null;
	
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
		if(gui == null)
			return;
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
		if(gui == null)
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
		if(gui == null)
			return;
		gui.remove(pre+cid+"::profile");
		gui.refresh();
	}
	
	public static void profileSwitchedAccountType(String cid, ProfileType type) {
		if(gui == null)
			return;
		remProfile(cid);
		addLoadedProfile(cid, Manager.getProfilePos(cid), type);
		gui.refresh();
		Manager.updateProfile(cid);
	}
	
	public static void updateSlotRunning(String cid, int slot, boolean run) {
		if(gui == null)
			return;
		GUI.setGradient(pspre+cid+"::"+slot+"::run", Colors.getGradient("main buttons " + (run?"on":"def")));
		GUI.setForeground(pspre+cid+"::"+slot+"::run", Colors.getColor("main buttons " + (run?"on":"def")));
	}
	
	public static void updateTimer(String cid, int slot, String time) {
		if(gui == null)
			return;
		GUI.setText(pspre+cid+"::"+slot+"::time", time);
	}
	
	public static void updateSlot(String cid, int slot, Raid raid, boolean change) {
		if(gui == null)
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
			Image img = new Image("data/Other/icon.png");
			img.setSquare(100);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::img", img);
			} catch (IOException e) {
				Logger.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
			GUI.setText(pspre+cid+"::"+slot+"::wins", "??");
			img = new Image("data/LoyaltyPics/noloy.png");
			img.setSquare(20);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::loy", img);
			} catch (IOException e) {
				Logger.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
			GUI.setGradient(pspre+cid+"::"+slot+"::block", Colors.getGradient("main buttons def"));
			GUI.setForeground(pspre+cid+"::"+slot+"::block", Colors.getColor("main buttons def"));
			GUI.setGradient(pspre+cid+"::"+slot+"::fav", Colors.getGradient("main buttons def"));
			GUI.setForeground(pspre+cid+"::"+slot+"::fav", Colors.getColor("main buttons def"));
			img = new Image("data/ChestPics/nochest.png");
			img.setSquare(25);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::chest", img);
			} catch (IOException e) {
				Logger.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
		} else {
			GUI.setText(pspre+cid+"::"+slot+"::capname", raid.get(SRC.Raid.twitchDisplayName));
			Image img = new Image(raid.get(SRC.Raid.twitchUserImage));
			img.setUrl(true);
			img.setSquare(100);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::img", img);
			} catch (IOException e) {
				Logger.print("MainFrame -> onUpdateSlot: err=couldnt set image, url="+raid.get(SRC.Raid.twitchUserImage), Logger.general, Logger.error, cid, slot, true);
				try {
					img = new Image("data/Other/icon.png");
					GUI.setImage(pspre+cid+"::"+slot+"::img", img);
				} catch (IOException e1) {
					Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set default image", e, Logger.general, Logger.error, cid, slot, true);
				}
			}
			GUI.setText(pspre+cid+"::"+slot+"::wins", raid.get(SRC.Raid.pveWins));
			int loy = Integer.parseInt(raid.get(SRC.Raid.pveLoyaltyLevel));
			img = new Image("data/LoyaltyPics/" + Viewer.pveloy[loy] + ".png");
			img.setSquare(20);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::loy", img);
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
			String cap = raid.get(SRC.Raid.twitchDisplayName);
			Integer val = Configs.getCapInt(cid, "(all)", cap, raid.type == RaidType.DUNGEON ? Configs.dungeon : Configs.campaign, Configs.fav);
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
			String ct = Remaper.map(raid.getFromNode(SRC.MapNode.chestType));
			if(ct == null || !cts.contains(new JsonPrimitive(ct))) {
				Logger.print("MainFrame -> updateSlot -> chest_img: err=nochest, ct="+ct, Logger.lowerr, Logger.error, cid, slot, true);
				ct = "nochest";
			}
			img = new Image("data/ChestPics/"+ct+".png");
			img.setSquare(25);
			try {
				GUI.setImage(pspre+cid+"::"+slot+"::chest", img);
			} catch (IOException e) {
				Logger.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Logger.general, Logger.error, cid, slot, true);
			}
		}
	}
	
	
	public static void updateSlotSync(String cid, int slot, boolean synced) {
		if(gui == null)
			return;
		GUI.setEnabled(pspre+cid+"::"+slot+"::run", !synced);
		GUI.setEnabled(pspre+cid+"::"+slot+"::skip", !synced);
	}
	
	public static void updateCurrency(String cid, String type, int amount) {
		if(gui == null)
			return;
		GUI.setText(pspre+cid+"::"+type, ""+amount);
	}
	
	public static void updateGeneral(String cid, String pn, String ln, Color lc) {
		if(gui == null)
			return;
		GUI.setText(pspre+cid+"::pname", pn);
		GUI.setText(pspre+cid+"::layer", ln);
		GUI.setBackground(pspre+cid+"::laycol", lc);
	}
	
	
	private static void close() {
		close(true);
	}
	
	private static void close(boolean dispose) {
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
			GUI.showErrors(false);
			
			WaitScreen.setText("reloading...");
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					open();
					HashSet<String> loaded = Manager.getLoadedProfiles();
					for(String cid : Configs.getConfigIds()) {
						if(loaded.contains(cid)) {
							for(int i=0; i<5; i++)
								updateSlotRunning(cid, i, Manager.getViewer(cid).isRunning(i));
						} else {
							addFailedProfile(cid, Manager.getProfilePos(cid), null);
						}
					}
					Manager.updateAllProfiles();
					
					GUI.showErrors(true);
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
	
}
