package userInterface;

import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.SwingConstants;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.WinLis;
import include.Http.NoConnectionException;
import program.ConfigsV2;
import program.Debug;
import program.SRC;
import program.SRR.NotAuthorizedException;
import program.Skins;
import program.Skins.Skin;
import program.Unit;
import run.BackEndHandler;
import run.Manager;

public class SkinSettings {

public static final String pre = "SkinSettings::";
	
	private final String uid, cid, lay;
	
	public SkinSettings(String cid, String lay) {
		this.lay = lay;
		this.cid = cid;
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
	}
	
	public void open(GUI parent) {
		//	own thread bcs window is blocking until closed
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Manager.getProfile(cid).useBackEndHandler(beh -> {
					open(parent, beh);
				});
			}
		});
		t.start();
	}
	
	private boolean closed = false;
	
	private void open(GUI parent, BackEndHandler beh) {
		closed = false;
		
		int p = 0;
		
		GUI gui = new GUI("Skin Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 500, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs skins background"));
		
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
				closed = true;
			}
		});
		
		Unit[] units_;
		Skins skins_;
		try {
			units_ = beh.getUnits(SRC.BackEndHandler.all, false);
			skins_ = beh.getSkins();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Debug.printException("SkinSettings -> open: err=unable to get units/skins", e, Debug.runerr, Debug.error, ConfigsV2.getPStr(cid, ConfigsV2.pname), null, true);
			gui.close();
			return;
		}
		
		Hashtable<String, ArrayList<Unit>> units_all = new Hashtable<>();
		Hashtable<String, ArrayList<Skin>> skins_all = new Hashtable<>();
		Hashtable<String, ArrayList<String>> skinNames_all = new Hashtable<>();
		
		JsonObject types = Unit.getTypes();
		
		for(String key : types.keySet()) {
			units_all.put(key, new ArrayList<>());
			skins_all.put(key, new ArrayList<>());
			skinNames_all.put(key, new ArrayList<>());
		}
		
		for(Unit u : units_)
			units_all.get(u.get(SRC.Unit.unitType)).add(u);
		
		ArrayList<String> skinUids = skins_.getSkinUids();
		for(String suid : skinUids) {
			Skin s = skins_.getSkin(suid);
			skins_all.get(s.unit).add(s);
			skinNames_all.get(s.unit).add(s.disname+" ("+s.type+")");
		}
		
		for(String key : types.keySet()) {
			Container ct = new Container();
			ct.setPos(0, p++);
			
				ArrayList<Unit> units = units_all.get(key);
				ArrayList<Skin> skins = skins_all.get(key);
				ArrayList<String> skinNames = skinNames_all.get(key);
				
				int x = 0;
				for(Unit u : units) {
					Container cu = new Container();
					cu.setPos(x++, 0);
					cu.setInsets(5, 10, 10, 10);
						
						final String gid = uid+u.get(SRC.Unit.unitId)+"::";
						
						Label lu = new Label();
						lu.setPos(0, 0);
						lu.setText(getExtendedUnitName(u));
						lu.setFill('h');
						lu.setHalign(SwingConstants.CENTER);
						lu.setForeground(Fonts.getColor("stngs skins labels"));
						cu.addLabel(lu, gid+"l");

						ArrayList<String> list = new ArrayList<>(skinNames);
						String suid = u.get(SRC.Unit.skin);
						String sn = null;
						if(suid != null) {
							for(Skin s : skins) {
								if(!s.uid.equals(suid))
									continue;
								sn = s.disname+" ("+s.type+")";
								break;
							}
						}
						list.add(0, "(---)");
						if(sn != null) {
							list.remove(sn);
							list.add(0, sn);
						}
						ComboBox cbu = new ComboBox(gid+"cb");
						cbu.setPos(0, 1);
						cbu.setList(list.toArray(new String[list.size()]));
						cbu.setFill('h');
						cbu.setCL(new CombListener() {
							@Override
							public void unselected(String id, ItemEvent e) {}
							@Override
							public void selected(String id, ItemEvent e) {
								String sel = GUI.getSelected(gid+"cb");
								Skin s = null;
								for(Skin s_ : skins) {
									if((s_.disname+" ("+s_.type+")").equals(sel)) {
										s = s_;
										break;
									}
								}
								try {
									String err = beh.equipSkin(u, s);
									if(err != null)
										Debug.print("SkinSettings -> open -> selected: err="+err, Debug.runerr, Debug.error, cid, null, true);
									else
										GUI.setText(gid+"l", getExtendedUnitName(u));
								} catch (NoConnectionException e1) {
									Debug.printException("SkinSettings -> open -> selected: err=No Connection", e1, Debug.runerr, Debug.error, ConfigsV2.getPStr(cid, ConfigsV2.pname), null, true);
								}
							}
							
						});
						cu.addComboBox(cbu);
					
					ct.addContainer(cu);
					
				}
				
			
			gui.addContainer(ct);
		}
		
		gui.refresh();
		
		//	block until window closed
		//	that way beh won't be unloaded until finished
		while(!closed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
		}
		
	}
	
	private static String getExtendedUnitName(Unit u) {
		return    "<html><center>"
				+ u.get(SRC.Unit.disName)+"<br>"
				+ "Lvl: "+u.get(SRC.Unit.level)+" "+u.get(SRC.Unit.unitType)+"<br>"
				+ u.get(SRC.Unit.specializationDisName)+"<br>"
				+ "(id="+u.get(SRC.Unit.unitId)+")"
				+ "</center></html>";
	}
}
