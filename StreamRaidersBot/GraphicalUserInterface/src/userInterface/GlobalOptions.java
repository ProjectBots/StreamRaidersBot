package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import program.ConfigsV2;

public class GlobalOptions {

	
public static final String pre = "GlobalOptions::";
	
	private String uid = null;
	
	public void open() {
		
		uid = pre + LocalDateTime.now().toString().hashCode() + "::";
		
		int p = 0;
		
		GUI gui = new GUI("Global Settings", 400, 500, MainFrame.getGUI(), null);
		
		gui.setBackgroundGradient(Fonts.getGradient("stngs global background"));
		
		Button umr = new Button();
		umr.setPos(0, p++);
		umr.setText("Prevent spiking Memory");
		if(ConfigsV2.getGBoo(ConfigsV2.useMemoryReleaser)) {
			umr.setGradient(Fonts.getGradient("stngs global buttons on"));
			umr.setForeground(Fonts.getColor("stngs global buttons on"));
		} else {
			umr.setGradient(Fonts.getGradient("stngs global buttons def"));
			umr.setForeground(Fonts.getColor("stngs global buttons def"));
		}
			
		umr.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ConfigsV2.getGBoo(ConfigsV2.useMemoryReleaser)) {
					ConfigsV2.setGBoo(ConfigsV2.useMemoryReleaser, false);
					GUI.setGradient(uid+"umr", Fonts.getGradient("stngs global buttons def"));
					GUI.setForeground(uid+"umr", Fonts.getColor("stngs global buttons def"));
				} else {
					ConfigsV2.setGBoo(ConfigsV2.useMemoryReleaser, true);
					GUI.setGradient(uid+"umr", Fonts.getGradient("stngs global buttons on"));
					GUI.setForeground(uid+"umr", Fonts.getColor("stngs global buttons on"));
				}
			}
		});
		gui.addBut(umr, uid+"umr");
		
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
				MainFrame.forgetMe();
			}
		});
		gui.addBut(fm);
	}
}
