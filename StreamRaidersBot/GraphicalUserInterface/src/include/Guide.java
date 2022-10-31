package include;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.UIManager;

import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Image;
import include.GUI.Label;
import include.GUI.Menu;

public class Guide {

	private static class Subject {
		private String name;
		private Container con = new Container();
		public Subject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		private int pos = 0;
		public void add(Container con) {
			con.setPos(0, pos++);
			this.con.addContainer(con);
		}
		public Container getCon() {
			return con;
		}
	}
	
	private String path;
	private List<Subject> subs = new ArrayList<>();
	private Hashtable<String, Method> onViewedListeners = new Hashtable<>();
	
	public Guide(String dir) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		this.path = dir+"/";
		String[] text = NEF.readLines(path+"guide.txt");
		Subject sub = null;
		StringBuilder sbt = null;
		StringBuilder sbw = null;
		String[] spt = null;
		for(int i=0; i<text.length; i++) {
			if(text[i].startsWith(";-"))
				continue;
			String[] sp = text[i].split(" ");
			if(text[i].startsWith("##")) {
				Container c = new Container();
				if(sbt != null) {
					sbt.append("</html>");
					Label l = new Label();
					l.setText(sbt.toString());
					boolean bold = false;
					int size = UIManager.getDefaults().getFont("Label.font").getSize();
					for(int j=1; j<spt.length; j++) {
						try {
							size = Integer.parseInt(spt[j]);
						} catch (NumberFormatException e) {
							if(spt[j].equals("b"))
								bold = true;
							else {
								l.setAnchor(spt[j]);
								c.setFill('h');
							}
						}
					}
					l.setFont(new Font(null, bold ? Font.BOLD : Font.PLAIN, size));
					c.addLabel(l);
					sub.add(c);
					sbt = null;
					c = new Container();
				} else if(sbw != null) {
					sbw.append("</html>");
					Label l = new Label();
					l.setText(sbw.toString());
					if(spt.length >= 2)
						l.setAnchor(spt[1]);
					c.addLabel(l);
					sub.add(c);
					sbw = null;
					c = new Container();
				}
				switch(text[i].charAt(2)) {
				case 's':
					if(sub != null)
						subs.add(sub);
					sub = new Subject(sp[1]);
					break;
				case 't':
					sbt = new StringBuilder("<html>");
					spt = sp;
					break;
				case 'i':
					Image img = new Image(path+sp[1]);
					for(int j=2; j<sp.length; j++) {
						if(sp[j].equals("c")) {
							c.setFill('h');
							img.setAnchor("c");
						} else {
							img.setWidth(Integer.parseInt(sp[j]));
						}
					}
					c.addImage(img);
					sub.add(c);
					break;
				case 'r':
					c.setFill('h');
					Button b = new Button();
					b.setText(sp[1]);
					boolean bold = false;
					int size = UIManager.getDefaults().getFont("Button.font").getSize();
					for(int j=2; j<sp.length; j++) {
						if(sp[j].equals("b"))
							bold = true;
						else 
							size = Integer.parseInt(sp[j]);
					}
					b.setWeightX(1);
					b.setFill('h');
					b.setFont(new Font(null, bold ? Font.BOLD : Font.PLAIN, size));
					b.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(show(gui, sp[1])) {
								gui.close();
								gui = next;
								next = null;
							}
						}
					});
					c.addBut(b);
					sub.add(c);
					break;
				case 'c':
					Class<?> sc = Class.forName(sp[1]);
					
					Method method = sc.getMethod(sp[2]);
					
					Container add = (Container) method.invoke(null);
					if(add != null)
						sub.add(add);
					break;
				case 'e':
					sc = Class.forName(sp[1]);
					onViewedListeners.put(sub.getName(), sc.getMethod(sp[2]));
					break;
				case 'd':
					c.setInsets(Integer.parseInt(sp[1]), 0, 0, 0);
					sub.add(c);
					break;
				case 'h':
					sbw = new StringBuilder("<html>");
					spt = sp;
					break;
				case 'l':
					if(!Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
						break;
					c.setFill('h');
					final String l = sp[1];
					Button link = new Button();
					boolean bo = false;
					int size1 = UIManager.getDefaults().getFont("Button.font").getSize();
					String name = l;
					for(int j=2; j<sp.length; j++) {
						if(sp[j].equals("b"))
							bo = true;
						else if(sp[j].matches("^[0-9]+$"))
							size1 = Integer.parseInt(sp[j]);
						else
							name = sp[j];
					}
					link.setWeightX(1);
					link.setFill('h');
					link.setText(name);
					link.setFont(new Font(null, bo ? Font.BOLD : Font.PLAIN, size1));
					link.setAL(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Desktop.getDesktop().browse(new URI(l));
							} catch (IOException | URISyntaxException e1) {
								e1.printStackTrace();
							}
						}
					});
					c.addBut(link);
					sub.add(c);
					break;
				}
			} else if(sbt != null)
				sbt.append(text[i]+"<br>");
			else if(sbw != null)
				sbw.append(text[i]);
			
		}
		Container c = new Container();
		if(sbt != null) {
			sbt.append("</html>");
			Label l = new Label();
			l.setText(sbt.toString());
			boolean bold = false;
			int size = UIManager.getDefaults().getFont("Label.font").getSize();
			for(int j=1; j<spt.length; j++) {
				try {
					size = Integer.parseInt(spt[j]);
				} catch (NumberFormatException e) {
					if(spt[j].equals("b"))
						bold = true;
					else {
						l.setAnchor(spt[j]);
						c.setFill('h');
					}
				}
			}
			l.setFont(new Font(null, bold ? Font.BOLD : Font.PLAIN, size));
			c.addLabel(l);
			sub.add(c);
		} else if(sbw != null) {
			sbw.append("</html>");
			Label l = new Label();
			l.setText(sbw.toString());
			if(spt.length >= 2)
				l.setAnchor(spt[1]);
			c.addLabel(l);
			sub.add(c);
		}
		
		if(sub != null)
			subs.add(sub);
	}
	
	
	private GUI gui = null;
	private GUI next = null;
	
	public boolean show(GUI parent, String subject) {
		Container con = null;
		List<String> subnames = new ArrayList<>();
		for(Subject s : subs) {
			if(s.getName().equals(subject))
				con = s.getCon();
			else
				subnames.add(s.getName());
			
		}
		if(con == null)
			return false;
		
		Menu menu = new Menu(subject, subnames.toArray(new String[subnames.size()]));
		menu.setFont(new Font(null, Font.BOLD, 22));
		for(int i=0; i<subnames.size(); i++) {
			final String n = subnames.get(i);
			menu.setAL(i, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(show(gui, n)) {
						gui.close();
						gui = next;
						next = null;
					}	
				}
			});
		}
		
		next = new GUI("Guide", 700, 750, parent, null);
		next.addMenu(menu);
		next.addContainer(con);
		next.refresh();
		
		
		if(gui == null) {
			gui = next;
			next = null;
		}
		
		Method m = onViewedListeners.get(subject);
		if(m != null)
			try {
				m.invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		return true;
	}
	
}
