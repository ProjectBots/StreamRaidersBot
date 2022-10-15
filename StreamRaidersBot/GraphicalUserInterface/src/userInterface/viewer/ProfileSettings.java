package userInterface.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
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
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Configs.Boo;
import otherlib.Configs.Int;
import otherlib.Configs.SleInt;
import otherlib.Configs.StorePrioType;
import run.viewer.Viewer;
import srlib.SRC;
import srlib.SRR.NotAuthorizedException;
import srlib.store.Item;
import srlib.store.Store;
import userInterface.AbstractSettings;
import userInterface.Colors;
import run.Manager;
import run.ProfileType;

public class ProfileSettings extends AbstractSettings {
	
	private final Viewer v;

	protected ProfileSettings(String cid, String lid, GUI parent) {
		super(cid, lid, parent, 500, 500, true, true);

		v = Manager.getViewer(cid);
		addHead();
		addLayerChooser();
		addContent();
	}

	@Override
	protected String getSettingsName() {
		return "Profile";
	}
	
	@Override
	protected ProfileType getProfileType() {
		return ProfileType.VIEWER;
	}

	@Override
	protected void openNewInstance(String lid) {
		new ProfileSettings(cid, lid, gui);
	}
	
	private void addHead() {
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
				v.updateVbe();
			}
		});
		
		
		Container cname = new Container();
		cname.setPos(0, g++);
		cname.setInsets(20, 2, 2, 2);
		
			Label lname = new Label();
			lname.setPos(0, 0);
			lname.setText("Name: ");
			lname.setForeground(Colors.getColor(fontPath+"labels"));
			cname.addLabel(lname);
			
			TextField tfname = new TextField();
			tfname.setText(Configs.getPStr(cid, Configs.pname));
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
					if(Configs.isPNameTaken(sel))
						GUI.setBackground(uid+"name", new Color(255, 122, 122));
					else {
						GUI.setBackground(uid+"name", Color.white);
						Configs.setPStr(cid, Configs.pname, sel);
					}
				}
			});
			cname.addTextField(tfname, uid+"name");
		
		gui.addContainer(cname);
		
		Container csync = new Container();
		csync.setPos(0, g++);
		csync.setInsets(20, 2, 2, 2);
		
			Label lsync = new Label();
			lsync.setPos(0, 0);
			lsync.setText("Sync with: ");
			lsync.setForeground(Colors.getColor(fontPath+"labels"));
			csync.addLabel(lsync);
			
			
			ArrayList<String> profiles = Configs.getConfigIds();
			for(int i=0; i<profiles.size(); i++) {
				String cid_ = profiles.get(i);
				if(cid.equals(cid_) || !Configs.getPStr(cid_, Configs.syncedViewer).equals("(none)"))
					profiles.remove(i--);
				else
					profiles.set(i, Configs.getPStr(cid_, Configs.pname));
			}
			String sel = Configs.getPStr(cid, Configs.syncedViewer);
			if(!sel.equals("(none)")) {
				sel = Configs.getPStr(sel, Configs.pname);
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
						Configs.syncProfile(cid, null, ProfileType.VIEWER);
						return;
					} else {
						if(!gui.showConfirmationBox("do you really want to\nsync your settings?\nThis Profile will have the same\nconfig as the set profile"))
							return;
						for(String dcid : Configs.getConfigIds()) {
							if(Configs.getPStr(dcid, Configs.pname).equals(sel)) {
								Configs.syncProfile(cid, dcid, ProfileType.VIEWER);
								for(String scid : Configs.getConfigIds())
									if(Configs.getPStr(scid, Configs.syncedViewer).equals(cid))
										Configs.syncProfile(scid, dcid, ProfileType.VIEWER);
								openNewInstance(lid);
								return;
							}
						}
					}
				}
			});
			csync.addComboBox(cbsync);
			
		
		gui.addContainer(csync);
	}

	
	@Override
	protected void addContent() {
		Container cdslot = new Container();
		cdslot.setPos(0, g++);
		cdslot.setInsets(10, 2, 2, 2);
		
			Label ldslot = new Label();
			ldslot.setPos(0, 0);
			ldslot.setText("Dungeon Slot: ");
			ldslot.setForeground(Colors.getColor(fontPath+"labels"));
			cdslot.addLabel(ldslot);
			
			
			String slot = Configs.getStr(cid, lid, Configs.dungeonSlotViewer);
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
						Configs.setStr(cid, lid, Configs.dungeonSlotViewer, ""+(Integer.parseInt(GUI.getSelected(id))-1));
					} catch (NumberFormatException e1) {
						Configs.setStr(cid, lid, Configs.dungeonSlotViewer, "(none)");
					}
					
				}
			});
			cdslot.addComboBox(dslot);
			
		gui.addContainer(cdslot);
		
		
		Container csleep = new Container();
		csleep.setPos(0, g++);
		csleep.setInsets(10, 2, 2, 2);
		
			Label lsleepg = new Label();
			lsleepg.setPos(0, 0);
			lsleepg.setText("Timer");
			lsleepg.setForeground(Colors.getColor(fontPath+"labels"));
			csleep.addLabel(lsleepg);
			
			for(int i=0; i<5; i++) {
				final int ii = i;
				
				Label lsleep = new Label();
				lsleep.setPos(0, i+1);
				lsleep.setText(i==4 ? "s:" : (i+1)+":");
				lsleep.setForeground(Colors.getColor(fontPath+"labels"));
				csleep.addLabel(lsleep);
				
				Label sleepl = new Label();
				sleepl.setPos(2, i+1);
				sleepl.setText("-");
				sleepl.setForeground(Colors.getColor(fontPath+"labels"));
				csleep.addLabel(sleepl);
				
				for(String m : "min max".split(" ")) {
					
					Integer val = Configs.getSleepInt(cid, lid, ""+i, new SleInt(m, ProfileType.VIEWER));
					
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
								Configs.setSleepInt(cid, lid, ""+ii, Configs.maxViewer, max);
								Configs.setSleepInt(cid, lid, ""+ii, Configs.minViewer, min);
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
				
				Integer iss = Configs.getSleepInt(cid, lid, ""+i, Configs.syncSlotViewer);
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
							Configs.setSleepInt(cid, lid, ""+ii, Configs.syncSlotViewer, -1);
							return;
						case "s":
							s = 4;
							break;
						default:
							s = Integer.parseInt(in) - 1;
							break;
						}
						Manager.syncSlots(cid, lid, ii, s);
						
						openNewInstance(lid);
					}
				});
				csleep.addComboBox(cbss);
			}
			
		gui.addContainer(csleep);
		
		Container clock = new Container();
		clock.setPos(0, g++);
		clock.setInsets(10, 2, 2, 2);
		
			Label llock = new Label();
			llock.setText("Locked Slots:");
			llock.setPos(0, 0);
			llock.setSpan(4, 1);
			llock.setForeground(Colors.getColor(fontPath+"labels"));
			clock.addLabel(llock);
			
			for(int i=0; i<4; i++) {
				final int ii = i;
				
				Button lock = new Button();
				lock.setPos(i, 1);
				lock.setText(""+(i+1));
				Boolean val = Configs.isSlotLocked(cid, lid, ""+i);
				if(val == null) {
					lock.setGradient(Colors.getGradient(fontPath+"buttons cat"));
					lock.setForeground(Colors.getColor(fontPath+"buttons cat"));
				} else if(val) {
					lock.setGradient(Colors.getGradient(fontPath+"buttons on"));
					lock.setForeground(Colors.getColor(fontPath+"buttons on"));
				} else {
					lock.setGradient(Colors.getGradient(fontPath+"buttons def"));
					lock.setForeground(Colors.getColor(fontPath+"buttons def"));
				}
				lock.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Boolean b = Configs.isSlotLocked(cid, lid, ""+ii);
						if(b == null || !b) {
							Configs.setSlotLocked(cid, lid, ""+ii, true);
							GUI.setGradient(uid+"lock::"+ii, Colors.getGradient(fontPath+"buttons on"));
							GUI.setForeground(uid+"lock::"+ii, Colors.getColor(fontPath+"buttons on"));
						} else {
							Configs.setSlotLocked(cid, lid, ""+ii, false);
							GUI.setGradient(uid+"lock::"+ii, Colors.getGradient(fontPath+"buttons def"));
							GUI.setForeground(uid+"lock::"+ii, Colors.getColor(fontPath+"buttons def"));
						}
					}
				});
				clock.addBut(lock, uid+"lock::"+i);
			}
		
		gui.addContainer(clock);
		
		
		Container buts = new Container();
		buts.setPos(0, g++);
		buts.setInsets(10, 2, 2, 2);
			
			int y = 0;
			String[] sbuts = "Prefer Rogues On Treasure Maps  Allow Place First  Store Price As Default Prio  Use Skin From Captain".split("  ");
			if(Options.is("exploits"))
				sbuts = ArrayUtils.addAll(sbuts, "Use Multi Place Exploit  Use Multi Quest Exploit  Use Multi Event Exploit  Use Multi Chest Exploit  Use Multi Unit Exploit".split("  "));
			
			for(String key : sbuts) {
				final Boo con = new Boo(key.substring(0, 1).toLowerCase() + key.substring(1).replace(" ", ""), ProfileType.VIEWER);
				Button cbsb = new Button();
				cbsb.setPos(0, y++);
				cbsb.setText(key);
				cbsb.setFill('h');
				Boolean val = Configs.getBoolean(cid, lid, con);
				if(val == null) {
					cbsb.setGradient(Colors.getGradient(fontPath+"buttons cat"));
					cbsb.setForeground(Colors.getColor(fontPath+"buttons cat"));
				} else if(val) {
					cbsb.setGradient(Colors.getGradient(fontPath+"buttons on"));
					cbsb.setForeground(Colors.getColor(fontPath+"buttons on"));
				} else {
					cbsb.setGradient(Colors.getGradient(fontPath+"buttons def"));
					cbsb.setForeground(Colors.getColor(fontPath+"buttons def"));
				}
				cbsb.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Boolean b = Configs.getBoolean(cid, lid, con);
						if(b == null || !b) {
							Configs.setBoolean(cid, lid, con, true);
							GUI.setGradient(uid+"buts::"+con.con, Colors.getGradient(fontPath+"buttons on"));
							GUI.setForeground(uid+"buts::"+con.con, Colors.getColor(fontPath+"buttons on"));
						} else {
							Configs.setBoolean(cid, lid, con, false);
							GUI.setGradient(uid+"buts::"+con.con, Colors.getGradient(fontPath+"buttons def"));
							GUI.setForeground(uid+"buts::"+con.con, Colors.getColor(fontPath+"buttons def"));
						}
					}
				});
				buts.addBut(cbsb, uid+"buts::"+con.con);
			}
		
		gui.addContainer(buts);
		
		
		Container csi = new Container();
		csi.setPos(0, g++);
		csi.setInsets(10, 2, 2, 2);

			y = 0;
			Label ltsi = new Label();
			ltsi.setPos(0, y++);
			ltsi.setText("Special Store:");
			ltsi.setForeground(Colors.getColor(fontPath+"labels"));
			csi.addLabel(ltsi);
			
			boolean did_sth = false;
			
			for(final int s : new int[]{0,1}) {
				final String section;
				final StorePrioType spt;
				final String cur;
				switch(s) {
				case 0:
					section = SRC.Store.dungeon;
					spt = Configs.keysViewer;
					cur = Store.keys.get();
					break;
				case 1:
					section = SRC.Store.event;
					spt = Configs.eventViewer;
					cur = Options.get("currentEventCurrency");
					break;
				default:
					//	can not happen
					section = null;
					spt = null;
					cur = null;
				}
				
				ArrayList<Item> items;
				try {
					items = v.getBackEnd().getAvailableEventStoreItems(section, true);
				} catch (NoConnectionException | NotAuthorizedException e3) {
					Logger.printException("ProfileSettings -> open -> specialShop: err=unable to get items", e3, Logger.runerr, Logger.error, cid, null, true);
					gui.close();
					return;
				}
				HashSet<String> gotPrios = new HashSet<>();
				for(Item item : items) {
					final String iuid = item.uid;
					gotPrios.add(iuid);
					
					Label lsi = new Label();
					lsi.setPos(0, y);
					lsi.setText(iuid+" ("+item.name+")"+(item.quantity==-1?"":" x"+item.quantity)+" @"+item.price+" "+cur);
					lsi.setForeground(Colors.getColor(fontPath+"labels"));
					csi.addLabel(lsi);
					
					Integer prioint = Configs.getStorePrioInt(cid, lid, spt, iuid);
					
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
								Configs.setStorePrioInt(cid, lid, spt, iuid, Integer.parseInt(GUI.getInputText(uid+"storePrios::tf::"+iuid)));
								GUI.setBackground(uid+"storePrios::tf::"+iuid, Color.white);
							} catch (NumberFormatException e) {
								GUI.setBackground(uid+"storePrios::tf::"+iuid, new Color(255, 122, 122));
							}
						}
					});;
					csi.addTextField(tfsi, uid+"storePrios::tf::"+iuid);
					
					
					
					Label lsip = new Label();
					lsip.setPos(2, y);
					lsip.setText(item.purchased ? "a.b." : "");
					lsip.setForeground(Colors.getColor(fontPath+"labels"));
					csi.addLabel(lsip);
					
					y++;
				}
				
				HashSet<String> all = Configs.getStorePrioList(cid, lid, spt);
				for(String pr : new ArrayList<>(all))
					if(!gotPrios.contains(pr))
						Configs.remStorePrioInt(cid, lid, spt, pr);
				
				did_sth = did_sth || (items.size()>0);
			}
			
			
			if(!did_sth) {
				Label lsi = new Label();
				lsi.setPos(0, y++);
				lsi.setText("Nothing to show currently :(");
				lsi.setForeground(Colors.getColor(fontPath+"labels"));
				csi.addLabel(lsi);
			}
		
		gui.addContainer(csi);
		
		Container csr = new Container();
		csr.setPos(0, g++);
		csr.setInsets(10, 2, 2, 2);
			
			Label lstm = new Label();
			lstm.setPos(0, 0);
			lstm.setSpan(3, 1);
			lstm.setText("Store Refreshes:");
			lstm.setForeground(Colors.getColor(fontPath+"labels"));
			csr.addLabel(lstm);
			
			for(int l=0; l<4; l++) {
				final int ll = l;
				Label lst = new Label();
				lst.setAnchor("c");
				lst.setText(l == 3 ? "3+" : ""+l);
				lst.setPos(0, l+1);
				lst.setForeground(Colors.getColor(fontPath+"labels"));
				csr.addLabel(lst);
				
				Integer refint = Configs.getStoreRefreshInt(cid, ProfileType.VIEWER, lid, l);
				
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
							Configs.setStoreRefreshInt(cid, ProfileType.VIEWER, lid, ll, Integer.parseInt(GUI.getInputText(uid+"storeRefresh::tf::"+ll)));
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
				lsr.setForeground(Colors.getColor(fontPath+"labels"));
				csr.addLabel(lsr);
			}
			
		gui.addContainer(csr);
		
		
		Container cupd = new Container();
		cupd.setPos(0, g++);
		cupd.setInsets(10, 2, 2, 2);
		
			int im = 0;
			
			Label lupd = new Label();
			lupd.setPos(im++, 0);
			lupd.setText("Unit place delay");
			lupd.setForeground(Colors.getColor(fontPath+"labels"));
			lupd.setAnchor("c");
			cupd.addLabel(lupd);
			
			String[] mm = "min max".split(" ");
			
			for(String m : mm) {
				Integer minmax = Configs.getInt(cid, lid, m.equals("min") ? Configs.unitPlaceDelayMinViewer : Configs.unitPlaceDelayMaxViewer);
				
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
							Configs.setInt(cid, lid, Configs.unitPlaceDelayMinViewer, min);
							Configs.setInt(cid, lid, Configs.unitPlaceDelayMaxViewer, max);
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
		cstorem.setPos(0, g++);
		cstorem.setInsets(10, 0, 0, 0);
			
			String[] smgs = "Store Min Gold  Store Min Keys  Store Min Eventcurrency  Upgrade Min Gold  Unlock Min Gold  Unit Place Retries  Map Reload After X Retries  Max Unit Per Raid  Cap Inactive Treshold  Unit Update  Raid Update  Map Update  Store Update  Skin Update  Quest Event Rewards Update  Caps Update  Souls Update".split("  ");
			int l = 0;
			for(String key : smgs) {
				final Int con = new Int(key.substring(0, 1).toLowerCase() + key.substring(1).replace(" ", ""), ProfileType.VIEWER);
				
				Label lstorem = new Label();
				lstorem.setText(key);
				lstorem.setPos(0, l);
				lstorem.setForeground(Colors.getColor(fontPath+"labels"));
				cstorem.addLabel(lstorem);

				Integer val = Configs.getInt(cid, lid, con);
				
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
							Configs.setInt(cid, lid, con, Integer.parseInt(GUI.getInputText(uid+"storem::"+con.con)));
							GUI.setBackground(uid+"storem::"+con.con, Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+"storem::"+con.con, new Color(255, 122, 122));
						}
					}
				});
				cstorem.addTextField(tfstorem, uid+"storem::"+con.con);
				
				l++;
			}
		
		gui.addContainer(cstorem);
		
		Container cua = new Container();
		cua.setPos(0, g++);
		cua.setInsets(10, 2, 2, 2);
		
			Label lua = new Label();
			lua.setText("User Agent");
			lua.setPos(0, 0);
			lua.setForeground(Colors.getColor(fontPath+"labels"));
			cua.addLabel(lua);
			
			String sua = Configs.getStr(cid, lid, Configs.userAgentViewer);
			
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
						Configs.setStr(cid, lid, Configs.userAgentViewer, GUI.getInputText(uid+"user-agent"));
						GUI.setBackground(uid+"user-agent", Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+"user-agent", new Color(255, 122, 122));
					}
				}
			});
			cua.addTextField(tfua, uid+"user-agent");
		
		gui.addContainer(cua);
		
		
		Container cproxy = new Container();
		cproxy.setPos(0, g++);
		cproxy.setInsets(10, 0, 0, 0);
			
			l = 0;
			
			Label lpd = new Label();
			lpd.setText("Proxy Domain");
			lpd.setPos(0, l);
			lpd.setForeground(Colors.getColor(fontPath+"labels"));
			cproxy.addLabel(lpd);
			
			String pd = Configs.getStr(cid, lid, Configs.proxyDomainViewer);
			
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
					Configs.setStr(cid, lid, Configs.proxyDomainViewer, GUI.getInputText(uid+"proxy::domain"));
				}
			});
			cproxy.addTextField(tfpd, uid+"proxy::domain");
			
			Label lpp = new Label();
			lpp.setText("Proxy Port");
			lpp.setPos(0, l);
			lpp.setForeground(Colors.getColor(fontPath+"labels"));
			cproxy.addLabel(lpp);
			
			Integer val = Configs.getInt(cid, lid, Configs.proxyPortViewer);
			
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
						Configs.setInt(cid, lid, Configs.proxyPortViewer, Integer.parseInt(GUI.getInputText(uid+"proxy::port")));
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
			lpu.setForeground(Colors.getColor(fontPath+"labels"));
			cproxy.addLabel(lpu);
			
			String pu = Configs.getStr(cid, lid, Configs.proxyUserViewer);
			
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
					Configs.setStr(cid, lid, Configs.proxyUserViewer, GUI.getInputText(uid+"proxy::user"));
				}
			});
			cproxy.addTextField(tfpu, uid+"proxy::user");
			
			Label lpw = new Label();
			lpw.setText("Proxy Password");
			lpw.setPos(0, l);
			lpw.setForeground(Colors.getColor(fontPath+"labels"));
			cproxy.addLabel(lpw);
			
			String pw = Configs.getStr(cid, lid, Configs.proxyPassViewer);
			
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
					Configs.setStr(cid, lid, Configs.proxyPassViewer, GUI.getInputText(uid+"proxy::pass"));
				}
			});
			cproxy.addTextField(tfpw, uid+"proxy::pass");
			
			
			
			Boolean pb = Configs.getBoolean(cid, lid, Configs.proxyMandatoryViewer);
			
			Button bpm = new Button();
			bpm.setPos(0, l++);
			bpm.setSpan(2, 1);
			bpm.setText("Proxy Mandatory");
			if(pb == null) {
				bpm.setGradient(Colors.getGradient(fontPath+"buttons cat"));
				bpm.setForeground(Colors.getColor(fontPath+"buttons cat"));
			} else if(pb) {
				bpm.setGradient(Colors.getGradient(fontPath+"buttons on"));
				bpm.setForeground(Colors.getColor(fontPath+"buttons on"));
			} else {
				bpm.setGradient(Colors.getGradient(fontPath+"buttons def"));
				bpm.setForeground(Colors.getColor(fontPath+"buttons def"));
			}
			bpm.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Boolean b = Configs.getBoolean(cid, lid, Configs.proxyMandatoryViewer);
					if(b == null || !b) {
						Configs.setBoolean(cid, lid, Configs.proxyMandatoryViewer, true);
						GUI.setGradient(uid+"proxy::mandatory", Colors.getGradient(fontPath+"buttons on"));
						GUI.setForeground(uid+"proxy::mandatory", Colors.getColor(fontPath+"buttons on"));
					} else {
						Configs.setBoolean(cid, lid, Configs.proxyMandatoryViewer, false);
						GUI.setGradient(uid+"proxy::mandatory", Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(uid+"proxy::mandatory", Colors.getColor(fontPath+"buttons def"));
					}
				}
			});
			cproxy.addBut(bpm, uid+"proxy::mandatory");
			
			Button bpt = new Button();
			bpt.setPos(0, l++);
			bpt.setText("Proxy Test");
			bpt.setSpan(2, 1);
			bpt.setGradient(Colors.getGradient(fontPath+"buttons def"));
			bpt.setForeground(Colors.getColor(fontPath+"buttons def"));
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
								String user = Configs.getStr(cid, lid, Configs.proxyUserViewer);
								String[] ips = Http.testProxy(Configs.getStr(cid, lid, Configs.proxyDomainViewer), 
										Configs.getInt(cid, lid, Configs.proxyPortViewer),
										user.equals("") ? null : user,
										Configs.getStr(cid, lid, Configs.proxyPassViewer)
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
		
		
		Button resUnits = new Button();
		resUnits.setPos(0, g++);
		resUnits.setText("Reset Units");
		resUnits.setInsets(20, 10, 2, 2);
		resUnits.setTooltip("Reset saved units for this profile");
		resUnits.setGradient(Colors.getGradient(fontPath+"buttons def"));
		resUnits.setForeground(Colors.getColor(fontPath+"buttons def"));
		resUnits.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("Reset Units?"))
					Configs.clearUnitInfo(cid, pt);
			}
		});
		gui.addBut(resUnits);
		
		Button resStat = new Button();
		resStat.setPos(0, g++);
		resStat.setInsets(2, 10, 20, 2);
		resStat.setText("Reset Stats");
		resStat.setTooltip("Reset the stats for this profile");
		resStat.setGradient(Colors.getGradient(fontPath+"buttons def"));
		resStat.setForeground(Colors.getColor(fontPath+"buttons def"));
		resStat.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("Reset Stats?")) {
					Configs.getProfile(cid).remove(Configs.statsViewer.con);
					Configs.check(cid);
				}
			}
		});
		gui.addBut(resStat);
		
		
		Button bdelpro = new Button();
		bdelpro.setPos(0, g++);
		bdelpro.setInsets(20, 10, 20, 2);
		bdelpro.setText("Delete Profile");
		bdelpro.setTooltip("Removes this Profile from the bot");
		bdelpro.setGradient(Colors.getGradient(fontPath+"buttons def"));
		bdelpro.setForeground(Colors.getColor(fontPath+"buttons def"));
		bdelpro.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.showConfirmationBox("delete " + Configs.getPStr(cid, Configs.pname) + "?")) {
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
