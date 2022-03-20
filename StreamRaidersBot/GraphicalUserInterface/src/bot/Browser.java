package bot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings.LogSeverity;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefCookieVisitor;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

public class Browser {

	private static String URL = "https://www.streamraiders.com/game/";
	
	private static CefApp cefApp;
	
	private static boolean useOSR = false;
	private static boolean isTransparent = false;
	
	
	public static void create() throws UnsupportedPlatformException, InterruptedException, CefInitializationException, IOException {
		
		
		CefAppBuilder builder = new CefAppBuilder();
		
		builder.getCefSettings().log_severity = LogSeverity.LOGSEVERITY_DISABLE;
		builder.getCefSettings().windowless_rendering_enabled = useOSR;
		
		cefApp = builder.build();
	}
	
	private static String ai = null;
	private static boolean ready = false;
	
	synchronized public static String getAccessInfoCookie() {
		CefClient client = cefApp.createClient();
		CefBrowser browser = client.createBrowser(URL, useOSR, isTransparent);
		Component browserUI = browser.getUIComponent();
		JFrame frame = new JFrame();
		frame.getContentPane().add(browserUI, BorderLayout.CENTER);
		frame.setSize(800,600);
		frame.setVisible(true);
		frame.setResizable(false);
		
		ready = false;
		ai = null;
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				CefCookieManager cm = CefCookieManager.getGlobalManager();
				
				cm.visitAllCookies(new CefCookieVisitor() {
					@Override
					public boolean visit(CefCookie c, int arg1, int arg2, BoolRef arg3) {
						if(c.domain.contains("streamraiders") && c.name.equals("ACCESS_INFO")) 
							ai = c.value;
						cm.deleteCookies(c.domain, c.name);
						return true;
					}
				});
				
				//	TODO test if bug has been resolved (visitAllCookies not blocking until finished)
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {}
				
				cm.dispose();
				browser.close(true);
				client.dispose();
				frame.dispose();
				
				
				ready = true;
			}
		});
		
		while(!ready) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
		}
		
		return ai;
	}
	
	public static void dispose() {
		cefApp.dispose();
	}
}
