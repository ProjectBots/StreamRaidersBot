package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import include.GUI;
import include.GUI.Button;
import include.GUI.TextField;
import program.SRC;

public class ActionsDelayed {
	
	public static final String pre = "ActionsDelayed::";

	public static void open(GUI parent, boolean start) {

		GUI t = new GUI("Time", 500, 200, parent, null);
		
		t.setBackgroundGradient(Fonts.getGradient("actdel background"));
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							int time = (int) (Float.parseFloat(GUI.getInputText(pre+"tf")) * 1000);
							if(start) {
								MainFrame.doAll(SRC.MainFrame.start, time);
							} else {
								MainFrame.doAll(SRC.MainFrame.skip, time);
							}
						} catch (NumberFormatException e) {
							t.msg("Wrong Input", "You can't do that", GUI.MsgConst.WARNING);
						}
					}
				});
				th.start();
				t.close();
			}
		};
		
		TextField tf = new TextField();
		tf.setPos(0, 0);
		tf.setText("0");
		tf.setSize(60, 23);
		tf.setAL(al);
		t.addTextField(tf, pre+"tf");
		
		Button but = new Button();
		but.setPos(0, 1);
		but.setText("start all delayed");
		but.setGradient(Fonts.getGradient("actdel button"));
		but.setAL(al);
		t.addBut(but);
	}
	
}
