package program;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButton;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import include.JsonParser;
import include.NEF;

public class Configs {

	private static final String path = "data\\configs.json";
	

	private static JsonObject configs = null;
	
	
	public static JsonObject getProfile(String name) {
		return configs.getAsJsonObject(name);
	}
	
	public static void remProfile(String name) {
		configs.remove(name);
	}
	
	private static class All {
		private String con = null;
		public All(String con) {
			this.con = con;
		}
		public String get() {
			return con;
		}
	}
	
	private static class Str extends All {
		public Str(String con) {
			super(con);
		}
	}
	
	public static final Str cookies = new Str("cookies");
	public static final Str dungeonSlot = new Str("dungeonSlot");
	
	public static String getStr(String name, Str con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsString();
	}
	
	public static void setStr(String name, Str con, String val) {
		configs.getAsJsonObject(name).addProperty(con.get(), val);
	}
	
	
	private static class Int extends All {
		public Int(String con) {
			super(con);
		}
	}
	
	public static final Int maxPage = new Int("maxPage");
	
	public static int getInt(String name, Int con) {
		return configs.getAsJsonObject(name).getAsJsonPrimitive(con.get()).getAsInt();
	}
	
	public static void setInt(String name, Int con, int val) {
		configs.getAsJsonObject(name).addProperty(con.get(), val);
	}
	
	
	public static class Obj extends All {
		public Obj(String con) {
			super(con);
		}
	}
	
	public static final Obj favs = new Obj("favs");
	public static final Obj stats = new Obj("stats");
	
	public static JsonObject getObj(String name, Obj con) {
		return configs.getAsJsonObject(name).getAsJsonObject(con.get());
	}
	
	public static void setObj(String name, Obj con, JsonObject val) {
		configs.getAsJsonObject(name).add(con.get(), val);
	}
	
	
	
	public static class B extends All {
		public B(String con) {
			super(con);
		}
	}
	
	public static final B place = new B("place");
	public static final B upgrade = new B("upgrade");
	public static final B unlock = new B("unlock");
	public static final B dupe = new B("dupe");
	public static final B buy = new B("buy");
	
	public static int getUnitInt(String name, String uType, B con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.getAsJsonPrimitive(con.get())
				.getAsInt();
	}
	
