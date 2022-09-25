package run.captain;

import com.google.gson.JsonObject;

import run.AbstractProfile;
import run.Slot;

public class SpecialSlot extends Slot {

	public SpecialSlot(AbstractProfile<?, ?> p, Slot[] slots) {
		super(p, slots, 1);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean canManageItself() {
		return true;
	}

	@Override
	protected JsonObject dump() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void slotSequence() {
		// TODO Auto-generated method stub

	}

	

}
