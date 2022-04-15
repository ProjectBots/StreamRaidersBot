package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.WindowConstants;
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
import include.GUI.WinLis;
import include.Http;
import include.Http.NoConnectionException;
import include.Http.NotAllowedProxyException;
import program.ConfigsV2;
import program.Options;
import program.SRC;
import program.Store;
import program.SRR.NotAuthorizedException;
import program.Store.Item;
import run.ViewerBackEnd;
import run.Manager;
import run.Viewer;
import program.ConfigsV2.Boo;
import program.ConfigsV2.Int;
import program.ConfigsV2.SleInt;
import program.ConfigsV2.StorePrioType;
import program.Debug;

public class ProfileSettings {

	public static final String pre = "ProfileSettings::";
	
	private final String uid, cid, lay;
	
	public ProfileSettings(String cid, String lay) {
		this.lay = lay;
		this.cid = cid;
		uid = pre + cid + "::" + LocalDateTime.now().toString().hashCode() + "::";
	}
	
	public void open(GUI parent) {
		Viewer run = Manager.getViewer(cid);
		run.useViewerBackEnd(beh -> {
			open(parent, run, beh);
		});
	}
	
	private void open(GUI parent, Viewer run, ViewerBackEnd beh) {
		
		int p = 0;
		
		GUI gui = new GUI("Profile Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 500, 500, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs profile background"));
		
