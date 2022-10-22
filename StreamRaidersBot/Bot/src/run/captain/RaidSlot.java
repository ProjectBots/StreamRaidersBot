package run.captain;

import run.AbstractProfile;
import run.Slot;

public class RaidSlot extends Slot {

	public RaidSlot(AbstractProfile<?> p, Slot[] slots) {
		super(p, slots, 0);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean canManageItself() {
		return true;
	}

	@Override
	protected void slotSequence() {
		// TODO Auto-generated method stub

	}

	

}
