package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.TextField;
import program.Browser;
import program.ConfigsV2;

public class NewProfile {
	
	private static GUI np = null;
	
	public static void open(GUI parent) {
		
		final String uid = LocalDateTime.now().toString().hashCode()+"::";
		
		np = new GUI("New Profile", 300, 400, parent, null);
		np.setBackgroundGradient(Fonts.getGradient("add background"));
		
		int y = 0;
		Label lab1 = new Label();
		lab1.setPos(0, y++);
		lab1.setText("Profilename");
		lab1.setForeground(Fonts.getColor("add labels"));
		np.addLabel(lab1);
		
		ActionListener openBrowser = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String in = GUI.getInputText(uid+"newName");
				if(checkDupeName(in))
					return;
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						Browser.show(in);
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
		np.addTextField(name, uid+"newName");
		
		Button open = new Button();
		open.setPos(0, y++);
		open.setText("open Browser to Login");
		open.setAL(openBrowser);
		open.setForeground(Fonts.getColor("add buttons"));
		open.setGradient(Fonts.getGradient("add buttons"));
		np.addBut(open);
		
		Label lor = new Label();
		lor.setPos(0, y++);
		lor.setText("or: ACCESS_INFO=");
		lor.setForeground(Fonts.getColor("add labels"));
		np.addLabel(lor);
		
		ActionListener direct = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = GUI.getInputText(uid+"newName");
				if(checkDupeName(name))
					return;
				
				String ai = GUI.getInputText(uid+"ai");
				if(ai.equals("")) {
					np.msg("ACCESS_INFO needed", "Without your ACCESS_INFO this bot can't work", GUI.MsgConst.WARNING);
					return;
				}
				np.close();
				ConfigsV2.add(name, ai);
				MainFrame.refresh(true);
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
		bd.setForeground(Fonts.getColor("add buttons"));
		bd.setGradient(Fonts.getGradient("add buttons"));
		np.addBut(bd);
	}
	
	private static boolean checkDupeName(String name) {
		List<String> taken = new ArrayList<>();
		for(String cid : ConfigsV2.getCids())
			taken.add(ConfigsV2.getPStr(cid, ConfigsV2.name));
		taken.add("Global");
		if(taken.contains(name)) {
			np.msg("Name Already Taken", name+" is already taken", GUI.MsgConst.WARNING);
			return true;
		}
		if(name.equals("")) 
			return !np.showConfirmationBox("go ahead without a name for the profile?");
		return false;
	}
	
}
