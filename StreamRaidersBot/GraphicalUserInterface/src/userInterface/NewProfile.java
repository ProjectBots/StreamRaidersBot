package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.TextField;
import program.Browser;
import program.ConfigsV2;

public class NewProfile {

	public static void open(GUI parent) {
		GUI np = new GUI("New Profile", 300, 400, parent, null);
		np.setBackgroundGradient(Fonts.getGradient("add background"));
		
		Label lab1 = new Label();
		lab1.setPos(0, 0);
		lab1.setText("Profilename");
		lab1.setForeground(Fonts.getColor("add labels"));
		np.addLabel(lab1);
		
		ActionListener openBrowser = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String in = GUI.getInputText("newName");
				List<String> taken = new ArrayList<>();
				for(String cid : ConfigsV2.getCids())
					taken.add(ConfigsV2.getPStr(cid, ConfigsV2.name));
				taken.add("Global");
				if(taken.contains(in)) {
					np.msg("Name Already Taken", in+" is already taken", GUI.MsgConst.WARNING);
					return;
				}
				if(in.equals("")) 
					if(!np.showConfirmationBox("go ahead without a name for the profile?")) 
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
		name.setPos(0, 1);
		name.setText("");
		name.setFill('h');
		name.setAL(openBrowser);
		np.addTextField(name, "newName");
		
		Button open = new Button();
		open.setPos(0, 2);
		open.setText("open Browser to Login");
		open.setAL(openBrowser);
		open.setForeground(Fonts.getColor("add buttons"));
		open.setGradient(Fonts.getGradient("add buttons"));
		np.addBut(open);
	}
	
}
