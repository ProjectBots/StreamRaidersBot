package userInterface.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;

import include.GUI;
import include.GUI.Button;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextField;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Configs.CapBoo;
import otherlib.Configs.ListType;
import otherlib.Logger;
import run.Manager;
import run.ProfileType;
import run.viewer.ViewerBackEnd;
import srlib.SRC;
import srlib.SRR.NotAuthorizedException;
import srlib.viewer.CaptainData;
import userInterface.AbstractSettings;
import userInterface.Colors;

public class CaptainSettings extends AbstractSettings {

	private ListType list;
	private boolean searchFav = false;
	private boolean exportCaps = false;
	
	public CaptainSettings(String cid, String lid, GUI parrent) {
		super(cid, lid, parrent, 800, 500, false, true);
		this.list = new ListType("campaign");
		addContent();
	}
	
	public CaptainSettings(String cid, String lid, GUI parrent, ListType list) {
		super(cid, lid, parrent, 800, 500, false, true);
		this.list = list;
		addContent();
	}
	
	@Override
	protected String getSettingsName() {
		return "Captain";
	}

	@Override
	protected ProfileType getProfileType() {
		return ProfileType.VIEWER;
	}
	
	@Override
	protected void openNewInstance(String lid) {
		new CaptainSettings(cid, lid, gui, list);
	}

