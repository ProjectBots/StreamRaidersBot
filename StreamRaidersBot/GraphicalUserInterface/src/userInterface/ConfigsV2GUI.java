package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonObject;

import include.GUI;
import include.Json;
import include.Maths;
import include.NEF;
import include.GUI.Button;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import program.ConfigsV2;
import program.ConfigsV2.Exportable;
import program.ConfigsV2.Exportable.Profile;
import program.ConfigsV2.Exportable.Profile.Layer;
import program.ConfigsV2.Importable;
import program.Debug;

public class ConfigsV2GUI {

	
	public static final String pre = "ConfigsV2GUI::";
	private final String uid = pre + LocalDateTime.now().toString().hashCode() + "::";
	

	private static Hashtable<String, List<String>> configs = ConfigsV2.ConfigTypes.all;
	
	private GUI gui = null;
	public void exportConfig(GUI parent) {
		
		Hashtable<String, Container> cons = new Hashtable<>();
		
		gui = new GUI("Export Config", 400, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs export background"));
		
		int p = 0;
		
		Label gname = new Label();
		gname.setPos(0, p);
		gname.setText("Global");
		gname.setForeground(Fonts.getColor("stngs export labels"));
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
			pname.setForeground(Fonts.getColor("stngs export labels"));
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
				cbc.setForeground(Fonts.getColor("stngs export labels"));
				con.addCheckBox(cbc);
			}
			
			for(String lid : ConfigsV2.getLayerIds(cid)) {
				final String cl = c+lid+"::";
				
				Label lname = new Label();
				lname.setPos(1, l);
				lname.setText(ConfigsV2.getStr(cid, lid, ConfigsV2.lname));
				lname.setForeground(Fonts.getColor("stngs export labels"));
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
						lcs.setForeground(Fonts.getColor("stngs export labels"));
						conlay.addLabel(lcs);
						
						for(String sc : configs.get(s)) {
							CButton cb = new CButton(cl+s+"::"+sc);
							cb.setText(sc);
							cb.setPos(1, y++);
							cb.setForeground(Fonts.getColor("stngs export labels"));
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
		exs.setForeground(Fonts.getColor("stngs export buttons"));
		exs.setGradient(Fonts.getGradient("stngs export buttons"));
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
		exa.setForeground(Fonts.getColor("stngs export buttons"));
		exa.setGradient(Fonts.getGradient("stngs export buttons"));
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
	
	private static class Pair {
		public final String cid;
		public final String name;
		public Pair(String cid, String name) {
			this.cid = cid;
			this.name = name;
		}
	}
	
	private static class Triple {
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof String) {
				String s = (String) obj;
				return s.equals(name);
			}
			return false;
		}
		public final String cid;
		public final String lid;
		public final String name;
		public Triple(String cid, String lid, String name) {
			this.cid = cid;
			this.lid = lid;
			this.name = name;
		}
	}
	
	
	boolean global;
	List<Pair> profiles;
	List<Triple> layers;
	String[] lnames;
	Hashtable<String, List<String>> addLays;
	
	public void importConfig(GUI parent) {
		File file;
		file = parent.showFileChooser("Select Config", false, new FileNameExtensionFilter("Json Files", "json"));
			
		if(file == null)
			return;
		
		JsonObject c;
		try {
			c = Json.parseObj(NEF.read(file.getAbsolutePath()));
		} catch (IOException e) {
			Debug.printException("ConfigsV2 -> importConfig: err=can't read file", e, Debug.runerr, Debug.error, null, null, true);
			return;
		}
		
		if(!c.has("version")) {
			if(parent.showConfirmationBox("Convert Config?"))
				importFromOldClient(c);
			return;
		}
		
		global = c.has("Global");
		profiles = new ArrayList<>();
		layers = new ArrayList<>();
		addLays = new Hashtable<>();
		Hashtable<String, Integer> poss = new Hashtable<>();
		
		for(String cid : c.keySet()) {
			if(cid.equals("Global"))
				continue;
			JsonObject pro = c.getAsJsonObject(cid);
			String name = pro.get("name").getAsString();
			if(pro.has("cookies"))
				profiles.add(new Pair(cid, name));
			
			JsonObject lays = pro.getAsJsonObject("layers");
			
			for(String lid : lays.keySet())
				layers.add(new Triple(cid, lid, name + " - " + lays.getAsJsonObject(lid).get("name").getAsString()));
		}

		int y = 1;
		
		lnames = new String[layers.size()+1];
		lnames[0] = "(add layer)";
		for(Triple t : layers)
			lnames[y++] = t.name;
		
		//TODO import
		y = 0;
		gui = new GUI("Import Config", 400, 500, parent, null);
		
		if(global) {
			CButton bg = new CButton(uid + "global");
			bg.setPos(0, y++);
			bg.setText("import global options");
			gui.addCheckBox(bg);
		}
		
		List<String> cids = ConfigsV2.getCids();
		for(final String cid : cids) {
			poss.put(cid, 2);
			addLays.put(cid, new LinkedList<>());
			
			Container cal = new Container();
			cal.setPos(0, y++);
			
				Label lpn = new Label();
				lpn.setPos(0, 0);
				lpn.setText(ConfigsV2.getPStr(cid, ConfigsV2.pname));
				cal.addLabel(lpn);
				
				ComboBox cbal = new ComboBox(uid+cid+"::add::layer");
				cbal.setList(lnames);
				cbal.setPos(1, 0);
				cbal.setCL(new CombListener() {
					@Override
					public void unselected(String id, ItemEvent e) {}
					@Override
					public void selected(String id, ItemEvent e) {
						final String sel = GUI.getSelected(id);
						GUI.setSelected(id, 0);
						if(sel.equals("(add layer)") || addLays.get(cid).contains(sel))
							return;
						
						addLays.get(cid).add(sel);
						
						int x = poss.get(cid);
						poss.put(cid, x+1);
						
						Container call = new Container();
						call.setPos(x, 0);
							Button bal = new Button();
							bal.setPos(0, 0);
							bal.setText(sel);
							bal.setAL(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									addLays.get(cid).remove(sel);
									GUI.removeFromContainer(uid+cid+"::add::layer::con", uid+cid+"::add::layer::"+sel);
									gui.refresh();
								}
							});
							call.addBut(bal);
						gui.addToContainer(uid+cid+"::add::layer::con", call, uid+cid+"::add::layer::"+sel);
						gui.refresh();
					}
				});
				cal.addComboBox(cbal);
			
			gui.addContainer(cal, uid+cid+"::add::layer::con");
		}
		
		
		Container cnps = new Container();
		cnps.setPos(0, y++);
		
