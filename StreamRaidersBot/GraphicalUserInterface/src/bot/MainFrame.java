package bot;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Menu;
import include.GUI.TextArea;
import include.GUI.TextField;
import include.GUI.WinLis;
import include.Json;
import program.Configs;
import program.Debug;
import program.Options;
import program.Raid;
import program.Run;
import program.SRC;
import program.SRRHelper;
import program.Unit;
import program.Run.FrontEndHandler;
import include.Http.NoConnectionException;

public class MainFrame {

	private static GUI gui = null;
	
	public static GUI getGUI() {
		return gui;
	}
	
	private static Hashtable<String, Run> profiles = new Hashtable<>();
	
	public static Hashtable<String, Run> getProfiles() {
		return profiles;
	}
	
	private static void add(String name, Run run) {
		profiles.put(name, run);
		addProfile(name);
	}
	
	private static void remove(String name) {
		try {
			profiles.get(name).setRunning(false);
		} catch (Exception e) {}
		
		profiles.remove(name);
	}

	public static void open() {
		
		GUI.setDefIcon("data/Other/icon.png");
		
		String bver = Options.get("botVersion");
		
		gui = new GUI("StreamRaider Bot v" + bver, 900, 700);
		
		gui.addWinLis(new WinLis() {
			@Override
			public void onIconfied(WindowEvent e) {}
			@Override
			public void onFocusLost(WindowEvent e) {}
			@Override
			public void onFocusGained(WindowEvent e) {}
			@Override
			public void onDeIconfied(WindowEvent e) {
				e.getWindow().requestFocus();
			}
			@Override
			public void onClose(WindowEvent e) {
				close();
			}
		});
		
		gui.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if(!((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0)) return;
				if(e.getKeyCode() == KeyEvent.VK_P) {
					MapGUI.showPlanTypes(gui);
				}
			}
		});
		
		
		int m = 0;
		Menu bot = new Menu("Bot", "Guide  General  Add a Profile  start all  start all delayed  stop all  skip time all  skip time all delayed  reload Config".split("  "));
		
			bot.setFont(new Font(null, Font.BOLD, 25));
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GuideContent.show();
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GUI opt = new GUI("Options", 400, 500, gui, null);
					
					int pos = 0;
					
					Button usmr = new Button();
					usmr.setPos(0, pos++);
					usmr.setText("use smart Memory Releaser");
					usmr.setBackground(StreamRaiders.isMemRelRunning() ? Color.green : null);
					usmr.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(StreamRaiders.isMemRelRunning()) {
								StreamRaiders.stopMemReleaser();
								GUI.setBackground("mf::memreleaser", GUI.getDefButCol());
							} else {
								StreamRaiders.startMemReleaser();
								GUI.setBackground("mf::memreleaser", Color.green);
							}
						}
					});
					opt.addBut(usmr, "mf::memreleaser");
					
					Button fm = new Button();
					fm.setPos(0, pos++);
					fm.setText("forget me");
					fm.setTooltip("deletes all profiles");
					fm.setInsets(20, 2, 2, 2);
					fm.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							forgetMe();
						}
					});
					opt.addBut(fm);
					
					Label s1 = new Label();
					s1.setPos(0, pos++);
					s1.setSize(1, 20);
					s1.setText("");
					opt.addLabel(s1);
					
					Button cai = new Button();
					cai.setPos(0, pos++);
					cai.setText("show stats");
					cai.setAL(new ActionListener() {
						
						private JsonObject nrews = null;
						
						private String get(String key) {
							return key.replace("skin", "").replace("scroll", "") + " " + nrews.getAsJsonPrimitive(key).getAsFloat() + "  \n";
						}
						
						private String getc(String key) {
							return key.replace("chest", "") + " " + nrews.getAsJsonPrimitive(key).getAsFloat() + "  \n";
						}
						
						@Override
						public void actionPerformed(ActionEvent e) {
							
							JsonObject all = new JsonObject();
							double hours = 0;
							
							for(String name : Configs.keySet()) {
								
								JsonObject stats = Configs.getObj(name, Configs.stats);
								
								hours += (double) stats.getAsJsonPrimitive("time").getAsLong() / 60 / 60;
								
								JsonObject rews = stats.getAsJsonObject("rewards");
								for(String key : rews.keySet()) {
									if(all.has(key)) {
										all.addProperty(key, all.getAsJsonPrimitive(key).getAsInt() + rews.getAsJsonPrimitive(key).getAsInt());
									} else {
										all.add(key, rews.get(key).deepCopy());
									}
								}
							}
							
							nrews = new JsonObject();
							
							for(String key : all.keySet())
								nrews.addProperty(key, (float) Math.round((double) all.getAsJsonPrimitive(key).getAsInt() / hours * 1000) / 1000);
							
							StringBuilder text = new StringBuilder();
							JsonArray rem = new JsonArray();
							
							text.append("chests:  \n- ");
							
							for(String key : nrews.keySet()) {
								if(key.contains("chest")) {
									text.append(getc(key));
									rem.add(key);
								}
							}
							
							
							List<String> rewBefore = Arrays.asList("gold token potion meat keys".split(" "));
							
							text.append("  \nbasic:  \n- ");
							
							for(String key : nrews.keySet()) {
								if(rewBefore.contains(key)) {
									text.append(get(key));
									rem.add(key);
								}
							}
							
							
							text.append("  \nskins (rare but possible):  \n- ");
							
							for(String key : nrews.keySet()) {
								if(key.contains("skin") && !key.contains("chest")) {
									text.append(get(key));
									rem.add(key);
								}
							}
							
							
							for(int i=0; i<rem.size(); i++) 
								nrews.remove(rem.get(i).getAsString());
							
							
							text.append("  \nscrolls:  \n- ");
							
							for(String key : nrews.keySet()) 
								text.append(get(key));
							
							
							long min = Math.round(hours * 60) % 60;
							text.append("\n(tested for " + Math.round(hours) + " hours and " + min + " minutes)  ");
							
							
							GUI ai = new GUI("Average Income per hour per profile", 400, 500, opt, null);
							
							TextArea ta = new TextArea();
							ta.setEditable(false);
							ta.setText(text.toString());
							ai.addTextArea(ta);
							
							ai.refresh();
						}
					});
					opt.addBut(cai);
					
					Button udas = new Button();
					udas.setPos(0, pos++);
					udas.setText("update stats");
					udas.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(String key : profiles.keySet()) 
								profiles.get(key).saveStats();
						}
					});
					opt.addBut(udas);
					
					Button resStats = new Button();
					resStats.setPos(0, pos++);
					resStats.setText("Reset all Stats");
					resStats.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(opt.showConfirmationBox("Reset all the Stats?")) {
								for(String key : Configs.keySet()) {
									JsonObject nstats = new JsonObject();
									nstats.addProperty("time", 0);
									nstats.add("rewards", new JsonObject());
									Configs.getProfile(key).add(Configs.stats.get(), nstats);
								}
							}
						}
					});
					opt.addBut(resStats);
					
					
					opt.addContainer(Donators.getContainer(0, pos++));
					
					TextArea don = new TextArea();
					don.setPos(0, pos++);
					don.setEditable(false);
					don.setText("Donations:\n"
							+ "Paypal:\n"
							+ "https://paypal.me/projectbots\n"
							+ "\n"
							+ "Bitcoin:\n"
							+ "3FUVTkAijeuNgDyvrvvcGiAXhbxGhyCGBR\n"
							+ "\n"
							+ "Litecoin:\n"
							+ "MP8Y6X6irqarK8KpL6LkWnZpW4LZgGZgWU\n"
							+ "\n"
							+ "Ethereum:\n"
							+ "0x0c9a44a9b6388f030d91dec2ed50b6d3139418a1\n"
							+ "\n"
							+ "Monero:\n"
							+ "49Jk21tDSxsHGvzK7JrMu5UxrKhvkXu3xCXsrrNyqGJc1Kus27PUHZDDSK13fCQL8S7BcokBM3tbX7fwg1cFt6QeE3ycaYT\n"
					);
					opt.addTextArea(don);
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if(Options.is("no_browser")) {
						gui.msg("Error occured", "The Browser is disabled", GUI.MsgConst.ERROR);
						return;
					}
					
					GUI np = new GUI("New Profile", 300, 400, gui, null);
					
					Label lab1 = new Label();
					lab1.setPos(0, 0);
					lab1.setText("Profilename");
					np.addLabel(lab1);
					
					ActionListener openBrowser = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String in = GUI.getInputText("newName");
							if(in.equals("")) 
								if(!np.showConfirmationBox("go ahead without a name for the profile?")) 
									return;
							
							Thread t = new Thread(new Runnable() {
								@Override
								public void run() {
									Browser.show(in);
								}
							});
							t.start();
							np.close();
						}
					};
					
					TextField name = new TextField();
					name.setPos(0, 1);
					name.setText("");
					name.setFill('h');
					name.setAL(openBrowser);
					np.addTextField(name, "newName");
					
					Button open = new Button();
					open.setPos(0, 2);
					open.setText("open Browser to Login");
					open.setAL(openBrowser);
					np.addBut(open);
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for(String key: profiles.keySet()) {
						GUI.setCButSelected(key+"::start", true);
					}
				}
			});
			bot.setAL(m++, new ActionListener() {
				GUI t = null;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					t = new GUI("Time", 500, 200, gui, null);
					
					TextField tf = new TextField();
					tf.setPos(0, 0);
					tf.setText("0");
					tf.setSize(60, 23);
					tf.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							go();
						}
					});
					t.addTextField(tf, "start::delayed::tf");
					
					Button but = new Button();
					but.setPos(0, 1);
					but.setText("start all delayed");
					but.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							go();
						}
					});
					t.addBut(but, "start::delayed::but");
					
				}
				
				private void go() {
					Thread th = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								int time = (int) (Float.parseFloat(GUI.getInputText("start::delayed::tf")) * 1000);
								
								for(String key: profiles.keySet()) {
									GUI.setCButSelected(key+"::start", true);
									try {
										Thread.sleep(time);
									} catch (InterruptedException e) {}
								}
							} catch (NumberFormatException e) {
								t.msg("Wrong Input", "You can't do that", GUI.MsgConst.WARNING);
							}
						}
					});
					th.start();
					t.close();
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for(String key: profiles.keySet()) {
						GUI.setCButSelected(key+"::start", false);
					}
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for(String key : profiles.keySet()) {
						try {
							profiles.get(key).interrupt();
						} catch (Exception e1) {}
					}
				}
			});
			bot.setAL(m++, new ActionListener() {
				GUI t = null;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					t = new GUI("Time", 500, 200, gui, null);
					
					TextField tf = new TextField();
					tf.setPos(0, 0);
					tf.setText("0");
					tf.setSize(60, 23);
					tf.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							go();
						}
					});
					t.addTextField(tf, "start::delayed::tf");
					
					Button but = new Button();
					but.setPos(0, 1);
					but.setText("start all delayed");
					but.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							go();
						}
					});
					t.addBut(but, "start::delayed::but");
					
				}
				
				private void go() {
					Thread th = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								int time = (int) (Float.parseFloat(GUI.getInputText("start::delayed::tf")) * 1000);
								
								for(String key: profiles.keySet()) {
									try {
										profiles.get(key).interrupt();
										Thread.sleep(time);
									} catch (Exception e) {}
								}
							} catch (NumberFormatException e) {
								t.msg("Wrong Input", "You can't do that", GUI.MsgConst.WARNING);
							}
						}
					});
					th.start();
					t.close();
				}
			});
			bot.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(gui.showConfirmationBox("<html><center>Settings took since last<br>restart will be removed!<br>reload config file?</center></html>"))
						refresh(true);
				}
			});
			bot.setSep(3);
			bot.setSep(8);
			gui.addMenu(bot);
			
			Menu sep = new Menu("|", new String[0]);
			sep.setFont(new Font(null, Font.BOLD, 25));
			gui.addMenu(sep);
			
			
			m = 0;
			Menu config = new Menu("Config", "export  import".split("  "));
			config.setFont(new Font(null, Font.BOLD, 25));
			config.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ConfigsGUI.exportConfig(gui);
					
				}
			});
			config.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ConfigsGUI.importConfig(gui);
				}
			});
			gui.addMenu(config);
			
			refresh(false);
			
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
	
	private static void forgetMe() {
		if(gui.showConfirmationBox("do you really want\nto delete all your\nprofiles?")) {
			Set<String> kset = profiles.keySet();
			String[] keys = new String[kset.size()];
			keys = kset.toArray(keys);
			for(String key : keys) {
				forget(key);
			}
		}
	}
	
	private static void forget(String name) {
		Configs.remProfile(name);
		gui.remove(name + "::part");
		remove(name);
	}
	
	private static int pos = 1;
	
	private static void addProfile(String name) {
		
		profiles.get(name).setFrontEndHandler(new FrontEndHandler() {
			@Override
			public void onStart(String name) {
				GUI.setBackground(name+"::start", Color.green);
			}
			@Override
			public void onFail(String name) {
				GUI.setBackground(name+"::start", Color.red);
			}
			@Override
			public void onReload(String name) {
				GUI.setBackground(name+"::start", Color.yellow);
			}
			@Override
			public void onNotAuthorized(String name) {
				GUI err = new GUI("User not authorized", 500, 200, MainFrame.getGUI(), null);
				Label l = new Label();
				l.setText("<html>Your Account is not authorized.<br>Please remove and add it again</html>");
				err.addLabel(l);
				err.refresh();
				GUI.setBackground(name+"::start", Color.red);
			}
			@Override
			public void onUpdateTimer(String name, String time) {
				GUI.setText(name + "::counter", time);
			}
			@Override
			public void onUpdateSlot(String name, int slot, Raid raid) {
				JsonObject favs = Configs.getObj(name, Configs.favs);
				JsonArray cts = Json.parseArr(Options.get("chests"));
				
				int wins = Integer.parseInt(raid.get(SRC.Raid.pveWins));
				int lvl = Integer.parseInt(raid.get(SRC.Raid.pveLoyaltyLevel));
				
				String disName = raid.get(SRC.Raid.twitchDisplayName);
				GUI.setText(name+"::name::"+slot, disName + " - " + wins + "|" + Run.pveloy[lvl]);
				
				String ct = raid.getFromNode(SRC.MapNode.chestType);
				if(ct == null) 
					ct = "nochest";
				if(!ct.contains("dungeon") && !ct.equals("nochest") && !cts.contains(new JsonPrimitive(ct)))
					ct = "chestboostedskin";
				
				Image img = new Image("data/ChestPics/" + ct + ".png");
				img.setSquare(30);
				try {
					GUI.setImage(name+"::chest::"+slot, img);
				} catch (IOException e) {
					Debug.printException("MainFrame -> onUpdateSlot: err=couldnt set image", e, Debug.general, Debug.error, null, null, true);
				}
				GUI.setEnabled(name+"::lockBut::"+slot, true);
				
				if(favs.has(disName)) {
					GUI.setText(name+"::favBut::"+slot, "\u2764");
					GUI.setForeground(name+"::favBut::"+slot, new Color(227,27,35));
				} else {
					GUI.setText(name+"::favBut::"+slot, "\uD83D\uDC94");
					GUI.setForeground(name+"::favBut::"+slot, Color.black);
				}
			}
			@Override
			public void onSlotBlocked(String name, int slot, boolean b) {
				if(b) 
					GUI.setForeground(name+"::name::"+slot, Color.red);
				else
					GUI.setForeground(name+"::name::"+slot, Color.black);
			}
			@Override
			public void onSlotLocked(String name, int slot, boolean l) {
				if(l) {
					GUI.setText(name+"::lockBut::"+slot, "\uD83D\uDD12");
					GUI.setBackground(name+"::lockBut::"+slot, Color.green);
				} else {
					GUI.setText(name+"::lockBut::"+slot, "\uD83D\uDD13");
					GUI.setBackground(name+"::lockBut::"+slot, GUI.getDefButCol());
				}
			}
			@Override
			public void onSlotEmpty(String name, int slot) {
				GUI.setText(name+"::name::"+slot, "");
				Image img = new Image("data/ChestPics/nochest.png");
				img.setSquare(30);
				try {
					GUI.setImage(name+"::chest::"+slot, img);
				} catch (IOException e) {
					Debug.printException("MainFrame -> onSlotEmpty: err=couldnt set image", e, Debug.general, Debug.error, null, null, true);
				}
			}
		});
		
		Container both = new Container();
		both.setPos(0, pos++);
		both.setFill('h');
		both.setBorder(Color.lightGray, 1, 30);
		both.setInsets(5, 0, 5, 0);
		
		Container top = new Container();
		top.setFill('h');
		top.setPos(0, 0);
		top.setWeightX(1);
		
		Label profileName = new Label();
		profileName.setPos(0, 0);
		profileName.setText(name);
		profileName.setTooltip("profile name");
		top.addLabel(profileName);
		
		Label s2 = new Label();
		s2.setPos(1, 0);
		s2.setSize(20, 1);
		s2.setText("");
		top.addLabel(s2);
		
		Label counter = new Label();
		counter.setPos(2, 0);
		counter.setText("");
		counter.setTooltip("time until next round");
		top.addLabel(counter, name + "::counter");
		
		Label s3 = new Label();
		s3.setPos(3, 0);
		s3.setText("");
		s3.setWeightX(1);
		top.addLabel(s3);
		
		both.addContainer(top);
		
		Container part = new Container();
		part.setFill('h');
		part.setPos(0, 1);
		part.setWeightX(1);
		
		CButton start = new CButton(name+"::start");
		start.setPos(0, 0);
		start.setSpan(1, 4);
		start.setFill('v');
		start.setText("\u23F5");
		start.setFont(new Font(null, Font.PLAIN, 20));
		start.setTooltip("start/stop");
		start.setCBL(new CButListener() {
			@Override
			public void unselected(String id, ActionEvent e) {
				GUI.setText(id, "\u23F5");
				GUI.setBackground(id, GUI.getDefButCol());
				profiles.get(name).setRunning(false);
			}
			@Override
			public void selected(String id, ActionEvent e) {
				GUI.setText(id, "\u23F8");
				GUI.setBackground(id, Color.green);
				profiles.get(name).setRunning(true);
			}
		});
		part.addCBut(start);
		
		Button next = new Button();
		next.setPos(1, 0);
		next.setSpan(1, 4);
		next.setFill('v');
		next.setText("\u23E9");
		next.setFont(new Font(null, Font.PLAIN, 20));
		next.setTooltip("skip time");
		next.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				profiles.get(name).interrupt();
			}
		});
		part.addBut(next);
		
		for(int i=0; i<4; i++) {
			Label name1 = new Label();
			name1.setText(""+i);
			name1.setPos(2, i);
			name1.setTooltip("name - wins|loyalty");
			part.addLabel(name1, name+"::name::"+i);
			
			final int ii = i;
			Button lockBut = new Button();
			lockBut.setPos(3, i);
			lockBut.setText("\uD83D\uDD13");
			lockBut.setTooltip("lock/unlock streamer");
			lockBut.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(Configs.isSlotLocked(name, ""+ii)) {
						Configs.setSlotLocked(name, ""+ii, false);
						GUI.setText(name+"::lockBut::"+ii, "\uD83D\uDD13");
						GUI.setBackground(name+"::lockBut::"+ii, GUI.getDefButCol());
					} else {
						Configs.setSlotLocked(name, ""+ii, true);
						GUI.setText(name+"::lockBut::"+ii, "\uD83D\uDD12");
						GUI.setBackground(name+"::lockBut::"+ii, Color.green);
					}
				}
			});
			part.addBut(lockBut, name+"::lockBut::"+i);
			
			
			Button fav = new Button();
			fav.setPos(4, i);
			fav.setText("\uD83D\uDC94");
			fav.setTooltip("favorite/unfavorite streamer");
			fav.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JsonObject favs = Configs.getObj(name, Configs.favs);
					SRRHelper srrh = profiles.get(name).getSRRH();
					if(srrh != null) {
						Raid[] raids = srrh.getRaids();
						for(int j=0; j<raids.length; j++) {
							if(raids[j].get(SRC.Raid.userSortIndex).equals(""+ii)) {
								String disName = new JsonPrimitive(raids[j].get(SRC.Raid.twitchDisplayName)).getAsString();
								if(favs.has(disName)) {
									favs.remove(disName);
									GUI.setText(name+"::favBut::"+ii, "\uD83D\uDC94");
									GUI.setForeground(name+"::favBut::"+ii, Color.black);
								} else {
									favs.addProperty(disName, false);
									GUI.setText(name+"::favBut::"+ii, "\u2764");
									GUI.setForeground(name+"::favBut::"+ii, new Color(227,27,35));
								}
							}
						}
					}
				}
			});
			part.addBut(fav, name+"::favBut::"+i);
			
			Button map = new Button();
			map.setPos(5, i);
			map.setText("Map");
			map.setTooltip("show map");
			map.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MapGUI.asGui_old(profiles.get(name), ii);
				}
			});
			part.addBut(map, name+"::map::"+i);
			
			
			Image chest = new Image("data/ChestPics/nochest.png");
			chest.setPos(6, i);
			chest.setSquare(30);
			chest.setTooltip("the chest type this raid will bring");
			part.addImage(chest, name+"::chest::"+i);
			
		}
		
		Label s1 = new Label();
		s1.setPos(7, 0);
		s1.setSpan(1, 4);
		s1.setText("");
		s1.setWeightX(1);
		s1.setFill('h');
		part.addLabel(s1);
		
		Button seeRews = new Button();
		seeRews.setPos(8, 0);
		seeRews.setSpan(1, 4);
		seeRews.setFill('v');
		seeRews.setText("\u26C1");
		seeRews.setFont(new Font(null, Font.PLAIN, 20));
		seeRews.setTooltip("show income");
		seeRews.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JsonObject rews = profiles.get(name).getRews();
				
				String text = "";
				Set<String> keys = rews.keySet();
				
				for(String key : keys) {
					if(!key.contains("chest")) continue;
					text += "|" + key + " " + rews.getAsJsonPrimitive(key).getAsString() + "\n";
				}
				
				for(int i=0; i<rewBefore.length; i++) {
					try {
						text += "|" + rewBefore[i] + " " + rews.getAsJsonPrimitive(rewBefore[i]).getAsString() + "\n";
					} catch (Exception e1) {}
				}
				
				for(String key: keys) {
					if(Arrays.asList(rewBefore).indexOf(key) != -1 || key.contains("chest")) continue;
					text += "|" + key + " " + rews.getAsJsonPrimitive(key).getAsString() + "\n";
				}
				
				JsonObject bought = profiles.get(name).getBoughtItems();
				StringBuilder sb = new StringBuilder("bought from store:\n");
				for(String key : bought.keySet())
					sb.append(key + " " + bought.get(key).getAsString() + "\n");
				
				GUI guir = new GUI("Rewards for " + name, 300, 300, gui, null);
				
				TextArea ta = new TextArea();
				ta.setEditable(false);
				ta.setText(text);
				ta.setPos(0, 0);
				guir.addTextArea(ta);
				
				TextArea ta1 = new TextArea();
				ta1.setEditable(false);
				ta1.setText(sb.substring(0, sb.length()-1));
				ta1.setPos(0, 1);
				guir.addTextArea(ta1);
				
				
				
			}
		});
		part.addBut(seeRews);
		
		Button stngs = new Button();
		stngs.setPos(9, 0);
		stngs.setSpan(1, 4);
		stngs.setFill('v');
		stngs.setText("\u23E3");
		stngs.setFont(new Font(null, Font.PLAIN, 20));
		stngs.setTooltip("settings");
		stngs.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI sgui = new GUI("Profile Settings for " + name, 900, 800, gui, null);
				
				sgui.addWinLis(new WinLis() {
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
						Configs.saveb();
					}
				});
				
				int g = 0;
				
				JsonObject types = Unit.getTypes();
				
				Container units = new Container();
				units.setPos(0, g++);
				
				int x = 0;
				int y = 1;
				for(final String type : types.keySet()) {
					
					int rank = types.getAsJsonObject(type).getAsJsonPrimitive("rank").getAsInt();
					if(rank != y) {
						y = rank;
						x = 0;
					}
					
					Container cimg = new Container();
					Image img = new Image("data/UnitPics/" + type.toLowerCase().replace("allies", "") + ".png");
					img.setSquare(100);
					cimg.addImage(img);
					
					Button c = new Button();
					c.setPos(x++, y);
					c.setContainer(cimg);
					c.setFill('h');
					c.setTooltip("Unit settings for " + type);
					c.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							
							GUI uset = new GUI(type + " settings", 400, 650, sgui, null);
							int y = 0;
							
							Image img = new Image("data/UnitPics/" + type.toLowerCase().replace("allies", "") + ".png");
							img.setPos(0, y++);
							img.setSpan(2, 1);
							img.setSquare(200);
							img.setInsets(2, 2, 20, 2);
							uset.addImage(img);
							
							String[] ts = "place epic upgrade unlock dupe".split(" ");
							
							for(int i=0; i<ts.length; i++) {
								Label l = new Label();
								l.setPos(0, y);
								l.setText(ts[i] + " prio");
								l.setAnchor("e");
								l.setFont(new Font(null, Font.PLAIN, 19));
								uset.addLabel(l);
								
								TextField tf = new TextField();
								tf.setPos(1, y++);
								tf.setInsets(2, 15, 2, 2);
								tf.setText(""+Configs.getUnitInt(name, type, new Configs.B(ts[i])));
								tf.setFill('h');
								tf.setFont(new Font(null, Font.PLAIN, 19));
								uset.addTextField(tf, name + "::" + type + "::uset::" + ts[i]);
							}
							
							if(!Unit.isLegendary(type)) {
								Label l = new Label();
								l.setPos(0, y);
								l.setAnchor("e");
								l.setText("buy prio");
								l.setFont(new Font(null, Font.PLAIN, 19));
								uset.addLabel(l);
								
								TextField tf = new TextField();
								tf.setPos(1, y++);
								tf.setText(""+Configs.getUnitInt(name, type, Configs.buy));
								tf.setFill('h');
								tf.setInsets(2, 15, 2, 2);
								tf.setFont(new Font(null, Font.PLAIN, 19));
								uset.addTextField(tf, name + "::" + type + "::uset::buy");
								
							}
							
							Button upd = new Button();
							upd.setPos(0, y++);
							upd.setSpan(2, 1);
							upd.setInsets(10, 2, 20, 2);
							upd.setText("\uD83D\uDDD8 update");
							upd.setFont(new Font(null, Font.PLAIN, 19));
							upd.setFill('h');
							upd.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									String[] tfids;
									
									if(Unit.isLegendary(type)) {
										tfids = ts.clone();
									} else {
										tfids = new String[ts.length + 1];
										System.arraycopy(ts, 0, tfids, 0, ts.length);
										tfids[ts.length] = "buy";
									}
									
									JsonArray wrong = new JsonArray();
									for(int i=0; i<tfids.length; i++) {
										try {
											Configs.setUnitInt(name, type, new Configs.B(tfids[i]), Integer.parseInt(GUI.getInputText(name + "::" + type + "::uset::" + tfids[i])));
										} catch (NumberFormatException e1) {
											wrong.add(tfids[i]);
										}
									}
									
									if(wrong.size() == 0) {
										uset.close();
									} else {
										StringBuilder sb = new StringBuilder("You can't do this:");
										for(int i=0; i<wrong.size(); i++)
											sb.append("\n" + wrong.get(i).getAsString() + " prio");
										
										uset.msg("Wrong input", sb.toString(), GUI.MsgConst.WARNING);
									}
								}
							});
							uset.addBut(upd);
							
							Button spec = new Button();
							spec.setPos(0, y++);
							spec.setInsets(2, 2, 20, 2);
							spec.setSpan(2, 1);
							spec.setText("\u23E3 specialize");
							spec.setFont(new Font(null, Font.PLAIN, 19));
							spec.setFill('h');
							spec.setTooltip("choose a Specialization for this Unit");
							spec.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									final JsonArray uids = Json.parseObj(Options.get("specUIDs")).getAsJsonArray(type);
									
									String old = Configs.getUnitString(name, type, Configs.spec);
									
									GUI gspec = new GUI("specialize " + type, 400, 300, sgui, null);
									
									for(int i=0; i<3; i++) {
										final String u = uids.get(i).getAsJsonObject().getAsJsonPrimitive("uid").getAsString();
										final String uName = uids.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString();
										final int ii = i;
										Button uid = new Button();
										uid.setPos(0, i);
										uid.setText(uName);
										if(old.equals(u)) uid.setBackground(Color.green);
										uid.setFill('h');
										uid.setAL(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												if(Configs.getUnitString(name, type, Configs.spec).equals(u)) {
													Configs.setUnitString(name, type, Configs.spec, "null");
													GUI.setBackground(name + "::spec::" + type + "::" + ii, GUI.getDefButCol());
												} else {
													Configs.setUnitString(name, type, Configs.spec, u);
													GUI.setBackground(name + "::spec::" + type + "::" + ii, Color.green);
													GUI.setBackground(name + "::spec::" + type + "::" + ((ii+1)%3), GUI.getDefButCol());
													GUI.setBackground(name + "::spec::" + type + "::" + ((ii+2)%3), GUI.getDefButCol());
												}
											}
										});
										gspec.addBut(uid, name + "::spec::" + type + "::" + i);
									}
									
								}
							});
							uset.addBut(spec);
						}
					});
					units.addBut(c);
				}
				
				sgui.addContainer(units);
				

				JsonArray chestTypes = Json.parseArr(Options.get("chests"));
				chestTypes.remove(new JsonPrimitive("chestsalvage"));
				
				Container chests = new Container();
				chests.setPos(0, g++);
				
				y = 0;
				x = 0;
				for(int i=0; i<chestTypes.size(); i++) {
					
					String type = chestTypes.get(i).getAsString();
					
					Container cimg = new Container();
					Image img = new Image("data/ChestPics/" + type + ".png");
					img.setSquare(100);
					cimg.addImage(img);
					
					Button chest = new Button();
					chest.setPos(x, y);
					chest.setContainer(cimg);
					chest.setFill('h');
					chest.setTooltip("Chest settings for " + type.replace("chest", ""));
					chest.setInsets(0, 10, 10, 0);
					chest.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							GUI cgui = new GUI(type + " settings", 400, 650, sgui, null);
							
							int y = 0;
							
							Image imgc = new Image("data/ChestPics/" + type + ".png");
							imgc.setSquare(150);
							imgc.setAnchor("c");
							imgc.setPos(0, y++);
							imgc.setSpan(3, 1);
							imgc.setInsets(2, 2, 20, 2);
							cgui.addImage(imgc);
							
							
							String[] mm = "min max".split(" ");
							
							for(String key : mm) {
								
								int wins = Configs.getChestInt(name, type, key.equals("min") ? Configs.minc : Configs.maxc);
								
								Label lmm = new Label();
								lmm.setPos(0, y);
								lmm.setText(key+" wins");
								lmm.setFont(new Font(null, Font.PLAIN, 20));
								cgui.addLabel(lmm);
								
								TextField tmm = new TextField();
								tmm.setPos(1, y);
								tmm.setText(""+wins);
								tmm.setSize(80, 40);
								tmm.setInsets(2, 10, 2, 2);
								tmm.setFont(new Font(null, Font.PLAIN, 20));
								tmm.setDocLis(new DocumentListener() {
									@Override
									public void removeUpdate(DocumentEvent e) {
										changed();
									}
									@Override
									public void insertUpdate(DocumentEvent e) {
										changed();
									}
									@Override
									public void changedUpdate(DocumentEvent e) {
										changed();
									}
									public void changed() {
										try {
											int wins = Integer.parseInt(GUI.getInputText(name+"::chest::"+type+"::"+key));
											int w;
											if(wins < 0) 
												if(key.equals("min"))
													w = 1;
												else
													w = 3;
											else if(wins < 15) 
												w = 1;
											else if(wins < 50) 
												w = 2;
											else 
												w = 3;
											
											Image img = new Image("data/LoyaltyPics/" + Run.pveloy[w] +".png");
											img.setSquare(35);
											try {
												GUI.setImage(name+"::chest::"+type+"::loyImg::"+key, img);
											} catch (IOException e) {
												Debug.printException("MainFrame -> changed: err=couldnt set image", e, Debug.general, Debug.error, null, null, true);
											}
											GUI.setBackground(name+"::chest::"+type+"::loyBut::"+key, loyCols[w]);
										} catch (NumberFormatException e) {}
									}
								});
								cgui.addTextField(tmm, name+"::chest::"+type+"::"+key);
								
								
								int w;
								if(wins < 0) 
									if(key.equals("min"))
										w = 1;
									else
										w = 3;
								else if(wins < 15) 
									w = 1;
								else if(wins < 50) 
									w = 2;
								else 
									w = 3;
								
								
								Container cimg = new Container();
								Image img = new Image("data/LoyaltyPics/" + Run.pveloy[w] +".png");
								img.setSquare(35);
								cimg.addImage(img, name+"::chest::"+type+"::loyImg::"+key);
								
								Button bmm = new Button();
								bmm.setPos(2, y);
								bmm.setBackground(loyCols[w]);
								bmm.setContainer(cimg);
								bmm.setAL(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										try {
											int w = Integer.parseInt(GUI.getInputText(name+"::chest::"+type+"::"+key));
											int n;
											if(key.equals("min")) 
												if(w < 15) 
													n = 15;
												else if(w < 50)
													n = 50;
												else 
													n = -1;
											else 
												if(w < 0 || w > 49)
													n = 14;
												else if(w < 15) 
													n = 49;
												else
													n = -1;
											
											GUI.setText(name+"::chest::"+type+"::"+key, ""+n);
											
										} catch (NumberFormatException e1) {}
									}
								});
								cgui.addBut(bmm, name+"::chest::"+type+"::loyBut::"+key);
								
								y++;
							}
							
							Button upd = new Button();
							upd.setPos(0, y++);
							upd.setSpan(3, 1);
							upd.setFill('h');
							upd.setText("\uD83D\uDDD8 update");
							upd.setFont(new Font(null, Font.PLAIN, 20));
							upd.setInsets(20, 2, 20, 2);
							upd.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									try {
										int min = Integer.parseInt(GUI.getInputText(name+"::chest::"+type+"::min"));
										int max = Integer.parseInt(GUI.getInputText(name+"::chest::"+type+"::max"));
										if(min > max && max > -1)
											throw new NumberFormatException();
										Configs.setChestInt(name, type, Configs.minc, min);
										Configs.setChestInt(name, type, Configs.maxc, max);
										cgui.close();
									} catch (NumberFormatException e1) {
										cgui.msg("Wrong input", "You cant do that", GUI.MsgConst.WARNING);
									}
								}
							});
							cgui.addBut(upd);
							
							
							Button en = new Button();
							en.setPos(0, y++);
							en.setSpan(3, 1);
							en.setFill('h');
							if(Configs.getChestBoolean(name, type, Configs.enabled)) {
								en.setText("Enabled");
								en.setBackground(Color.green);
							} else {
								en.setText("Disabled");
							}
							en.setFont(new Font(null, Font.PLAIN, 20));
							en.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									if(Configs.getChestBoolean(name, type, Configs.enabled)) {
										Configs.setChestBoolean(name, type, Configs.enabled, false);
										GUI.setBackground(name+"::chest::"+type+"::enable", GUI.getDefButCol());
										GUI.setText(name+"::chest::"+type+"::enable", "Disabled");
									} else {
										Configs.setChestBoolean(name, type, Configs.enabled, true);
										GUI.setBackground(name+"::chest::"+type+"::enable", Color.green);
										GUI.setText(name+"::chest::"+type+"::enable", "Enabled");
									}
								}
							});
							cgui.addBut(en, name+"::chest::"+type+"::enable");
							
						}
					});
					chests.addBut(chest, name + "::" + type);
					
					if(x >= 3) {
						x = 0;
						y++;
					} else {
						x++;
					}
				}
				
				sgui.addContainer(chests);
				
				
				Container cdslot = new Container();
				cdslot.setPos(0, g++);
				
					Label ldslot = new Label();
					ldslot.setPos(0, 0);
					ldslot.setText("Dungeon Slot: ");
					ldslot.setFont(new Font(null, Font.PLAIN, 25));
					cdslot.addLabel(ldslot);
					
					
					String sel = Configs.getStr(name, Configs.dungeonSlot);
					
					ComboBox dslot = new ComboBox(name+"::dslot");
					dslot.setPos(1, 0);
					dslot.setFont(new Font(null, Font.PLAIN, 23));
					dslot.setList(ArrayUtils.insert(0, ArrayUtils.removeElement("(none) 0 1 2 3".split(" "), sel), sel));
					dslot.setCL(new CombListener() {
						@Override
						public void unselected(String id, ItemEvent e) {}
						@Override
						public void selected(String id, ItemEvent e) {
							Configs.setStr(name, Configs.dungeonSlot, GUI.getSelected(id));
						}
					});
					cdslot.addComboBox(dslot);
					
					Label bc = new Label();
					bc.setPos(2, 0);
					bc.setText("buy Chest:");
					bc.setFont(new Font(null, Font.PLAIN, 25));
					cdslot.addLabel(bc);
					
					String sel2 = Configs.getStr(name, Configs.canBuyChest);
					
					ComboBox cbc = new ComboBox(name+"::canBuyChest");
					cbc.setPos(3, 0);
					cbc.setFont(new Font(null, Font.PLAIN, 23));
					cbc.setList(ArrayUtils.insert(0, ArrayUtils.removeElement("(none) vampire saint".split(" "), sel2), sel2));
					cbc.setCL(new CombListener() {
						@Override
						public void unselected(String id, ItemEvent e) {}
						@Override
						public void selected(String id, ItemEvent e) {
							Configs.setStr(name, Configs.canBuyChest, GUI.getSelected(id));
						}
					});
					cdslot.addComboBox(cbc);
					
				sgui.addContainer(cdslot);
				
				Container becc = new Container();
				becc.setPos(0, g++);
					
					Label becl = new Label();
					becl.setText("Buy Event Chest: ");
					becl.setPos(0, 0);
					becl.setFont(new Font(null, Font.PLAIN, 25));
					becc.addLabel(becl);
					
					String sel3 = Configs.getStr(name, Configs.buyEventChest);
					
					ComboBox beccb = new ComboBox(name+"::canBuyEventChest");
					beccb.setList(ArrayUtils.insert(0, ArrayUtils.removeElement("(none)  St. Jude  AFSP  MHA  Toys For Tots".split("  "), sel3), sel3));
					beccb.setPos(1, 0);
					beccb.setFont(new Font(null, Font.PLAIN, 23));
					beccb.setCL(new CombListener() {
						@Override
						public void unselected(String id, ItemEvent e) {}
						@Override
						public void selected(String id, ItemEvent e) {
							Configs.setStr(name, Configs.buyEventChest, GUI.getSelected(id));
						}
					});
					becc.addComboBox(beccb);
					
				
				sgui.addContainer(becc);
				
				Button cbsb = new Button();
				cbsb.setPos(0, g++);
				cbsb.setFont(new Font(null, Font.PLAIN, 23));
				cbsb.setInsets(10, 2, 5, 2);
				cbsb.setText("can buy Scrolls");
				if(Configs.getBoolean(name, Configs.canBuyScrolls))
					cbsb.setBackground(Color.green);
				cbsb.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(Configs.getBoolean(name, Configs.canBuyScrolls)) {
							Configs.setBoolean(name, Configs.canBuyScrolls, false);
							GUI.setBackground(name+"::canBuyScrolls", GUI.getDefButCol());
						} else {
							Configs.setBoolean(name, Configs.canBuyScrolls, true);
							GUI.setBackground(name+"::canBuyScrolls", Color.green);
						}
					}
				});
				sgui.addBut(cbsb, name+"::canBuyScrolls");
				
				
				
				Container csr = new Container();
				csr.setPos(0, g++);
					
					Label lstm = new Label();
					lstm.setPos(0, 0);
					lstm.setSpan(3, 1);
					lstm.setText("Store Refreshes:");
					lstm.setFont(new Font(null, Font.PLAIN, 23));
					csr.addLabel(lstm);
					
					int l=0;
					for(; l<4; l++) {
						Label lst = new Label();
						lst.setAnchor("c");
						lst.setText(l == 3 ? "3+" : ""+l);
						lst.setPos(0, l+1);
						lst.setFont(new Font(null, Font.PLAIN, 19));
						csr.addLabel(lst);
						
						TextField tfsr = new TextField();
						tfsr.setText(""+Configs.getStoreRefreshInt(name, l));
						tfsr.setSize(80, 21);
						tfsr.setPos(1, l+1);
						tfsr.setFont(new Font(null, Font.PLAIN, 17));
						csr.addTextField(tfsr, name+"::storeRefresh::tf::"+l);
						
						Label lsr = new Label();
						lsr.setText("gold");
						lsr.setPos(2, l+1);
						lsr.setFont(new Font(null, Font.PLAIN, 19));
						csr.addLabel(lsr);
					}
					
					Button bsr = new Button();
					bsr.setPos(0, l+1);
					bsr.setSpan(3, 1);
					bsr.setFont(new Font(null, Font.PLAIN, 18));
					bsr.setText("\uD83D\uDDD8 update");
					bsr.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(int i=0; i<4; i++) {
								try {
									Configs.setStoreRefreshInt(name, i, Integer.parseInt(GUI.getInputText(name+"::storeRefresh::tf::"+i)));
								} catch (NumberFormatException e1) {
									sgui.msg("Wrong Input", "You can't do this!", GUI.MsgConst.WARNING);
								}
							}
						}
					});
					csr.addBut(bsr);
					
				sgui.addContainer(csr);
				
				Label bsl = new Label();
				bsl.setPos(0, g++);
				bsl.setInsets(20, 2, 2, 2);
				bsl.setText("Exlude Slots from auto farming:");
				bsl.setFont(new Font(null, Font.PLAIN, 25));
				sgui.addLabel(bsl);
				
				Container cbs = new Container();
				cbs.setPos(0, g++);
				
				for(int i=0; i<4; i++) {
					final int ii = i;
					Button bs = new Button();
					bs.setPos(i, 0);
					bs.setText(""+i);
					bs.setTooltip("exclude slot " + i);
					bs.setFont(new Font(null, Font.PLAIN, 25));
					if(Configs.isSlotBlocked(name, ""+i))
						bs.setBackground(Color.green);
					bs.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(Configs.isSlotBlocked(name, ""+ii)) {
								Configs.setSlotBlocked(name, ""+ii, false);
								GUI.setBackground(name+"::slots::"+ii, GUI.getDefButCol());
							} else {
								Configs.setSlotBlocked(name, ""+ii, true);
								GUI.setBackground(name+"::slots::"+ii, Color.green);
							}
						}
					});
					cbs.addBut(bs, name+"::slots::"+i);
				}
				
				sgui.addContainer(cbs);
				
				Container time = new Container();
				time.setPos(0, g++);
				time.setInsets(20, 10, 10, 2);
				
					Label lt1 = new Label();
					lt1.setText("Time:");
					lt1.setFont(new Font(null, Font.PLAIN, 20));
					lt1.setPos(0, 0);
					lt1.setSpan(3, 1);
					time.addLabel(lt1);
					
					TextField tfmin = new TextField();
					tfmin.setText(""+Configs.getTime(name, Configs.min));
					tfmin.setSize(40, 20);
					tfmin.setTooltip("min");
					tfmin.setPos(0, 1);
					tfmin.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							changeTime(sgui, name);
						}
					});
					time.addTextField(tfmin, name + "::time::min");
					
					Label lt2 = new Label();
					lt2.setText("s - ");
					lt2.setPos(1, 1);
					time.addLabel(lt2);
					
					TextField tfmax = new TextField();
					tfmax.setText(""+Configs.getTime(name, Configs.max));
					tfmax.setSize(40, 20);
					tfmax.setTooltip("max");
					tfmax.setPos(2, 1);
					tfmax.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							changeTime(sgui, name);
						}
					});
					time.addTextField(tfmax, name + "::time::max");
					
					Label lt3 = new Label();
					lt3.setText("s");
					lt3.setPos(3, 1);
					time.addLabel(lt3);
					
					Button tbut = new Button();
					tbut.setText("\uD83D\uDDD8 update");
					tbut.setPos(4, 1);
					tbut.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							changeTime(sgui, name);
						}
					});
					time.addBut(tbut);
					
				sgui.addContainer(time);
				
				int posFav = g++;
				
				createFavCon(sgui, name, posFav);
				
				Container search = new Container();
				search.setPos(0, g++);
				search.setInsets(20, 10, 10, 2);
				
					TextField stf = new TextField();
					stf.setPos(0, 0);
					stf.setSize(100, 28);
					stf.setText("");
					stf.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							searchCap(sgui, name, posFav);
						}
					});
					search.addTextField(stf, name+"::search::cap");
					
					Button sbut = new Button();
					sbut.setText("search captain");
					sbut.setPos(1, 0);
					sbut.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							searchCap(sgui, name, posFav);
						}
					});
					search.addBut(sbut);
					
				sgui.addContainer(search);
				
				Container mpc = new Container();
				mpc.setPos(0, g++);
				
				Label lmp = new Label();
				lmp.setPos(0, 0);
				lmp.setText("Max Page");
				mpc.addLabel(lmp);
				
				TextField tmp = new TextField();
				tmp.setPos(1, 0);
				tmp.setText(""+Configs.getInt(name, Configs.maxPage));
				tmp.setSize(80, 23);
				tmp.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setMaxPage(name, sgui);
					}
				});
				mpc.addTextField(tmp, name+"::mp");
				
				Button bmp = new Button();
				bmp.setPos(2, 0);
				bmp.setText("\uD83D\uDDD8 update");
				bmp.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setMaxPage(name, sgui);
					}
				});
				mpc.addBut(bmp);
				
				sgui.addContainer(mpc);
				
				
				Container cupd = new Container();
				cupd.setPos(0, g++);
				
				Label lupd = new Label();
				lupd.setPos(0, 0);
				lupd.setText("Unit place delay");
				cupd.addLabel(lupd);
				
				TextField tupd = new TextField();
				tupd.setPos(1, 0);
				tupd.setText(""+ (float) Configs.getInt(name, Configs.unitPlaceDelay) / 1000);
				tupd.setSize(80, 23);
				tupd.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setUnitPlaceDelay(name, sgui);
					}
				});
				cupd.addTextField(tupd, name+"::tupd");
				
				Button bupd = new Button();
				bupd.setPos(2, 0);
				bupd.setText("\uD83D\uDDD8 update");
				bupd.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setUnitPlaceDelay(name, sgui);
					}
				});
				cupd.addBut(bupd);
				
				sgui.addContainer(cupd);
				
				Button resStat = new Button();
				resStat.setPos(0, g++);
				resStat.setInsets(20, 10, 20, 2);
				resStat.setText("Reset Stats");
				resStat.setTooltip("Reset the stats for this profile");
				resStat.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(sgui.showConfirmationBox("Reset Stats?"))
							Configs.getProfile(name).add(Configs.stats.get(), new JsonArray());
					}
				});
				sgui.addBut(resStat);
			}
		});
		part.addBut(stngs);
		
		Button del = new Button();
		del.setPos(10, 0);
		del.setSpan(1, 4);
		del.setFill('v');
		del.setText("\uD83D\uDDD1");
		del.setFont(new Font(null, Font.PLAIN, 20));
		del.setTooltip("delete profile");
		del.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("delete " + name + "?")) {
					forget(name);
				}
			}
		});
		part.addBut(del);
		
		both.addContainer(part);
		
		gui.addContainer(both, name + "::part");
	}
	
	private static void setMaxPage(String name, GUI sgui) {
		try {
			int mp = Integer.parseInt(GUI.getInputText(name+"::mp"));
			if(mp < 1)
				throw new NumberFormatException();
			Configs.setInt(name, Configs.maxPage, mp);
		} catch (NumberFormatException e) {
			sgui.msg("Wrong input", "You can't do this", GUI.MsgConst.WARNING);
		}
	}
	
	private static void setUnitPlaceDelay(String name, GUI sgui) {
		try {
			int mp = (int) Math.round(Float.parseFloat(GUI.getInputText(name+"::tupd")) * 1000);
			if(mp < 0)
				throw new NumberFormatException();
			Configs.setInt(name, Configs.unitPlaceDelay, mp);
		} catch (NumberFormatException e) {
			sgui.msg("Wrong input", "You can't do this", GUI.MsgConst.WARNING);
		}
	}
	
	private static void searchCap(GUI parent, String name, int posFav) {
		String dname = GUI.getInputText(name+"::search::cap");
		
		JsonArray caps;
		try {
			caps = profiles.get(name).getSRRH().search(1, 8, false, false, SRC.Search.all, true, dname);
		} catch (NoConnectionException e) {
			Debug.printException(name + " -> MainFrame -> searchCap: err=failed to search captain", e, Debug.runerr, Debug.error, null, null, true);
			return;
		} catch (NullPointerException e) {
			parent.msg("Error", "The Profile has to be\nrunning for this to work", GUI.MsgConst.ERROR);
			return;
		}
		
		JsonObject favs = Configs.getObj(name, Configs.favs);
		
		GUI sea = new GUI("Search Captain", 300, 400, parent, null);
		
		sea.addWinLis(new WinLis() {
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
				parent.remove(name+"::fav::c");
				createFavCon(parent, name, posFav);
				parent.refresh();
			}
		});
		
		for(int i=0; i<caps.size(); i++) {
			JsonObject cap = caps.get(i).getAsJsonObject();
			
			String tdn = cap.getAsJsonPrimitive(SRC.Raid.twitchDisplayName).getAsString();
			
			Label sl = new Label();
			sl.setPos(0, i);
			sl.setText(tdn);
			sea.addLabel(sl);
			
			Button fav = new Button();
			fav.setPos(1, i);
			if(favs.has(tdn)) {
				fav.setText("\u2764");
				fav.setForeground(new Color(227,27,35));
			} else {
				fav.setText("\uD83D\uDC94");
			}
			fav.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(favs.has(tdn)) {
						favs.remove(tdn);
						GUI.setForeground(name+"::search::"+tdn, Color.black);
					} else {
						favs.addProperty(tdn, false);
						GUI.setForeground(name+"::search::"+tdn, new Color(227,27,35));
					}
				}
			});
			sea.addBut(fav, name+"::search::"+tdn);
			
		}
	}
	
	private static void createFavCon(GUI sgui, String name, int y) {
		Container favc = new Container();
		favc.setPos(0, y);
		favc.setInsets(20, 10, 10, 2);
		
			JsonObject favs = Configs.getObj(name, Configs.favs);
			int y1 = 0;
			for(String key : favs.keySet()) {
				Label favl = new Label();
				favl.setPos(0, y1);
				favl.setText(key);
				favl.setFont(new Font(null, Font.PLAIN, 20));
				favc.addLabel(favl, name+"::fav::lab::"+key);
				
				Button ic = new Button();
				ic.setPos(1, y1);
				ic.setText("ic");
				ic.setTooltip("ignore Config");
				if(favs.getAsJsonPrimitive(key).getAsBoolean())
					ic.setBackground(Color.green);
				ic.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(favs.getAsJsonPrimitive(key).getAsBoolean()) {
							favs.addProperty(key, false);
							GUI.setBackground(name+"::fav::ic::"+key, GUI.getDefButCol());
						} else {
							favs.addProperty(key, true);
							GUI.setBackground(name+"::fav::ic::"+key, Color.green);
						}
					}
				});
				favc.addBut(ic, name+"::fav::ic::"+key);
				
				Button favb = new Button();
				favb.setPos(2, y1);
				favb.setText("\u2764");
				favb.setForeground(new Color(227,27,35));
				favb.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						favs.remove(key);
						sgui.remove(name+"::fav::c");
						createFavCon(sgui, name, y);
						sgui.refresh();
					}
				});
				favc.addBut(favb, name+"::fav::heart::"+key);
				
				y1++;
			}
		
		sgui.addContainer(favc, name+"::fav::c");
	}
	
	
	private static void changeTime(GUI sgui, String name) {
		int min;
		int max;
		try {
			min = Integer.parseInt(GUI.getInputText(name+"::time::min"));
			max = Integer.parseInt(GUI.getInputText(name+"::time::max"));
		} catch (NumberFormatException e1) {
			sgui.msg("Update time", "Please enter numbers", GUI.MsgConst.ERROR);
			return;
		}
		if(min > max) {
			sgui.msg("Update time", "max have to be higher than min", GUI.MsgConst.ERROR);
			return;
		}
		
		if(min + max < 500) {
			if(!sgui.showConfirmationBox("lower time between checks\ncan result in bans.\nchange anyway?"))
				return;
		}
		
		Configs.setTime(name, Configs.max, max);
		Configs.setTime(name, Configs.min, min);
	}
	
	private static final Color[] loyCols = new Color[] {null, new Color(192, 137, 112), new Color(192,192,192), new Color(212, 175, 55)};
	
	private static final String[] rewBefore = new String[] {"gold", "token", "potion", "meat"};
	
	
	public static void refresh(boolean reload) {
		try {
			if(reload)
				Configs.load();
			if(gui != null) {
				for(String key : Configs.keySet()) 
					if(!profiles.keySet().contains(key)) 
						add(key, new Run(key, Configs.getStr(key, Configs.cookies)));
				
				gui.refresh();
			}
		} catch (IOException e) {}
	}

	public static void close() {
		try {
			gui.close();
		} catch (Exception e) {}
		
		if(Options.is("no_browser"))
			Browser.dispose();
		
		for(String key : profiles.keySet()) 
			profiles.get(key).setRunning(false);
		
		Configs.save();
		
		Debug.print("System exit", Debug.general, Debug.info, null, null);
		System.exit(0);
	}
	
	
}
