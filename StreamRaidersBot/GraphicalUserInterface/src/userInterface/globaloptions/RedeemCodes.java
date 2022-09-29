package userInterface.globaloptions;

import javax.swing.WindowConstants;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import include.GUI.TextArea;
import run.Manager;
import userInterface.Colors;

public class RedeemCodes extends AbstractOptionWindow {

	private static boolean isWaiting;
	public static void finished() {
		isWaiting = false;
	}

	public RedeemCodes(GUI parent) {
		super("redeemCodes", "Redeem Codes", 400, 500, parent);
		if(isWaiting)
			throw new RuntimeException();
	}
	
	@Override
	boolean canOpen() {
		return !isWaiting;
	}
	
	@Override
	void addContent() {
		TextArea ta = new TextArea();
		ta.setPos(0, 0);
		ta.setText("XXXX-XXXX-XXXX\nXXXX XXXX XXXX\nXXXXXXXXXXXX");
		gui.addTextArea(ta, uid+"ta");
		
		Button bgo = new Button();
		bgo.setText("Redeem Codes");
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
			lwait.setText("Please wait and do nothing while it is redeeming the codes");
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
	}
	
	
}
