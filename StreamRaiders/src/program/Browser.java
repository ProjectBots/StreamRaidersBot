package program;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.JCefLoader;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefCookieVisitor;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;

import com.google.gson.JsonObject;

import include.GUI;
import include.GUI.Label;

public class Browser {

	private static JFrame frame = new JFrame();
	private static String URL = "https://www.streamraiders.com/game/";
	
	private static CefApp cefApp;
	private static CefClient client;
	private static CefBrowser browser;
	private static Component browserUI;
	
	private static boolean useOSR = OS.isLinux();
	private static boolean isTransparent = false;

	
	public static void create() throws IOException, RuntimeException {
		
		
		CefApp.addAppHandler(new CefAppHandlerAdapter(null) {});
		CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = useOSR;
		settings.log_severity = LogSeverity.LOGSEVERITY_DISABLE;
			
		
		GUI load = new GUI("Embeded Browser", 300, 300);
			
		Label l1 = new Label();
		l1.setText("loading ...\nthis can take a bit");
			
		load.addLabel(l1);
			
		cefApp = JCefLoader.installAndLoadCef(settings);
		
		load.close();
	}
	
	public static void show(String name) {
		client = cefApp.createClient();
		browser = client.createBrowser(URL, useOSR, isTransparent);
		browserUI = browser.getUIComponent();
		
		frame = new JFrame();
		frame.getContentPane().add(browserUI, BorderLayout.CENTER);
		frame.pack();
		frame.setSize(800,600);
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				JsonObject cookies = new JsonObject();
				
				CefCookieManager cm = CefCookieManager.getGlobalManager();
				
				cm.visitAllCookies(new CefCookieVisitor() {
					@Override
					public boolean visit(CefCookie c, int arg1, int arg2, BoolRef arg3) {
						if(c.domain.contains("streamraiders")) 
							cookies.addProperty(c.name, c.value);
						cm.deleteCookies(c.domain, c.name);
						return true;
					}
				});
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {}
				
				cm.dispose();
				browser.close(true);
				client.dispose();
				frame.dispose();
				
				Configs.add(name, cookies);
				
				MainFrame.refresh();
			}
		});
	}
	
	public static void dispose() {
		CefApp.getInstance().dispose();
	}
}
