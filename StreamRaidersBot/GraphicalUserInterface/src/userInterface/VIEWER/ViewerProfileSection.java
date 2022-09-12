package userInterface.VIEWER;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import otherlib.Configs;
import otherlib.Logger;
import run.Manager;
import run.ProfileType;
import userInterface.Colors;
import userInterface.MainFrame;
import userInterface.MapGUI;

public class ViewerProfileSection {

	public static final String pre = "ViewerProfileSection::";
	
	
	private static String[] sc = "Gold Potions Meat EventCurrency Keys Bones".split(" ");
	
	private final String cid;
	
	public ViewerProfileSection(String cid) {
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
				head.addLabel(pname, pre+cid+"::pname");
				
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
					head.addLabel(lab, pre+cid+"::"+key.toLowerCase());
				}
				
				Label counter = new Label();
				counter.setPos(p++, 0);
				counter.setText("00:00");
				counter.setTooltip("Time until next Check");
				counter.setForeground(Colors.getColor("main labels"));
				head.addLabel(counter, pre+cid+"::4::time");
				
				Button start = new Button();
				start.setPos(p++, 0);
				start.setText("\u23F5");
				start.setTooltip("Start/Stop Slot");
				start.setGradient(Colors.getGradient("main buttons def"));
				start.setForeground(Colors.getColor("main buttons def"));
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
				skiph.setTooltip("Skip Timer");
				skiph.setGradient(Colors.getGradient("main buttons def"));
				skiph.setForeground(Colors.getColor("main buttons def"));
				skiph.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Manager.skipSleep(cid, 4);
					}
				});
				head.addBut(skiph, pre+cid+"::4::skip");
				
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
				head.addLabel(layer, pre+cid+"::layer");
				
				Label laycol = new Label();
				laycol.setPos(p++, 0);
				laycol.setSize(25, 25);
				laycol.setText("");
				laycol.setTooltip("Layer color");
				laycol.setBackground(Color.LIGHT_GRAY);
				laycol.setOpaque(true);
				head.addLabel(laycol, pre+cid+"::laycol");
				
			con.addContainer(head);
			
			for(int i=0; i<4; i++) {
				p = 0;
				final int ii = i;
				
				Container raid = new Container();
				raid.setPos(i, 1);
				raid.setBorder(Colors.getColor("main borders"), 1, 5);
				
					Container tsr = new Container();
					tsr.setPos(0, p++);
					tsr.setAnchor("c");
					
						Label time = new Label();
						time.setPos(0, 0);
						time.setText("00:00");
						time.setTooltip("Timer until next Check");
						time.setInsets(2, 2, 2, 10);
						time.setForeground(Colors.getColor("main labels"));
						tsr.addLabel(time, pre+cid+"::"+i+"::time");
						
						Button run = new Button();
						run.setPos(1, 0);
						run.setText("\u23F5");
						run.setTooltip("Start/Stop Slot");
						run.setGradient(Colors.getGradient("main buttons def"));
						run.setForeground(Colors.getColor("main buttons def"));
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
						skip.setTooltip("Skip Timer");
						skip.setGradient(Colors.getGradient("main buttons def"));
						skip.setForeground(Colors.getColor("main buttons def"));
						skip.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.skipSleep(cid, ii);
							}
						});
						tsr.addBut(skip, pre+cid+"::"+i+"::skip");
				
					raid.addContainer(tsr);
					
					Label cap = new Label();
					cap.setPos(0, p++);
					cap.setAnchor("c");
					cap.setText("???????");
					cap.setTooltip("Captain Name");
					cap.setForeground(Colors.getColor("main labels"));
					raid.addLabel(cap, pre+cid+"::"+i+"::capname");
					
					Image img = new Image("data/Other/icon.png");
					img.setPos(0, p++);
					img.setAnchor("c");
					img.setSquare(100);
					img.setTooltip("Captain Profile Pic");
					raid.addImage(img, pre+cid+"::"+i+"::img");
					
					Button twitch = new Button();
					twitch.setPos(0, p++);
					twitch.setFill('h');
					twitch.setText("watch");
					twitch.setTooltip("opens the Stream in your Browser");
					twitch.setGradient(Colors.getGradient("main buttons twitch"));
					twitch.setForeground(Colors.getColor("main buttons twitch"));
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
						wins.setTooltip("Wins in this Event with this Captain");
						wins.setForeground(Colors.getColor("main labels"));
						cloy.addLabel(wins, pre+cid+"::"+i+"::wins");
						
						Image loy = new Image("data/LoyaltyPics/noloy.png");
						loy.setPos(1, 0);
						loy.setSquare(20);
						loy.setInsets(2, 15, 2, 15);
						loy.setTooltip("Your Loyalty with this Captain");
						cloy.addImage(loy, pre+cid+"::"+i+"::loy");
						
						Button change = new Button();
						change.setPos(2, 0);
						change.setText("\u267B");
						change.setTooltip("Change Captain after Raid");
						change.setGradient(Colors.getGradient("main buttons def"));
						change.setForeground(Colors.getColor("main buttons def"));
						change.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Manager.switchSlotFriendly(cid, ii);
							}
						});
						cloy.addBut(change, pre+cid+"::"+i+"::change");
					
					raid.addContainer(cloy);
					
					Container capActs = new Container();
					capActs.setPos(0, p++);
					
						Button lock = new Button();
						lock.setPos(0, 0);
						lock.setText("\uD83D\uDD13");
						lock.setTooltip("Lock/Unlock this Slot");
						lock.setGradient(Colors.getGradient("main buttons def"));
						lock.setForeground(Colors.getColor("main buttons def"));
						lock.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Boolean isLocked = Configs.isSlotLocked(cid, "(all)", ""+ii);
								if(isLocked == null)
									isLocked = false;
								Configs.setSlotLocked(cid, "(all)", ""+ii, !isLocked);
								update();
							}
						});
						capActs.addBut(lock, pre+cid+"::"+i+"::lock");
						
						Button fav = new Button();
						fav.setPos(1, 0);
						fav.setText("\u2764");
						fav.setTooltip("Fav/Unfav this Captain");
						fav.setGradient(Colors.getGradient("main buttons def"));
						fav.setForeground(Colors.getColor("main buttons def"));
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
						block.setTooltip("Block/Unblock this Captain");
						block.setGradient(Colors.getGradient("main buttons def"));
						block.setForeground(Colors.getColor("main buttons def"));
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
						map.setTooltip("Open Map");
						map.setInsets(2, 2, 2, 10);
						map.setGradient(Colors.getGradient("main buttons def"));
						map.setForeground(Colors.getColor("main buttons def"));
						map.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								MapGUI.asGui(MainFrame.getGUI(), Manager.getViewer(cid), ii);
							}
						});
						mapchest.addBut(map);
						
						Image chest = new Image("data/ChestPics/nochest.png");
						chest.setPos(1, 0);
						chest.setAnchor("c");
						chest.setSquare(25);
						chest.setTooltip("The Chest this Raid will return");
						mapchest.addImage(chest, pre+cid+"::"+i+"::chest");
					
					raid.addContainer(mapchest);
				
				con.addContainer(raid);
			}
			
			p = 0;
			
			Container stngs = new Container();
			stngs.setPos(4, 1);
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
						new Stats(cid).open();
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
				
				String[] stngsNames = "Profile Units Chests Layers Captains Skins".split(" ");
				
				for(final String key : stngsNames) {
					final String name = (key.equals("Profile") 
											? key
											: key.substring(0, key.length()-1)
										) + "Settings";
					
					Button prof = new Button();
					prof.setPos(0, p++);
					prof.setFill('h');
					prof.setText(key);
					prof.setTooltip("opens "+name);
					prof.setGradient(Colors.getGradient("main buttons def"));
					prof.setForeground(Colors.getColor("main buttons def"));
					prof.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Class.forName("userInterface."+ProfileType.VIEWER.toString()+"."+name)
									.getDeclaredConstructor(String.class, String.class, GUI.class)
									.newInstance(cid, Manager.getCurrentLayer(cid), MainFrame.getGUI());
							} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e1) {
								Logger.printException("ProfileSection -> create: err=couldn't get Settings class for " + key, e1, Logger.runerr, Logger.error, null, null, true);
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
				String syncTo = Configs.getPStr(cid, Configs.syncedViewer);
				if(syncTo.equals("(none)"))
					syncTo = cid;
				
				for(String s : Manager.getLoadedProfiles())
					if(Configs.getPStr(s, Configs.syncedViewer).equals(cid) || s.equals(cid))
						Manager.updateProfile(s);
			}
		});
		t.start();
	}

}
