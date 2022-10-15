package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import run.AbstractProfile;
import run.Manager;
import run.ProfileType;
import run.AbstractBackEnd.UpdateEventListener;
import srlib.RaidType;
import srlib.SRC;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;
import srlib.store.Store;
import srlib.store.Store.C;
import srlib.units.Unit;
import srlib.units.UnitType;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;

public class Viewer extends AbstractProfile<Viewer.ViewerBackEndRunnable,ViewerBackEnd> {
	
	public static interface ViewerBackEndRunnable extends AbstractProfile.BackEndRunnable<ViewerBackEnd> {}
	
	public static final int slotSize = 5;
	
	
	private static final String[] rew_sources = "chests bought event".split(" ");
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestboostedscroll chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "dungeonchests eventchests".split(" ");
	private static String[] rew_types;
	
	private static String[] genRewTypes() {
		ArrayList<String> ret = new ArrayList<String>(Arrays.asList("gold potions token eventcurrency keys meat bones skin soulvessel".split(" ")));
		ArrayList<String> utypes = new ArrayList<>(UnitType.typeUids.size());
		for(int i=0; i<UnitType.typeUids.size(); i++)
			utypes.add("scroll"+UnitType.typeUids.get(i).replace("allies", ""));
		ret.addAll(utypes);
		return ret.toArray(new String[ret.size()]);
	}
	
	@Override
	public void updateRews() {
		rew_types = genRewTypes();
		for(String s : rew_sources) {
			if(!rews.has(s))
				rews.add(s, new JsonObject());
			JsonObject source = rews.getAsJsonObject(s);
			switch(s) {
			case "chests":
				for(String t : rew_chests_chests)
					if(!source.has(t))
						source.addProperty(t, 0);
				break;
			case "bought":
				for(String t : rew_bought_chests)
					if(!source.has(t))
						source.addProperty(t, 0);
				break;
			}
			for(String t : rew_types)
				if(!source.has(t))
					source.addProperty(t, 0);
		}
	}
	
	public Viewer(String cid, SRR req) throws Exception {
		super(cid, new ViewerBackEnd(cid, req, new UpdateEventListener<ViewerBackEnd>() {
			@Override
			public void afterUpdate(String obj, ViewerBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				boolean dungeon = false;
				switch(obj) {
				case "caps::true":
					dungeon = true;
					//$FALL-THROUGH$
				case "caps::false":
					try {
						final String currentLayer = Manager.getCurrentLayer(cid);
						CaptainData[] caps;
						caps = vbe.getCaps(dungeon);
						HashSet<String> got = new HashSet<>();
						for(CaptainData c : caps)
							got.add(c.twitchUserName);
						
						HashSet<String> favs = Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign);
						for(String tun : favs) {
							if(got.contains(tun) || !Configs.getCapBoo(cid, currentLayer, tun, dungeon ? Configs.dungeon : Configs.campaign, Configs.il))
								continue;
							
							CaptainData[] results = vbe.searchCaptains(false, false, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, true, tun, 1);
							if(results.length == 0)
								continue;
							
							if(results[0].isPlaying)
								caps = add(caps, results[0]);
						}
						vbe.setCaps(caps, dungeon);
					} catch(Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve caps", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				case "units":
					try {
						Unit[] units = vbe.getUnits(false);
						for(Unit u : units)
							Configs.addUnitId(cid, ProfileType.VIEWER, ""+u.unitId, u.type.uid, u.level);
					} catch (Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve units", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				}
			}
		}), ProfileType.VIEWER, slotSize);
		updateRews();
	}
	
	protected void iniSlots() {
		int i=0;
		for(;i<slotSize-1;i++)
			slots[i] = new RaidSlot(this, slots, i);
		slots[i] = new SpecialSlot(this, slots);
	}

	
	protected Hashtable<String, Long> bannedCaps = new Hashtable<>();
	
	public void switchChange(int slot) {
		((RaidSlot) slots[slot]).switchChange();
	}
	
	public void fav(int slot, int val) {
		Raid r;
		try {
			r = be.getRaid(slot, false);
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("Viewer -> fav: err=failed to get raid", e, Logger.runerr, Logger.error, cid, slot, true);
			return;
		}
		if(r == null)
			return;
		
		Integer v = Configs.getCapInt(cid, "(all)", r.twitchUserName, r.type == RaidType.DUNGEON ? Configs.dungeon : Configs.campaign, Configs.fav);
		if(v == null 
				|| v == Integer.MAX_VALUE-1
				|| v == Integer.MIN_VALUE+1
				|| Math.signum(val)*Math.signum(v) <= 0)
			Configs.favCap(cid, "(all)", r.twitchUserName, Configs.all, val);
		else 
			Configs.favCap(cid, "(all)", r.twitchUserName, Configs.all, null);
	}
	
	
	public static boolean canUseSlot(ViewerBackEnd vbe, int slot) throws NoConnectionException, NotAuthorizedException {
		int uCount = vbe.getUnits(false).length;
		switch (slot) {
		case 0:
			return true;
		case 1:
			return uCount > 4;
		case 2:
			return uCount > 7;
		case 3:
			return vbe.hasBattlePass();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	

	private static final C[] sc = new C[] {Store.gold, Store.potions, Store.meat, Store.eventcurrency, Store.keys, Store.bones};
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	@Override
	synchronized public void updateFrame() throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;

		updateLayer();
		
		updateSlotSync();
		
		Raid[] raids = be.getRaids(SRC.BackEnd.all);

		for(int i=0; i<4; i++)
			Manager.blis().onProfileUpdateSlotViewer(cid, i, raids[i], Configs.isSlotLocked(cid, currentLayer, ""+i), ((RaidSlot) slots[i]).isChange());
		
		Hashtable<String, Integer> curs = be.getCurrencies();

		for(C key : sc) {
			String k = key.get();
			Manager.blis().onProfileUpdateCurrency(cid, ProfileType.VIEWER, k, curs.containsKey(k) ? curs.get(k) : 0);
		}
		
	}
	
	
	public void updateVbe() {
		String proxy = Configs.getStr(cid, currentLayer, Configs.proxyDomainViewer);
		String user = Configs.getStr(cid, currentLayer, Configs.proxyUserViewer);
		be.setProxyAndUserAgent(proxy.equals("") ? null : proxy, 
				Configs.getInt(cid, currentLayer, Configs.proxyPortViewer),
				user.equals("") ? null : user,
				Configs.getStr(cid, currentLayer, Configs.proxyPassViewer),
				Configs.getStr(cid, currentLayer, Configs.userAgentViewer),
				Configs.getBoolean(cid, currentLayer, Configs.proxyMandatoryViewer));
		
		be.setUpdateTimes( Configs.getInt(cid, currentLayer, Configs.unitUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.skinUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.soulsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.capsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.raidUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.mapUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.storeUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.questEventRewardsUpdateViewer));
	}
	
	public String getTwitchLink(int slot) {
		try {
			return "https://twitch.tv/"+be.getRaid(slot, false).twitchUserName;
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("Viewer -> getTwitchLink: err=failed to get raid", e, Logger.runerr, Logger.error, cid, slot, true);
		}
		return null;
	}
	
	
	
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
