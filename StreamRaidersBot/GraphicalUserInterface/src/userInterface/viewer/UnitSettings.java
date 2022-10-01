package userInterface.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.Json;
import include.GUI.Button;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextField;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Ressources;
import otherlib.Configs.UniInt;
import otherlib.Configs.UniStr;
import run.Manager;
import run.ProfileType;
import srlib.units.Unit;
import srlib.units.UnitRarity;
import userInterface.AbstractSettings;
import userInterface.Colors;

public class UnitSettings extends AbstractSettings {
	
	private static final String[] DEFAULT_SYNC_LIST = new String[] {"(loading)"};
	private static final int UNIT_TYPE_LEVEL = 100;
	
	private static final String[] prios = "place epic placedun epicdun upgrade unlock dupe buy".split(" ");
	private static final HashSet<String> typeOnlyPrio = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{add("unlock"); add("dupe"); add("buy");}
	};
	private static final String[] opts = "favOnly markerOnly canVibe".split(" ");
	private static final String[] optopts = "nc nd ec ed".split(" ");
	private static final String[] dunEpicModes = "boss ifNeeded first second".split(" ");
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
	
	private static class U implements Comparable<U> {
		@Override
		public int compareTo(U u) {
			int rd = this.ur.rank - u.ur.rank;
			if(rd != 0)
				return rd;
			
			int td = this.typeName.compareTo(u.typeName);
			if(td != 0)
				return td;
			
			if(this.from == null)
				return -1;
			
			if(u.from == null)
				return 1;
			
			int fd = this.from.compareTo(u.from);
			if(fd != 0)
				return fd;
			
			return u.lvl - this.lvl;
		}
		
		@Override
		public String toString() {
			return new StringBuffer("{uId:").append(uId)
					.append(", type:").append(type)
					.append(", lvl:").append(lvl)
					.append(", from:").append(from)
					.append(", sync:").append(sync)
					.append(", hasRow:").append(hasRow)
					.append("}").toString();
		}
		
		final String uId;
		final String from;
		final String type;
		private final String typeName;
		final UnitRarity ur;
		final int lvl;
		final String name;
		String sync;
		boolean hasRow = false;
		
		U(String type, String sync) {
			this.uId = type;
			this.from = null;
			this.type = type;
			this.typeName = Unit.getName(type);
			this.ur = UnitRarity.parseType(type);
			this.lvl = UNIT_TYPE_LEVEL;
			this.name = typeName;
			this.sync = sync;
		}
		
		U(String uId, String type, int lvl, String sync, String from) {
			this.uId = uId;
			this.from = from;
			this.type = type;
			this.typeName = Unit.getName(type);
			this.ur = UnitRarity.parseType(type);
			this.lvl = lvl;
			this.name = genName_().toString();
			this.sync = sync;
		}
		
		U(String uId, String type, int lvl, String pname, String sync, String from) {
			this.uId = uId;
			this.from = from;
			this.type = type;
			this.typeName = Unit.getName(type);
			this.ur = UnitRarity.parseType(type);
			this.lvl = lvl;
			this.name = genName_().append(" (").append(pname).append(")").toString();
			this.sync = sync;
		}
		
		private StringBuilder genName_() {
			return new StringBuilder(typeName).append(" ").append(lvl).append(" (").append(uId).append(")");
		}

		
	}
	
	private final boolean contains_other;
	private final U[] units;
	private final Hashtable<String, U> unitsById;
	
	
	public UnitSettings(String cid, String lid, GUI parrent) {
		super(cid, lid, parrent, 500, 500, true, true);
		
		gui.setGlobalKeyLis(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_R:
						try {
							Manager.getViewer(cid).useBackEnd(vbe -> vbe.updateUnits(true));
							openNewInstance(lid);
						} catch (Exception e1) {
							Logger.printException("UnitSettings -> reload: err=unable to get units/souls", e1, Logger.runerr, Logger.error, cid, null, true);
						}
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				}
			}
		});
		
		addLayerChooser();
		
		ArrayList<String> types = Unit.getTypesList();
		
		String[] unitIDs_raw = Configs.getUnitIdsArray(cid, pt);
		units = new U[unitIDs_raw.length + types.size()];
		String[] froms = new String[unitIDs_raw.length];
		boolean containsOther = false;
		int i = 0;
		for(; i<unitIDs_raw.length; i++) {
			froms[i] = Configs.getUnitInfoStr(cid, unitIDs_raw[i], Configs.fromViewer);
			containsOther |= !froms[i].equals(cid);
		}
		
		unitsById = new Hashtable<>();
		if(containsOther) {
			Hashtable<String, String> cidNameConv = new Hashtable<>();
			for(String c : Configs.getConfigIds())
				cidNameConv.put(c, Configs.getPStr(c, Configs.pname));
			
			for(i=0; i<unitIDs_raw.length; i++) {
				final String type = Configs.getUnitInfoStr(cid, unitIDs_raw[i], Configs.typeViewer);
				units[i] = new U(unitIDs_raw[i], type, 
							Configs.getUnitInfoInt(cid, unitIDs_raw[i], Configs.levelViewer),
							cidNameConv.get(Configs.getUnitInfoStr(cid, unitIDs_raw[i], Configs.fromViewer)),
							Configs.getUnitSync(cid, pt, lid, unitIDs_raw[i]), froms[i]);
			}
		} else {
			for(i=0; i<unitIDs_raw.length; i++) {
				final String type = Configs.getUnitInfoStr(cid, unitIDs_raw[i], Configs.typeViewer);
				units[i] = new U(unitIDs_raw[i], type,
							Configs.getUnitInfoInt(cid, unitIDs_raw[i], Configs.levelViewer),
							Configs.getUnitSync(cid, pt, lid, unitIDs_raw[i]), froms[i]);
			}
		}
		
		
		
		for(int j=0; j<types.size(); j++)
			units[i++] = new U(types.get(j), Configs.getUnitSync(cid, pt, lid, types.get(j)));
		
		Arrays.sort(units);
		
		for(i=0; i<units.length; i++)
			unitsById.put(units[i].uId, units[i]);
		
		contains_other = containsOther;
		
		addContent();
	}

	@Override
	public String getSettingsName() {
		return "Unit";
	}
	
	@Override
	protected ProfileType getProfileType() {
		return ProfileType.VIEWER;
	}

	@Override
	protected void openNewInstance(String lid) {
		new UnitSettings(cid, lid, gui);
	}

	
	@Override
	protected void addContent() {
		
		
		gui.setFullScreen(true);
		
		int p = 1;
		
		Label llp = new Label();
		llp.setPos(p++, g);
		llp.setText("Unit lvl id"+(contains_other?" pn":""));
		llp.setForeground(Colors.getColor(fontPath+"labels"));
		gui.addLabel(llp);
		
		Label lsync = new Label();
		lsync.setPos(p++, g);
		lsync.setText("sync");
		lsync.setForeground(Colors.getColor(fontPath+"labels"));
		gui.addLabel(lsync);
		
		p++;
		for(String key : prios) {
			Label lp = new Label();
			lp.setPos(p++, g);
			lp.setText(key + "");
			lp.setAnchor("c");
			lp.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(lp);
		}
		
		p+=3+chests.size()-optopts.length;
		
		for(String s : opts) {
			Label lp = new Label();
			lp.setPos(p+=optopts.length+1, g);
			lp.setSpan(optopts.length, 1);
			lp.setText(s + "");
			lp.setAnchor("c");
			lp.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(lp);
		}
		
		p+=optopts.length+1;
		
		Label lp = new Label();
		lp.setPos(p, g);
		lp.setSpan(dunEpicModes.length, 1);
		lp.setText("Dungeon Epic Mode");
		lp.setAnchor("c");
		lp.setForeground(Colors.getColor(fontPath+"labels"));
		gui.addLabel(lp);
		
		unitStartPos = ++g;
		
		for(int i=0; i<units.length; i++)
			addUnit(units[i], g++);
		
		updateUnitSync();
	}
	
	private int unitStartPos;
	
	private void addUnit(final U u, final int g) {
		int p = 0;
		
		Image upic = new Image(Ressources.get("UnitPics/"+u.type.replace("allies", ""), java.awt.Image.class));
		upic.setPos(p++, g);
		upic.setSquare(18);
		gui.addImage(upic);
		
		Label lun = new Label();
		lun.setPos(p++, g);
		lun.setText(u.name);
		lun.setForeground(Colors.getColor(fontPath+"labels"));
		gui.addLabel(lun);
		
		ComboBox cbsync = new ComboBox(uid+u.uId+"::sync");
		cbsync.setPos(p++, g);
		cbsync.setList(DEFAULT_SYNC_LIST);
		cbsync.setCL(new CombListener() {
			@Override
			public void unselected(String id, ItemEvent e) {}
			@Override
			public void selected(String id, ItemEvent e) {
				String sel = GUI.getSelected(id);
				//	if sel equals none, it means that it was synced before,
				//	therefore no other unit should be synced with this unit atm
				unsync:
				if(!sel.equals(SYNC_NONE)) {
					//	name to uId
					for(int i=0; i<units.length; i++) {
						if(units[i].name.equals(sel)) {
							sel = units[i].uId;
							break;
						}
					}
					
					if(lid.equals("(all)")) {
						//	most likely the majority of units won't be synced with this.
						//	to save time we make a list of units that are synced to this on at least one layer
						ArrayList<String> unitIdsToTest = new ArrayList<>();
						for(int i=0; i<units.length; i++) {
							if(units[i].sync.contains(u.uId)) {
								unitIdsToTest.add(units[i].uId);
								//	replace uId from this unit with sel if not already present
								//	:: is the separator between uIds if lid equals "(all)"
								units[i].sync = units[i].sync.replace(u.uId, "").replace("::::", "::");
								if(!units[i].sync.contains(sel))
									units[i].sync += "::" + sel;
							}
						}
						
						if(unitIdsToTest.size() == 0)
							//	there are no units synced to this on any layer
							break unsync;
						
						//	go through each layer and sync units that have been synced with this unit to sel
						for(String l : Configs.getLayerIds(cid, pt)) {
							//boolean selUnsync = !Configs.getUnitSync(cid, pt, l, sel).equals(SYNC_NONE);
							for(int i=0; i<unitIdsToTest.size(); i++) {
								if(Configs.getUnitSync(cid, pt, l, unitIdsToTest.get(i)).equals(u.uId)) {
									//if(selUnsync)
									//	Configs.syncUnit(cid, pt, l, sel, null);
									Configs.syncUnit(cid, pt, l, unitIdsToTest.get(i), sel);
								}
							}
						}
					} else {
						for(int i=0; i<units.length; i++) {
							if(units[i].sync.equals(u.uId)) {
								Configs.syncUnit(cid, pt, lid, units[i].uId, sel);
								units[i].sync = sel;
							}
						}
					}
				}
				Configs.syncUnit(cid, pt, lid, u.uId, sel);
				u.sync = sel;
				
				updateUnitSync();
			}
		});
		gui.addComb(cbsync);
		
		if(u.lvl < 20 || u.lvl == UNIT_TYPE_LEVEL)
			addSpecs(u.type, u.uId, g);
		
	}
	

	private static final String SYNC_NONE = "(none)";
	
	private void updateUnitSync() {
		ArrayList<U> syncList = new ArrayList<>(units.length);
		for(int i=0; i<units.length; i++)
				if(units[i].sync.equals(SYNC_NONE))
					syncList.add(units[i]);
		
		for(int i=0; i<units.length; i++) {
			GUI.setCombList(uid+units[i].uId+"::sync", genUnitSyncArray(syncList, units[i]));
			
			if(units[i].sync.equals(SYNC_NONE) != units[i].hasRow) {
				if(units[i].hasRow)
					remRow(units[i]);
				else
					addRow(units[i], unitStartPos + i);
			}
		}
		
		gui.refresh();
		
	}
	
	private static final String[] UNIT_SYNC_ARRAY_DEFAULT_ELEMENTS1 = {SYNC_NONE};
	private static final String[] UNIT_SYNC_ARRAY_DEFAULT_ELEMENTS2 = {"(---)", SYNC_NONE};
	
	private String[] genUnitSyncArray(ArrayList<U> syncList, U u) {
		boolean multiSync = u.sync.contains("::");
		boolean isSynced = !u.sync.equals(SYNC_NONE);
		//	all units that can recieve a sync - u (itself) + (none) + (---) 
		String[] ret = new String[syncList.size() + (isSynced?1:0) + (multiSync?1:0)];
		String[] defArrayElements = multiSync ? UNIT_SYNC_ARRAY_DEFAULT_ELEMENTS2 : UNIT_SYNC_ARRAY_DEFAULT_ELEMENTS1;
		int ind = 0;
		if(isSynced)
				ret[ind++] = unitsById.get(u.sync).name;
		
		System.arraycopy(defArrayElements, 0, ret, ind, defArrayElements.length);
		ind += defArrayElements.length;
		U u_;
		for(int i=0; i<syncList.size(); i++) {
			u_ = syncList.get(i);
			if(u_.uId.equals(isSynced?u.sync:u.uId))
				continue;
			ret[ind++] = u_.name;
		}
		
		return ret;
	}
	
	
	private void addRow(final U u, final int g) {
		int p = 4;
		
		for(final String key : prios) {
			//	some options doesn't make sense to be unit specific
			//	and are only displayed on the type specific rows
			if(u.from != null && typeOnlyPrio.contains(key)) {
				p++;
				continue;
			}
			
			//	legendary unit scrolls can't be bought
			if(key.equals("buy") && Unit.isLegendary(u.type)) {
				p++;
				continue;
			}
			
			String id = uid+u.uId+"::"+key;
			
			Integer val = Configs.getUnitInt(cid, lid, u.uId, new UniInt(key, pt));
			
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
						Configs.setUnitInt(cid, lid, u.uId, new UniInt(key, pt), val);
						GUI.setBackground(id, Color.white);
					} catch (NumberFormatException e1) {
						GUI.setBackground(id, new Color(255, 122, 122));
					}
				}
			});
			
			gui.addTextField(tf, id);
		}
		
		p+=3;
		
		String val = Configs.getUnitString(cid, lid, u.uId, Configs.chestsViewer);
		int c = StringUtils.countMatches(val, "::") + 1;
		
		for(final String s : chests) {
			
			final String id = uid+u.uId+"::chests::"+s;
			
			int m = StringUtils.countMatches(val, s+",");
			Container cimg = new Container();
			Image img = new Image(Ressources.get("ChestPics/"+s, java.awt.Image.class));
			img.setSquare(18);
			cimg.addImage(img);
			
			Button bcs = new Button();
			bcs.setPos(p++, g);
			bcs.setContainer(cimg);
			if(m == c) {
				bcs.setGradient(Colors.getGradient(fontPath+"buttons on"));
				bcs.setForeground(Colors.getColor(fontPath+"buttons on"));
			} else if(m == 0) {
				bcs.setGradient(Colors.getGradient(fontPath+"buttons def"));
				bcs.setForeground(Colors.getColor(fontPath+"buttons def"));
			} else {
				bcs.setGradient(Colors.getGradient(fontPath+"buttons cat"));
				bcs.setForeground(Colors.getColor(fontPath+"buttons cat"));
			}
			bcs.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String val = Configs.getUnitString(cid, lid, u.uId, Configs.chestsViewer);
					if(lid.equals("(all)")) {
						int c = StringUtils.countMatches(val, "::") + 1;
						int m = StringUtils.countMatches(val, s+",");
						if(m == c) {
							for(String lay : Configs.getLayerIds(cid, pt))
								Configs.setUnitString(cid, lay, u.uId, Configs.chestsViewer, Configs.getUnitString(cid, lay, u.uId, Configs.chestsViewer).replace(s+",", ""));
							GUI.setGradient(id, Colors.getGradient(fontPath+"buttons def"));
						} else {
							for(String lay : Configs.getLayerIds(cid, pt)) {
								String old = Configs.getUnitString(cid, lay, u.uId, Configs.chestsViewer);
								if(old.contains(s+","))
									continue;
								old += s+",";
								Configs.setUnitString(cid, lay, u.uId, Configs.chestsViewer, old);
							}
							GUI.setGradient(id, Colors.getGradient(fontPath+"buttons on"));
						}
					} else {
						if(val.contains(s+",")) {
							val = val.replace(s+",", "");
							GUI.setGradient(id, Colors.getGradient(fontPath+"buttons def"));
						} else {
							val += s+",";
							GUI.setGradient(id, Colors.getGradient(fontPath+"buttons on"));
						}
						Configs.setUnitString(cid, lid, u.uId, Configs.chestsViewer, val);
					}
				}
			});
			gui.addBut(bcs, id);
		}
		
		for(final String o : opts) {
			Label space = new Label();
			space.setPos(p++, g);
			space.setSize(15, 1);
			space.setText("");
			gui.addLabel(space, uid+u.uId+"::opt::"+o+"::space1");
			
			final UniStr us = new UniStr(o, pt);
			val = Configs.getUnitString(cid, lid, u.uId, us);
			c = StringUtils.countMatches(val, "::") + 1;
			
			for(final String s : optopts) {
				final String id1 = uid+u.uId+"::opt::"+o+"::"+s;
				
				Button bopt = new Button();
				bopt.setPos(p++, g);
				bopt.setText(s);
				int m = StringUtils.countMatches(val, s);
				if(m == c) {
					bopt.setGradient(Colors.getGradient(fontPath+"buttons on"));
					bopt.setForeground(Colors.getColor(fontPath+"buttons on"));
				} else if(m == 0) {
					bopt.setGradient(Colors.getGradient(fontPath+"buttons def"));
					bopt.setForeground(Colors.getColor(fontPath+"buttons def"));
				} else {
					bopt.setGradient(Colors.getGradient(fontPath+"buttons cat"));
					bopt.setForeground(Colors.getColor(fontPath+"buttons cat"));
				}
				bopt.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String val = Configs.getUnitString(cid, lid, u.uId, us);
						if(lid.equals("(all)")) {
							int c = StringUtils.countMatches(val, "::") + 1;
							int m = StringUtils.countMatches(val, s);
							if(m == c) {
								for(String l : Configs.getLayerIds(cid, pt))
									Configs.setUnitString(cid, l, u.uId, us, Configs.getUnitString(cid, l, u.uId, us).replaceFirst(s+",?", ""));
								GUI.setGradient(id1, Colors.getGradient(fontPath+"buttons def"));
								GUI.setForeground(id1, Colors.getColor(fontPath+"buttons def"));
							} else {
								for(String l : Configs.getLayerIds(cid, pt)) {
									String old = Configs.getUnitString(cid, l, u.uId, us);
									if(old.contains(s))
										continue;
									if(old.equals(""))
										old = s;
									else
										old += ","+s;
									Configs.setUnitString(cid, l, u.uId, us, old);
								}
								GUI.setGradient(id1, Colors.getGradient(fontPath+"buttons on"));
								GUI.setForeground(id1, Colors.getColor(fontPath+"buttons on"));
							}
						} else {
							if(val.contains(s)) {
								val = val.replaceFirst(s+",?", "");
								GUI.setGradient(id1, Colors.getGradient(fontPath+"buttons def"));
								GUI.setForeground(id1, Colors.getColor(fontPath+"buttons def"));
							} else {
								if(val.equals(""))
									val = s;
								else
									val += ","+s;
								GUI.setGradient(id1, Colors.getGradient(fontPath+"buttons on"));
								GUI.setForeground(id1, Colors.getColor(fontPath+"buttons on"));
							}
							Configs.setUnitString(cid, lid, u.uId, us, val);
						}
					}
				});
				gui.addBut(bopt, id1);
			}
		}
		
		Label s1 = new Label();
		s1.setText("");
		s1.setPos(p++, g);
		s1.setSize(20, 1);
		gui.addLabel(s1, uid+u.uId+"::space2");
		
		
		String dem = Configs.getUnitString(cid, lid, u.uId, Configs.dunEpicModeViewer);
		c = StringUtils.countMatches(dem, "::") + 1;
		for(final String s : dunEpicModes) {
			Button bdem = new Button();
			bdem.setPos(p++, g);
			bdem.setText(s);
			int m = StringUtils.countMatches(dem, s);
			if(m == c) {
				bdem.setGradient(Colors.getGradient(fontPath+"buttons on"));
				bdem.setForeground(Colors.getColor(fontPath+"buttons on"));
			} else if(m == 0) {
				bdem.setGradient(Colors.getGradient(fontPath+"buttons def"));
				bdem.setForeground(Colors.getColor(fontPath+"buttons def"));
			} else {
				bdem.setGradient(Colors.getGradient(fontPath+"buttons cat"));
				bdem.setForeground(Colors.getColor(fontPath+"buttons cat"));
			}
			bdem.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Configs.setUnitString(cid, lid, u.uId, Configs.dunEpicModeViewer, s);
					for(final String o : dunEpicModes) {
						GUI.setGradient(uid+u.uId+"::dem::"+o, Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(uid+u.uId+"::dem::"+o, Colors.getColor(fontPath+"buttons def"));
					}
					GUI.setGradient(uid+u.uId+"::dem::"+s, Colors.getGradient(fontPath+"buttons on"));
					GUI.setForeground(uid+u.uId+"::dem::"+s, Colors.getColor(fontPath+"buttons on"));
				}
			});
			gui.addBut(bdem, uid+u.uId+"::dem::"+s);
		}

		u.hasRow = true;
	}
	
	private void remRow(U u) {
		LinkedList<String> ids = new LinkedList<>();
		
		for(String key : prios)
			ids.add(uid+u.uId+"::"+key);
		
		for(final String s : chests)
			ids.add(uid+u.uId+"::chests::"+s);
		
		for(String o : opts) {
			ids.add(uid+u.uId+"::opt::"+o+"::space1");
			for(String s : optopts)
				ids.add(uid+u.uId+"::opt::"+o+"::"+s);
		}
		ids.add(uid+u.uId+"::space2");
		
		for(final String s : dunEpicModes)
			ids.add(uid+u.uId+"::dem::"+s);
		
		while(ids.size() > 0) {
			try {
				gui.removeSilent(ids.removeFirst());
			} catch (NullPointerException e) {}
		}
		u.hasRow = false;
	}
	
	private void addSpecs(final String type, final String unitId, final int g) {
		final JsonArray specs = Unit.getSpecs(type);
		
		String old = Configs.getUnitSpec(cid, pt, lid, unitId);
		
		int p = 12;
		
		for(int i=0; i<3; i++) {
			final String u = specs.get(i).getAsJsonObject().get("uid").getAsString();
			final String id1 = uid+unitId+"::spec::";
			final int ii = i;
			Button buid = new Button();
			buid.setPos(p++, g);
			buid.setText(specs.get(i).getAsJsonObject().get("name").getAsString());
			if(old.equals(u)) {
				buid.setGradient(Colors.getGradient(fontPath+"buttons on"));
				buid.setForeground(Colors.getColor(fontPath+"buttons on"));
			} else if(old.contains(u)) {
				buid.setGradient(Colors.getGradient(fontPath+"buttons cat"));
				buid.setForeground(Colors.getColor(fontPath+"buttons cat"));
			} else {
				buid.setGradient(Colors.getGradient(fontPath+"buttons def"));
				buid.setForeground(Colors.getColor(fontPath+"buttons def"));
			}
			buid.setFill('h');
			buid.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(Configs.getUnitSpec(cid, pt, lid, unitId).equals(u)) {
						Configs.setUnitSpec(cid, pt, lid, unitId, "null");
						GUI.setGradient(id1+ii, Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(id1+ii, Colors.getColor(fontPath+"buttons def"));
					} else {
						Configs.setUnitSpec(cid, pt, lid, unitId, u);
						GUI.setGradient(id1+ii, Colors.getGradient(fontPath+"buttons on"));
						GUI.setForeground(id1+ii, Colors.getColor(fontPath+"buttons on"));
						GUI.setGradient(id1+((ii+1)%3), Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(id1+((ii+1)%3), Colors.getColor(fontPath+"buttons def"));
						GUI.setGradient(id1+((ii+2)%3), Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(id1+((ii+2)%3), Colors.getColor(fontPath+"buttons def"));
					}
				}
			});
			gui.addBut(buid, id1+i);
		}
	}

	

	

}
