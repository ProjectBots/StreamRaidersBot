package run.captain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import run.AbstractBackEnd;
import run.AbstractProfile;
import run.Manager;
import run.ProfileType;
import run.AbstractBackEnd.UpdateEventListener;
import run.AbstractProfile.BackEndRunnable;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

public class Captain extends AbstractProfile<Captain.CaptainBackEndRunnable,CaptainBackEnd> {

	public static interface CaptainBackEndRunnable extends AbstractProfile.BackEndRunnable<CaptainBackEnd> {}

	public static final int slotSize = 5;
	
	public Captain(String cid, SRR req) throws Exception {
		super(cid, new CaptainBackEnd(cid, req), ProfileType.CAPTAIN, slotSize);
		uelis = new UpdateEventListener<CaptainBackEnd>() {
			@Override
			public void afterUpdate(String obj, CaptainBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				switch(obj) {
				
				}
			}
		};
		useBackEnd(cbe -> {
			cbe.setUpdateEventListener(uelis);
			cbe.ini();
		});
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
	public void saveStats() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFrame(CaptainBackEnd be) throws NoConnectionException, NotAuthorizedException {
		// TODO Auto-generated method stub
		
	}

	
	
	



	
}
