package run.captain;

import run.AbstractProfile;
import run.Slot;

public class SpecialSlot extends Slot {

	public SpecialSlot(AbstractProfile<?> p, Slot[] slots) {
		super(p, slots, 1);
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
