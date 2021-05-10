package include;

import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.JsonObject;

import include.GUI.Label;
import program.MainFrame;
import program.StreamRaiders;

public class Version {
	
	public static int dif(String ver1, String ver2) {
		String[] ver1s = ver1.split("\\.");
		String[] ver2s = ver2.split("\\.");
		for(int i=0; i<ver1s.length; i++) {
			if(Integer.parseInt(ver1s[i]) > Integer.parseInt(ver2s[i]))
				return 1;
			if(Integer.parseInt(ver1s[i]) < Integer.parseInt(ver2s[i]))
				return -1;
		}
		return 0;
	}
	
	public static void check() {
		Http get = new Http();
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
		get.setUrl("https://raw.githubusercontent.com/ProjectBots/StreamRaidersBot/master/StreamRaiders/changelog.json");
		
		try {
			JsonObject cl = JsonParser.parseObj(get.sendGet());
			
			String ver = cl.getAsJsonPrimitive("newest").getAsString();
			
			String bver = StreamRaiders.get("botVersion").replace("beta", "").replace("debug", "");
			
			if(Version.dif(ver, bver) == 1) {
				GUI verg = new GUI("New Version is out", 500, 700, MainFrame.getGUI(), null);
				
				int y = 0;
				
				Label title = new Label();
				title.setPos(0, y++);
				title.setText("A New Version is out!");
				title.setInsets(2, 20, 2, 2);
				title.setFont(new Font(null, Font.BOLD, 30));
				verg.addLabel(title);
				
				Label title2 = new Label();
				title2.setPos(0, y++);
				title2.setText("Download for the newest features");
				title2.setInsets(2, 2, 20, 2);
				title2.setFont(new Font(null, Font.PLAIN, 25));
				verg.addLabel(title2);
				
				for(String key : cl.keySet()) {
					if(key.equals("newest"))
						continue;
					if(Version.dif(key, bver) >= 0) {
						Label l = new Label();
						l.setPos(0, y++);
						l.setText(cl.getAsJsonPrimitive(key).getAsString());
						l.setInsets(2, 2, 40, 2);
						verg.addLabel(l);
					}
				}
				
				verg.refresh();
			}
		} catch (URISyntaxException | IOException e2) {
			StreamRaiders.log("Version -> check: err=Couldnt get changelog", e2);
		}
		
		
	}
	
}
