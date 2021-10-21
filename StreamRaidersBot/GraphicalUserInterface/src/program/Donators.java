package program;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;

public class Donators {

	private static class Don {
		public static final String currency = "\u20AC";
		
		private String name = null;
		private float amount = -1;
		private String link = null;
		private String text = null;
		
		
		public static Don c(String name, float amount) {
			return new Don().setName(name).setAmount(amount);
		}
		
		public Don setName(String name) {
			this.name = name;
			return this;
		}
		
		public Don setLink(String link) {
			this.link = link;
			return this;
		}
		
		public Don setAmount(float amount) {
			this.amount = amount;
			return this;
		}
		
		public Don setText(String text) {
			this.text = text;
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public String getLink() {
			return link;
		}
		
		public float getAmount() {
			return amount;
		}
		
		public String getText() {
			return text;
		}
	}
	
	private static Don[] dons = new Don[] {
		Don.c("Skyzor", (float) 32.12).setText("Real men support"),
		Don.c("Obelisk", (float) 22.01).setText("<html>thanks for taking the time<br>from your schedule to make this :)</html>"),
		Don.c("Snugsel", 20).setText("<html>She wanted to let me decide.<br>Little does she know, I'm not creative ;)<br><br>Never gonna give you up<br><br><img src=\"data\\Other\\rickroll.jpg\" width=\"400\" height=\"250\"></html>"),
		Don.c("CeKay", 15).setText("<html><center>Thx for this amazing<br>- for me -<br>timesaving project 3<br><br>link: cekay.de</center></html>"),
		Don.c("candyknack", (float) 12.58).setText("He didn't want to say anything"),
		Don.c("DeathDriver", (float) 11.10).setText("<html>Thanks for<br>creating this<br>master piece</html>"),
		Don.c("Noa3", 10).setLink("https://github.com/Noa3"),
		Don.c("CaptainYesz", (float) 7.91),
		Don.c("Kain", (float) 5.29),
		Don.c("Chris180", 5),
		Don.c("Volkoff", (float) 4.36).setText("<html>I would like to thank the developer for sharing this tool.<br>Donation is the best way we can do to encourage the same.<br>Help the project.</html>")
	};
	
	public static Container getContainer(int x, int y) {
		
		Container con = new Container();
		con.setPos(x, y);
		con.setBorder(Color.lightGray, 1, 20);
		con.setInsets(20, 20, 20, 2);
		con.setOpaque(true);
		
		Label l = new Label();
		l.setPos(0, 0);
		l.setText("Donators:");
		l.setFont(new Font(null, Font.BOLD, 20));
		con.addLabel(l);
		
		for(int i=0; i<dons.length; i++) {
			final Don don = dons[i];
			
			Button but = new Button();
			but.setPos(0, i+1);
			but.setText(don.getName() + " " + don.getAmount() + Don.currency);
			but.setFont(new Font(null, Font.PLAIN, 20));
			but.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(don.getLink() != null) {
						try {
							Desktop.getDesktop().browse(new URI(don.getLink()));
						} catch (IOException | URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
					String text = don.getText();
					if(text != null) {
						GUI gui = new GUI(don.getName(), 400, 500);
						Label l = new Label();
						if(text.startsWith("<html>")) {
							l.setText(htmlTestImg(text));
						} else {
							l.setText(text);
						}
						gui.addLabel(l);
					}
					
				}
			});
			con.addBut(but);
		}
		
		return con;
	}
	
	
	public static String htmlTestImg(String text) {
		StringBuilder sb = new StringBuilder(text);
		int index = 0;
		while(true) {
			index = sb.indexOf("<img src=\"", index) + 10;
			if(index == 9) break;
			int lin = sb.indexOf("\"", index);
			if(lin != -1) {
				try {
					URL src = new File(sb.substring(index, lin)).toURI().toURL();
					if(src == null) continue;
					sb.replace(index, lin, src.toString());
				} catch (MalformedURLException e) {}
			}
		}
		return sb.toString();
	}
	
	
}
