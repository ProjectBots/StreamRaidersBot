package userInterface;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import include.GUI;
import include.GUI.Button;
import include.GUI.Container;
import include.GUI.Label;
import include.GUI.TextArea;
import program.Logger;
import program.Donators.Don;

public class Donators {

	public static void open(GUI parent) {
		GUI gui = new GUI("Donators", 700, 800, parent, null);
		gui.setBackgroundGradient(Colors.getGradient("donators background"));
		
		int y0 = 0;
		Container dc = new Container();
		dc.setPos(0, y0++);
		dc.setSpan(2, 1);
		dc.setInsets(2, 40, 40, 2);
		
			int y1 = 0;
			for(Don d : program.Donators.dons) {
				int x = 1;
				
				Label ln = new Label();
				ln.setPos(x++, y1);
				ln.setText(""+(y1+1));
				ln.setInsets(6, 2, 6, 20);
				ln.setFont(new Font(null, Font.PLAIN, 22));
				ln.setForeground(Colors.getColor("donators labels"));
				dc.addLabel(ln);
				
				Label nl = new Label();
				nl.setPos(x++, y1);
				nl.setText(d.name);
				nl.setFont(new Font(null, Font.PLAIN, 22));
				nl.setForeground(Colors.getColor("donators labels"));
				dc.addLabel(nl);
				
				Label al = new Label();
				al.setPos(x++, y1);
				al.setText(d.getEur());
				al.setInsets(6, 20, 6, 20);
				al.setAnchor("e");
				al.setFont(new Font(null, Font.PLAIN, 22));
				al.setForeground(Colors.getColor("donators labels"));
				dc.addLabel(al);
				
				Label tl = new Label();
				tl.setPos(x++, y1);
				tl.setText(d.text);
				tl.setFont(new Font(null, Font.PLAIN, 22));
				tl.setForeground(Colors.getColor("donators labels"));
				dc.addLabel(tl);
				
				y1++;
			}
			
		gui.addContainer(dc);
		
		
		for(String s : "PayPal https://paypal.me/projectbots  Patreon https://www.patreon.com/projectbots".split("  ")) {
			String[] ss = s.split(" ");
			Button bpp = new Button();
			bpp.setPos(0, y0);
			bpp.setText(ss[0]);
			bpp.setSize(350, 30);
			bpp.setInsets(2, 50, 2, 2);
			bpp.setForeground(Colors.getColor("donators buttons"));
			bpp.setGradient(Colors.getGradient("donators buttons"));
			bpp.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openBrowser(ss[1]);
				}
			});
			gui.addBut(bpp);
			
			TextArea ta = new TextArea();
			ta.setPos(1, y0);
			ta.setText(ss[1]);
			ta.setEditable(false);
			gui.addTextArea(ta);
			
			y0++;
		}
		
		
		for(String c : "Bitcoin 3FUVTkAijeuNgDyvrvvcGiAXhbxGhyCGBR  Litecoin MP8Y6X6irqarK8KpL6LkWnZpW4LZgGZgWU  Ethereum 0x0c9a44a9b6388f030d91dec2ed50b6d3139418a1  Monero 49Jk21tDSxsHGvzK7JrMu5UxrKhvkXu3xCXsrrNyqGJc1Kus27PUHZDDSK13fCQL8S7BcokBM3tbX7fwg1cFt6QeE3ycaYT".split("  ")) {
			String[] cs = c.split(" ");
			Button btc = new Button();
			btc.setPos(0, y0);
			btc.setText(cs[0]);
			btc.setSize(350, 30);
			btc.setInsets(2, 50, 2, 2);
			btc.setForeground(Colors.getColor("donators buttons"));
			btc.setGradient(Colors.getGradient("donators buttons"));
			btc.setAL(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					clipboard(cs[1]);
					gui.msg("Copied", "Copied "+cs[0]+"-address\nto clipboard", GUI.MsgConst.INFO);
				}
			});
			gui.addBut(btc);
			
			TextArea ta = new TextArea();
			ta.setPos(1, y0);
			ta.setText(cs[1]);
			ta.setEditable(false);
			gui.addTextArea(ta);
			
			y0++;
		}
		
		
		
		gui.refresh();
	}
	
	private static void openBrowser(String link) {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(link));
			} catch (IOException | URISyntaxException e) {
				Logger.printException("Donators -> openBrowser: err=can't open DesktopBrowser", e, Logger.runerr, Logger.error, null, null, true);
			}
		} else {
			Logger.print("Donators -> openBrowser: err=desktop not supported", Logger.runerr, Logger.error, null, null, true);
		}
	}
	
	private static void clipboard(String text) {
		Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.setContents(new StringSelection(text), null);
	}
	
}
