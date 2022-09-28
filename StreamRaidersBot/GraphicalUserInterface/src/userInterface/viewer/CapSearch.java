package userInterface.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.WinLis;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Configs.ListType;
import run.Manager;
import srlib.SRC;
import userInterface.Colors;

public class CapSearch {

	
	private final String uid = UUID.randomUUID().toString()+"::", cid, lay;
	private GUI gui = null;
	
	public CapSearch(String cid, String lay) {
		this.lay = lay;
		this.cid = cid;
	}
	
	public void open(GUI parent, ListType list, String search, CaptainSettings capStngs) {
		
		gui = new GUI("Captain Search for " + Configs.getPStr(cid, Configs.pname), 400, 500, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("VIEWER stngs captain search background"));
		
		Label load = new Label();
		load.setText("Loading...");
		load.setForeground(Colors.getColor("VIEWER stngs captain search names"));
		gui.addLabel(load, uid+"load");
		
		// using new Thread because same Thread would block updating frame (not diplaying the Loading...)
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				JsonArray caps = new JsonArray();
				try {
					Manager.getViewer(cid).useBackEnd(beh -> {
						caps.addAll(beh.searchCap(1, null, false, false, SRC.Search.all, true, search));
					});
				} catch (Exception e) {
					Logger.printException("CapSearch -> open: err=failed to search captain", e, Logger.general, Logger.error, null, null, true);
				}
				
				gui.remove(uid+"load");
				
				if(caps.size() == 0) {
					Label notFound = new Label();
					notFound.setText("Not Found :(");
					notFound.setForeground(Colors.getColor("VIEWER stngs captain search names"));
					gui.addLabel(notFound);
					gui.refresh();
					return;
				}

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
				
				for(int i=0; i<caps.size(); i++) {
					JsonObject cap = caps.get(i).getAsJsonObject();
					
					String tdn = cap.get("twitchDisplayName").getAsString();
					Integer val = Configs.getCapInt(cid, lay, tdn, list, Configs.fav);
					
					Label sl = new Label();
					sl.setPos(0, i);
					sl.setText(tdn);
					sl.setForeground(Colors.getColor("VIEWER stngs captain search names"));
					gui.addLabel(sl);
					
					Button fav = new Button();
					fav.setPos(1, i);
					fav.setText("\u2764");
					if(val == null || val < 0) {
						fav.setForeground(Colors.getColor("VIEWER stngs captain search buttons def"));
						fav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons def"));
					} else if(val == 0 || val == Integer.MAX_VALUE-1) {
						fav.setForeground(Colors.getColor("VIEWER stngs captain search buttons h_cat"));
						fav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons h_cat"));
					} else {
						fav.setForeground(Colors.getColor("VIEWER stngs captain search buttons heart"));
						fav.setGradient(Colors.getGradient("VIEWER stngs captain search buttons heart"));
					}
					fav.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Integer val = Configs.getCapInt(cid, lay, tdn, list, Configs.fav);
							if(val == null || val <= 0 || val == Integer.MAX_VALUE-1) {
								Configs.favCap(cid, lay, tdn, list, 1);
								GUI.setForeground(uid+"::search::fav::"+tdn, Colors.getColor("VIEWER stngs captain search buttons heart"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons heart"));
								GUI.setForeground(uid+"::search::block::"+tdn, Colors.getColor("VIEWER stngs captain search buttons def"));
								GUI.setGradient(uid+"::search::block::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons def"));
							} else {
								Configs.favCap(cid, lay, tdn, list, null);
								GUI.setForeground(uid+"::search::fav::"+tdn, Colors.getColor("VIEWER stngs captain search buttons def"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons def"));
							}
						}
					});
					gui.addBut(fav, uid+"::search::fav::"+tdn);
					
					
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
							Integer val = Configs.getCapInt(cid, lay, tdn, list, Configs.fav);
							if(val == null || val >= 0 || val == Integer.MIN_VALUE+1) {
								Configs.favCap(cid, lay, tdn, list, -1);
								GUI.setForeground(uid+"::search::block::"+tdn, Colors.getColor("VIEWER stngs captain search buttons cross"));
								GUI.setGradient(uid+"::search::block::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons cross"));
								GUI.setForeground(uid+"::search::fav::"+tdn, Colors.getColor("VIEWER stngs captain search buttons def"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons def"));
							} else {
								Configs.favCap(cid, lay, tdn, list, null);
								GUI.setForeground(uid+"::search::block::"+tdn, Colors.getColor("VIEWER stngs captain search buttons def"));
								GUI.setGradient(uid+"::search::block::"+tdn, Colors.getGradient("VIEWER stngs captain search buttons def"));
							}
						}
					});
					gui.addBut(block, uid+"::search::block::"+tdn);
					
				}
				gui.refresh();
			}
		});
		t.start();
		
		
	}
	
	
}
