package include;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;


public class GUI{
	
	/*
	 * This class is for creating a simple GUI
	 * 
	 * static:
	 * getDefButCol()
	 * remove(String id)
	 * setEnabled(boolean b)
	 * 
	 * -components
	 * setBorder(String id, Color col, int thickness, boolean roundedCorners)
	 * setForeground(String id, Color col)
	 * setBackground(String id, Color col)
	 * setFont(String id, Font f)
	 * setText(String id, String text)
	 * setPreferredSize(String id, int x, int y)
	 * 
	 * -label
	 * getText(String id)
	 * 
	 * -buttons
	 * addALBut(String id, ActionListener al)
	 * getALBut(String id)
	 * remALBut(String id, int pos)
	 * 
	 * -cbutton/checkbox/togglebutton
	 * isCButSelected(String id)
	 * 
	 * -textarea/textfield
	 * getInputText(String id)
	 * 
	 * -combobox
	 * getSelected(String id)
	 * getCombList(String id)
	 * setCombList(String id, String[] list)
	 * 
	 * -list
	 * getSelected(String id)
	 * setSelected(String id, int index)
	 * getList(String id)
	 * setList(String id, String[] list)
	 * 
	 * -progressbar
	 * getProStr(String id)
	 * getProVal(String id)
	 * setProStr(String id, String val)
	 * setProVal(String id, int val)
	 * 
	 * -slider
	 * getSliVal(String id)
	 * 
	 * -image
	 * imageZoom(String id, float zoom)		1 = original size
	 * 
	 * 
	 * 
	 * non-static:
	 * 
	 * GUI:
	 * - GUI(String title, int sizex, int sizey)
	 * - GUI(boolean) (private)(if true then gui will not create a window. instead a container that can be obtained by getPane())
	 * addWinLis(WinLis lis)
	 * addMenu(Menu menus)
	 * addLabel(Label opt [, String id])
	 * addCButton(CButton opt)
	 * addCheckBox(CButton opt)
	 * addTogBut(CButton opt)
	 * addBut(Button opt [, String id])
	 * addTextArea(Label opt [, String id])
	 * addTextField(TextField opt [, String id])
	 * addComb(ComboBox opt)
	 * addList(List opt [, String id])
	 * addProBar(ProgressBar opt [, String id])
	 * addSlider(Slider opt [, String id])
	 * addImage(Image opt [, String id])
	 * addContainer(Container opt [, String id])
	 * 
	 * 
	 * 
	 * Layout: used by every component
	 * setInsets(int top, int left, int bottom, int right)
	 * setIpad(int x, int y)
	 * setPos(int x, int y)
	 * setScroll(boolean b)		//	to get the ScrollPane you can use "sp::"+id  
	 * setSpan(int x, int y)
	 * setFill(char fill) 	n, h, v, b
	 * setWeightX(double x)
	 * setWeightY(double y)
	 * setAnchor(String alignment)	n, ne, e, se, s, sw, w, nw, c
	 * 
	 * 
	 * Menu: Menu(String name, String[] items)
	 * setAL(int pos, ActionListener al)
	 * setSep(int pos)
	 * setSubMenu(int pos, Menu submenu)
	 * 
	 * 
	 * Label:
	 * setText(String text)
	 * 
	 * 
	 * CButton: CButton(String id)
	 * setText(String text)
	 * setCBL(CButListener opt)
	 * 
	 * 
	 * Button:
	 * setText(String text)
	 * but.setAL(ActionListener al)
	 * 
	 * 
	 * TextField:
	 * setText(String text)
	 * setAL(ActionListener al)
	 * 
	 * 
	 * ComboBox: ComboBox(String id)
	 * setList(String[] list)
	 * setCL(CombListener cl)
	 * 
	 * List:
	 * setList(String[] list)
	 * setSL(SelectListener sl)
	 * 
	 * 
	 * ProgressBar:
	 * setOrientation(char o)  // h=HORIZONTAL, v=VERTICAL
	 * setMinMax(int min, int max)
	 * 
	 * 
	 * Slider:
	 * setOrientation(char o)  // h=HORIZONTAL, v=VERTICAL
	 * setMinMax(int min, int max)
	 * setVal(int val)
	 * setMajor(int space)
	 * setMinor(int space)
	 * addLabel(int pos, String lab)
	 * setCL(ChangeListener cl)
	 * 
	 * Image: Image(String path)
	 * setWidth(int x)
	 * setHeight(int y)
	 * setSquare(int a)
	 * setBackColor(Color c)
	 * 
	 * Container:
	 * add{component name}({component needed class} opt [, String id])
	 */
	
	
	//	Program Start
	
	//	The Frame
	private JFrame frame = new JFrame();
	
	//	Defining the default Color of Buttons
	private static Color defButCol = new JButton().getBackground();
	
	public static Color getDefButCol() {
		return defButCol;
	}

	public static void setDefButCol(Color defButCol) {
		GUI.defButCol = defButCol;
	}
	
	
	//	for the window
	private String title = "default";
	private int[] size = new int[] {200, 300};
	JPanel pane;
	JMenuBar menubar = null;
	
	// Constructor, setting the options then do window
	public GUI(String title, int sizex, int sizey) {
		this.title = title;
		size[0] = sizex;
		size[1] = sizey;
		window(false);
	}
	
	private GUI(boolean container) {
		window(true);
	}
	
