package userInterface;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import include.GUI;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import program.Configs;
import run.ProfileType;

public abstract class AbstractSettings {
	
	public static class AlreadyOpenException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	private static Hashtable<String, GUI> open = new Hashtable<>();
	
	protected final String uid = UUID.randomUUID().toString()+"::", cid, lid, fontPath;
	protected final GUI gui;
	
	protected int g = 0;
	
	protected AbstractSettings(ProfileType pt, String cid, String lid, GUI parent, int width, int height, boolean skipLayerChooser, boolean skipAddContent) throws AlreadyOpenException {
		this.cid = cid;
		this.lid = lid;
		
		String sn = getSettingsName();
		
		if(open.contains(sn)) {
			open.get(sn).toFront();
			throw new AlreadyOpenException();
		}
		
		fontPath = pt.toString() + " stngs " + sn.toLowerCase() + " ";
		
		gui = new GUI(sn + " Settings for " + Configs.getPStr(cid, Configs.pname), width, height, parent, null);
		gui.setBackgroundGradient(Colors.getGradient(fontPath+"background"));
		
		open.put(sn, gui);
		
		if(!skipLayerChooser)
			addLayerChooser();
		
		if(!skipAddContent)
			addContent();
		
	}
	
	protected abstract String getSettingsName();
	
	protected abstract AbstractSettings getNewInstance(String lid) throws AlreadyOpenException; 
	
	public void openNewInstance(String lid) {
		open.remove(getSettingsName());
		try {
			getNewInstance(lid);
		} catch (AlreadyOpenException e) {}
		gui.close();
	}
	
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
