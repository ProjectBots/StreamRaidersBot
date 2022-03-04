package userInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import bot.MapGUI;
import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import program.ConfigsV2;
import program.Debug;
import run.Manager;

public class ProfileSection {

	public static final String pre = "ProfileSection::";
	
	
	private static String[] sc = "Gold Potions Meat EventCurrency Keys Bones".split(" ");
	
	private final String cid;
	
	public ProfileSection(String cid) {
		this.cid = cid;
	}
	
	public Container create() {
		
		int p = 0;
		
		Container con = new Container();
		con.setBorder(Fonts.getColor("main borders"), 2, 25);
		con.setInsets(5, 2, 5, 2);

			Container head = new Container();
			head.setPos(0, 0);
			head.setSpan(5, 1);
			head.setFill('h');
			
				Label pname = new Label();
				pname.setPos(p++, 0);
				pname.setText(ConfigsV2.getPStr(cid, ConfigsV2.pname));
				pname.setInsets(2, 2, 2, 20);
				pname.setForeground(Fonts.getColor("main labels"));
				head.addLabel(pname, pre+cid+"::pname");
				
				for(String key : sc) {
					Label slab = new Label();
					slab.setPos(p++, 0);
					slab.setText(key+":");
					slab.setForeground(Fonts.getColor("main labels"));
					head.addLabel(slab);
					
					Label lab = new Label();
					lab.setPos(p++, 0);
					lab.setText("???");
					lab.setInsets(2, 2, 2, 20);
					lab.setForeground(Fonts.getColor("main labels"));
					head.addLabel(lab, pre+cid+"::"+key.toLowerCase());
				}
				
				Label counter = new Label();
				counter.setPos(p++, 0);
				counter.setText("00:00");
				counter.setForeground(Fonts.getColor("main labels"));
				head.addLabel(counter, pre+cid+"::4::time");
				
				Button start = new Button();
				start.setPos(p++, 0);
				start.setText("\u23F5");
				start.setGradient(Fonts.getGradient("main buttons def"));
				start.setForeground(Fonts.getColor("main buttons def"));
				start.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Manager.switchRunning(cid, 4);
					}
				});
				head.addBut(start, pre+cid+"::4::run");
				
				Button skiph = new Button();
				skiph.setPos(p++, 0);
				skiph.setText("\u23E9");
				skiph.setGradient(Fonts.getGradient("main buttons def"));
				skiph.setForeground(Fonts.getColor("main buttons def"));
				skiph.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Manager.skipSleep(cid, 4);
					}
				});
				head.addBut(skiph);
				
				Label s1 = new Label();
				s1.setPos(p++, 0);
				s1.setText("");
				s1.setWeightX(1);
				head.addLabel(s1);
				
				Label layer = new Label();
				layer.setPos(p++, 0);
				layer.setText("(default)");
				layer.setForeground(Fonts.getColor("main labels"));
				head.addLabel(layer, pre+cid+"::layer");
				
				Label laycol = new Label();
				laycol.setPos(p++, 0);
				laycol.setSize(25, 25);
				laycol.setText("");
				laycol.setBackground(Color.LIGHT_GRAY);
				laycol.setOpaque(true);
				head.addLabel(laycol, pre+cid+"::laycol");
				
			con.addContainer(head);
			
			for(int i=0; i<4; i++) {
				p = 0;
				final int ii = i;
				
				Container raid = new Container();
				raid.setPos(i, 1);
				raid.setBorder(Fonts.getColor("main borders"), 1, 5);
				
					Container tsr = new Container();
					tsr.setPos(0, p++);
					tsr.setAnchor("c");
					
						Label time = new Label();
						time.setPos(0, 0);
						time.setText("00:00");
						time.setInsets(2, 2, 2, 10);
						time.setForeground(Fonts.getColor("main labels"));
						tsr.addLabel(time, pre+cid+"::"+i+"::time");
						
						Button run = new Button();
						run.setPos(1, 0);
						run.setText("\u23F5");
						run.setGradient(Fonts.getGradient("main buttons def"));
						run.setForeground(Fonts.getColor("main buttons def"));
						run.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.switchRunning(cid, ii);
							}
						});
						tsr.addBut(run, pre+cid+"::"+i+"::run");
						
						Button skip = new Button();
						skip.setPos(2, 0);
						skip.setText("\u23E9");
						skip.setGradient(Fonts.getGradient("main buttons def"));
						skip.setForeground(Fonts.getColor("main buttons def"));
						skip.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.skipSleep(cid, ii);
							}
						});
						tsr.addBut(skip);
				
					raid.addContainer(tsr);
					
					Label cap = new Label();
					cap.setPos(0, p++);
					cap.setAnchor("c");
					cap.setText("???????");
					cap.setForeground(Fonts.getColor("main labels"));
					raid.addLabel(cap, pre+cid+"::"+i+"::capname");
					
					Image img = new Image("data/Other/icon.png");
					img.setPos(0, p++);
					img.setAnchor("c");
					img.setSquare(100);
					raid.addImage(img, pre+cid+"::"+i+"::img");
					
					Button twitch = new Button();
					twitch.setPos(0, p++);
					twitch.setFill('h');
					twitch.setText("watch");
					twitch.setGradient(Fonts.getGradient("main buttons twitch"));
					twitch.setForeground(Fonts.getColor("main buttons twitch"));
					twitch.setInsets(2, 2, 10, 2);
					twitch.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							MainFrame.openDesktopBrowser(Manager.getTwitchCaptainLink(cid, ii));
						}
					});
					raid.addBut(twitch);
					
					Container cloy = new Container();
					cloy.setPos(0, p++);
					cloy.setAnchor("c");
					
						Label wins = new Label();
						wins.setPos(0, 0);
						wins.setText("??");
						wins.setForeground(Fonts.getColor("main labels"));
						cloy.addLabel(wins, pre+cid+"::"+i+"::wins");
						
						Image loy = new Image("data/LoyaltyPics/noloy.png");
						loy.setPos(1, 0);
						loy.setSquare(20);
						loy.setInsets(2, 15, 2, 15);
						cloy.addImage(loy, pre+cid+"::"+i+"::loy");
						
						Button change = new Button();
						change.setPos(2, 0);
						change.setText("\u267B");
						change.setGradient(Fonts.getGradient("main buttons def"));
						change.setForeground(Fonts.getColor("main buttons def"));
						change.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.switchSlotChangeMarker(cid, ii);
							}
						});
						cloy.addBut(change, pre+cid+"::"+i+"::change");
					
					raid.addContainer(cloy);
					
					Container capActs = new Container();
					capActs.setPos(0, p++);
					
						Button lock = new Button();
						lock.setPos(0, 0);
						lock.setText("\uD83D\uDD13");
						lock.setGradient(Fonts.getGradient("main buttons def"));
						lock.setForeground(Fonts.getColor("main buttons def"));
						lock.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Boolean isLocked = ConfigsV2.isSlotLocked(cid, "(all)", ""+ii);
								if(isLocked == null)
									isLocked = false;
								ConfigsV2.setSlotLocked(cid, "(all)", ""+ii, !isLocked);
								update();
							}
						});
						capActs.addBut(lock, pre+cid+"::"+i+"::lock");
						
						Button fav = new Button();
						fav.setPos(1, 0);
						fav.setText("\u2764");
						fav.setGradient(Fonts.getGradient("main buttons def"));
						fav.setForeground(Fonts.getColor("main buttons def"));
						fav.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.favCaptain(cid, ii, 10);
								update();
							}
						});
						capActs.addBut(fav, pre+cid+"::"+i+"::fav");
						
						Button block = new Button();
						block.setPos(2, 0);
						block.setText("\u2B59");
						block.setGradient(Fonts.getGradient("main buttons def"));
						block.setForeground(Fonts.getColor("main buttons def"));
						block.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.favCaptain(cid, ii, -1);
								update();
							}
						});
						capActs.addBut(block, pre+cid+"::"+i+"::block");
					
					raid.addContainer(capActs);
					
					Container mapchest = new Container();
					mapchest.setPos(0, p++);
					mapchest.setAnchor("c");
					
						Container cmapimg = new Container();
						Image mapimg = new Image("data/Other/map.png");
						mapimg.setSquare(15);
						cmapimg.addImage(mapimg);
						
						Button map = new Button();
						map.setPos(0, 0);
						map.setContainer(cmapimg);
						map.setInsets(2, 2, 2, 10);
						map.setGradient(Fonts.getGradient("main buttons def"));
						map.setForeground(Fonts.getColor("main buttons def"));
						map.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								MapGUI.asGui(Manager.getProfile(cid), ii);
							}
						});
						mapchest.addBut(map);
						
						Image chest = new Image("data/ChestPics/nochest.png");
						chest.setPos(1, 0);
						chest.setAnchor("c");
						chest.setSquare(25);
						mapchest.addImage(chest, pre+cid+"::"+i+"::chest");
					
					raid.addContainer(mapchest);
				
				con.addContainer(raid);
			}
			
			p = 0;
			
			Container stngs = new Container();
			stngs.setPos(4, 1);
			stngs.setBorder(Fonts.getColor("main borders"), 1, 5);
		
				Button stats = new Button();
				stats.setPos(0, p++);
				stats.setFill('h');
				stats.setText("Stats");
				stats.setGradient(Fonts.getGradient("main buttons def"));
				stats.setForeground(Fonts.getColor("main buttons def"));
				stats.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new Stats(cid).open();
					}
				});
				stngs.addBut(stats);
				
				Label stngsSymb = new Label();
				stngsSymb.setPos(0, p++);
				stngsSymb.setAnchor("c");
				stngsSymb.setText("\u23E3");
				stngsSymb.setForeground(Fonts.getColor("main labels"));
				stngs.addLabel(stngsSymb);
				
				String[] stngsNames = "Profile Units Chests Layers Captains Skins".split(" ");
				
				for(String key : stngsNames) {
					Button prof = new Button();
					prof.setPos(0, p++);
					prof.setFill('h');
					prof.setText(key);
					prof.setGradient(Fonts.getGradient("main buttons def"));
					prof.setForeground(Fonts.getColor("main buttons def"));
					prof.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Class<?> sc = Class.forName("userInterface." + (
										key.equals("Profile") 
										? key
										: key.substring(0, key.length()-1)
									) + "Settings");
								
								sc.getDeclaredMethod("open", GUI.class)
									.invoke(sc.getDeclaredConstructor(String.class, String.class)
												.newInstance(cid, Manager.getCurrentLayer(cid)),
											MainFrame.getGUI());
							} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException e1) {
								Debug.printException("ProfileSection -> create: err=couldn't get Settings class for " + key, e1, Debug.runerr, Debug.error, null, null, true);
							}
						}
					});
					stngs.addBut(prof);
				}
				
			con.addContainer(stngs);
			
		return con;
	}

	public void update() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				String syncTo = ConfigsV2.getPStr(cid, ConfigsV2.synced);
				if(syncTo.equals("(none)")) {
					for(String s : Manager.getLoadedProfiles())
						if(ConfigsV2.getPStr(s, ConfigsV2.synced).equals(cid) || s.equals(cid))
							Manager.updateProfile(s);
				} else {
					ProfileSection section = MainFrame.getSections().get(syncTo);
					if(section != null)
						section.update();
					else
						Manager.updateProfile(cid);
				}
				
			}
		});
		t.start();
	}

}
