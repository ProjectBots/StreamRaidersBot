package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import include.GUI;
import include.GUI.Button;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import program.ConfigsV2;
import program.ConfigsV2.CapBoo;
import program.ConfigsV2.ListType;

public class CaptainSettings {

	public static final String pre = "CaptainSettings::";
	
	private final String uid, cid, lay;
	private GUI gui = null;
	
	public CaptainSettings(String cid, String lay) {
		this.cid = cid;
		this.lay = lay;
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
	}

	private ListType list = new ListType("campaign");
	
	public CaptainSettings setList(String list) {
		this.list = new ListType(list.toLowerCase());
		return this;
	}
	
	public void open(GUI parent) {
		
		int p = 0;
		
		gui = new GUI("Captain Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 800, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs caps background"));
		
		String[] listlays = ConfigsV2.getLayerIds(cid);
		for(int i=0; i<listlays.length; i++)
			listlays[i] = ConfigsV2.getStr(cid, listlays[i], ConfigsV2.lname);
		
		listlays = ArrayUtils.insert(0, listlays, "(all)");
		if(!lay.equals("(all)"))
			listlays = putFirst(listlays, ConfigsV2.getStr(cid, lay, ConfigsV2.lname));
		
		
		Container clay = new Container();
		clay.setPos(0, p++);
		clay.setSpan(3, 1);
		clay.setInsets(20, 2, 2, 2);
		
			Label llay = new Label();
			llay.setPos(0, 0);
			llay.setText("Layer: ");
			llay.setForeground(Fonts.getColor("stngs caps names"));
			clay.addLabel(llay);
		
