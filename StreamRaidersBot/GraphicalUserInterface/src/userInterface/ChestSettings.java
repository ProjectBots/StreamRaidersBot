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
import program.ConfigsV2;
import program.Options;
import program.Run;
import program.Debug;

public class ChestSettings {

	public static final String pre = "ChestSettings::";
	
	private final String uid, cid, lay;
	
	public ChestSettings(String cid, String lay) {
		this.cid = cid;
		this.lay = lay;
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
	}
	
	private static String[] mm = "min max".split(" ");
	private static final Color[] loyCols = new Color[] {new Color(54, 54, 54), new Color(192, 137, 112), new Color(192,192,192), new Color(212, 175, 55)};
	
	public void open(GUI parent) {
		
		
		JsonArray cts = Json.parseArr(Options.get("chests"));
		cts.remove(new JsonPrimitive("chestsalvage"));
		
		GUI gui = new GUI("Chest Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 500, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs chests background"));
		
		
		int g = 0;
		int p = 0;
		
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
			llay.setForeground(Fonts.getColor("stngs chests names"));
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
						new ChestSettings(cid, "(all)").open(gui);
						gui.close();
						return;
					}
					for(String lay : list) {
						if(sel.equals(ConfigsV2.getStr(cid, lay, ConfigsV2.lname))) {
							new ChestSettings(cid, lay).open(gui);
							gui.close();
							return;
						}
					}
				}
			});
			clay.addComboBox(cblay);
			
		gui.addContainer(clay);
		
		for(String key : mm) {
			Label mima = new Label();
			mima.setPos(p+=2, g);
			mima.setText(key+" wins");
			mima.setSpan(2, 1);
			mima.setAnchor("c");
			mima.setForeground(Fonts.getColor("stngs chests names"));
			gui.addLabel(mima);
		}
		
		g++;
		
		for(int i=0; i<cts.size(); i++) {
			p = 0;
			String chest = cts.get(i).getAsString();
			
			Image ci = new Image("data/ChestPics/"+chest+".png");
			ci.setPos(p++, g);
			ci.setSquare(22);
			gui.addImage(ci);
			
			Label cname = new Label();
			cname.setPos(p++, g);
			cname.setText(chest.substring(5, 6).toUpperCase() + chest.substring(6));
			cname.setForeground(Fonts.getColor("stngs chests names"));
			gui.addLabel(cname);
			
			for(String key : mm) {
				
				Integer wins = ConfigsV2.getChestInt(cid, lay, chest, key.equals("min") ? ConfigsV2.minc : ConfigsV2.maxc);
				
				TextField tmm = new TextField();
				tmm.setPos(p++, g);
				tmm.setText(wins == null ? "" : ""+wins);
				tmm.setSize(50, 22);
				tmm.setDocLis(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						changed();
					}
					public void changed() {
						try {
							int wins = Integer.parseInt(GUI.getInputText(uid+chest+"::"+key));
							int w;
							if(wins < 0) 
								if(key.equals("min"))
									w = 1;
								else
									w = 3;
							else if(wins < 15) 
								w = 1;
							else if(wins < 50) 
								w = 2;
							else 
								w = 3;
							
							Image img = new Image("data/LoyaltyPics/" + Run.pveloy[w] +".png");
							img.setSquare(18);
							try {
								GUI.setImage(uid+chest+"::loyImg::"+key, img);
							} catch (IOException e) {
								Debug.printException("ChestSettings -> changed: err=couldnt set image", e, Debug.general, Debug.error, null, null, true);
							}
							GUI.setBackground(uid+chest+"::loyBut::"+key, loyCols[w]);
							
							ConfigsV2.setChestInt(cid, lay, chest, key.equals("min") ? ConfigsV2.minc : ConfigsV2.maxc, wins);
							GUI.setBackground(uid+chest+"::"+key, Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+chest+"::"+key, new Color(255, 122, 122));
						}
					}
				});
				gui.addTextField(tmm, uid+chest+"::"+key);
				
				int w;
				if(wins == null) {
					w = 0;
				} else {
					if(wins < 0) 
						if(key.equals("min"))
							w = 1;
						else
							w = 3;
					else if(wins < 15) 
						w = 1;
					else if(wins < 50) 
						w = 2;
					else 
						w = 3;
				}
				
				Container cimg = new Container();
				Image img = new Image("data/LoyaltyPics/" + Run.pveloy[w] +".png");
				img.setSquare(18);
				cimg.addImage(img, uid+chest+"::loyImg::"+key);
				
				Button bmm = new Button();
				bmm.setPos(p++, g);
				bmm.setBackground(loyCols[w]);
				bmm.setContainer(cimg);
				bmm.setInsets(2, 2, 2, 15);
				bmm.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer w = ConfigsV2.getChestInt(cid, lay, chest, key.equals("min") ? ConfigsV2.minc : ConfigsV2.maxc);
						int n;
						if(w == null) {
							n = -1;
						} else {
							if(key.equals("min")) 
								if(w < 15) 
									n = 15;
								else if(w < 50)
									n = 50;
								else 
									n = -1;
							else 
								if(w < 0 || w > 49)
									n = 14;
								else if(w < 15) 
									n = 49;
								else
									n = -1;
						}
						GUI.setText(uid+chest+"::"+key, ""+n);
					}
				});
				gui.addBut(bmm, uid+chest+"::loyBut::"+key);
				
			}

			Button en = new Button();
			en.setPos(p++, g);
			en.setSize(90, 23);
			Boolean ben = ConfigsV2.getChestBoolean(cid, lay, chest, ConfigsV2.enabled);
			if(ben == null) {
				en.setText("(---)");
				en.setGradient(Fonts.getGradient("stngs chests buttons cat"));
				en.setForeground(Fonts.getColor("stngs chests buttons cat"));
			} else if(ben) {
				en.setText("Enabled");
				en.setGradient(Fonts.getGradient("stngs chests buttons on"));
				en.setForeground(Fonts.getColor("stngs chests buttons on"));
			} else {
				en.setText("Disabled");
				en.setGradient(Fonts.getGradient("stngs chests buttons def"));
				en.setForeground(Fonts.getColor("stngs chests buttons def"));
			}
			en.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Boolean ben = ConfigsV2.getChestBoolean(cid, lay, chest, ConfigsV2.enabled);
					if(ben == null || !ben) {
						ConfigsV2.setChestBoolean(cid, lay, chest, ConfigsV2.enabled, true);
						GUI.setText(uid+chest+"::enable", "Enabled");
						GUI.setGradient(uid+chest+"::enable", Fonts.getGradient("stngs chests buttons on"));
						GUI.setForeground(uid+chest+"::enable", Fonts.getColor("stngs chests buttons on"));
					} else {
						ConfigsV2.setChestBoolean(cid, lay, chest, ConfigsV2.enabled, false);
						GUI.setText(uid+chest+"::enable", "Disabled");
						GUI.setGradient(uid+chest+"::enable", Fonts.getGradient("stngs chests buttons def"));
						GUI.setForeground(uid+chest+"::enable", Fonts.getColor("stngs chests buttons def"));
					}
				}
			});
			gui.addBut(en, uid+chest+"::enable");
			
			g++;
		}
		

		p = 0;
		for(String key : mm) {
			Label mima = new Label();
			mima.setPos(p+=2, g);
			mima.setText(key+" Room");
			mima.setSpan(2, 1);
			mima.setAnchor("c");
			mima.setInsets(10, 2, 2, 2);
			mima.setForeground(Fonts.getColor("stngs chests names"));
			gui.addLabel(mima);
		}
		
		g++;
		p = 0;
		
		Image ci = new Image("data/ChestPics/dungeonchest.png");
		ci.setPos(p++, g);
		ci.setSquare(22);
		gui.addImage(ci);
		
		Label cname = new Label();
		cname.setPos(p++, g);
		cname.setText("Dungeon");
		cname.setForeground(Fonts.getColor("stngs chests names"));
		gui.addLabel(cname);
		

		String chest = "dungeonchest";
		
		p = 0;
		for(String key : mm) {
			Integer rooms = ConfigsV2.getChestInt(cid, lay, chest, key.equals("min") ? ConfigsV2.minc : ConfigsV2.maxc); 
			
			TextField tmm = new TextField();
			tmm.setPos(p+=2, g);
			tmm.setText(rooms == null ? "" : ""+rooms);
			tmm.setSize(100, 22);
			tmm.setSpan(2, 1);
			tmm.setAnchor("c");
			tmm.setDocLis(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					changed();
				}
				public void changed() {
					try {
						int wins = Integer.parseInt(GUI.getInputText(uid+chest+"::"+key));
						ConfigsV2.setChestInt(cid, lay, chest, key.equals("min") ? ConfigsV2.minc : ConfigsV2.maxc, wins);
						GUI.setBackground(uid+chest+"::"+key, Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+chest+"::"+key, new Color(255, 122, 122));
					}
				}
			});
			gui.addTextField(tmm, uid+chest+"::"+key);
		}
	}
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}
	
}
