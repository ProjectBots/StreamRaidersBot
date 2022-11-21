package run.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonObject;

import include.Maths;
import include.Pathfinding;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.MapConv;
import otherlib.Options;
import otherlib.Remaper;
import otherlib.Configs.ListType;
import otherlib.MapConv.NoFinException;
import run.Slot;
import run.StreamRaidersException;
import run.viewer.ViewerBackEnd.ErrorRetrievingCaptainsException;
import srlib.RaidType;
import srlib.Reward;
import srlib.SRC;
import srlib.Time;
import srlib.SRR.NotAuthorizedException;
import srlib.map.Map;
import srlib.skins.Skin;
import srlib.skins.Skins;
import srlib.store.Store;
import srlib.units.Unit;
import srlib.units.UnitRarity;
import srlib.units.UnitType;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;

public class RaidSlot extends Slot {

	private final Viewer v;
	
	public RaidSlot(Viewer v, Slot[] slots, int slot) {
		super(v, slots, slot);
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
			
			if(!Viewer.canUseSlot(vbe, slot))
				return;

			Logger.print("chest", Logger.general, Logger.info, cid, slot);
			chest(vbe);

			Logger.print("captain", Logger.general, Logger.info, cid, slot);
			captain(vbe);

			Logger.print("place", Logger.general, Logger.info, cid, slot);
			place(vbe);

			Logger.print("updateFrame", Logger.general, Logger.info, cid, slot);
			v.updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("RaidSlot (viewer) -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Logger.runerr, Logger.fatal, cid, slot, true);
		} catch (StreamRaidersException | NoCapMatchesException e) {
		} catch (Exception e) {
			Logger.printException("RaidSlot (viewer) -> slotSequence: slot=" + slot + " err=unknown", e, Logger.runerr, Logger.fatal, cid, slot, true);
		}
		Logger.print("finished slotSequence for slot "+slot, Logger.general, Logger.info, cid, slot);
	}
	
	private long placeTime = System.currentTimeMillis();
	
