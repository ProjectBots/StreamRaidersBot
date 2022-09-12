package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import bot.Browser;
import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.TextField;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import run.Manager;

public class AddProfile {
	
	private static GUI np = null;
	
	public static void open(GUI parent, String cid) {
		
		final String uid = UUID.randomUUID().toString()+"::";
		
		np = new GUI(cid == null ? "New Profile" : "Update Cookies", 300, 400, parent, null);
		np.setBackgroundGradient(Colors.getGradient("stngs add background"));
		
		int y = 0;
		Label lab1 = new Label();
		lab1.setPos(0, y++);
		lab1.setText("Profilename");
		lab1.setForeground(Colors.getColor("stngs add labels"));
		np.addLabel(lab1);
		
		ActionListener openBrowser = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(Options.is("no_browser")) {
					np.msg("No Browser Warning", "inbuild Browser disabled", GUI.MsgConst.WARNING);
					return;
				}
				
				String in = GUI.getInputText(uid+"newName");
				if(cid == null && checkDupeName(in))
					return;
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						String ai = Browser.getAccessInfoCookie();
						
						if(ai != null) {
							if(cid == null)
								Manager.addProfile(in, ai);
							else {
								Configs.setPStr(cid, Configs.cookies, "ACCESS_INFO="+ai);
								Manager.retryLoadProfile(cid);
							}
						} else
							Logger.print("NewProfile -> open -> openBrowser: err=no access_info", Logger.runerr, Logger.error, null, null, true);
					}
				});
				t.start();
				np.close();
			}
		};
		
		TextField name = new TextField();
		name.setPos(0, y++);
		name.setText("");
		name.setFill('h');
		name.setAL(openBrowser);
		if(cid != null) {
			name.setText(Configs.getPStr(cid, Configs.pname));
			name.setEditable(false);
		}
		np.addTextField(name, uid+"newName");
		
		Button open = new Button();
		open.setPos(0, y++);
		open.setText("open Browser to Login");
		open.setAL(openBrowser);
		open.setForeground(Colors.getColor("stngs add buttons"));
		open.setGradient(Colors.getGradient("stngs add buttons"));
		np.addBut(open);
		
		Label lor = new Label();
		lor.setPos(0, y++);
		lor.setText("or: ACCESS_INFO=");
		lor.setForeground(Colors.getColor("stngs add labels"));
		np.addLabel(lor);
		
		ActionListener direct = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = GUI.getInputText(uid+"newName");
				if(cid == null && checkDupeName(name))
					return;
				
				String ai = GUI.getInputText(uid+"ai");
				if(ai.equals("")) {
					np.msg("ACCESS_INFO needed", "Without your ACCESS_INFO cookie this bot can't work", GUI.MsgConst.WARNING);
					return;
				}
				np.close();
				if(cid == null)
					Manager.addProfile(name, ai);
				else {
					Configs.setPStr(cid, Configs.cookies, "ACCESS_INFO="+ai);
					Manager.retryLoadProfile(cid);
				}
			}
		};
		
		TextField tfai = new TextField();
		tfai.setPos(0, y++);
		tfai.setText("");
		tfai.setFill('h');
		tfai.setAL(direct);
		np.addTextField(tfai, uid+"ai");
		
		Button bd = new Button();
		bd.setPos(0, y++);
		bd.setText("add directly");
		bd.setAL(direct);
		bd.setForeground(Colors.getColor("stngs add buttons"));
		bd.setGradient(Colors.getGradient("stngs add buttons"));
		np.addBut(bd);
	}
	
	private static boolean checkDupeName(String name) {
		if(Configs.isPNameTaken(name)) {
			np.msg("Name Already Taken", name+" is already taken", GUI.MsgConst.WARNING);
			return true;
		}
		if(name.equals("")) 
			return !np.showConfirmationBox("go ahead without a name for the profile?");
		return false;
	}
	
}
