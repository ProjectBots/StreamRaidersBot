package userInterface.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.UUID;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.WinLis;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Configs.ListType;
import run.Manager;
import srlib.viewer.CaptainData;
import userInterface.Colors;

public class CapSearch {

	
	private final String uid = UUID.randomUUID().toString()+"::", cid, lay;
	private GUI gui = null;
	
	public CapSearch(String cid, String lay) {
		this.lay = lay;
		this.cid = cid;
	}
	
	public void open(GUI parent, ListType list, String search, boolean fav, CaptainSettings capStngs) {
		
		gui = new GUI("Captain Search for " + Configs.getPStr(cid, Configs.pname), 400, 500, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("VIEWER stngs captain search background"));
		
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
				capStngs.openNewInstance(lay);
			}
		});
		
		Label load = new Label();
		load.setText("Loading...");
		load.setForeground(Colors.getColor("VIEWER stngs captain search names"));
		gui.addLabel(load, uid+"load");
		
		new Thread(() -> {
			CaptainData[] caps;
			try {
				caps = Manager.getViewer(cid).getBackEnd().searchCaptains(fav, false, null, null, true, search, 5);
			} catch (Exception e) {
				Logger.printException("CapSearch -> open: err=failed to search captain", e, Logger.general, Logger.error, null, null, true);
				gui.close();
				return;
			}
			
			gui.remove(uid+"load");
			
			if(caps.length == 0) {
				Label notFound = new Label();
				notFound.setText("Not Found :(");
				notFound.setForeground(Colors.getColor("VIEWER stngs captain search names"));
				gui.addLabel(notFound);
				gui.refresh();
				return;
			}
			
			for(int i=0; i<caps.length; i++) {
				
				final String tun = caps[i].twitchUserName;
				Integer val = Configs.getCapInt(cid, lay, tun, list, Configs.fav);
				
				Label sl = new Label();
				sl.setPos(0, i);
				sl.setText(caps[i].twitchDisplayName+" ("+tun+")");
				sl.setForeground(Colors.getColor("VIEWER stngs captain search names"));
				gui.addLabel(sl);
				
				Button bfav = new Button();
				bfav.setPos(1, i);
				bfav.setText("\u2764");
				if(val == null || val < 0) {
					bfav.setForeground(Colors.getColor("VIEWER stngs captain search buttons def"));
					bfav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons def"));
				} else if(val == 0 || val == Integer.MAX_VALUE-1) {
					bfav.setForeground(Colors.getColor("VIEWER stngs captain search buttons h_cat"));
					bfav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons h_cat"));
				} else {
					bfav.setForeground(Colors.getColor("VIEWER stngs captain search buttons heart"));
					bfav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons heart"));
				}
				bfav.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer val = Configs.getCapInt(cid, lay, tun, list, Configs.fav);
						if(val == null || val <= 0 || val == Integer.MAX_VALUE-1) {
							Configs.favCap(cid, lay, tun, list, 1);
							GUI.setForeground(uid+"::search::fav::"+tun, Colors.getColor("VIEWER stngs captain search buttons heart"));
							GUI.setGradient(uid+"::search::fav::"+tun, Colors.getGradient("VIEWER stngs captain search buttons heart"));
							GUI.setForeground(uid+"::search::block::"+tun, Colors.getColor("VIEWER stngs captain search buttons def"));
							GUI.setGradient(uid+"::search::block::"+tun, Colors.getGradient("VIEWER stngs captain search buttons def"));
						} else {
							Configs.favCap(cid, lay, tun, list, null);
							GUI.setForeground(uid+"::search::fav::"+tun, Colors.getColor("VIEWER stngs captain search buttons def"));
							GUI.setGradient(uid+"::search::fav::"+tun, Colors.getGradient("VIEWER stngs captain search buttons def"));
						}
					}
				});
				gui.addBut(bfav, uid+"::search::fav::"+tun);
				
				
				Button block = new Button();
				block.setPos(2, i);
				block.setText("\u2B59");
				if(val == null || val > 0) {
					block.setForeground(Colors.getColor("VIEWER stngs captain search buttons def"));
					block.setGradient(Colors.getGradient("VIEWER stngs captain search buttons def"));
				} else if(val == 0 || val == Integer.MIN_VALUE+1) {
					block.setForeground(Colors.getColor("VIEWER stngs captain search buttons c_cat"));
					block.setGradient(Colors.getGradient("VIEWER stngs captain search buttons c_cat"));
				} else {
					block.setForeground(Colors.getColor("VIEWER stngs captain search buttons cross"));
					block.setGradient(Colors.getGradient("VIEWER stngs captain search buttons cross"));
				}
				block.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer val = Configs.getCapInt(cid, lay, tun, list, Configs.fav);
						if(val == null || val >= 0 || val == Integer.MIN_VALUE+1) {
							Configs.favCap(cid, lay, tun, list, -1);
							GUI.setForeground(uid+"::search::block::"+tun, Colors.getColor("VIEWER stngs captain search buttons cross"));
							GUI.setGradient(uid+"::search::block::"+tun, Colors.getGradient("VIEWER stngs captain search buttons cross"));
							GUI.setForeground(uid+"::search::fav::"+tun, Colors.getColor("VIEWER stngs captain search buttons def"));
							GUI.setGradient(uid+"::search::fav::"+tun, Colors.getGradient("VIEWER stngs captain search buttons def"));
						} else {
							Configs.favCap(cid, lay, tun, list, null);
							GUI.setForeground(uid+"::search::block::"+tun, Colors.getColor("VIEWER stngs captain search buttons def"));
							GUI.setGradient(uid+"::search::block::"+tun, Colors.getGradient("VIEWER stngs captain search buttons def"));
						}
					}
				});
				gui.addBut(block, uid+"::search::block::"+tun);
				
			}
			gui.refresh();
		}).start();
		
		
	}
	
	
}
