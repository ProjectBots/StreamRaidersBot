package bot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButton;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import program.Configs;
import program.Debug;
import include.Json;
import include.NEF;

public class ConfigsGUI {
	
	private static JsonObject configs;

	private static String[] exopts = "cookies unit_place unit_epic unit_upgrade unit_unlock unit_dupe unit_buy unit_spec chests_min chests_max chests_enabled blockedSlots lockedSlots dungeonSlot time maxPage unitPlaceDelay canBuyChest buyEventChest canBuyScrolls storeRefresh favs stats".split(" ");
	
	public static void exportConfig(GUI parent) {
		
		configs = Configs.getConfigs();
		
		GUI gui = new GUI("Export Config", 500, 600, parent, null);
		int y = 0;
		
		
		Label lpros = new Label();
		lpros.setText("Profiles:");
		lpros.setPos(0, y++);
		lpros.setInsets(40, 2, 2, 2);
		gui.addLabel(lpros);
		
		for(String key : configs.keySet()) {
			CButton cb = new CButton("ex::" + key + "::cb");
			cb.setText(key);
			cb.setPos(0, y++);
			gui.addCheckBox(cb);
		}
		

		y = 0;
		
		Label lopts = new Label();
		lopts.setText("Settings:");
		lopts.setPos(1, y++);
		lopts.setInsets(40, 40, 2, 2);
		gui.addLabel(lopts);
		
		for(String key : exopts) {
			CButton cb = new CButton("ex::" + key);
			cb.setText(key);
			cb.setPos(1, y++);
			cb.setInsets(2, 40, 2, 2);
			gui.addCheckBox(cb);
			
			if(!key.equals("cookies"))
				GUI.setCButSelected("ex::" + key, true);
		}
		
		
		Container c = new Container();
		c.setPos(2, 1);
		c.setSpan(1, 3);
		
		Button ex = new Button();
		ex.setText("export selected");
		ex.setPos(0, 0);
		ex.setInsets(2, 20, 2, 2);
		ex.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(GUI.isCButSelected("ex::cookies"))
					if(!gui.showConfirmationBox("You are about to export your cookie values.\nWith these anyone can login into your StreamRaiders Account.\nExport anyway?"))
						return;
				
				saveExportedConfig(gui, false);
				
			}
		});
		c.addBut(ex);
		
		Button exall = new Button();
		exall.setText("export all");
		exall.setPos(0, 1);
		exall.setInsets(2, 20, 2, 2);
		exall.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!gui.showConfirmationBox("You are about to export your cookie values.\nWith these anyone can login into your StreamRaiders Account.\nExport anyway?"))
					return;
				
				saveExportedConfig(gui, true);
			}
		});
		c.addBut(exall);
		
		gui.addContainer(c);
		
		gui.refresh();
		
		
	}
	
	private static String[] units_opt = "place epic upgrade unlock dupe buy spec".split(" ");
	private static String[] chest_opt = "min max enabled".split(" ");
	
	private static void saveExportedConfig(GUI gui, boolean b) {
		
		File file = gui.showFileChooser("Export Config", false, new FileNameExtensionFilter("Json Files", "json"));
		if(file == null)
			return;
		if(!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("json")) 
		    file = new File(file.toString() + ".json");
		
		
		JsonObject ret = new JsonObject();
		for(String key : configs.keySet()) {
			if(!(b || GUI.isCButSelected("ex::" + key + "::cb"))) continue;
			
			JsonObject con = configs.getAsJsonObject(key);
			JsonObject pro = new JsonObject();
			
			if(b || GUI.isCButSelected("ex::cookies")) 
				pro.add("cookies", con.get("cookies"));
			
			JsonObject units = con.getAsJsonObject("units");
			for(int i=0; i<units_opt.length; i++) {
				if(b || GUI.isCButSelected("ex::unit_"+units_opt[i])) {
					for(String u : units.keySet()) {
						pro.add("unit_"+u+"_"+units_opt[i] + (units_opt[i].equals("spec") ? "" : "1"), units.getAsJsonObject(u).get(units_opt[i]));
					}
				}
			}
			
			JsonObject chests = con.getAsJsonObject("chests");
			for(int i=0; i<chest_opt.length; i++) {
				if(b || GUI.isCButSelected("ex::chests_"+chest_opt[i])) {
					for(String c : chests.keySet()) {
						pro.add("chests_"+c+"_"+chest_opt[i], chests.getAsJsonObject(c).get(chest_opt[i]));
					}
				}
			}
			
			for(int i=0; i<2; i++) {
				if(b || GUI.isCButSelected("ex::" + (i==1?"":"b") + "lockedSlots")) {
					JsonObject bss = con.getAsJsonObject((i==1?"":"b") + "lockedSlots");
					for(String bs : bss.keySet()) {
						pro.add((i==1?"":"b") + "lockedSlots_"+bs, bss.get(bs));
					}
				}
			}
			
			if(b || GUI.isCButSelected("ex::dungeonSlot")) {
				pro.add("dungeonSlot", con.get("dungeonSlot"));
			}
			
			if(b || GUI.isCButSelected("ex::time")) {
				JsonObject time = con.getAsJsonObject("time");
				pro.add("time_max", time.get("max"));
				pro.add("time_min", time.get("min"));
			}
			
			
			
			if(b || GUI.isCButSelected("ex::maxPage")) {
				pro.add("maxPage", con.get("maxPage"));
			}
			
			if(b || GUI.isCButSelected("ex::unitPlaceDelay")) {
				pro.add("unitPlaceDelay", con.get("unitPlaceDelay"));
			}
			
			if(b || GUI.isCButSelected("ex::canBuyChest")) {
				pro.add("canBuyChest", con.get("canBuyChest"));
			}
			
			if(b || GUI.isCButSelected("ex::canBuyScrolls")) {
				pro.add("canBuyScrolls", con.get("canBuyScrolls"));
			}
			
			if(b || GUI.isCButSelected("ex::storeRefresh")) {
				pro.addProperty("storeRefresh", con.get("storeRefresh").toString());
			}
			
			if(b || GUI.isCButSelected("ex::favs")) {
				pro.addProperty("favs", con.getAsJsonObject("favs").toString());
			}
			
			if(b || GUI.isCButSelected("ex::stats")) {
				pro.addProperty("stats1", con.get("stats").toString());
			}
			
			ret.add(key, pro);
		}
		
		try {
			NEF.save(file.getAbsolutePath(), Json.prettyJson(ret));
		} catch (IOException e1) {
			Debug.printException("Configs -> export: err=unable to save export", e1, Debug.runerr, Debug.error, null, null, true);
		}
		
		gui.close();
	}
	
	
	private static class InPro {
		private static StringBuilder names = new StringBuilder("(none)");
		public static String[] getNames() {
			return names.toString().split("~~");
		}
		
		private static StringBuilder cnames = new StringBuilder();
		public static String[] getCNames() {
			return cnames.toString().split("~~");
		}
		
		public static void reset() {
			names = new StringBuilder("(none)");
			cnames = new StringBuilder();
		}
		
		private JsonObject content;
		private String name;
		
		public InPro(String name, JsonObject content) {
			this.content = content;
			this.name = name;
			names.append("~~" + name);
			if(content.has("cookies"))
				cnames.append(name + "~~");
		}
		
		public String getName() {
			return name;
		}
		
		public JsonObject getContent() {
			return content;
		}
		
	}

	private static int pos;
	private static int gpos;
	private static GUI gin;
	
	private static String[] pros;
	private static InPro[] inPros;
	
	public static void importConfig(GUI parent) {
		configs = Configs.getConfigs();
		
		InPro.reset();
		JsonObject in;
		try {
			in = Json.parseObj(NEF.read(parent.showFileChooser("Import Config", false, new FileNameExtensionFilter("Json Files", "json")).getAbsolutePath()));
		} catch (NullPointerException e) {
			return;
		} catch (IOException e1) {
			Debug.printException("Configs -> importConfig: err=failed to load config file", e1, Debug.runerr, Debug.error, null, null, true);
			return;
		}
		
		inPros = new InPro[in.size()];
		int i = 0;
		for(String key : in.keySet()) 
			inPros[i++] = new InPro(key, in.getAsJsonObject(key));
		
		pros = configs.keySet().toArray(new String[configs.size()]);
		
		String[] vals = InPro.getNames();
		
		
		gin = new GUI("Import config", 500, 600, parent, null);
		
		for(i=0; i<pros.length; i++) {
			Label l = new Label();
			l.setPos(0, i);
			l.setText(pros[i]);
			gin.addLabel(l);
			
			ComboBox cb = new ComboBox("im::"+pros[i]);
			cb.setPos(1, i);
			cb.setList(vals);
			gin.addComb(cb);
		}
		
		pos = i;
		gpos = i;
		
		genAddRemBut();
		
	}
	
	private static void genAddRemBut() {
		Container c = new Container();
		c.setPos(0, pos);
		c.setSpan(2, 1);
		
		Button add = new Button();
		add.setPos(0, 0);
		add.setText("+");
		add.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shift(true);
			}
		});
		c.addBut(add);
		
		Button rem = new Button();
		rem.setPos(1, 0);
		rem.setText("-");
		rem.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shift(false);
			}
		});
		c.addBut(rem);
		
		Button im = new Button();
		im.setPos(2, 0);
		im.setText("import");
		im.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imports();
			}
		});
		c.addBut(im);
		
		gin.addContainer(c, "im::addrem");
	}

	private static void shift(boolean down) {
		if(down) {
			gin.remove("im::addrem");
			
			TextField ta = new TextField();
			ta.setText("");
			ta.setSize(80, 20);
			ta.setPos(0, pos);
			gin.addTextField(ta, "im::add::"+pos);
			
			ComboBox cb = new ComboBox("im::cb::"+pos);
			cb.setList(InPro.getCNames());
			cb.setPos(1, pos);
			gin.addComb(cb);
			
			pos++;
			genAddRemBut();
			
		} else {
			if(pos == gpos) return;
			gin.remove("im::add::" + --pos);
			gin.remove("im::cb::"+pos);
		}
		
		gin.refresh();
	}
	
	private static void imports() {
		
		for(int i=0; i<pros.length; i++) {
			String sel = GUI.getSelected("im::"+pros[i]);
			if(sel.equals("(none)"))
				continue;
			
			JsonObject imp = null;
			for(int j=0; j<inPros.length; j++) {
				if(!sel.equals(inPros[j].getName()))
					continue;
				imp = inPros[j].getContent();
				break;
			}
			
			if(imp == null) {
				Debug.print("Configs -> imports: imp=null", Debug.runerr, Debug.error, null, null, true);
				continue;
			}
			
			Configs.conMerge(pros[i], imp);
		}
		
		for(int i=gpos; i<pos; i++) {
			String sel = GUI.getSelected("im::cb::"+i);
			
			for(int j=0; j<inPros.length; j++) {
				if(!sel.equals(inPros[j].getName()))
					continue;
				
				String name = GUI.getInputText("im::add::"+i);
				Configs.add(name, inPros[j].getContent().getAsJsonPrimitive("cookies").getAsString());
				Configs.conMerge(name, inPros[j].getContent());
				break;
			}
		}
		
		gin.close();
		MainFrame.refresh(false);
	}
}
