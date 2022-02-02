package userInterface;

import javax.swing.WindowConstants;

import include.GUI;
import include.GUI.Label;

public class WaitScreen {

	private static GUI ws = null;
	
	public static final String pre = "WaitScreen::";
	
	private static void open() {
		ws = new GUI("Loading ...", 400, 200);
		ws.setAlwaysOnTop(true);
		ws.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ws.removeDefaultCloseListener();
		
		Label l = new Label();
		l.setText("");
		ws.addLabel(l, pre+"lab");
		
		ws.refresh();
	}
	
	public static void setText(String text) {
		if(ws == null)
			open();
		GUI.setText(pre+"lab", text, true);
	}
	
	public static void close() {
		if(ws == null)
			return;
		ws.close();
		ws = null;
	}
	
	
}
