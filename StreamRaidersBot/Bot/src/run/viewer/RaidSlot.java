package run.viewer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import include.Json;
import include.Maths;
import include.Pathfinding;
import include.Time;
import include.Http.NoConnectionException;
import otherlib.Configs;
import otherlib.Logger;
import otherlib.MapConv;
import otherlib.Options;
import otherlib.Remaper;
import otherlib.Configs.ListType;
import otherlib.MapConv.NoFinException;
import run.Manager;
import run.Slot;
import run.StreamRaidersException;
import srlib.Map;
import srlib.SRC;
import srlib.Store;
import srlib.Unit;
import srlib.SRR.NotAuthorizedException;
import srlib.skins.Skin;
import srlib.skins.Skins;
import srlib.viewer.CaptainData;
import srlib.viewer.Raid;
import srlib.viewer.RaidType;

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
	public JsonObject dump() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	protected void slotSequence() {
		try {
			v.useBackEnd(vbe -> {
				v.updateVbe(vbe);
				
				if(!Viewer.canUseSlot(vbe, slot))
					return;

				Logger.print("chest", Logger.general, Logger.info, cid, slot);
				chest(vbe, slot);

				Logger.print("captain", Logger.general, Logger.info, cid, slot);
				captain(vbe, slot);

				Logger.print("place", Logger.general, Logger.info, cid, slot);
				place(vbe, slot);

				Logger.print("updateFrame", Logger.general, Logger.info, cid, slot);
				v.updateFrame(vbe);
			});
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException("RaidSlot (viewer) -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Logger.runerr, Logger.fatal, cid, slot, true);
		} catch (StreamRaidersException | NoCapMatchesException e) {
		} catch (Exception e) {
			Logger.printException("RaidSlot (viewer) -> slotSequence: slot=" + slot + " err=unknown", e, Logger.runerr, Logger.fatal, cid, slot, true);
		}
		Logger.print("finished slotSequence for slot "+slot, Logger.general, Logger.info, cid, slot);
	}
	
	private boolean goMultiPlace;
	private LocalDateTime placeTime = LocalDateTime.now();
	
	private void place(final ViewerBackEnd vbe, final int slot) throws NoConnectionException, NotAuthorizedException {
		//	Unit place delay
		long tdif = ChronoUnit.MILLIS.between(LocalDateTime.now(), placeTime);
		if(tdif > 0) {
			try {
				Thread.sleep(tdif);
			} catch (InterruptedException e) {}
		}
		
		Raid r = vbe.getRaid(slot, true);
		if(r == null)
			return;

		if(r.type == RaidType.VERSUS)
			return;
		
		String placeSer = r.get(SRC.Raid.placementsSerialized);
		if(!r.canPlaceUnit(Manager.getServerTime())
			|| Configs.getInt(cid, currentLayer, Configs.maxUnitPerRaidViewer) < (placeSer == null 
																					? 0 
																					: placeSer.split(vbe.getViewerUserId()).length-1))
			return;
		
		
		final boolean dungeon = r.type == RaidType.DUNGEON;
		
		//	check place settings (eg.: min loy/room) before placement (bcs of locked slots)
		
		String tdn = r.get(SRC.Raid.twitchDisplayName);
		Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, dungeon ? Configs.dungeon : Configs.campaign, Configs.ic);
		if(ic == null)
			ic = false;
		
		String ct = r.getFromNode(SRC.MapNode.chestType);
		if(ct == null)
			return;
		ct = Remaper.map(ct);
		
		//	check if epic is available
		boolean epic;
		int dunLvl = -1;
		final int loy;
		final int length;
		if(dungeon) {
			vbe.addUserDungeonInfo(r);
			JsonObject udi = r.getUserDungeonInfo();
			epic = udi.get("epicChargesUsed").getAsInt() == 0;
			loy = udi.get("completedLevels").getAsInt() + 1;
			if(epic)
				dunLvl = loy % 3;
			
			length = 360;
		} else {
			Integer potionsc = vbe.getCurrency(Store.potions, true);
			epic = potionsc == null ? false : (potionsc >= 45);
			
			loy = Integer.parseInt(r.get(SRC.Raid.pveWins));
			
			length = 1800;
		}

		final boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		
		int maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		int minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
		
		int maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		int minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		maxTimeLeft = length - maxTimeLeft;
		
		if((dungeon ^ Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot))
				|| (!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusSeconds(maxTimeLeft), Manager.getServerTime()))
				||(!ic && Time.isAfter(Manager.getServerTime(),
						Time.parse(r.get(SRC.Raid.creationDate))
						.plusSeconds(length - minTimeLeft)))
				|| (!ic && !enabled)
				|| (!ic && (loy < minLoy || loy > maxLoy))
				) {
			return;
		}
		
		Map map = vbe.getMap(slot, true);
		
		if(!Configs.getBoolean(cid, currentLayer, Configs.allowPlaceFirstViewer) 
			&& (placeSer == null ? true : Json.parseArr(placeSer).size() == 0))
			return;
		
		boolean dunNeeded = false;
		if(dungeon) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate)).plusMinutes(1);
			LocalDateTime now = Time.parse(Manager.getServerTime());
			long t = ChronoUnit.SECONDS.between(start, now);
			double tp = (100 * t) / 300.0;
			
			double mp = map.mapPower;
			int pp = map.getPlayerPower();
			
			double pd = (100 * pp) / mp;
			
			dunNeeded = tp > pd + 10;
			Logger.print("t="+t+" tp="+tp+" mp="+mp+" pp="+pp+" pd="+pd, Logger.place, Logger.info, cid, slot);
		}
		
		final Unit[] units = vbe.getPlaceableUnits(r);
		Logger.print("units="+Arrays.toString(units), Logger.units, Logger.info, cid, slot);
		
		int[] mh = new MapConv().createHeatMap(map).getMaxHeat();
		
		int re = 0;
		int retries = Configs.getInt(cid, currentLayer, Configs.unitPlaceRetriesViewer);
		int reload = Configs.getInt(cid, currentLayer, Configs.mapReloadAfterXRetriesViewer);
		HashSet<String> bannedPos = new HashSet<>();
		
		List<String> neededUnits = vbe.getNeededUnitTypesForQuests();
		
		if(Configs.getBoolean(cid, currentLayer, Configs.preferRoguesOnTreasureMapsViewer) 
			&& r.getFromNode(SRC.MapNode.nodeType).contains("treasure")
			) {
			neededUnits.add("rogue");
			neededUnits.add("flyingarcher");
		}
		
		Logger.print("neededUnits="+neededUnits, Logger.units, Logger.info, cid, slot);
		
		while(true) {
			Logger.print("place "+re, Logger.loop, Logger.info, cid, slot);
			
			if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiPlaceExploitViewer)) {
				goMultiPlace = false;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					final Place pla = findPlace(map, mh, bannedPos, neededUnits, units, epic, dungeon, dunLvl, dunNeeded, r.getFromNode(SRC.MapNode.chestType),
							Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign).contains(r.get(SRC.Raid.twitchDisplayName)), slot, vbe.getSkins(), r.get(SRC.Raid.captainId));
					if(pla == null)
						continue;
					bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiPlace) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {}
							}
							try {
								vbe.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan, pla.skin);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
				}
				goMultiPlace = true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				break;
			} else {
				final String node = Remaper.map(r.getFromNode(SRC.MapNode.chestType));
				final Place pla = findPlace(map, mh, bannedPos, neededUnits, units, epic, dungeon, dunLvl, dunNeeded, node,
						Configs.getFavCaps(cid, currentLayer, dungeon ? Configs.dungeon : Configs.campaign).contains(r.get(SRC.Raid.twitchDisplayName)), slot, vbe.getSkins(), r.get(SRC.Raid.captainId));
				

//				TODO rem
				System.out.println(5);
				
				if(pla == null) {
					Logger.print("place=null", Logger.place, Logger.info, cid, slot);
					System.out.println("place=null, "+Configs.getPStr(cid, Configs.pname)+" - "+slot);
					break;
				}
				
				Logger.print("Place="+pla.toString(), Logger.place, Logger.info, cid, slot);
				String err = vbe.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan, pla.skin);
				bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
				
