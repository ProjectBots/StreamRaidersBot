package run;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;

import include.Maths;
import otherlib.Configs;
import otherlib.Logger;


public abstract class Slot {
	
	protected final String cid;
	protected final int slot;
	protected boolean isRunning = false;
	protected boolean isActivelyRunning = false;
	private final Slot[] slots;
	private final boolean[] synced;
	private boolean isSynced = false;
	private final AbstractProfile<?, ?> p;
	protected String currentLayer;
	
	
	public Slot(AbstractProfile<?, ?> p, Slot[] slots, int slot) {
		this.cid = p.cid;
		this.slot = slot;
		this.slots = slots;
		this.synced = new boolean[slots.length];
		this.p = p;
		this.currentLayer = p.currentLayer;
	}
	
	public abstract boolean canManageItself();

	protected abstract JsonObject dump();
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public boolean isActivelyRunning() {
		return isActivelyRunning;
	}
	
	public void syncSlot(int slot, boolean b) {
		if(b && this.slot == slot)
			throw new IllegalArgumentException("cannot sync with itself");
		if(b && isSynced)
			throw new IllegalStateException("cannot sync a slot to a synced slot");
		synced[slot] = b;
	}
	
	public void setSynced(boolean b) {
		isSynced = b;
	}
	
	private final LinkedList<Boolean> queue = new LinkedList<>();
	private boolean setRun = false;
	
	public void setRunning(boolean b) {
		p.updateSlotSync();
		if(isSynced || p.isSwitching())
			b = false;
		
		queue.add(b);
		if(setRun)
			return;
		setRun = true;
		new Thread(() -> {
			while(queue.size() > 0) {
				final boolean bb = queue.remove(0);
				Manager.blis().onProfileChangedRunning(cid, slot, bb);
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
	
	protected void runSlot() {
		p.updateLayer();
		p.updateSlotSync();
		currentLayer = p.currentLayer;
		
		if(!isRunning())
			return;
		
		Logger.print("requesting action", Logger.general, Logger.info, cid, slot);
		try {
			Manager.requestAction();
		} catch (InterruptedException e1) {
			Logger.print("action rejected", Logger.general, Logger.info, cid, slot);
			return;
		}
		
		slotSequence();
		
		for(int i=0; i<synced.length; i++)
			if(synced[i])
				slots[i].slotSequence();
		
		Logger.print("releasing action", Logger.general, Logger.info, cid, slot);
		Manager.releaseAction();
		
		LocalDateTime now_ldt = LocalDateTime.now();
		// current time in layer-units
		int now = ((now_ldt.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now_ldt.getHour() * 12) 
				+ (now_ldt.getMinute() / 5);
		
		
		// --- calculate time to sleep ---
		
		JsonObject ptimes = Configs.getUObj(cid, Configs.ptimesViewer);
		int end = Integer.parseInt(p.currentLayerId.split("-")[1]);
		
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
			Manager.blis().onProfileChangedRunning(cid, slot, false);
			isRunning = false;
			isActivelyRunning = false;
		}
		
		Logger.print("before MemoryReleaser", Logger.general, Logger.info, cid, slot);
		Manager.gc();
		Logger.print("after MemoryReleaser", Logger.general, Logger.info, cid, slot);
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
				
				Manager.blis().onProfileTimerUpdate(cid, slot, ms);
				for(int i=0; i<5; i++)
					if(synced[i])
						Manager.blis().onProfileTimerUpdate(cid, i, ms);
				
				sleep--;
				
				if(sleep < 0) {
					t.cancel();
					runSlot();
				}
			}
		}, 0, 1000);
	}
	
	public void skipSleep() {
		sleep = 0;
	}
	
	protected abstract void slotSequence();
	
}
