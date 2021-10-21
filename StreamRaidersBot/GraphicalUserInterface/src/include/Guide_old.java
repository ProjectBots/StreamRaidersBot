package include;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.Menu;
import include.GUI.WinLis;

public class Guide_old {
	
	private GUI guide = null;
	private Subject[] subs = new Subject[0];
	
	
	public void addSubject(String name) {
		Subject[] subs2 = new Subject[subs.length + 1];
		System.arraycopy(subs, 0, subs2, 0, subs.length);
		subs2[subs.length] = new Subject(name);
		subs = subs2;
	}
	
	public void addSection(String title, Container con) {
		subs[subs.length-1].addSection(title, con);
	}
	
	private Subject getSub(String name) {
		for(Subject sub : subs)
			if(sub.getName().equals(name))
				return sub;
		return null;
	}
	
	private String[] getSubNames() {
		String[] ret = new String[subs.length];
		for(int i=0; i<ret.length; i++)
			ret[i] = subs[i].getName();
		return ret;
	}
	
	
	private static class Subject {
		private String name;
		private Section[] secs = new Section[0];
		private OnLoad ol = null;
		
		public Subject(String name) {
			this.name = name;
		}
		
		public void addSection(String title, Container con) {
			Section[] secs2 = new Section[secs.length + 1];
			System.arraycopy(secs, 0, secs2, 0, secs.length);
			secs2[secs.length] = new Section(title, con);
			secs = secs2;
		}
		
		public void addReference(String name) {
			secs[secs.length-1].addRef(name);
		}
		
		public void setOnLoad(OnLoad ol) {
			this.ol = ol;
		}

		public String getName() {
			return name;
		}
		
		public void load() {
			if(ol != null) {
				ol.run();
			}
		}

		public Container getCon(Guide_old instance) {
			
			Container con = new Container();
			con.setFill('h');
			con.setAnchor("w");
			
			Label title = new Label();
			title.setText(name);
			title.setPos(0, 0);
			title.setFont(new Font(null, Font.BOLD, 30));
			title.setInsets(10, 10, 20, 10);
			title.setAnchor("c");
			con.addLabel(title);
			
			int i=1;
			for(Section sec : secs) {
				Container s = new Container();
				s.setPos(0, i++);
				s.setInsets(10, 10, 2, 2);
				
				String name = sec.getTitle();
				if(name != null) {
					Label n = new Label();
					n.setPos(0, 0);
					n.setAnchor("w");
					n.setText(name);
					n.setFont(new Font(null, Font.BOLD, 20));
					n.setPos(0, 0);
					s.addLabel(n);
				}
				
				Container c = sec.getCon();
				c.setFill('h');
				c.setPos(0, 1);
				s.addContainer(c);
				
				
				String[] refs = sec.getRefs();
				if(refs.length != 0) {
					Label l = new Label();
					l.setPos(0, i++);
					l.setText("References:");
					l.setInsets(20, 10, 5, 2);
					con.addLabel(l);
					
					for(String ref : refs) {
						Button r = new Button();
						r.setText(ref);
						r.setPos(0, i++);
						r.setFill('h');
						r.setFont(new Font(null, Font.BOLD, 15));
						r.setInsets(2, 10, 2, 2);
						r.setAL(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								instance.switchCon(instance.getSub(ref));
							}
						});
						con.addBut(r);
					}

					Label space = new Label();
					space.setText("");
					space.setPos(0, i++);
					space.setSize(1, 40);
					con.addLabel(space);
				}
				
				con.addContainer(s);
			}
			
			return con;
		}
		
		
		
	}
	
	public static interface OnLoad {
		public void run();
	}
	
	private static class Section {
		private String title;
		private Container con;
		private String[] refs = new String[0];
		
		public Section(String title, Container con) {
			this.title = title;
			this.con = con;
		}
		
		public void addRef(String name) {
			String[] refs2 = new String[refs.length + 1];
			System.arraycopy(refs, 0, refs2, 0, refs.length);
			refs2[refs.length] = name;
			refs = refs2;
		}
		
		public String getTitle() {
			return title;
		}

		public Container getCon() {
			return con;
		}
		
		public String[] getRefs() {
			return refs;
		}
		
	}
	
	
	
	public void create(GUI relativeTo, Point xy) {
		
		guide = new GUI("Guide", 500, 600, relativeTo, xy);
		
		Menu menu = new Menu("Categories", getSubNames());
		
		for(int i=0; i<subs.length; i++) {
			final int ii = i;
			menu.setAL(i, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchCon(subs[ii]);
				}
			});
		}
		
		guide.addMenu(menu);
		
		guide.addContainer(new Container(), "guide::con");
		
		switchCon(subs[0]);
	}
	
	private void switchCon(Subject sub) {
		guide.remove("guide::con");
		guide.addContainer(sub.getCon(this), "guide::con");
		sub.load();
		guide.refresh();
	}
	
	
	
	public void addReference(String name) {
		subs[subs.length-1].addReference(name);
	}
	
	public void setOnLoad(OnLoad ol) {
		subs[subs.length-1].setOnLoad(ol);
	}
	
	public void setWindowEvent(WinLis wl) {
		guide.addWinLis(wl);
	}
	
	
	
}
