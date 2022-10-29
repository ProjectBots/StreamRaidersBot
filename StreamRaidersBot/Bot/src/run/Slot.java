package run;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;

import include.HeapDump;
import include.Maths;
import otherlib.Configs;
import otherlib.Logger;
import srlib.SRC;


public abstract class Slot {
	
	protected final String cid;
	protected final int slot;
	protected boolean isRunning = false;
	protected boolean isActivelyRunning = false;
	private final Slot[] slots;
	private int sync = -1;
	private final AbstractProfile<?> p;
	protected String currentLayer;
	
	public Slot(AbstractProfile<?> p, Slot[] slots, int slot) {
		this.cid = p.cid;
		this.slot = slot;
		this.slots = slots;
		this.p = p;
		this.currentLayer = p.currentLayer;
	}
	
	/**
	 * some slots may require to be synced with another slot
	 * @return true if the slot is able to run on its own
	 */
	public abstract boolean canManageItself();

	public boolean isRunning() {
		return isRunning;
	}
	
	public boolean isActivelyRunning() {
		return isActivelyRunning;
	}
	
	public void sync(int slot) {
		if(this.slot == slot)
			throw new IllegalArgumentException("cannot sync with itself");
		if(slot != -1 && slots[slot].sync != -1)
			throw new IllegalStateException("cannot sync a slot to a synced slot");
		sync = slot;
	}
	
	public int getSync() {
		return sync;
	}
	
	private final LinkedList<Boolean> queue = new LinkedList<>();
	private boolean setRun = false;
	
	public void setRunning(boolean b) {
		if(sync != -1 || p.isSwitching())
			b = false;
		
		queue.add(b);
		if(setRun)
			return;
		setRun = true;
		new Thread(() -> {
			while(queue.size() > 0) {
				final boolean bb = queue.remove(0);
				Manager.blis().onProfileChangedRunning(cid, p.getType(), slot, bb);
				if(isRunning == bb)
					continue;
				isRunning = bb;
				while(isActivelyRunning) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
				if(bb && canManageItself()) {
					isActivelyRunning = true;
					new Thread(() -> runSlot()).start();
				}
			}
			setRun = false;
		}).start();
	}
	
	/**
	 * executes this slot and any slot synced to it.<br>
	 * starts the sleep timer after execution.
	 */
	private void runSlot() {
		p.updateLayer();
		p.updateSlotSync();
		currentLayer = p.currentLayer;
		
		if(!isRunning())
			return;
		
		//	paying respect to max concurrent actions
		Logger.print("requesting action", Logger.general, Logger.info, cid, slot);
		try {
			Manager.requestAction();
		} catch (InterruptedException e1) {
			Logger.print("action rejected", Logger.general, Logger.info, cid, slot);
			return;
		}
		
		exeSlotSequence();
		
		for(int i=0; i<slots.length; i++)
			if(slots[i].sync == slot)
				slots[i].exeSlotSequence();
		
		
		Logger.print("releasing action", Logger.general, Logger.info, cid, slot);
		Manager.releaseAction();
		
		LocalDateTime now_ldt = LocalDateTime.now();
		// current time in layer-units
		int now = ((now_ldt.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now_ldt.getHour() * 12) 
				+ (now_ldt.getMinute() / 5);
		
		
		// --- calculate time to sleep ---
		
		JsonObject ptimes = Configs.getUObj(cid, Configs.ptimesViewer);
		int end = Integer.parseInt(p.currentTimeId.split("-")[1]);
		
		int min = Configs.getSleepInt(cid, p.currentLayer, ""+slot, Configs.minViewer);
		int max = Configs.getSleepInt(cid, p.currentLayer, ""+slot, Configs.maxViewer);
		
		int w = -1;
		
		
		// test if sleep is not possible before next layer
		if_clause:
		if(min < 0 || max < 0 || now+(min/300) > end) {
			// change layer
			//	loop multiple times to be sure that it finds the next layer
			for(int i=0; i<ptimes.size(); i++) {
				// loop until first layer after current which is not disabled
				for(String t : ptimes.keySet()) {
					int start = Integer.parseInt(t.split("-")[0]);
					if(start != (end == 2015 ? 0 : end+1))
						continue;
					
					if(Configs.getSleepInt(cid, ptimes.get(t).getAsString(), ""+slot, Configs.minViewer) < 0 ||
							Configs.getSleepInt(cid, ptimes.get(t).getAsString(), ""+slot, Configs.maxViewer) < 0) {
						end = Integer.parseInt(t.split("-")[1]);
						continue;
					}

					// shift start if before now
					if(start < now)
						start += 2016;
					
					// calculate time until next layer which is not disabled
					w = (start-now)*300;
					break if_clause;
				}
			}
		} else {
			// test if max is still in same layer or else set max to end time of layer
			if(now+(max/300) >= end)
				max = (end-now)*300;
			// generate random sleep-time
			w = Maths.ranInt(min, max);
		}
		
		if(w > -1) {
			Logger.print("sleeping "+w+" sec", Logger.general, Logger.info, cid, slot);
			sleep(w, slot);
		} else {
			Logger.print("Viewer -> slotSequence: err=couldn't find wait time", Logger.runerr, Logger.fatal, cid, slot, true);
			Manager.blis().onProfileChangedRunning(cid, p.getType(), slot, false);
			isRunning = false;
			isActivelyRunning = false;
		}
		
		Logger.print("before MemoryReleaser", Logger.general, Logger.info, cid, slot);
		Manager.gc();
		Logger.print("after MemoryReleaser", Logger.general, Logger.info, cid, slot);
	}
	
	/**
	 * executes this slot in a thread safe way
	 */
	private void exeSlotSequence() {
		synchronized (p.getSlotLock()) {
			final String threadName = Thread.currentThread().getName();
			
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					boolean dumped = true;
					try {
						HeapDump.dumpHeap("heapdump.hprof", true);
					} catch (Exception e) {
						dumped = false;
					}
					Logger.print("Slot -> exeSlotSequence: err=slot seems to be stuck, threadName="+threadName+", heapdump_created="+dumped, Logger.runerr, Logger.error, cid, slot, true);
				}
			}, 3*60*1000);
			
			slotSequence();
			
			t.cancel();
		}
	}
	
	
	private int sleep;
	private void sleep(int sec, int slot) {
		sleep = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isRunning) {
					t.cancel();
					isActivelyRunning = false;
				}
				
				int mm = sleep / 60;
				int ss = sleep % 60;
				
				String ms = "";
				
				if(mm != 0) {
					ms += mm+":";
					if(ss < 10)
						ms += "0";
				}
				
				ms += ss;
				
				Manager.blis().onProfileTimerUpdate(cid, p.getType(), slot, ms);
				for(int i=0; i<slots.length; i++)
					if(slots[i].sync == slot)
						Manager.blis().onProfileTimerUpdate(cid, p.getType(), i, ms);
				
				sleep--;
				
				if(sleep < 0) {
					t.cancel();
					runSlot();
				}
			}
		}, 0, 1000);
	}
	
	/**
	 * sets the sleep to <code>0</code>, effectively skipping the sleep.<br>
	 * for the slot to execute it has to be running
	 */
	public void skipSleep() {
		sleep = 0;
	}
	
	/**
	 * heart of the slot<br>
	 * will be called when the slot should execute
	 */
	protected abstract void slotSequence();
	

	private boolean goMultiExploit;
	
	protected void useMultiExploit(Runnable run) {
		goMultiExploit = false;
		for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while(!goMultiExploit) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {}
					}
					run.run();
				}
			});
			t.start();
		}
		goMultiExploit = true;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
	}
}
