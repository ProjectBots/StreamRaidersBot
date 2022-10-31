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
import run.viewer.ViewerBackEnd.QuestClaimFailedException;
import srlib.SRC;
import srlib.Quest;
import srlib.Reward;
import srlib.SRR.NotAuthorizedException;
import srlib.store.BuyableUnit;
import srlib.store.Item;
import srlib.store.Store;

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
		int ming = Configs.getInt(cid, currentLayer, Configs.upgradeMinGoldViewer);
		if(ming < 0)
			return;
		
		int maxPrice = vbe.getCurrency(Store.gold, false) - ming;
		if(maxPrice <= 10)
			//	doesnt even need to try
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

	
	private void unlock(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		int ming = Configs.getInt(cid, currentLayer, Configs.unlockMinGoldViewer);
		if(ming < 0)
			return;
		
		int maxPrice = vbe.getCurrency(Store.gold, false) - ming;
		if(maxPrice <= 10)
			//	doesnt even need to try
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

				final BuyableUnit picked = us[ind];
				useMultiExploit(() -> {
					try {
						vbe.unlockUnit(picked);
					} catch (NoConnectionException e) {}
				});
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
	
	/**
	 * collects the daily award (if any)<br>
	 * buys from the special store<br>
	 * buys scrolls<br>
	 * buys store refreshes<br>
	 * <br>
	 * this function is part of the slotSequence for this slot (see {@link #slotSequence()})<br>
	 * <br>
	 * this function will respect the config
	 * <br>
	 * this function may force reload the store<br>
	 * see {@link #store(ViewerBackEnd, boolean)} to disable a possible reload of the store
	 * @param vbe
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	private void store(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		store(vbe, true);
	}
	
	/**
	 * see {@link #store(ViewerBackEnd)}
	 * 
	 * this function may force reload the store if first is true
	 * @param vbe
	 * @param first
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	private void store(final ViewerBackEnd vbe, final boolean first) throws NoConnectionException, NotAuthorizedException {
		vbe.updateStore(false);
		
		storeCollectDaily(vbe);
		
		//	buying from dungeon, event and versus store if available
		//	TODO versus
		int min = Configs.getInt(cid, currentLayer, Configs.storeMinKeysViewer);
		if(min >= 0)
			storeBuySpecial(vbe, vbe.getCurrency(Store.keys, false) - min, SRC.Store.dungeon, Configs.keysViewer, "dungeonchests", first);
		
		min = Configs.getInt(cid, currentLayer, Configs.storeMinEventcurrencyViewer);
		if(min >= 0)
			storeBuySpecial(vbe, vbe.getCurrency(Store.eventcurrency, false) - min, SRC.Store.event, Configs.eventViewer, "eventchests", first);
		
		//	scrolls
		min = Configs.getInt(cid, currentLayer, Configs.storeMinGoldViewer);
		if(min >= 0) {
			storeBuyScrolls(vbe, min, first);
			//	inside of minGold >= 0 bcs if scroll buying is disabled, no store refreshes should be bought either
			storeBuyRefresh(vbe);
		}
		
	}
	
	/**
	 * collects the daily award if any exist
	 * @param vbe
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	private void storeCollectDaily(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		Item daily = vbe.getDaily();
		if(daily != null && !daily.purchased) {
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiDailyClaimExploitViewer)) {
				useMultiExploit(() -> {
					try {
						JsonElement err = vbe.buyItem(daily).get(SRC.errorMessage);
						if(!err.isJsonPrimitive())
							v.addRew(vbe, SRC.Run.bought, Store.eventcurrency.get(), daily.quantity);
					} catch (NoConnectionException | NotAuthorizedException e) {}
				});
			} else {
				JsonElement err = vbe.buyItem(daily).get(SRC.errorMessage);
				if(err.isJsonPrimitive())
					Logger.print("SpecialSlot (viewer) -> storeCollectDaily: err="+err.getAsString(), Logger.runerr, Logger.error, cid, 4, true);
				else
					v.addRew(vbe, SRC.Run.bought, Store.eventcurrency.get(), daily.quantity);
			}
			
		}
	}
	
	/**
	 * buys from the special store, respects config
	 * @param vbe
	 * @param maxPrice
	 * @param section
	 * @param spt
	 * @param chestType
	 * @param first
	 * @throws NoConnectionException
	 * @throws NotAuthorizedException
	 */
	private void storeBuySpecial(final ViewerBackEnd vbe, final int maxPrice, final String section, final StorePrioType spt, final String chestType, final boolean first) throws NoConnectionException, NotAuthorizedException {
		if(maxPrice <= 5)
			//	doesnt even need to try and buy sth
			return;
		
		ArrayList<Item> items = vbe.getAvailableEventStoreItems(section, false);
		Item best = null;
		int p = Integer.MIN_VALUE;
		for(final Item item : items) {
			
			Integer p_ = Configs.getStorePrioInt(cid, currentLayer, spt, item.uid);
			if(p_ == null) {
				p_ = Configs.getBoolean(cid, currentLayer, Configs.storePriceAsDefaultPrioViewer) ? item.price : -1;
				Configs.setStorePrioInt(cid, currentLayer, spt, item.uid, p_);
			}
			
			//	prefer higher prio
			//	if same prio, prefer item with highest price that can be bought
			if(p_ > p || (p_ == p && item.price > best.price && item.price <= maxPrice)) {
				best = item;
				p = p_;
			}
		}
		
		if(p < 0 || best.price > maxPrice)
			return;
		
		JsonObject resp = vbe.buyItem(best);
		JsonElement err = resp.get(SRC.errorMessage);
		if(err == null || !err.isJsonPrimitive()) {
			switch(resp.get("buyType").getAsString()) {
			case "ITEM":
				v.addRew(vbe, SRC.Run.bought, best.name, best.quantity);
				break;
			case "CHEST":
				v.addRew(vbe, SRC.Run.bought, chestType, 1);
				JsonArray data = resp.getAsJsonObject("data").getAsJsonArray("rewards");
				for(int i=0; i<data.size(); i++) {
					Reward rew = Reward.genChestReward(data.get(i).getAsString(), cid, 4);
					v.addRew(vbe, SRC.Run.bought, rew.name, rew.amount);
				}
				break;
			case "SKIN":
				v.addRew(vbe, SRC.Run.bought, "skin", 1);
				break;
			default:
				Logger.print("SpecialSlot (viewer) -> store -> storeBuySpecial: err=unknown_buyType, item="+best.toString()+", resp="+resp.toString(), Logger.runerr, Logger.error, cid, 4, true);
			}
		} else if(!err.getAsString().startsWith("not enough ")) {
			Logger.print("SpecialSlot (viewer) -> storeBuySpecial: err="+err.getAsString()+", item="+best.toString()+", resp="+resp.toString(), Logger.runerr, Logger.error, cid, 4, true);
			if(first) {
				//	error may have been caused due to the shop not being up to date
				vbe.updateStore(true);
				store(vbe, false);
				return;
			}
		}
	}
	
	/**
	 * buys scrolls from the store, respects config
	 * @param vbe
	 * @param minGold
	 * @param first
	 * @throws NotAuthorizedException
	 * @throws NoConnectionException
	 */
	private void storeBuyScrolls(final ViewerBackEnd vbe, final int minGold, final boolean first) throws NotAuthorizedException, NoConnectionException {
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
					Logger.printException("SpecialSlot (viewer) -> storeBuyScrolls: err=item is not correct, item=" + item.toString(), e, Logger.runerr, Logger.error, cid, 4, true);
					ps[i] = -1;
				}
			}
			
			int maxPrice = vbe.getCurrency(Store.gold, false) - minGold;
			
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

				ps[ind] = -1;
				
				JsonElement err = vbe.buyItem(item).get(SRC.errorMessage);
				if(err == null || !err.isJsonPrimitive()) {
					v.addRew(vbe, SRC.Run.bought, item.name, item.quantity);
					maxPrice -= item.price;
					continue;
				}
				
				if(err.getAsString().startsWith("not enough"))
					continue;

				Logger.print("SpecialSlot (viewer) -> storeBuyScrolls: err=" + err.getAsString() + ", item=" + item.toString(), Logger.lowerr, Logger.error, cid, 4, true);
				if(first) {
					//	error may have been caused due to the shop not being up to date
					vbe.updateStore(true);
					store(vbe, false);
					return;
				}
			}
		}
		
	}
	
	/**
	 * buys a store refresh, respects config
	 * @param vbe
	 * @throws NotAuthorizedException
	 * @throws NoConnectionException
	 */
	private void storeBuyRefresh(final ViewerBackEnd vbe) throws NotAuthorizedException, NoConnectionException {
		int gold = vbe.getCurrency(Store.gold, false);
		int src = vbe.getStoreRefreshCount();
		int min = Configs.getStoreRefreshInt(cid, ProfileType.VIEWER, currentLayer, src > 3 ? 3 : src);
		if(min >= 0 && min <= gold) {
			String err = vbe.refreshStore();
			if(err != null)
				Logger.print("SpecialSlot (viewer) -> storeBuyRefresh: err="+err, Logger.runerr, Logger.error, cid, 4, true);
			store(vbe);
		}
	}

	private void quest(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		ArrayList<Quest> quests = vbe.getClaimableQuests();
		
		for(final Quest q : quests) {
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiQuestExploitViewer)) {
				useMultiExploit(() -> {
					try {
						claimQuest(vbe, q, true);
					} catch (NoConnectionException e) {}
				});
			} else {
				claimQuest(vbe, q, false);
			}
		}
	}
	
	private void claimQuest(ViewerBackEnd vbe, Quest q, boolean skipError) throws NoConnectionException {
		try {
			Reward r = vbe.claimQuest(q);
			v.addRew(vbe, SRC.Run.event, r);
		} catch (QuestClaimFailedException e) {
			if(!skipError)
				Logger.printException("SpecialSlot (viewer) -> claimQuests: err=failed to claim quest", e, Logger.runerr, Logger.error, cid, 4, true);
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
	
	private void collectEvent(final ViewerBackEnd vbe, final int tier, final boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiEventExploitViewer)) {
			useMultiExploit(() -> {
				try {
					collectEventInner(vbe, tier, bp, true);
				} catch (NoConnectionException | NullPointerException e) {}
			});
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
