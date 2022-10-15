package run.captain;

import run.AbstractProfile;
import run.Slot;

public class DiscordSlot extends Slot {

	public DiscordSlot(AbstractProfile<?, ?> p, Slot[] slots) {
		super(p, slots, 3);
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
