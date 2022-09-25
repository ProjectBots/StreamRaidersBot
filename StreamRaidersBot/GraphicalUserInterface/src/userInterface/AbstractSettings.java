package userInterface;

import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import include.GUI;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.WinLis;
import otherlib.Configs;
import run.ProfileType;

public abstract class AbstractSettings {
	
	private static Hashtable<String, GUI> open = new Hashtable<>();
	
	protected final String uid = UUID.randomUUID().toString()+"::", cid, lid, fontPath, sn;
	protected final GUI gui;
	protected final ProfileType pt;
	
	protected int g = 0;
	
	protected AbstractSettings(String cid, String lid, GUI parent, int width, int height, boolean skipLayerChooser, boolean skipAddContent) {
		this.cid = cid;
		this.lid = lid;
		this.pt = getProfileType();
		this.sn = getSettingsName();
		
		fontPath = pt.toString() + " stngs " + sn.toLowerCase() + " ";
		
		gui = new GUI(sn + " Settings for " + Configs.getPStr(cid, Configs.pname), width, height, parent, null);
		
		//	close old gui if it exist
		if(open.containsKey(pt.toString()+sn))
			open.get(pt.toString()+sn).close();
		
		gui.setBackgroundGradient(Colors.getGradient(fontPath+"background"));
		
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
				open.remove(pt.toString()+sn);
			}
		});
		
		open.put(pt.toString()+sn, gui);
		
		if(!skipLayerChooser)
			addLayerChooser();
		
		if(!skipAddContent)
			addContent();
		
	}
	

	protected abstract String getSettingsName();
	protected abstract ProfileType getProfileType();
	
	protected abstract void openNewInstance(String lid); 
	
	protected void addLayerChooser() {
		ArrayList<String> listlays = Configs.getLayerIds(cid, ProfileType.VIEWER);
		for(int i=0; i<listlays.size(); i++)
			listlays.set(i, Configs.getStr(cid, listlays.get(i), Configs.lnameViewer));
		
		listlays.add(0, "(all)");
		if(!lid.equals("(all)"))
			putFirstList(listlays, Configs.getStr(cid, lid, Configs.lnameViewer));
		
		Container clay = new Container();
		clay.setPos(0, g++);
		clay.setSpan(3, 1);
		clay.setInsets(20, 2, 20, 2);
		
			Label llay = new Label();
			llay.setPos(0, 0);
			llay.setText("Layer: ");
			llay.setForeground(Colors.getColor(fontPath+"labels"));
			clay.addLabel(llay);
		
			ComboBox cblay = new ComboBox(uid+"cblay");
			cblay.setPos(1, 0);
			cblay.setList(listlays.toArray(new String[listlays.size()]));
			cblay.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String sel = GUI.getSelected(id);
					if(!sel.equals("(all)")) {
						ArrayList<String> lays = Configs.getLayerIds(cid, ProfileType.VIEWER);
						for(String lay : lays) {
							if(sel.equals(Configs.getStr(cid, lay, Configs.lnameViewer))) {
								openNewInstance(lay);
								return;
							}
						}
					} else {
						openNewInstance(sel);
					}
				}
			});
			clay.addComboBox(cblay);
			
		gui.addContainer(clay);
	}
	
	protected abstract void addContent();
	
	private ArrayList<String> putFirstList(ArrayList<String> list, String item) {
		list.remove(item);
		list.add(0, item);
		return list;
	}
}
