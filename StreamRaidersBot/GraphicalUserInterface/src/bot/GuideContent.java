package bot;

import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import program.Debug;
import program.Options;
import program.Remaper;
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
		JsonObject chest_rews = Json.parseObj(Options.get("rewards"));
		
		int i=-1;
		for(String chest : chests.keySet()) {
			chest = Remaper.map(chest);
			
			Image img;
			if(chest.contains("dungeon") || chest.contains("vampire"))
				img = new Image("data/ChestPics/dungeonchest.png");
			else if(cts.contains(new JsonPrimitive(chest)))
				img = new Image("data/ChestPics/" + chest + ".png");
			else
				continue;
			
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
			if(key.equals("anightmarechest") || key.equals("bonechest") || key.contains("achampion"))
				continue;
			
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
