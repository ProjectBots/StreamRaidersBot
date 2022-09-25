package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.Options;
import otherlib.Remaper;
import run.AbstractProfile;
import run.Manager;
import run.ProfileType;
import run.AbstractBackEnd.UpdateEventListener;
import srlib.Map;
import srlib.SRC;
import srlib.SRR;
import srlib.Store;
import srlib.Unit;
import srlib.SRR.NotAuthorizedException;
import srlib.Store.C;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;
import srlib.viewer.RaidType;

public class Viewer extends AbstractProfile<Viewer.ViewerBackEndRunnable,ViewerBackEnd> {
	
	public static interface ViewerBackEndRunnable extends AbstractProfile.BackEndRunnable<ViewerBackEnd> {}
	
	public static final int slotSize = 5;
	
	private JsonObject rews = null;
	
	private static final String[] rew_sources = "chests bought event".split(" ");
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestboostedscroll chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "dungeonchests eventchests".split(" ");
	private static String[] rew_types;
	
	private static String[] genRewTypes() {
		ArrayList<String> ret = new ArrayList<String>(Arrays.asList("gold potions token eventcurrency keys meat bones skin".split(" ")));
		ArrayList<String> utypes = Unit.getTypesList();
		for(int i=0; i<utypes.size(); i++)
			utypes.set(i, "scroll"+utypes.get(i).replace("allies", ""));
		ret.addAll(utypes);
		return ret.toArray(new String[ret.size()]);
	}
	
