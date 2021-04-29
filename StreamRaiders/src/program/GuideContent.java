package program;

import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;

import java.io.IOException;

import com.google.gson.JsonObject;

import include.Guide;
import include.JsonParser;
import include.NEF;

public class GuideContent {

	private static final String path = "data/Guide/";
	
	public static void show() {
		Guide g = new Guide();
		
		g.addSubject("Home");
		g.addSection(null, genBasicImage("home.png"));
		g.addReference("Head Section");
		g.addReference("Profile Section");
		g.addReference("Other Projects");
		
		g.addSubject("Head Section");
		g.addSection(null, genBasicImage("head.png"));
		g.addSection("Time Skip", genBasicLabel("<html>Skips the time for every Profile</html>"));
		g.addSection("Refresh", genBasicLabel("<html>Force Reloads the config File.<br>Causes the removal of every change to<br>the settings and stats since last restart.</html>"));
		g.addSection("Guide", genBasicLabel("<html>Shows this Guide</html>"));
		g.addReference("Add a Profile");
		g.addReference("General");
		
		g.addSubject("Profile Section");
		g.addSection(null, genBasicImage("profile.png"));
		g.addSection("Profile-Name", genBasicLabel("<html>Displays the name of your Profile</html>"));
		g.addSection("Counter", genBasicLabel("<html>Displays the time till the next Round starts</html>"));
		g.addSection("Lock", genBasicLabel("<html>The Bot can not switch the Streamer if locked.<br>The Button is green when active</html>"));
		g.addSection("Delete", genBasicLabel("<html>Removes the Profile from existence</html>"));
		g.addSection("Start/Stop", genBasicLabel("<html>Starts/Stops the Bot.<br>When stopping the stats will be saved for the profile</html>"));
		g.addSection("Skip Time", genBasicLabel("<html>Skips the wait time and instantly does the next round</html>"));
		g.addSection("Streamer", genBasicLabel("<html>Displays the Streamer and your loyalty<br>Streamer_Name - wins|level<br>0-14&nbsp;&nbsp;&nbsp;bronze<br>15-49 silver<br>50+&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gold</html>"));
		g.addSection("Favorite", genBasicLabel("<html>Favorites a Streamer locally (not in SR itself).<br>Favorited Streamers have a higher priority when<br>a Streamer has to be switched out.<br>Heart will turn red when active</html>"));
		g.addSection("Stats", genBasicLabel("<html>Shows how much this Bot earned in this run</html>"));
		g.addReference("Chest-Types");
		g.addReference("Profile Settings");
		g.addReference("Map");
		
		g.addSubject("General");
		g.addSection("Forget Me", genBasicLabel("<html>Removes every Profile from existence</html>"));
		g.addSection("Show Stats", genBasicLabel("<html>Shows an average of the Stats per profile per hour</html>"));
		g.addSection("Update Stats", genBasicLabel("<html>Collects the Stats from running Profiles and saves them.</html>"));
		g.addSection("Reset all Stats", genBasicLabel("<html>Deletes the Stats from every Profile.</html>"));
		
		g.addSubject("Add a Profile");
		g.addSection(null, addAProfile());

		g.addSubject("Profile Settings");
		g.addSection(null, genBasicImage("profilesettings.png"));
		g.addSection("Units", genBasicLabel("<html>Only whitelisted Units will be placed on a raid.<br>The Button is green when the Unit is whitelisted.</html>"));
		g.addSection("Specialize", genBasicLabel("<html>Opens a new window where a specialization can be choosed.</html>"));
		g.addSection("Chests", genBasicLabel("<html>Only Raids with whitelisted Chests will be choosen .<br>The Button is green when the Chest is whitelisted.</html>"));
		g.addSection("Normal Chests max Loyalty", genBasicLabel("<html>The highest Loyalty for Normal Chests.<br>A Raid will be switched when the condition is not met.</html>"));
		g.addSection("Loyalty Chests min Loyalty", genBasicLabel("<html>The lowest Loyalty for Loyalty Chests.<br>A Raid will be switched when the condition is not met.</html>"));
		g.addSection("Reset Stats", genBasicLabel("<html>Removes the Stats for this Profile from existence</html>"));
		g.addReference("Unit-Types");
		g.addReference("Chest-Types");
		
		g.addSubject("Map");
		g.addSection(null, genBasicImage("map.png"));
		g.addSection(null, genBasicLabel("<html><font size=6>Colors</font><table><tr><th color=green>Green</th><th>Allies</th></tr><tr><th color=red>Red</th><th>Enemies</th></tr><tr><th color=#E77471>Light Red</th><th>Enemie place zone</th></tr><tr><th color=#43BFC4>Blue</th><th>Player place zone</th></tr><tr><th color=#7D0552>Violet</th><th>Holding zone</th></tr><tr><th color=#101010>Dark Gray</th><th>Obstacle</th></tr><tr><th color =#555555>Light Gray</th><th>overflyable Obstacle</th></tr></table><br><font size=6>Plan</font><table><tr><th>N</th><th>No Placement</th></tr><tr><th>V</th><th>Vibe</th></tr><tr><th>R</th><th>Ranged</th></tr><tr><th>A</th><th>Armored</th></tr><tr><th>M</th><th>Melee</th></tr><tr><th>S</th><th>Support</th></tr><tr><th>D</th><th>AssassinDagger</th></tr><tr><th>E</th><th>AssassinExplosive</th></tr><tr><th>(X</th><th>Not Player Allie)</th></tr></table></html>"));
		
		g.addSubject("Chest-Types");
		g.addSection(null, chestTypes());
		
		g.addSubject("Unit-Types");
		
		
		
		
		
		g.addSubject("Other Projects");
		
		g.create(MainFrame.getGUI(), null);
	}
	

