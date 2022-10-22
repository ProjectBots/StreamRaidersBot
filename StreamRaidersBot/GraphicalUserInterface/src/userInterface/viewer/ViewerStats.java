package userInterface.viewer;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.Hashtable;
import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.ProgressBar;
import include.GUI.TextArea;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Ressources;
import run.AbstractProfile;
import run.viewer.Viewer;
import run.viewer.ViewerBackEnd;
import srlib.SRR.NotAuthorizedException;
import srlib.store.Store;
import srlib.units.Unit;
import srlib.units.UnitType;
import userInterface.AbstractStats;
import userInterface.Colors;


public class ViewerStats extends AbstractStats<Viewer, ViewerBackEnd> {
	
	private static final AffineTransform affinetransform = new AffineTransform();     
	private static final FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
	private static final Font font = new Font("Arial", Font.PLAIN, 12);
	
	public ViewerStats(String cid, GUI parent) {
		super(cid, parent);
	}
	
	@Override
	public void open(Viewer run, ViewerBackEnd vbe) {
		
		int[] pos = new int[4];
		
		Unit[] units;
		Hashtable<String, Integer> curs;
		try {
			units = vbe.getUnits(false);
			curs = vbe.getCurrencies();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("Stats -> open: err=failed to get infos", e, Logger.general, Logger.error, run.cid, null, true);
			return;
		}
		Hashtable<Short, Hashtable<String, Integer>> rews = run.getRews();
		JsonObject stats = Configs.getUObj(cid, Configs.statsViewer);
		
		gui.setBackgroundGradient(Colors.getGradient("VIEWER stats background"));
		gui.setFullScreen(true);
		
		gui.setGlobalKeyLis(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				} else if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_R:
						try {
							vbe.updateUnits(true);
							vbe.updateStore(true);
							new ViewerStats(cid, gui);
						} catch (Exception e1) {
							Logger.printException("Stats -> open -> reload: err=failed to update Units/Store", e1, Logger.runerr, Logger.error, run.cid, null, true);
						}
						break;
					}
				} else if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
					switch(e.getKeyCode()) {
					}
				}
			}
		});
		
		Container crews = new Container();
		crews.setPos(0, 0);
		crews.setSpan(20, 1);
		crews.setInsets(20, 20, 2, 2);
			
			Label today = new Label();
			today.setPos(0, 0);
			today.setText("Today");
			today.setForeground(Colors.getColor("VIEWER stats labels"));
			crews.addLabel(today);
			
			Label past = new Label();
			past.setPos(1, 0);
			past.setText("Past");
			past.setForeground(Colors.getColor("VIEWER stats labels"));
			past.setInsets(2, 20, 2, 2);
			crews.addLabel(past);
			
			Container cft = new Container();
			cft.setPos(0, 1);
			
			Container cfp = new Container();
			cfp.setPos(1, 1);
			cfp.setInsets(2, 20, 2, 2);
				
				int p = 0;
				for(Short rs : rews.keySet()) {
					String rsn = AbstractProfile.getRewSouceName(rs);
					
					StringBuffer sbt = new StringBuffer();
					sbt.append("--- ").append(rsn).append(" ---");
					
					StringBuffer sbp = new StringBuffer();
					sbp.append("--- ").append(rsn).append(" ---");
					
					Hashtable<String, Integer> rews_ = rews.get(rs);
					JsonObject stats_ = stats.getAsJsonObject(rsn);
					for(String r : rews_.keySet()) {
						int w = (int) font.getStringBounds(r, frc).getWidth();
						String b = "\n" + r + "\u0009" + (w < 106 ? "\u0009" : "") + (w < 53 ? "\u0009" : "");
						sbt.append(b).append(rews_.get(r));
						sbp.append(b).append(stats_.has(r) ? stats_.get(r).getAsInt() : 0);
					}
						
					
					TextArea ta = new TextArea();
					ta.setMargin(new Insets(10, 10, 10, 40));
					ta.setPos(p, 0);
					ta.setText(sbt.toString());
					ta.setEditable(false);
					ta.setTabSize(5);
					cft.addTextArea(ta);
					
					ta = new TextArea();
					ta.setMargin(new Insets(10, 10, 10, 40));
					ta.setPos(p++, 0);
					ta.setText(sbp.toString());
					ta.setEditable(false);
					ta.setTabSize(5);
					cfp.addTextArea(ta);
					
					p++;
				}
				
			crews.addContainer(cft);
			
			crews.addContainer(cfp);
			
		gui.addContainer(crews);
		
		
		for(final Unit u : units) {
			final UnitType type = u.type;
			
			Container uc = new Container();
			uc.setPos(pos[type.rarity.rank]++, type.rarity.rank+1);
			uc.setBorder(Colors.getColor("VIEWER stats borders"), 2, 25);
			uc.setFill('b');
			uc.setInsets(5, 5, 5, 5);
				
				Label name = new Label();
				name.setPos(0, 0);
				name.setText(u.getDisName() + " - " + u.level);
				name.setAnchor("c");
				name.setForeground(Colors.getColor("VIEWER stats labels"));
				uc.addLabel(name);
				
				Image img = new Image(Ressources.get("UnitPics/" + type.uid.replace("allies", ""), java.awt.Image.class));
				img.setPos(0, 1);
				img.setSquare(80);
				img.setAnchor("c");
				img.setInsets(10, 2, 5, 2);
				uc.addImage(img);
				
				Integer val = curs.get(type.uid.replace("allies", ""));
				if(val == null)
					val = 0;
				
				if(u.level != 30) {
					int need = Store.getCost(type.rarity, u.level, false)[1];
					
					
					Label nums = new Label();
					nums.setPos(0, 2);
					nums.setText(val + "/" + need + " Scrolls");
					nums.setAnchor("c");
					nums.setForeground(Colors.getColor("VIEWER stats labels"));
					uc.addLabel(nums);
					
					ProgressBar pb = new ProgressBar();
					pb.setPos(0, 3);
					pb.setMinMax(0, need);
					pb.setVal(val);
					pb.setOrientation('h');
					pb.setGradient(Colors.getGradient("VIEWER stats progressBars"));
					uc.addProBar(pb);
				} else {
					Label nums = new Label();
					nums.setPos(0, 2);
					nums.setText(val + " Scrolls");
					nums.setAnchor("c");
					nums.setForeground(Colors.getColor("VIEWER stats labels"));
					uc.addLabel(nums);
				}
				
			gui.addContainer(uc);
		}
		
		
		gui.refresh();
	}

}
