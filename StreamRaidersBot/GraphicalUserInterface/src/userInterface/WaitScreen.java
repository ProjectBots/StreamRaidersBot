package userInterface;

import javax.swing.WindowConstants;

import include.GUI;
import include.GUI.Label;

public class WaitScreen {

	private static GUI ws = null;
	
	public static final String pre = "WaitScreen::";
	
	public static void open(String startText) {
		ws = new GUI("Loading ...", 400, 200);
		ws.setAlwaysOnTop(true);
		ws.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		
		Label l = new Label();
		l.setText(startText);
		ws.addLabel(l, pre+"lab");
		
		ws.refresh();
	}
	
	public static void setText(String text) {
		if(ws != null)
			GUI.setText(pre+"lab", text);
	}
	
	public static void close() {
		if(ws == null)
			return;
		ws.close();
		ws = null;
	}
	
	
}
