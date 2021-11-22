package userInterface;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
import program.ConfigsV2.UniStr;
import program.Debug;
import program.Options;
import program.Unit;

public class UnitSettings {

	public static final String pre = "UnitSettings::";
	
	private final String uid, cid, lay;
	private String pn;
	
	private static final String[] prios = "place epic placedun epicdun upgrade unlock dupe buy".split(" ");
	private static final String[] opts = "favOnly markerOnly canVibe".split(" ");
	private static final String[] optopts = "nc nd ec ed".split(" ");
	private static final List<String> chests = Collections.unmodifiableList(new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			JsonArray cts = Json.parseArr(Options.get("chests"));
			cts.remove(new JsonPrimitive("chestsalvage"));
			for(int i=0; i<cts.size(); i++)
				add(cts.get(i).getAsString());
			add("dungeonchest");
		}
	});
	
	public UnitSettings(String cid, String lay) {
		this.cid = cid;
		this.lay = lay;
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
	}
	
	
	public void open(GUI parent) {
		
		pn = ConfigsV2.getPStr(cid, ConfigsV2.pname);
		
		JsonObject uns;
		try {
			uns = Json.parseObj(NEF.read("data/Guide_old/unitDisName.json")).getAsJsonObject("byName");
		} catch (IOException e) {
			Debug.printException("UnitSettings -> open: err=couldn't get unit names", e, Debug.runerr, Debug.error, pn, null, true);
			return;
		}
		
		int g = 0;
		
		GUI gui = new GUI("Unit Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 1600, 800, parent, null);
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
						new UnitSettings(cid, "(all)").open(gui);
						gui.close();
						return;
					}
					for(String lay : list) {
						if(sel.equals(ConfigsV2.getStr(cid, lay, ConfigsV2.lname))) {
							new UnitSettings(cid, lay).open(gui);
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
			Label lp = new Label();
			lp.setPos(p++, g);
			lp.setText(key + "");
			lp.setAnchor("c");
			lp.setForeground(Fonts.getColor("stngs units labels"));
			gui.addLabel(lp);
		}
		
		p+=3+chests.size()-optopts.length;
		
		for(String s : opts) {
			Label lp = new Label();
			lp.setPos(p+=optopts.length+1, g);
			lp.setSpan(optopts.length, 1);
			lp.setText(s + "");
			lp.setAnchor("c");
			lp.setForeground(Fonts.getColor("stngs units labels"));
			gui.addLabel(lp);
		}
		
		g++;
		
		for(String un : uns.keySet()) {
			p = 0;
			String type = uns.get(un).getAsString();
			
			Image upic = new Image("data/UnitPics/"+type.replace("allies", "")+".png");
			upic.setPos(p++, g);
			upic.setSquare(18);
			gui.addImage(upic);
			
			Label lun = new Label();
			lun.setPos(p++, g);
			lun.setText(un);
			lun.setForeground(Fonts.getColor("stngs units labels"));
			gui.addLabel(lun);
			
			for(String key : prios) {
				
				
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
				buid.setPos(p++, g);
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
			
			
			String val = ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.chests);
			int c = StringUtils.countMatches(val, "::") + 1;
			
			for(final String s : chests) {
				
				int m = StringUtils.countMatches(val, s+",");
				
				Container cimg = new Container();
				Image img = new Image("data/ChestPics/"+s+".png");
				img.setSquare(18);
				cimg.addImage(img);
				
				Button bcs = new Button();
				bcs.setPos(p++, g);
				bcs.setContainer(cimg);
				if(m == c) {
					bcs.setGradient(Fonts.getGradient("stngs units buttons on"));
					bcs.setForeground(Fonts.getColor("stngs units buttons on"));
				} else if(m == 0) {
					bcs.setGradient(Fonts.getGradient("stngs units buttons def"));
					bcs.setForeground(Fonts.getColor("stngs units buttons def"));
				} else {
					bcs.setGradient(Fonts.getGradient("stngs units buttons cat"));
					bcs.setForeground(Fonts.getColor("stngs units buttons cat"));
				}
				bcs.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String val = ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.chests);
						if(lay.equals("(all)")) {
							int c = StringUtils.countMatches(val, "::") + 1;
							int m = StringUtils.countMatches(val, s+",");
							if(m == c) {
								for(String lay : ConfigsV2.getLayerIds(cid))
									ConfigsV2.setUnitString(cid, lay, type, ConfigsV2.chests, ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.chests).replace(s+",", ""));
								GUI.setGradient(uid+type+"::chests::"+s, Fonts.getGradient("stngs units buttons def"));
							} else {
								for(String lay : ConfigsV2.getLayerIds(cid)) {
									String old = ConfigsV2.getUnitString(cid, lay, type, ConfigsV2.chests);
									if(old.contains(s+","))
										continue;
									old += s+",";
									ConfigsV2.setUnitString(cid, lay, type, ConfigsV2.chests, old);
								}
								GUI.setGradient(uid+type+"::chests::"+s, Fonts.getGradient("stngs units buttons on"));
							}
						} else {
							if(val.contains(s+",")) {
								val = val.replace(s+",", "");
								GUI.setGradient(uid+type+"::chests::"+s, Fonts.getGradient("stngs units buttons def"));
							} else {
								val += s+",";
								GUI.setGradient(uid+type+"::chests::"+s, Fonts.getGradient("stngs units buttons on"));
							}
							ConfigsV2.setUnitString(cid, lay, type, ConfigsV2.chests, val);
						}
					}
				});
				gui.addBut(bcs, uid+type+"::chests::"+s);
			}
			
			for(String o : opts) {
				Label space = new Label();
				space.setPos(p++, g);
				space.setSize(15, 1);
				space.setText("");
				gui.addLabel(space);
				
				final UniStr us = new UniStr(o);
				val = ConfigsV2.getUnitString(cid, lay, type, us);
				c = StringUtils.countMatches(val, "::") + 1;
				
				for(String s : optopts) {
					Button bopt = new Button();
					bopt.setPos(p++, g);
					bopt.setText(s);
					int m = StringUtils.countMatches(val, s);
					if(m == c) {
						bopt.setGradient(Fonts.getGradient("stngs units buttons on"));
						bopt.setForeground(Fonts.getColor("stngs units buttons on"));
					} else if(m == 0) {
						bopt.setGradient(Fonts.getGradient("stngs units buttons def"));
						bopt.setForeground(Fonts.getColor("stngs units buttons def"));
					} else {
						bopt.setGradient(Fonts.getGradient("stngs units buttons cat"));
						bopt.setForeground(Fonts.getColor("stngs units buttons cat"));
					}
					bopt.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String val = ConfigsV2.getUnitString(cid, lay, type, us);
							if(lay.equals("(all)")) {
								int c = StringUtils.countMatches(val, "::") + 1;
								int m = StringUtils.countMatches(val, s);
								if(m == c) {
									for(String lay : ConfigsV2.getLayerIds(cid))
										ConfigsV2.setUnitString(cid, lay, type, us, ConfigsV2.getUnitString(cid, lay, type, us).replaceFirst(s+",?", ""));
									GUI.setGradient(uid+type+"::opt::"+o+"::"+s, Fonts.getGradient("stngs units buttons def"));
									GUI.setForeground(uid+type+"::opt::"+o+"::"+s, Fonts.getColor("stngs units buttons def"));
								} else {
									for(String lay : ConfigsV2.getLayerIds(cid)) {
										String old = ConfigsV2.getUnitString(cid, lay, type, us);
										if(old.contains(s))
											continue;
										if(old.equals(""))
											old = s;
										else
											old += ","+s;
										ConfigsV2.setUnitString(cid, lay, type, us, old);
									}
									GUI.setGradient(uid+type+"::opt::"+o+"::"+s, Fonts.getGradient("stngs units buttons on"));
									GUI.setForeground(uid+type+"::opt::"+o+"::"+s, Fonts.getColor("stngs units buttons on"));
								}
							} else {
								if(val.contains(s)) {
									val = val.replaceFirst(s+",?", "");
									GUI.setGradient(uid+type+"::opt::"+o+"::"+s, Fonts.getGradient("stngs units buttons def"));
									GUI.setForeground(uid+type+"::opt::"+o+"::"+s, Fonts.getColor("stngs units buttons def"));
								} else {
									if(val.equals(""))
										val = s;
									else
										val += ","+s;
									GUI.setGradient(uid+type+"::opt::"+o+"::"+s, Fonts.getGradient("stngs units buttons on"));
									GUI.setForeground(uid+type+"::opt::"+o+"::"+s, Fonts.getColor("stngs units buttons on"));
								}
								ConfigsV2.setUnitString(cid, lay, type, us, val);
							}
						}
					});
					gui.addBut(bopt, uid+type+"::opt::"+o+"::"+s);
				}
			}
			
			g++;
		}
		
	}
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}
	
}
