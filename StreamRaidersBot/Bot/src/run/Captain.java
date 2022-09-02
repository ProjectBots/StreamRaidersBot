package run;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import include.Http.NoConnectionException;
import program.Logger;
import program.SRR;
import program.SRR.NotAuthorizedException;
import run.AbstractBackEnd.UpdateEventListener;

public class Captain extends AbstractProfile<Captain.CaptainBackEndRunnable,CaptainBackEnd> {
	
	public Captain(String cid, SRR req) throws Exception {
		super(cid, new CaptainBackEnd(cid, req), ProfileType.CAPTAIN);
		uelis = new UpdateEventListener<CaptainBackEnd>() {
			@Override
			public void afterUpdate(String obj, CaptainBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				switch(obj) {
				
				}
			}
		};
		useBackEnd(cbe -> {
			cbe.setUpdateEventListener(uelis);
			cbe.ini();
		});
	}

	public static interface CaptainBackEndRunnable extends AbstractProfile.BackEndRunnable<CaptainBackEnd> {}

	@Override
	public void saveStats() {
		// TODO Auto-generated method stub
		
	}
	
	/* slots:
	 * 0 raid
	 * 1 store, event, etc.
	 * 2 twitch chat bot
	 * 3 discord chat bot
	 * 4 (?) telegram chat bot
	 * 5 ...
	 */
	private boolean[] isRunning = new boolean[4];
	private boolean[] isActiveRunning = new boolean[2];
	private boolean[] setRun = new boolean[2];
	private List<List<Boolean>> queue = Collections.synchronizedList(new LinkedList<List<Boolean>>() {
		private static final long serialVersionUID = 1L;
		{
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
		}
	});
	
	@Override
	public void setRunning(boolean bb, int slot) {
		if(slot < 2) {
			if(isRunning[slot] == bb)
				return;
			final boolean b_ = bb;
			new Thread(() -> {
				List<Boolean> q = queue.get(slot);
				q.add(b_);
				if(setRun[slot])
					return;
				setRun[slot] = true;
				while(q.size() > 0) {
					boolean b = q.remove(0);
					Manager.blis.onProfileChangedRunning(cid, slot, b);
					if(isRunning[slot] == b)
						continue;
					isRunning[slot] = b;
					while(isActiveRunning[slot]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					if(b) {
						isActiveRunning[slot] = true;
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								slotSequence(slot);
							}
						});
						t.start();
					}
				}
				setRun[slot] = false;
			}
			).start();
		} else {
			isRunning[slot] = bb;
			Manager.blis.onProfileChangedRunning(cid, slot, bb);
		}
	}
	
	
	private void slotSequence(int slot) {
		
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

	@Override
	synchronized public void updateSlotSync() {
		// TODO Auto-generated method stub
		
	}


	
}
