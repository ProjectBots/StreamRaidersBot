package userInterface.globaloptions;

import include.GUI;
import include.GUI.Button;
import otherlib.Configs;
import userInterface.Colors;

public class ChangeProfileOrder extends AbstractOptionWindow {

	private String[] cids;
	
	public ChangeProfileOrder(GUI parent) {
		super("changeProfileOrder", "Change Profile Order", 400, 500, parent);
	}
	
	@Override
	boolean canOpen() {
		return true;
	}

	@Override
	void addContent() {
		cids = Configs.getConfigIdsArr();
		int y=0;
		for(; y<cids.length; y++) {
			final int yy = y;
			
			Button bs = new Button();
			bs.setPos(0, y);
			bs.setForeground(Colors.getColor("stngs global changeProfileOrder buttons def"));
			bs.setGradient(Colors.getGradient("stngs global changeProfileOrder buttons def"));
			bs.setText(Configs.getPStr(cids[y], Configs.pname));
			bs.setAL(a -> {
				addPos(yy);
			});
			gui.addBut(bs, uid+y);
		}
		
		Button bf = new Button();
		bf.setPos(0, y++);
		bf.setText("Finish");
		bf.setForeground(Colors.getColor("stngs global changeProfileOrder buttons def"));
		bf.setGradient(Colors.getGradient("stngs global changeProfileOrder buttons def"));
		bf.setInsets(20, 2, 2, 2);
		bf.setAL(a -> {
			final int l = 37*cids.length;
			StringBuffer sb = new StringBuffer(l);
			for(int i=0; i<cids.length; i++)
				sb.append(cids[i]).append("/");
			sb.deleteCharAt(l-1);
			Configs.setGStr(Configs.newOrder, sb.toString());
			gui.msg("Succesful", "Changes will be applied next restart", GUI.MsgConst.INFO);
			gui.close();
		});
		gui.addBut(bf);
	}
	
	
	private int lp = -1;
	private void addPos(int p) {
		if(lp == p) {
			mark(p, false);
			lp = -1;
			return;
		}
		if(lp != -1) {
			swap(lp, p);
			lp = -1;
			return;
		}
		lp = p;
		mark(p, true);
	}
	
	private void swap(int p1, int p2) {
		String s = GUI.getText(uid+p1);
		GUI.setText(uid+p1, GUI.getText(uid+p2));
		GUI.setText(uid+p2, s);
		
		s = cids[p1];
		cids[p1] = cids[p2];
		cids[p2] = s;
		
		mark(p1, false);
	}
	
	
	private void mark(int p, boolean b) {
		GUI.setGradient(uid+p, Colors.getGradient("stngs global changeProfileOrder buttons "+(b?"on":"def")));
		GUI.setForeground(uid+p, Colors.getColor("stngs global changeProfileOrder buttons "+(b?"on":"def")));
	}

	

	
}
