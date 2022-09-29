package userInterface.globaloptions;

import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.UUID;

import include.GUI;
import include.GUI.WinLis;
import userInterface.Colors;

public abstract class AbstractOptionWindow {
	
	public static class CanNotOpenThisRightNowException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	private static Hashtable<String, GUI> guis = new Hashtable<>();

	final String uid = UUID.randomUUID().toString()+"::";
	private final String on;
	GUI gui;
	
	
	public AbstractOptionWindow(String on, String dn, int x, int y, GUI parent) {
		if(!canOpen())
			throw new CanNotOpenThisRightNowException();
		
		this.on = on;
		
		if(guis.containsKey(on))
			guis.get(on).close();
		
		open(dn, x, y, parent);
	}
	
	abstract boolean canOpen();
	
	private void open(String dn, int x, int y, GUI parent) {
		
		if(gui != null)
			gui.close();

		gui = new GUI(dn, x, y, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("stngs global "+on+" background"));
		
		gui.addWinLis(new WinLis() {
			@Override
			public void onIconfied(WindowEvent e) {}
			@Override
			public void onFocusLost(WindowEvent e) {}
			@Override
			public void onFocusGained(WindowEvent e) {}
			@Override
			public void onDeIconfied(WindowEvent e) {}
			@Override
			public void onClose(WindowEvent e) {
				guis.remove(on);
			}
		});
		
		
		addContent();
		
		
		gui.refresh();
	}
	
	abstract void addContent();
	
}