	public static void setUnitInt(String name, String uType, B con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), val);
	}
	
	
	private static class S extends All {
		public S(String con) {
			super(con);
		}
	}
	
	public static final S spec = new S("spec");
	
	public static String getUnitString(String name, String uType, S con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.getAsJsonPrimitive(con.get())
				.getAsString();
	}
	
	public static void setUnitString(String name, String uType, S con, String str) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("units")
				.getAsJsonObject(uType)
				.addProperty(con.get(), str);
	}
	
	
	private static class CB extends All {
		public CB(String con) {
			super(con);
		}
	}
	
	public static final CB enabled = new CB("enabled");
	
	public static boolean getChestBoolean(String name, String cType, CB con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.getAsJsonPrimitive(con.get())
				.getAsBoolean();
	}
	
	public static void setChestBoolean(String name, String cType, CB con, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), b);
	}
	
	
	private static class CI extends All {
		public CI(String con) {
			super(con);
		}
	}
	
	public static final CI minc = new CI("min");
	public static final CI maxc = new CI("max");
	
	public static int getChestInt(String name, String cType, CI con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.getAsJsonPrimitive(con.get())
				.getAsInt();
	}
	
	public static void setChestInt(String name, String cType, CI con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("chests")
				.getAsJsonObject(cType)
				.addProperty(con.get(), val);
	}
	
	
	private static class T extends All {
		public T(String con) {
			super(con);
		}
	}
	
	public static final T max = new T("max");
	public static final T min = new T("min");
	
	public static int getTime(String name, T con) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("time")
				.getAsJsonPrimitive(con.get()).getAsInt();
	}
	
	public static void setTime(String name, T con, int val) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("time")
				.addProperty(con.get(), val);
	}
	
	
	
	public static boolean isSlotBlocked(String name, String slot) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("blockedSlots")
				.getAsJsonPrimitive(""+slot)
				.getAsBoolean();
	}
	
	public static void setSlotBlocked(String name, String slot, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("blockedSlots")
				.addProperty(""+slot, b);
	}
	
	
	public static boolean isSlotLocked(String name, String slot) {
		return configs.getAsJsonObject(name)
				.getAsJsonObject("lockedSlots")
				.getAsJsonPrimitive(""+slot)
				.getAsBoolean();
	}
	
	public static void setSlotLocked(String name, String slot, boolean b) {
		configs.getAsJsonObject(name)
				.getAsJsonObject("lockedSlots")
				.addProperty(""+slot, b);
	}
	
	
	public static Set<String> keySet() {
		return configs.keySet();
	}
	
	public static void load() throws IOException {
		load(false);
	}
	
	public static void load(boolean create) throws IOException {
		if(create) {
			configs = new JsonObject();
		} else {
			try {
				configs = JsonParser.parseObj(NEF.read(path));
				JsonObject def = JsonParser.parseObj(StreamRaiders.get("defConfig"));
				for(String key : configs.keySet())
					JsonParser.check(configs.getAsJsonObject(key), def);
			} catch (FileNotFoundException e) {
				configs = new JsonObject();
			}
		}
	}
	
	private static final List<String> cookiesa = Arrays.asList("ACCESS_INFO scsession".split(" "));
	
	public static void add(String name, JsonObject cookies) {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for(String key : cookies.keySet()) {
			if(Configs.cookiesa.contains(key)) {
				sb.append(key + "=" + cookies.getAsJsonPrimitive(key).getAsString() + "; ");
				c++;
			}
		}
		if(c != Configs.cookiesa.size()) {
			int index = 0;
			while(true) {
				index = sb.indexOf("=", index)+1;
				if(index == 0)
					break;
				sb.replace(index, sb.indexOf(";", index), "{hidden}");
			}
			StreamRaiders.log("Not enough cookies, got: "+sb.toString(), null);
			return;
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", sb.toString().substring(0, sb.length()-2));
		configs.add(name, JsonParser.check(jo, JsonParser.parseObj(StreamRaiders.get("defConfig"))));
	}
	
	private static void add(String name, String cookies) {
		JsonObject jo = new JsonObject();
		jo.addProperty("cookies", cookies);
		configs.add(name, JsonParser.check(jo, JsonParser.parseObj(StreamRaiders.get("defConfig"))));
	}
	
	
	public static void save() {
		try {
			NEF.save(path, JsonParser.prettyJson(configs));
		} catch (IOException e) {
			StreamRaiders.log("Failed to save configs", e);
		}
	}
	
	
	private static String[] exopts = "cookies unit_place unit_upgrade unit_unlock unit_dupe unit_buy unit_spec chests_min chests_max chests_enabled blockedSlots lockedSlots dungeonSlot time maxPage favs stats".split(" ");
	
	public static void exportConfig(GUI parent) {
		
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
	
	private static String[] units_opt = "place upgrade unlock dupe buy spec".split(" ");
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
			
			if(b || GUI.isCButSelected("ex::favs")) {
				pro.addProperty("favs", con.getAsJsonObject("favs").toString());
			}
			
			if(b || GUI.isCButSelected("ex::stats")) {
				pro.addProperty("stats1", con.get("stats").toString());
			}
			
			ret.add(key, pro);
		}
		
		try {
			NEF.save(file.getAbsolutePath(), JsonParser.prettyJson(ret));
		} catch (IOException e1) {
			StreamRaiders.log("Configs -> export: err=unable to save export", e1);
		}
		
		gui.close();
	}
	
	
	private static class InPro {
		private static StringBuilder names = new StringBuilder();
		public static String getNames() {
			return names.toString();
		}
		
		private static StringBuilder cnames = new StringBuilder();
		public static String getCNames() {
			return cnames.toString();
		}
		
		
		private JsonObject content;
		private String name;
		
		public InPro(String name, JsonObject content) {
			this.content = content;
			this.name = name;
			names.append(" " + name);
			if(content.has("cookies"))
				cnames.append(name + " ");
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
		JsonObject in;
		try {
			in = JsonParser.parseObj(NEF.read(parent.showFileChooser("Export Config", false, new FileNameExtensionFilter("Json Files", "json")).getAbsolutePath()));
		} catch (IOException e1) {
			StreamRaiders.log("Configs -> importConfig: err=failed to load config file", e1);
			return;
		}
		
		inPros = new InPro[in.size()];
		int i = 0;
		for(String key : in.keySet()) 
			inPros[i++] = new InPro(key, in.getAsJsonObject(key));
		
		pros = configs.keySet().toArray(new String[configs.size()]);
		
		String[] vals = ("(none)" + InPro.getNames()).split(" ");
		
		
		gin = new GUI("import config", 500, 600, parent, null);
		
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
			cb.setList(InPro.getCNames().split(" "));
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
				StreamRaiders.log("Configs -> imports: imp=null", null);
				continue;
			}
			
			conMerge(pros[i], imp);
		}
		
		for(int i=gpos; i<pos; i++) {
			String sel = GUI.getSelected("im::cb::"+i);
			
			for(int j=0; j<inPros.length; j++) {
				if(!sel.equals(inPros[j].getName()))
					continue;
				
				String name = GUI.getInputText("im::add::"+i);
				add(name, inPros[j].getContent().getAsJsonPrimitive("cookies").getAsString());
				conMerge(name, inPros[j].getContent());
				break;
			}
		}
		
		gin.close();
		MainFrame.refresh(false);
	}
	
	private static void conMerge(String name, JsonObject imp) {
		
		B[] bs = new B[] {buy, dupe, unlock, upgrade, place};
		List<String> sbs = Arrays.asList("buy1 dupe1 unlock1 upgrade1 place1".split(" "));
		
		for(String key : imp.keySet()) {
			String[] keys = key.split("_");
			
			switch(keys[0]) {
			case "cookies":
				configs.getAsJsonObject(name).add("cookies", imp.get(key));
				break;
			case "unit":
				if(keys[2].equals("spec")) {
					setUnitString(name, keys[1], spec, imp.getAsJsonPrimitive(key).getAsString());
				} else if(keys[2].endsWith("1")) {
					setUnitInt(name, keys[1], bs[sbs.indexOf(keys[2])], imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "chests":
				if(keys[2].equals("enabled")) {
					setChestBoolean(name, keys[1], enabled, imp.getAsJsonPrimitive(key).getAsBoolean());
				} else {
					setChestInt(name, keys[1], keys[2].equals("min") ? minc : maxc, imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "blockedSlots":
				setSlotBlocked(name, keys[1], imp.getAsJsonPrimitive(key).getAsBoolean());
				break;
			case "lockedSlots":
				setSlotLocked(name, keys[1], imp.getAsJsonPrimitive(key).getAsBoolean());
				break;
			case "dungeonSlot":
				setStr(name, dungeonSlot, imp.getAsJsonPrimitive(key).getAsString());
				break;
			case "time":
				if(keys[1].equals("max")) {
					setTime(name, max, imp.getAsJsonPrimitive(key).getAsInt());
				} else {
					setTime(name, min, imp.getAsJsonPrimitive(key).getAsInt());
				}
				break;
			case "maxPage":
				setInt(name, maxPage, imp.getAsJsonPrimitive(key).getAsInt());
				break;
			case "favs":
				setObj(name, favs, JsonParser.parseObj(imp.getAsJsonPrimitive(key).getAsString()));
				break;
			case "stats1":
				JsonObject stat = getObj(name, stats);
				JsonObject ostat = JsonParser.parseObj(imp.getAsJsonPrimitive(key).getAsString());
				stat.addProperty("time", (long) stat.getAsJsonPrimitive("time").getAsLong() + ostat.getAsJsonPrimitive("time").getAsLong());
				
				JsonObject rews = stat.getAsJsonObject("rewards");
				JsonObject orews = ostat.getAsJsonObject("rewards");
				
				for(String rew : orews.keySet()) {
					JsonElement je = rews.get(rew);
					if(je != null) {
						rews.addProperty(rew, je.getAsInt() + orews.get(rew).getAsInt());
					} else {
						rews.add(rew, orews.get(rew).deepCopy());
					}
				}
				break;
			}
		}
		
		
		
	}
	
}
