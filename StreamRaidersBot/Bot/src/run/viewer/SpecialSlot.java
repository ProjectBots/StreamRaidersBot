package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Configs.StorePrioType;
import run.ProfileType;
import run.Slot;
import run.StreamRaidersException;
import srlib.SRC;
import srlib.Store;
import srlib.Unit;
import srlib.Quests.Quest;
import srlib.SRR.NotAuthorizedException;
import srlib.Store.Item;
import srlib.viewer.Raid.Reward;

public class SpecialSlot extends Slot {

	private final Viewer v;
	
	public SpecialSlot(Viewer v, Slot[] slots) {
		super(v, slots, 4);
		this.v = v;
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
		try {
			v.useBackEnd(vbe -> {
				v.updateVbe(vbe);
				
				Logger.print("event", Logger.general, Logger.info, cid, slot);
				event(vbe);

				Logger.print("quest", Logger.general, Logger.info, cid, slot);
				quest(vbe);

				Logger.print("store", Logger.general, Logger.info, cid, slot);
				store(vbe);

				Logger.print("unlock", Logger.general, Logger.info, cid, slot);
				unlock(vbe);

				Logger.print("upgrade", Logger.general, Logger.info, cid, slot);
				upgrade(vbe);
				
				Logger.print("grantExtraRewards", Logger.general, Logger.info, cid, slot);
				vbe.grantTeamReward();
				vbe.grantEventQuestMilestoneReward();

				Logger.print("updateFrame", Logger.general, Logger.info, cid, slot);
				v.updateFrame(vbe);
			});
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("SpecialSlot (viewer) -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Logger.runerr, Logger.fatal, cid, slot, true);
		} catch (StreamRaidersException e) {
		} catch (Exception e) {
			Logger.printException("SpecialSlot (viewer) -> slotSequence: slot=" + slot + " err=unknown", e, Logger.runerr, Logger.fatal, cid, slot, true);
		}
		Logger.print("finished slotSequence for slot "+slot, Logger.general, Logger.info, cid, slot);
	}

	private void upgrade(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < Configs.getInt(cid, currentLayer, Configs.upgradeMinGoldViewer))
			return;
		
		Unit[] us = beh.getUnits(SRC.BackEndHandler.isUnitUpgradeable, false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = Configs.getUnitInt(cid, currentLayer, us[i].unitId, Configs.upgradeViewer);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = beh.upgradeUnit(us[ind], Configs.getUnitSpec(cid, ProfileType.VIEWER, currentLayer, us[ind].unitId));
			if(err != null && (!(err.equals("no specUID") || err.equals("cant upgrade unit")))) {
				Logger.print("SpecialSlot (viewer) -> upgradeUnits: type=" + us[ind].unitType + " err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
				break;
			}
			
			ps[ind] = -1;
		}
	}

	private boolean goMultiUnit;
	
	private void unlock(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < Configs.getInt(cid, currentLayer, Configs.unlockMinGoldViewer))
			return;
		
		Unit[] unlockable = beh.getUnits(SRC.BackEndHandler.isUnitUnlockable, true);
		if(unlockable.length == 0)
			return;
		
		int[] ps = new int[unlockable.length];
		for(int i=0; i<unlockable.length; i++)
			ps[i] = Configs.getUnitInt(cid, currentLayer, unlockable[i].unitType, unlockable[i].dupe ? Configs.dupeViewer : Configs.unlockViewer);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind])
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			if(!beh.canUnlockUnit(unlockable[ind])) {
				ps[ind] = -1;
				continue;
			}
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiUnitExploitViewer)) {
				goMultiUnit = false;
				final Unit picked = unlockable[ind];
				for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiUnit) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								beh.unlockUnit(picked);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiUnit = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				beh.updateStore(true);
			} else {
				String err = beh.unlockUnit(unlockable[ind]);
				if(err != null && !err.equals("not enough gold")) 
					Logger.print("SpecialSlot (viewer) -> unlock: type=" + unlockable[ind].unitType + ", err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
			}
			
			ps[ind] = -1;
		}
	}

	private void store(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		
		beh.updateStore(false);
		
		//	collecting daily reward if any
		Item daily = beh.getDaily();
		if(daily != null) {
			JsonElement err = beh.buyItem(daily).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				Logger.print("SpecialSlot (viewer) -> store -> daily: err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
			else
				v.addRew(beh, SRC.Run.bought, Store.eventcurrency.get(), daily.quantity);
		}
		
		//	buying from dungeon(0) and event(1) store if available
		for(final int sec : new int[] {0,1}) {
			final String section;
			final StorePrioType spt;
			switch(sec) {
			case 0:
				if(beh.getCurrency(Store.keys, false) < Configs.getInt(cid, currentLayer, Configs.storeMinKeysViewer))
					continue;
				section = SRC.Store.dungeon;
				spt = Configs.keysViewer;
				break;
			case 1:
				if(beh.getCurrency(Store.eventcurrency, false) < Configs.getInt(cid, currentLayer, Configs.storeMinEventcurrencyViewer))
					continue;
				section = SRC.Store.event;
				spt = Configs.eventViewer;
				break;
			default:
				//	not gonna happen but important for compiler
				section = null;
				spt = null;
			}
			
			ArrayList<Item> items = beh.getAvailableEventStoreItems(section, false);
			Item best = null;
			int p = -1;
			for(Item item : items) {
				Integer p_ = Configs.getStorePrioInt(cid, currentLayer, spt, item.uid);
				if(p_ == null) {
					p_ = Configs.getBoolean(cid, currentLayer, Configs.storePriceAsDefaultPrioViewer) ? item.price : -1;
					Configs.setStorePrioInt(cid, currentLayer, spt, item.uid, p_);
				}
				if(p_ > p) {
					best = item;
					p = p_;
				}
			}
			if(p < 0)
				continue;
			
			JsonObject resp = beh.buyItem(best);
			
			JsonElement err = resp.get(SRC.errorMessage);
			if(err == null || !err.isJsonPrimitive()) {
				switch(resp.get("buyType").getAsString()) {
				case "item":
					v.addRew(beh, SRC.Run.bought, best.name, best.quantity);
					break;
				case "chest":
					v.addRew(beh, SRC.Run.bought, sec==0?"dungeonchests":"eventchests", 1);
					JsonArray data = resp.getAsJsonObject("data").getAsJsonArray("rewards");
					for(int i=0; i<data.size(); i++) {
						Reward rew = new Reward(data.get(i).getAsString(), cid, 4);
						v.addRew(beh, SRC.Run.bought, rew.name, rew.quantity);
					}
					break;
				case "skin":
					v.addRew(beh, SRC.Run.bought, "skin", 1);
					break;
				default:
					Logger.print("SpecialSlot (viewer) -> store -> buyItem: err=unknown buyType, buyType="+resp.get("buyType").getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
				}
			} else if(!err.getAsString().startsWith("not enough "))
				Logger.print("SpecialSlot (viewer) -> store -> buyItem: err="+err.getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
		}
		
		
		//	buying scrolls
		if(beh.getCurrency(Store.gold, false) >= Configs.getInt(cid, currentLayer, Configs.storeMinGoldViewer)) {
			ArrayList<Item> items = beh.getPurchasableScrolls();
			if(items.size() != 0) {
				int[] ps = new int[items.size()];
				for(int i=0; i<items.size(); i++) {
					Item item = items.get(i);
					String type = item.name.replace("scroll", "");
					
					//	switch if sr decides to add more units with allies
					switch(type) {
					case "paladin":
						type = "allies" + type;
						break;
					}
					
					try {
						ps[i] = Configs.getUnitInt(cid, currentLayer, type, Configs.buyViewer);
					} catch (NullPointerException e) {
						Logger.printException("SpecialSlot (viewer) -> store: err=item is not correct, item=" + item.toString(), e, Logger.runerr, Logger.error, cid, 4, true);
						ps[i] = -1;
					}
				}
				
				
				while(true) {
					int ind = 0;
					for(int i=1; i<ps.length; i++)
						if(ps[i] > ps[ind]) 
							ind = i;
					
					if(ps[ind] < 0)
						break;
					
					Item item = items.get(ind);
					
					JsonElement err = beh.buyItem(item).get(SRC.errorMessage);
					if(err != null && err.isJsonPrimitive()) {
						if(!err.getAsString().startsWith("not enough"))
							Logger.print("SpecialSlot (viewer) -> store: err=" + err.getAsString() + ", item=" + item.toString(), Logger.lowerr, Logger.error, cid, 4, true);
					} else
						v.addRew(beh, SRC.Run.bought, item.name, item.quantity);
					
					ps[ind] = -1;
				}
			}
			
			Integer gold = beh.getCurrency(Store.gold, false);
			if(gold != null) {
				int src = beh.getStoreRefreshCount();
				int min = Configs.getStoreRefreshInt(cid, ProfileType.VIEWER, currentLayer, src > 3 ? 3 : src);
				if(min > -1 && min < gold) {
					String err = beh.refreshStore();
					if(err != null)
						Logger.print("SpecialSlot (viewer) -> Store: err="+err, Logger.runerr, Logger.error, cid, 4, true);
					store(beh);
				}
			}
			
		}
	}

	private boolean goMultiQuestClaim;
	
	private void quest(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		ArrayList<Quest> quests = beh.getClaimableQuests();
		
		for(Quest q : quests) {
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiQuestExploitViewer)) {
				goMultiQuestClaim = false;
				final Quest picked = q;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiQuestClaim) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								beh.claimQuest(picked);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiQuestClaim = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
			} else {
				JsonObject dat = beh.claimQuest(q);
				JsonElement err = dat.get(SRC.errorMessage);
				if(err.isJsonPrimitive())
					Logger.print("SpecialSlot (viewer) -> claimQuests: err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
				else {
					dat = dat.getAsJsonObject("data").getAsJsonObject("rewardData");
					String item = dat.get("ItemId").getAsString();
					if(item.equals("goldpiecebag"))
						item = Store.gold.get();
					else if(item.startsWith("skin"))
						item = "skin";
					else if(!item.startsWith("scroll") && !item.equals("eventcurrency")) {
						Logger.print("SpecialSlot (viewer) -> claimQuests: err=unknown reward, item="+item, Logger.lowerr, Logger.error, cid, 4, true);
						return;
					}
					int a = dat.get("Amount").getAsInt();
					v.addRew(beh, SRC.Run.event, item, a);
				}
			}
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	private void event(ViewerBackEnd beh) throws NoConnectionException, NotAuthorizedException {
		if(!beh.isEvent())
			return;
		
		boolean bp = beh.hasBattlePass();
		int tier = beh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(bp)
				collectEvent(beh, i, true);
			
			if(potionsTiers.contains(i) && beh.getCurrency(Store.potions, false) > 10)
				continue;
			
			collectEvent(beh, i, false);
		}
	}
	
	private boolean goMultiEventClaim;
	
	private void collectEvent(ViewerBackEnd beh, int tier, boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!beh.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiEventExploitViewer)) {
			goMultiEventClaim = false;
			for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						while(!goMultiEventClaim) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							JsonObject ce = beh.collectEvent(tier, bp);
							JsonElement err = ce.get(SRC.errorMessage);
							if(err == null || !err.isJsonPrimitive()) {
								String rew = ce.get("reward").getAsString();
								if(!rew.equals("badges"))
									v.addRew(beh, SRC.Run.event, rew, ce.get("quantity").getAsInt());
							}
						} catch (NoConnectionException e) {
						} catch (NullPointerException e) {
							Logger.print("SpecialSlot (viewer) -> event -> collectEvent(exploit): err=failed to collectEvent(exploit), tier="+tier+", bp="+bp, Logger.runerr, Logger.error, cid, 4, true);
						}
						
					}
				});
				t.start();
			}
			goMultiEventClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			JsonObject ce = beh.collectEvent(tier, bp);
			JsonElement err = ce.get(SRC.errorMessage);
			if(err != null && err.isJsonPrimitive()) {
				Logger.print("SpecialSlot (viewer) -> event -> collectEvent: tier="+tier+", bp="+bp+", err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
			} else {
				String rew = ce.get("reward").getAsString();
				if(!rew.equals("badges"))
					v.addRew(beh, SRC.Run.event, rew, ce.get("quantity").getAsInt());
			}
		}
		
	}


}