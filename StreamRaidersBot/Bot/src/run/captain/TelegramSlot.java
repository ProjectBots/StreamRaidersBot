package run.captain;

import com.google.gson.JsonObject;

import run.AbstractProfile;
import run.Slot;

public class TelegramSlot extends Slot {

	public TelegramSlot(AbstractProfile<?, ?> p, Slot[] slots) {
		super(p, slots, 4);
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
