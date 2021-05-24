package program;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Menu;
import include.GUI.TextArea;
import include.GUI.TextField;
import include.GUI.WinLis;
import include.JsonParser;
import include.Version;
import program.SRR.NoInternetException;

public class MainFrame {

	private static GUI gui = null;
	
	public static GUI getGUI() {
		return gui;
	}
	
	private static Hashtable<String, Run> profiles = new Hashtable<>();
	
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
		
		
		String bver = StreamRaiders.get("botVersion");
		
		gui = new GUI("StreamRaider Bot v" + bver, 900, 700);
		
		gui.addWinLis(new WinLis() {
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
				close();
			}
		});
		
			int m = 0;
			Menu bot = new Menu("Bot", "Guide  General  Add a Profile  start all  start all delayed  stop all  skip time all  reload Config".split("  "));
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
					
					Button fm = new Button();
					fm.setPos(0, 0);
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
					s1.setPos(0, 1);
					s1.setSize(1, 20);
					s1.setText("");
					opt.addLabel(s1);
					
					
					Button cai = new Button();
					cai.setPos(0, 2);
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
								System.out.println(rews.toString());
								for(String key : rews.keySet()) {
									System.out.println(key);
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
							
							
							List<String> rewBefore = Arrays.asList(new String[] {"gold", "token", "potion", "meat"});
							
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
					udas.setPos(0, 3);
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
					resStats.setPos(0, 4);
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
					
					
					opt.addContainer(Donators.getContainer(0, 5));
					
					TextArea don = new TextArea();
					don.setPos(0, 6);
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
				@Override
				public void actionPerformed(ActionEvent e) {
					if(gui.showConfirmationBox("<html><center>Settings took since last<br>restart will be removed!<br>reload config file?</center></html>"))
						refresh(true);
				}
			});
			bot.setSep(3);
			bot.setSep(6);
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
					Configs.exportConfig(gui);
					
				}
			});
			config.setAL(m++, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Configs.importConfig(gui);
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
			
			Version.check();
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
		part.addCButton(start);
		
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
					SRRHelper srrh = profiles.get(name).getSRRH();
					if(srrh != null) {
						Raid[] raids = srrh.getRaids();
						if(ii < raids.length) {
							String usi = raids[ii].get(SRC.Raid.userSortIndex);
							if(Configs.isSlotLocked(name, usi)) {
								Configs.setSlotLocked(name, usi, false);
								GUI.setText(name+"::lockBut::"+ii, "\uD83D\uDD13");
								GUI.setBackground(name+"::lockBut::"+ii, GUI.getDefButCol());
							} else {
								Configs.setSlotLocked(name, usi, true);
								GUI.setText(name+"::lockBut::"+ii, "\uD83D\uDD12");
								GUI.setBackground(name+"::lockBut::"+ii, Color.green);
							}
						}
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
						if(ii < raids.length) {
							String disName = new JsonPrimitive(raids[ii].get(SRC.Raid.twitchDisplayName)).getAsString();
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
			});
			part.addBut(fav, name+"::favBut::"+i);
			
			Button map = new Button();
			map.setPos(5, i);
			map.setText("Map");
			map.setTooltip("show map");
			map.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					profiles.get(name).showMap(ii);
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
				
				GUI guir = new GUI("Rewards for " + name, 300, 300, gui, null);
				
				TextArea ta = new TextArea();
				ta.setEditable(false);
				ta.setText(text);
				ta.setPos(0, 0);
				guir.addTextArea(ta);
				
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
				GUI sgui = new GUI("Profile Settings", 900, 800, gui, null);
				
				JsonObject types = Unit.getTypes();
				
				Container units = new Container();
				units.setPos(0, 0);
				
				int x = 0;
				int y = 1;
				for(final String type : types.keySet()) {
					
					int rank = types.getAsJsonObject(type).getAsJsonPrimitive("rank").getAsInt();
					if(rank != y) {
						y = rank;
						x = 0;
					}
					
					Container cimg = new Container();
					Image img = new Image("data/UnitPics/" + type + ".gif");
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
							
							GUI uset = new GUI("Unit settings " + type, 400, 600, sgui, null);
							int y = 0;
							
							Image img = new Image("data/UnitPics/" + type + ".gif");
							img.setPos(0, y++);
							img.setSquare(300);
							uset.addImage(img);
							
							String[] ts = "place upgrade unlock dupe".split(" ");
							
							for(int i=0; i<ts.length; i++) {
								final int ii = i;
								Button place = new Button();
								place.setPos(0, y++);
								place.setText("can " + ts[i]);
								place.setTooltip("Set if the Bot can " + ts[i] + " this Unit");
								place.setFont(new Font(null, Font.PLAIN, 19));
								place.setFill('h');
								if(Configs.getUnitBoolean(name, type, new Configs.B(ts[i])))
									place.setBackground(Color.green);
								place.setAL(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										if(Configs.getUnitBoolean(name, type, new Configs.B(ts[ii]))) {
											Configs.setUnitBoolean(name, type, new Configs.B(ts[ii]), false);
											GUI.setBackground(name + "::" + type + "::uset::" + ts[ii], GUI.getDefButCol());
										} else {
											Configs.setUnitBoolean(name, type, new Configs.B(ts[ii]), true);
											GUI.setBackground(name + "::" + type + "::uset::" + ts[ii], Color.green);
										}
									}
								});
								uset.addBut(place, name + "::" + type + "::uset::" + ts[i]);
							}
							
							if(!Unit.isLegendary(type)) {
								Button bs = new Button();
								bs.setPos(0, y++);
								bs.setText("can buy scrolls");
								bs.setTooltip("Set if Scrolls for this Unit can be bought in the store");
								bs.setFont(new Font(null, Font.PLAIN, 19));
								bs.setFill('h');
								if(Configs.getUnitBoolean(name, type, Configs.buy))
									bs.setBackground(Color.green);
								bs.setAL(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										if(Configs.getUnitBoolean(name, type, Configs.buy)) {
											Configs.setUnitBoolean(name, type, Configs.buy, false);
											GUI.setBackground(name + "::buy::" + type, GUI.getDefButCol());
										} else {
											Configs.setUnitBoolean(name, type, Configs.buy, true);
											GUI.setBackground(name + "::buy::" + type, Color.green);
										}
									}
								});
								uset.addBut(bs, name + "::buy::" + type);
							}
							
							Button spec = new Button();
							spec.setPos(0, y++);
							spec.setText("\u23E3 specialize");
							spec.setFont(new Font(null, Font.PLAIN, 19));
							spec.setFill('h');
							spec.setInsets(2, 2, 20, 2);
							spec.setTooltip("choose a Specialization for this Unit");
							spec.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									final JsonArray uids = JsonParser.parseObj(StreamRaiders.get("specUIDs")).getAsJsonArray(type);
									
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
				

				JsonArray chestTypes = JsonParser.parseArr(StreamRaiders.get("chests"));
				
				Container chests = new Container();
				chests.setPos(0, 1);
				
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
					chest.setTooltip("whitelist/blacklist this chest type");
					chest.setInsets(0, 10, 10, 0);
					if(Configs.getChestBoolean(name, type))
						chest.setBackground(Color.green);
					chest.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(Configs.getChestBoolean(name, type)) {
								Configs.setChestBoolean(name, type, false);
								GUI.setBackground(name + "::" + type, GUI.getDefButCol());
							} else {
								Configs.setChestBoolean(name, type, true);
								GUI.setBackground(name + "::" + type, Color.green);
							}
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
				
				Container minMax = new Container();
				minMax.setPos(0, 2);
				minMax.setInsets(20, 2, 2, 2);
				
				
				for(int i=0; i<2; i++) {
					Label mml = new Label();
					mml.setPos(0, i);
					mml.setText(i==0 ? "Normal Chests max Loyalty:" : "Loyalty Chests min Loyalty:");
					mml.setFont(new Font(null, Font.PLAIN, 25));
					minMax.addLabel(mml);
					
					final int ii = i;
					int ml = Configs.getChestInt(name, i==0 ? Configs.normChestLoyMax : Configs.loyChestLoyMin);

					Container cimg = new Container();
					Image img = new Image("data\\LoyaltyPics\\"+Run.pveloy[ml]+".png");
					img.setSquare(35);
					cimg.addImage(img, name+"::loyImg::"+i);
					
					Button bl = new Button();
					bl.setPos(1, i);
					bl.setContainer(cimg);
					bl.setBackground(loyCols[ml]);
					bl.setTooltip("Loyalty Level");
					bl.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int ml = Configs.getChestInt(name, ii==0 ? Configs.normChestLoyMax : Configs.loyChestLoyMin) + 1;
							if(ml > 3) ml = 1;
							Configs.setChestInt(name, ii==0 ? Configs.normChestLoyMax : Configs.loyChestLoyMin, ml);
							Image img = new Image("data\\LoyaltyPics\\"+Run.pveloy[ml]+".png");
							img.setSquare(35);
							GUI.setImage(name+"::loyImg::"+ii, img);
							GUI.setBackground(name+"::loyMinMax::"+ii, loyCols[ml]);
						}
					});
					minMax.addBut(bl, name+"::loyMinMax::"+i);
					
				}
				
				sgui.addContainer(minMax);
				
				Label bsl = new Label();
				bsl.setPos(0, 3);
				bsl.setInsets(20, 2, 2, 2);
				bsl.setText("Exlude Slots from auto farming:");
				bsl.setFont(new Font(null, Font.PLAIN, 25));
				sgui.addLabel(bsl);
				
				Container cbs = new Container();
				cbs.setPos(0, 4);
				
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
				time.setPos(0, 5);
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
					tbut.setText("update");
					tbut.setPos(4, 1);
					tbut.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							changeTime(sgui, name);
						}
					});
					time.addBut(tbut);
					
				sgui.addContainer(time);
				
				int posFav = 6;
				
				createFavCon(sgui, name, posFav);
				
				Container search = new Container();
				search.setPos(0, 7);
				search.setInsets(20, 10, 10, 2);
				
					TextField stf = new TextField();
					stf.setPos(0, 0);
					stf.setSize(100, 28);
					stf.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							searchCap(sgui, name, posFav);
						}
					});
					search.addTextField(stf, name+"::search::cap");
					
					Button sbut = new Button();
					sbut.setText("search");
					sbut.setPos(1, 0);
					sbut.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							searchCap(sgui, name, posFav);
						}
					});
					search.addBut(sbut);
					
				sgui.addContainer(search);
				
				Button resStat = new Button();
				resStat.setPos(0, 8);
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
	
	private static void searchCap(GUI parent, String name, int posFav) {
		String dname = GUI.getInputText(name+"::search::cap");
		
		JsonArray caps;
		try {
			caps = profiles.get(name).getSRRH().search(1, 8, false, false, true, dname);
		} catch (URISyntaxException | IOException | NoInternetException e) {
			StreamRaiders.log(name + " -> MainFrame -> searchCap: err=failed to search captain", e);
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
		
		Browser.dispose();
		
		for(String key : profiles.keySet()) 
			profiles.get(key).setRunning(false);
		
		Configs.save();
		
		Debug.print("System exit", Debug.general);
		System.exit(0);
	}
	
	
}
