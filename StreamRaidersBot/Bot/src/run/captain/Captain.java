package run.captain;

import include.Http.NoConnectionException;
import otherlib.Logger;
import run.AbstractProfile;
import run.ProfileType;
import run.AbstractBackEnd.UpdateEventListener;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

public class Captain extends AbstractProfile<Captain.CaptainBackEndRunnable,CaptainBackEnd> {

	public static interface CaptainBackEndRunnable extends AbstractProfile.BackEndRunnable<CaptainBackEnd> {}

	public static final int slotSize = 5;
	
	public Captain(String cid, SRR req) throws Exception {
		super(cid, new CaptainBackEnd(cid, req, new UpdateEventListener<CaptainBackEnd>() {
			@Override
			public void afterUpdate(String obj, CaptainBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				switch(obj) {
				
				}
			}
		}), ProfileType.CAPTAIN, slotSize);
	}

	@Override
	protected void iniSlots() {
		slots[0] = new RaidSlot(this, slots);
		slots[1] = new SpecialSlot(this, slots);
		slots[2] = new TwitchSlot(this, slots);
		slots[3] = new DiscordSlot(this, slots);
		slots[4] = new TelegramSlot(this, slots);
	}

	
	@Override
	public void updateRews() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateFrame() throws NoConnectionException, NotAuthorizedException {
		// TODO Auto-generated method stub
		
	}

	

	
	
	



	
}
