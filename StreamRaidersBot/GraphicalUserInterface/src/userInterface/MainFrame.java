package userInterface;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import bot.Browser;
import bot.MapGUI;
import include.GUI;
import include.Json;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Menu;
import include.GUI.TrayMenu;
import include.GUI.WinLis;
import program.ConfigsV2;
import program.Debug;
import include.Guide;
import program.Options;
import program.Raid;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;
import program.Run.SilentException;
import program.SRC;
import run.Run;
import run.Run.FrontEndHandler;

public class MainFrame {
	
	public static final String pre = "MainFrame::";
	
	private static GUI gui = null;
	
	public static GUI getGUI() {
		return gui;
	}
	
	private static int gpos = 0;
	
	
	private static Hashtable<String, Run> profiles = new Hashtable<>();
	
	public static Hashtable<String, Run> getProfiles() {
		return profiles;
	}
	
	
	public static String[] getUserIds() {
		String[] ret = new String[profiles.size()];
		int i=0;
		for(String key : profiles.keySet())
			ret[i++] = profiles.get(key).getBackEndHandler().getUserId();
		return ret;
	}
	
	private static void add(String cid, int pos) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Run r = new Run(cid, ConfigsV2.getPStr(cid, ConfigsV2.cookies));
					r.setFrontEndHandler(new FrontEndHandler() {
						@Override
						public void onStart(String cid, int slot) {
							GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::run", Fonts.getGradient("main buttons on"));
							GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::run", Fonts.getColor("main buttons on"));
						}
						@Override
						public void onStop(String cid, int slot) {
							GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::run", Fonts.getGradient("main buttons def"));
							GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::run", Fonts.getColor("main buttons def"));
						}
						@Override
						public void onTimerUpdate(String cid, int slot, String time) {
							GUI.setText(ProfileSection.pre+cid+"::"+slot+"::time", time);
						}
						@Override
						public void onUpdateSlot(String cid, int slot, Raid raid, boolean locked, boolean change) {
							GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::change", Fonts.getColor("main buttons " + (change ? "on" : "def")));
							GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::change", Fonts.getGradient("main buttons " + (change ? "on" : "def")));
							
							GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::lock", Fonts.getGradient("main buttons " + (locked ? "on" : "def")));
							GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::lock", Fonts.getColor("main buttons " + (locked ? "on" : "def")));
							
							if(raid == null) {
								GUI.setText(ProfileSection.pre+cid+"::"+slot+"::capname", "????????");
								Image img = new Image("data/Other/icon.png");
								img.setSquare(100);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::img", img);
								} catch (IOException e) {
									Debug.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Debug.general, Debug.error, true);
								}
								GUI.setText(ProfileSection.pre+cid+"::"+slot+"::wins", "??");
								img = new Image("data/LoyaltyPics/noloy.png");
								img.setSquare(20);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::loy", img);
								} catch (IOException e) {
									Debug.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Debug.general, Debug.error, true);
								}
								GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons def"));
								GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons def"));
								GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons def"));
								GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons def"));
								img = new Image("data/ChestPics/nochest.png");
								img.setSquare(25);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::chest", img);
								} catch (IOException e) {
									Debug.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Debug.general, Debug.error, true);
								}
							} else {
								GUI.setText(ProfileSection.pre+cid+"::pname", ConfigsV2.getPStr(cid, ConfigsV2.name));
								GUI.setText(ProfileSection.pre+cid+"::"+slot+"::capname", raid.get(SRC.Raid.twitchDisplayName));
								Image img = new Image(raid.get(SRC.Raid.twitchUserImage));
								img.setUrl(true);
								img.setSquare(100);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::img", img);
								} catch (IOException e) {
									Debug.print("MainFrame -> onUpdateSlot: err=couldnt set image, url="+raid.get(SRC.Raid.twitchUserImage), Debug.general, Debug.error, true);
									try {
										img = new Image("data/Other/icon.png");
										GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::img", img);
									} catch (IOException e1) {
										Debug.printException("MainFrame -> onUpdateSlot: err=couldnt set default image", e, Debug.general, Debug.error, true);
									}
								}
								GUI.setText(ProfileSection.pre+cid+"::"+slot+"::wins", raid.get(SRC.Raid.pveWins));
								int loy = Integer.parseInt(raid.get(SRC.Raid.pveLoyaltyLevel));
								img = new Image("data/LoyaltyPics/" + Run.pveloy[loy] + ".png");
								img.setSquare(20);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::loy", img);
								} catch (IOException e) {
									Debug.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Debug.general, Debug.error, true);
								}
								String cap = raid.get(SRC.Raid.twitchDisplayName);
								Integer val = ConfigsV2.getCapInt(cid, "(all)", cap, raid.isDungeon() ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.fav);
								if(val == null) {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons def"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons def"));
								} else if(val == Integer.MAX_VALUE-1) {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons fav_cat"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons fav_cat"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons def"));
								} else if(val == Integer.MIN_VALUE+1) {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons def"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons cat"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons cat"));
								} else if(val == 0) {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons fav_cat"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons fav_cat"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons cat"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons cat"));
								} else if(val > 0) {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons fav_on"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons fav_on"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons def"));
								} else {
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getGradient("main buttons def"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::fav", Fonts.getColor("main buttons def"));
									GUI.setGradient(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getGradient("main buttons on"));
									GUI.setForeground(ProfileSection.pre+cid+"::"+slot+"::block", Fonts.getColor("main buttons on"));
								}
								JsonArray cts = Json.parseArr(Options.get("chests"));
								String ct = raid.getFromNode(SRC.MapNode.chestType);
								if(ct == null) 
									ct = "nochest";
								if(!ct.contains("dungeon") && !ct.equals("nochest") && !cts.contains(new JsonPrimitive(ct)))
									ct = "chestboostedskin";
								img = new Image("data/ChestPics/"+ct+".png");
								img.setSquare(25);
								try {
									GUI.setImage(ProfileSection.pre+cid+"::"+slot+"::chest", img);
								} catch (IOException e) {
									Debug.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Debug.general, Debug.error, true);
								}
							}
						}
						@Override
						public void onUpdateCurrency(String type, int amount) {
							GUI.setText(ProfileSection.pre+cid+"::"+type, ""+amount);
						}
						@Override
						public void onUpdateLayer(String name, Color col) {
							GUI.setText(ProfileSection.pre+cid+"::layer", name);
							GUI.setBackground(ProfileSection.pre+cid+"::laycol", col);
						}
					});
					profiles.put(cid, r);
					
					Container c = new ProfileSection().create(cid);
					c.setPos(0, pos);
					gui.addContainer(c, pre+cid+"::profile");
					updateWS(false);
					
					r.setReady(true);
					r.updateFrame();
					
					return;
				} catch (SilentException e) {
				} catch (NoConnectionException e) {
					Debug.printException(ConfigsV2.getPStr(cid, ConfigsV2.name) + ": Run -> Maybe your internet connection failed", e, Debug.general, Debug.error, true);
				} catch (NotAuthorizedException e3) {
					GUI err = new GUI("User not authorized", 500, 200, MainFrame.getGUI(), null);
					Label l = new Label();
					l.setText("<html>Your Account is not authorized.<br>Please remove and add it again</html>");
					err.addLabel(l);
					err.refresh();
				} catch (Exception e) {
					Debug.printException(ConfigsV2.getPStr(cid, ConfigsV2.name) + ": Run -> setRunning: err=failed to add profile", e, Debug.general, Debug.error, true);
				}
				
				createFailedContainer(cid, pos);
				updateWS(true);
				return;
			}
		});
		t.start();
	}
	
	private static void createFailedContainer(String cid, int pos) {
		Container c = new Container();
		c.setPos(0, pos);
		c.setBorder(Color.gray, 2, 25);
		c.setInsets(5, 2, 5, 2);
		
			Label name = new Label();
			name.setPos(0, 0);
			name.setText(ConfigsV2.getPStr(cid, ConfigsV2.name));
			c.addLabel(name);
		
			Button retry = new Button();
			retry.setPos(1, 0);
			retry.setText("\u27F2 retry");
			retry.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gui.remove(pre+cid+"::profile");
					add(cid, pos);
				}
			});
			;
			c.addBut(retry);
			
		gui.addContainer(c, pre+cid+"::profile");
	}
	
	private static int proCount = 0, proReady = 0, proFailed = 0;
	
	synchronized private static void updateWS(boolean failed) {
		if(failed)
			proFailed++;
		
		proReady++;
		
		WaitScreen.setText("<html><center>Loading Profiles</center><br><center>Ready: " + proReady + "/" + proCount + "</center><br><center>Failed: " + proFailed + "</center></html>");
		
		if(proCount == proReady) {
			frameReady();
		}
	}
	
	private static void frameReady() {
		WaitScreen.setText("Refresh Frame");
		gui.refresh();
		
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
		
		WaitScreen.close();
	}
	
	private static void remove(String name) {
		try {
			for(int i=0; i<4; i++)
				profiles.get(name).setRunning(false, i);
		} catch (Exception e) {}
		profiles.remove(name);
		gui.remove(pre+name+"::profile");

	}
	
	public static void open(boolean add) {
		
		Fonts.ini();
		
		GUI.setDefIcon("data/Other/icon.png");
		
		String bver = Options.get("botVersion");
		
		gui = new GUI("StreamRaider Bot v" + bver, 1000, 700);
		
		gui.setBackgroundGradient(Fonts.getGradient("main background"));
		
		gui.addWinLis(new WinLis() {
			@Override
			public void onIconfied(WindowEvent e) {}
			@Override
			public void onFocusLost(WindowEvent e) {}
			@Override
			public void onFocusGained(WindowEvent e) {
				for(String key : profiles.keySet()) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								profiles.get(key).updateFrame();
							} catch (NoConnectionException | NotAuthorizedException e1) {
								Debug.printException("MainFrame -> open -> onFocusGained: err=failed to update frame", e1, Debug.general, Debug.error, true);
							}
						}
					});
					t.start();
				}
			}
			@Override
			public void onDeIconfied(WindowEvent e) {}
			@Override
			public void onClose(WindowEvent e) {
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
						ActionsDelayed.open(gui, true);
						break;
					case KeyEvent.VK_I:
						ActionsDelayed.open(gui, false);
						break;
					case KeyEvent.VK_R:
						Fonts.ini();
						close(false);
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_P:
						MapGUI.showPlanTypes(gui);
						break;
					case KeyEvent.VK_A:
						doAll(SRC.MainFrame.start, 0);
						break;
					case KeyEvent.VK_O:
						doAll(SRC.MainFrame.stop, 0);
						break;
					case KeyEvent.VK_I:
						doAll(SRC.MainFrame.skip, 0);
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
		
		gui.setMenuBarGradient(Fonts.getGradient("main menubar"));

		int m = 0;
		Menu bot = new Menu("Bot", "Hide Window  Add a Profile  start all  start all delayed  stop all  skip time all  skip time all delayed".split("  "));
		bot.setFont(new Font(null, Font.BOLD, 25));
		bot.setForeground(Fonts.getColor("main menubar"));
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!gui.hide(tray))
					Debug.print("MainFrame -> open: err=Couldn't add to tray", Debug.general, Debug.error, true);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewProfile.open(gui);
			}
		});
		bot.setSep(m);
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAll(SRC.MainFrame.start, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionsDelayed.open(gui, true);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAll(SRC.MainFrame.stop, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAll(SRC.MainFrame.skip, 0);
			}
		});
		bot.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionsDelayed.open(gui, false);
			}
		});
		gui.addMenu(bot);
		
		
		Menu sep = new Menu(" ", new String[0]);
		sep.setFont(new Font(null, Font.BOLD, 25));
		gui.addMenu(sep);
		
		m = 0;
		Menu config = new Menu("Config", "export  import  Settings".split("  "));
		config.setFont(new Font(null, Font.BOLD, 25));
		config.setForeground(Fonts.getColor("main menubar"));
		config.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ConfigsV2GUI().exportConfig(gui);
			}
		});
		config.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ConfigsV2GUI().importConfig(gui);
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
		help.setForeground(Fonts.getColor("main menubar"));
		help.setAL(m++, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Guide("data/Guide").show(gui, "Home");
				} catch (Exception e1) {
					Debug.printException("MainFrame -> openGuide: err=sth went wrong", e1, Debug.runerr, Debug.error, true);
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
	
		refresh(add);
		
	}
	
	public static void doAll(int con, int delay) {
		for(String key : profiles.keySet()) {
			for(int i=0; i<5; i++) {
				Run r = profiles.get(key);
				switch (con) {
				case SRC.MainFrame.start:
					r.setRunning(true, i);
					break;
				case SRC.MainFrame.skip:
					r.skip(i);
					break;
				case SRC.MainFrame.stop:
					r.setRunning(false, i);
					break;
				}
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {}
			}
		}
	}

	public static void forgetMe() {
		if(gui.showConfirmationBox("do you really want\nto delete all your\nprofiles?")) {
			Set<String> kset = profiles.keySet();
			String[] keys = new String[kset.size()];
			keys = kset.toArray(keys);
			for(String key : keys) {
				forget(key, true);
			}
		}
	}
	
	static boolean forget(String cid, boolean force) {
		boolean ret = force || gui.showConfirmationBox("delete " + ConfigsV2.getPStr(cid, ConfigsV2.name) + "?");
		if(ret) {
			ConfigsV2.remProfile(cid);
			remove(cid);
		}
		return ret;
	}
	
	
	public static void refresh(boolean add) {
		List<String> keys = ConfigsV2.getCids();
		
		proCount = keys.size();
		
		WaitScreen.setText("<html><center>Loading Profiles</center><br><center>Ready: 0/" + proCount + "</center><br><center>Failed: 0</center></html>");
		
		if(gui != null) {
			for(String cid : keys)
				if(add) {
					if(!profiles.keySet().contains(cid))
						add(cid, gpos++);
					else
						updateWS(false);
				} else {
					Container c = new ProfileSection().create(cid);
					c.setPos(0, gpos++);
					gui.addContainer(c, pre+cid+"::profile");
				}
			
			if(proCount == 0)
				frameReady();
		}
			
		
		
	}
	
	
	private static void close() {
		close(true);
	}
	
	private static void close(boolean dispose) {
		GUI.showErrors(false);
		try {
			gui.close();
		} catch (Exception e) {}
		
		if(dispose) {
			for(String key : profiles.keySet())
				profiles.get(key).saveStats();
			
			Browser.dispose();
			
			ConfigsV2.save();
			
			Debug.print("System exit", Debug.general, Debug.info);
			System.exit(0);
		} else {
			gpos = 0;
			open(false);
			GUI.showErrors(true);
		}
	}
	
	
}
