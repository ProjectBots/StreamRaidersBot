package userInterface.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI;
import include.Json;
import include.GUI.Button;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Container;
import include.GUI.WinLis;
import include.Http.NoConnectionException;
import otherlib.Logger;
import otherlib.Ressources;
import run.Manager;
import run.ProfileType;
import run.viewer.ViewerBackEnd;
import srlib.SRC;
import srlib.SRR.NotAuthorizedException;
import srlib.Store;
import srlib.souls.Soul;
import srlib.souls.SoulType;
import srlib.units.Unit;
import srlib.viewer.Raid;
import userInterface.AbstractSettings;
import userInterface.Colors;

public class SoulSettings extends AbstractSettings {
	
	private boolean closed = false;
	

	protected SoulSettings(String cid, String lid, GUI parent) {
		super(cid, lid, parent, 750, 750, true, false);
	}

	@Override
	protected String getSettingsName() {
		return "Soul";
	}

	@Override
	protected ProfileType getProfileType() {
		return ProfileType.VIEWER;
	}

	@Override
	protected void openNewInstance(String lid) {
		new SoulSettings(cid, lid, gui);
	}

	@Override
	protected void addContent() {
		new Thread(() -> {
			try {
				Manager.getViewer(cid).useBackEnd(vbe -> {
					addContent(vbe);
				});
			} catch (Exception e) {
				Logger.printException("SkinSettings -> open: err=unable to load skin settings", e, Logger.runerr, Logger.error, cid, null, true);
			}
		}).start();
	}