			ComboBox cblay = new ComboBox(uid+"cblay");
			cblay.setPos(1, 0);
			cblay.setList(listlays);
			cblay.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String[] lays = ConfigsV2.getLayerIds(cid);
					String sel = GUI.getSelected(id);
					if(sel.equals("(all)")) {
						new CaptainSettings(cid, "(all)").setList(list.get()).open(gui);
						gui.close();
						return;
					}
					for(String lay : lays) {
						if(sel.equals(ConfigsV2.getStr(cid, lay, ConfigsV2.lname))) {
							new CaptainSettings(cid, lay).setList(list.get()).open(gui);
							gui.close();
							return;
						}
					}
				}
			});
			clay.addComboBox(cblay);
			
		gui.addContainer(clay);
		
		
		String[] lists = "Campaign Dungeon All".split(" ");
		lists = putFirst(lists, list.get().substring(0, 1).toUpperCase() + list.get().substring(1));
		
		Container clist = new Container();
		clist.setPos(0, p++);
		clist.setSpan(3, 1);
		clist.setInsets(20, 2, 2, 2);
		
			Label llist = new Label();
			llist.setPos(0, 0);
			llist.setText("List: ");
			llist.setForeground(Fonts.getColor("stngs profile labels"));
			clist.addLabel(llist);
		
			ComboBox cblist = new ComboBox(uid+"cblist");
			cblist.setPos(1, 0);
			cblist.setList(lists);
			cblist.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					new CaptainSettings(cid, lay).setList(GUI.getSelected(id)).open(gui);
					gui.close();
				}
			});
			clist.addComboBox(cblist);
			
		gui.addContainer(clist);
		
		
		Container search = new Container();
		search.setPos(0, p++);
		
			TextField stf = new TextField();
			stf.setPos(0, 0);
			stf.setSize(100, 28);
			stf.setText("");
			stf.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new CapSearch(cid, lay).open(gui, list, GUI.getInputText(uid+"search::cap"));
				}
			});
			search.addTextField(stf, uid+"search::cap");
			
			Button sbut = new Button();
			sbut.setText("search captain");
			sbut.setPos(1, 0);
			sbut.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new CapSearch(cid, lay).open(gui, list, GUI.getInputText(uid+"search::cap"));
				}
			});
			search.addBut(sbut);
		
		gui.addContainer(search);
		
		Container ccaps = new Container();
		ccaps.setPos(0, p++);
		
			Container cfav = new Container();
			cfav.setPos(0, 0);
			cfav.setAnchor("nw");
			ccaps.addContainer(cfav, uid+"fav");
			
			Container cblock = new Container();
			cblock.setPos(1, 0);
			cblock.setInsets(2, 15, 2, 2);
			cblock.setAnchor("nw");
			ccaps.addContainer(cblock, uid+"block");
			
		gui.addContainer(ccaps, uid+"caps");
		
		JsonArray caps = ConfigsV2.getFavCaps(cid, lay, list);
		String[] scaps = new String[caps.size()];
		
		
		for(int i=0; i<caps.size(); i++) 
			scaps[i] = caps.get(i).getAsString();
		
		Arrays.sort(scaps);
		
		for(int i=0; i<scaps.length; i++) 
			addCap(scaps[i], i);
		
		
	}
	
	private void addCap(String tdn, int pos) {
		Integer val = ConfigsV2.getCapInt(cid, lay, tdn, list, ConfigsV2.fav);
		boolean fav = val >= 0;
		
		Container ccap = new Container();
		ccap.setPos(0, pos);
		ccap.setInsets(10, 2, 2, 2);
		ccap.setFill('h');
			int x = 0;
			
			Label lname = new Label();
			lname.setPos(x++, 0);
			lname.setText(tdn);
			lname.setForeground(Fonts.getColor("stngs caps names"));
			lname.setWeightX(1);
			ccap.addLabel(lname);
			
			
			if(fav) {
				TextField tf = new TextField();
				tf.setPos(x++, 0);
				tf.setSize(80, 23);
				tf.setText(val == 0 || val >= Integer.MAX_VALUE-1 ? "" : ""+val);
				tf.setDocLis(new DocumentListener() {
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
						try {
							int val = Integer.parseInt(GUI.getInputText(uid+"val::"+tdn));
							if(val <= 0 || val >= Integer.MAX_VALUE-1)
								throw new NumberFormatException();
							ConfigsV2.setCapInt(cid, lay, tdn, list, ConfigsV2.fav, val);
							GUI.setBackground(uid+"val::"+tdn, Color.white);
							GUI.setForeground(uid+"heart::"+tdn, Fonts.getColor("stngs caps buttons heart on"));
							GUI.setForeground(uid+"cross::"+tdn, Fonts.getColor("stngs caps buttons cross def"));
							GUI.setGradient(uid+"heart::"+tdn, Fonts.getGradient("stngs caps buttons heart on"));
							GUI.setGradient(uid+"cross::"+tdn, Fonts.getGradient("stngs caps buttons cross def"));
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"val::"+tdn, new Color(255, 122, 122));
						}
					}
				});
				ccap.addTextField(tf, uid+"val::"+tdn);
				
				for(String key : "ic il".split(" ")) {
					final CapBoo con = new CapBoo(key);
					
					Integer b = ConfigsV2.getCapBooTend(cid, lay, tdn, list, con);
					
					Button but = new Button();
					but.setPos(x++, 0);
					but.setText(key);
					if(b < 0) {
						but.setForeground(Fonts.getColor("stngs caps buttons icil def"));
						but.setGradient(Fonts.getGradient("stngs caps buttons icil def"));
					} else if(b == 0 || b == 1) {
						but.setForeground(Fonts.getColor("stngs caps buttons icil cat"));
						but.setGradient(Fonts.getGradient("stngs caps buttons icil cat"));
					} else {
						but.setForeground(Fonts.getColor("stngs caps buttons icil on"));
						but.setGradient(Fonts.getGradient("stngs caps buttons icil on"));
					}
					but.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Integer b = ConfigsV2.getCapBooTend(cid, lay, tdn, list, con);
							if(b == 2) {
								ConfigsV2.setCapBoo(cid, lay, tdn, list, con, false);
								GUI.setForeground(uid+key+"::"+tdn, Fonts.getColor("stngs caps buttons icil def"));
								GUI.setGradient(uid+key+"::"+tdn, Fonts.getGradient("stngs caps buttons icil def"));
							} else {
								ConfigsV2.setCapBoo(cid, lay, tdn, list, con, true);
								GUI.setForeground(uid+key+"::"+tdn, Fonts.getColor("stngs caps buttons icil on"));
								GUI.setGradient(uid+key+"::"+tdn, Fonts.getGradient("stngs caps buttons icil on"));
							}
						}
					});
					ccap.addBut(but, uid+key+"::"+tdn);
				}
			}
			
			for(int i=0; i<2; i++) {
				final int ii = i;
				Button but = new Button();
				but.setPos(x++, 0);
				but.setText(i == 0 ? "\u2764" : "\u2B59");
				if(i == 0) {
					if(val < 0) {
						but.setForeground(Fonts.getColor("stngs caps buttons heart def"));
						but.setGradient(Fonts.getGradient("stngs caps buttons heart def"));
					} else if(val == 0 || val == Integer.MAX_VALUE-1) {
						but.setForeground(Fonts.getColor("stngs caps buttons heart cat"));
						but.setGradient(Fonts.getGradient("stngs caps buttons heart cat"));
					} else {
						but.setForeground(Fonts.getColor("stngs caps buttons heart on"));
						but.setGradient(Fonts.getGradient("stngs caps buttons heart on"));
					}
				} else {
					if(val > 0) {
						but.setForeground(Fonts.getColor("stngs caps buttons cross def"));
						but.setGradient(Fonts.getGradient("stngs caps buttons cross def"));
					} else if(val == 0 || val == Integer.MIN_VALUE+1) {
						but.setForeground(Fonts.getColor("stngs caps buttons cross cat"));
						but.setGradient(Fonts.getGradient("stngs caps buttons cross cat"));
					} else {
						but.setForeground(Fonts.getColor("stngs caps buttons cross on"));
						but.setGradient(Fonts.getGradient("stngs caps buttons cross on"));
					}
				}
				but.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer val = ConfigsV2.getCapInt(cid, lay, tdn, list, ConfigsV2.fav);
						GUI.removeFromContainer(uid + (fav ? "fav" : "block"), uid+"cap::"+tdn);
						if(ii == 0) {
							if(val > 0 && val != Integer.MAX_VALUE-1) {
								ConfigsV2.favCap(cid, lay, tdn, list, null);
							} else {
								ConfigsV2.favCap(cid, lay, tdn, list, 1);
								addCap(tdn, pos);
							}
						} else {
							if(val < 0 && val != Integer.MIN_VALUE+1) {
								ConfigsV2.favCap(cid, lay, tdn, list, null);
							} else {
								ConfigsV2.favCap(cid, lay, tdn, list, -1);
								addCap(tdn, pos);
							}
						}
						gui.refresh();
					}
				});
				ccap.addBut(but, uid+(i==0 ? "heart::" : "cross::")+tdn);
			}
			
		gui.addToContainer(uid + (fav ? "fav" : "block"), ccap, uid+"cap::"+tdn);
		
	}
	
	
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}
	
}
