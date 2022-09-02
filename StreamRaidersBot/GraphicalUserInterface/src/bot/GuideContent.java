package bot;

import include.GUI.Button;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import program.Logger;
import program.Options;
import program.Remaper;
import program.Store;
import program.Unit;
import program.viewer.Raid;
import run.Manager;
import include.Json;
import include.NEF;

public class GuideContent {

	private static final String path = "data/Guide/";
	
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
		
		int i = 0;
		for(String chest : chests.keySet()) {
			chest = Remaper.map(chest);

			Image img;
			if(chest.contains("dungeon") || chest.contains("vampire"))
				img = new Image("data/ChestPics/dungeonchest.png");
			else if(cts.contains(new JsonPrimitive(chest)))
				img = new Image("data/ChestPics/" + chest + ".png");
			else
				img = new Image("data/ChestPics/nochest.png");;
			
			img.setSquare(100);
			img.setPos(0, i);
			c.addImage(img);
			
			Label t = new Label();
			t.setPos(1, i++);
			t.setText("<html><font size=7>" + chest.replace("chest", "") + "</font></html>");
			c.addLabel(t);
			
			
			JsonObject ch = chests.getAsJsonObject(chest);
			StringBuffer sb = new StringBuffer("<html><center>");
			int ind = 0;
			for(String p : "Viewer Slots  Bonus Slots  Captain Slots".split("  ")) {
				String p_ = p.substring(0, 1);
				sb.append("<font size=5>"+p+"</font><br><table>");
				while(ch.has(p_+ind)) {
					sb.append("<tr><th>(" + (ind+1) + ")</th><th><table>");
					JsonObject slot = ch.getAsJsonObject(p_+ind);
					for(String k : slot.keySet()) {
						sb.append("<tr><th>" + k + "</th><th>" + slot.get(k).getAsString() + "%</th></tr>");
					}
					ind++;
					sb.append("</table></th></tr><tr><th colspan=\"2\"><hr></th></tr>");
				}
				ind = 0;
				sb.append("</table>");
			}
			sb.append("</center></html>");
			
			Label l = new Label();
			l.setPos(0, i++);
			l.setSpan(2, 1);
			l.setText(sb.toString());
			l.setAnchor("c");
			c.addLabel(l);
		}
		
