package userInterface.globaloptions;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import include.GUI.WinLis;
import otherlib.Configs;
import run.Manager;
import run.ProfileType;
import userInterface.Colors;
import userInterface.MainFrame;
import userInterface.globaloptions.AbstractOptionWindow.CanNotOpenThisRightNowException;

public class GlobalOptions {

	private final String uid = UUID.randomUUID().toString()+"::";
	
	private static GUI gui = null;
	
	public void open() {
		
		if(gui != null)
			gui.close();
		
		int p = 0;
		
		gui = new GUI("Global Settings", 400, 500, MainFrame.getGUI(), null);
		gui.setBackgroundGradient(Colors.getGradient("stngs global background"));
		
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
				gui = null;
			}
		});
		
		
		Button bumr = new Button();
		bumr.setPos(0, p++);
		bumr.setText("Prevent spiking Memory");
		boolean umr = Configs.getGBoo(Configs.useMemoryReleaser);
		bumr.setGradient(Colors.getGradient("stngs global buttons "+(umr?"on":"def")));
		bumr.setForeground(Colors.getColor("stngs global buttons "+(umr?"on":"def")));
		bumr.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean umr = Configs.getGBoo(Configs.useMemoryReleaser);
				Configs.setGBoo(Configs.useMemoryReleaser, !umr);
				GUI.setGradient(uid+"umr", Colors.getGradient("stngs global buttons "+(umr?"def":"on")));
				GUI.setForeground(uid+"umr", Colors.getColor("stngs global buttons "+(umr?"def":"on")));
			}
		});
		gui.addBut(bumr, uid+"umr");
		
		Button bncc = new Button();
		bncc.setPos(0, p++);
		bncc.setText("Need Close Confirmation");
		boolean ncc = Configs.getGBoo(Configs.needCloseConfirm);
		bncc.setGradient(Colors.getGradient("stngs global buttons "+(ncc?"on":"def")));
		bncc.setForeground(Colors.getColor("stngs global buttons "+(ncc?"on":"def")));
		bncc.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean ncc = Configs.getGBoo(Configs.needCloseConfirm);
				Configs.setGBoo(Configs.needCloseConfirm, !ncc);
				GUI.setGradient(uid+"ncc", Colors.getGradient("stngs global buttons "+(ncc?"def":"on")));
				GUI.setForeground(uid+"ncc", Colors.getColor("stngs global buttons "+(ncc?"def":"on")));
			}
		});
		gui.addBut(bncc, uid+"ncc");
		
		Button brc = new Button();
		brc.setPos(0, p++);
		brc.setText("Redeem Codes");
		brc.setGradient(Colors.getGradient("stngs global buttons def"));
		brc.setForeground(Colors.getColor("stngs global buttons def"));
		brc.setAL(a -> {
			try {
				new RedeemCodes(gui);
			} catch (CanNotOpenThisRightNowException e) {}
		});
		gui.addBut(brc);
		
		Button bcpo = new Button();
		bcpo.setPos(0, p++);
		bcpo.setText("Change Profile Order");
		bcpo.setGradient(Colors.getGradient("stngs global buttons def"));
		bcpo.setForeground(Colors.getColor("stngs global buttons def"));
		bcpo.setAL(a -> new ChangeProfileOrder(gui));
		gui.addBut(bcpo);
		
		
		Button bfumbud = new Button();
		bfumbud.setPos(0, p++);
		bfumbud.setText("free up memory by using drive");
		boolean fumbud = Configs.getGBoo(Configs.freeUpMemoryByUsingDrive);
		bfumbud.setGradient(Colors.getGradient("stngs global buttons "+(fumbud?"on":"def")));
		bfumbud.setForeground(Colors.getColor("stngs global buttons "+(fumbud?"on":"def")));
		bfumbud.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean fumbud = Configs.getGBoo(Configs.freeUpMemoryByUsingDrive);
				Configs.setGBoo(Configs.freeUpMemoryByUsingDrive, !fumbud);
				GUI.setGradient(uid+"fumbud", Colors.getGradient("stngs global buttons "+(fumbud?"def":"on")));
				GUI.setForeground(uid+"fumbud", Colors.getColor("stngs global buttons "+(fumbud?"def":"on")));
			}
		});
		gui.addBut(bfumbud, uid+"fumbud");
		
		Container cmca = new Container();
		cmca.setPos(0, p++);
		
			Label lmca = new Label();
			lmca.setPos(0, 0);
			lmca.setText("max concurrent actions:");
			lmca.setForeground(Colors.getColor("stngs global labels"));
			cmca.addLabel(lmca);
			
			TextField tfmca = new TextField();
			tfmca.setPos(1, 0);
			tfmca.setText(""+Configs.getGInt(Configs.maxProfileActions));
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
						Configs.setGInt(Configs.maxProfileActions, val);
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
			ofc.setGradient(Colors.getGradient("stngs global buttons def"));
			ofc.setForeground(Colors.getColor("stngs global buttons def"));
			ofc.setAL(a -> new ChooseFontFile(gui));
			cff.addBut(ofc);
			
			String ff = Configs.getGStr(Configs.fontFile);
			Label lff = new Label();
			lff.setPos(1, 0);
			lff.setForeground(Colors.getColor("stngs global labels"));
			lff.setText(ff.substring(0, ff.lastIndexOf(".")));
			cff.addLabel(lff, uid+"lff");
		
		gui.addContainer(cff);
		
		Button bbe = new Button();
		bbe.setPos(0, p++);
		bbe.setText("Blocked Errors");
		bbe.setTooltip("Manage blocked errors");
		bbe.setGradient(Colors.getGradient("stngs global buttons def"));
		bbe.setForeground(Colors.getColor("stngs global buttons def"));
		bbe.setAL(a -> new ManageBlockedErrors(gui));
		gui.addBut(bbe);
		
		
		Button resStats = new Button();
		resStats.setPos(0, p++);
		resStats.setText("Reset all Stats");
		resStats.setGradient(Colors.getGradient("stngs global buttons def"));
		resStats.setForeground(Colors.getColor("stngs global buttons def"));
		resStats.setInsets(20, 2, 2, 2);
		resStats.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("Reset all the Stats?")) {
					for(String key : Configs.getConfigIds()) {
						Configs.getProfile(key)
							.getAsJsonObject(ProfileType.VIEWER.toString())
							.remove(Configs.statsViewer.con);
						Configs.getProfile(key)
							.getAsJsonObject(ProfileType.CAPTAIN.toString())
							.remove(Configs.statsCaptain.con);
						Configs.check(key);
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
		fm.setGradient(Colors.getGradient("stngs global buttons def"));
		fm.setForeground(Colors.getColor("stngs global buttons def"));
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
