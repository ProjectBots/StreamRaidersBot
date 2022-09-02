package userInterface.VIEWER;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.TextField;
import include.Json;
import program.Configs;
import program.Options;
import run.ProfileType;
import run.Viewer;
import userInterface.AbstractSettings;
import userInterface.Colors;
import program.Logger;

public class ChestSettings extends AbstractSettings {

	private static final String[] mm = "min max".split(" ");
	private static final Color[] loyCols = new Color[] {new Color(54, 54, 54), new Color(192, 137, 112), new Color(192,192,192), new Color(212, 175, 55)};
	
	public ChestSettings(String cid, String lid, GUI parent) throws AlreadyOpenException {
		super(ProfileType.VIEWER, cid, lid, parent, 750, 550, false, false);
	}

	@Override
	protected String getSettingsName() {
		return "Chest";
	}

	@Override
	protected AbstractSettings getNewInstance(String lid) throws AlreadyOpenException {
		return new ChestSettings(cid, lid, gui);
	}

	@Override
	protected void addContent() {
		JsonArray cts = Json.parseArr(Options.get("chests"));
		cts.remove(new JsonPrimitive("chestsalvage"));
		
		int p = 0;
		
		for(String key : mm) {
			Label mima = new Label();
			mima.setPos(p+=2, g);
			mima.setText(key+" wins");
			mima.setSpan(2, 1);
			mima.setAnchor("c");
			mima.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(mima);
			
			Label mimat = new Label();
			mimat.setPos(p+5, g);
			mimat.setText(key+" time");
			mimat.setSpan(2, 1);
			mimat.setAnchor("c");
			mimat.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(mimat);
		}
		
		g++;
		
		for(int i=0; i<cts.size(); i++) {
			p = 0;
			String chest = cts.get(i).getAsString();
			
			Image ci = new Image("data/ChestPics/"+chest+".png");
			ci.setPos(p++, g);
			ci.setSquare(22);
			gui.addImage(ci);
			
			Label cname = new Label();
			cname.setPos(p++, g);
			cname.setText(chest.substring(5, 6).toUpperCase() + chest.substring(6));
			cname.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(cname);
			
			for(String s : mm) {
				
				Integer wins = Configs.getChestInt(cid, lid, chest, s.equals("min") ? Configs.minLoyViewer : Configs.maxLoyViewer);
				
				TextField tmm = new TextField();
				tmm.setPos(p++, g);
				tmm.setText(wins == null ? "" : ""+wins);
				tmm.setSize(50, 22);
				tmm.setDocLis(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						changed();
					}
					public void changed() {
						try {
							int wins = Integer.parseInt(GUI.getInputText(uid+chest+"::loy::"+s));
							int w;
							if(wins < 0) 
								if(s.equals("min"))
									w = 1;
								else
									w = 3;
							else if(wins < 15) 
								w = 1;
							else if(wins < 50) 
								w = 2;
							else 
								w = 3;
							
							Image img = new Image("data/LoyaltyPics/" + Viewer.pveloy[w] +".png");
							img.setSquare(18);
							try {
								GUI.setImage(uid+chest+"::loyImg::"+s, img);
							} catch (IOException e) {
								Logger.printException("ChestSettings -> changed: err=couldnt set image", e, Logger.general, Logger.error, null, null, true);
							}
							GUI.setBackground(uid+chest+"::loyBut::"+s, loyCols[w]);
							
							Configs.setChestInt(cid, lid, chest, s.equals("min") ? Configs.minLoyViewer : Configs.maxLoyViewer, wins);
							GUI.setBackground(uid+chest+"::loy::"+s, Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+chest+"::loy::"+s, new Color(255, 122, 122));
						}
					}
				});
				gui.addTextField(tmm, uid+chest+"::loy::"+s);
				
				int w;
				if(wins == null) {
					w = 0;
				} else {
					if(wins < 0) 
						if(s.equals("min"))
							w = 1;
						else
							w = 3;
					else if(wins < 15) 
						w = 1;
					else if(wins < 50) 
						w = 2;
					else 
						w = 3;
				}
				
				Container cimg = new Container();
				Image img = new Image("data/LoyaltyPics/" + Viewer.pveloy[w] +".png");
				img.setSquare(18);
				cimg.addImage(img, uid+chest+"::loyImg::"+s);
				
				Button bmm = new Button();
				bmm.setPos(p++, g);
				bmm.setBackground(loyCols[w]);
				bmm.setContainer(cimg);
				bmm.setInsets(2, 2, 2, 15);
				bmm.setAL(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Integer w = Configs.getChestInt(cid, lid, chest, s.equals("min") ? Configs.minLoyViewer : Configs.maxLoyViewer);
						int n;
						if(w == null) {
							n = -1;
						} else {
							if(s.equals("min")) 
								if(w < 15) 
									n = 15;
								else if(w < 50)
									n = 50;
								else 
									n = -1;
							else 
								if(w < 0 || w > 49)
									n = 14;
								else if(w < 15) 
									n = 49;
								else
									n = -1;
						}
						GUI.setText(uid+chest+"::loy::"+s, ""+n);
					}
				});
				gui.addBut(bmm, uid+chest+"::loyBut::"+s);
				
			}

			Button en = new Button();
			en.setPos(p++, g);
			en.setSize(90, 23);
			en.setInsets(2, 2, 2, 10);
			Boolean ben = Configs.getChestBoolean(cid, lid, chest, Configs.enabledViewer);
			if(ben == null) {
				en.setText("(---)");
				en.setGradient(Colors.getGradient(fontPath+"buttons cat"));
				en.setForeground(Colors.getColor(fontPath+"buttons cat"));
			} else if(ben) {
				en.setText("Enabled");
				en.setGradient(Colors.getGradient(fontPath+"buttons on"));
				en.setForeground(Colors.getColor(fontPath+"buttons on"));
			} else {
				en.setText("Disabled");
				en.setGradient(Colors.getGradient(fontPath+"buttons def"));
				en.setForeground(Colors.getColor(fontPath+"buttons def"));
			}
			en.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Boolean ben = Configs.getChestBoolean(cid, lid, chest, Configs.enabledViewer);
					if(ben == null || !ben) {
						Configs.setChestBoolean(cid, lid, chest, Configs.enabledViewer, true);
						GUI.setText(uid+chest+"::enable", "Enabled");
						GUI.setGradient(uid+chest+"::enable", Colors.getGradient(fontPath+"buttons on"));
						GUI.setForeground(uid+chest+"::enable", Colors.getColor(fontPath+"buttons on"));
					} else {
						Configs.setChestBoolean(cid, lid, chest, Configs.enabledViewer, false);
						GUI.setText(uid+chest+"::enable", "Disabled");
						GUI.setGradient(uid+chest+"::enable", Colors.getGradient(fontPath+"buttons def"));
						GUI.setForeground(uid+chest+"::enable", Colors.getColor(fontPath+"buttons def"));
					}
				}
			});
			gui.addBut(en, uid+chest+"::enable");
			
			for(String s : mm) {
				
				Integer time = Configs.getChestInt(cid, lid, chest, s.equals("min") ? Configs.minTimeViewer : Configs.maxTimeViewer);
				
				DocumentListener dl = new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						changed();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						changed();
					}
					public void changed() {
						try {
							int t = Integer.parseInt(GUI.getInputText(uid+chest+"::time::min::"+s)) * 60;
							t += Integer.parseInt(GUI.getInputText(uid+chest+"::time::sec::"+s));
							
							Configs.setChestInt(cid, lid, chest, s.equals("min") ? Configs.minTimeViewer : Configs.maxTimeViewer, t);
							
							GUI.setBackground(uid+chest+"::time::min::"+s, Color.white);
							GUI.setBackground(uid+chest+"::time::sec::"+s, Color.white);
						} catch (NumberFormatException e) {
							GUI.setBackground(uid+chest+"::time::min::"+s, new Color(255, 122, 122));
							GUI.setBackground(uid+chest+"::time::sec::"+s, new Color(255, 122, 122));
						}
					}
				};
				
				
				TextField tftm = new TextField();
				tftm.setPos(p++, g);
				tftm.setText(time == null ? "" : ""+((int) time/60));
				tftm.setSize(30, 22);
				tftm.setInsets(2, 7, 2, 2);
				tftm.setDocLis(dl);
				gui.addTextField(tftm, uid+chest+"::time::min::"+s);
				
				TextField tfts = new TextField();
				tfts.setPos(p++, g);
				tfts.setText(time == null ? "" : ""+(time%60));
				tfts.setSize(30, 22);
				tfts.setDocLis(dl);
				tfts.setInsets(2, 2, 2, 7);
				gui.addTextField(tfts, uid+chest+"::time::sec::"+s);
				
			}
			
			g++;
		}
		

		p = 0;
		for(String key : mm) {
			Label mima = new Label();
			mima.setPos(p+=2, g);
			mima.setText(key+" Room");
			mima.setSpan(2, 1);
			mima.setAnchor("c");
			mima.setInsets(10, 2, 2, 2);
			mima.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(mima);
			
			Label mimat = new Label();
			mimat.setPos(p+5, g);
			mimat.setText(key+" time");
			mimat.setSpan(2, 1);
			mimat.setAnchor("c");
			mimat.setForeground(Colors.getColor(fontPath+"labels"));
			gui.addLabel(mimat);
		}
		
		g++;
		p = 0;
		
		Image ci = new Image("data/ChestPics/dungeonchest.png");
		ci.setPos(p++, g);
		ci.setSquare(22);
		gui.addImage(ci);
		
		Label cname = new Label();
		cname.setPos(p++, g);
		cname.setText("Dungeon");
		cname.setForeground(Colors.getColor(fontPath+"labels"));
		gui.addLabel(cname);
		

		String chest = "dungeonchest";
		
		p = 0;
		for(String key : mm) {
			Integer rooms = Configs.getChestInt(cid, lid, chest, key.equals("min") ? Configs.minLoyViewer : Configs.maxLoyViewer); 
			
			TextField tmm = new TextField();
			tmm.setPos(p+=2, g);
			tmm.setText(rooms == null ? "" : ""+rooms);
			tmm.setSize(100, 22);
			tmm.setSpan(2, 1);
			tmm.setAnchor("c");
			tmm.setDocLis(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					changed();
				}
				public void changed() {
					try {
						int wins = Integer.parseInt(GUI.getInputText(uid+chest+"::"+key));
						Configs.setChestInt(cid, lid, chest, key.equals("min") ? Configs.minLoyViewer : Configs.maxLoyViewer, wins);
						GUI.setBackground(uid+chest+"::"+key, Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+chest+"::"+key, new Color(255, 122, 122));
					}
				}
			});
			gui.addTextField(tmm, uid+chest+"::"+key);
		}
		
		p+=3;
		
		for(String s : mm) {
			
			Integer time = Configs.getChestInt(cid, lid, chest, s.equals("min") ? Configs.minTimeViewer : Configs.maxTimeViewer);
			
			DocumentListener dl = new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					changed();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					changed();
				}
				public void changed() {
					try {
						int t = Integer.parseInt(GUI.getInputText(uid+chest+"::time::min::"+s)) * 60;
						t += Integer.parseInt(GUI.getInputText(uid+chest+"::time::sec::"+s));
						
						Configs.setChestInt(cid, lid, chest, s.equals("min") ? Configs.minTimeViewer : Configs.maxTimeViewer, t);
						
						GUI.setBackground(uid+chest+"::time::min::"+s, Color.white);
						GUI.setBackground(uid+chest+"::time::sec::"+s, Color.white);
					} catch (NumberFormatException e) {
						GUI.setBackground(uid+chest+"::time::min::"+s, new Color(255, 122, 122));
						GUI.setBackground(uid+chest+"::time::sec::"+s, new Color(255, 122, 122));
					}
				}
			};
			
			
			TextField tftm = new TextField();
			tftm.setPos(p++, g);
			tftm.setText(time == null ? "" : ""+((int) time/60));
			tftm.setSize(30, 22);
			tftm.setInsets(2, 7, 2, 2);
			tftm.setDocLis(dl);
			gui.addTextField(tftm, uid+chest+"::time::min::"+s);
			
			TextField tfts = new TextField();
			tfts.setPos(p++, g);
			tfts.setText(time == null ? "" : ""+(time%60));
			tfts.setSize(30, 22);
			tfts.setDocLis(dl);
			tfts.setInsets(2, 2, 2, 7);
			gui.addTextField(tfts, uid+chest+"::time::sec::"+s);
			
		}
	}
}
