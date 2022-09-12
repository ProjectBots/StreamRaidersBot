package userInterface;

import java.awt.event.WindowEvent;
import java.util.UUID;

import include.GUI;
import include.GUI.WinLis;
import otherlib.Configs;
import otherlib.Logger;
import run.AbstractBackEnd;
import run.AbstractProfile;
import run.Manager;

public abstract class AbstractStats <P extends AbstractProfile<R, B>, B extends AbstractBackEnd<B>, R extends AbstractProfile.BackEndRunnable<B>> {

	protected final String uid = UUID.randomUUID().toString()+"::", cid;
	
	protected static GUI gui = null;
	public AbstractStats(String cid, B be) {
		this.cid = cid;
		
		if(gui != null)
			gui.close();
		

		gui = new GUI("Stats for " + Configs.getPStr(cid, Configs.pname), 1400, 900, MainFrame.getGUI(), null);
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
		
		
		try {
			@SuppressWarnings("unchecked")
			P p = (P) Manager.getProfile(cid);
			open(p, be);
		} catch (Exception e) {
			Logger.printException("Stats -> open: err=unable to load stats", e, Logger.runerr, Logger.error, Configs.getPStr(cid, Configs.pname), null, true);
		}
	}
	
	protected abstract void open(P p, B be);
	
}
