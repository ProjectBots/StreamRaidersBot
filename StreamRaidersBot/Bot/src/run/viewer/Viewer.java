package run.viewer;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

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

public class Viewer extends AbstractProfile<ViewerBackEnd> {
	
	public static final int slotSize = 5;
	
	
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestboostedscroll chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "dungeonchests eventchests".split(" ");
	private static final String[] rew_basics = "gold potions token eventcurrency keys meat bones skin soulvessel".split(" ");
	
	private static String[] genRewTypes() {
		List<String> utypes = UnitType.getTypeUids();
		
		String[] ret = new String[rew_basics.length+utypes.size()];
		System.arraycopy(rew_basics, 0, ret, 0, rew_basics.length);
		for(int i=0; i<utypes.size(); i++)
			ret[rew_basics.length+i] = "scroll"+utypes.get(i).replace("allies", "");
		
		return ret;
	}
	
	@Override
	public void updateRews() {
		String[] rew_types = genRewTypes();
		for(short i=0; i<rew_sources.length; i++) {
			if(!rews.containsKey(i))
				rews.put(i, new Hashtable<>());
			Hashtable<String, Integer> source = rews.get(i);
			switch(rew_sources[i]) {
			case "chests":
				for(String t : rew_chests_chests)
					if(!source.containsKey(t))
						source.put(t, 0);
				break;
			case "bought":
				for(String t : rew_bought_chests)
					if(!source.containsKey(t))
						source.put(t, 0);
				break;
			}
			for(int j=0; j<rew_types.length; j++)
				if(!source.containsKey(rew_types[j]))
					source.put(rew_types[j], 0);
		}
	}
	
	public Viewer(String cid, SRR req) throws Exception {
		super(cid, new ViewerBackEnd(cid, req, new UpdateEventListener<ViewerBackEnd>() {
			@Override
			public void afterUpdate(String obj, ViewerBackEnd vbe) {
				Logger.print("updated "+obj, Logger.general, Logger.info, cid, null);
				if(obj.startsWith("caps::")) {
					RaidType rt = RaidType.valueOf(obj.substring(6));
					try {
						final String currentLayer = Manager.getCurrentLayer(cid);
						CaptainData[] caps = vbe.getCaps(rt);
						HashSet<String> got = new HashSet<>();
						for(CaptainData c : caps)
							got.add(c.twitchUserName);
						
						HashSet<String> favs = Configs.getFavCaps(cid, currentLayer, Configs.getListTypeByRaidType(rt));
						CaptainData[] buffer = new CaptainData[favs.size()];
						int c = 0;
						for(String tun : favs) {
							if(got.contains(tun) || !Configs.getCapBoo(cid, currentLayer, tun, Configs.getListTypeByRaidType(rt), Configs.il))
								continue;
							
							CaptainData[] results = vbe.searchCaptains(false, false, false, rt, true, tun, 1);
							if(results.length == 0)
								continue;
							
							if(results[0].isPlaying)
								buffer[c++] = results[0];
						}
						CaptainData[] res = new CaptainData[caps.length+c];
						System.arraycopy(buffer, 0, res, 0, c);
						System.arraycopy(caps, 0, res, c, caps.length);
						vbe.setCaps(res, rt);
					} catch(Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve caps", e, Logger.runerr, Logger.error, cid, null, true);
					}
				} else {
					switch(obj) {
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
	
}
