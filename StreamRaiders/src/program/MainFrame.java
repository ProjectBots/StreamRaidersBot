package program;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.TextField;
import include.GUI.WinLis;

public class MainFrame {

	private static final String version = "1.0.0";
	
	private static GUI gui = null;
	
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
		
		
		gui = new GUI("StreamRaider Bot " + version, 500, 700);
		
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
		
		Container head = new Container();
		head.setPos(0, 0);
		head.setFill('h');
		
			Button nextAll = new Button();
			nextAll.setPos(0, 0);
			nextAll.setText("\u23E9");
			nextAll.setFont(new Font(null, Font.PLAIN, 20));
			nextAll.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for(String key : profiles.keySet()) {
						try {
							profiles.get(key).interrupt();
						} catch (Exception e1) {}
					}
				}
			});
			head.addBut(nextAll);
		
			Label s1 = new Label();
			s1.setPos(1, 0);
			s1.setText("");
			s1.setWeightX(1);
			head.addLabel(s1);
			
			Button refresh = new Button();
			refresh.setPos(2, 0);
			refresh.setText("\u27F3");
			refresh.setFont(new Font(null, Font.PLAIN, 20));
			refresh.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refresh();
				}
			});
			head.addBut(refresh);
			
			Button addPro = new Button();
			addPro.setPos(3, 0);
			addPro.setText("+");
			addPro.setFont(new Font(null, Font.PLAIN, 20));
			addPro.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GUI np = new GUI("New Profile", 300, 400);
					
					
					Label lab1 = new Label();
					lab1.setPos(0, 0);
					lab1.setText("Profilename");
					np.addLabel(lab1);
					
					ActionListener openBrowser = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String in = GUI.getInputText("newName");
							if(in == null) return;
							new Browser(in);
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
			head.addBut(addPro);
			
			Button opt = new Button();
			opt.setPos(4, 0);
			opt.setText("\u23E3");
			opt.setFont(new Font(null, Font.PLAIN, 20));
			opt.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GUI opt = new GUI("Options", 400, 500);
					
					Button fm = new Button();
					fm.setPos(0, 0);
					fm.setText("forget me");
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
					
					TextArea don = new TextArea();
					don.setPos(0, 2);
					don.setEditable(false);
					don.setText("Donations:\n"
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
			head.addBut(opt);
			
			gui.addContainer(head);
			
			refresh();
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
		File pro = new File("profiles/" + name + ".app");
		File con = new File("configs/" + name + ".app");
		try {
			pro.delete();
			con.delete();
			gui.remove(name + "::part");
			remove(name);
		} catch (Exception e) {
			gui.msg("Error", "Error while trying to forget:\n" + name, GUI.MsgConst.ERROR);
			e.printStackTrace();
		}
	}
	
	private static int pos = 1;
	
	private static Hashtable<String, Hashtable<String, String>> blacks = new Hashtable<>();
	
	public static Hashtable<String, String> getBlacklist(String name) {
		return blacks.get(name);
	}
	
	private static void addProfile(String name) {
		try {
			blacks.put(name, NEF.getOpt("configs/" + name + ".app"));
		} catch (Exception e) {
			Hashtable<String, String> def = new Hashtable<>();
			
			String[][] types = Unit.getTypes();
			
			for(int i=0; i<types.length-1; i++) {
				for(int j=0; j<types[i].length; j++) {
					def.put(types[i][j], "false");
				}
			}
			for(int i=0; i<types[types.length-1].length; i++) {
				def.put(types[types.length-1][i], "true");
			}
			blacks.put(name, def);
		}
		
		Container both = new Container();
		both.setPos(0, pos++);
		both.setFill('h');
		
		Container top = new Container();
		top.setPos(0, 0);
		
		Label profileName = new Label();
		profileName.setPos(0, 0);
		profileName.setText(name);
		top.addLabel(profileName);
		
		Label s2 = new Label();
		s2.setPos(1, 0);
		s2.setSize(20, 1);
		s2.setText("");
		top.addLabel(s2);
		
		Label counter = new Label();
		counter.setPos(2, 0);
		counter.setText("");
		top.addLabel(counter, name + "::counter");
		
		both.addContainer(top);
		
		Container part = new Container();
		part.setFill('h');
		part.setPos(0, 1);
		
		CButton start = new CButton(name+"::start");
		start.setPos(0, 0);
		start.setSpan(1, 4);
		start.setFill('v');
		start.setText("\u23F5");
		start.setFont(new Font(null, Font.PLAIN, 20));
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
		next.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				profiles.get(name).interrupt();
			}
		});
		part.addBut(next);
		
		
		for(int i=0; i<4; i++) {
			Label name1 = new Label();
			name1.setText(""+(i+1));
			name1.setPos(2, i);
			part.addLabel(name1, name+"::name::"+i);
		}
		
		for(int i=0; i<4; i++) {
			final int ii = i;
			Button map = new Button();
			map.setPos(3, i);
			map.setText("Map");
			map.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					profiles.get(name).showMap(ii);
				}
			});
			part.addBut(map);
		}
		
		Label s1 = new Label();
		s1.setPos(4, 0);
		s1.setSpan(1, 4);
		s1.setText("");
		s1.setWeightX(1);
		s1.setFill('h');
		part.addLabel(s1);
		
		Button seeRews = new Button();
		seeRews.setPos(5, 0);
		seeRews.setSpan(1, 4);
		seeRews.setFill('v');
		seeRews.setText("\u26C1");
		seeRews.setFont(new Font(null, Font.PLAIN, 20));
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
				
				GUI gui = new GUI("Rewards for " + name, 300, 300);
				
				TextArea ta = new TextArea();
				ta.setEditable(false);
				ta.setText(text);
				ta.setPos(0, 0);
				gui.addTextArea(ta);
				
			}
		});
		part.addBut(seeRews);
		
		Button stngs = new Button();
		stngs.setPos(6, 0);
		stngs.setSpan(1, 4);
		stngs.setFill('v');
		stngs.setText("\u23E3");
		stngs.setFont(new Font(null, Font.PLAIN, 20));
		stngs.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI gui = new GUI("Profile Settings", 800, 300);
				
				String[][] types = Unit.getTypes();
				Hashtable<String, String> black = blacks.get(name);
				
				for(int i=0; i<types.length; i++) {
					for(int j=0; j<types[i].length; j++) {
						Button c = new Button();
						c.setPos(j, i);
						c.setText(types[i][j]);
						c.setFill('h');
						if(!Boolean.parseBoolean(black.get(types[i][j]))) {
							c.setBackground(Color.green);
						} else {
							c.setBackground(GUI.getDefButCol());
						}
						final int ii = i;
						final int jj = j;
						c.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if(Boolean.parseBoolean(black.get(types[ii][jj]))) {
									blacks.get(name).put(types[ii][jj], "false");
									GUI.setBackground(name + "::" + types[ii][jj], Color.green);
								} else {
									blacks.get(name).put(types[ii][jj], "true");
									GUI.setBackground(name + "::" + types[ii][jj], GUI.getDefButCol());
								}
							}
						});
						gui.addBut(c, name + "::" + types[i][j]);
					}
				}
			}
		});
		part.addBut(stngs);
		
		Button del = new Button();
		del.setPos(7, 0);
		del.setSpan(1, 4);
		del.setFill('v');
		del.setText("\uD83D\uDDD1");
		del.setFont(new Font(null, Font.PLAIN, 20));
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
	
	private static String[] rewBefore = new String[] {"gold", "token", "potion", "meat"};
	
	private static String[] getAppFiles(String folderpath) {
		class Filter implements FilenameFilter {
			@Override
			public boolean accept(File dir, String name) {
				if(!name.contains(".app")) return false;
				return true;
			}
		}
		
		File folder = new File(folderpath);
		
		return folder.list(new Filter());
	}
	
	public static void refresh() {
		if(gui != null) {
			String[] proFiles = getAppFiles("profiles");
			if(proFiles == null) return;
			for(int i=0; i<proFiles.length; i++) {
				String name = proFiles[i].replace(".app", "");
				if(!profiles.keySet().contains(name)) {
					add(name, new Run(name, NEF.read("profiles/" + proFiles[i]).replace("\n", "; ")));
				}
			}
			gui.refresh();
		}
	}

	public static void close() {
		Set<String> keys = profiles.keySet();
		for(String key : keys) {
			NEF.saveOpt("configs/" + key + ".app", blacks.get(key));
			profiles.get(key).setRunning(false);
		}
		
		try {
			gui.close();
		} catch (Exception e) {}
		
		System.exit(0);
	}
	
	
}