	private void place(final ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		//	Unit place delay
		long tdif = placeTime - System.currentTimeMillis();
		if(tdif > 0) {
			try {
				Thread.sleep(tdif);
			} catch (InterruptedException e) {}
		}
		
		Raid r = vbe.getRaid(slot, true);
		if(r == null) {
			Logger.print("can not place, reason=r is null", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		if(r.type == RaidType.VERSUS) {
			Logger.print("can not place, reason=raid type is versus", Logger.place, Logger.info, cid, slot);
			return;
		}

		if(!r.canPlaceUnit()) {
			Logger.print("can not place, reason=can not place unit", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		final boolean dungeon = r.type == RaidType.DUNGEON;
		
		if(dungeon ^ Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot)) {
			Logger.print("can not place, reason=dungeon slot mismatch", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		Boolean ic = Configs.getCapBoo(cid, currentLayer, r.twitchUserName, dungeon ? Configs.dungeon : Configs.campaign, Configs.ic);
		if(ic == null)
			ic = false;
		
		String ct = r.chestType;
		if(ct == null) {
			Logger.print("can not place, reason=ct is null", Logger.place, Logger.info, cid, slot);
			return;
		}
		ct = Remaper.map(ct);
		

		final boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		
		if(!ic && !enabled) {
			Logger.print("can not place, reason=chest not enabled", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		int maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		int minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		
		if(((!ic && Time.isAfterServerTime(r.creationDate + r.type.raidDuration - maxTimeLeft)))
				|| (!ic && Time.isBeforeServerTime(r.creationDate + r.type.raidDuration - minTimeLeft))) {
			Logger.print("can not place, reason=time mismatch", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		boolean epic;
		int dunLvl = -1;
		final int loy;
		if(dungeon) {
			vbe.addUserDungeonInfo(r);
			JsonObject udi = r.getUserDungeonInfo();
			epic = udi.get("epicChargesUsed").getAsInt() == 0;
			loy = udi.get("completedLevels").getAsInt() + 1;
			if(epic)
				dunLvl = loy % 3;
		} else {
			Integer potionsc = vbe.getCurrency(Store.potions, true);
			epic = potionsc == null ? false : (potionsc >= 45);
			loy = r.pveWins;
		}
		
		
		int maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		int minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
		
		if(!ic && (loy < minLoy || loy > maxLoy)) {
			Logger.print("can not place, reason=loyalty mismatch", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		
		Map map = vbe.getMap(slot, true);
		
		if(Configs.getInt(cid, currentLayer, Configs.maxUnitPerRaidViewer) < map.countUnitsFrom(vbe.getViewerUserId())) {
			Logger.print("can not place, reason=max unit per raid reached", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		if(r.powerCurrent == 0 && !Configs.getBoolean(cid, currentLayer, Configs.allowPlaceFirstViewer)) {
			Logger.print("can not place, reason=not allowed to place first", Logger.place, Logger.info, cid, slot);
			return;
		}
		
		boolean dunNeeded = false;
		if(dungeon) {
			long now = Time.getServerTime();
			
			long timeElapsed = now - r.creationDate - RaidType.DUNGEON.planningPeriodDuration;
			dunNeeded = (double) timeElapsed / (RaidType.DUNGEON.raidDuration - RaidType.DUNGEON.planningPeriodDuration) > (double) r.powerCurrent / map.mapPower + 0.1;
			Logger.print("timeElapsed="+timeElapsed+" mapPower="+map.mapPower+" powerCurrent="+r.powerCurrent, Logger.place, Logger.info, cid, slot);
		}
		
		final Unit[] units = vbe.getPlaceableUnits(r);
		Logger.print("units="+Arrays.toString(units), Logger.units, Logger.info, cid, slot);
		
		int[] mh = new MapConv().createHeatMap(map).getMaxHeat();
		
		int re = 0;
		int retries = Configs.getInt(cid, currentLayer, Configs.unitPlaceRetriesViewer);
		int reload = Configs.getInt(cid, currentLayer, Configs.mapReloadAfterXRetriesViewer);
		HashSet<String> bannedPos = new HashSet<>();
		
		HashSet<UnitType> neededUnits = vbe.getNeededUnitTypesForQuests();
		
		if(Configs.getBoolean(cid, currentLayer, Configs.preferRoguesOnTreasureMapsViewer) 
			&& r.nodeType.contains("treasure")
			) {
			neededUnits.add(UnitType.getType("rogue"));
			neededUnits.add(UnitType.getType("flyingarcher"));
		}
		
		Logger.print("neededUnits="+neededUnits, Logger.units, Logger.info, cid, slot);
		final boolean isFav = Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign).contains(r.twitchUserName);


		final String chest = Remaper.map(r.chestType);
		while(true) {
			Logger.print("place "+re, Logger.loop, Logger.info, cid, slot);
			
			final Place pla = findPlace(map, mh, bannedPos, neededUnits, units, epic, dungeon, dunLvl, dunNeeded, chest, isFav, vbe.getSkins(false), r.captainId);
			if(pla == null) {
				Logger.print("place=null", Logger.place, Logger.info, cid, slot);
				System.out.println("place=null, "+Configs.getPStr(cid, Configs.pname)+" - "+slot);
				break;
			}
			Logger.print("Place="+pla.toString(), Logger.place, Logger.info, cid, slot);
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiPlaceExploitViewer)) {
				useMultiExploit(() -> {
					try {
						placeUnit(vbe, pla, dungeon);
					} catch (NoConnectionException e) {}
				});
				break;
			} else {
				String err = placeUnit(vbe, pla, dungeon);
				
				if(err == null || err.equals("PERIOD_ENDED"))
					break;
				
				bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
				
				if(err.equals("NOT_ENOUGH_POTIONS")) {
					vbe.setCurrency(Store.potions, 0);
					epic = false;
				}
				
				if(re++ < retries) {
					if(re % reload == 0) {
						map = vbe.getMap(slot, true);
					}
					continue;
				}
				
				Logger.print("RaidSlot (viewer) -> place: tdn="+r.toString()+", pla="+pla.toString()+", err="+err, Logger.lowerr, Logger.error, cid, slot, true);
				break;
			}
			
		}
		
	}
	
	private String placeUnit(ViewerBackEnd vbe, Place pla, boolean dungeon) throws NoConnectionException {
		String err = vbe.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan, pla.skin);
		
		if(err == null) {
			if(pla.epic && !dungeon)
				vbe.decreaseCurrency(Store.potions, 45);
			
			vbe.addCurrency(Store.potions, 1);
			UnitType ut = pla.unit.type;
			if(ut.rarity != UnitRarity.LEGENDARY)
				vbe.addCurrency(pla.unit.type.uid, 1);
			placeTime = System.currentTimeMillis() 
						+ Maths.ranInt(Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMinViewer),
									Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMaxViewer));
		}
		
		return err;
	}
	
	
	private static class Place {
		public final Unit unit;
		public final int[] pos;
		public final boolean epic;
		public final boolean isOnPlan;
		public final Skin skin;
		public Place(Unit u, int[] pos, boolean epic, boolean isOnPlan, Skin skin) {
			this.unit = u;
			this.pos = pos;
			this.epic = epic;
			this.isOnPlan = isOnPlan;
			this.skin = skin;
		}
		@Override
		public String toString() {
			return new StringBuilder(unit.type.uid)
					.append("|").append(pos[0]).append("-")
					.append(pos[1]).append(epic ? "|epic" : "").append(isOnPlan ? "|plan" : "")
					.toString();
		}
	}
	
	
	
	private static class Prio {
		public final Unit unit;
		public final int[] ps;
		public final boolean[] vs;
		public Prio(Unit unit, int np, int ep, int n, int e, boolean nv, boolean ev) {
			this.unit = unit;
			ps = new int[] {ep, np, e, n};
			vs = new boolean[] {ev, nv};
		}
		@Override
		public String toString() {
			return new StringBuffer("{unit=").append(unit.toString())
					.append(", ps=").append(Arrays.toString(ps))
					.append(", vs=").append(Arrays.toString(vs))
					.append("}").toString();
		}
	}
	
	private Place findPlace(Map map, int[] mh, HashSet<String> bannedPos, HashSet<UnitType> neededUnits, final Unit[] units, final boolean epic, final boolean dungeon, final int dunLvl, final boolean dunNeeded, final String chest, final boolean fav, Skins skins, final String captainId) {
		HashSet<String> nupts = map.getUsablePlanTypes(false);
		HashSet<String> eupts = map.getUsablePlanTypes(true);
		
		Prio[] prios = new Prio[units.length];
		for(int i=0; i<units.length; i++) {
			final UnitType ut = units[i].type;
			final String uId = ""+units[i].unitId;
			
			int n = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.placedunViewer : Configs.placeViewer);
			int e = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.epicdunViewer : Configs.epicViewer);
			final String chests = Configs.getUnitString(cid, currentLayer, uId, Configs.chestsViewer) + ",";
			final String favOnly = Configs.getUnitString(cid, currentLayer, uId, Configs.favOnlyViewer);
			final String markerOnly = Configs.getUnitString(cid, currentLayer, uId, Configs.markerOnlyViewer);
			final String canVibe = Configs.getUnitString(cid, currentLayer, uId, Configs.canVibeViewer);
			
			final String nx = dungeon ? "nd" : "nc";
			final String ex = dungeon ? "ed" : "ec";
			
			
			if(!fav && favOnly.contains(nx))
				n = -1;
			
			if(!fav && favOnly.contains(ex))
				e = -1;
			
			if(!chests.contains(chest+",")) {
				n = -1;
				e = -1;
			}
			
			if(neededUnits.contains(ut))
				n = Integer.MAX_VALUE;
			
			int ep = -1;
			if(eupts.contains(ut.ptag) || eupts.contains(ut.role.uid))
				ep = e;
			

			int np = -1;
			if(nupts.contains(ut.ptag) || nupts.contains(ut.role.uid))
				np = n;
			

			boolean nv = false;
			if(nupts.contains("vibe") && canVibe.contains(nx)) {
				np = n;
				nv = true;
			}
			

			boolean ev = false;
			if(eupts.contains("vibe") && canVibe.contains(ex)) {
				ep = e;
				ev = true;
			}
			
			if(markerOnly.contains(nx))
				n = -1;
			
			if(markerOnly.contains(ex))
				e = -1;
			
			if(dungeon) {
				String dem = Configs.getUnitString(cid, currentLayer, uId, Configs.dunEpicModeViewer);
				boolean ce = true;
				inner: {
					int nl;
					switch(dem) {
					case "ifNeeded":
						ce = dunNeeded;
						break inner;
					case "boss":
						nl = 0;
						break;
					case "first":
						nl = 1;
						break;
					case "second":
						nl = 2;
						break;
					default:
						nl = -1;
					}
					ce = nl == dunLvl;
				}
				if(!ce) {
					ep = -1;
					e = -1;
				}
			}
			
			prios[i] = new Prio(units[i], np, ep, n, e, nv, ev);
		}
		
		//	shuffle to prevent favoritism bcs of the order
		ArrayUtils.shuffle(prios);
		
		Logger.print("prios=" + Arrays.toString(prios), Logger.units, Logger.info, cid, slot);
		
		for(int i=0; i<4; i++) {
			Logger.print("outer i="+i, Logger.place, Logger.info, cid, slot);
			if(!epic && i%2 == 0)
				continue;

			int loop = 1;
			while(true) {
				Logger.print("inner loop="+loop++, Logger.place, Logger.info, cid, slot);
				int p = 0;
				
				for(int j=1; j<prios.length; j++) 
					if(prios[j].ps[i] > prios[p].ps[i] 
						|| (prios[j].ps[i] == prios[p].ps[i]
							&& prios[j].unit.level > prios[p].unit.level))
						p = j;
				
				Logger.print("inner 1", Logger.place, Logger.info, cid, slot);
				
				if(prios[p].ps[i] < 0)
					break;
				
				prios[p].ps[i] = -1;
				
				final Unit u = prios[p].unit;
				
				Set<String> pts = null;
				if(i<2) {
					pts = new HashSet<>();
					pts.add(u.type.ptag);
					pts.add(u.type.role.uid);
					if(prios[p].vs[i])
						pts.add("vibe");
					
				}
				
				Logger.print("inner 2", Logger.place, Logger.info, cid, slot);
				
				int[] pos = null;
				try {
					pos = new Pathfinding().search(new MapConv().createField2DArray(map, u.type.canFly, pts, mh, bannedPos).getField2DArray(), cid, slot, i%2==0);
				} catch (NoFinException e) {}
				if(pos == null)
					continue;
				
				Logger.print("inner 3", Logger.place, Logger.info, cid, slot);
				if(Configs.getBoolean(cid, currentLayer, Configs.useSkinFromCaptainViewer)) {
					ArrayList<Skin> ss = skins.searchSkins(captainId, u.type);
					return new Place(u, pos, i%2==0, i<2, ss.size() == 0 ? null : ss.get(new Random().nextInt(ss.size())));
				} else {
					return new Place(u, pos, i%2==0, i<2, null);
				}
			}
		}
		
		return null;
	}
	

	private boolean change = false;
	public void switchChange() {
		change = !change;
		try {
			v.updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException(cid+": Viewer -> change: slot="+slot+", err=failed to update Frame", e, Logger.runerr, Logger.error, cid, slot, true);
		}
	}
	
	public boolean isChange() {
		return change;
	}
	
	private void captain(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			captain(vbe, false);
		} catch (ErrorRetrievingCaptainsException e) {
			Logger.printException("RaidSlot -> captain: err=failed to retrieve captains", e, Logger.runerr, Logger.error, cid, slot, true);
		}
	}

	private void captain(ViewerBackEnd vbe, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException, ErrorRetrievingCaptainsException {
		
		Raid r = vbe.getRaid(slot, true);
		
		final RaidType rt = getRaidTypeCheckLockedSlot(vbe, r);
		
		if(Configs.isSlotLocked(cid, currentLayer, ""+slot) && !change)
			return;
		
		if(r == null) {
			Logger.print("switched due raid=null", Logger.caps, Logger.info, cid, slot);
			change = false;
			switchCap(vbe, rt, null, null, noCap);
			return;
		}
		
		if(r.placedUnit && !r.placementEnded) {
			Logger.print("can not switch raid due unit placed", Logger.caps, Logger.info, cid, slot);
			return;
		}

		final String tun = r.twitchUserName;
		
		if(change) {
			Logger.print("switched due change=true", Logger.caps, Logger.info, cid, slot);
			change = false;
			switchCap(vbe, rt, r, tun, noCap);
		}
		
		ListType list = Configs.getListTypeByRaidType(rt);
		
		Boolean il = Configs.getCapBoo(cid, currentLayer, tun, list, Configs.il);
		il = il == null ? false : il;
		
		if(r.isOffline(!il, Configs.getInt(cid, currentLayer, Configs.capInactiveTresholdViewer)*60)) {
			Logger.print("switched due offline", Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		}
		
		if(r.placedUnit) {
			Logger.print("placed unit", Logger.caps, Logger.info, cid, slot);
			return;
		}
		
		if(r.isCodeLocked) {
			Logger.print("switched due code locked", Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		}
		
		//	TODO remove at some point and time
		if(r.type == RaidType.VERSUS) {
			Logger.print("switched due versus", Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		}
		
		if(r.type != rt) {
			Logger.print("switched due slot type mismatch, r is "+r.type.toString()+", should be "+rt.toString(), Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		}
		
		String ct = Remaper.map(r.chestType);
		if(ct.equals("nochest")) {
			Logger.print("switched due nochest", Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		}

		Boolean ic = Configs.getCapBoo(cid, currentLayer, tun, list, Configs.ic);
		ic = ic == null ? false : ic;
		Integer fav = Configs.getCapInt(cid, currentLayer, tun, list, Configs.fav);
		fav = fav == null ? 0 : fav;
		
		int loy = switch(rt) {
		case CAMPAIGN -> r.pveWins;
		//	after every dungeon room, one boon is being added
		case DUNGEON -> loy = (r.allyBoons+",").split(",").length;
		//	TODO versus loy
		case VERSUS -> loy = 0;
		default -> throw new IllegalArgumentException("Unexpected value: " + rt);
		};
		
		Integer maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		Integer minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		Boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		Integer maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		Integer minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		if(maxLoy == null || minLoy == null || enabled == null || maxTimeLeft == null || minTimeLeft == null) {
			Logger.print("RaidSlot (viewer) -> captain: ct="+ct+", err=failed to get chest config", Logger.runerr, Logger.error, cid, slot, true);
			Logger.print("switched due no chest config", Logger.caps, Logger.info, cid, slot);
			switchCap(vbe, rt, r, tun, noCap);
			return;
		} else if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
		
		
		boolean[] conditions = new boolean[] {
				!ic && (rt != RaidType.DUNGEON) && Time.isBeforeServerTime(r.creationDate + r.type.raidDuration - minTimeLeft),
				!ic && rt != RaidType.DUNGEON && Time.isAfterServerTime(r.creationDate + r.type.raidDuration - maxTimeLeft),
				!ic && !enabled,
				!ic && (loy < minLoy || loy > maxLoy),
				fav < 0
		};
		
		for(int i=0; i<conditions.length; i++) {
			if(conditions[i]) {
				Logger.print("switched due main "+i, Logger.caps, Logger.info, cid, slot);
				switchCap(vbe, rt, r, tun, noCap);
				return;
			}
		}
		
	}
	
	private RaidType getRaidTypeCheckLockedSlot(ViewerBackEnd vbe, Raid r) throws NoConnectionException, NotAuthorizedException {
		final RaidType ret;
		if(Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot))
			ret = RaidType.DUNGEON;
		else if(Configs.getStr(cid, currentLayer, Configs.versusSlotViewer).equals(""+slot))
			ret = RaidType.VERSUS;
		else
			ret = RaidType.CAMPAIGN;
		
		
		if(r == null || !Configs.isSlotLocked(cid, currentLayer, ""+slot) || r.type == ret)
			return ret;
		
		if(ret == RaidType.CAMPAIGN) {
			for(int i=0; i<4; i++) {
				if(!Configs.isSlotLocked(cid, currentLayer, ""+i)) {
					switchSlotRaidType(i, r.type);
					break;
				}
			}
			switchSlotRaidType(slot, ret);
		} else {
			Raid[] all = vbe.getRaids(SRC.BackEnd.all);
			
			for(int i=0; i<all.length; i++) {
				if(i == slot || all[i] == null)
					continue;
				
				if(all[i].type == ret && Configs.isSlotLocked(cid, currentLayer, ""+i))
					//	cannot switch slot raid type
					return ret;
			}
			
			switchSlotRaidType(slot, ret);
		}
		
		return ret;
	}
	
	private void switchSlotRaidType(int slot, RaidType rt) {
		switch(rt) {
		case DUNGEON:
			Configs.setStr(cid, currentLayer, Configs.dungeonSlotViewer, ""+slot);
			break;
		case VERSUS:
			Configs.setStr(cid, currentLayer, Configs.versusSlotViewer, ""+slot);
			break;
		default: break;
		}
	}
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(ViewerBackEnd vbe, final RaidType rt, Raid r, String cap, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException, ErrorRetrievingCaptainsException {
		try {
			switchCap_(vbe, rt, r, cap, noCap);
		} catch (NoCapMatchesException e) {
			if(!noCap) {
				vbe.updateCaps(true, rt);
				try {
					switchCap_(vbe, rt, r, cap, true);
				} catch (NoCapMatchesException e2) {
					Logger.print("RaidSlot (viewer) -> switchCap: slot="+slot+", err=No Captain Matches Config", Logger.runerr, Logger.error, cid, slot, true);
				}
			}
		}
	}
	
	private void switchCap_(ViewerBackEnd vbe, final RaidType rt, Raid r, String cap, boolean noCap) throws NoConnectionException, NotAuthorizedException, ErrorRetrievingCaptainsException, NoCapMatchesException {

		long now = Time.getServerTime();
		
		if(r != null && cap != null) {
			v.bannedCaps.put(cap, (now - (r.creationDate + r.type.raidDuration - 120) > 0 ? now : r.creationDate + r.type.raidDuration) + 3*60);
			Logger.print("blocked " + cap, Logger.caps, Logger.info, cid, slot);
		}
		
		HashSet<String> removed = new HashSet<>();
		for(String key : v.bannedCaps.keySet().toArray(new String[v.bannedCaps.size()])) {
			if(v.bannedCaps.get(key) < now) {
				removed.add(key);
				v.bannedCaps.remove(key);
			}
		}
		Logger.print("unblocked " + removed.toString(), Logger.caps, Logger.info, cid, slot);

		CaptainData[] caps = vbe.getCaps(rt);
		Logger.print("got caps " + Arrays.toString(caps), Logger.caps, Logger.info, cid, slot);
		
		Raid[] all = vbe.getRaids(SRC.BackEnd.all);
		HashSet<String> otherCaps = new HashSet<>();
		for(Raid raid : all) {
			if(raid == null)
				continue;
			otherCaps.add(raid.twitchUserName);
		}
		
		
		CaptainData best = null;
		int val = -1;
		int loy = 0;
		
		HashSet<String> skipped = new HashSet<>();
		
		ListType list = Configs.getListTypeByRaidType(rt);

		for(int i=0; i<caps.length; i++) {
			String tun = caps[i].twitchUserName;

			Integer fav = Configs.getCapInt(cid, currentLayer, tun, list, Configs.fav);
			fav = fav == null ? 0 : fav;
			if(fav < 0 
				|| v.bannedCaps.containsKey(tun)
				|| otherCaps.contains(tun)
				) {
				skipped.add(tun);
				continue;
			}
			int nloy = caps[i].pveWins;
			if(fav > val || (fav == val && nloy > loy)) {
				best = caps[i];
				val = fav;
				loy = nloy;
				continue;
			}
		}
		
		Logger.print("skipped " + skipped.toString(), Logger.caps, Logger.info, cid, slot);
		
		if(best == null) 
			throw new NoCapMatchesException();
		
		String err = vbe.switchRaid(best, slot);
		
		if(err == null)
			Logger.print("switched to " + best.twitchDisplayName, Logger.caps, Logger.info, cid, slot);
		else {
			Logger.print("RaidSlot -> switchCap: err="+err, Logger.runerr, Logger.error, cid, slot, true);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
		
		captain(vbe, noCap);
	}

	
	
	private void chest(ViewerBackEnd vbe) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.isReward(slot))
			return;
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiChestExploitViewer)) {
			useMultiExploit(() -> {
				try {
					ArrayList<Reward> rews = vbe.getChest(slot);
					for(Reward r : rews)
						v.addRew(vbe, SRC.Run.chests, r.name, r.amount);
				} catch (Exception e) {}
			});
		} else {
			try {
				ArrayList<Reward> rews = vbe.getChest(slot);
				for(Reward r : rews)
					v.addRew(vbe, SRC.Run.chests, r.name, r.amount);
			} catch (RuntimeException e) {
				Logger.printException("RaidSlot (viewer) -> chest: err=failed to claim chest", e, Logger.runerr, Logger.error, cid, slot, true);
			}
		}
		
	}

}
