package run.captain;

import com.google.gson.JsonObject;

import run.AbstractProfile;
import run.Slot;

public class TwitchSlot extends Slot {

	public TwitchSlot(AbstractProfile<?, ?> p, Slot[] slots) {
		super(p, slots, 2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canManageItself() {
		return false;
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
