package run;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import run.AbstractBackEnd.UpdateEventListener;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

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
	private int[] sleep = new int[2];
	private List<List<Boolean>> queue = Collections.synchronizedList(new LinkedList<List<Boolean>>() {
		private static final long serialVersionUID = 1L;
		{
			add(Collections.synchronizedList(new LinkedList<>()));
			add(Collections.synchronizedList(new LinkedList<>()));
		}
	});
	
	@Override
	public void setRunning(boolean b, int slot) {
		if(slot < isActiveRunning.length) {
			if(Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.syncSlotCaptain) != -1 || isSwitching)
				b = false;
			if(isRunning[slot] == b)
				return;
			List<Boolean> q = queue.get(slot);
			q.add(b);
			if(setRun[slot])
				return;
			setRun[slot] = true;
			new Thread(() -> {
				while(q.size() > 0) {
					final boolean bb = q.remove(0);
					Manager.blis.onProfileChangedRunning(cid, slot, bb);
					if(isRunning[slot] == bb)
						continue;
					isRunning[slot] = bb;
					while(isActiveRunning[slot]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					if(bb) {
						isActiveRunning[slot] = true;
						new Thread(() -> slotSequence(slot)).start();
					}
				}
				setRun[slot] = false;
			}).start();
		} else {
			//	passive slots
			isRunning[slot] = b;
			Manager.blis.onProfileChangedRunning(cid, slot, b);
		}
	}
	
	@Override
	public void setRunningAll(boolean b) {
		for(int i=0; i<isRunning.length; i++)
			setRunning(b, i);
	}

	@Override
	public boolean isRunning(int slot) {
		return isRunning[slot];
	}

	@Override
	public boolean hasStopped() {
		boolean b = false;
		for(int i=0; i<isActiveRunning.length; i++)
			b |= isActiveRunning[i];
		return !b;
	}

	@Override
	public void skip(int slot) {
		sleep[slot] = 0;
	}

	@Override
	public void skipAll() {
		for(int i=0; i<5; i++)
			skip(i);
	}
	
	private void slotSequence(int slot) {
		//TODO
	}

	@Override
	public void updateFrame(CaptainBackEnd be) throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;
		
		updateSlotSync();
		
		updateLayer();
		
	}

	@Override
	synchronized public void updateSlotSync() {
		for(int slot=0; slot<2; slot++) {
			int sync = Configs.getSleepInt(cid, currentLayer, ""+slot, Configs.syncSlotCaptain);
			Manager.blis.onProfileUpdateSlotSync(cid, slot, sync);
			if(sync == -1)
				continue;
			if(isRunning(slot)) {
				setRunning(false, slot);
				setRunning(true, sync);
			}
		}
		
	}


	
}
