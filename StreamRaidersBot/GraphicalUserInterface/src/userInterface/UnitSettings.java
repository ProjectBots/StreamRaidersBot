package userInterface;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.time.LocalDateTime;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Button;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextField;
import include.Json;
import include.NEF;
import program.ConfigsV2;
import program.ConfigsV2.UniInt;
import program.Debug;
import program.Options;
import program.Unit;

public class UnitSettings {

	public static final String pre = "UnitSettings::";
	
	private String uid;
	
	private static String[] prios = "place epic placedun epicdun upgrade unlock dupe buy difmin difmax epicdifmin epicdifmax".split(" ");
	private static String prio_jump = "difmin";
	
	
	public void open(String cid, String lay, GUI parent) {
		
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
		
		JsonObject uns;
		try {
			uns = Json.parseObj(NEF.read("data/Guide_old/unitDisName.json")).getAsJsonObject("byName");
		} catch (IOException e) {
			Debug.printException("UnitSettings -> open: err=couldn't get unit names", e, Debug.runerr, Debug.error, true);
			return;
		}
		
		int g = 0;
		
		GUI gui = new GUI("Unit Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.name), 1600, 800, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs units background"));
		gui.setFullScreen(true);
		
		String[] list = ConfigsV2.getLayerIds(cid);
		for(int i=0; i<list.length; i++)
			list[i] = ConfigsV2.getStr(cid, list[i], ConfigsV2.lname);
		
		list = ArrayUtils.insert(0, list, "(all)");
		if(!lay.equals("(all)"))
			list = putFirst(list, ConfigsV2.getStr(cid, lay, ConfigsV2.lname));
		
		
		Container clay = new Container();
		clay.setPos(0, g++);
		clay.setSpan(3, 1);
		clay.setInsets(20, 2, 2, 2);
		
			Label llay = new Label();
			llay.setPos(0, 0);
			llay.setText("Layer: ");
			llay.setForeground(Fonts.getColor("stngs units labels"));
			clay.addLabel(llay);
		
			ComboBox cblay = new ComboBox(uid+"cblay");
			cblay.setPos(1, 0);
			cblay.setList(list);
			cblay.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String[] list = ConfigsV2.getLayerIds(cid);
					String sel = GUI.getSelected(id);
					if(sel.equals("(all)")) {
						new UnitSettings().open(cid, "(all)", gui);
						gui.close();
						return;
					}
					for(String lay : list) {
						if(sel.equals(ConfigsV2.getStr(cid, lay, ConfigsV2.lname))) {
							new UnitSettings().open(cid, lay, gui);
							gui.close();
							return;
						}
					}
				}
			});
			clay.addComboBox(cblay);
			
		gui.addContainer(clay);
		
		
		int p = 1;
		
		Label llp = new Label();
		llp.setPos(p++, g);
		llp.setText("Unit");
		llp.setForeground(Fonts.getColor("stngs units labels"));
		gui.addLabel(llp);
		
		for(String key : prios) {
			
			if(key.equals(prio_jump))
				p+=3;
			
			Label lp = new Label();
			lp.setPos(p++, g);
			lp.setText(key + "");
			lp.setForeground(Fonts.getColor("stngs units labels"));
			gui.addLabel(lp);
			
		}
		
		g++;
		
		for(String un : uns.keySet()) {
			p = 0;
			String type = uns.get(un).getAsString();
			
			Image upic = new Image("data/UnitPics/"+type.replace("allies", "")+".png");
			upic.setPos(p++, g);
			upic.setSquare(22);
			gui.addImage(upic);
			
			Label lun = new Label();
			lun.setPos(p++, g);
			lun.setText(un);
			lun.setForeground(Fonts.getColor("stngs units labels"));
			gui.addLabel(lun);
			
			int sp = 0;
			
			for(String key : prios) {
				
				if(key.equals(prio_jump)) {
					sp = p;
					p+=3;
				}
				
				if(key.equals("buy") && Unit.isLegendary(type)) {
					p++;
					continue;
				}
				
				String id = uid+un+"::"+key;
				
				Integer val = ConfigsV2.getUnitInt(cid, lay, type, new UniInt(key));
				
				TextField tf = new TextField();
				tf.setPos(p++, g);
				tf.setSize(70, 20);
				tf.setText(val == null ? "" : ""+val);
				tf.setDocLis(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						check();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						check();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						check();
					}
					private void check() {
						try {
							int val = Integer.parseInt(GUI.getInputText(id));
							if((val > 5 || val < 1) && key.contains("dif"))
								throw new NumberFormatException();
							ConfigsV2.setUnitInt(cid, lay, uns.get(un).getAsString(), new UniInt(key), val);
							GUI.setBackground(id, Color.white);
						} catch (NumberFormatException e1) {
							GUI.setBackground(id, new Color(255, 122, 122));
						}
					}
				});
				
				gui.addTextField(tf, id);
			}
			
			final JsonArray uids = Json.parseObj(Options.get("specUIDs")).getAsJsonArray(type);
			
			String old = ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.spec);
			
			for(int i=0; i<3; i++) {
				final String u = uids.get(i).getAsJsonObject().getAsJsonPrimitive("uid").getAsString();
				final int ii = i;
				Button buid = new Button();
				buid.setPos(sp++, g);
				buid.setText(uids.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString());
				if(old.equals(u)) {
					buid.setGradient(Fonts.getGradient("stngs units buttons on"));
					buid.setForeground(Fonts.getColor("stngs units buttons on"));
				} else if(old.contains(u)) {
					buid.setGradient(Fonts.getGradient("stngs units buttons cat"));
					buid.setForeground(Fonts.getColor("stngs units buttons cat"));
				} else {
					buid.setGradient(Fonts.getGradient("stngs units buttons def"));
					buid.setForeground(Fonts.getColor("stngs units buttons def"));
				}
				buid.setFill('h');
				buid.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.spec).equals(u)) {
							ConfigsV2.setUnitString(cid, lay, type, ConfigsV2.spec, "null");
							GUI.setGradient(uid+"spec::"+type+"::"+ii, Fonts.getGradient("stngs units buttons def"));
							GUI.setForeground(uid+"spec::"+type+"::"+ii, Fonts.getColor("stngs units buttons def"));
						} else {
							ConfigsV2.setUnitString(cid, lay, type, ConfigsV2.spec, u);
							GUI.setGradient(uid+"spec::"+type+"::"+ii, Fonts.getGradient("stngs units buttons on"));
							GUI.setForeground(uid+"spec::"+type+"::"+ii, Fonts.getColor("stngs units buttons on"));
							GUI.setGradient(uid+"spec::"+type+"::"+((ii+1)%3), Fonts.getGradient("stngs units buttons def"));
							GUI.setForeground(uid+"spec::"+type+"::"+((ii+1)%3), Fonts.getColor("stngs units buttons def"));
							GUI.setGradient(uid+"spec::"+type+"::"+((ii+2)%3), Fonts.getGradient("stngs units buttons def"));
							GUI.setForeground(uid+"spec::"+type+"::"+((ii+2)%3), Fonts.getColor("stngs units buttons def"));
						}
					}
				});
				gui.addBut(buid, uid+"spec::"+type+"::"+i);
			}
			
			g++;
		}
		
	}
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}
	
}
