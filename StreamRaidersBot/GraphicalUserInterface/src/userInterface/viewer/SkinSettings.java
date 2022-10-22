package userInterface.viewer;

import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import javax.swing.SwingConstants;


import include.GUI;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.Http.NoConnectionException;
import otherlib.Logger;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skin;
import srlib.skins.Skins;
import srlib.units.Unit;
import srlib.units.UnitType;
import userInterface.AbstractSettings;
import userInterface.Colors;
import run.Manager;
import run.ProfileType;
import run.viewer.ViewerBackEnd;

public class SkinSettings extends AbstractSettings {

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
	protected void openNewInstance(String lid) {
		new SkinSettings(cid, lid, gui);
	}

	
	@Override
	protected void addContent() {
		ViewerBackEnd vbe = Manager.getViewer(cid).getBackEnd();
		
		gui.setGlobalKeyLis(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_R:
						try {
							vbe.updateSkins(true);
							vbe.updateUnits(true);
							
							openNewInstance(lid);
						} catch (NoConnectionException | NotAuthorizedException e1) {
							Logger.printException("SkinSettings -> reload: err=unable to get units/skins", e1, Logger.runerr, Logger.error, cid, null, true);
						}
						return;
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				}
			}
		});
		
		
		
		Unit[] units_;
		Skins skins_;
		try {
			units_ = vbe.getUnits(false);
			skins_ = vbe.getSkins(false);
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("SkinSettings -> open: err=unable to get units/skins", e, Logger.runerr, Logger.error, cid, null, true);
			gui.close();
			return;
		}
		
		Hashtable<UnitType, ArrayList<Unit>> units_all = new Hashtable<>();
		Hashtable<UnitType, ArrayList<Skin>> skins_all = new Hashtable<>();
		Hashtable<UnitType, ArrayList<String>> skinNames_all = new Hashtable<>();
		
		for(UnitType type : UnitType.getTypes()) {
			units_all.put(type, new ArrayList<>());
			skins_all.put(type, new ArrayList<>());
			skinNames_all.put(type, new ArrayList<>());
		}
		
		for(Unit u : units_)
			units_all.get(u.type).add(u);
		
		ArrayList<String> skinUids = skins_.getSkinUids();
		for(String suid : skinUids) {
			Skin s = skins_.getSkin(suid);
			skins_all.get(s.unitType).add(s);
			skinNames_all.get(s.unitType).add(s.disname+" ("+s.type+")");
		}
		
		UnitType[] tps = UnitType.getTypes().toArray(new UnitType[UnitType.getTypes().size()]);
		
		Arrays.sort(tps);
		
		for(UnitType key : tps) {
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
						
						final String gid = uid+u.unitId+"::";
						
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
						String suid = u.getSkin();
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
									Logger.printException("SkinSettings -> open -> selected: err=No Connection", e1, Logger.runerr, Logger.error, cid, null, true);
								}
							}
							
						});
						cu.addComboBox(cbu);
					
					ct.addContainer(cu);
					
				}
				
			gui.addContainer(ct);
		}
		
		gui.refresh();
	}

	
	private static String getExtendedUnitName(Unit u) {
		return concat("<html><center>",
				u.getDisName(),"<br>",
				"Lvl: ",""+u.level," ",u.type.name,"<br>",
				u.specializationDisName,"<br>",
				"(id=",""+u.unitId,")",
				"</center></html>");
		
	}
	
	private static String concat(String... args) {
		StringBuilder sb = new StringBuilder();
		for(String arg : args)
			sb.append(arg);
		return sb.toString();
	}

	
}
