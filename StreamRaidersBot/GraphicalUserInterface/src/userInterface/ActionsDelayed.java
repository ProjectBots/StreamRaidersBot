package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import include.GUI;
import include.GUI.Button;
import include.GUI.TextField;
import run.Manager;
import srlib.SRC;

public class ActionsDelayed {
	
	private final String uid = UUID.randomUUID().toString()+"::";
	
	public void open(GUI parent, boolean start) {
		
		GUI t = new GUI(start?"start all":"skip time all", 500, 200, parent, null);
		
		t.setBackgroundGradient(Colors.getGradient("actdel background"));
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Manager.doAll(start?SRC.Manager.start:SRC.Manager.skip,
									(int) (Float.parseFloat(GUI.getInputText(uid+"tf")) * 1000));
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
		t.addTextField(tf, uid+"tf");
		
		Button but = new Button();
		but.setPos(0, 1);
		but.setText(start?"start all delayed":"skip time all delayed");
		but.setGradient(Colors.getGradient("actdel button"));
		but.setAL(al);
		t.addBut(but);
	}
	
}
