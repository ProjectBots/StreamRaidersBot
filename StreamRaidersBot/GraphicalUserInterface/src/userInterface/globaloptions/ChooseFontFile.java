package userInterface.globaloptions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import include.GUI;
import include.GUI.Button;
import otherlib.Configs;
import userInterface.Colors;

public class ChooseFontFile extends AbstractOptionWindow {

	public ChooseFontFile(GUI parent) {
		super("chooser", "Choose Font File", 400, 500, parent);
	}
	
	@Override
	boolean canOpen() {
		return true;
	}
	
	@Override
	void addContent() {
		File dir = new File("data/Colors");
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		
		int i = 0;
		for(String file : files) {
			String name = file.substring(0, file.lastIndexOf("."));
			Button bf = new Button();
			bf.setPos(0, i++);
			bf.setText(name);
			bf.setFill('h');
			if(file.equals("default.json") || file.equals(Configs.getGStr(Configs.fontFile))) {
				bf.setGradient(Colors.getGradient("stngs global chooser buttons on"));
				bf.setForeground(Colors.getColor("stngs global chooser buttons on"));
			} else {
				bf.setGradient(Colors.getGradient("stngs global chooser buttons def"));
				bf.setForeground(Colors.getColor("stngs global chooser buttons def"));
			}
			bf.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Configs.setGStr(Configs.fontFile, file);
					GUI.setText(uid+"lff", name);
					gui.close();
				}
			});
			gui.addBut(bf);
		}
	}
	
}
