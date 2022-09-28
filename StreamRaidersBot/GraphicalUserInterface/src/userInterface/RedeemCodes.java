package userInterface;

import java.awt.event.WindowEvent;
import java.util.UUID;

import javax.swing.WindowConstants;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.TextArea;
import include.GUI.WinLis;
import run.Manager;

public class RedeemCodes {

	private final String uid = UUID.randomUUID().toString()+"::";
	
	private static GUI gui = null;
	private static boolean isWaiting;
	
	public static void finished() {
		isWaiting = false;
	}
	
	public void open() {
		
		if(isWaiting)
			return;
		
		if(gui != null)
			gui.close();

		gui = new GUI("Redeem Codes", 400, 500, null, null);
		gui.setBackgroundGradient(Colors.getGradient("stngs global redeemCodes background"));
		
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
				gui = null;
			}
		});
		
		TextArea ta = new TextArea();
		ta.setPos(0, 0);
		ta.setText("XXXX-XXXX-XXXX\nXXXX XXXX XXXX\nXXXXXXXXXXXX");
		gui.addTextArea(ta, uid+"ta");
		
		Button bgo = new Button();
		bgo.setText("redeem codes");
		bgo.setPos(0, 1);
		bgo.setForeground(Colors.getColor("stngs global redeemCodes buttons"));
		bgo.setGradient(Colors.getGradient("stngs global redeemCodes buttons"));
		bgo.setAL((ae) -> {
			String[] text = GUI.getInputText(uid+"ta").toUpperCase().replace(" ", "-").split("\n");
			for(int i=0; i<text.length; i++) {
				if(text[i].length() != 14) {
					StringBuffer sb = new StringBuffer(text[i]);
					if(sb.charAt(4) != '-')
						sb.insert(4, '-');
					if(sb.charAt(9) != '-')
						sb.insert(9, '-');
					if(sb.length() != 14) {
						gui.msg("Error occured", "Parse error", GUI.MsgConst.ERROR);
						return;
					}
					text[i] = sb.toString();
				}
			}
			Manager.redeemCodesAll(text);
			
			GUI wait = new GUI("Please Wait", 400, 200, gui, null);
			wait.setBackgroundGradient(Colors.getGradient("stngs global redeemCodes background"));
			wait.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			Label lwait = new Label();
			lwait.setText("Please wait and do nothing\n\rwhile it is redeeming the codes");
			lwait.setForeground(Colors.getColor("stngs global redeemCodes labels"));
			wait.addLabel(lwait);
			wait.refresh();
			
			gui.close();
			new Thread(() -> {
				isWaiting = true;
				while(isWaiting) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
				}
				wait.msg("Finished", "Finished redeeming all codes", GUI.MsgConst.INFO);
				wait.close();
			}).start();
		});
		gui.addBut(bgo);
		
		gui.refresh();
	}
}
