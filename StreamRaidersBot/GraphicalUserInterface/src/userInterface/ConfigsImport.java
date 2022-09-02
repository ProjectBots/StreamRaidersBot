package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.ArrayUtils;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import include.GUI.WinLis;
import program.Configs;
import program.Configs.IllegalConfigTypeException;
import program.Configs.IllegalConfigVersionException;
import program.Configs.Importable;
import program.Logger;
import run.Manager;
import run.ProfileType;

public class ConfigsImport {

	private static GUI gui = null;
	private static Importable im;
	private static String[] profiles;
	
	public static void open(GUI parent) {
		if(gui != null) {
			gui.toFront();
			return;
		}
		
		File file = parent.showFileChooser("Import Config", false, new FileNameExtensionFilter("Json Files", "json"));
		if(file == null)
			return;
		
		try {
			im = new Importable(file.getAbsolutePath());
		} catch (IOException e1) {
			Logger.printException("ConfigsImport -> open: err=Could not create Importable Object", e1, Logger.runerr, Logger.error, null, null, true);
			return;
		} catch (IllegalConfigTypeException | IllegalConfigVersionException e1) {
			Logger.printException("ConfigsImport -> open: err="+e1.getMessage(), e1, Logger.runerr, Logger.error, null, null, true);
			return;
		}
		
		if(im.isCompatibleOldConfig) {
			if(parent.showConfirmationBox("Convert Configlayout?")) {
				im.importAndMerge();
				Manager.loadAllNewProfiles();
			}
			return;
		}
		
		final String uid = UUID.randomUUID().toString()+"::";
		
		gui = new GUI("Import Config", 1200, 900, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("stngs import background"));
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
				profiles = null;
			}
		});
		
		int y = 0;
		
		if(im.hasGlobalOptions()) {
			CButton bg = new CButton(uid + "global");
			bg.setPos(0, y++);
			bg.setText("import global options");
			bg.setForeground(Colors.getColor("stngs import labels"));
			gui.addCheckBox(bg);
		}
		
		//	contents should not be changed later on
		final ArrayList<String> allLayers = new ArrayList<String>() {
			private static final long serialVersionUID = 1L; {
				add("(add layer)");
				for(String ln : im.getLayerNames(ProfileType.VIEWER))
					add(ln+" (VIEWER)");
				for(String ln : im.getLayerNames(ProfileType.CAPTAIN))
					add(ln+" (CAPTAIN)");
			}
		};
		
		//	contents should not be changed later on
		final ArrayList<String> viewerLayers = new ArrayList<String>() {
			private static final long serialVersionUID = 1L; {
				add("(add layer)");
				for(String ln : im.getLayerNames(ProfileType.VIEWER))
					add(ln);
			}
		};
		
		//	contents should not be changed later on
		final String[] lnamesAll = allLayers.toArray(new String[allLayers.size()]);
		final String[] lnamesViewer = viewerLayers.toArray(new String[viewerLayers.size()]);
		
		
		//	contents should not be changed later on
		final HashMap<String, String[][]> overridableLayersAll = new HashMap<String, String[][]>() {
			private static final long serialVersionUID = 1L; {
				for(String cid : Configs.getConfigIds()) {
					ArrayList<String> tmp1 = new ArrayList<>();
					tmp1.add("(override)");
					ArrayList<String> tmp2 = new ArrayList<>();
					tmp2.add("ProjectBots"); //	can be anything, won't be looked up
					for(String lid : Configs.getLayerIds(cid, ProfileType.VIEWER)) {
						tmp1.add(Configs.getStr(cid, lid, Configs.lnameViewer)+" (VIEWER)");
						tmp2.add(lid);
					}
					for(String lid : Configs.getLayerIds(cid, ProfileType.CAPTAIN)) {
						tmp1.add(Configs.getStr(cid, lid, Configs.lnameCaptain)+" (CAPTAIN)");
						tmp2.add(lid);
					}
					put(cid, new String[][] {tmp1.toArray(new String[tmp1.size()]), tmp2.toArray(new String[tmp2.size()])});
				}
			}
		};
		
		
		//	contents should not be changed later on
		final HashMap<String, String[][]> overridableLayersViewer = new HashMap<String, String[][]>() {
			private static final long serialVersionUID = 1L; {
				for(String cid : Configs.getConfigIds()) {
					ArrayList<String> tmp1 = new ArrayList<>();
					tmp1.add("(override)");
					ArrayList<String> tmp2 = new ArrayList<>();
					tmp2.add("ProjectBots"); //	can be anything, won't be looked up
					for(String lid : Configs.getLayerIds(cid, ProfileType.VIEWER)) {
						tmp1.add(Configs.getStr(cid, lid, Configs.lnameViewer));
						tmp2.add(lid);
					}
					put(cid, new String[][] {tmp1.toArray(new String[tmp1.size()]), tmp2.toArray(new String[tmp2.size()])});
				}
			}
		};

		final HashMap<String, Integer> poss = new HashMap<>();
		for(final String cid : Configs.getConfigIds()) {
			poss.put(cid, 0);
			
			final String uce = uid+cid+"::existing::";
			final boolean canCaptain = Configs.getPBoo(cid, Configs.canCaptain);
			
			Container cal = new Container();
			cal.setPos(0, y++);
			
				int x = 0;
			
				Label lpn = new Label();
				lpn.setPos(x++, 0);
				lpn.setText(Configs.getPStr(cid, Configs.pname));
				lpn.setForeground(Colors.getColor("stngs import labels"));
				cal.addLabel(lpn);
				
				CombListener cl = new CombListener() {
					@Override
					public void unselected(String id, ItemEvent e) {}
					@Override
					public void selected(String id, ItemEvent e) {
						//	TODO check if layer name is already taken
						String lay = GUI.getSelected(uce+"cblay");
						String over = GUI.getSelected(uce+"cbover");
						if(lay.equals("(add layer)") || (!over.equals("(override)") && (canCaptain && lay.endsWith(ProfileType.VIEWER.toString()+")") != over.endsWith(ProfileType.VIEWER.toString()+")")))) {
							GUI.setForeground(uce+"badd", Colors.getColor("stngs import buttons disabled"));
							GUI.setGradient(uce+"badd", Colors.getGradient("stngs import buttons disabled"));
						} else {
							GUI.setForeground(uce+"badd", Colors.getColor("stngs import buttons def"));
							GUI.setGradient(uce+"badd", Colors.getGradient("stngs import buttons def"));
						}
					}
				};
				
				ComboBox cblay = new ComboBox(uce+"cblay");
				cblay.setList(canCaptain ? lnamesAll : lnamesViewer);
				cblay.setPos(x++, 0);
				cblay.setCL(cl);
				cal.addComboBox(cblay);
				
				ComboBox cbover = new ComboBox(uce+"cbover");
				cbover.setList((canCaptain ? overridableLayersAll : overridableLayersViewer).get(cid)[0]);
				cbover.setPos(x++, 0);
				cbover.setCL(cl);
				cal.addComboBox(cbover);
				
				TextField tfnewname = new TextField();
				tfnewname.setPos(x++, 0);
				tfnewname.setText("");
				tfnewname.setSize(120, 22);
				cal.addTextField(tfnewname, uce+"tfNewNameLayer");
				
				Button badd = new Button();
				badd.setPos(x++, 0);
				badd.setText("+");
				badd.setGradient(Colors.getGradient("stngs import buttons disabled"));
				badd.setForeground(Colors.getColor("stngs import buttons disabled"));
				badd.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final String lay_ = GUI.getSelected(uce+"cblay");
						final String over_ = GUI.getSelected(uce+"cbover");
						final String new_name = GUI.getInputText(uce+"tfNewNameLayer");
						if(lay_.equals("(add layer)"))
							return;
						
						final ProfileType pt = canCaptain ? (lay_.endsWith(ProfileType.VIEWER.toString()+")") ? ProfileType.VIEWER : ProfileType.CAPTAIN) : ProfileType.VIEWER;
						
						String butText = null;
						
						final String lay = canCaptain ? lay_.substring(0, lay_.lastIndexOf(' ')) : lay_;
						final String over;
						if(over_.equals("(override)")) {
							if(!im.setAddLayer(pt, lay, cid, true, new_name))
								return;
							over = null;
							butText = lay + (new_name.equals("") ? "" : " as " + new_name);
																//	== because both can be captain (false == false)
						} else if(!canCaptain || lay_.endsWith(ProfileType.VIEWER.toString()+")") == over_.endsWith(ProfileType.VIEWER.toString()+")")) {
							String[][] arrs = overridableLayersAll.get(cid);
							String lid = arrs[1][ArrayUtils.indexOf(arrs[0], over_)];
							if(!im.setMergeLayer(pt, lay, cid, lid, true))
								return;
							over = lid;
							butText = lay + " -> " + Configs.getStr(cid, lid, pt == ProfileType.VIEWER ? Configs.lnameViewer : Configs.lnameCaptain);
						} else 
							return;
						
						GUI.setText(uce+"tfNewNameLayer", "");
						
						final int pos = nextPos(poss, cid);
						Container crem = new Container();
						crem.setPos(pos, 0);
						Button brem = new Button();
						brem.setText(butText);
						brem.setGradient(Colors.getGradient("stngs import buttons def"));
						brem.setForeground(Colors.getColor("stngs import buttons def"));
						brem.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if(over == null)
									im.setAddLayer(pt, lay, cid, false, new_name);
								else
									im.setMergeLayer(pt, lay, cid, over, false);
								GUI.removeFromContainer(uce+"remcon", uce+"rem::"+pos);
								gui.refresh();
							}
						});
						crem.addBut(brem);
						gui.addToContainer(uce+"remcon", crem, uce+"rem::"+pos);
						gui.refresh();
					}
				});
				cal.addBut(badd, uce+"badd");
				
				Container crem = new Container();
				crem.setPos(x++, 0);
				cal.addContainer(crem, uce+"remcon");
			
			gui.addContainer(cal);
		}
		
		
		
		poss.put("(add Profiles)", 0);
		final String uce = uid+"::new::";
		
		Container cap = new Container();
		cap.setPos(0, y++);
		
			int x = 0;
			
			TextField tfNewName = new TextField();
			tfNewName.setPos(x++, 0);
			tfNewName.setText("");
			tfNewName.setSize(120, 20);
			cap.addTextField(tfNewName, uce+"tfNewNameProfile");
		
			profiles = new String[im.getProfileCount()+1];
			profiles[0] = "(add Profiles)";
			System.arraycopy(im.getProfileNamesArray(), 0, profiles, 1, im.getProfileCount());
			
			ComboBox cbap = new ComboBox(uce+"cbap");
			cbap.setPos(x++, 0);
			cbap.setList(profiles);
			cbap.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					final String sel = GUI.getSelected(id);
					final String name = GUI.getInputText(uce+"tfNewNameProfile");
					
					im.setAddProfile(sel, true, name);
					
					profiles = ArrayUtils.removeElement(profiles, sel);
					GUI.setCombList(id, profiles);
					GUI.setSelected(id, 0);
					GUI.setText(uce+"tfNewNameProfile", "");
					
					final int pos = nextPos(poss, "(add Profiles)");
					Container crem = new Container();
					crem.setPos(pos, 0);
					Button brem = new Button();
					brem.setText(sel + (name.equals("") ? "" : " as " + name));
					brem.setGradient(Colors.getGradient("stngs import buttons def"));
					brem.setForeground(Colors.getColor("stngs import buttons def"));
					brem.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							profiles = ArrayUtils.insert(1, profiles, sel);
							GUI.setCombList(id, profiles);
							im.setAddProfile(sel, false, null);
							GUI.removeFromContainer(uce+"crem", uce+"rem::"+pos);
						}
					});
					crem.addBut(brem);
					gui.addToContainer(uce+"crem", crem, uce+"rem::"+pos);
					gui.refresh();
				}
			});
			cap.addComboBox(cbap);
			
			Container crem = new Container();
			crem.setPos(x++, 0);
			cap.addContainer(crem, uce+"crem");
			
		gui.addContainer(cap);
		
		Button bim = new Button();
		bim.setPos(0, y++);
		bim.setText("Import as selected");
		bim.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.close();
				im.importAndMerge();
				Manager.loadAllNewProfiles();
			}
		});
		gui.addBut(bim);
	}
	
	private static int nextPos(HashMap<String, Integer> poss, String cid) {
		return poss.put(cid, poss.get(cid)+1);
	}
	
}