			poss.put("new::profiles", 0);
			
		gui.addContainer(cnps, uid+"container::new::profiles");
		
		Container canp = new Container();
		canp.setPos(0, y++);
			
			List<String> pnames = new ArrayList<>();
			pnames.add("(add Profile)");
			for(Pair p : profiles)
				pnames.add(p.name);
			
			taken = new Hashtable<>();
			
			ComboBox cbanp = new ComboBox(uid+"comb::new::profile");
			cbanp.setPos(0, 0);
			cbanp.setList(pnames.toArray(new String[pnames.size()]));
			cbanp.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String sel = GUI.getSelected(id);
					if(sel.equals("(add Profile)"))
						return;
					pnames.remove(sel);
					GUI.setCombList(id, pnames.toArray(new String[pnames.size()]));
					
					String name = sel;
					if(isPNameTaken(name))
						name = Maths.ranString(5);
					
					taken.put(sel, name);
					
					int y = poss.get("new::profiles");
					poss.put("new::profiles", y+1);
					Container cnp = new Container();
					cnp.setPos(0, y);
					
						Label lon = new Label();
						lon.setPos(0, 0);
						lon.setText(sel);
						cnp.addLabel(lon);
					
						TextField tfsn = new TextField();
						tfsn.setPos(1, 0);
						tfsn.setText(name);
						tfsn.setDocLis(new DocumentListener() {
							@Override
							public void removeUpdate(DocumentEvent e) {
								update();
							}
							@Override
							public void insertUpdate(DocumentEvent e) {
								update();
							}
							@Override
							public void changedUpdate(DocumentEvent e) {
								update();
							}
							private void update() {
								String name = GUI.getInputText(uid+"new::profile::"+sel+"::name");
								taken.put(sel, name);
								GUI.setBackground(uid+"new::profile::"+sel+"::name", isPNameTaken(name) 
												? new Color(255, 125, 125)
												: Color.white);
							}
						});
						cnp.addTextField(tfsn, uid+"new::profile::"+sel+"::name");
						
