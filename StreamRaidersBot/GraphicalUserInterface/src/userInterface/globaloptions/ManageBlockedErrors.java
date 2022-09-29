package userInterface.globaloptions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import otherlib.Configs;

public class ManageBlockedErrors extends AbstractOptionWindow {

	public ManageBlockedErrors(GUI parent) {
		super("blockedErrors", "Blocked Errors", 400, 500, parent);
	}
	
	@Override
	boolean canOpen() {
		return true;
	}
	
	@Override
	void addContent() {
		List<String> blocked = new ArrayList<>(Arrays.asList(Configs.getGStr(Configs.blocked_errors).split("\\|")));
		if(blocked.get(0).equals("")) {
			Label lnts = new Label();
			lnts.setText("Nothing to show :(");
			gui.addLabel(lnts);
			return;
		}
		int y = 0;
		for(String s : blocked) {
			final String eid = uid + LocalDateTime.now().toString().hashCode() + "::" + s;
			
			Button b = new Button();
			b.setPos(0, y++);
			b.setText(s);
			b.setFill('h');
			b.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gui.remove(eid);
					blocked.remove(s);
					if(blocked.size() == 0)
						gui.close();
					Configs.setGStr(Configs.blocked_errors, String.join("|", blocked));
				}
			});
			gui.addBut(b, eid);
		}
	}
	
	
	
}
