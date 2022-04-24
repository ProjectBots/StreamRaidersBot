package run;

import include.Http.NoConnectionException;
import program.SRR;
import program.SRR.NotAuthorizedException;

public class Captain extends Profile<Captain.CaptainBackEndRunnable,CaptainBackEnd> {
	
	public Captain(String cid, SRR req) {
		super(cid, new CaptainBackEnd(cid, req), ProfileType.CAPTAIN);
	}

	public static interface CaptainBackEndRunnable extends Profile.BackEndRunnable<CaptainBackEnd> {}

	@Override
	public void saveStats() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRunning(boolean b, int slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRunningAll(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunning(int slot) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void skip(int slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skipAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFrame(CaptainBackEnd be) throws NoConnectionException, NotAuthorizedException {
		// TODO Auto-generated method stub
		
	}


	
}