						Button brnp = new Button();
						brnp.setPos(2, 0);
						brnp.setText("X");
						brnp.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								taken.remove(sel);
								pnames.add(sel);
								GUI.setCombList(id, pnames.toArray(new String[pnames.size()]));
								GUI.removeFromContainer(uid+"container::new::profiles", uid+"new::profile::"+sel);
							}
						});
						cnp.addBut(brnp);
						
					gui.addToContainer(uid+"container::new::profiles", cnp, uid+"new::profile::"+sel);
					gui.refresh();
				}
			});
			canp.addComboBox(cbanp);
		
		gui.addContainer(canp);
		
		Button bi = new Button();
		bi.setPos(0, y++);
		bi.setText("Import now");
		bi.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.close();
				importc(c);
			}
		});
		gui.addBut(bi);
		
		gui.refresh();
	}
	
	private Hashtable<String, String> taken; 
	
	private boolean isPNameTaken(String name) {
		if(ConfigsV2.isPNameTaken(name))
			return true;
		int c = 0;
		for(String key : taken.keySet())
			if(taken.get(key).equals(name))
				c++;
		return c > 1;
	}
	
	private void importc(JsonObject c) {
		//TODO import
		Importable imp = new Importable();
		if(global && GUI.isCButSelected(uid + "global"))
			imp.addGlobal(c.getAsJsonObject("Global").deepCopy());
		
		for(String cid : addLays.keySet()) {
			for(String ln : addLays.get(cid)) {
				Triple t = layers.get(ArrayUtils.indexOf(lnames, ln));
				JsonObject pro = c.getAsJsonObject(t.cid);
				List<String> times = new ArrayList<>();
				if(pro.has("times")) {
					JsonObject ptimes = pro.getAsJsonObject("times");
					for(String key : ptimes.keySet())
						if(ptimes.get(key).getAsString().equals(t.lid))
							times.add(key);
				}
				imp.addLayer(cid, times, pro.getAsJsonObject("layers").getAsJsonObject(t.lid).deepCopy());
			}
		}
		
		for(Pair p : profiles) {
			if(taken.contains(p.name)) {
				JsonObject pro = c.getAsJsonObject(p.cid).deepCopy();
				pro.addProperty(ConfigsV2.pname.get(), taken.get(p.name));
				imp.addProfile(pro);
			}
		}
		
		ConfigsV2.importConfig(imp);
		ConfigsV2.saveb();
		MainFrame.refresh(true);
	}
	
	private void importFromOldClient(JsonObject c) {
		Importable imp = new Importable();
		for(String name : c.keySet()) {
			JsonObject raw = c.getAsJsonObject(name);
			if(!raw.has("cookies"))
				continue;
			if(ConfigsV2.isPNameTaken(name))
				name += "_"+Maths.ranString(3);
			JsonObject pro = new JsonObject();
			pro.add("cookies", raw.get("cookies"));
			pro.addProperty("name", name);
			
			JsonObject defLay = new JsonObject();
			
			
			for(String key : raw.keySet()) {
				String[] args = key.split("_");
				switch(args[0]) {
				case "unit":
					Json.set(defLay, new String[]{"units", args[1], args[2].replace("1", "")}, raw.get(key));
					break;
				case "chests":
					Json.set(defLay, new String[] {"chests", args[1], args[2] + (args[2].equals("enabled") ? "" : "Loy")}, raw.get(key));
					break;
				case "favs":
					JsonObject rfavs = Json.parseObj(raw.get(key).getAsString());
					for(String k : rfavs.keySet()) {
						Json.set(defLay, new String[] {"caps", "campaign", k, "ic"}, rfavs.get(k));
						Json.set(defLay, new String[] {"caps", "dungeon", k, "ic"}, rfavs.get(k));
					}
					break;
				}
			}
			
			Json.set(pro, "layers (default)", defLay);
			
			imp.addProfile(pro);
		}
		
		ConfigsV2.importConfig(imp);
		ConfigsV2.saveb();
		MainFrame.refresh(true);
	}
	
}
