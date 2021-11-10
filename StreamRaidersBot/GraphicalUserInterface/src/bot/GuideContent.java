package bot;

import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.WinLis;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.Guide_old;
import include.Guide_old.OnLoad;
import program.Debug;
import program.Options;
import include.Json;
import include.NEF;

public class GuideContent {

	private static final String path = "data/Guide_old/";
	
	private static boolean uptodate = false;
	
	public static void show() {
		if(!uptodate)
			if(!MainFrame.getGUI().showConfirmationBox("The Guide isn't up to date\r\nopen anyways?"))
				return;
		
		Guide_old g = new Guide_old();
		
		g.addSubject("Home");
		g.addSection(null, genBasicImage("home.png"));
		g.addReference("Head Section");
		g.addReference("Profile Section");
		g.addReference("Other Projects");
		
		g.addSubject("Head Section");
		g.addSection("Bot", genBasicLabel("<html><font size=5>Guide</font><br>open this Guide<br><br><font size=5>General</font><br>opens the general menu<br><br><font size=5>Add a Profile</font><br>opens the add a profile menu<br><br><font size=5>start all</font><br>starts all non running profiles<br><br><font size=5>start all delayed</font><br>opens a gui where you can enter the delay<br>in which the profiles should start<br>(start - wait - start - wait ... )<br><br><font size=5>stop all</font><br>stops every running profile<br><br><font size=5>skip time all</font><br>skips the time for every profile<br><br><font size=5>skip time all delayed</font><br>opens a gui where you can enter the delay<br>in which the profiles should skip the time<br>(skip - wait - skip - wait ... )<br><br><font size=5>reload Config</font><br>Force Reloads the config File.<br>Causes the removal of every change to<br>the settings and stats since last restart.</html>"));
		g.addSection("Config", genBasicLabel(""));
		g.addReference("Add a Profile");
		g.addReference("General");
		g.addReference("Config Export");
		g.addReference("Config Import");
		
		g.addSubject("Profile Section");
		g.addSection(null, genBasicImage("profile.png"));
		g.addSection("Profile-Name", genBasicLabel("<html>Displays the name of your Profile</html>"));
		g.addSection("Counter", genBasicLabel("<html>Displays the time till the next Round starts</html>"));
		g.addSection("Lock", genBasicLabel("<html>The Bot can not switch the Streamer if locked.<br>The Button is green when active</html>"));
		g.addSection("Delete", genBasicLabel("<html>Removes the Profile from existence</html>"));
		g.addSection("Start/Stop", genBasicLabel("<html>Starts/Stops the Bot.<br>When stopping the stats will be saved for the profile</html>"));
		g.addSection("Skip Time", genBasicLabel("<html>Skips the wait time and instantly does the next round</html>"));
		g.addSection("Streamer", genBasicLabel("<html>Displays the Streamer and your loyalty<br>Streamer_Name - wins|level<br>0-14&nbsp;&nbsp;&nbsp;bronze<br>15-49 silver<br>50+&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gold</html>"));
		g.addSection("Favorite", genBasicLabel("<html>Favorites a Streamer locally (not in SR itself).<br>Favorited Streamers have a higher priority when<br>a Streamer has to be switched out.<br>Heart will turn red when active</html>"));
		g.addSection("Stats", genBasicLabel("<html>Shows how much this Bot earned in this run</html>"));
		g.addReference("Chest-Types");
		g.addReference("Profile Settings");
		g.addReference("Map");
		
		g.addSubject("General");
		g.addSection("Forget Me", genBasicLabel("<html>Removes every Profile from existence</html>"));
		g.addSection("Show Stats", genBasicLabel("<html>Shows an average of the Stats per profile per hour</html>"));
		g.addSection("Update Stats", genBasicLabel("<html>Collects the Stats from running Profiles and saves them.</html>"));
		g.addSection("Reset all Stats", genBasicLabel("<html>Deletes the Stats from every Profile.</html>"));
		
		g.addSubject("Add a Profile");
		g.addSection(null, addAProfile());

		g.addSubject("Profile Settings");
		g.addSection(null, genBasicImage("profilesettings.png"));
		g.addSection("Units", genBasicLabel("<html>Will open a Menu with differnt options about this specific Unit.</html>"));
		g.addSection("Chests", genBasicLabel("<html>Only Raids with whitelisted Chests will be choosen .<br>The Button is green when the Chest is whitelisted.</html>"));
		g.addSection("Normal Chests max Loyalty", genBasicLabel("<html>The highest Loyalty for Normal Chests.<br>A Raid will be switched when the condition is not met.</html>"));
		g.addSection("Loyalty Chests min Loyalty", genBasicLabel("<html>The lowest Loyalty for Loyalty Chests.<br>A Raid will be switched when the condition is not met.</html>"));
		g.addSection("Exlude Slots", genBasicLabel("<html>Specify the slots that the bot should not use.</html>"));
		g.addSection("Reset Stats", genBasicLabel("<html>Removes the Stats for this Profile from existence</html>"));
		g.addReference("Unit Settings");
		g.addReference("Unit-Types");
		g.addReference("Chest-Types");
		
		g.addSubject("Unit Settings");
		g.addSection(null, genBasicImage("usettings.png"));
		g.addSection("can place", genBasicLabel("<html>Set if the Bot can place this Unit.</html>"));
		g.addSection("can upgrade", genBasicLabel("<html>Set if the Bot can upgrade this Unit.</html>"));
		g.addSection("can unlock", genBasicLabel("<html>Set if the Bot can unlock this Unit.</html>"));
		g.addSection("can dupe", genBasicLabel("<html>Set if the Bot can unlock the dupe of this Unit.</html>"));
		g.addSection("specialize", genBasicLabel("<html>Opens a new window where a specialization can be choosen.</html>"));
		
		g.addSubject("Map");
		g.addSection(null, genBasicImage("map.png"));
		g.addSection(null, genBasicLabel("<html><font size=6>Colors</font><table><tr><th color=green>Green</th><th>Allies</th></tr><tr><th color=#ffc800>Orange</th><th>You</th></tr><tr><th color=#c8ff00>Yellowish</th><th>Captain</th></tr><tr><th color=red>Red</th><th>Enemies</th></tr><tr><th color=#E77471>Light Red</th><th>Enemie place zone</th></tr><tr><th color=#43BFC4>Blue</th><th>Player place zone</th></tr><tr><th color=#7D0552>Violet</th><th>Holding zone</th></tr><tr><th color=#333333>Dark Gray</th><th>Obstacle</th></tr><tr><th color=#5e5e5e>Gray</th><th>overflyable Obstacle</th></tr></tr><tr><th color =#919191>Light Gray</th><th>overwalkable Obstacle</th></tr></table><br><font size=6>Plan</font><table><tr><th>N</th><th>No Placement</th></tr><tr><th>V</th><th>Vibe</th></tr><tr><th>R</th><th>Ranged</th></tr><tr><th>A</th><th>Armored</th></tr><tr><th>M</th><th>Melee</th></tr><tr><th>S</th><th>Support</th></tr><tr><th>L</th><th>FlagBearer</th></tr><tr><th>H</th><th>Healer</th></tr><tr><th>I</th><th>Assassine</th></tr><tr><th>D</th><th>Rogue</th></tr><tr><th>F</th><th>FlyingRogue</th></tr><tr><th>E</th><th>Buster</th></tr><tr><th>B</th><th>BalloonBuster</th></tr><tr><th>(X</th><th>Not Player Allie)</th></tr></table></html>"));
		
		g.addSubject("Chest-Types");
		g.addSection(null, chestTypes());
		
		g.addSubject("Unit-Types");
		g.addSection(null, genUnits());
		g.setOnLoad(new OnLoad() {
			@Override
			public void run() {
				loadUnits();
			}
		});
		
		g.addSubject("Config Export");
		
		g.addSubject("Config Import");
		
		g.addSubject("Other Projects");
		g.addSection("JsonExplorer", jsonexplorer());
		g.addSection("JHtmlEditor", jhtmleditor());
		
		g.create(MainFrame.getGUI(), null);
		
		g.setWindowEvent(new WinLis() {
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
				epic = false;
			}
		});
	}
	

	private static Container jhtmleditor() {
		Container c = new Container();
		Image img = new Image(path + "jhtmleditor.png");
		img.setWidth(400);
		c.addImage(img);
		Label l = new Label();
		l.setPos(0, 1);
		l.setText("<html>Html Editor for Java Applications<br><br>Have you always wanted an editor that shows you what an html page looks like in a JLabel in java?<br>Then this little program is for you!<br><br>features:<br>- adjustable autocomplete<br>- real time outcome</html>");
		c.addLabel(l);
		TextArea ta = new TextArea();
		ta.setPos(0, 2);
		ta.setText("https://github.com/ProjectBots/JHtml-Editor");
		ta.setEditable(false);
		c.addTextArea(ta);
		return c;
	}


	private static Container jsonexplorer() {
		Container c = new Container();
		Image img = new Image(path + "jsonexplorer.png");
		img.setWidth(400);
		c.addImage(img);
		Label l = new Label();
		l.setPos(0, 1);
		l.setText("<html>simple commandline based JsonExplorer<br><br>explore and edit realy big jsons without crashing your computer<br>(tested it with a 733.595 lines long file)<br><br>features:<br>exploring like a file system in cmd<br>easy changing of values<br>very customizeable search<br>saving as \"pretty\" json or as \"one line\" json<br>exporting objects/arrays into a new file (search compatible!)</html>");
		c.addLabel(l);
		TextArea ta = new TextArea();
		ta.setPos(0, 2);
		ta.setText("https://github.com/ProjectBots/JsonExplorer");
		ta.setEditable(false);
		ta.setInsets(2, 2, 20, 2);
		c.addTextArea(ta);
		return c;
	}


	private static Container genBasicLabel(String text) {
		Container c = new Container();
		c.setAnchor("w");
		Label l = new Label();
		l.setText(text);
		c.addLabel(l);
		return c;
	}
	
	private static Container genBasicImage(String name) {
		Container c = new Container();
		Image img = new Image(path + name);
		img.setWidth(400);
		c.addImage(img);
		return c;
	}
	
	private static Container addAProfile() {
		Container c = new Container();
		Image img1 = new Image(path + "addaprofile.png");
		img1.setWidth(200);
		c.addImage(img1);
		Label l1 = new Label();
		l1.setText("<html>Type in the name of your profile.<br>This name does not need to be your real profile name</html>");
		l1.setPos(0, 1);
		c.addLabel(l1);
		Image img2 = new Image(path + "addaprofileload.png");
		img2.setWidth(200);
		img2.setInsets(40, 2, 2, 2);
		img2.setPos(0, 2);
		c.addImage(img2);
		Label l2 = new Label();
		l2.setText("<html>You should let it load until this</html>");
		l2.setPos(0, 3);
		c.addLabel(l2);
		return c;
	}
	

	public static Container chestTypes() {
		Container c = new Container();
		
		JsonObject chests;
		try {
			chests = Json.parseObj(NEF.read(path + "chestRewards.json"));
		} catch (IOException e) {
			e.printStackTrace();
			return c;
		}
		
		JsonArray cts = Json.parseArr(Options.get("chests"));
		JsonObject chest_rews = Json.parseObj(Options.get("rewards"));
		
		int i=-1;
		for(String chest : chests.keySet()) {
			
			Image img;
			if(chest.contains("dungeon") || chest.contains("vampire") || cts.contains(new JsonPrimitive(chest)))
				img = new Image("data/ChestPics/" + chest.replace("2", "").replace("vampire", "dungeon") + ".png");
			else
				img = new Image("data/ChestPics/chestboostedskin.png");
			img.setSquare(100);
			img.setPos(0, ++i);
			c.addImage(img);
			
			Label t = new Label();
			t.setPos(1, i);
			t.setText("<html><font size=7>" + chest.replace("chest", "") + "</font></html>");
			c.addLabel(t);
			
			JsonObject ch = chests.getAsJsonObject(chest);
			
			StringBuilder bs= new StringBuilder();
			int slots = ch.size()-3;
			int ind = 0;
			for(String key : ch.keySet()) {
				if(ind++ == slots) 
					break;
				bs.append("<tr><th>" + ind + "</th><th><table>");
				
				JsonObject slot = ch.getAsJsonObject(key);
				for(String k : slot.keySet()) {
					JsonObject item = chest_rews.getAsJsonObject(k);
					String disname = item.get("DisplayName").getAsString();
					bs.append("<tr><th>" + (disname.contains("scrolls") ? item.get("Quantity").getAsString() + " " : "") + disname + "</th><th>" + slot.getAsJsonPrimitive(k).getAsString() + "%</th></tr>");
				}
					
				bs.append("</table></th></tr><tr><th colspan=\"2\"><hr></th></tr>");
			}
			
			StringBuilder vs = new StringBuilder();
			ind = 0;
			for(String key : ch.keySet()) {
				if(ind++ < slots) continue;
				vs.append("<tr><th>" + (ind-slots) + "</th><th><table>");
				
				JsonObject slot = ch.getAsJsonObject(key);
				for(String k : slot.keySet()) {
					JsonObject item = chest_rews.getAsJsonObject(k);
					String disname = item.get("DisplayName").getAsString();
					vs.append("<tr><th>" + (disname.contains("scrolls") ? item.get("Quantity").getAsString() + " " : "") + disname + "</th><th>" + slot.getAsJsonPrimitive(k).getAsString() + "%</th></tr>");
				}
				vs.append("</table></th></tr><tr><th colspan=\"2\"><hr></th></tr>");
			}
			
			
			Label l = new Label();
			l.setPos(0, ++i);
			l.setSpan(2, 1);
			l.setText("<html><font size=5>Viewer Slots</font><br><table>" + vs.toString() + "</table><font size=5>Bonus Slots</font><table>" + bs.toString() + "</table></html>");
			c.addLabel(l);
		}
		
		return c;
	}

	
	public static void saveChestRewards(JsonObject sheets) {
		JsonObject chests = sheets.getAsJsonObject("Chests");
		JsonObject slots = sheets.getAsJsonObject("ChestRewardSlots");
		
		JsonObject complete = new JsonObject();
		
		for(String key : chests.keySet()) {
			if(key.equals("anightmarechest") || key.equals("bonechest") || key.contains("achampion")) continue;
			complete.add(key, new JsonObject());
			
			String[] vs = chests.getAsJsonObject(key).getAsJsonPrimitive("ViewerSlots").getAsString().replace(" ", "").split(",");
			String[] bs = chests.getAsJsonObject(key).getAsJsonPrimitive("BonusSlots").getAsString().replace(" ", "").split(",");
			
			String[] all = new String[bs.length + vs.length];
			System.arraycopy(bs, 0, all, 0, bs.length);
			System.arraycopy(vs, 0, all, bs.length, vs.length);
			
			for(int i=0; i<all.length; i++) {
				String n = all[i];
				while(true) {
					if(complete.getAsJsonObject(key).has(n)) {
						n += "_";
					} else {
						complete.getAsJsonObject(key).add(n, new JsonObject());
						break;
					}
				}
				
				JsonObject slot = slots.getAsJsonObject(all[i]);
				
				String[] chances = slot.get("LootChanceList").getAsString().split(",");
				String[] rews = slot.get("RewardList").getAsString().split(",");
				
				for(int j=0; j<chances.length && j<rews.length; j++) {
					int q = Integer.parseInt(chances[j]);
					complete.getAsJsonObject(key).getAsJsonObject(n).addProperty(rews[j], q);
				}
				
			}
			
		}
		
		try {
			NEF.save(path + "chestRewards.json", Json.prettyJson(complete));
		} catch (IOException e) {
			Debug.printException("GuideContent -> saveChestRewards: err=failed to save chestRewards.json", e, Debug.runerr, Debug.error, null, null, true);
		}
	}
	
	private static JsonObject stats;
	private static String uid = "";
	private static int lvl = -1;
	private static String spec = null;
	private static boolean epic = false;
	
	
	private static JsonObject specs = Json.parseObj(Options.get("specUIDs"));
	
	private static void resetUnits() {
		uid = "archer";
		lvl = 0;
		spec = null;
		if(epic) {
			GUI.setBackground("epic", Color.green);
		}
	}
	
	public static void loadUnits() {
		resetUnits();
		GUI.setEnabled("guide::spec", false);
		update(-1);
	}
	
	
	public static Container genUnits() {

		Container c = new Container();
		
		JsonObject names;
		try {
			stats = Json.parseObj(NEF.read(path + "units.json"));
			names = Json.parseObj(NEF.read(path + "unitDisName.json")).getAsJsonObject("byName");
		} catch (IOException e) {
			e.printStackTrace();
			return c;
		}
		
		
		String[] nameList = names.keySet().toArray(new String[names.size()]);
		
		ComboBox unit = new ComboBox("guide::unit");
		unit.setPos(0, 0);
		unit.setList(nameList);
		unit.setCL(new CombListener() {
			@Override
			public void unselected(String id, ItemEvent e) {}
			@Override
			public void selected(String id, ItemEvent e) {
				uid = names.getAsJsonPrimitive(GUI.getSelected(id)).getAsString();
				Image img = new Image("data/UnitPics/" + uid.toLowerCase().replace("allies", "") + ".png");
				img.setSquare(200);
				try {
					GUI.setImage("guide::unit::img", img);
				} catch (IOException e1) {
					Debug.printException("GuideContent -> genUnits: err=failed to genUnits, uid="+uid, e1, Debug.general, Debug.error, null, null, true);
				}
				update(-1);
			}
		});
		c.addComboBox(unit);
		
		ComboBox level = new ComboBox("guide::level");
		level.setPos(1, 0);
		level.setList("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30".split(" "));
		level.setCL(new CombListener() {
			@Override
			public void unselected(String id, ItemEvent e) {}
			@Override
			public void selected(String id, ItemEvent e) {
				lvl = Integer.parseInt(GUI.getSelected(id)) - 1;
				String sel = GUI.getSelected("guide::spec");
				if(spec != null) {
					spec = getSpec(uid, sel);
					update(Arrays.asList(GUI.getCombItems("guide::spec")).indexOf(sel));
				} else {
					update(-1);
				}
			}
		});
		c.addComboBox(level);
		
		
		JsonArray ss = specs.getAsJsonArray("archer");
		String[] list = new String[3];
		for(int j=0; j<3; j++) 
			list[j] = ss.get(j).getAsJsonObject().getAsJsonPrimitive("name").getAsString();
		
		ComboBox speccb = new ComboBox("guide::spec");
		speccb.setPos(0, 1);
		speccb.setSpan(2, 1);
		speccb.setList(list);
		speccb.setCL(new CombListener() {
			@Override
			public void unselected(String id, ItemEvent e) {}
			@Override
			public void selected(String id, ItemEvent e) {
				String sel = GUI.getSelected(id);
				spec = getSpec(uid, sel);
				update(Arrays.asList(GUI.getCombItems(id)).indexOf(sel));
			}
		});
		c.addComboBox(speccb);
		
		CButton epic = new CButton("epic");
		epic.setPos(0, 2);
		epic.setText("epic");
		epic.setCBL(new CButListener() {
			@Override
			public void unselected(String id, ActionEvent e) {
				GuideContent.epic = false;
				GUI.setBackground("epic", GUI.getDefButCol());
				String sel = GUI.getSelected("guide::spec");
				if(spec != null) {
					spec = getSpec(uid, sel);
					update(Arrays.asList(GUI.getCombItems("guide::spec")).indexOf(sel));
				} else {
					update(-1);
				}
			}
			@Override
			public void selected(String id, ActionEvent e) {
				GuideContent.epic = true;
				GUI.setBackground("epic", Color.green);
				String sel = GUI.getSelected("guide::spec");
				if(spec != null) {
					spec = getSpec(uid, sel);
					update(Arrays.asList(GUI.getCombItems("guide::spec")).indexOf(sel));
				} else {
					update(-1);
				}
			}
		});
		c.addCBut(epic);
		
		
		Image img = new Image("data/UnitPics/archer.png");
		img.setSquare(200);
		img.setPos(0, 3);
		img.setSpan(2, 1);
		c.addImage(img, "guide::unit::img");
		
		
		Label out = new Label();
		out.setPos(0, 4);
		out.setSpan(2, 1);
		out.setText("");
		c.addLabel(out, "out");
		
		return c;
	}
	
	private static void update(int specIndex) {
		
		GUI.setEnabled("guide::spec", false);
		
		JsonObject unit = stats.getAsJsonArray((epic ? "epic" : "") + uid).get(lvl).getAsJsonObject().deepCopy();
		
		if(lvl >= 19) {
			GUI.setEnabled("guide::spec", true);
			JsonArray ss = specs.getAsJsonArray(uid);
			String[] list = new String[3];
			for(int i=0; i<3; i++) 
				list[i] = ss.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString();
			
			GUI.setCombList("guide::spec", list);
			
			if(specIndex != -1) {
				spec = getSpec(uid, list[specIndex]);
				GUI.setSelected("guide::spec", specIndex);
			} else {
				spec = getSpec(uid, list[0]);
			}
			
			JsonObject specStats = Json.parseObj(Options.get("specsRaw")).getAsJsonObject((epic ? "epic" : "") + spec);
			
			for(String key : unit.keySet()) {
				String val = specStats.getAsJsonPrimitive(key).getAsString();
				if(!val.equals(""))
					unit.addProperty(key, val);
			}
			
		} else {
			spec = null;
		}
		
		StringBuilder sb = new StringBuilder("<html><table>");
		
		for(String key : unit.keySet()) {
			StringBuilder value = new StringBuilder(unit.getAsJsonPrimitive(key).getAsString());
			for(int i=40; i<value.length(); i+=40) {
				int index = value.indexOf(" ", i);
				if(index == -1) break;
				value.replace(index, index+1, "<br>");
			}
			sb.append("<tr><td>" + key + "</td><td>" + value.toString() + "</td></tr>");
		}
		
		sb.append("</table></html>");
		
		GUI.setText("out", sb.toString());
	}


	private static String getSpec(String unitUID, String name) {
		for(int i=0; i<3; i++) {
			JsonObject jo = specs.getAsJsonArray(unitUID).get(i).getAsJsonObject();
			if(jo.getAsJsonPrimitive("name").getAsString().equals(name)) {
				return jo.getAsJsonPrimitive("uid").getAsString();
			}
		}
		return null;
	}
	
	
	private static String[] props = new String[] {
			"DisplayName", "Level", "Rarity", "Role",
			"HP", "Damage", "Heal", "AttackRate", "AttackType", "Speed", "Range",
			"IsFlying", "Power",
			"TargetTeam", "TargetPriorityTagsList", "TargetingPriorityRange", "UnitTargetingType",
			"Description", "SpecialAbilityDescription", "SpecialAbilityRate"
	};
	
	public static void gainStats(JsonObject units) {
		
		JsonObject typesraw = Json.parseObj(Options.get("unitTypes"));
		
		JsonArray types = new JsonArray();
		for(String key : typesraw.keySet()) {
			if(key.equals("allTypes")) continue;
			types.add(key);
			types.add("epic"+key);
		}
		
		JsonObject us = new JsonObject();
		
		for(int i=0; i<types.size(); i++) {
			String type = types.get(i).getAsString();
			JsonArray u = new JsonArray();
			for(int j=0; j<30; j++)
				u.add(0);
			
			for(String key : units.keySet()) {
				if(Pattern.matches(type + "[0-9]+", key)) {
					JsonObject ru = units.getAsJsonObject(key);
					JsonObject pu = new JsonObject();
					for(int j=0; j<props.length; j++) 
						pu.add(props[j], ru.get(props[j]));
					
					u.set(Integer.parseInt(key.replace(type, ""))-1, pu);
				}
			}
			us.add(type, u);
		}
		
		try {
			NEF.save(path + "units.json", us.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