	private void window(boolean container) {
		if(!container) {
			//	make a JFrame
			frame.setTitle(title);
			frame.setSize(size[0], size[1]);
			frame.setLocationRelativeTo(null);
			
			//	set the default close operation
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			frame.setVisible(true);
			
			//	Layout
			pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			
			//	make the frame scrollable from the begining
			JScrollPane sp = new JScrollPane();
			sp.setViewportView(pane);
			
			java.awt.Container window = frame.getContentPane();
			window.add(sp);
		} else {
			pane = new JPanel();
			pane.setLayout(new GridBagLayout());
		}
	}
	
	public static interface WinLis {
		public void onClose(WindowEvent e);
		public void onFocusGained(WindowEvent e);
		public void onFocusLost(WindowEvent e);
		public void onIconfied(WindowEvent e);
		public void onDeIconfied(WindowEvent e);
	}
	
	public void addWinLis(WinLis lis) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
		        lis.onClose(e);
		    }
			public void windowGainedFocus(WindowEvent e) {
				lis.onFocusGained(e);
			}
			public void windowLostFocus(WindowEvent e) {
				lis.onFocusLost(e);
			}
			public void windowIconified(WindowEvent e) {
				lis.onIconfied(e);
			}
			public void windowDeiconified(WindowEvent e) {
				lis.onDeIconfied(e);
			}
		});
	}
	
	public JPanel getPane() {
		return pane;
	}
	
	public void refresh() {
		frame.setVisible(false);
		frame.setVisible(true);
	}
	
	


	//	general methods for components
	//	Hashtable with saved components
	private static Hashtable<String, Object> comps = new Hashtable<>();
	
	private void addComp(String id, Object obj) {
		comps.put(id, obj);
	}
	
	private static Object getComp(String id) {
		return comps.get(id);
	}
	
	
	//	setting different things
	public static void setForeground(String id, Color col) {
		((Component) getComp(id)).setForeground(col);;
	}
	
	public static void setBackground(String id, Color col) {
		((Component) getComp(id)).setBackground(col);
	}
	
	public static void setFont(String id, Font f) {
		((Component) getComp(id)).setFont(f);
	}
	
	public static void setText(String id, String text) {
		try {
			getComp(id).getClass().getMethod("setText", String.class).invoke(getComp(id), text);
		} catch (Exception e) {
			System.out.println("setText: "+e.getClass().getSimpleName());
		}
	}
	
	public static String getText(String id) {
		return ((JLabel) getComp(id)).getText();
	}
	
	public static void setPreferredSize(String id, int x, int y) {
		((Component) getComp(id)).setPreferredSize(new Dimension(x, y));
	}
	
	public static void setEnabled(String id, boolean b) {
		((Component) getComp(id)).setEnabled(b);
	}
	
	public static void setBorder(String id, Color col, int thickness, boolean roundedCorners) {
		((JComponent) getComp(id)).setBorder(new LineBorder(col, thickness, roundedCorners));
	}
	
	
	
	
	
	
	//	general methods
	public void close() {
		frame.dispose();
	}
	
	public void remove(String id) {
		try {
			pane.remove((Component) getComp(id));
			refresh();
		} catch (Exception e) {
			e(e);
		}
	}
	
	public static class MsgConst {
		public static final int ERROR = JOptionPane.ERROR_MESSAGE;
		public static final int INFO = JOptionPane.INFORMATION_MESSAGE;
		public static final int WARNING = JOptionPane.WARNING_MESSAGE;
		public static final int QUESTION = JOptionPane.QUESTION_MESSAGE;
		public static final int PLAIN = JOptionPane.PLAIN_MESSAGE;
	}
	
	public void msg(String title, String msg, int con) {
		JOptionPane.showMessageDialog(frame.getContentPane(), msg, title, con);
	}
	
	public boolean showConfirmationBox(String msg) {
		return JOptionPane.showConfirmDialog(frame.getContentPane(), msg, title, JOptionPane.OK_CANCEL_OPTION) == 0;
	}
	
	//	Menu
	public static class Menu {
		
		private String name;
		private String[] items;
		private ActionListener[] als;
		private boolean[] sep;
		private Menu[] submenus;
		
		public Menu(String name, String[] items) {
			this.name = name;
			this.items = items;
			
			als = new ActionListener[items.length];
			sep = new boolean[items.length];
			submenus = new Menu[items.length];
		}
		
		

		public void setAL(int pos, ActionListener al) {
			als[pos] = al;
		}
		
		public void setSep(int pos) {
			this.sep[pos] = true;
		}
		
		public void setSubMenu(int pos, Menu submenu) {
			submenus[pos] = submenu;
		}
		
		
		public String getName() {
			return name;
		}

		public String[] getItems() {
			return items;
		}

		public ActionListener[] getALs() {
			return als;
		}
		
		public boolean[] getSep() {
			return sep;
		}

		public Menu[] getSubMenus() {
			return submenus;
		}
	}
	
	public void addMenu(Menu menus) {
		//	creates a new menubar if it doesnt exist
		if(menubar == null) {
			menubar = new JMenuBar();
			frame.setJMenuBar(menubar);
		}
		//	adds the return of addSubMenu
		menubar.add(addSubMenu(menus));
	}
	
	//	its a rekursiv method to add infinite submenus
	private JMenu addSubMenu(Menu submenus) {
		//	new menu
		JMenu menu = new JMenu(submenus.getName());
		//	goes through every item in the menu
		for(int i=0; i<submenus.getItems().length; i++) {
			//	adds a seperator if placed
			if(submenus.getSep()[i] == true) {
				menu.addSeparator();
			}
			//	tests if the item is a submenu
			if(submenus.getSubMenus()[i] == null) {
				//	create item
				JMenuItem item = new JMenuItem(submenus.getItems()[i]);
				//	add listener
				if(submenus.getALs() != null) {
					item.addActionListener(submenus.getALs()[i]);
				}
				//	add item
				menu.add(item);
			} else {
				//	add submenu
				menu.add(addSubMenu(submenus.getSubMenus()[i]));
			}
		}
		//	return the menu
		return menu;
	}
	
	//	BlankForm
	public static class BlankForm {
		private Insets in = new Insets(2, 2, 2, 2);
		private int[] ipad = new int[] {0, 0};
		private int[] grid = new int[] {0, 0, 1, 1};
		private boolean scroll = false;
		private int[] alignment = new int[] {GridBagConstraints.NONE, GridBagConstraints.WEST};
		private double[] weight = new double[] {0, 0};
		
		
		public void setInsets(int top, int left, int bottom, int right) {
			in = new Insets(top, left, bottom, right);
		}
		
		public void setIpad(int x, int y) {
			this.ipad = new int[] {x, y};
		}
		
		public void setPos(int x, int y) {
			this.grid[0] = x;
			this.grid[1] = y;
		}
		
		public void setSpan(int x, int y) {
			this.grid[2] = x;
			this.grid[3] = y;
		}
		
		public void setScroll(boolean b) {
			scroll = b;
		}
		
		public void setFill(char fill) {
			switch(fill) {
			case 'n':
				this.alignment[0] = GridBagConstraints.NONE;
				break;
			case 'h':
				this.alignment[0] = GridBagConstraints.HORIZONTAL;
				break;
			case 'v':
				this.alignment[0] = GridBagConstraints.VERTICAL;
				break;
			case 'b':
				this.alignment[0] = GridBagConstraints.BOTH;
				break;
			}
		}
		
		public void setAnchor(String alignment) {
			switch(alignment) {
			case "n":
				this.alignment[1] = GridBagConstraints.NORTH;
				break;
			case "ne":
				this.alignment[1] = GridBagConstraints.NORTHEAST;
				break;
			case "e":
				this.alignment[1] = GridBagConstraints.EAST;
				break;
			case "se":
				this.alignment[1] = GridBagConstraints.SOUTHEAST;
				break;
			case "s":
				this.alignment[1] = GridBagConstraints.SOUTH;
				break;
			case "sw":
				this.alignment[1] = GridBagConstraints.SOUTHWEST;
				break;
			case "w":
				this.alignment[1] = GridBagConstraints.WEST;
				break;
			case "nw":
				this.alignment[1] = GridBagConstraints.NORTHWEST;
				break;
			case "c":
				this.alignment[1] = GridBagConstraints.CENTER;
				break;
			}
		}
		
		public void setWeightX(double x) {
			weight[0] = x;
		}
		
		public void setWeightY(double y) {
			weight[1] = y;
		}


		public Insets getIn() {
			return in;
		}

		public int[] getIpad() {
			return ipad;
		}

		public int[] getGrid() {
			return grid;
		}
		
		public boolean getScroll() {
			return scroll;
		}
		
		public int[] getAlignment() {
			return alignment;
		}
		
		public double[] getWeight() {
			return weight;
		}
	}
	
	//	very often used
	//	sets the GridBagConstraints to place the components at their right place
	private GridBagConstraints getc(Insets in, int[] ipad, int[] grid, int[] alignment, double[] weight) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = alignment[0];
		c.anchor = alignment[1];
		c.insets = in;
		c.ipadx = ipad[0];
		c.ipady = ipad[1];
		c.gridx = grid[0];
		c.gridy = grid[1];
		c.gridwidth = grid[2];
		c.gridheight = grid[3];
		c.weightx = weight[0];
		c.weighty = weight[1];
		return c;
	}
	
	//	adds the components to the pane
	private void addObj(Object opt, Object obj, String id) {
		//	tests if scroll is enabled for the component
		if(((GUI.BlankForm) opt).getScroll()) {
			//	add a new scrollpanel with setViewportView on the component
			JScrollPane sp = new JScrollPane();
			sp.setViewportView((Component) obj);
			pane.add(sp, getc(((GUI.BlankForm) opt).getIn(), ((GUI.BlankForm) opt).getIpad(), ((GUI.BlankForm) opt).getGrid(), ((BlankForm) opt).getAlignment(), ((BlankForm) opt).getWeight()));
			if(id != null) {
				addComp("sp::"+id, sp);
			}
		} else {
			//	adds the component without scroll
			pane.add((Component) obj, getc(((GUI.BlankForm) opt).getIn(), ((GUI.BlankForm) opt).getIpad(), ((GUI.BlankForm) opt).getGrid(), ((BlankForm) opt).getAlignment(), ((BlankForm) opt).getWeight()));
		}
		//	saves the component if id != null
		if(id != null) {
			addComp(id, obj);
		}
	}
	
	
	//	Label
	public static class Label extends BlankForm{
		private String text = "default";
		private boolean center = false;
		private int[] size = null;
		private boolean opaque = false;
		private Color back = null;
		private Border border = null;
		private Font font = null;
		
		public void setText(String text) {
			this.text = text;
		}
		
		public void setCenter(boolean b) {
			center = b;
		}
		
		public String getText() {
			return text;
		}
		
		public boolean isCenter() {
			return center;
		}
		
		public void setSize(int x, int y) {
			size = new int[] {x, y};
		}
		
		public int[] getSize() {
			return size;
		}

		public void setOpaque(boolean b) {
			opaque = b;
		}
		
		public boolean getOpaque() {
			return opaque;
		}
		
		public void setBackground(Color col) {
			back = col;
		}
		
		public Color getBackground() {
			return back;
		}
		
		public void setBorder(Color col, int thickness) {
			this.border = BorderFactory.createLineBorder(col, thickness);
		}
		
		public Border getBorder() {
			return border;
		}
		
		public void setFont(Font f) {
			font = f;
		}
		
		public Font getFont() {
			return font;
		}

	}
	
	public void addLabel(Label opt, String id) {
		JLabel lab;
		if(opt.isCenter()) {
			lab = new JLabel(opt.getText(), SwingConstants.CENTER);
		} else {
			lab = new JLabel(opt.getText());
		}
		
		int[] size = opt.getSize();
		if(size != null) {
			lab.setPreferredSize(new Dimension(size[0], size[1]));
		}
		
		lab.setOpaque(opt.getOpaque());
		
		Color back = opt.getBackground();
		if(back != null) {
			lab.setBackground(back);
		}
		
		Border border = opt.getBorder();
		if(border != null) {
			lab.setBorder(border);
		}
		
		addObj(opt, lab, id);
	}
	
	public void addLabel(Label opt) {
		addLabel(opt, null);
	}
	
	//	CButton
	//	saves if the cbutton/checkbox/togglebutton is selected
	private static Hashtable<String, Boolean> cbut = new Hashtable<String, Boolean>();
	
	public static interface CButListener {
		public abstract void selected(String id, ActionEvent e);
		public abstract void unselected(String id, ActionEvent e);
	}
	
	public static class CButton extends Label {
		private ActionListener Al;
		private String id;
		
		public CButton(String id) {
			cbut.put(id, false);
			this.id = id;
		}
		
		public void setCBL(CButListener opt) {
			//	new listener
			Al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//	changes the value in the hashtable
					cbut.put(id, !cbut.get(id));
					//	tests if its selected or not
					if(cbut.get(id)) {
						opt.selected(id, e);
					} else {
						opt.unselected(id, e);
					}
				}
			};
		}
		
		public ActionListener getAl() {
			return Al;
		}
		
		public String getId() {
			return id;
		}

			
	}
	

	public void addCBut(CButton opt) {
		JButton cbut = new JButton(opt.getText());
		cbut.addActionListener(opt.getAl());
		Font f = opt.getFont();
		if(f != null) {
			cbut.setFont(f);
		}
		addObj(opt, cbut, opt.getId());
	}
	
	public static boolean isCButSelected(String id) {
		return cbut.get(id);
	}
	
	
	//	CheckBox
	public void addCheckBox(CButton opt) {
		JCheckBox cb = new JCheckBox(opt.getText());
		cb.addActionListener(opt.getAl());
		addObj(opt, cb, opt.getId());
	}
	
	
	//	ToggleButton
	public void addTogBut(CButton opt) {
		JToggleButton tg = new JToggleButton(opt.getText());
		tg.addActionListener(opt.getAl());
		addObj(opt, tg, opt.getId());
	}
	
	
	//	Button
	public static class Button extends Label {
		private ActionListener al = null;
		
		public ActionListener getAl() {
			return al;
		}
		
		public void setAL(ActionListener al) {
			this.al = al;
		}
	}
	
	public void addBut(Button opt, String id) {
		JButton but = new JButton(opt.getText());
		ActionListener al = opt.getAl();
		if(al != null) {
			but.addActionListener(al);
		}
		Color back = opt.getBackground();
		if(back != null) {
			but.setBackground(back);
		}
		Font f = opt.getFont();
		if(f != null) {
			but.setFont(f);
		}
		addObj(opt, but, id);
	}
	
	public void addBut(Button opt) {
		addBut(opt, null);
	}
	
	//	add one listener to a button with the id
	public static void addALBut(String id, ActionListener al) {
		((AbstractButton) getComp(id)).addActionListener(al);
	}
	
	//	remove one listener to a button with the id
	public static void remALBut(String id, int pos) {
		((AbstractButton) getComp(id)).removeActionListener(((AbstractButton) getComp(id)).getActionListeners()[pos]);
	}
	
	//	return all listener from a button with the id
	public static ActionListener[] getALBut(String id) {
		return ((AbstractButton) getComp(id)).getActionListeners();
	}
	
	//	TextArea
	public static class TextArea extends Label {
		private boolean editable = true;
		
		public void setEditable(boolean b) {
			editable = b;
		}
		
		public boolean isEditable() {
			return editable;
		}
	}
	
	public void addTextArea(TextArea opt, String id) {
		JTextArea ta = new JTextArea(opt.getText());
		ta.setEditable(opt.isEditable());
		
		Color back = opt.getBackground();
		if(back != null) ta.setBackground(back);
		
		addObj(opt, ta, id);
	}
	
	public void addTextArea(TextArea opt) {
		addTextArea(opt, null);
	}
	
	//	get the input text from TextArea and TextField
	public static String getInputText(String id) {
		return ((JTextComponent) getComp(id)).getText();
	}
	
	public static void setEditable(String id, boolean b) {
		((JTextComponent) getComp(id)).setEditable(b);
	}
	
	//	TextField
	public static class TextField extends Label {
		//	default listener
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("no ActionListener");
			}
		};
		
		public void setAL(ActionListener al) {
			this.al = al;
		}
		
		public ActionListener getAL() {
			return al;
		}
	}
	
	public void addTextField(TextField opt, String id) {
		JTextField tf = new JTextField(opt.getText());
		int[] size = opt.getSize();
		if(size != null) {
			tf.setPreferredSize(new Dimension(size[0], size[1]));
		}
		tf.addActionListener(opt.getAL());
		addObj(opt, tf, id);
	}
	
	public void addTextField(TextField opt) {
		addTextField(opt, null);
	}
	
	
	//	BlankList
	public static class BlankList extends BlankForm {
		private String[] list = {"default one", "default two"};
		
		
		public void setList(String[] list) {
			this.list = list;
		}
		
		public String[] getList() {
			return this.list;
		}
		
	}
	
	public static String getSelected(String id) {
		//	tests if its a list or combobox and returns the selected value
		if(getComp(id) instanceof JList<?>) {
			return (String) ((JList<?>) getComp(id)).getSelectedValue();
		}
		if(getComp(id) instanceof JComboBox<?>) {
			return (String) ((JComboBox<?>) getComp(id)).getSelectedItem();
		}
		return null;
	}
	
	//	ComboBox
	public static interface CombListener {
		public void selected(String id, ItemEvent e);
		public void unselected(String id, ItemEvent e);
	}
	
	public static class ComboBox extends BlankList {
		String id;
		private ItemListener il = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				System.out.println("no ItemListener");
			}
		};
		
		public ComboBox(String id) {
			this.id = id;
		}
		
		public void setCL(CombListener cl) {
			//	same as CButton but with another listener
			il = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						cl.selected(id, e);
					} else {
						cl.unselected(id, e);
					}
				}
			};
		}
		
		
		public ItemListener getIL() {
			return this.il;
		}
		
		public String getId() {
			return id;
		}
	}
	
	public void addComb(ComboBox opt) {
		//	new string combobox with a array of strings
		JComboBox<String> comb = new JComboBox<String>(opt.getList());
		comb.addItemListener(opt.getIL());
		addObj(opt, comb, opt.getId());
	}
	
	
	public static String[] getCombItems(String id) {
		//	get ComboBox
		Object obj = getComp(id);
		String[] items = new String[0];
		//	cast Object to ComboBox
		if(obj instanceof JComboBox<?>) {
			JComboBox<?> cb = (JComboBox<?>) obj;
			//	save items to String[]
			for(int i=0; i<cb.getItemCount(); i++) {
				items = Container.add(items, cb.getItemAt(i).toString());
			}
		}
		return items;
	}
	
	public static void setCombList(String id, String[] list) {
		// get ComboBox
		Object obj = getComp(id);
		//	cast to ComboBox
		if(obj instanceof JComboBox<?>) {
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>) obj;
			//	remove all items
			cb.removeAllItems();
			//	add new items
			for(int i=0; i<list.length; i++) {
				cb.addItem(list[i]);
			}
		}
	}
	
	//	List
	public static interface SelectListener {
		public void selected(String item, ListSelectionEvent e);
	}
	
	public static class List extends BlankList {
		private String id = "";
		private ListSelectionListener lsl = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				System.out.println("no ListSelectionListener");
			}
		};
		
		public List(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
		
		
		public void setSL(SelectListener sl) {
			this.lsl = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					//	run only if the value is selected
					if(!e.getValueIsAdjusting()) {
						sl.selected(getSelected(id), e);
					}
				}
			};
		}
		
		
		public ListSelectionListener getLSL() {
			return this.lsl;
		}
	}
	
	public static Hashtable<String, DefaultListModel<String>> models = new Hashtable<String, DefaultListModel<String>>();
	
	public void addList(List opt) {
		//	new List model
		DefaultListModel<String> model = new DefaultListModel<>();
		//	new string list with the model specified
		JList<String> list = new JList<String>(model);
		//	add listener
		list.addListSelectionListener(opt.getLSL());
		//	add all the items
		String[] items = opt.getList();
		for(int i=0; i<items.length; i++) {
			model.addElement(items[i]);
		}
		//	add the list
		addObj(opt, list, opt.getId());
		//	saving model for later changes
		models.put(opt.getId(), model);
	}
	
	
	public static void setList(String id, String[] list) {
		//	get list model
		DefaultListModel<String> model = models.get(id);
		//	remove every item
		model.removeAllElements();
		//	add new items
		for(int i=0; i<list.length; i++) {
			model.addElement(list[i]);
		}
	}
	
	public static String[] getList(String id) {
		Object[] list = models.get(id).toArray();
		String[] items = new String[0];
		for(int i=0; i<list.length; i++) {
			//	adds one item to String Array
			items = Container.add(items, list[i].toString());
		}
		return items;
	}
	
	public static void setSelected(String id, int index) {
		((JList<?>) getComp(id)).setSelectedIndex(index);
	}
	
	//	ProgressBar
	public static class ProgressBar extends BlankForm {
		private int orientation = JProgressBar.HORIZONTAL;
		private int[] val = new int[] {0, 100, 50};
		
		
		public void setOrientation(char o) {
			if(o == 'h') {
				orientation = JSlider.HORIZONTAL;
			} else {
				orientation = JSlider.VERTICAL;
			}
		}
		
		public void setMinMax(int min, int max) {
			val[0] = min;
			val[1] = max;
		}
		
		public void setVal(int val) {
			this.val[2] = val;
		}
		
		
		public int getOrientation() {
			return orientation;
		}

		public int[] getVal() {
			return val;
		}
	}
	
	public void addProBar(ProgressBar opt, String id) {
		//	creates a progressbar with orientation and min/max
		JProgressBar pb = new JProgressBar(opt.getOrientation(), opt.getVal()[0], opt.getVal()[1]);
		addObj(opt, pb, id);
	}
	
	public void addProBar(ProgressBar opt) {
		addProBar(opt, null);
	}
	
	//	sets the value of a progressbar
	public static void setProVal(String id, int val) {
		((JProgressBar) getComp(id)).setValue(val);
	}
	
	//	sets the string displayed on a progressbar
	public static void setProStr(String id, String val) {
		((JProgressBar) getComp(id)).setString(val);
	}
	
	public static int getProVal(String id) {
		return ((JProgressBar) getComp(id)).getValue();
	}
	
	public static String getProStr(String id) {
		return ((JProgressBar) getComp(id)).getString();
	}
	
	
	//	Slider
	public static class Slider extends ProgressBar {
		private int[] tick = new int[] {0, 0};
		private Hashtable<Integer, JLabel> table = null;
		private ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("no ChangeListener");
			}
		};
		
		
		public void setMajor(int space) {
			tick[0] = space;
		}
		
		public void setMinor(int space) {
			tick[1] = space;
		}
		
		public void setCL(ChangeListener cl) {
			this.cl = cl;
		}
		
		
		public void addLabel(int pos, String lab) {
			if(table == null) {
				table = new Hashtable<Integer, JLabel>();
			}
			table.put(pos, new JLabel(lab));
		}
		
		
		public int[] getSpace() {
			return tick;
		}
		
		public Hashtable<Integer, JLabel> getTable() {
			return table;
		}
		
		public ChangeListener getCL() {
			return cl;
		}
		
	}
	
	public void addSlider(Slider opt, String id) {
		//	creates a slider with orientation, min, max and start value
		JSlider slider = new JSlider(opt.getOrientation(), opt.getVal()[0], opt.getVal()[1], opt.getVal()[2]);
		//	if MajorTickSpacing
		if(!(opt.getSpace()[0] == 0)) {
			slider.setMajorTickSpacing(opt.getSpace()[0]);
			slider.setPaintTicks(true);
		}
		//	if MinorTickSpacing
		if(!(opt.getSpace()[1] == 0)) {
			slider.setMinorTickSpacing(opt.getSpace()[1]);
			slider.setPaintTicks(true);
		}
		//	if PaintLabels
		if(!(opt.getTable() == null)) {
			slider.setPaintLabels(true);
			slider.setLabelTable(opt.getTable());
		}
		//	listener
		slider.addChangeListener(opt.getCL());
		addObj(opt, slider, id);
	}
	
	public void addSlider(Slider opt) {
		addSlider(opt, null);
	}
	
	//	returns the value of a slider with the id
	public static int getSliVal(String id) {
		return ((JSlider) getComp(id)).getValue();
	}
	
	
	//	Image
	public static class Image extends BlankForm {
		
		String path;
		int[] size = new int[] {-1, -1};
		boolean square = false;
		Color squareBack = null;
		
		
		public Image(String path) {
			this.path = path;
		}
		
		public void setWidth(int x) {
			size[0] = x;
		}
		
		public void setHeight(int y) {
			size[1] = y;
		}
		
		public void setSquare(int a) {
			square = true;
			size[0] = a;
		}
		
		public void setBackColor(Color c) {
			squareBack = c;
		}
		
		public String getPath() {
			return path;
		}
		
		public int[] getSize() {
			return size;
		}
		
		public boolean isSquare() {
			return square;
		}
		
		public Color getBack() {
			return squareBack;
		}
		
	}
	
	public void addImage(Image opt, String id) {
		try {
			//	new JLabel
			JLabel imgl;
			BufferedImage rawimg = ImageIO.read(new File(opt.path));
			java.awt.Image img;
			//	testing if it should be a square
			if(opt.isSquare()) {
				//	get size
				int x = rawimg.getWidth();
				int y = rawimg.getHeight();
				if(x >= y) {
					//	if the width is greater than or the same as the height then scale it that way
					img = rawimg.getScaledInstance(opt.getSize()[0], -1, java.awt.Image.SCALE_SMOOTH);
				} else {
					//	reversed
					img = rawimg.getScaledInstance(-1, opt.getSize()[0], java.awt.Image.SCALE_SMOOTH);
				}
				imgl = new JLabel(new ImageIcon(img));
				//	making the label a square
				imgl.setPreferredSize(new Dimension(opt.getSize()[0], opt.getSize()[0]));
				if(opt.getBack() != null) {
					//	adding background color
					imgl.setBackground(opt.getBack());
					imgl.setOpaque(true);
				}
			} else {
				//	adding new JLabel with imageIcon with scaled BufferedImage with file with path
				img = rawimg.getScaledInstance(opt.getSize()[0], opt.getSize()[1], java.awt.Image.SCALE_SMOOTH);
				imgl = new JLabel(new ImageIcon(img));
			}
			//	add the JLabel to the pane
			
			addObj(opt, imgl, id);
			
			if(id != null) {
				addComp("image::"+id, img);
			}
		} catch (Exception e) {
			e(e);
		}
	}
	
	public void addImage(Image opt) {
		addImage(opt, null);
	}
	

	public static void imageZoom(String id, float zoom) {
		
		java.awt.Image img = (java.awt.Image) getComp("image::"+id);
		
		int x = (int) Math.ceil(img.getWidth(null)*zoom);
		if(x <= 0) x = -1;
		
		int y = (int) Math.ceil(img.getHeight(null)*zoom);
		if(y <= 0) y = -1;
		
		((JLabel) getComp(id)).setIcon(new ImageIcon(img.getScaledInstance(x, y, java.awt.Image.SCALE_SMOOTH)));
	}
	
	
	
	//	the Container class
	public static class Container extends BlankForm {
		private Label[] labels = new Label[0];
		private String[] idlabels = new String[0];
		private CButton[] cButtons = new CButton[0];
		private CButton[] checkBoxs = new CButton[0];
		private CButton[] togButs = new CButton[0];
		private Button[] buttons = new Button[0];
		private String[] idbuttons = new String[0];
		private TextArea[] textAreas = new TextArea[0];
		private String[] idtextAreas = new String[0];
		private TextField[] textFields = new TextField[0];
		private String[] idtextFields = new String[0];
		private ComboBox[] combs = new ComboBox[0];
		private List[] lists = new List[0];
		private ProgressBar[] proBars = new ProgressBar[0];
		private String[] idproBars = new String[0];
		private Slider[] sliders = new Slider[0];
		private String[] idsliders = new String[0];
		private Image[] imgs = new Image[0];
		private String[] idimgs = new String[0];
		private Container[] containers = new Container[0];
		private String[] idcontainers = new String[0];
		
		
		private static String[] add(String[] arr, String obj) {
			String[] arr2 = new String[arr.length + 1];
			System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr2[arr.length] = obj;
			return arr2;
		}
		
		
		public void addLabel(Label opt, String id) {
			idlabels = add(idlabels, id);
			
			Label[] labels2 = new Label[labels.length + 1];
			System.arraycopy(labels, 0, labels2, 0, labels.length);
			labels2[labels.length] = opt;
			labels = labels2;
		}
		
		public void addLabel(Label opt) {
			addLabel(opt, null);
		}
		
		public Label[] getLabels() {
			return labels;
		}
		
		public String[] getIdLabels() {
			return idlabels;
		}
		
		
		
		public void addCButton(CButton opt) {
			CButton[] cButtons2 = new CButton[cButtons.length + 1];
			System.arraycopy(cButtons, 0, cButtons2, 0, cButtons.length);
			cButtons2[cButtons.length] = opt;
			cButtons = cButtons2;
		}
		
		public CButton[] getCButtons() {
			return cButtons;
		}

		

		public void addCheckBox(CButton opt) {
			CButton[] checkBoxs2 = new CButton[checkBoxs.length + 1];
			System.arraycopy(checkBoxs, 0, checkBoxs2, 0, checkBoxs.length);
			checkBoxs2[checkBoxs.length] = opt;
			checkBoxs = checkBoxs2;
		}

		public CButton[] getCheckBoxs() {
			return checkBoxs;
		}


		
		public void addTogBut(CButton opt) {
			CButton[] togButs2 = new CButton[togButs.length + 1];
			System.arraycopy(togButs, 0, togButs2, 0, togButs.length);
			togButs2[togButs.length] = opt;
			togButs = togButs2;
		}

		public CButton[] getTogButs() {
			return togButs;
		}
		
		
		
		public void addBut(Button opt, String id) {
			idbuttons = add(idbuttons, id);
			
			Button[] buttons2 = new Button[buttons.length + 1];
			System.arraycopy(buttons, 0, buttons2, 0, buttons.length);
			buttons2[buttons.length] = opt;
			buttons = buttons2;
		}
		
		public void addBut(Button opt) {
			addBut(opt, null);
		}
		
		public Button[] getButtons() {
			return buttons;
		}
		
		public String[] getIdButtons() {
			return idbuttons;
		}


		
		public void addTextArea(TextArea opt, String id) {
			idtextAreas = add(idtextAreas, id);
			
			TextArea[] textAreas2 = new TextArea[textAreas.length + 1];
			System.arraycopy(textAreas, 0, textAreas2, 0, textAreas.length);
			textAreas2[textAreas.length] = opt;
			textAreas = textAreas2;
		}
		
		public void addTextArea(TextArea opt) {
			addTextArea(opt, null);
		}

		public TextArea[] getTextAreas() {
			return textAreas;
		}

		public String[] getIdTextAreas() {
			return idtextAreas;
		}

		

		public void addTextField(TextField opt, String id) {
			idtextFields = add(idtextFields, id);
			
			TextField[] textFields2 = new TextField[textFields.length + 1];
			System.arraycopy(textFields, 0, textFields2, 0, textFields.length);
			textFields2[textFields.length] = opt;
			textFields = textFields2;
		}
		
		public void addTextFiels(TextField opt) {
			addTextField(opt, null);
		}
		
		public TextField[] getTextFields() {
			return textFields;
		}

		public String[] getIdTextFields() {
			return idtextFields;
		}
		

		
		public void addComboBox(ComboBox opt) {
			ComboBox[] combs2 = new ComboBox[combs.length + 1];
			System.arraycopy(combs, 0, combs2, 0, combs.length);
			combs2[combs.length] = opt;
			combs = combs2;
		}

		public ComboBox[] getCombs() {
			return combs;
		}


		
		public void addList(List opt, String id) {
			List[] lists2 = new List[lists.length + 1];
			System.arraycopy(lists, 0, lists2, 0, lists.length);
			lists2[lists.length] = opt;
			lists = lists2;
		}
		
		public void addList(List opt) {
			addList(opt, null);
		}

		public List[] getLists() {
			return lists;
		}


		
		public void addProBar(ProgressBar opt, String id) {
			idproBars = add(idproBars, id);
			
			ProgressBar[] proBars2 = new ProgressBar[proBars.length + 1];
			System.arraycopy(proBars, 0, proBars2, 0, proBars.length);
			proBars2[proBars.length] = opt;
			proBars = proBars2;
		}
		
		public void addProBar(ProgressBar opt) {
			addProBar(opt, null);
		}

		public ProgressBar[] getProBars() {
			return proBars;
		}
		
		public String[] getIdProBars() {
			return idproBars;
		}


		
		public void addSlider(Slider opt, String id) {
			idsliders = add(idsliders, id);
			
			Slider[] sliders2 = new Slider[sliders.length + 1];
			System.arraycopy(sliders, 0, sliders2, 0, sliders.length);
			sliders2[sliders.length] = opt;
			sliders = sliders2;
		}
		
		public void addSlider(Slider opt) {
			addSlider(opt, null);
		}

		public Slider[] getSliders() {
			return sliders;
		}
		
		public String[] getIdSliders() {
			return idsliders;
		}
		
		
		
		public void addImage(Image opt, String id) {
			idimgs = add(idimgs, id);
			
			Image[] imgs2 = new Image[imgs.length + 1];
			System.arraycopy(imgs, 0, imgs2, 0, imgs.length);
			imgs2[imgs.length] = opt;
			imgs = imgs2;
		}
		
		public void addImage(Image opt) {
			addImage(opt, null);
		}
		
		public Image[] getImages() {
			return imgs;
		}
		
		public String[] getIdImages() {
			return idimgs;
		}


		
		public void addContainer(Container opt, String id) {
			idcontainers = add(idcontainers, id);
			
			Container[] containers2 = new Container[containers.length + 1];
			System.arraycopy(containers, 0, containers2, 0, containers.length);
			containers2[containers.length] = opt;
			containers = containers2;
		}
		
		public void addContainer(Container opt) {
			addContainer(opt, null);
		}

		public Container[] getContainers() {
			return containers;
		}

		public String[] getIdContainers() {
			return idcontainers;
		}
		
	}
	
	public void addContainer(Container opt, String id) {
		//	creates new GUI class without a window
		GUI con = new GUI(true);
		
		//	going through every component added to
		//	the container and add it to con
		//	some dont have extra ids
		Label[] labs = opt.getLabels();
		String[] ids = opt.getIdLabels();
		for(int i=0; i<labs.length; i++) {
			con.addLabel(labs[i], ids[i]);
		}
		
		CButton[] cbuts = opt.getCButtons();
		for(int i=0; i<cbuts.length; i++) {
			con.addCBut(cbuts[i]);
		}
		
		cbuts = opt.getCheckBoxs();
		for(int i=0; i<cbuts.length; i++) {
			con.addCheckBox(cbuts[i]);
		}
		
		cbuts = opt.getTogButs();
		for(int i=0; i<cbuts.length; i++) {
			con.addTogBut(cbuts[i]);
		}
		
		Button[] buts = opt.getButtons();
		ids = opt.getIdButtons();
		for(int i=0; i<buts.length; i++) {
			con.addBut(buts[i], ids[i]);
		}
		
		TextArea[] tas = opt.getTextAreas();
		ids = opt.getIdTextAreas();
		for(int i=0; i<tas.length; i++) {
			con.addTextArea(tas[i], ids[i]);
		}
		
		TextField[] tfs = opt.getTextFields();
		ids = opt.getIdTextFields();
		for(int i=0; i<tfs.length; i++) {
			con.addTextField(tfs[i], ids[i]);
		}
		
		ComboBox[] cbs = opt.getCombs();
		for(int i=0; i<cbs.length; i++) {
			con.addComb(cbs[i]);
		}
		
		List[] lists = opt.getLists();
		for(int i=0; i<lists.length; i++) {
			con.addList(lists[i]);
		}
		
		ProgressBar[] pbs = opt.getProBars();
		ids = opt.getIdProBars();
		for(int i=0; i<pbs.length; i++) {
			con.addProBar(pbs[i], ids[i]);
		}
		
		Slider[] sliders = opt.getSliders();
		ids = opt.getIdSliders();
		for(int i=0; i<sliders.length; i++) {
			con.addSlider(sliders[i], ids[i]);
		}
		
		Image[] imgs = opt.getImages();
		ids = opt.getIdImages();
		for(int i=0; i<imgs.length; i++) {
			con.addImage(imgs[i], ids[i]);
		}
		
		Container[] cons = opt.getContainers();
		ids = opt.getIdContainers();
		for(int i=0; i<cons.length; i++) {
			con.addContainer(cons[i], ids[i]);
		}
		
		//	get pane from con and add it as Container
		addObj(opt, con.getPane(), id);
	}
	
	public void addContainer(Container opt) {
		addContainer(opt, null);
	}
	
	
	
	
	
	public static void e(Exception e) {
		System.out.println(e.getClass().getSimpleName() + " at Line: " + e.getStackTrace()[0].getLineNumber());
		e.printStackTrace();
	}
	
}