	@Override
	protected void addContent() {
		String[] lists = "Campaign Dungeon All".split(" ");
		lists = putFirst(lists, list.con.substring(0, 1).toUpperCase() + list.con.substring(1));
		
		Container clist = new Container();
		clist.setPos(0, g++);
		clist.setSpan(3, 1);
		clist.setInsets(20, 2, 2, 2);
		
			Label llist = new Label();
			llist.setPos(0, 0);
			llist.setText("List: ");
			llist.setForeground(Colors.getColor(fontPath+"labels"));
			clist.addLabel(llist);
		
			ComboBox cblist = new ComboBox(uid+"cblist");
			cblist.setPos(1, 0);
			cblist.setList(lists);
			cblist.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					list = new ListType(GUI.getSelected(id).toLowerCase());
					openNewInstance(lid);
				}
			});
			clist.addComboBox(cblist);
			
		gui.addContainer(clist);
		
		
		Container csrcaps = new Container();
		csrcaps.setPos(0, g++);
		
			final CaptainSettings capStngs = this;
		
			TextField stf = new TextField();
			stf.setPos(0, 0);
			stf.setSize(100, 28);
			stf.setText("");
			stf.setAL(ae -> new CapSearch(cid, lid).open(gui, list, GUI.getInputText(uid+"search::cap"), searchFav, capStngs));
			csrcaps.addTextField(stf, uid+"search::cap");
			
			Button bfav = new Button();
			bfav.setPos(1, 0);
			bfav.setText("\u2764");
			bfav.setForeground(Colors.getColor(fontPath+"buttons heart def"));
			bfav.setGradient(Colors.getGradient(fontPath+"buttons heart def"));
			bfav.setAL(ae -> {
				searchFav = !searchFav;
				GUI.setForeground(uid+"search::fav", Colors.getColor(fontPath+"buttons heart "+(searchFav?"on":"def")));
				GUI.setGradient(uid+"search::fav", Colors.getGradient(fontPath+"buttons heart "+(searchFav?"on":"def")));
			});
			csrcaps.addBut(bfav, uid+"search::fav");
			
			Button sbut = new Button();
			sbut.setText("search captain");
			sbut.setPos(2, 0);
			sbut.setForeground(Colors.getColor(fontPath+"buttons def"));
			sbut.setGradient(Colors.getGradient(fontPath+"buttons def"));
			sbut.setAL(ae -> new CapSearch(cid, lid).open(gui, list, GUI.getInputText(uid+"search::cap"), searchFav, capStngs));
			csrcaps.addBut(sbut);
			
			Button bex = new Button();
			bex.setPos(3, 0);
			bex.setInsets(2, 20, 2, 2);
			bex.setText("export");
			bex.setTooltip("override sr favs with current view");
			bex.setForeground(Colors.getColor(fontPath+"buttons def"));
			bex.setGradient(Colors.getGradient(fontPath+"buttons def"));
			bex.setAL(ae -> {
				if(exportCaps)
					return;
				
				exportCaps = true;
				new Thread(() -> {
					GUI.setForeground(uid+"search::export", Colors.getColor(fontPath+"buttons on"));
					GUI.setGradient(uid+"search::export", Colors.getGradient(fontPath+"buttons on"));
					try {
						ViewerBackEnd vbe = Manager.getViewer(cid).getBackEnd();
						CaptainData[] srfavs = vbe.searchCaptains(true, false, SRC.Search.all, false, null, 10);
						HashSet<String> srfavnames = new HashSet<>();
						HashSet<String> bfavs = Configs.getFavCaps(cid, lid, list);
						
						for(int i=0; i<srfavs.length; i++) {
							srfavnames.add(srfavs[i].twitchUserName);
							if(bfavs.contains(srfavs[i].twitchUserName))
								continue;
							String err = vbe.updateFavoriteCaptain(srfavs[i], false);
							if(err != null)
								Logger.print("CaptainSettings -> addContent -> export: err="+err+", part=unfav, tun="+srfavs[i].twitchUserName, Logger.runerr, Logger.error, cid, null, true);
						}
						
						for(String cap : bfavs) {
							if(srfavnames.contains(cap))
								continue;
							CaptainData[] s = vbe.searchCaptains(false, false, SRC.Search.all, true, cap, 1);
							if(s.length == 0) {
								Logger.print("CaptainSettings -> addContent -> export: err=failed to get captain, part=fav, tun="+cap, Logger.runerr, Logger.error, cid, null, true);
								continue;
							}
							String err = vbe.updateFavoriteCaptain(s[0], true);
							if(err != null)
								Logger.print("CaptainSettings -> addContent -> export: err="+err+", part=fav, tun="+cap, Logger.runerr, Logger.error, cid, null, true);
						}
					} catch (NoConnectionException | NotAuthorizedException e1) {
						Logger.printException("CaptainSettings -> addContent -> export: err=failed to export", e1, Logger.runerr, Logger.error, cid, null, true);
					}
					exportCaps = false;
					GUI.setForeground(uid+"search::export", Colors.getColor(fontPath+"buttons def"));
					GUI.setGradient(uid+"search::export", Colors.getGradient(fontPath+"buttons def"));
				}).start();
			});
			csrcaps.addBut(bex, uid+"search::export");
			
		
		gui.addContainer(csrcaps);
		
		Container ccaps = new Container();
		ccaps.setPos(0, g++);
		
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
		
		HashSet<String> caps = Configs.getFavCaps(cid, lid, list);
		String[] scaps = caps.toArray(new String[caps.size()]);
		
		Arrays.sort(scaps);
		
		for(int i=0; i<scaps.length; i++) 
			addCap(scaps[i], i);
		
		
	}
	
	private void addCap(final String tun, int pos) {
		Integer val = Configs.getCapInt(cid, lid, tun, list, Configs.fav);
		boolean fav = val >= 0;
		
		Container ccap = new Container();
		ccap.setPos(0, pos);
		ccap.setInsets(10, 2, 2, 2);
		ccap.setFill('h');
			int x = 0;
			
			Label lname = new Label();
			lname.setPos(x++, 0);
			lname.setText(tun);
			lname.setForeground(Colors.getColor(fontPath+"labels"));
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
							int val = Integer.parseInt(GUI.getInputText(uid+"val::"+tun));
							if(val <= 0 || val >= Integer.MAX_VALUE-1)
								throw new NumberFormatException();
							Configs.setCapInt(cid, lid, tun, list, Configs.fav, val);
							GUI.setBackground(uid+"val::"+tun, Color.white);
							GUI.setForeground(uid+"heart::"+tun, Colors.getColor(fontPath+"buttons heart on"));
							GUI.setForeground(uid+"cross::"+tun, Colors.getColor(fontPath+"buttons cross def"));
							GUI.setGradient(uid+"heart::"+tun, Colors.getGradient(fontPath+"buttons heart on"));
							GUI.setGradient(uid+"cross::"+tun, Colors.getGradient(fontPath+"buttons cross def"));
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"val::"+tun, new Color(255, 122, 122));
						}
					}
				});
				ccap.addTextField(tf, uid+"val::"+tun);
				
				for(String key : "ic il".split(" ")) {
					final CapBoo con = new CapBoo(key);
					
					Integer b = Configs.getCapBooTend(cid, lid, tun, list, con);
					
					Button but = new Button();
					but.setPos(x++, 0);
					but.setText(key);
					if(b < 0) {
						but.setForeground(Colors.getColor(fontPath+"buttons icil def"));
						but.setGradient(Colors.getGradient(fontPath+"buttons icil def"));
					} else if(b == 0 || b == 1) {
						but.setForeground(Colors.getColor(fontPath+"buttons icil cat"));
						but.setGradient(Colors.getGradient(fontPath+"buttons icil cat"));
					} else {
						but.setForeground(Colors.getColor(fontPath+"buttons icil on"));
						but.setGradient(Colors.getGradient(fontPath+"buttons icil on"));
					}
					but.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Integer b = Configs.getCapBooTend(cid, lid, tun, list, con);
							if(b == 2) {
								Configs.setCapBoo(cid, lid, tun, list, con, false);
								GUI.setForeground(uid+key+"::"+tun, Colors.getColor(fontPath+"buttons icil def"));
								GUI.setGradient(uid+key+"::"+tun, Colors.getGradient(fontPath+"buttons icil def"));
							} else {
								Configs.setCapBoo(cid, lid, tun, list, con, true);
								GUI.setForeground(uid+key+"::"+tun, Colors.getColor(fontPath+"buttons icil on"));
								GUI.setGradient(uid+key+"::"+tun, Colors.getGradient(fontPath+"buttons icil on"));
							}
						}
					});
					ccap.addBut(but, uid+key+"::"+tun);
				}
			}
			
			for(int i=0; i<2; i++) {
				final int ii = i;
				Button but = new Button();
				but.setPos(x++, 0);
				but.setText(i == 0 ? "\u2764" : "\u2B59");
				if(i == 0) {
					if(val < 0) {
						but.setForeground(Colors.getColor(fontPath+"buttons heart def"));
						but.setGradient(Colors.getGradient(fontPath+"buttons heart def"));
					} else if(val == 0 || val == Integer.MAX_VALUE-1) {
						but.setForeground(Colors.getColor(fontPath+"buttons heart cat"));
						but.setGradient(Colors.getGradient(fontPath+"buttons heart cat"));
					} else {
						but.setForeground(Colors.getColor(fontPath+"buttons heart on"));
						but.setGradient(Colors.getGradient(fontPath+"buttons heart on"));
					}
				} else {
					if(val > 0) {
						but.setForeground(Colors.getColor(fontPath+"buttons cross def"));
						but.setGradient(Colors.getGradient(fontPath+"buttons cross def"));
					} else if(val == 0 || val == Integer.MIN_VALUE+1) {
						but.setForeground(Colors.getColor(fontPath+"buttons cross cat"));
						but.setGradient(Colors.getGradient(fontPath+"buttons cross cat"));
					} else {
						but.setForeground(Colors.getColor(fontPath+"buttons cross on"));
						but.setGradient(Colors.getGradient(fontPath+"buttons cross on"));
					}
				}
				but.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer val = Configs.getCapInt(cid, lid, tun, list, Configs.fav);
						GUI.removeFromContainer(uid + (fav ? "fav" : "block"), uid+"cap::"+tun);
						if(ii == 0) {
							if(val > 0 && val != Integer.MAX_VALUE-1) {
								Configs.favCap(cid, lid, tun, list, null);
							} else {
								Configs.favCap(cid, lid, tun, list, 1);
								addCap(tun, pos);
							}
						} else {
							if(val < 0 && val != Integer.MIN_VALUE+1) {
								Configs.favCap(cid, lid, tun, list, null);
							} else {
								Configs.favCap(cid, lid, tun, list, -1);
								addCap(tun, pos);
							}
						}
						gui.refresh();
					}
				});
				ccap.addBut(but, uid+(i==0 ? "heart::" : "cross::")+tun);
			}
			
		gui.addToContainer(uid + (fav ? "fav" : "block"), ccap, uid+"cap::"+tun);
		
	}
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}

	
}
