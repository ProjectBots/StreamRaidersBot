package userInterface;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import include.GUI;
import include.GUI.Button;
import include.GUI.Label;
import program.Debug;

public class About {

	public static void open(GUI parent) {
		
		GUI gui = new GUI("About", 500, 600, parent, null);
		gui.setBackgroundGradient(Fonts.getGradient("about background"));
		
		int y = 0;
		
		Label lsrb = new Label();
		lsrb.setPos(0, y++);
		lsrb.setText("StreamRaidersBot");
		lsrb.setAnchor("c");
		lsrb.setFont(new Font(null, Font.PLAIN, 22));
		lsrb.setForeground(Fonts.getColor("about labels"));
		gui.addLabel(lsrb);
		
		Label lbpb = new Label();
		lbpb.setPos(0, y++);
		lbpb.setText("by ProjectBots");
		lbpb.setAnchor("c");
		lbpb.setInsets(2, 2, 20, 2);
		lbpb.setForeground(Fonts.getColor("about labels"));
		gui.addLabel(lbpb);
		
		for(String s : "Contributors: TheTonttu".split(" ")) {
			Label lcbs = new Label();
			lcbs.setPos(0, y++);
			lcbs.setText(s);
			lcbs.setAnchor("c");
			lcbs.setForeground(Fonts.getColor("about labels"));
			gui.addLabel(lcbs);
		}
		
		Label space1 = new Label();
		space1.setPos(0, y++);
		space1.setText("");
		space1.setSize(0, 20);
		gui.addLabel(space1);
		
		for(String s : "Discord Server  https://discord.gg/u7e5nTRaZQ   GitHub  https://github.com/ProjectBots/StreamRaidersBot".split("   ")) {
			String[] ss = s.split("  ");
			
			Button bd = new Button();
			bd.setPos(0, y++);
			bd.setText(ss[0]);
			bd.setAnchor("c");
			bd.setSize(200, 25);
			bd.setForeground(Fonts.getColor("about buttons"));
			bd.setGradient(Fonts.getGradient("about buttons"));
			bd.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openBrowser(ss[1]);
				}
			});
			gui.addBut(bd);
		}
		
		Label ll = new Label();
		ll.setPos(0, y++);
		ll.setText("License: GNU General Public License v3.0");
		ll.setAnchor("c");
		ll.setInsets(20, 2, 20, 2);
		ll.setForeground(Fonts.getColor("about labels"));
		gui.addLabel(ll);
		
		Label lol = new Label();
		lol.setPos(0, y++);
		lol.setText("Used Libraries");
		lol.setAnchor("c");
		lol.setInsets(20, 2, 10, 2);
		lol.setForeground(Fonts.getColor("about labels"));
		gui.addLabel(lol);
		
		for(String s : "gson by Google - Apache License 2.0  https://github.com/google/gson   commons-io by Apache - Apache License 2.0  https://github.com/apache/commons-io   commons-lang by Apache - Apache License 2.0  https://github.com/apache/commons-lang   httpcomponents-client by Apache - Apache License 2.0  https://github.com/apache/httpcomponents-client   JCEF Maven by FriwiDev - Apache License 2.0  https://github.com/jcefmaven/jcefmaven".split("   ")) {
			String[] ss = s.split("  ");
			Button bol = new Button();
			bol.setPos(0, y++);
			bol.setText(ss[0]);
			bol.setAnchor("c");
			bol.setSize(400, 25);
			bol.setForeground(Fonts.getColor("about buttons"));
			bol.setGradient(Fonts.getGradient("about buttons"));
			bol.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openBrowser(ss[1]);
				}
			});
			gui.addBut(bol);
		}
		
	}
	
	private static void openBrowser(String link) {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(link));
			} catch (IOException | URISyntaxException e) {
				Debug.printException("About -> openBrowser: err=can't open DesktopBrowser", e, Debug.runerr, Debug.error, null, null, true);
			}
		} else {
			Debug.print("About -> openBrowser: err=desktop not supported", Debug.runerr, Debug.error, null, null, true);
		}
	}
}