	private static Container genBasicLabel(String text) {
		Container c = new Container();
		c.setAnchor("w");
		Label l = new Label();
		l.setText(text);
		c.addLabel(l);
		return c;
	}
	
	private static Container genBasicImage(String name) {
		Container c = new Container();
		Image img = new Image(path + name);
		img.setWidth(400);
		c.addImage(img);
		return c;
	}
	
	private static Container addAProfile() {
		Container c = new Container();
		Image img1 = new Image(path + "addaprofile.png");
		img1.setWidth(200);
		c.addImage(img1);
		Label l1 = new Label();
		l1.setText("<html>Type in the name of your profile.<br>This name does not need to be your real profile name</html>");
		l1.setPos(0, 1);
		c.addLabel(l1);
		Image img2 = new Image(path + "addaprofileload.png");
		img2.setWidth(200);
		img2.setInsets(40, 2, 2, 2);
		img2.setPos(0, 2);
		c.addImage(img2);
		Label l2 = new Label();
		l2.setText("<html>You should let it load until this</html>");
		l2.setPos(0, 3);
		c.addLabel(l2);
		return c;
	}
	

	private static Container chestTypes() {
		Container c = new Container();
		
		JsonObject chests;
		try {
			chests = JsonParser.parseObj(NEF.read(path + "chestRewards.json"));
		} catch (IOException e) {
			e.printStackTrace();
			return c;
		}
		
		int i=-1;
		for(String chest : chests.keySet()) {
			Image img = new Image("data/ChestPics/" + chest + ".png");
			img.setSquare(100);
			img.setPos(0, ++i);
			c.addImage(img);
			
			Label t = new Label();
			t.setPos(1, i);
			t.setText("<html><font size=7>" + chest.replace("chest", "") + "</font></html>");
			c.addLabel(t);
			
			JsonObject ch = chests.getAsJsonObject(chest);
			
			StringBuilder bs= new StringBuilder();
			int slots = ch.size()-3;
			int ind = 0;
			for(String key : ch.keySet()) {
				if(ind++ == slots) break;
				bs.append("<tr><th>" + ind + "</th><th><table>");
				
				JsonObject slot = ch.getAsJsonObject(key);
				for(String k : slot.keySet())
					bs.append("<tr><th>" + k + "</th><th>" + slot.getAsJsonPrimitive(k).getAsString() + "%</th></tr>");
				
				bs.append("</table></th></tr><tr><th colspan=\"2\"><hr></th></tr>");
			}
			
			StringBuilder vs = new StringBuilder();
			ind = 0;
			for(String key : ch.keySet()) {
				if(ind++ < slots) continue;
				vs.append("<tr><th>" + (ind-slots) + "</th><th><table>");
				
				JsonObject slot = ch.getAsJsonObject(key);
				for(String k : slot.keySet())
					vs.append("<tr><th>" + k + "</th><th>" + slot.getAsJsonPrimitive(k).getAsString() + "%</th></tr>");
				
				vs.append("</table></th></tr><tr><th colspan=\"2\"><hr></th></tr>");
			}
			
			
			Label l = new Label();
			l.setPos(0, ++i);
			l.setSpan(2, 1);
			l.setText("<html><font size=5>Viewer Slots</font><br><table>" + vs.toString() + "</table><font size=5>Bonus Slots</font><table>" + bs.toString() + "</table></html>");
			c.addLabel(l);
		}
		
		return c;
	}

	
	public static void saveChestRewards(JsonObject sheets) {
		JsonObject chests = sheets.getAsJsonObject("Chests");
		JsonObject rews = sheets.getAsJsonObject("ChestRewards");
		
		JsonObject complete = new JsonObject();
		
		for(String key : chests.keySet()) {
			if(key.equals("anightmarechest") || key.equals("bonechest")) continue;
			complete.add(key, new JsonObject());
			
			String[] vs = chests.getAsJsonObject(key).getAsJsonPrimitive("ViewerSlots").getAsString().replace(" ", "").split(",");
			String[] bs = chests.getAsJsonObject(key).getAsJsonPrimitive("BonusSlots").getAsString().replace(" ", "").split(",");
			
			String[] all = new String[bs.length + vs.length];
			System.arraycopy(bs, 0, all, 0, bs.length);
			System.arraycopy(vs, 0, all, bs.length, vs.length);
			
			
			for(int i=0; i<all.length; i++) {
				String n = all[i];
				while(true) {
					if(complete.getAsJsonObject(key).has(n)) {
						n += "_";
					} else {
						complete.getAsJsonObject(key).add(n, new JsonObject());
						break;
					}
				}
				
				for(String sitem : rews.keySet()) {
					JsonObject item = rews.getAsJsonObject(sitem);
					int q = item.getAsJsonPrimitive(all[i]).getAsInt();
					if(q > 0) complete.getAsJsonObject(key).getAsJsonObject(n).addProperty(sitem, q);
				}
			}
			
		}
		
		try {
			NEF.save(path + "chestRewards.json", JsonParser.prettyJson(complete));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
