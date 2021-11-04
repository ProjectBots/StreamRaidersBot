package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.WinLis;
import program.ConfigsV2;
import program.SRC;
import program.ConfigsV2.ListType;
import program.Debug;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;

public class CapSearch {

	
	public static final String pre = "CapSearch::";
	
	private String uid = null;
	private GUI gui = null;
	
	public void open(GUI parent, String cid, String lay, ListType list, String search) {
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
		
		gui = new GUI("Captain Search for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 400, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs caps search background"));
		
		Label load = new Label();
		load.setText("Loading...");
		load.setForeground(Fonts.getColor("stngs caps search names"));
		gui.addLabel(load, uid+"load");
		
		// using new Thread because same Thread would block updating frame (not diplaying the Loading...)
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				JsonArray caps;
				try {
					caps = MainFrame.getProfiles().get(cid).getBackEndHandler().searchCap(1, 8, false, false, SRC.Search.all, true, search);
				} catch (NoConnectionException | NotAuthorizedException e) {
					Debug.printException("CapSearch -> open: err=failed to search captain", e, Debug.general, Debug.error, true);
					return;
				}
				
				gui.remove(uid+"load");
				
				if(caps.size() == 0) {
					Label notFound = new Label();
					notFound.setText("Not Found :(");
					notFound.setForeground(Fonts.getColor("stngs caps search names"));
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
						new CaptainSettings().setList(list.get()).open(cid, lay, parent);
						parent.close();
					}
				});
				
				for(int i=0; i<caps.size(); i++) {
					JsonObject cap = caps.get(i).getAsJsonObject();
					
					String tdn = cap.getAsJsonPrimitive(SRC.Raid.twitchDisplayName).getAsString();
					Integer val = ConfigsV2.getCapInt(cid, lay, tdn, list, ConfigsV2.fav);
					
					Label sl = new Label();
					sl.setPos(0, i);
					sl.setText(tdn);
					sl.setForeground(Fonts.getColor("stngs caps search names"));
					gui.addLabel(sl);
					
					Button fav = new Button();
					fav.setPos(1, i);
					fav.setText("\u2764");
					if(val == null || val < 0) {
						fav.setForeground(Fonts.getColor("stngs caps search buttons def"));
						fav.setGradient(Fonts.getGradient("stngs caps search buttons def"));
					} else if(val == 0 || val == Integer.MAX_VALUE-1) {
						fav.setForeground(Fonts.getColor("stngs caps search buttons h_cat"));
						fav.setGradient(Fonts.getGradient("stngs caps search buttons h_cat"));
					} else {
						fav.setForeground(Fonts.getColor("stngs caps search buttons heart"));
						fav.setGradient(Fonts.getGradient("stngs caps search buttons heart"));
					}
					fav.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Integer val = ConfigsV2.getCapInt(cid, lay, tdn, list, ConfigsV2.fav);
							if(val == null || val <= 0 || val == Integer.MAX_VALUE-1) {
								ConfigsV2.favCap(cid, lay, tdn, list, 1);
								GUI.setForeground(uid+"::search::fav::"+tdn, Fonts.getColor("stngs caps search buttons heart"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Fonts.getGradient("stngs caps search buttons heart"));
								GUI.setForeground(uid+"::search::block::"+tdn, Fonts.getColor("stngs caps search buttons def"));
								GUI.setGradient(uid+"::search::block::"+tdn, Fonts.getGradient("stngs caps search buttons def"));
							} else {
								ConfigsV2.favCap(cid, lay, tdn, list, null);
								GUI.setForeground(uid+"::search::fav::"+tdn, Fonts.getColor("stngs caps search buttons def"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Fonts.getGradient("stngs caps search buttons def"));
							}
						}
					});
					gui.addBut(fav, uid+"::search::fav::"+tdn);
					
					
					Button block = new Button();
					block.setPos(2, i);
					block.setText("\u2B59");
					if(val == null || val > 0) {
						block.setForeground(Fonts.getColor("stngs caps search buttons def"));
						block.setGradient(Fonts.getGradient("stngs caps search buttons def"));
					} else if(val == 0 || val == Integer.MIN_VALUE+1) {
						block.setForeground(Fonts.getColor("stngs caps search buttons c_cat"));
						block.setGradient(Fonts.getGradient("stngs caps search buttons c_cat"));
					} else {
						block.setForeground(Fonts.getColor("stngs caps search buttons cross"));
						block.setGradient(Fonts.getGradient("stngs caps search buttons cross"));
					}
					block.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Integer val = ConfigsV2.getCapInt(cid, lay, tdn, list, ConfigsV2.fav);
							if(val == null || val >= 0 || val == Integer.MIN_VALUE+1) {
								ConfigsV2.favCap(cid, lay, tdn, list, -1);
								GUI.setForeground(uid+"::search::block::"+tdn, Fonts.getColor("stngs caps search buttons cross"));
								GUI.setGradient(uid+"::search::block::"+tdn, Fonts.getGradient("stngs caps search buttons cross"));
								GUI.setForeground(uid+"::search::fav::"+tdn, Fonts.getColor("stngs caps search buttons def"));
								GUI.setGradient(uid+"::search::fav::"+tdn, Fonts.getGradient("stngs caps search buttons def"));
							} else {
								ConfigsV2.favCap(cid, lay, tdn, list, null);
								GUI.setForeground(uid+"::search::block::"+tdn, Fonts.getColor("stngs caps search buttons def"));
								GUI.setGradient(uid+"::search::block::"+tdn, Fonts.getGradient("stngs caps search buttons def"));
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