	private void addContent(final ViewerBackEnd vbe) {
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
							vbe.updateUnits(true);
							vbe.updateSouls(true);
							vbe.updateStore(true);
							
							openNewInstance(lid);
						} catch (NoConnectionException | NotAuthorizedException e1) {
							Logger.printException("SoulSettings -> reload: err=unable to get units/souls", e1, Logger.runerr, Logger.error, cid, null, true);
						}
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				}
			}
		});
		
		
		final Unit[] units;
		final Soul[] souls;
		final Raid[] raids;
		final int soulvessel;
		try {
			units = vbe.getUnits(SRC.BackEndHandler.all, false);
			souls = vbe.getSouls(false);
			raids = vbe.getRaids(SRC.BackEndHandler.all);
			soulvessel = vbe.getCurrency(Store.soulvessel, false);
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("SoulSettings -> open: err=unable to get units/souls", e, Logger.runerr, Logger.error, cid, null, true);
			gui.close();
			return;
		}
		

		Arrays.sort(units);
		
		HashSet<Integer> unitsInBattle = new HashSet<>();
		
		for(int i=0; i<raids.length; i++) {
			if(raids[i] == null || raids[i].placementsSerialized == null)
				continue;
			
			JsonArray pser = Json.parseArr(raids[i].placementsSerialized);
			for(int p=0; p<pser.size(); p++) {
				JsonObject place = pser.get(p).getAsJsonObject();
				
				if(!place.get("userId").getAsString().equals(vbe.getViewerUserId()))
					continue;
				
				unitsInBattle.add(place.get("unitId").getAsInt());
			}
		}
		
		final HashMap<SoulType, ArrayList<Soul>> ss = new HashMap<>();
		
		for(SoulType st : SoulType.values())
			ss.put(st, new ArrayList<>());
		
		for(int i=0; i<souls.length; i++)
			ss.get(souls[i].type).add(souls[i]);
		
		
		Container chead = new Container();
		chead.setPos(0, g++);
		chead.setFill('h');
		chead.setInsets(20, 2, 20, 2);
		
			int x=0;

			for(SoulType st : SoulType.values()) {
				Image ist = new Image(Ressources.get("SoulPics/"+st.toString().toLowerCase(), java.awt.Image.class));
				ist.setPos(x++, g);
				ist.setSquare(50);
				ist.setInsets(2, 10, 2, 2);
				chead.addImage(ist);
				
				Label lc = new Label();
				lc.setPos(x++, g);
				lc.setText(": "+ss.get(st).size());
				lc.setForeground(Colors.getColor(fontPath+"labels"));
				chead.addLabel(lc);
			}
			
			Image ist = new Image(Ressources.get("SoulPics/graysoul", java.awt.Image.class));
			ist.setPos(x++, g);
			ist.setSquare(50);
			ist.setInsets(2, 60, 2, 2);
			chead.addImage(ist);
			
			Label lc = new Label();
			lc.setPos(x++, g);
			lc.setText(": "+soulvessel);
			lc.setForeground(Colors.getColor(fontPath+"labels"));
			chead.addLabel(lc);
			
		gui.addContainer(chead);
		
		Container cbody = new Container();
		cbody.setPos(0, g);
		cbody.setFill('h');
			for(int y=0; y<units.length; y++) {
				final int yy = y;
				x = 0;
				
				Image upic = new Image(Ressources.get("UnitPics/"+units[y].type.uid.replace("allies", ""), java.awt.Image.class));
				upic.setPos(x++, y);
				upic.setSquare(25);
				cbody.addImage(upic);
				
				Label luname = new Label();
				luname.setPos(x++, y);
				luname.setText(concat(units[y].specializationDisName != null ? units[y].specializationDisName : units[y].type.name, " "+units[y].level, " ("+units[y].unitId, ")"));
				luname.setForeground(Colors.getColor(fontPath+"labels"));
				cbody.addLabel(luname);
				
				
				
				for(final SoulType st : SoulType.values()) {
					Container cist = new Container();
					ist = new Image(Ressources.get("SoulPics/"+st.toString().toLowerCase(), java.awt.Image.class));
					ist.setSquare(25);
					cist.addImage(ist);
					
					Button bst = new Button();
					bst.setPos(x++, y);
					bst.setContainer(cist);
					if(unitsInBattle.contains(units[y].unitId)) {
						bst.setGradient(Colors.getGradient(fontPath+"buttons disabled_"+(units[y].getSoulType()==st?"on":"def")));
					} else {
						bst.setGradient(Colors.getGradient(fontPath+"buttons "+(units[y].getSoulType()==st?"on":"def")));
						bst.setAL(a -> new Thread(() -> {
							SoulType stb = units[yy].getSoulType();
							if(stb == st) {
								try {
									String err = vbe.equipSoul(units[yy], null);
									if(err != null)
										throw new Exception(err);
	
									GUI.setGradient(uid+yy+"::"+stb.toString(), Colors.getGradient(fontPath+"buttons def"));
								} catch (Exception e2) {
									Logger.printException("SoulSettings -> bst: err=failed to unequip Soul, unit="+units[yy].toString(), e2, Logger.runerr, Logger.error, cid, null, true);
								}
								return;
							}
							
							for(Soul s : ss.get(st)) {
								if(s.getUnitId() != -1)
									continue;
								
								try {
									String err = vbe.equipSoul(units[yy], s);
									if(err != null)
										throw new Exception(err);
									
									GUI.setGradient(uid+yy+"::"+s.type.toString(), Colors.getGradient(fontPath+"buttons on"));
									if(stb != null)
										GUI.setGradient(uid+yy+"::"+stb.toString(), Colors.getGradient(fontPath+"buttons def"));
								} catch (Exception e1) {
									Logger.printException("SoulSettings -> bst: err=failed to equip Soul, unit="+units[yy].toString()+", soul="+s.toString(), e1, Logger.runerr, Logger.error, cid, null, true);
								}
								return;
							}
						}).start());
					}
					cbody.addBut(bst, uid+yy+"::"+st.toString());
					
					
				}
				
				if(units[y].level == 30 && SoulType.parseUnit(units[y].type.uid) != null) {
					Button bex = new Button();
					bex.setPos(x++, y);
					bex.setText("extract soul");
					bex.setInsets(2, 20, 2, 2);
					if(unitsInBattle.contains(units[y].unitId)) {
						bex.setGradient(Colors.getGradient(fontPath+"buttons disabled_def"));
						bex.setForeground(Colors.getColor(fontPath+"buttons disabled_def"));
					} else {
						bex.setGradient(Colors.getGradient(fontPath+"buttons def"));
						bex.setForeground(Colors.getColor(fontPath+"buttons def"));
						bex.setAL(a -> {
							if(!gui.showConfirmationBox("Are you sure to sacrifice this unit\nfor a "+SoulType.parseUnit(units[yy].type.uid).title+"?"))
								return;
							try {
								String err = vbe.extractSoul(units[yy]);
								if(err != null) {
									switch(err) {
									case "not enough soulvessel":
										gui.msg("Error Occured", "Not enough Soulvessels, needs 1", GUI.MsgConst.ERROR);
										return;
									case "not enough gold":
										gui.msg("Error Occured", "Not enough Gold, needs 2000", GUI.MsgConst.ERROR);
										return;
									default:
										throw new Exception(err);
									}
								}
								openNewInstance(lid);
							} catch (Exception e1) {
								Logger.printException("SoulSetings -> bex: err=failed to extract soul, unit="+units[yy].toString(), e1, Logger.runerr, Logger.error, cid, null, true);
							}
						});
					}
					cbody.addBut(bex, uid+"bex::"+y);
				}
			}
		gui.addContainer(cbody);
		
		gui.refresh();
		
		//	block until window closed
		//	that way vbe won't be unloaded until finished
		while(!closed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
		}
	}

	
	private static String concat(String... args) {
		StringBuilder sb = new StringBuilder();
		for(String arg : args)
			sb.append(arg);
		return sb.toString();
	}

}
