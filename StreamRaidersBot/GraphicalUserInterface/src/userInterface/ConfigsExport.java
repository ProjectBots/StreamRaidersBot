package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import include.GUI;
import include.GUI.Button;
import include.GUI.CButListener;
import include.GUI.CButton;
import include.GUI.CombListener;
import include.GUI.ComboBox;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.WinLis;
import program.Configs.Exportable;
import program.Configs.Exportable.Category;
import program.Logger;

public class ConfigsExport {

	private static GUI gui = null;
	private static Exportable ex;
	
	public static void open(GUI parent) {
		if(gui != null) {
			gui.toFront();
			return;
		}

		ex = new Exportable();
		
		gui = new GUI("Export Config", 700, 900, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("stngs export background"));
		gui.addWinLis(new WinLis() {
			@Override
			public void onIconfied(WindowEvent e) {}
			@Override
			public void onFocusLost(WindowEvent e) {}
			@Override
			public void onFocusGained(WindowEvent e) {}
			@Override
			public void onDeIconfied(WindowEvent e) {}
			@Override
			public void onClose(WindowEvent e) {
				gui = null;
			}
		});
		
		gui.addContainer(addCategory(UUID.randomUUID().toString()+"::", ex.root));
		
		Button bex = new Button();
		bex.setText("export selected");
		bex.setPos(0, 1);
		bex.setForeground(Colors.getColor("stngs export buttons"));
		bex.setGradient(Colors.getGradient("stngs export buttons"));
		bex.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(export())
					gui.close();
			}
		});
		gui.addBut(bex);
		
		Button bexa = new Button();
		bexa.setText("export all");
		bexa.setPos(0, 2);
		bexa.setForeground(Colors.getColor("stngs export buttons"));
		bexa.setGradient(Colors.getGradient("stngs export buttons"));
		bexa.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ex.root.setExportCategory(true);
				export();
				gui.close();
				gui = null;
			}
		});
		gui.addBut(bexa);
		
		gui.refresh();
	}
	
	private static Container addCategory(final String id, final Category cat) {
		
		int y = 0;
		
		Container con = new Container();
		
		Label space1 = new Label();
		space1.setPos(0, 0);
		space1.setInsets(0, 15, 0, 0);
		space1.setText("");
		con.addLabel(space1);
		
		ArrayList<String> items = cat.getItemNames();
		for(final String item : items) {
			CButton cb = new CButton(id+item);
			cb.setText(item);
			cb.setPos(1, y++);
			cb.setSpan(2, 1);
			cb.setStartValue(cat.getItemExportMode(item));
			cb.setForeground(Colors.getColor("stngs export labels"));
			cb.setCBL(new CButListener() {
				@Override
				public void unselected(String id, ActionEvent e) {
					cat.setExportItem(item, false);
				}
				@Override
				public void selected(String id, ActionEvent e) {
					cat.setExportItem(item, true);
				}
			});
			con.addCheckBox(cb);
		}
		
		Hashtable<String, Category> subs = cat.getSubcategories();
		for(final String key : subs.keySet()) {
			final String nid = id+key;
			
			final Category next = subs.get(key);
			Label lname = new Label();
			lname.setPos(1, y);
			lname.setText(key);
			lname.setForeground(Colors.getColor("stngs export labels"));
			con.addLabel(lname);
			
			Boolean exm = next.getExportMode();
			String l;
			if(exm == null)
				l = "none all custom";
			else if(exm)
				l = "all none custom";
			else
				l = "custom none all";
			
			
			ComboBox cb = new ComboBox("cb::"+id+key);
			cb.setPos(2, y++);
			cb.setList(l.split(" "));
			cb.setCL(new CombListener() {
				@Override
				public void unselected(String id, ItemEvent e) {}
				@Override
				public void selected(String id, ItemEvent e) {
					String sel = GUI.getSelected(id);
					if(sel.equals("custom")) {
						next.setExportCategory(false);
						gui.addToContainer("next::"+nid, addCategory(nid+"::", next), "con::"+nid+"::");
					} else {
						try {
							GUI.removeFromContainer("next::"+nid, "con::"+nid+"::");
						} catch (NullPointerException e2) {}
						//	if not all then none
						next.setExportCategory(sel.equals("all") ? true : null);
					}
					gui.refresh();
				}
			});
			con.addComboBox(cb);
			
			Container cnext = new Container();
			cnext.setPos(1, y++);
			cnext.setSpan(2, 1);
			con.addContainer(cnext, "next::"+nid);
		}
		
		return con;
	}
	
	private static boolean export() {
		File file = gui.showFileChooser("Export Config", true, new FileNameExtensionFilter("Json Files", "json"));
		if(file == null)
			return false;
		
		String path = file.getAbsolutePath();
		if(!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("json"))
			path += ".json";
		
		try {
			ex.export(path);
		} catch (IOException e) {
			Logger.printException("ConfigsExport -> export: err=failed to export", e, Logger.runerr, Logger.error, null, null, true);
			return false;
		}
		return true;
	}

}
