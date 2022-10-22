package run.captain;

import run.AbstractProfile;
import run.Slot;

public class TelegramSlot extends Slot {

	public TelegramSlot(AbstractProfile<?> p, Slot[] slots) {
		super(p, slots, 4);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean canManageItself() {
		return false;
	}

	@Override
	protected void slotSequence() {
		// TODO Auto-generated method stub

	}

	

}
