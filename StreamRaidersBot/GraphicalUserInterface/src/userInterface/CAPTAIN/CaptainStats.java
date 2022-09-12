package userInterface.CAPTAIN;


import run.Captain;
import run.CaptainBackEnd;
import userInterface.AbstractStats;

public class CaptainStats extends AbstractStats<Captain, CaptainBackEnd, Captain.CaptainBackEndRunnable> {

	public CaptainStats(String cid, CaptainBackEnd be) {
		super(cid, be);
	}

	@Override
	protected void open(Captain p, CaptainBackEnd be) {
		// TODO Auto-generated method stub
		
	}
	

}