		return c;
	}

	
	private static final HashSet<String> chestWhitelist = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			String chests = Options.get("chests");
			addAll(Arrays.asList(chests.substring(2, chests.length()-2).split("\",\"")));
			addAll(Store.getCurrentEventChests(Manager.getServerTime()));
		}
	};
	
	public static void saveChestRewards(JsonObject sheets) {
		JsonObject chests = sheets.getAsJsonObject("Chests");
		JsonObject slots = sheets.getAsJsonObject("ChestRewardSlots");
		JsonObject chestRews = sheets.getAsJsonObject("ChestRewards");
		
		JsonObject complete = new JsonObject();
		
		for(String key : chests.keySet()) {
			if(!chestWhitelist.contains(key))
				continue;
			
			JsonObject defChest = chests.getAsJsonObject(key);
			JsonObject chest = new JsonObject();
			
			for(String ss : "ViewerSlots BonusSlots CaptainSlots".split(" ")) {
				String[] chest_slots = defChest.get(ss).getAsString().split(",");
				if(chest_slots.length == 1 && chest_slots[0].equals(""))
					continue;
				chestAddRewards(chest, chest_slots, slots, chestRews, ss.substring(0, 1));
			}
				
			complete.add(key, chest);
		}
		
		try {
			NEF.save(path + "chestRewards.json", Json.prettyJson(complete));
		} catch (IOException e) {
			Logger.printException("GuideContent -> saveChestRewards: err=failed to save chestRewards.json", e, Logger.runerr, Logger.error, null, null, true);
		}
		
	}
	
	public static final HashSet<String> typChestScrollRewards = new HashSet<String>() {
		private static final long serialVersionUID = 1L; {
			addAll(Arrays.asList("commonscrolls uncommonscrolls rarescrolls".split(" ")));
	}};
	
	private static void chestAddRewards(JsonObject chest, String[] slots, JsonObject all_slots, JsonObject all_rewards, String prefix) {
		for(int i=0; i<slots.length; i++) {
			JsonObject s = all_slots.getAsJsonObject(slots[i]);
			
			JsonObject slot = new JsonObject();
			
			String[] chances = s.get("LootChanceList").getAsString().split(",");
			String[] rews = s.get("RewardList").getAsString().split(",");
			
			for(int j=0; j<chances.length && j<rews.length; j++) {
				int percent = Integer.parseInt(chances[j]);
				
				int quantity = all_rewards.getAsJsonObject(rews[j]).get("Quantity").getAsInt();
				String name;
				String frew = rews[j].split("_")[0];
				if(Raid.typChestBasicRewards.containsKey(frew)) 
					name = Raid.typChestBasicRewards.get(frew);
				else if(typChestScrollRewards.contains(frew) || frew.startsWith("scroll"))
					name = frew;
				else if(rews[j].contains("skin"))
					name = rews[j];
				else {
					Logger.print("GuideContent -> chestAddRewards: err=failed to determine reward, reward=" + rews[j], Logger.lowerr, Logger.error, null, null, true);
					name = "unknown";
				}
				
				name = (name.contains("skin") ? "" : quantity+" ") + name;
				
				//	sometimes two different rewards basically give the same
				//	combine their odds
				if(slot.has(name))
					percent += slot.get(name).getAsInt();
				
				slot.addProperty(name, percent);
			}
			
			chest.add(prefix+i, slot);
		}
	}
	
	private static JsonObject stats;
	private static String uid = "";
	private static int lvl = -1;
	private static String spec = null;
	private static boolean epic = false;
	
	
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
					Logger.printException("GuideContent -> genUnits: err=failed to genUnits, uid="+uid, e1, Logger.general, Logger.error, null, null, true);
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
		
		JsonArray ss = Unit.getSpecs("archer");
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
		
		
		Button epic = new Button();
		epic.setPos(0, 2);
		epic.setText("epic");
		epic.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuideContent.epic = !GuideContent.epic;
				if(GuideContent.epic) {
					GUI.setBackground("epic", Color.green);
					String sel = GUI.getSelected("guide::spec");
					if(spec != null) {
						spec = getSpec(uid, sel);
						update(Arrays.asList(GUI.getCombItems("guide::spec")).indexOf(sel));
					} else {
						update(-1);
					}
				} else {
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
			}
		});
		c.addBut(epic, "epic");
		
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
	
	private static JsonObject specs = null;
	static {
		try {
			specs = Json.parseObj(NEF.read(path+"specs.json"));
		} catch (IOException e) {
			Logger.printException("GuideContent -> static ini: err=failed to load specs", e, Logger.runerr, Logger.error, null, null, true);
		}
	}
	
	private static void update(int specIndex) {
		
		GUI.setEnabled("guide::spec", false);
		
		JsonObject unit = stats.getAsJsonArray((epic ? "epic" : "") + uid).get(lvl).getAsJsonObject().deepCopy();
		
		if(lvl >= 19) {
			GUI.setEnabled("guide::spec", true);
			JsonArray ss = Unit.getSpecs(uid);
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
			
			JsonObject specStats = specs.getAsJsonObject((epic ? "epic" : "") + spec);
			
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
		JsonArray specs = Unit.getSpecs(unitUID);
		for(int i=0; i<3; i++) {
			JsonObject jo = specs.get(i).getAsJsonObject();
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
	
	public static void gainStats(JsonObject data) {
		
		JsonObject typesraw = Json.parseObj(Options.get("unitTypes"));
		
		JsonArray types = new JsonArray();
		for(String key : typesraw.keySet()) {
			types.add(key);
			types.add("epic"+key);
		}
		
		JsonObject us = new JsonObject();
		
		JsonObject units = data.getAsJsonObject("Units");
		
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
		
		specs = data.getAsJsonObject("Specialization");
		
		try {
			NEF.save(path + "units.json", us.toString());
			NEF.save(path + "specs.json", specs.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
