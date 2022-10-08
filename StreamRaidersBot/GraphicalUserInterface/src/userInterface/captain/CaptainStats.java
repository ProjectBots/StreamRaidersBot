package userInterface.captain;


import include.GUI;
import run.captain.Captain;
import run.captain.CaptainBackEnd;
import userInterface.AbstractStats;

public class CaptainStats extends AbstractStats<Captain, CaptainBackEnd, Captain.CaptainBackEndRunnable> {

	public CaptainStats(String cid, CaptainBackEnd be, GUI parent) {
		super(cid, be, parent);
	}

	@Override
	protected void open(Captain p, CaptainBackEnd be) {
		// TODO Auto-generated method stub
		
	}
	

}
