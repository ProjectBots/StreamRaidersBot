package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.GUI.Button;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.MouseEvents;
import include.GUI.TextField;
import include.GUI.WinLis;
import include.Maths;
import program.ConfigsV2;

public class LayerSettings {

	public static final String pre = "LayerSettings::";
	
	private final String uid, cid;
	
	public LayerSettings(String cid, String lay) {
		this.cid = cid;
		uid = pre + LocalDateTime.now().toString().hashCode() + "::";
	}
	
	private boolean mouseDown = false;
	
	private String[] sch = new String[288*7];
	
	private static String[] daysOfWeek = "Mo Tu We Th Fr Sa Su".split(" ");
	
	private Hashtable<String, Layer> layers = new Hashtable<>();
	
	private String selcol = null;
	
	private int colpos = 0;
	
	private GUI gui;
	
	private static class Pos {
		private int start = -1;
		private int end = -1;
		
		public void reset() {
			start = -1;
			end = -1;
		}
		
		public void setStart(int p) {
			start = p;
		}
		
		public int getStart() {
			return start;
		}
		
		public void setEnd(int p) {
			end = p;
		}
		
		public int getEnd() {
			return end;
		}
	}
	
	private Pos pos = new Pos();
	
	public void open(GUI parent) {
		
		int y = 0;
		int p = 0;
		
		String[] lays = ConfigsV2.getLayerIds(cid);
		for(String key : lays)
			layers.put(key, new Layer(key, new Color(ConfigsV2.getInt(cid, key, ConfigsV2.color))));
		
		
		gui = new GUI("Layer Settings for " + ConfigsV2.getPStr(cid, ConfigsV2.pname), 700, 800, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("stngs layers background"));
		
		Container csch = new Container();
		csch.setPos(0, y++);
		csch.setInsets(20, 20, 20, 20);
		
		for(int d=0; d<7; d++) {
			for(int m=0; m<288; m++) {
				final int pp = p++;
				
				Color col = Color.lightGray;
				
				if(m%12 == 0) {
					Label dis = new Label();
					dis.setPos(d*2, m);
					dis.setSpan(1, 10);
					dis.setText(""+(m/12));
					dis.setAnchor("ne");
					dis.setForeground(Fonts.getColor("stngs layers numbers"));
					csch.addLabel(dis);
					
					col = Maths.getReadableColor(col);
				}
				
				Label l = new Label();
				l.setPos(d*2+1, m);
				l.setText("");
				l.setInsets(0, 0, 0, 10);
				l.setSize(50, 2);
				l.setBackground(col);
				l.setOpaque(true);
				l.setMouseEvent(new MouseEvents() {
					@Override
					public void pressed(MouseEvent e) {
						mouseDown = true;
						if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
							pos.setStart(snap(pp, false));
						} else if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
							pos.setStart(snap(pp, true));
						} else {
							pos.setStart(pp);
						}
					}
					@Override
					public void exited(MouseEvent e) {}
					@Override
					public void entered(MouseEvent e) {
						if(mouseDown) {
							pos.setEnd(pp);
						}
					}
					@Override
					public void released(MouseEvent e) {
						mouseDown = false;
						int end = pos.getEnd();
						if(end == -1)
							return;
						if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
							pos.setEnd(snap(end, false));
						} else if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
							pos.setEnd(snap(end, true));
						}
						paint(pos.getStart(), pos.getEnd(), selcol);
					}
					@Override
					public void clicked(MouseEvent e) {
						if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
							remove(pp);
						} else if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
							if(!sch[pp].equals("(default)")) {
								GUI change = new GUI("select", 400, 200, gui, null);
								change.setBackgroundGradient(Fonts.getGradient("stngs layers select background"));
								
								final int start = getStartingPoint(pp);
								final int end = getEndingPoint(pp);
								
								change.addWinLis(new WinLis() {
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
										String col = sch[pp];
										remove(pp);
										paint(pos.getStart(), pos.getEnd(), col);
									}
								});
								
								pos.setStart(start);
								pos.setEnd(end);
								
								String[] se = "start end".split(" ");
								int g = 0;
								for(String key : se) {
									Label ls = new Label();
									ls.setPos(0, g);
									ls.setText(key);
									ls.setForeground(Fonts.getColor("stngs layers select labels"));
									change.addLabel(ls);
									
									int s = key.equals("end") ? end : start;
									
									int d = s / 288;
									s = s - (d * 288);
									
									int h = s / 12;
									int m = s % 12 * 5;
									
									TextField tfs = new TextField();
									tfs.setPos(1, g);
									tfs.setSize(80, 22);
									tfs.setText(daysOfWeek[d] + " - " + h + ":" + m);
									tfs.setDocLis(new DocumentListener() {
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
											String in[] = GUI.getInputText(uid+key+"::tf").split(" - ");
											if(in.length != 2) {
												GUI.setBackground(uid+key+"::tf", new Color(255, 122, 122));
												return;
											}
											
											int d = Arrays.asList(daysOfWeek).indexOf(in[0]);
											if(d == -1) {
												GUI.setBackground(uid+key+"::tf", new Color(255, 122, 122));
												return;
											}
											
											String[] t = in[1].split(":");
											if(t.length != 2) {
												GUI.setBackground(uid+key+"::tf", new Color(255, 122, 122));
												return;
											}
											
											int h, m;
											try {
												h = Integer.parseInt(t[0]);
												m = Integer.parseInt(t[1]);
											} catch (NumberFormatException e) {
												GUI.setBackground(uid+key+"::tf", new Color(255, 122, 122));
												return;
											}
											
											if(h > 23 || m > 59 || h < 0 || m < 0 || m%5 != 0) {
												GUI.setBackground(uid+key+"::tf", new Color(255, 122, 122));
												return;
											}
											
											int p = (d * 288) + (h * 12) + (m / 5);
											GUI.setBackground(uid+key+"::tf", Color.white);
											
											if(key.equals("end")) {
												pos.setEnd(p);
											} else {
												pos.setStart(p);
											}
											
										}
									});
									change.addTextField(tfs, uid+key+"::tf");
									
									g++;
								}
								
							}
						}
					}
				});
				csch.addLabel(l, uid+pp);
				
				sch[pp] = "(default)";
			}
		}
		gui.addContainer(csch);
		
		JsonObject times = ConfigsV2.getPObj(cid, ConfigsV2.ptimes);
		for(String t : times.keySet()) {
			String[] sp = t.split("-");
			paint(Integer.parseInt(sp[0]), (Integer.parseInt(sp[1])+1)%sch.length, times.get(t).getAsString());
		}
		
		Button upd = new Button();
		upd.setPos(0, y++);
		upd.setText("update");
		upd.setFill('h');
		upd.setGradient(Fonts.getGradient("stngs layers buttons"));
		upd.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		gui.addBut(upd);
		
		Container al = new Container();
		al.setPos(0, y++);
		
			String rans;
			while(true) {
				rans = Maths.ranString(3);
				if(!ConfigsV2.isLNameTaken(cid, rans))
					break;
			}
			
			TextField nl = new TextField();
			nl.setPos(0, 0);
			nl.setSize(80, 22);
			nl.setText(rans);
			al.addTextField(nl, uid+"nl");
			
			Color ranc;
			while(true) {
				ranc = new Color(Maths.ranInt(-16777216, -1));
				if(!isTaken(ranc))
					break;
			}
			
			Button bcolc = new Button();
			bcolc.setPos(1, 0);
			bcolc.setBackground(ranc);
			bcolc.setSize(25, 25);
			bcolc.setText("");
			bcolc.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Color col = gui.showColorPicker("Choose Color", GUI.getBackgroung(uid+"colorPicker"));
					if(col == null)
						return;
					GUI.setBackground(uid+"colorPicker", col);
				}
			});
			al.addBut(bcolc, uid+"colorPicker");
			
			ComboBox cb = new ComboBox(uid+"cb");
			cb.setPos(3, 0);
			cb.setList(getNames());
			al.addComboBox(cb);
			
			Button addlay = new Button();
			addlay.setPos(4, 0);
			addlay.setText("+");
			addlay.setGradient(Fonts.getGradient("stngs layers buttons"));
			addlay.setForeground(Fonts.getColor("stngs layers buttons"));
			addlay.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String ln = GUI.getInputText(uid+"nl");
					
					if(ConfigsV2.isLNameTaken(cid, ln)) {
						gui.msg("Error Occured", "Name already taken", GUI.MsgConst.WARNING);
						return;
					}
					
					String lid = ""+LocalDateTime.now().toString().hashCode();
					
					final Color col = GUI.getBackgroung(uid+"colorPicker");
					
					if(isTaken(col)) {
						gui.msg("Error Occured", "Color already taken", GUI.MsgConst.WARNING);
						return;
					}
					
					ConfigsV2.addLayer(cid, lid, ln, getByName(GUI.getSelected(uid+"cb")));
					ConfigsV2.setInt(cid, lid, ConfigsV2.color, col.getRGB());
					layers.put(lid, new Layer(lid, col));
					
					GUI.setCombList(uid+"cb", getNames());
					
					addLay(colpos++, lid);
					
					String rans;
					while(true) {
						rans = Maths.ranString(3);
						if(!ConfigsV2.isLNameTaken(cid, rans))
							break;
					}
					
					GUI.setText(uid+"nl", rans);;
					
					
					Color ranc;
					while(true) {
						ranc = new Color(Maths.ranInt(0, 255), Maths.ranInt(0, 255), Maths.ranInt(0, 255));
						if(!isTaken(ranc))
							break;
					}
					
					GUI.setBackground(uid+"colorPicker", ranc);;
					
					gui.refresh();
				}
			});
			al.addBut(addlay);
			
		gui.addContainer(al);
		
		
		for(String l : layers.keySet()) {
			addLay(y++, l);
		}
		
		gui.refresh();
		
		colpos = y;
	}
	
	private void update() {
		JsonObject times = new JsonObject();
		String sel = sch[0];
		int s = 0;
		for(int i=1; i<sch.length; i++) {
			if(!sch[i].equals(sel)) {
				times.addProperty(s+"-"+(i-1), sel);
				s = i;
				sel = sch[i];
			}
		}
		
		times.addProperty(s+"-"+(7*288-1), sel);
		
		ConfigsV2.setPObj(cid, ConfigsV2.ptimes, times);
	}
	
	private void addLay(int y, String lid) {
		if(lid.equals("(default)"))
			return;
		
		Layer lay = layers.get(lid);
		
		Container clay = new Container();
		clay.setPos(0, y);
		
		TextField chna = new TextField();
		chna.setText(lay.getName());
		chna.setPos(0, 0);
		chna.setSize(80, 22);
		chna.setDocLis(new DocumentListener() {
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
				String in = GUI.getInputText(uid+"chna::"+y);
				if(ConfigsV2.isLNameTaken(cid, in) && !in.equals(lay.getName())) {
					GUI.setBackground(uid+"chna::"+y, new Color(255, 122, 122));
					return;
				}
				
				lay.setName(in);
				GUI.setBackground(uid+"chna::"+y, Color.white);
				GUI.setCombList(uid+"cb", getNames());
			}
		});
		clay.addTextField(chna, uid+"chna::"+y);
		
		Button bcol = new Button();
		bcol.setPos(1, 0);
		bcol.setText("");
		bcol.setBackground(lay.getCol());
		bcol.setSize(25, 25);
		bcol.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selcol = lid;
			}
		});
		clay.addBut(bcol, uid+"bcol::"+y);
		
		Button chcol = new Button();
		chcol.setPos(2, 0);
		chcol.setText("change Color");
		chcol.setGradient(Fonts.getGradient("stngs layers buttons"));
		chcol.setForeground(Fonts.getColor("stngs layers buttons"));
		chcol.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color col = gui.showColorPicker("Choose Color", lay.getCol());
				if(col == null)
					return;
				GUI.setBackground(uid+"bcol::"+y, col);
				lay.setCol(col);
				repaint(lid);
			}
		});
		clay.addBut(chcol);
		
		Button rem = new Button();
		rem.setText("X");
		rem.setPos(3, 0);
		rem.setGradient(Fonts.getGradient("stngs layers buttons"));
		rem.setForeground(Fonts.getColor("stngs layers buttons"));
		rem.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!gui.showConfirmationBox("Are you sure to remove this layer?\nAn automatic Update will occur by removing layers"))
					return;
				removeAll(lid);
				layers.get(lid).remove();
				GUI.setCombList(uid+"cb", getNames());
				ConfigsV2.remLayer(cid, lid);
				gui.remove(uid+"clay::"+y);
				gui.refresh();
				update();
			}
		});
		clay.addBut(rem);
		
		gui.addContainer(clay, uid+"clay::"+y);
	}
	
	private JsonArray takenc = new JsonArray();
	private Hashtable<String, String> names = new Hashtable<>();
	
	
	public boolean isTaken(Color col) {
		return takenc.contains(new JsonPrimitive(col.getRGB()));
	}
	
	public String[] getNames() {
		return names.keySet().toArray(new String[names.size()]);
	}
	
	
	public String getByName(String name) {
		return names.get(name);
	}
	
	
	private class Layer {
		private String name;
		private String lay;
		private Color col;
		public Layer(String lay, Color col) {
			this.name = ConfigsV2.getStr(cid, lay, ConfigsV2.lname);
			this.lay = lay;
			names.put(name, lay);
			this.col = col;
			takenc.add(col.getRGB());
		}
		
		public String getName() {
			return name;
		}
		
		public Color getCol() {
			return col;
		}
		
		public void setName(String name) {
			names.remove(this.name);
			this.name = name;
			names.put(name, lay);
			ConfigsV2.setStr(cid, lay, ConfigsV2.lname, name);
		}
		
		public void setCol(Color col) {
			takenc.remove(new JsonPrimitive(this.col.getRGB()));
			this.col = col;
			takenc.add(col.getRGB());
			ConfigsV2.setInt(cid, lay, ConfigsV2.color, col.getRGB());
		}
		
		public void remove() {
			takenc.remove(new JsonPrimitive(this.col.getRGB()));
			names.remove(this.name);
			layers.remove(lay);
		}
	}

	private static int snap(int pp, boolean half) {
		int p = (pp + (half ? 0 : 3)) % 6;
		int ret = -1;
		if(p == 0) {
			ret = pp;
		} else {
			if(p >= 3) {
				ret = pp - p + 6;
			} else {
				ret = pp - p;
			}
		}
		if(ret == 7*288)
			ret = 0;
		return ret;
	}
	
	private int getStartingPoint(int pp) {
		
		String sel = sch[pp];
		
		int s = -1;
		while(sch[pp].equals(sel) && s != pp) {
			if(s == -1)
				s = pp;
			pp--;
			if(pp < 0) 
				pp = 7*288-1;
		}
		
		pp++;
		if(pp == 7*288)
			pp = 0;
		
		return pp;
	}
	
	private int getEndingPoint(int pp) {
		
		String sel = sch[pp];
		
		int s = -1;
		while(sch[pp].equals(sel) && s != pp) {
			if(s == -1)
				s = pp;
			pp++;
			if(pp >= 7*288)
				pp = 0;
		}
		
		return pp;
	}
	
	
	
	private void remove(int pp) {
		String sel = sch[pp];
		
		if(sel.equals("(default)"))
			return;
		
		pp = getStartingPoint(pp);
		
		
		while(sch[pp].equals(sel)) {
			GUI.setBackground(uid+pp, pp%12 == 0 ? Color.black : Color.lightGray);
			sch[pp] = "(default)";
			pp++;
			if(pp >= sch.length)
				pp = 0;
		}
	}
	
	private void removeAll(String l) {
		for(int i=0; i<sch.length; i++) {
			if(sch[i].equals(l)) {
				sch[i] = "(default)";
				GUI.setBackground(uid+i, i%12 == 0 ? Color.black : Color.lightGray);
			}
		}
			
			
	}

	private void paint(int s, int e, String lid) {
		
		if(lid == null || !layers.containsKey(lid))
			return;
		
		if(s == -1 || e == -1)
			return;
		
		Color norm = layers.get(lid).getCol();
		
		while(s != e) {
			GUI.setBackground(uid+s, s%12 == 0 ? Maths.getReadableColor(norm) : norm);
			sch[s] = lid;
			s++;
			if(s >= sch.length)
				s = 0;
		}
		
		pos.reset();
		
	}
	
	private void repaint(String lid) {
		Color norm = layers.get(lid).getCol();
		
		for(int i=0; i<sch.length; i++)
			if(sch[i].equals(lid))
				GUI.setBackground(uid+i, i%12 == 0 ? Maths.getReadableColor(norm) : norm);
		
	}
	
}
