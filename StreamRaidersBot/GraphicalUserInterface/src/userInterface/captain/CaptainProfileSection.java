package userInterface.captain;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import otherlib.Configs;
import otherlib.Logger;
import run.Manager;
import run.ProfileType;
import userInterface.Colors;
import userInterface.MainFrame;

public class CaptainProfileSection {

	private static String[] sc = "Gold Potions Meat EventCurrency Keys Bones".split(" ");
	private static String[] stngsNames = "Profile Units Chests".split(" ");
	
	private final String cid;
	
	public CaptainProfileSection(String cid) {
		this.cid = cid;
	}

	public Container create() {
		
		int p = 0;
		
		Container con = new Container();
		con.setBorder(Colors.getColor("main borders"), 2, 25);
		con.setInsets(5, 2, 5, 2);
		
			Container head = new Container();
			head.setPos(0, 0);
			head.setSpan(5, 1);
			head.setFill('h');
			
				Label pname = new Label();
				pname.setPos(p++, 0);
				pname.setText(Configs.getPStr(cid, Configs.pname));
				pname.setTooltip("Profilename");
				pname.setInsets(2, 2, 2, 20);
				pname.setForeground(Colors.getColor("main labels"));
				head.addLabel(pname, MainFrame.pspre+cid+"::pname");
				
				Button bvie = new Button();
				bvie.setPos(p++, 0);
				bvie.setText("switch");
				bvie.setTooltip("switch to viewer");
				bvie.setGradient(Colors.getGradient("main buttons def"));
				bvie.setForeground(Colors.getColor("main buttons def"));
				bvie.setAL((ae) -> {
					new Thread(() -> {
						try {
							GUI.setEnabled(MainFrame.pspre+cid+"::switch", false);
							Manager.switchProfileType(cid);
							GUI.setEnabled(MainFrame.pspre+cid+"::switch", true);
							update();
						} catch (Exception e) {
							Logger.printException("CaptainProfileSection -> (head) -> switchProfileType: error=failed to switch profile type", e, Logger.runerr, Logger.error, cid, null, true);
						}
					}).start();
				});
				head.addBut(bvie, MainFrame.pspre+cid+"::switch");
				
				for(String key : sc) {
					Label slab = new Label();
					slab.setPos(p++, 0);
					slab.setText(key+":");
					slab.setTooltip("Amount of "+key);
					slab.setForeground(Colors.getColor("main labels"));
					head.addLabel(slab);
					
					Label lab = new Label();
					lab.setPos(p++, 0);
					lab.setText("???");
					lab.setTooltip("Amount of "+key);
					lab.setInsets(2, 2, 2, 20);
					lab.setForeground(Colors.getColor("main labels"));
					head.addLabel(lab, MainFrame.pspre+cid+"::"+key.toLowerCase());
				}
				
				Label s1 = new Label();
				s1.setPos(p++, 0);
				s1.setText("");
				s1.setWeightX(1);
				head.addLabel(s1);
				
				Label layer = new Label();
				layer.setPos(p++, 0);
				layer.setText("(default)");
				layer.setTooltip("current active Layer");
				layer.setForeground(Colors.getColor("main labels"));
				head.addLabel(layer, MainFrame.pspre+cid+"::layer");
				
				Label laycol = new Label();
				laycol.setPos(p++, 0);
				laycol.setSize(25, 25);
				laycol.setText("");
				laycol.setTooltip("Layer color");
				laycol.setBackground(Color.LIGHT_GRAY);
				laycol.setOpaque(true);
				head.addLabel(laycol, MainFrame.pspre+cid+"::laycol");
				
			con.addContainer(head);
			
			p = 0;
			
			Container cr = new Container();
			cr.setPos(p++, 1);
			cr.setBorder(Colors.getColor("main borders"), 1, 5);
			
				int x = 0;
				int y = 0;
				
			con.addContainer(cr);
			
			Container cs = new Container();
			cs.setPos(p++, 1);
			cs.setBorder(Colors.getColor("main borders"), 1, 5);
			
				x = 0;
				y = 0;
				
			con.addContainer(cs);
			
			/*
			Container ctwitch = new Container();
			ctwitch.setPos(p++, 1);
			ctwitch.setBorder(Colors.getColor("main borders"), 1, 5);
			
				x = 0;
				y = 0;
				
			con.addContainer(ctwitch);
			*/
			/*
			Container cdiscord = new Container();
			cdiscord.setPos(p++, 1);
			cdiscord.setBorder(Colors.getColor("main borders"), 1, 5);
			
				x = 0;
				y = 0;
				
			con.addContainer(cdiscord);
			*/
			/*
			Container ctelegram = new Container();
			ctelegram.setPos(p++, 1);
			ctelegram.setBorder(Colors.getColor("main borders"), 1, 5);
			
				x = 0;
				y = 0;
				
			con.addContainer(ctelegram);
			*/
			
			Container stngs = new Container();
			stngs.setPos(p++, 1);
			stngs.setBorder(Colors.getColor("main borders"), 1, 5);
		
				Button stats = new Button();
				stats.setPos(0, p++);
				stats.setFill('h');
				stats.setText("Stats");
				stats.setTooltip("Opens your Stats");
				stats.setGradient(Colors.getGradient("main buttons def"));
				stats.setForeground(Colors.getColor("main buttons def"));
				stats.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							Manager.getCaptain(cid).useBackEnd(cbe -> new CaptainStats(cid, cbe));
						} catch (Exception e1) {
							Logger.printException("CaptainProfileSection -> openStats: err=failed to load stats", e1, Logger.runerr, Logger.error, cid, null, true);
						}
					}
				});
				stngs.addBut(stats);
				
				Label stngsSymb = new Label();
				stngsSymb.setPos(0, p++);
				stngsSymb.setAnchor("c");
				stngsSymb.setText("\u23E3");
				stngsSymb.setForeground(Colors.getColor("main labels"));
				stngsSymb.setTooltip("Just a Symbol that symbolises that the following Buttons open Windows with Settings");
				stngs.addLabel(stngsSymb);
				
				
				y = 0;
				for(final String key : stngsNames) {
					final String name = (key.equals("Profile") 
											? key
											: key.substring(0, key.length()-1)
										) + "Settings";
					
					Button prof = new Button();
					prof.setPos(0, y++);
					prof.setFill('h');
					prof.setText(key);
					prof.setTooltip("opens "+name);
					prof.setGradient(Colors.getGradient("main buttons def"));
					prof.setForeground(Colors.getColor("main buttons def"));
					prof.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Class.forName("userInterface.captain."+name)
									.getDeclaredConstructor(String.class, String.class, GUI.class)
									.newInstance(cid, Manager.getCurrentLayer(cid), MainFrame.getGUI());
							} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e1) {
								Logger.printException("CaptainProfileSection -> create: err=couldn't get Settings class for " + key, e1, Logger.runerr, Logger.error, null, null, true);
							}
						}
					});
					stngs.addBut(prof);
				}
				
			con.addContainer(stngs);
		
		return con;
	}
	
	private void update() {
		new Thread(() -> {
			String syncTo = Configs.getPStr(cid, Configs.syncedViewer);
			if(syncTo.equals("(none)"))
				syncTo = cid;
			
			for(String s : Manager.getLoadedProfiles())
				if(Configs.getPStr(s, Configs.syncedViewer).equals(cid) || s.equals(cid))
					Manager.updateProfile(s);
		}).start();
	}
}
