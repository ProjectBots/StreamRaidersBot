package userInterface.viewer;

import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.SwingConstants;


import include.GUI;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.WinLis;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import srlib.SRC;
import srlib.Unit;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skin;
import srlib.skins.Skins;
import userInterface.AbstractSettings;
import userInterface.Colors;
import run.Manager;
import run.ProfileType;
import run.viewer.ViewerBackEnd;

public class SkinSettings extends AbstractSettings {

	private boolean closed = false;

	protected SkinSettings(String cid, String lid, GUI parent) {
		super(cid, lid, parent, 500, 500, true, false);
	}
	
	@Override
	protected String getSettingsName() {
		return "Skin";
	}
	
	@Override
	protected ProfileType getProfileType() {
		return ProfileType.VIEWER;
	}

	@Override
	protected AbstractSettings getNewInstance(String lid) {
		return new SkinSettings(cid, lid, gui);
	}

	@Override
	protected void addContent() {
		//	own thread bcs window is blocking until closed
		Thread t = new Thread(() -> {
			try {
				Manager.getViewer(cid).useBackEnd(vbe -> {
					addContent(vbe);
				});
			} catch (Exception e) {
				Logger.printException("SkinSettings -> open: err=unable to load skin settings", e, Logger.runerr, Logger.error, cid, null, true);
			}
		});
		t.start();
	}
	
	private void addContent(ViewerBackEnd vbe) {
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
			units_ = vbe.getUnits(SRC.BackEndHandler.all, false);
			skins_ = vbe.getSkins();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("SkinSettings -> open: err=unable to get units/skins", e, Logger.runerr, Logger.error, Configs.getPStr(cid, Configs.pname), null, true);
			gui.close();
			return;
		}
		
		Hashtable<String, ArrayList<Unit>> units_all = new Hashtable<>();
		Hashtable<String, ArrayList<Skin>> skins_all = new Hashtable<>();
		Hashtable<String, ArrayList<String>> skinNames_all = new Hashtable<>();
		
		ArrayList<String> types = Unit.getTypesList();
		
		for(String key : types) {
			units_all.put(key, new ArrayList<>());
			skins_all.put(key, new ArrayList<>());
			skinNames_all.put(key, new ArrayList<>());
		}
		
		for(Unit u : units_)
			units_all.get(u.unitType).add(u);
		
		ArrayList<String> skinUids = skins_.getSkinUids();
		for(String suid : skinUids) {
			Skin s = skins_.getSkin(suid);
			skins_all.get(s.unit).add(s);
			skinNames_all.get(s.unit).add(s.disname+" ("+s.type+")");
		}
		
		for(String key : types) {
			Container ct = new Container();
			ct.setPos(0, g++);
			
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
						lu.setForeground(Colors.getColor(fontPath+"labels"));
						cu.addLabel(lu, gid+"l");

						/* TODO	optimize
						 * 	skins are not unit specific, but type specific
						 * 	the lists for each type could be loaded beforehand
						 * 	therefore only the selected skin nedds to be moved in the array
						 */
						
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
									String err = vbe.equipSkin(u, s);
									if(err != null)
										Logger.print("SkinSettings -> open -> selected: err="+err, Logger.runerr, Logger.error, cid, null, true);
									else
										GUI.setText(gid+"l", getExtendedUnitName(u));
								} catch (NoConnectionException e1) {
									Logger.printException("SkinSettings -> open -> selected: err=No Connection", e1, Logger.runerr, Logger.error, Configs.getPStr(cid, Configs.pname), null, true);
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
		//	that way vbe won't be unloaded until finished
		while(!closed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
		}
	}

	
	private static String getExtendedUnitName(Unit u) {
		return concat("<html><center>",
				u.get(SRC.Unit.disName),"<br>",
				"Lvl: ",u.get(SRC.Unit.level)," ",u.unitType,"<br>",
				u.get(SRC.Unit.specializationDisName),"<br>",
				"(id=",u.get(SRC.Unit.unitId),")",
				"</center></html>");
		
	}
	
	private static String concat(String... args) {
		StringBuilder sb = new StringBuilder();
		for(String arg : args)
			sb.append(arg);
		return sb.toString();
	}

	
}
