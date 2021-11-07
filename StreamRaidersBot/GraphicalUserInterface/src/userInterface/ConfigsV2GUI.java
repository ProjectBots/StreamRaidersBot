package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import program.ConfigsV2;
import program.ConfigsV2.Exportable;
import program.ConfigsV2.Exportable.Profile;
import program.ConfigsV2.Exportable.Profile.Layer;
import program.Debug;

public class ConfigsV2GUI {

	
	public static final String pre = "ConfigsV2GUI::";
	private final String uid = pre + LocalDateTime.now().toString().hashCode() + "::";
	

	private static Hashtable<String, List<String>> configs = ConfigsV2.ConfigTypes.all;
	
	private GUI gui = null;
	public void exportConfig(GUI parent) {
		//TODO fonts
		
		Hashtable<String, Container> cons = new Hashtable<>();
		
		gui = new GUI("Export Config", 400, 500, parent, null);
		
		
		int p = 0;
		
		Label gname = new Label();
		gname.setPos(0, p);
		gname.setText("Global");
		gui.addLabel(gname);
		
		ComboBox selg = new ComboBox(uid+"global::select");
		selg.setPos(1, p++);
		selg.setList("none all custom".split(" "));
		selg.setCL(new CombListener() {
			@Override
			public void unselected(String id, ItemEvent e) {}
			@Override
			public void selected(String id, ItemEvent e) {
				String sel = GUI.getSelected(id);
				if(sel.equals("custom"))
					gui.addContainer(cons.get(uid), uid+"global::custom");
				else
					try {
						gui.remove(uid+"global::custom");
					} catch (NullPointerException e2) {}
				gui.refresh();
			}
		});
		gui.addComb(selg);
		
		Container cong = new Container();
		cong.setPos(0, p++);
		cong.setSpan(2, 1);
		
			Label space0 = new Label();
			space0.setPos(0, 0);
			space0.setInsets(0, 15, 0, 0);
			space0.setText("");
			cong.addLabel(space0);
			
			int g = 0;
			for(String c : configs.get("Global")) {
				CButton cb = new CButton(uid+"global::"+c);
				cb.setText(c);
				cb.setPos(1, g++);
				cong.addCheckBox(cb);
			}
			
		cons.put(uid, cong);
		
		List<String> cids = ConfigsV2.getCids();
		for(String cid : cids) {
			final String c = uid+cid+"::";
			Label pname = new Label();
			pname.setPos(0, p);
			pname.setText(ConfigsV2.getPStr(cid, ConfigsV2.pname));
			gui.addLabel(pname);
			
			ComboBox select = new ComboBox(c+"select");
			select.setPos(1, p);
			select.setList("none all custom".split(" "));
			select.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String sel = GUI.getSelected(id);
					if(sel.equals("custom"))
						gui.addContainer(cons.get(c), c+"custom");
					else
						try {
							gui.remove(c+"custom");
						} catch (NullPointerException e2) {}
					gui.refresh();
				}
			});
			gui.addComb(select);
			
			Container con = new Container();
			con.setPos(0, p+1);
			con.setSpan(2, 1);
			
			Label space1 = new Label();
			space1.setPos(0, 0);
			space1.setInsets(0, 15, 0, 0);
			space1.setText("");
			con.addLabel(space1);
			
			int l = 0;
			
			for(String pconf : configs.get("Profile")) {
				CButton cbc = new CButton(c+"pconf::"+pconf);
				cbc.setText(pconf);
				cbc.setPos(1, l++);
				con.addCheckBox(cbc);
			}
			
			for(String lid : ConfigsV2.getLayerIds(cid)) {
				final String cl = c+lid+"::";
				
				Label lname = new Label();
				lname.setPos(1, l);
				lname.setText(ConfigsV2.getStr(cid, lid, ConfigsV2.lname));
				con.addLabel(lname);
				
				ComboBox sellay = new ComboBox(cl+"select");
				sellay.setPos(2, l);
				sellay.setList("none all custom".split(" "));
				sellay.setCL(new CombListener() {
					@Override
					public void unselected(String id, ItemEvent e) {}
					@Override
					public void selected(String id, ItemEvent e) {
						String sel = GUI.getSelected(id);
						if(sel.equals("custom"))
							gui.addToContainer(c+"custom", cons.get(cl), cl+"custom");
						else
							try {
								GUI.removeFromContainer(c+"custom", cl+"custom");
							} catch (NullPointerException e2) {}
						gui.refresh();
					}
				});
				con.addComboBox(sellay);
				
				
				Container conlay = new Container();
				conlay.setPos(1, l+1);
				conlay.setSpan(2, 1);
				
					Label space2 = new Label();
					space2.setPos(0, 0);
					space2.setInsets(0, 15, 0, 0);
					space2.setText("");
					conlay.addLabel(space2);
					
					int y = 0;
					
					
					for(String s : configs.get("Layer")) {
						Label lcs = new Label();
						lcs.setPos(1, y++);
						lcs.setText("--"+s+"--");
						conlay.addLabel(lcs);
						
						for(String sc : configs.get(s)) {
							CButton cb = new CButton(cl+s+"::"+sc);
							cb.setText(sc);
							cb.setPos(1, y++);
							conlay.addCheckBox(cb);
						}
						
						Label space3 = new Label();
						space3.setPos(1, y++);
						space3.setText("");
						space3.setInsets(0, 0, 20, 0);
						conlay.addLabel(space3);
					}
					
					
				cons.put(cl, conlay);
				
				l+=2;
			}
			
			cons.put(c, con);
			
			p+=2;
		}
		
		Button exs = new Button();
		exs.setPos(0, p++);
		exs.setFill('h');
		exs.setSpan(2, 1);
		exs.setText("Export Selected");
		exs.setInsets(20, 2, 2, 2);
		exs.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Exportable ex = getExportable();
				if(ex == null)
					return;
				
				String sel = GUI.getSelected(uid+"global::select");
				switch(sel) {
				case "all":
					exGloAll(ex);
					break;
				case "custom":
					for(String c : configs.get("Global"))
						if(GUI.isCButSelected(uid+"global::"+c))
							ex.add(c);
					break;
				}
				
					
				for(String cid : cids) {
					final String c = uid+cid+"::";
					sel = GUI.getSelected(c+"select");
					Profile p;
					switch(sel) {
					case "all":
						p = exProAll(cid);
						break;
					case "custom":
						p = new Profile(cid);
						for(String pconf : configs.get("Profile"))
							if(GUI.isCButSelected(c+"pconf::"+pconf))
								p.add(pconf);
						
						for(String lid : ConfigsV2.getLayerIds(cid)) {
							final String cl = c+lid+"::";
							sel = GUI.getSelected(cl+"select");
							Layer l;
							switch(sel) {
							case "all":
								l = exLayAll(lid);
								break;
							case "custom":
								l = new Layer(lid);
								for(String s : configs.get("Layer")) 
									for(String sc : configs.get(s)) 
										if(GUI.isCButSelected(cl+s+"::"+sc)) 
											l.add(s, sc);
								break;
							default:
								continue;
							}
							if(l.getItems().size() > 0)
								p.add(l);
						}
						break;
					default:
						continue;
					}
					ex.add(p);
				}
				try {
					ConfigsV2.exportConfig(ex);
				} catch (IOException e1) {
					Debug.printException("ConfigsV2GUI -> exportConfig: mode=custom, err=failed to save file", e1, Debug.runerr, Debug.error, null, null, true);
				}
				gui.close();
			}
		});
		gui.addBut(exs);
		
		Button exa = new Button();
		exa.setPos(0, p++);
		exa.setText("Export All");
		exa.setFill('h');
		exa.setSpan(2, 1);
		exa.setInsets(5, 2, 20, 2);
		exa.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Exportable ex = getExportable();
				if(ex == null)
					return;
				exGloAll(ex);
				for(String cid : cids)
					ex.add(exProAll(cid));
				try {
					ConfigsV2.exportConfig(ex);
				} catch (IOException e1) {
					Debug.printException("ConfigsV2GUI -> exportConfig: mode=all, err=failed to save file", e1, Debug.runerr, Debug.error, null, null, true);
				}
				gui.close();
			}
		});
		gui.addBut(exa);
	}
	
	private Exportable getExportable() {
		File file = gui.showFileChooser("Export Config", false, new FileNameExtensionFilter("Json Files", "json"));
		if(file == null)
			return null;
		if(!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("json")) 
		    file = new File(file.toString() + ".json");
		
		return new Exportable(file.getAbsolutePath());
	}
	
	private static void exGloAll(Exportable in) {
		for(String g : configs.get("Global"))
			in.add(g);
	}
	
	private static Profile exProAll(String cid) {
		Profile ret = new Profile(cid);
		for(String pconf : configs.get("Profile"))
			ret.add(pconf);
		for(String lid : ConfigsV2.getLayerIds(cid)) 
			ret.add(exLayAll(lid));
		return ret;
	}
	
	private static Layer exLayAll(String lid) {
		Layer ret = new Layer(lid);
		for(String s : configs.get("Layer"))
			for(String sc : configs.get(s)) 
				ret.add(s, sc);
		return ret;
	}
	
	
	
	public void importConfig(GUI parent) {
		//TODO import
	}
	
}