//				TODO rem
				System.out.println(6);
				if(err == null) {
					if(pla.epic && !dungeon)
						vbe.decreaseCurrency(Store.potions, 45);
					
					vbe.addCurrency(Store.potions, 1);
					String ut = pla.unit.unitType;
					if(!Unit.isLegendary(ut))
						vbe.addCurrency(pla.unit.unitType, 1);
					placeTime = LocalDateTime.now().plus(Maths.ranInt(
															Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMinViewer),
															Configs.getInt(cid, currentLayer, Configs.unitPlaceDelayMaxViewer)), 
														ChronoUnit.MILLIS);
//					TODO rem
					System.out.println(9);
					break;
				}
				
//				TODO rem
				System.out.println(7);
				
				if(err.equals("NOT_ENOUGH_POTIONS")) {
					vbe.setCurrency(Store.potions, 0);
					epic = false;
				}
				
				if(err.equals("PERIOD_ENDED"))
					break;
				
				if(re++ < retries) {
					if(re % reload == 0) {
						map = vbe.getMap(slot, true);
					}
					continue;
				}
				
//				TODO rem
				System.out.println(8);
				
				Logger.print("RaidSlot (viewer) -> place: tdn="+r.toString()+" err="+err, Logger.lowerr, Logger.error, cid, slot, true);
				break;
			}
			
			
			
			
		}
		
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
			return new StringBuilder(unit.unitType)
					.append("|").append(pos[0]).append("-")
					.append(pos[1]).append(epic ? "|epic" : "").append(isOnPlan ? "|plan" : "")
					.toString();
		}
	}
	
	
	
	private static class Prio {
		public final Unit unit;
		public final int[] ps;
		public final boolean[] vs;
		public Prio(Unit unit, int np, int ep, int n, int e, boolean vn, boolean ve) {
			this.unit = unit;
			ps = new int[] {ep, np, e, n};
			vs = new boolean[] {ve, vn};
		}
		@Override
		public String toString() {
			return "{" + unit.unitType + "|ps=" + Arrays.toString(ps) + "|vs=" + Arrays.toString(vs) + "}";
		}
	}
	
	private Place findPlace(Map map, int[] mh, HashSet<String> bannedPos, List<String> neededUnits, final Unit[] units, final boolean epic, final boolean dungeon, final int dunLvl, final boolean dunNeeded, final String chest, final boolean fav, final int slot, Skins skins, final String captainId) {
		HashSet<String> nupts = map.getUsablePlanTypes(false);
		HashSet<String> eupts = map.getUsablePlanTypes(true);
		
		Prio[] prios = new Prio[units.length];
		for(int i=0; i<units.length; i++) {
			final String uType = units[i].unitType;
			final String uId = units[i].unitId;
			
			int n = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.placedunViewer : Configs.placeViewer);
			int e = Configs.getUnitInt(cid, currentLayer, uId, dungeon ? Configs.epicdunViewer : Configs.epicViewer);
			final String chests = Configs.getUnitString(cid, currentLayer, uId, Configs.chestsViewer);
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
			
			if(neededUnits.contains(uType))
				n = Integer.MAX_VALUE;
			
			
			int np = -1;
			int ep = -1;
			
			HashSet<String> pts = units[i].getPlanTypes();
			pts.remove("vibe");
			for(String pt : pts) {
				if(eupts.contains(pt))
					ep = e;
				if(nupts.contains(pt))
					np = n;
			}
			

			boolean vn = false;
			boolean ve = false;
			

			if(np < 0 && nupts.contains("vibe") && canVibe.contains(nx)) {
				np = n;
				vn = true;
			}
			
			if(ep < 0 && eupts.contains("vibe") && canVibe.contains(ex)) {
				ep = e;
				ve = true;
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
			
			prios[i] = new Prio(units[i], np, ep, n, e, vn, ve);
		}
		
		//	Fisher-Yates shuffle
		//	that way there is no favoritism bcs of the order
		Random r = new Random();
		for(int i=prios.length-1; i>0; i--) {
			int index = r.nextInt(i+1);
			Prio t = prios[index];
			prios[index] = prios[i];
			prios[i] = t;
		}
		
		Logger.print("prios=" + Arrays.toString(prios), Logger.units, Logger.info, cid, slot);
		
		for(int i=0; i<4; i++) {
			//	TODO rem
			System.out.println("i="+i);
			if(!epic && i%2 == 0)
				continue;

			//	TODO rem
			int loop = 1;
			while(true) {
				//	TODO rem
				System.out.println("loop="+loop++);
				int p = 0;
				
				for(int j=1; j<prios.length; j++) 
					if(prios[j].ps[i] > prios[p].ps[i] 
						|| ((prios[j].ps[i] == prios[p].ps[i])
							&& (Integer.parseInt(prios[j].unit.get(SRC.Unit.level)) > Integer.parseInt(prios[p].unit.get(SRC.Unit.level)))))
						p = j;
				
				//	TODO rem
				System.out.println(1);
				
				if(prios[p].ps[i] < 0)
					break;
				
				prios[p].ps[i] = -1;
				
				final Unit u = prios[p].unit;
				
				HashSet<String> pts = null;
				if(i<2) {
					pts = u.getPlanTypes();
					if(!prios[p].vs[i])
						pts.remove("vibe");
				}
				//	TODO rem
				System.out.println(2);
				
				int[] pos = null;
				try {
					pos = new Pathfinding().search(new MapConv().createField2DArray(map, u.canFly(), pts, mh, bannedPos).getField2DArray(), cid, slot, i%2==0);
				} catch (NoFinException e) {}
				if(pos == null)
					continue;
//				TODO rem
				System.out.println(3);
				if(Configs.getBoolean(cid, currentLayer, Configs.useSkinFromCaptainViewer)) {
					ArrayList<Skin> ss = skins.searchSkins(captainId, u.unitType);
//					TODO rem
					System.out.println(4);
					return new Place(u, pos, i%2==0, i<2, ss.size() == 0 ? null : ss.get(new Random().nextInt(ss.size())));
				} else {
//					TODO rem
					System.out.println(4);
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
			v.updateFrame(null);
		} catch (NoConnectionException | NotAuthorizedException e) {
			Logger.printException(cid+": Viewer -> change: slot="+slot+", err=failed to update Frame", e, Logger.runerr, Logger.error, cid, slot, true);
		}
	}
	
	public boolean isChange() {
		return change;
	}
	
	private void captain(ViewerBackEnd beh, int slot) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		captain(beh, slot, true, false);
	}

	private void captain(ViewerBackEnd vbe, int slot, boolean first, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		
		boolean dungeon = Configs.getStr(cid, currentLayer, Configs.dungeonSlotViewer).equals(""+slot);
		
		Raid r = vbe.getRaid(slot, true);
		
		if(r != null && Configs.isSlotLocked(cid, currentLayer, ""+slot)) {
			if(r.type == RaidType.DUNGEON && !dungeon) {
				Raid[] all = vbe.getRaids(SRC.BackEndHandler.all);
				boolean change = true;
				for(int i=0; i<all.length; i++) {
					if(i == slot || all[i] == null)
						continue;
					if(all[i].type == RaidType.DUNGEON && Configs.isSlotLocked(cid, currentLayer, ""+i))
						change = false;
				}
				if(change) {
					Configs.setStr(cid, currentLayer, Configs.dungeonSlotViewer, ""+slot);
					dungeon = true;
				}
			} else if(r.type != RaidType.DUNGEON && dungeon) {
				Configs.setStr(cid, currentLayer, Configs.dungeonSlotViewer, "(none)");
				dungeon = false;
			}
		}
		
		if(Configs.isSlotLocked(cid, currentLayer, ""+slot) && !change)
			return;
		
		if(r == null) {
			switchCap(vbe, slot, dungeon, null, null, noCap, first, null);
			return;
		}
		
		if(!r.isSwitchable(Manager.getServerTime(), Configs.getInt(cid, currentLayer, Configs.capInactiveTresholdViewer)))
			return;

		String tdn = r.get(SRC.Raid.twitchDisplayName);
		if(r.type == RaidType.VERSUS) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		String ct = r.getFromNode(SRC.MapNode.chestType);
		if(ct == null) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		ct = Remaper.map(ct);

		ListType list = dungeon ? Configs.dungeon : Configs.campaign;
		
		Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.ic);
		ic = ic == null ? false : ic;
		Boolean il = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.il);
		il = il == null ? false : il;
		Integer fav = Configs.getCapInt(cid, currentLayer, tdn, list, Configs.fav);
		fav = fav == null ? 0 : fav;
		
		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		Integer maxLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.maxLoyViewer);
		Integer minLoy = Configs.getChestInt(cid, currentLayer, ct, Configs.minLoyViewer);
		Boolean enabled = Configs.getChestBoolean(cid, currentLayer, ct, Configs.enabledViewer);
		Integer maxTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.maxTimeViewer);
		Integer minTimeLeft = Configs.getChestInt(cid, currentLayer, ct, Configs.minTimeViewer);
		if(maxLoy == null || minLoy == null || enabled == null || maxTimeLeft == null || minTimeLeft == null) {
			Logger.print("RaidSlot (viewer) -> captain: ct="+ct+", err=failed to get chest config", Logger.runerr, Logger.error, cid, slot, true);
			//	prevents picking the chest
			maxLoy = 5;
			minLoy = 8;
			enabled = false;
			maxTimeLeft = 10;
			minTimeLeft = 20;
		} else if(maxLoy < 0)
			maxLoy = Integer.MAX_VALUE;
			
		int length = dungeon ? 360 : 1800;
		
		maxTimeLeft = length - maxTimeLeft;
		
		if(!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusSeconds(maxTimeLeft),
								Manager.getServerTime())) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, maxTimeLeft);
			return;
		}
		
		
		JsonArray users = Json.parseArr(r.get(SRC.Raid.users));
		if(users != null) {
			String uid = vbe.getViewerUserId();
			for(int i=0; i<users.size(); i++)
				if(users.get(i).getAsJsonObject().get("userId").getAsString().equals(uid))
					minTimeLeft = Integer.MIN_VALUE;
		}
		
		String capTeam = Configs.getStr(cid, currentLayer, Configs.captainTeamViewer); //TODO rem after event
		
		if((dungeon ^ (r.type == RaidType.DUNGEON))
			|| !(ic || capTeam.equals("(none)")			//TODO rem after event
				|| capTeam.equals(r.get("teamUid")))	//TODO rem after event
			|| r.isOffline(Manager.getServerTime(), il, Configs.getInt(cid, currentLayer, Configs.capInactiveTresholdViewer))
			|| (!ic && Time.isAfter(Manager.getServerTime(),
							Time.parse(r.get(SRC.Raid.creationDate))
								.plusSeconds(length - minTimeLeft)))
			|| (!ic && !enabled)
			|| (!ic && (loy < minLoy || loy > maxLoy))
			|| fav < 0
			) {
			switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		if(change) {
			if(first)
				switchCap(vbe, slot, dungeon, r, tdn, noCap, first, null);
			else
				change = false;
		}
		
	}
	
	private Hashtable<String, LocalDateTime> banned = new Hashtable<>();
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(ViewerBackEnd beh, int slot, boolean dungeon, Raid r, String disname, boolean noCap, boolean first, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			switchCap(beh, slot, dungeon, r, disname, noCap, overrideBanTime);
		} catch (NoCapMatchesException e) {
			if(!noCap) {
				beh.updateCaps(true, dungeon);
				captain(beh, slot, first, true);
			} else {
				Logger.print("RaidSlot (viewer) -> switchCap: slot="+slot+", err=No Captain Matches Config", Logger.runerr, Logger.error, cid, slot, true);
				throw e;
			}
		}
	}
	
	private void switchCap(ViewerBackEnd beh, int slot, boolean dungeon, Raid r, String disname, boolean noCap, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {

		LocalDateTime now = Time.parse(Manager.getServerTime());
		
		if(!(r == null || disname == null)) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate));
			long plus;
			if(overrideBanTime != null)
				plus = overrideBanTime;
			else {
				switch(r.type) {
				case CAMPAIGN:
					plus = 2100;
					break;
				case DUNGEON:
					plus = 420;
					break;
				case VERSUS:
					plus = 420;
					break;
				default:
					//	won't happen, but important for compiler
					plus = 0;
				}
			}
			if(now.isAfter(start.plusSeconds(plus-120)))
				banned.put(disname, now.plusSeconds(120));
			else
				banned.put(disname, start.plusSeconds(plus));
			Logger.print("blocked " + disname, Logger.caps, Logger.info, cid, slot);
		}
		
		
		HashSet<String> removed = new HashSet<>();
		for(String key : banned.keySet().toArray(new String[banned.size()])) {
			if(now.isAfter(banned.get(key))) {
				removed.add(key);
				banned.remove(key);
			}
		}
		Logger.print("unblocked " + removed.toString(), Logger.caps, Logger.info, cid, slot);

		CaptainData[] caps = beh.getCaps(dungeon);
		Logger.print("got caps " + Arrays.toString(caps), Logger.caps, Logger.info, cid, slot);
		
		ListType list = dungeon
				? Configs.dungeon 
				: Configs.campaign;

		Raid[] all = beh.getRaids(SRC.BackEndHandler.all);
		ArrayList<String> otherCaps = new ArrayList<>();
		for(Raid raid : all) {
			if(raid == null)
				continue;
			otherCaps.add(raid.get(SRC.Raid.twitchDisplayName));
		}
		
		
		CaptainData best = null;
		int val = -1;
		int loy = 0;
		
		HashSet<String> skipped = new HashSet<>();
		
		String capTeam = Configs.getStr(cid, currentLayer, Configs.captainTeamViewer);	//TODO rem after event
		
		for(int i=0; i<caps.length; i++) {
			String tdn = caps[i].get(SRC.Captain.twitchDisplayName);

			Boolean ic = Configs.getCapBoo(cid, currentLayer, tdn, list, Configs.ic);	//TODO rem after event
			ic = ic == null ? false : ic;													//TODO rem after event
			
			Integer fav = Configs.getCapInt(cid, currentLayer, tdn, list, Configs.fav);
			fav = fav == null ? 0 : fav;
			if(fav < 0 
				|| banned.containsKey(tdn) 
				|| otherCaps.contains(tdn)
				|| !(ic || capTeam.equals("(none)")				//TODO rem after event
					|| capTeam.equals(caps[i].get("teamUid")))	//TODO rem after event
				) {
				skipped.add(tdn);
				continue;
			}
			int nloy = Integer.parseInt(caps[i].get(SRC.Captain.pveWins));
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
		
		beh.switchRaid(best, slot);
		
		Logger.print("switched to " + best.get(SRC.Captain.twitchDisplayName), Logger.caps, Logger.info, cid, slot);
		
		captain(beh, slot, false, noCap);
	}

	
	private boolean goMultiChestClaim;
	
	private void chest(ViewerBackEnd vbe, int slot) throws NoConnectionException, NotAuthorizedException {
		if(!vbe.isReward(slot))
			return;
		if(Options.is("exploits") && Configs.getBoolean(cid, currentLayer, Configs.useMultiChestExploitViewer)) {
			goMultiChestClaim = false;
			for(int i=0; i<SRC.Run.exploitThreadCount; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						while(!goMultiChestClaim) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
						}
						try {
							JsonObject rews = vbe.getChest(slot);
							if(rews == null)
								return;
							for(String rew : rews.keySet())
								v.addRew(vbe, SRC.Run.chests, rew, rews.get(rew).getAsInt());
						} catch (NoConnectionException | NotAuthorizedException e) {}
					}
				});
				t.start();
			}
			goMultiChestClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			JsonObject rews = vbe.getChest(slot);
			if(rews == null)
				return;
			for(String rew : rews.keySet())
				v.addRew(vbe, SRC.Run.chests, rew, rews.get(rew).getAsInt());
		}
		
	}

}
