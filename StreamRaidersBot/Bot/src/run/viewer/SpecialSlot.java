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
import srlib.Quests.Quest;
import srlib.SRR.NotAuthorizedException;
import srlib.store.BuyableUnit;
import srlib.store.Item;
import srlib.store.Store;
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
	protected void slotSequence() {
		try {
			v.updateVbe();
			
			ViewerBackEnd vbe = v.getBackEnd();
			
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
			v.updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("SpecialSlot (viewer) -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Logger.runerr, Logger.fatal, cid, slot, true);
		} catch (StreamRaidersException e) {
		} catch (Exception e) {
			Logger.printException("SpecialSlot (viewer) -> slotSequence: slot=" + slot + " err=unknown", e, Logger.runerr, Logger.fatal, cid, slot, true);
		}
		Logger.print("finished slotSequence for slot "+slot, Logger.general, Logger.info, cid, slot);
	}

	private void upgrade(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		int maxPrice = vbe.getCurrency(Store.gold, false) - Configs.getInt(cid, currentLayer, Configs.upgradeMinGoldViewer);
		if(maxPrice <= 0)
			return;
		
		BuyableUnit[] us = vbe.getUpgradeableUnits(false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = Configs.getUnitInt(cid, currentLayer, ""+us[i].unit.unitId, Configs.upgradeViewer);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0 || maxPrice < us[ind].price)
				break;
			
			String err = vbe.upgradeUnit(us[ind].unit, Configs.getUnitSpec(cid, ProfileType.VIEWER, currentLayer, ""+us[ind].unit.unitId));
			if(err != null && (!(err.equals("no specUID") || err.equals("cant upgrade unit")))) {
				Logger.print("SpecialSlot (viewer) -> upgradeUnits: type=" + us[ind].type + " err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
				break;
			}
			
			ps[ind] = -1;
			maxPrice -= us[ind].price;
		}
	}

	private boolean goMultiUnit;
	
	private void unlock(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		int maxPrice = vbe.getCurrency(Store.gold, false) - Configs.getInt(cid, currentLayer, Configs.unlockMinGoldViewer);
		if(maxPrice <= 0)
			return;
		
		BuyableUnit[] us = vbe.getUnlockableUnits(false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++)
			ps[i] = Configs.getUnitInt(cid, currentLayer, us[i].type.uid, us[i].dupe ? Configs.dupeViewer : Configs.unlockViewer);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind])
					ind = i;
			
			if(ps[ind] < 0 || maxPrice < us[ind].price)
				break;
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiUnitExploitViewer)) {
				goMultiUnit = false;
				final BuyableUnit picked = us[ind];
				for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
					new Thread(() -> {
						while(!goMultiUnit) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							vbe.unlockUnit(picked);
						} catch (NoConnectionException e) {}
					}).start();
				}
				goMultiUnit = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				vbe.updateStore(true);
			} else {
				String err = vbe.unlockUnit(us[ind]);
				if(err != null && !err.equals("not enough gold")) 
					Logger.print("SpecialSlot (viewer) -> unlock: type=" + us[ind].type + ", err=" + err, Logger.lowerr, Logger.error, cid, 4, true);
			}
			
			ps[ind] = -1;
			maxPrice -= us[ind].price;
		}
	}

	private void store(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		
		vbe.updateStore(false);
		
		//	collecting daily reward if any
		Item daily = vbe.getDaily();
		if(daily != null) {
			JsonElement err = vbe.buyItem(daily).get(SRC.errorMessage);
			if(err.isJsonPrimitive())
				Logger.print("SpecialSlot (viewer) -> store -> daily: err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
			else
				v.addRew(vbe, SRC.Run.bought, Store.eventcurrency.get(), daily.quantity);
		}
		
		//	buying from dungeon(0) and event(1) store if available
		for(final int sec : new int[] {0,1}) {
			final String section;
			final StorePrioType spt;
			final int maxPrice;
			switch(sec) {
			case 0:
				maxPrice = vbe.getCurrency(Store.keys, false) - Configs.getInt(cid, currentLayer, Configs.storeMinKeysViewer);
				section = SRC.Store.dungeon;
				spt = Configs.keysViewer;
				break;
			case 1:
				maxPrice = vbe.getCurrency(Store.eventcurrency, false) - Configs.getInt(cid, currentLayer, Configs.storeMinEventcurrencyViewer);
				section = SRC.Store.event;
				spt = Configs.eventViewer;
				break;
			default:
				//	not gonna happen but important for compiler
				section = null;
				spt = null;
				maxPrice = 0;
			}
			
			if(maxPrice <= 0)
				continue;
			
			ArrayList<Item> items = vbe.getAvailableEventStoreItems(section, false);
			Item best = null;
			int p = -1;
			for(final Item item : items) {
				
				Integer p_ = Configs.getStorePrioInt(cid, currentLayer, spt, item.uid);
				if(p_ == null) {
					p_ = Configs.getBoolean(cid, currentLayer, Configs.storePriceAsDefaultPrioViewer) ? item.price : -1;
					Configs.setStorePrioInt(cid, currentLayer, spt, item.uid, p_);
				}
				
				//	prefer higher prio
				//	if same, prefer item with highest price that can be bought
				if(p_ > p || (p_ == p && item.price > best.price && item.price <= maxPrice)) {
					best = item;
					p = p_;
				}
			}
			if(p < 0 || best.price > maxPrice)
				continue;
			
			
			JsonObject resp = vbe.buyItem(best);
			
			JsonElement err = resp.get(SRC.errorMessage);
			if(err == null || !err.isJsonPrimitive()) {
				switch(resp.get("buyType").getAsString()) {
				case "ITEM":
					v.addRew(vbe, SRC.Run.bought, best.name, best.quantity);
					break;
				case "CHEST":
					v.addRew(vbe, SRC.Run.bought, sec==0?"dungeonchests":"eventchests", 1);
					JsonArray data = resp.getAsJsonObject("data").getAsJsonArray("rewards");
					for(int i=0; i<data.size(); i++) {
						Reward rew = new Reward(data.get(i).getAsString(), cid, 4);
						v.addRew(vbe, SRC.Run.bought, rew.name, rew.quantity);
					}
					break;
				case "SKIN":
					v.addRew(vbe, SRC.Run.bought, "skin", 1);
					break;
				default:
					Logger.print("SpecialSlot (viewer) -> store -> buyItem: err=unknown buyType, buyType="+resp.get("buyType").getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
				}
			} else if(!err.getAsString().startsWith("not enough "))
				Logger.print("SpecialSlot (viewer) -> store -> buyItem: err="+err.getAsString()+", item="+best.toString(), Logger.runerr, Logger.error, cid, 4, true);
		}
		
		
		//	buying scrolls
		ArrayList<Item> items = vbe.getPurchasableScrolls();
		if(items.size() != 0) {
			int[] ps = new int[items.size()];
			for(int i=0; i<items.size(); i++) {
				final Item item = items.get(i);
				
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
			
			int maxPrice = vbe.getCurrency(Store.gold, false) - Configs.getInt(cid, currentLayer, Configs.storeMinGoldViewer);
			
			while(true) {
				int ind = 0;
				for(int i=1; i<ps.length; i++)
					if(ps[i] > ps[ind]) 
						ind = i;
				
				if(ps[ind] < 0)
					break;
				
				Item item = items.get(ind);
				
				if(item.price > maxPrice)
					break;
				
				JsonElement err = vbe.buyItem(item).get(SRC.errorMessage);
				if(err != null && err.isJsonPrimitive()) {
					if(!err.getAsString().startsWith("not enough"))
						Logger.print("SpecialSlot (viewer) -> store: err=" + err.getAsString() + ", item=" + item.toString(), Logger.lowerr, Logger.error, cid, 4, true);
				} else {
					v.addRew(vbe, SRC.Run.bought, item.name, item.quantity);
					maxPrice -= item.price;
				}
				
				ps[ind] = -1;
			}
		}
		
		Integer gold = vbe.getCurrency(Store.gold, false);
		if(gold != null) {
			int src = vbe.getStoreRefreshCount();
			int min = Configs.getStoreRefreshInt(cid, ProfileType.VIEWER, currentLayer, src > 3 ? 3 : src);
			if(min > -1 && min < gold) {
				String err = vbe.refreshStore();
				if(err != null)
					Logger.print("SpecialSlot (viewer) -> Store: err="+err, Logger.runerr, Logger.error, cid, 4, true);
				store(vbe);
			}
		}
	}

	private boolean goMultiQuestClaim;
	
	private void quest(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		ArrayList<Quest> quests = vbe.getClaimableQuests();
		
		for(final Quest q : quests) {
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiQuestExploitViewer)) {
				goMultiQuestClaim = false;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					new Thread(() -> {
						while(!goMultiQuestClaim) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							claimQuest(vbe, q, true);
						} catch (NoConnectionException e) {}
					}).start();
				}
				goMultiQuestClaim = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
			} else {
				claimQuest(vbe, q, false);
			}
		}
	}
	
	private void claimQuest(ViewerBackEnd vbe, Quest q, boolean skipError) throws NoConnectionException {
		JsonObject dat = vbe.claimQuest(q);
		JsonElement err = dat.get(SRC.errorMessage);
		boolean isErr = err.isJsonPrimitive();
		
		if(isErr && !skipError)
			Logger.print("SpecialSlot (viewer) -> claimQuests: err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
		
		if(!isErr) {
			dat = dat.getAsJsonObject("data").getAsJsonObject("rewardData");
			String item = dat.get("ItemId").getAsString();
			
			if(item.equals("goldpiecebag"))
				item = Store.gold.get();
			else if(item.contains("skin"))
				item = "skin";
			else if(Options.get("eventBadges").contains(item))
				return;
			
			int a = dat.get("Amount").getAsInt();
			v.addRew(vbe, SRC.Run.event, item, a);
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	private void event(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.isEvent())
			return;
		
		boolean bp = vbe.hasBattlePass();
		int tier = vbe.getEventTier();
		for(int i=1; i<tier; i++) {
			if(bp)
				collectEvent(vbe, i, true);
			
			if(potionsTiers.contains(i) && vbe.getCurrency(Store.potions, false) > 10)
				continue;
			
			collectEvent(vbe, i, false);
		}
	}
	
	private boolean goMultiEventClaim;
	
	private void collectEvent(final ViewerBackEnd vbe, final int tier, final boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiEventExploitViewer)) {
			goMultiEventClaim = false;
			for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
				new Thread(() -> {
					while(!goMultiEventClaim) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {}
					}
					try {
						collectEventInner(vbe, tier, bp, true);
					} catch (NoConnectionException | NullPointerException e) {}
				}).start();
			}
			goMultiEventClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			collectEventInner(vbe, tier, bp, false);
		}
		
	}
	
	private void collectEventInner(ViewerBackEnd vbe, int tier, boolean bp, boolean skipError) throws NoConnectionException {
		JsonObject ce = vbe.collectEvent(tier, bp);
		JsonElement err = ce.get(SRC.errorMessage);
		boolean isErr = err != null && err.isJsonPrimitive();
		
		if(isErr && !skipError)
			Logger.print("SpecialSlot (viewer) -> event -> collectEvent: tier="+tier+", bp="+bp+", err=" + err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
		
		if(!isErr) {
			String rew = ce.get("reward").getAsString();
			if(!rew.equals("badges"))
				v.addRew(vbe, SRC.Run.event, rew, ce.get("quantity").getAsInt());
		}
	}


}
