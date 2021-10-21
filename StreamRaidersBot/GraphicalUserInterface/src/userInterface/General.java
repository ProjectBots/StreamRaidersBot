package userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import include.GUI;
import include.GUI.Button;
import include.GUI.TextArea;
import program.ConfigsV2;
import program.Donators;

public class General {

	
	public static void open(GUI parent) {
		GUI opt = new GUI("Options", 400, 500, parent, null);
		opt.setBackgroundGradient(Fonts.getGradient("general background"));
		
		int p = 0;
		
		Button fm = new Button();
		fm.setPos(0, p++);
		fm.setText("forget me");
		fm.setTooltip("deletes all profiles");
		fm.setInsets(20, 20, 20, 2);
		fm.setGradient(Fonts.getGradient("general buttons"));
		fm.setForeground(Fonts.getColor("general buttons"));
		fm.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.forgetMe();
			}
		});
		opt.addBut(fm);
		
		
		Button resStats = new Button();
		resStats.setPos(0, p++);
		resStats.setText("Reset all Stats");
		resStats.setInsets(2, 20, 2, 2);
		resStats.setGradient(Fonts.getGradient("general buttons"));
		resStats.setForeground(Fonts.getColor("general buttons"));
		resStats.setAL(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(opt.showConfirmationBox("Reset all the Stats?")) {
					for(String key : ConfigsV2.getCids()) {
						ConfigsV2.getProfile(key).remove(ConfigsV2.stats.get());
						ConfigsV2.check(key);
					}
				}
			}
		});
		opt.addBut(resStats);
		
		
		opt.addContainer(Donators.getContainer(0, p++));
		
		TextArea don = new TextArea();
		don.setPos(0, p++);
		don.setEditable(false);
		don.setScroll(true);
		don.setText("Donations:\n"
				+ "Paypal:\n"
				+ "https://paypal.me/projectbots\n"
				+ "\n"
				+ "Bitcoin:\n"
				+ "3FUVTkAijeuNgDyvrvvcGiAXhbxGhyCGBR\n"
				+ "\n"
				+ "Litecoin:\n"
				+ "MP8Y6X6irqarK8KpL6LkWnZpW4LZgGZgWU\n"
				+ "\n"
				+ "Ethereum:\n"
				+ "0x0c9a44a9b6388f030d91dec2ed50b6d3139418a1\n"
				+ "\n"
				+ "Monero:\n"
				+ "49Jk21tDSxsHGvzK7JrMu5UxrKhvkXu3xCXsrrNyqGJc1Kus27PUHZDDSK13fCQL8S7BcokBM3tbX7fwg1cFt6QeE3ycaYT\n"
		);
		opt.addTextArea(don, "general::don");
		
		GUI.setPreferredSize("sp::general::don", 350, 300);
		
		
	}
	
}
