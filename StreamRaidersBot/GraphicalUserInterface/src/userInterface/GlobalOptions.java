package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import program.ConfigsV2;
import run.Manager;

public class GlobalOptions {

	
	public static final String pre = "GlobalOptions::";
	
	private final String uid = pre + LocalDateTime.now().toString().hashCode() + "::";
	
	public void open() {
		
		int p = 0;
		
		GUI gui = new GUI("Global Settings", 400, 500, MainFrame.getGUI(), null);
		
		gui.setBackgroundGradient(Fonts.getGradient("stngs global background"));
		
		Button bumr = new Button();
		bumr.setPos(0, p++);
		bumr.setText("Prevent spiking Memory");
		boolean umr = ConfigsV2.getGBoo(ConfigsV2.useMemoryReleaser);
		bumr.setGradient(Fonts.getGradient("stngs global buttons "+(umr?"on":"def")));
		bumr.setForeground(Fonts.getColor("stngs global buttons "+(umr?"on":"def")));
		bumr.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean umr = ConfigsV2.getGBoo(ConfigsV2.useMemoryReleaser);
				ConfigsV2.setGBoo(ConfigsV2.useMemoryReleaser, !umr);
				GUI.setGradient(uid+"umr", Fonts.getGradient("stngs global buttons "+(umr?"def":"on")));
				GUI.setForeground(uid+"umr", Fonts.getColor("stngs global buttons "+(umr?"def":"on")));
			}
		});
		gui.addBut(bumr, uid+"umr");
		
		Button bncc = new Button();
		bncc.setPos(0, p++);
		bncc.setText("Need Close Confirmation");
		boolean ncc = ConfigsV2.getGBoo(ConfigsV2.needCloseConfirm);
		bncc.setGradient(Fonts.getGradient("stngs global buttons "+(ncc?"on":"def")));
		bncc.setForeground(Fonts.getColor("stngs global buttons "+(ncc?"on":"def")));
		bncc.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean ncc = ConfigsV2.getGBoo(ConfigsV2.needCloseConfirm);
				ConfigsV2.setGBoo(ConfigsV2.needCloseConfirm, !ncc);
				GUI.setGradient(uid+"ncc", Fonts.getGradient("stngs global buttons "+(ncc?"def":"on")));
				GUI.setForeground(uid+"ncc", Fonts.getColor("stngs global buttons "+(ncc?"def":"on")));
			}
		});
		gui.addBut(bncc, uid+"ncc");
		
		
		Button bfumbud = new Button();
		bfumbud.setPos(0, p++);
		bfumbud.setText("free up memory by using drive");
		boolean fumbud = ConfigsV2.getGBoo(ConfigsV2.freeUpMemoryByUsingDrive);
		bfumbud.setGradient(Fonts.getGradient("stngs global buttons "+(fumbud?"on":"def")));
		bfumbud.setForeground(Fonts.getColor("stngs global buttons "+(fumbud?"on":"def")));
		bfumbud.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean fumbud = ConfigsV2.getGBoo(ConfigsV2.freeUpMemoryByUsingDrive);
				ConfigsV2.setGBoo(ConfigsV2.freeUpMemoryByUsingDrive, !fumbud);
				GUI.setGradient(uid+"ncc", Fonts.getGradient("stngs global buttons "+(fumbud?"def":"on")));
				GUI.setForeground(uid+"ncc", Fonts.getColor("stngs global buttons "+(fumbud?"def":"on")));
			}
		});
		gui.addBut(bfumbud, uid+"ncc");
		
		Container cmca = new Container();
		cmca.setPos(0, p++);
		
			Label lmca = new Label();
			lmca.setPos(0, 0);
			lmca.setText("max concurrent actions:");
			lmca.setForeground(Fonts.getColor("stngs global labels"));
			cmca.addLabel(lmca);
			
			TextField tfmca = new TextField();
			tfmca.setPos(1, 0);
			tfmca.setText(""+ConfigsV2.getGInt(ConfigsV2.maxProfileActions));
			tfmca.setSize(80, 22);
			tfmca.setDocLis(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					update();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}
				public void update() {
					try {
						int val = Integer.parseInt(GUI.getInputText(uid+"maxactions"));
						ConfigsV2.setGInt(ConfigsV2.maxProfileActions, val);
						Manager.setMaxConcurrentActions(val);
						GUI.setBackground(uid+"maxactions", Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+"maxactions", new Color(255, 125, 125));
					}
				}
			});
			cmca.addTextField(tfmca, uid+"maxactions");
		
		gui.addContainer(cmca);
		
		Container cff = new Container();
		cff.setPos(0, p++);
		
			Button ofc = new Button();
			ofc.setPos(0, 0);
			ofc.setText("choose Font");
			ofc.setGradient(Fonts.getGradient("stngs global buttons def"));
			ofc.setForeground(Fonts.getColor("stngs global buttons def"));
			ofc.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File dir = new File("data/Fonts");
					String[] files = dir.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".json");
						}
					});
					
					GUI fc = new GUI("Choose Font File", 400, 500, gui, null);
					fc.setBackgroundGradient(Fonts.getGradient("stngs global chooser background"));
					int i = 0;
					for(String file : files) {
						String name = file.substring(0, file.lastIndexOf("."));
						Button bf = new Button();
						bf.setPos(0, i++);
						bf.setText(name);
						bf.setFill('h');
						if(file.equals("default.json") || file.equals(ConfigsV2.getGStr(ConfigsV2.fontFile))) {
							bf.setGradient(Fonts.getGradient("stngs global chooser buttons on"));
							bf.setForeground(Fonts.getColor("stngs global chooser buttons on"));
						} else {
							bf.setGradient(Fonts.getGradient("stngs global chooser buttons def"));
							bf.setForeground(Fonts.getColor("stngs global chooser buttons def"));
						}
						bf.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								ConfigsV2.setGStr(ConfigsV2.fontFile, file);
								GUI.setText(uid+"lff", name);
								fc.close();
							}
						});
						fc.addBut(bf);
					}
				}
			});
			cff.addBut(ofc);
			
			String ff = ConfigsV2.getGStr(ConfigsV2.fontFile);
			Label lff = new Label();
			lff.setPos(1, 0);
			lff.setForeground(Fonts.getColor("stngs global labels"));
			lff.setText(ff.substring(0, ff.lastIndexOf(".")));
			cff.addLabel(lff, uid+"lff");
		
		gui.addContainer(cff);
		
		Button bbe = new Button();
		bbe.setPos(0, p++);
		bbe.setText("Blocked Errors");
		bbe.setTooltip("Manage blocked errors");
		bbe.setGradient(Fonts.getGradient("stngs global buttons def"));
		bbe.setForeground(Fonts.getColor("stngs global buttons def"));
		bbe.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				GUI err = new GUI("Blocked Errors", 400, 500, gui, null);
				
				
				List<String> blocked = new ArrayList<>(Arrays.asList(ConfigsV2.getGStr(ConfigsV2.blocked_errors).split("\\|")));
				if(blocked.get(0).equals("")) {
					Label lnts = new Label();
					lnts.setText("Nothing to show :(");
					err.addLabel(lnts);
					return;
				}
				int y = 0;
				for(String s : blocked) {
					final String eid = uid + LocalDateTime.now().toString().hashCode() + "::" + s;
					
					Button b = new Button();
					b.setPos(0, y++);
					b.setText(s);
					b.setFill('h');
					b.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							err.remove(eid);
							blocked.remove(s);
							if(blocked.size() == 0)
								err.close();
							ConfigsV2.setGStr(ConfigsV2.blocked_errors, String.join("|", blocked));
						}
					});
					err.addBut(b, eid);
				}
			}
		});
		gui.addBut(bbe);
		
		
		Button resStats = new Button();
		resStats.setPos(0, p++);
		resStats.setText("Reset all Stats");
		resStats.setGradient(Fonts.getGradient("stngs global buttons def"));
		resStats.setForeground(Fonts.getColor("stngs global buttons def"));
		resStats.setInsets(20, 2, 2, 2);
		resStats.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("Reset all the Stats?")) {
					for(String key : ConfigsV2.getCids()) {
						ConfigsV2.getProfile(key).remove(ConfigsV2.stats.get());
						ConfigsV2.check(key);
					}
				}
			}
		});
		gui.addBut(resStats);
		
		Button fm = new Button();
		fm.setPos(0, p++);
		fm.setText("forget me");
		fm.setTooltip("deletes all profiles");
		fm.setInsets(20, 2, 20, 2);
		fm.setGradient(Fonts.getGradient("stngs global buttons def"));
		fm.setForeground(Fonts.getColor("stngs global buttons def"));
		fm.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("do you really want\nto delete all your\nprofiles?")) {
					HashSet<String> cids = Manager.getLoadedProfiles();
					for(String cid : cids)
						Manager.remProfile(cid);
				}
			}
		});
		gui.addBut(fm);
	}
}