		gui.addWinLis(new WinLis() {
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
				run.updateProxySettings(beh);
			}
		});
		
		
		Container cname = new Container();
		cname.setPos(0, p++);
		cname.setInsets(20, 2, 2, 2);
		
			Label lname = new Label();
			lname.setPos(0, 0);
			lname.setText("Name: ");
			lname.setForeground(Fonts.getColor("stngs profile labels"));
			cname.addLabel(lname);
			
			TextField tfname = new TextField();
			tfname.setText(ConfigsV2.getPStr(cid, ConfigsV2.pname));
			tfname.setPos(1, 0);
			tfname.setSize(80, 21);
			tfname.setDocLis(new DocumentListener() {
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
					String sel = GUI.getInputText(uid+"name");
					if(ConfigsV2.isPNameTaken(sel))
						GUI.setBackground(uid+"name", new Color(255, 122, 122));
					else {
						GUI.setBackground(uid+"name", Color.white);
						ConfigsV2.setPStr(cid, ConfigsV2.pname, sel);
					}
				}
			});
			cname.addTextField(tfname, uid+"name");
		
		gui.addContainer(cname);
		
		Container csync = new Container();
		csync.setPos(0, p++);
		csync.setInsets(20, 2, 2, 2);
		
			Label lsync = new Label();
			lsync.setPos(0, 0);
			lsync.setText("Sync with: ");
			lsync.setForeground(Fonts.getColor("stngs profile labels"));
			csync.addLabel(lsync);
			
			
			List<String> profiles = ConfigsV2.getCids();
			for(int i=0; i<profiles.size(); i++) {
				String cid_ = profiles.get(i);
				if(cid.equals(cid_) || !ConfigsV2.getPStr(cid_, ConfigsV2.synced).equals("(none)"))
					profiles.remove(i--);
				else
					profiles.set(i, ConfigsV2.getPStr(cid_, ConfigsV2.pname));
			}
			String sel = ConfigsV2.getPStr(cid, ConfigsV2.synced);
			if(!sel.equals("(none)")) {
				sel = ConfigsV2.getPStr(sel, ConfigsV2.pname);
				profiles.remove(sel);
				profiles.add(0, "(none)");
			}
			profiles.add(0, sel);
			
			ComboBox cbsync = new ComboBox(uid+"cbsync");
			cbsync.setPos(1, 0);
			cbsync.setList(profiles.toArray(new String[profiles.size()]));
			cbsync.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String sel = GUI.getSelected(id);
					if(sel.equals("(none)")) {
						ConfigsV2.sync(cid, null);
						return;
					} else {
						if(!gui.showConfirmationBox("do you really want to\nsync your settings?\nThis Profile will have the same\nconfig as the set profile"))
							return;
						for(String dcid : ConfigsV2.getCids()) {
							if(ConfigsV2.getPStr(dcid, ConfigsV2.pname).equals(sel)) {
								ConfigsV2.sync(cid, dcid);
								for(String scid : ConfigsV2.getCids())
									if(ConfigsV2.getPStr(scid, ConfigsV2.synced).equals(cid))
										ConfigsV2.sync(scid, dcid);
								new ProfileSettings(cid, lay).open(gui);
								gui.close();
								return;
							}
						}
					}
				}
			});
			csync.addComboBox(cbsync);
			
		
		gui.addContainer(csync);
		
		Container clay = new Container();
		clay.setPos(0, p++);
		clay.setInsets(20, 2, 2, 2);
		
			Label llay = new Label();
			llay.setPos(0, 0);
			llay.setText("Layer: ");
			llay.setForeground(Fonts.getColor("stngs profile labels"));
			clay.addLabel(llay);
			
			String[] list = ConfigsV2.getLayerIds(cid);
			for(int i=0; i<list.length; i++)
				list[i] = ConfigsV2.getStr(cid, list[i], ConfigsV2.lname);
			
			list = ArrayUtils.insert(0, list, "(all)");
			if(!lay.equals("(all)"))
				list = putFirst(list, ConfigsV2.getStr(cid, lay, ConfigsV2.lname));
		
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
						new ProfileSettings(cid, sel).open(gui);
						gui.close();
						return;
					}
					for(String lay : list) {
						if(sel.equals(ConfigsV2.getStr(cid, lay, ConfigsV2.lname))) {
							new ProfileSettings(cid, lay).open(gui);
							gui.close();
							return;
						}
					}
				}
			});
			clay.addComboBox(cblay);
			
		gui.addContainer(clay);
		
		
		Container cdslot = new Container();
		cdslot.setPos(0, p++);
		cdslot.setInsets(10, 2, 2, 2);
		
			Label ldslot = new Label();
			ldslot.setPos(0, 0);
			ldslot.setText("Dungeon Slot: ");
			ldslot.setForeground(Fonts.getColor("stngs profile labels"));
			cdslot.addLabel(ldslot);
			
			
			String slot = ConfigsV2.getStr(cid, lay, ConfigsV2.dungeonSlot);
			if(slot == null) {
				slot = "(---)";
			} else {
				try {
					slot = ""+(Integer.parseInt(slot)+1);
				} catch (NumberFormatException e) {}
			}
			
			
			ComboBox dslot = new ComboBox(uid+"dslot");
			dslot.setPos(1, 0);
			dslot.setList(putFirst("(none) 1 2 3 4".split(" "), slot));
			dslot.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					try {
						ConfigsV2.setStr(cid, lay, ConfigsV2.dungeonSlot, ""+(Integer.parseInt(GUI.getSelected(id))-1));
					} catch (NumberFormatException e1) {
						ConfigsV2.setStr(cid, lay, ConfigsV2.dungeonSlot, "(none)");
					}
					
				}
			});
			cdslot.addComboBox(dslot);
			
		gui.addContainer(cdslot);
		
		
		Container csleep = new Container();
		csleep.setPos(0, p++);
		csleep.setInsets(10, 2, 2, 2);
		
			Label lsleepg = new Label();
			lsleepg.setPos(0, 0);
			lsleepg.setText("Timer");
			lsleepg.setForeground(Fonts.getColor("stngs profile labels"));
			csleep.addLabel(lsleepg);
			
			for(int i=0; i<5; i++) {
				final int ii = i;
				
				Label lsleep = new Label();
				lsleep.setPos(0, i+1);
				lsleep.setText(i==4 ? "s:" : (i+1)+":");
				lsleep.setForeground(Fonts.getColor("stngs profile labels"));
				csleep.addLabel(lsleep);
				
				Label sleepl = new Label();
				sleepl.setPos(2, i+1);
				sleepl.setText("-");
				sleepl.setForeground(Fonts.getColor("stngs profile labels"));
				csleep.addLabel(sleepl);
				
				for(String m : "min max".split(" ")) {
					
					Integer val = ConfigsV2.getSleepInt(cid, lay, ""+i, new SleInt(m));
					
					TextField tsleep = new TextField();
					tsleep.setText(val == null ? "" : ""+val);
					tsleep.setSize(80, 21);
					tsleep.setPos(m.equals("min") ? 1 : 3, i+1);
					tsleep.setDocLis(new DocumentListener() {
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
								int min = Integer.parseInt(GUI.getInputText(uid+"sleep::min::"+ii));
								int max = Integer.parseInt(GUI.getInputText(uid+"sleep::max::"+ii));
								if(min > max)
									throw new NumberFormatException();
								ConfigsV2.setSleepInt(cid, lay, ""+ii, ConfigsV2.max, max);
								ConfigsV2.setSleepInt(cid, lay, ""+ii, ConfigsV2.min, min);
								GUI.setBackground(uid+"sleep::min::"+ii, Color.white);
								GUI.setBackground(uid+"sleep::max::"+ii, Color.white);
							} catch (NumberFormatException e) {
								GUI.setBackground(uid+"sleep::min::"+ii, new Color(255, 122, 122));
								GUI.setBackground(uid+"sleep::max::"+ii, new Color(255, 122, 122));
							}
						}
					});
					csleep.addTextField(tsleep, uid+"sleep::"+m+"::"+i);
				}
				
				Integer iss = ConfigsV2.getSleepInt(cid, lay, ""+i, ConfigsV2.sync);
				String sss = iss == null
								? "(---)"
								: iss == -1
									? "(none)"
									: iss == 4
										? "s"
										: ""+(iss+1);
				
				ComboBox cbss = new ComboBox(uid+"sleep::sync::"+i);
				cbss.setPos(4, i+1);
				cbss.setList(putFirst("(none) 1 2 3 4 s".split(" "), sss));
				cbss.setCL(new CombListener() {
					@Override
					public void unselected(String id, ItemEvent e) {}
					@Override
					public void selected(String id, ItemEvent e) {
						String in = GUI.getSelected(id);
						int s;
						switch(in) {
						case "(none)":
							ConfigsV2.setSleepInt(cid, lay, ""+ii, ConfigsV2.sync, -1);
							return;
						case "s":
							s = 4;
							break;
						default:
							s = Integer.parseInt(in) - 1;
							break;
						}
						Manager.syncSlots(cid, lay, ii, s);
						
						new ProfileSettings(cid, lay).open(gui);
						gui.close();
					}
				});
				csleep.addComboBox(cbss);
			}
			
		gui.addContainer(csleep);
		
		//TODO after event remove from here
		Container cct = new Container();
		cct.setPos(0, p++);
		cct.setInsets(10, 2, 2, 2);
		
			Label lsct = new Label();
			lsct.setPos(0, 0);
			lsct.setText("use team: ");
			lsct.setForeground(Fonts.getColor("stngs profile labels"));
			cct.addLabel(lsct);
			
			String ct = ConfigsV2.getStr(cid, lay, ConfigsV2.captainTeam);
			if(ct == null)
				ct = "(---)";
			else if(!ct.equals("(none)"))
				ct = ct.split("_")[1];
		
			ComboBox cbct = new ComboBox(uid+"useTeam");
			cbct.setPos(1, 0);
			cbct.setList(putFirst("(none) green blue yellow red pink".split(" "), ct));
			cbct.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String in = GUI.getSelected(id);
					if(!in.equals("(none)"))
						in = "team_"+in+"_02";
					ConfigsV2.setStr(cid, lay, ConfigsV2.captainTeam, in);
				}
			});
			cct.addComboBox(cbct);
		
		gui.addContainer(cct);
		//TODO after event remove to here
		
		Container clock = new Container();
		clock.setPos(0, p++);
		clock.setInsets(10, 2, 2, 2);
		
			Label llock = new Label();
			llock.setText("Locked Slots:");
			llock.setPos(0, 0);
			llock.setSpan(4, 1);
			llock.setForeground(Fonts.getColor("stngs profile labels"));
			clock.addLabel(llock);
			
			for(int i=0; i<4; i++) {
				final int ii = i;
				
				Button lock = new Button();
				lock.setPos(i, 1);
				lock.setText(""+(i+1));
				Boolean val = ConfigsV2.isSlotLocked(cid, lay, ""+i);
				if(val == null) {
					lock.setGradient(Fonts.getGradient("stngs profile buttons cat"));
					lock.setForeground(Fonts.getColor("stngs profile buttons cat"));
				} else if(val) {
					lock.setGradient(Fonts.getGradient("stngs profile buttons on"));
					lock.setForeground(Fonts.getColor("stngs profile buttons on"));
				} else {
					lock.setGradient(Fonts.getGradient("stngs profile buttons def"));
					lock.setForeground(Fonts.getColor("stngs profile buttons def"));
				}
				lock.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Boolean b = ConfigsV2.isSlotLocked(cid, lay, ""+ii);
						if(b == null || !b) {
							ConfigsV2.setSlotLocked(cid, lay, ""+ii, true);
							GUI.setGradient(uid+"lock::"+ii, Fonts.getGradient("stngs profile buttons on"));
							GUI.setForeground(uid+"lock::"+ii, Fonts.getColor("stngs profile buttons on"));
						} else {
							ConfigsV2.setSlotLocked(cid, lay, ""+ii, false);
							GUI.setGradient(uid+"lock::"+ii, Fonts.getGradient("stngs profile buttons def"));
							GUI.setForeground(uid+"lock::"+ii, Fonts.getColor("stngs profile buttons def"));
						}
					}
				});
				clock.addBut(lock, uid+"lock::"+i);
			}
		
		gui.addContainer(clock);
		
		
		Container buts = new Container();
		buts.setPos(0, p++);
		buts.setInsets(10, 2, 2, 2);
			
			int g = 0;
			String[] sbuts = "Prefer Rogues On Treasure Maps  Allow Place First".split("  ");
			if(Options.is("exploits"))
				sbuts = ArrayUtils.addAll(sbuts, "Use Multi Place Exploit  Use Multi Quest Exploit  Use Multi Event Exploit  Use Multi Chest Exploit  Use Multi Unit Exploit".split("  "));
			
			for(String key : sbuts) {
				final Boo con = new Boo(key.substring(0, 1).toLowerCase() + key.substring(1).replace(" ", ""));
				Button cbsb = new Button();
				cbsb.setPos(0, g++);
				cbsb.setText(key);
				cbsb.setFill('h');
				Boolean val = ConfigsV2.getBoolean(cid, lay, con);
				if(val == null) {
					cbsb.setGradient(Fonts.getGradient("stngs profile buttons cat"));
					cbsb.setForeground(Fonts.getColor("stngs profile buttons cat"));
				} else if(val) {
					cbsb.setGradient(Fonts.getGradient("stngs profile buttons on"));
					cbsb.setForeground(Fonts.getColor("stngs profile buttons on"));
				} else {
					cbsb.setGradient(Fonts.getGradient("stngs profile buttons def"));
					cbsb.setForeground(Fonts.getColor("stngs profile buttons def"));
				}
				cbsb.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Boolean b = ConfigsV2.getBoolean(cid, lay, con);
						if(b == null || !b) {
							ConfigsV2.setBoolean(cid, lay, con, true);
							GUI.setGradient(uid+"buts::"+con.get(), Fonts.getGradient("stngs profile buttons on"));
							GUI.setForeground(uid+"buts::"+con.get(), Fonts.getColor("stngs profile buttons on"));
						} else {
							ConfigsV2.setBoolean(cid, lay, con, false);
							GUI.setGradient(uid+"buts::"+con.get(), Fonts.getGradient("stngs profile buttons def"));
							GUI.setForeground(uid+"buts::"+con.get(), Fonts.getColor("stngs profile buttons def"));
						}
					}
				});
				buts.addBut(cbsb, uid+"buts::"+con.get());
			}
		
		gui.addContainer(buts);
		
		
		Container csi = new Container();
		csi.setPos(0, p++);
		csi.setInsets(10, 2, 2, 2);

			int y = 0;
			Label ltsi = new Label();
			ltsi.setPos(0, y++);
			ltsi.setText("Special Store:");
			ltsi.setForeground(Fonts.getColor("stngs profile labels"));
			csi.addLabel(ltsi);
			
			boolean did_sth = false;
			
			for(final int s : new int[]{0,1}) {
				final String section;
				final StorePrioType spt;
				final String cur;
				switch(s) {
				case 0:
					section = SRC.Store.dungeon;
					spt = ConfigsV2.keys;
					cur = Store.keys.get();
					break;
				case 1:
					section = SRC.Store.Event;
					spt = ConfigsV2.event;
					cur = Options.get("currentEventCurrency");
					break;
				default:
					//	to lazy to handle properly bcs can never happen
					section = null;
					spt = null;
					cur = null;
				}
				
				List<Item> items;
				List<Item> items_;
				try {
					items = beh.getAvailableEventStoreItems(section, true);
					items_ = beh.getAvailableEventStoreItems(section, false);
				} catch (NoConnectionException | NotAuthorizedException e3) {
					Debug.printException("ProfileSettings -> open -> specialShop: err=unable to get items", e3, Debug.runerr, Debug.error, ConfigsV2.getPStr(cid, ConfigsV2.pname), null, true);
					gui.close();
					return;
				}
				HashSet<String> gotPrios = new HashSet<>();
				for(Item item : items) {
					final String iuid = item.getStr("Uid");
					gotPrios.add(iuid);
					
					Label lsi = new Label();
					lsi.setPos(0, y);
					lsi.setText(iuid+" ("+item.getItem()+")"+(item.getQuantity()==-1?"":" x"+item.getQuantity())+" @"+item.getPrice()+" "+cur);
					lsi.setForeground(Fonts.getColor("stngs profile labels"));
					csi.addLabel(lsi);
					
					Integer prioint = ConfigsV2.getStorePrioInt(cid, lay, spt, iuid);
					
					TextField tfsi = new TextField();
					tfsi.setText(prioint == null ? "" : ""+prioint);
					tfsi.setSize(55, 21);
					tfsi.setPos(1, y);
					tfsi.setDocLis(new DocumentListener() {
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
								ConfigsV2.setStorePrioInt(cid, lay, spt, iuid, Integer.parseInt(GUI.getInputText(uid+"storePrios::tf::"+iuid)));
								GUI.setBackground(uid+"storePrios::tf::"+iuid, Color.white);
							} catch (NumberFormatException e) {
								GUI.setBackground(uid+"storePrios::tf::"+iuid, new Color(255, 122, 122));
							}
						}
					});;
					csi.addTextField(tfsi, uid+"storePrios::tf::"+iuid);
					
					Label lsip = new Label();
					lsip.setPos(2, y);
					lsip.setText(items_.contains(item) ? "" : "a.b.");
					lsip.setForeground(Fonts.getColor("stngs profile labels"));
					csi.addLabel(lsip);
					
					y++;
				}
				
				HashSet<String> all = ConfigsV2.getStorePrioList(cid, lay, spt);
				for(String pr : new ArrayList<>(all))
					if(!gotPrios.contains(pr))
						ConfigsV2.remStorePrioInt(cid, lay, spt, pr);
				
				did_sth = did_sth || (items.size()>0);
			}
			
			
			if(!did_sth) {
				Label lsi = new Label();
				lsi.setPos(0, y++);
				lsi.setText("Nothing to show currently :(");
				lsi.setForeground(Fonts.getColor("stngs profile labels"));
				csi.addLabel(lsi);
			}
		
		gui.addContainer(csi);
		
		Container csr = new Container();
		csr.setPos(0, p++);
		csr.setInsets(10, 2, 2, 2);
			
			Label lstm = new Label();
			lstm.setPos(0, 0);
			lstm.setSpan(3, 1);
			lstm.setText("Store Refreshes:");
			lstm.setForeground(Fonts.getColor("stngs profile labels"));
			csr.addLabel(lstm);
			
			for(int l=0; l<4; l++) {
				final int ll = l;
				Label lst = new Label();
				lst.setAnchor("c");
				lst.setText(l == 3 ? "3+" : ""+l);
				lst.setPos(0, l+1);
				lst.setForeground(Fonts.getColor("stngs profile labels"));
				csr.addLabel(lst);
				
				Integer refint = ConfigsV2.getStoreRefreshInt(cid, lay, l);
				
				TextField tfsr = new TextField();
				tfsr.setText(refint == null ? "" : ""+refint);
				tfsr.setSize(80, 21);
				tfsr.setPos(1, l+1);
				tfsr.setDocLis(new DocumentListener() {
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
							ConfigsV2.setStoreRefreshInt(cid, lay, ll, Integer.parseInt(GUI.getInputText(uid+"storeRefresh::tf::"+ll)));
							GUI.setBackground(uid+"storeRefresh::tf::"+ll, Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"storeRefresh::tf::"+ll, new Color(255, 122, 122));
						}
					}
				});;
				csr.addTextField(tfsr, uid+"storeRefresh::tf::"+l);
				
				Label lsr = new Label();
				lsr.setText("gold");
				lsr.setPos(2, l+1);
				lsr.setForeground(Fonts.getColor("stngs profile labels"));
				csr.addLabel(lsr);
			}
			
		gui.addContainer(csr);
		
		
		Container cupd = new Container();
		cupd.setPos(0, p++);
		cupd.setInsets(10, 2, 2, 2);
		
			int im = 0;
			
			Label lupd = new Label();
			lupd.setPos(im++, 0);
			lupd.setText("Unit place delay");
			lupd.setForeground(Fonts.getColor("stngs profile labels"));
			lupd.setAnchor("c");
			cupd.addLabel(lupd);
			
			String[] mm = "min max".split(" ");
			
			for(String m : mm) {
				Integer minmax = ConfigsV2.getUnitPlaceDelayInt(cid, lay, m.equals("min") ? ConfigsV2.minu : ConfigsV2.maxu);
				
				TextField tupd = new TextField();
				tupd.setPos(im++, 0);
				tupd.setText(minmax == null ? "" : ""+(minmax / (float) 1000));
				tupd.setSize(80, 23);
				tupd.setDocLis(new DocumentListener() {
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
							int min = (int) Math.round(Float.parseFloat(GUI.getInputText(uid+"tupd::min")) * 1000);
							int max = (int) Math.round(Float.parseFloat(GUI.getInputText(uid+"tupd::max")) * 1000);
							if(min > max || max < 0 || min < 0)
								throw new NumberFormatException();
							ConfigsV2.setUnitPlaceDelayInt(cid, lay, ConfigsV2.minu, min);
							ConfigsV2.setUnitPlaceDelayInt(cid, lay, ConfigsV2.maxu, max);
							GUI.setBackground(uid+"tupd::min", Color.white);
							GUI.setBackground(uid+"tupd::max", Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"tupd::min", new Color(255, 122, 122));
							GUI.setBackground(uid+"tupd::max", new Color(255, 122, 122));
						}
					}
				});
				cupd.addTextField(tupd, uid+"tupd::"+m);
			}
			
		gui.addContainer(cupd);
		
		
		Container cstorem = new Container();
		cstorem.setPos(0, p++);
		cstorem.setInsets(10, 0, 0, 0);
			
			String[] smgs = "Scrolls Min Gold  Store Min Keys  Store Min Eventcurrency  Upgrade Min Gold  Unlock Min Gold  Unit Place Retries  Map Reload After X Retries  Max Unit Per Raid  Cap Inactive Treshold  Unit Update  Raid Update  Map Update  Store Update  Skin Update  Quest Event Rewards Update  Caps Update".split("  ");
			int l = 0;
			for(String key : smgs) {
				final Int con = new Int(key.substring(0, 1).toLowerCase() + key.substring(1).replace(" ", ""));
				
				Label lstorem = new Label();
				lstorem.setText(key);
				lstorem.setPos(0, l);
				lstorem.setForeground(Fonts.getColor("stngs profile labels"));
				cstorem.addLabel(lstorem);
				
				Integer val = ConfigsV2.getInt(cid, lay, con);
				
				TextField tfstorem = new TextField();
				tfstorem.setText(val == null ? "" : ""+val);
				tfstorem.setPos(1, l);
				tfstorem.setSize(80, 21);
				tfstorem.setDocLis(new DocumentListener() {
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
							ConfigsV2.setInt(cid, lay, con, Integer.parseInt(GUI.getInputText(uid+"storem::"+con.get())));
							GUI.setBackground(uid+"storem::"+con.get(), Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"storem::"+con.get(), new Color(255, 122, 122));
						}
					}
				});
				cstorem.addTextField(tfstorem, uid+"storem::"+con.get());
				
				l++;
			}
		
		gui.addContainer(cstorem);
		
		Container cua = new Container();
		cua.setPos(0, p++);
		cua.setInsets(10, 2, 2, 2);
		
			Label lua = new Label();
			lua.setText("User Agent");
			lua.setPos(0, 0);
			lua.setForeground(Fonts.getColor("stngs profile labels"));
			cua.addLabel(lua);
			
			String sua = ConfigsV2.getStr(cid, lay, ConfigsV2.userAgent);
			
			TextField tfua = new TextField();
			tfua.setText(sua == null ? "" : sua);
			tfua.setPos(1, 0);
			tfua.setSize(200, 21);
			tfua.setDocLis(new DocumentListener() {
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
						ConfigsV2.setStr(cid, lay, ConfigsV2.userAgent, GUI.getInputText(uid+"user-agent"));
						GUI.setBackground(uid+"user-agent", Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+"user-agent", new Color(255, 122, 122));
					}
				}
			});
			cua.addTextField(tfua, uid+"user-agent");
		
		gui.addContainer(cua);
		
		
		Container cproxy = new Container();
		cproxy.setPos(0, p++);
		cproxy.setInsets(10, 0, 0, 0);
			
			l = 0;
			
			Label lpd = new Label();
			lpd.setText("Proxy Domain");
			lpd.setPos(0, l);
			lpd.setForeground(Fonts.getColor("stngs profile labels"));
			cproxy.addLabel(lpd);
			
			String pd = ConfigsV2.getStr(cid, lay, ConfigsV2.proxyDomain);
			
			TextField tfpd = new TextField();
			tfpd.setText(pd == null ? "" : pd);
			tfpd.setPos(1, l++);
			tfpd.setSize(160, 21);
			tfpd.setDocLis(new DocumentListener() {
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
					ConfigsV2.setStr(cid, lay, ConfigsV2.proxyDomain, GUI.getInputText(uid+"proxy::domain"));
				}
			});
			cproxy.addTextField(tfpd, uid+"proxy::domain");
			
			Label lpp = new Label();
			lpp.setText("Proxy Port");
			lpp.setPos(0, l);
			lpp.setForeground(Fonts.getColor("stngs profile labels"));
			cproxy.addLabel(lpp);
			
			Integer val = ConfigsV2.getInt(cid, lay, ConfigsV2.proxyPort);
			
			TextField tfpp = new TextField();
			tfpp.setText(val == null ? "" : ""+val);
			tfpp.setPos(1, l++);
			tfpp.setSize(80, 21);
			tfpp.setDocLis(new DocumentListener() {
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
						ConfigsV2.setInt(cid, lay, ConfigsV2.proxyPort, Integer.parseInt(GUI.getInputText(uid+"proxy::port")));
						GUI.setBackground(uid+"proxy::port", Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+"proxy::port", new Color(255, 122, 122));
					}
				}
			});
			cproxy.addTextField(tfpp, uid+"proxy::port");
			
			
			Label lpu = new Label();
			lpu.setText("Proxy User");
			lpu.setPos(0, l);
			lpu.setForeground(Fonts.getColor("stngs profile labels"));
			cproxy.addLabel(lpu);
			
			String pu = ConfigsV2.getStr(cid, lay, ConfigsV2.proxyUser);
			
			TextField tfpu = new TextField();
			tfpu.setText(pu == null ? "" : pu);
			tfpu.setPos(1, l++);
			tfpu.setSize(160, 21);
			tfpu.setDocLis(new DocumentListener() {
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
					ConfigsV2.setStr(cid, lay, ConfigsV2.proxyUser, GUI.getInputText(uid+"proxy::user"));
				}
			});
			cproxy.addTextField(tfpu, uid+"proxy::user");
			
			Label lpw = new Label();
			lpw.setText("Proxy Password");
			lpw.setPos(0, l);
			lpw.setForeground(Fonts.getColor("stngs profile labels"));
			cproxy.addLabel(lpw);
			
			String pw = ConfigsV2.getStr(cid, lay, ConfigsV2.proxyPass);
			
			TextField tfpw = new TextField();
			tfpw.setText(pw == null ? "" : pw);
			tfpw.setPos(1, l++);
			tfpw.setSize(160, 21);
			tfpw.setDocLis(new DocumentListener() {
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
					ConfigsV2.setStr(cid, lay, ConfigsV2.proxyPass, GUI.getInputText(uid+"proxy::pass"));
				}
			});
			cproxy.addTextField(tfpw, uid+"proxy::pass");
			
			
			
			Boolean pb = ConfigsV2.getBoolean(cid, lay, ConfigsV2.proxyMandatory);
			
			Button bpm = new Button();
			bpm.setPos(0, l++);
			bpm.setSpan(2, 1);
			bpm.setText("Proxy Mandatory");
			if(pb == null) {
				bpm.setGradient(Fonts.getGradient("stngs profile buttons cat"));
				bpm.setForeground(Fonts.getColor("stngs profile buttons cat"));
			} else if(pb) {
				bpm.setGradient(Fonts.getGradient("stngs profile buttons on"));
				bpm.setForeground(Fonts.getColor("stngs profile buttons on"));
			} else {
				bpm.setGradient(Fonts.getGradient("stngs profile buttons def"));
				bpm.setForeground(Fonts.getColor("stngs profile buttons def"));
			}
			bpm.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Boolean b = ConfigsV2.getBoolean(cid, lay, ConfigsV2.proxyMandatory);
					if(b == null || !b) {
						ConfigsV2.setBoolean(cid, lay, ConfigsV2.proxyMandatory, true);
						GUI.setGradient(uid+"proxy::mandatory", Fonts.getGradient("stngs profile buttons on"));
						GUI.setForeground(uid+"proxy::mandatory", Fonts.getColor("stngs profile buttons on"));
					} else {
						ConfigsV2.setBoolean(cid, lay, ConfigsV2.proxyMandatory, false);
						GUI.setGradient(uid+"proxy::mandatory", Fonts.getGradient("stngs profile buttons def"));
						GUI.setForeground(uid+"proxy::mandatory", Fonts.getColor("stngs profile buttons def"));
					}
				}
			});
			cproxy.addBut(bpm, uid+"proxy::mandatory");
			
			Button bpt = new Button();
			bpt.setPos(0, l++);
			bpt.setText("Proxy Test");
			bpt.setSpan(2, 1);
			bpt.setGradient(Fonts.getGradient("stngs profile buttons def"));
			bpt.setForeground(Fonts.getColor("stngs profile buttons def"));
			bpt.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GUI gt = new GUI("Proxy Test", 400, 150, gui, null);
					gt.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					
					Label l = new Label();
					l.setText("Testing ...");
					gt.addLabel(l);
					
					gt.refresh();
					
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String user = ConfigsV2.getStr(cid, lay, ConfigsV2.proxyUser);
								String[] ips = Http.testProxy(ConfigsV2.getStr(cid, lay, ConfigsV2.proxyDomain), 
										ConfigsV2.getInt(cid, lay, ConfigsV2.proxyPort),
										user.equals("") ? null : user,
										ConfigsV2.getStr(cid, lay, ConfigsV2.proxyPass)
										);
								if(ips == null) 
									gt.msg("Failed", "Test Failed: Cant validate ip", GUI.MsgConst.ERROR);
								else
									gt.msg("Connection established", "Your ip: " + ips[0] + "\nNew ip: " + ips[1], GUI.MsgConst.INFO);
							} catch (URISyntaxException e1) {
								gt.msg("Failed", "Test Failed: No Connection", GUI.MsgConst.ERROR);
							} catch (NotAllowedProxyException e) {
								gt.msg("Failed", "Test Failed: Not Allowed", GUI.MsgConst.ERROR);
							} catch (IllegalArgumentException e2) {
								gt.msg("Failed", "Test Failed: Illegal Arguments", GUI.MsgConst.ERROR);
							} catch (Exception e) {
								gt.msg("Failed", "Test Failed: sth went terribly wrong", GUI.MsgConst.ERROR);
							}
							
							gt.close();
						}
					});
					t.start();
					
				}
			});
			cproxy.addBut(bpt);
			
			
		gui.addContainer(cproxy);
		
		
		Button resStat = new Button();
		resStat.setPos(0, p++);
		resStat.setInsets(20, 10, 20, 2);
		resStat.setText("Reset Stats");
		resStat.setTooltip("Reset the stats for this profile");
		resStat.setGradient(Fonts.getGradient("stngs profile buttons def"));
		resStat.setForeground(Fonts.getColor("stngs profile buttons def"));
		resStat.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("Reset Stats?")) {
					ConfigsV2.getProfile(cid).remove(ConfigsV2.stats.get());
					ConfigsV2.check(cid);
				}
			}
		});
		gui.addBut(resStat);
		
		
		Button bdelpro = new Button();
		bdelpro.setPos(0, p++);
		bdelpro.setInsets(20, 10, 20, 2);
		bdelpro.setText("Delete Profile");
		bdelpro.setTooltip("Removes this Profile from the bot");
		bdelpro.setGradient(Fonts.getGradient("stngs profile buttons def"));
		bdelpro.setForeground(Fonts.getColor("stngs profile buttons def"));
		bdelpro.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("delete " + ConfigsV2.getPStr(cid, ConfigsV2.pname) + "?")) {
					gui.close();
					Manager.remProfile(cid);
				}
			}
		});
		gui.addBut(bdelpro);
		
	}
	
	private static <T>T[] putFirst(T[] arr, T item) {
		return ArrayUtils.insert(0, ArrayUtils.removeElement(arr, item), item);
	}
	
}