	public void updateRews() {
		rew_types = genRewTypes();
		if(rews == null)
			rews = new JsonObject();
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
	
	synchronized public void addRew(ViewerBackEnd beh, String con, String type, int amount) {
		type = Remaper.map(type).replace(Options.get("currentEventCurrency"), Store.eventcurrency.get());
		try {
			JsonObject r = rews.getAsJsonObject(con);
			r.addProperty(type, r.get(type).getAsInt() + amount);
			beh.addCurrency(type, amount);
		} catch (NullPointerException e) {
			Logger.printException("Viewer -> addRew: err=failed to add reward, con=" + con + ", type=" + type + ", amount=" + amount, e, Logger.runerr, Logger.error, cid, null, true);
		}
	}
	
	public JsonObject getRews() {
		return rews;
	}
	
	@Override
	public void saveStats() {
		JsonObject astats = Configs.getUObj(cid, Configs.statsViewer);
		for(String s : rews.keySet()) {
			JsonObject stats = astats.getAsJsonObject(s);
			JsonObject rew = rews.getAsJsonObject(s);
			for(String v : rew.keySet()) {
				try {
					stats.addProperty(v, stats.get(v).getAsInt() + rew.get(v).getAsInt());
				} catch (NullPointerException e) {
					Logger.print("Viewer -> saveStats: err=unknown stat, v="+v+", s="+s, Logger.general, Logger.error, cid, null, true);
					stats.addProperty(v, rew.get(v).getAsInt());
				}
				rew.addProperty(v, 0);
			}
		}
	}
	
	
	public Viewer(String cid, SRR req) throws Exception {
		super(cid, new ViewerBackEnd(cid, req), ProfileType.VIEWER, slotSize);
		uelis = new UpdateEventListener<ViewerBackEnd>() {
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
						CaptainData[] caps;
						caps = vbe.getCaps(dungeon);
						HashSet<String> got = new HashSet<>();
						for(CaptainData c : caps)
							got.add(c.get(SRC.Captain.twitchDisplayName));
						
						HashSet<String> favs = Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign);
						for(String tdn : favs) {
							if(got.contains(tdn) || !Configs.getCapBoo(cid, currentLayer, tdn, dungeon ? Configs.dungeon : Configs.campaign, Configs.il))
								continue;
							
							JsonArray results = vbe.searchCap(1, null, false, false, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, true, tdn);
							if(results.size() == 0)
								continue;
							
							CaptainData n = new CaptainData(results.get(0).getAsJsonObject());
							
							if(n.get(SRC.Captain.isPlaying).equals("1"))
								caps = add(caps, n);
						}
						vbe.setCaps(caps, dungeon);
					} catch(Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve caps", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				case "units":
					try {
						Unit[] units = vbe.getUnits(SRC.BackEndHandler.all, false);
						for(Unit u : units)
							Configs.addUnitId(cid, ProfileType.VIEWER, u.unitId, u.unitType, Integer.parseInt(u.get(SRC.Unit.level)));
					} catch (Exception e) {
						Logger.printException("Viewer -> constructor -> uelis: err=unable to retrieve units", e, Logger.runerr, Logger.error, cid, null, true);
					}
					break;
				}
			}
		};
		useBackEnd(vbe -> {
			vbe.setUpdateEventListener(uelis);
			vbe.ini();
			raids = vbe.getRaids(SRC.BackEndHandler.all);
			curs = vbe.getCurrencies();
		});
		updateRews();
	}
	
	protected void iniSlots() {
		int i=0;
		for(;i<slotSize-1;i++)
			slots[i] = new RaidSlot(this, slots, i);
		slots[i] = new SpecialSlot(this, slots);
	}

	
	
	public void switchChange(int slot) {
		((RaidSlot) slots[slot]).switchChange();
	}
	
	public void fav(int slot, int val) {
		String cap = cnames[slot+4];
		if(cap == null)
			return;
		
		Integer v = Configs.getCapInt(cid, "(all)", cap, cnames[slot+8].equals("d") ? Configs.dungeon : Configs.campaign, Configs.fav);
		if(v == null 
				|| v == Integer.MAX_VALUE-1
				|| v == Integer.MIN_VALUE+1
				|| Math.signum(val)*Math.signum(v) <= 0)
			Configs.favCap(cid, "(all)", cap, Configs.all, val);
		else 
			Configs.favCap(cid, "(all)", cap, Configs.all, null);
	}
	
	
	public static boolean canUseSlot(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException {
		int uCount = beh.getUnits(SRC.BackEndHandler.all, false).length;
		switch (slot) {
		case 0:
			return true;
		case 1:
			return uCount > 4;
		case 2:
			return uCount > 7;
		case 3:
			return beh.hasBattlePass();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	

	private static final C[] sc = new C[] {Store.gold, Store.potions, Store.meat, Store.eventcurrency, Store.keys, Store.bones};
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	private Raid[] raids = null;
	private Hashtable<String, Integer> curs = null;
	
	
	@Override
	synchronized public void updateFrame(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;
		
		updateSlotSync();
		
		updateLayer();
		
		if(vbe != null)
			raids = vbe.getRaids(SRC.BackEndHandler.all);

		for(int i=0; i<4; i++) {
			if(raids[i] == null) {
				cnames[i] = null;
				cnames[i+4] = null;
				cnames[i+8] = null;
			} else {
				cnames[i] = raids[i].get(SRC.Raid.twitchUserName);
				cnames[i+4] = raids[i].get(SRC.Raid.twitchDisplayName);
				cnames[i+8] = raids[i].type == RaidType.DUNGEON ? "d" : "c";
			}
			Manager.blis().onProfileUpdateSlot(cid, i, raids[i], Configs.isSlotLocked(cid, currentLayer, ""+i), ((RaidSlot) slots[i]).isChange());
		}

		if(vbe != null)
			curs = vbe.getCurrencies();

		for(C key : sc) {
			String k = key.get();
			Manager.blis().onProfileUpdateCurrency(cid, k, curs.containsKey(k) ? curs.get(k) : 0);
		}
		
	}
	
	
	public void updateVbe(ViewerBackEnd beh) {
		String proxy = Configs.getStr(cid, currentLayer, Configs.proxyDomainViewer);
		String user = Configs.getStr(cid, currentLayer, Configs.proxyUserViewer);
		beh.setOptions(proxy.equals("") ? null : proxy, 
				Configs.getInt(cid, currentLayer, Configs.proxyPortViewer),
				user.equals("") ? null : user,
				Configs.getStr(cid, currentLayer, Configs.proxyPassViewer),
				Configs.getStr(cid, currentLayer, Configs.userAgentViewer),
				Configs.getBoolean(cid, currentLayer, Configs.proxyMandatoryViewer));
		
		beh.setUpdateTimes( Configs.getInt(cid, currentLayer, Configs.unitUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.raidUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.mapUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.storeUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.questEventRewardsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.capsUpdateViewer),
							Configs.getInt(cid, currentLayer, Configs.skinUpdateViewer));
	}
	
	private String[] cnames = new String[12];
	
	public String getTwitchLink(int slot) {
		return "https://twitch.tv/"+cnames[slot];
	}
	
	public Map getMap(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException {
		return beh.getMap(slot, false);
	}
	
	
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
