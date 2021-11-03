package run;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Heatmap;
import include.Json;
import include.Maths;
import include.Pathfinding;
import include.Time;
import program.Captain;
import program.ConfigsV2;
import program.Raid;
import program.Raid.Reward;
import program.Remaper;
import program.SRC;
import program.Store;
import include.Http.NoConnectionException;
import program.SRR.NotAuthorizedException;
import program.SRR.OutdatedDataException;

import program.Store.C;
import program.Unit;
import run.BackEndHandler.UpdateEventListener;
import program.ConfigsV2.ListType;
import program.MapConv.NoFinException;
import program.QuestEventRewards.Quest;
import program.Debug;
import program.Map;
import program.MapConv;
import program.Options;

public class Run {
	
	/*	TODO
	 * 	proxy mandatory - disable proxy if allowed and needed (for example if proxy doesn't respond anymore)
	 * 	make about page + fonts
	 *  donator page fonts
	 * 	config import
	 * 	config import from old client
	 * 	config import/export fonts
	 * 	place only on marker: differentiate campaign and dungeon
	 * 	option to suppress specific error popups
	 * 	kill (slot) round and restart if it takes more than x min
	 * 	debug: pn and slot as parameters
	 * 	remove button for failed profiles
	 * 
	 * 	after release:
	 * 	get Donators from github source
	 * 	
	 * 
	 */

	private String cid;
	private BackEndHandler beh;
	
	private String currentLayer = "(default)";
	private String currentLayerId = null;
	private boolean[] isRunning = new boolean[5];
	private boolean[] change = new boolean[4];
	private int[] sleep = new int[5];
	
	private boolean ready = false;
	public void setReady(boolean b) {
		ready = b;
	}
	
	private JsonObject rews = null;
	
	private static final String[] rew_sources = "chests bought event".split(" ");
	private static final String[] rew_chests_chests = "chestboostedgold chestbosssuper chestboostedskin chestboss chestboostedtoken chestgold chestsilver chestbronze chestsalvage".split(" ");
	private static final String[] rew_bought_chests = "polterheistorangechest polterheistgreenchest polterheistpurplechest dungeonchest vampirechest".split(" ");
	private static final String[] rew_types = "gold potions token candy keys meat bones skins scrollmage scrollwarbeast scrolltemplar scrollorcslayer scrollballoonbuster scrollartillery scrollflyingarcher scrollberserker scrollcenturion scrollmusketeer scrollmonk scrollbuster scrollbomber scrollbarbarian scrollpaladin scrollhealer scrollvampire scrollsaint scrollflagbearer scrollrogue scrollwarrior scrolltank scrollarcher".split(" ");
	
	
	private void iniRews() {
		rews = new JsonObject();
		for(String s : rew_sources) {
			JsonObject source = new JsonObject();
			switch(s) {
			case "chests":
				for(String t : rew_chests_chests)
					source.addProperty(t, 0);
				break;
			case "bought":
				for(String t : rew_bought_chests)
					source.addProperty(t, 0);
				break;
			}
			for(String t : rew_types)
				source.addProperty(t, 0);
			rews.add(s, source);
		}
	}
	
	synchronized public void addRew(String con, String type, int amount) {
		type = Remaper.map(type);
		try {
			JsonObject r = rews.getAsJsonObject(con);
			r.addProperty(type, r.get(type).getAsInt() + amount);
			beh.addCurrency(type.replace("scroll", ""), amount);
		} catch (NullPointerException e) {
			Debug.printException("Run -> addRew: err=failed to add reward, con=" + con + ", type=" + type + ", amount=" + amount, e, Debug.runerr, Debug.error, true);
		}
	}
	
	public JsonObject getRews() {
		return rews;
	}
	

	public void saveStats() {
		JsonObject astats = ConfigsV2.getPObj(cid, ConfigsV2.stats);
		for(String s : rews.keySet()) {
			JsonObject stats = astats.getAsJsonObject(s);
			JsonObject rew = rews.getAsJsonObject(s);
			for(String v : rew.keySet())
				stats.addProperty(v, stats.get(v).getAsInt() + rew.get(v).getAsInt());
			
		}
		iniRews();
	}
	
	public static interface FrontEndHandler {
		public default void onStart(String cid, int slot) {}
		public default void onStop(String cid, int slot) {}
		public default void onTimerUpdate(String cid, int slot, String time) {}
		public default void onUpdateSlot(String cid, int slot, Raid raid, boolean locked, boolean change) {}
		public default void onUpdateCurrency(String type, int amount) {}
		public default void onUpdateLayer(String name, Color col) {}
	}
	
	private FrontEndHandler feh = new FrontEndHandler() {};
	
	public void setFrontEndHandler(FrontEndHandler feh) {
		this.feh = feh;
	}
	
	
	public static class StreamRaidersException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private Exception cause;
		private String text;
		public StreamRaidersException(String text, Exception cause, boolean silent) {
			this.cause = cause;
			this.text = text;
			Debug.printException(text, cause, Debug.runerr, Debug.fatal, !silent);
		}
		public StreamRaidersException(String text, Exception cause) {
			this.cause = cause;
			this.text = text;
			Debug.printException(text, cause, Debug.runerr, Debug.fatal, true);
		}
		public StreamRaidersException(String text) {
			cause = null;
			this.text = text;
			Debug.print(text, Debug.runerr, Debug.fatal, true);
		}
		public String getText() {
			return text;
		}
		public Exception getCause() {
			return cause;
		}
	}
	
	
	public Run(String cid, String cookies) throws NotAuthorizedException, NoConnectionException, OutdatedDataException {
		this.cid = cid;
		pn = ConfigsV2.getPStr(cid, ConfigsV2.name);
		beh = new BackEndHandler(cookies);
		beh.setUpdateEventListener(new UpdateEventListener() {
			@Override
			public void afterUpdate(String obj) {
				Debug.print(pn+" updated "+obj, Debug.general, Debug.info);
				if(obj.contains("caps::")) {
					boolean dungeon = obj.contains("::true");
					Captain[] caps;
					try {
						caps = beh.getCaps(dungeon);
						HashSet<String> got = new HashSet<>();
						for(Captain c : caps)
							got.add(c.get(SRC.Captain.twitchDisplayName));
						
						JsonArray favs = ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign);
						for(int i=0; i<favs.size(); i++) {
							String tdn = favs.get(i).getAsString();
							if(got.contains(tdn) || !ConfigsV2.getCapBoo(cid, currentLayer, tdn, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.il))
								continue;
							
							JsonArray results = beh.searchCap(1, 8, false, false, dungeon ? SRC.Search.dungeons : SRC.Search.campaign, true, tdn);
							if(results.size() == 0)
								continue;
							
							Captain n = new Captain(results.get(0).getAsJsonObject());
							
							if(n.get(SRC.Captain.isPlaying).equals("1"))
								caps = add(caps, n);
						}
						beh.setCaps(caps, dungeon);
						
					} catch (NoConnectionException | NotAuthorizedException e) {
						Debug.printException(pn+": Run -> constr.: err=unable to retrieve caps", e, Debug.runerr, Debug.error, true);
						return;
					}
				}
			}
		});
		iniRews();
	}
	
	public BackEndHandler getBackEndHandler() {
		return beh;
	}
	
	public void switchRunning(int slot) {
		setRunning(!isRunning(slot), slot);
	}
	
	public void setRunning(boolean b, int slot) {
		if(isRunning(slot) == b)
			return;
		if(b) {
			feh.onStart(cid, slot);
		} else {
			feh.onStop(cid, slot);
		}
		isRunning[slot] = b;
		if(b) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					slotSequence(slot);
				}
			});
			t.start();
		}
	}
	
	public boolean isRunning(int slot) {
		return isRunning[slot];
	}

	public void skip(int slot) {
		sleep[slot] = 0;
	}
	
	public void change(int slot) {
		change[slot] = !change[slot];
		try {
			updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Debug.printException(pn+": Run -> change: slot="+slot+", err=failed to update Frame", e, Debug.runerr, Debug.error, true);
		}
	}
	
	public void fav(int slot, int val) {
		String cap = cnames[slot+4];
		if(cap == null)
			return;
		
		Integer v = ConfigsV2.getCapInt(cid, "(all)", cap, cnames[slot+8].equals("d") ? ConfigsV2.dungeon : ConfigsV2.campaign, ConfigsV2.fav);
		if(v == null 
				|| v == Integer.MAX_VALUE-1 
				|| v == Integer.MIN_VALUE+1
				|| Math.signum(val)*Math.signum(v) <= 0)
			ConfigsV2.favCap(cid, "(all)", cap, ConfigsV2.all, val);
		else 
			ConfigsV2.favCap(cid, "(all)", cap, ConfigsV2.all, null);
	}
	
	private String pn = null;
	
	synchronized private void slotSequence(int slot) {
		pn = ConfigsV2.getPStr(cid, ConfigsV2.name);
		try {
			if_clause:
			if(slot == 4) {
				Debug.print(pn+" ["+slot+"]: event", Debug.general, Debug.info);
				event();

				Debug.print(pn+" ["+slot+"]: quest", Debug.general, Debug.info);
				quest();

				Debug.print(pn+" ["+slot+"]: store", Debug.general, Debug.info);
				store();

				Debug.print(pn+" ["+slot+"]: unlock", Debug.general, Debug.info);
				unlock();

				Debug.print(pn+" ["+slot+"]: upgrade", Debug.general, Debug.info);
				upgrade();
				
				Debug.print(pn+" ["+slot+"]: grantTeamReward", Debug.general, Debug.info);
				beh.grantTeamReward();
				
			} else {
				
				if(!canUseSlot(slot))
					break if_clause;

				Debug.print(pn+" ["+slot+"]: chest", Debug.general, Debug.info);
				chest(slot);

				Debug.print(pn+" ["+slot+"]: captain", Debug.general, Debug.info);
				captain(slot);

				Debug.print(pn+" ["+slot+"]: place", Debug.general, Debug.info);
				place(slot);
				
			}
			

			Debug.print(pn+" ["+slot+"]: updateFrame", Debug.general, Debug.info);
			updateFrame();
		} catch (NoConnectionException | NotAuthorizedException e) {
			Debug.printException(pn+": Run -> slotSequence: slot=" + slot + " err=No stable Internet Connection", e, Debug.runerr, Debug.fatal, true);
		} catch (StreamRaidersException | NoCapMatchesException e) {
		} catch (Exception e) {
			Debug.printException(pn+": Run -> slotSequence: slot=" + slot + " err=unknown", e, Debug.runerr, Debug.fatal, true);
		}
		
		
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);
		
		
		// --- calculate time to sleep ---
		
		JsonObject ptimes = ConfigsV2.getPObj(cid, ConfigsV2.ptimes);
		int e = Integer.parseInt(currentLayerId.split("-")[1]);
		
		int min = ConfigsV2.getSleep(cid, currentLayer, ""+slot, ConfigsV2.min);
		int max = ConfigsV2.getSleep(cid, currentLayer, ""+slot, ConfigsV2.max);
		
		int w = -1;
		
		
		// test if sleep is not possible before next layer
		if_clause:
		if(min < 0 || max < 0 || n+(min/300) > e) {
			// change layer
			//	loop multiple times to be sure that it finds the next layer
			for(int i=0; i<ptimes.size(); i++) {
				// loop until first layer after current which is not disabled
				for(String t : ptimes.keySet()) {
					int s = Integer.parseInt(t.split("-")[0]);
					if(s != (e == 2015 ? 0 : e+1))
						continue;
					
					if(ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.min) < 0 ||
							ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.max) < 0) {
						e = Integer.parseInt(t.split("-")[1]);
						continue;
					}

					// shift start if before now
					if(s < n)
						s += 2015;
					
					// calculate time until next layer which is not disabled
					w = (s-n)*300;
					break if_clause;
				}
			}
		} else {
			// test if max is still in same layer or else set max to end time of layer
			if(n+(max/300) >= e)
				max = (e-n)*300;
			// generate random sleep-time
			w = Maths.ranInt(min, max);
		}
		
		if(w > -1) {
			Debug.print(pn+" ["+slot+"]: sleeping "+w+" sec", Debug.general, Debug.info);
			sleep(w, slot);
		} else
			Debug.print("Run -> slotSequence: err=couldnt find wait time", Debug.runerr, Debug.fatal, true);
		
		System.gc();
	}
	
	public boolean canUseSlot(int slot) throws NoConnectionException, NotAuthorizedException {
		int uCount = beh.getUnits(SRC.Manager.all, false).length;
		return (slot == 0)
				|| (slot == 1 && uCount > 4)
				|| (slot == 2 && uCount > 7)
				|| (slot == 3 && beh.hasBattlePass());
	}
	
	
	private void sleep(int sec, int slot) {
		sleep[slot] = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isRunning[slot])
					t.cancel();
				
				int min = sleep[slot] / 60;
				int sec = sleep[slot] % 60;
				
				String ms = "";
				
				if(min != 0) {
					ms += min+":";
					if(sec < 10)
						ms += "0";
				}
				
				ms += sec;
				
				feh.onTimerUpdate(cid, slot, ms);
				

				sleep[slot]--;
				
				if(sleep[slot] < 0) {
					t.cancel();
					slotSequence(slot);
				}
				
				
			}
		}, 0, 1000);
	}

	private HashSet<String> bannedPos = new HashSet<>();
	private boolean goMultiPlace;
	private LocalDateTime placeTime = LocalDateTime.now();
	
	private void place(int slot) throws NoConnectionException, NotAuthorizedException {
		
		//	Unit place delay
		long tdif = ChronoUnit.MILLIS.between(LocalDateTime.now(), placeTime);
		if(tdif > 0) {
			try {
				Thread.sleep(tdif);
			} catch (InterruptedException e) {}
		}
		
		Raid r = beh.getRaid(slot, true);
		if(r == null)
			return;

		String placeSer = r.get(SRC.Raid.placementsSerialized);
		if(!r.canPlaceUnit(beh.getServerTime())
			|| ConfigsV2.getInt(cid, currentLayer, ConfigsV2.maxUnitPerRaid) <= (placeSer == null 
																					? 0 
																					: placeSer.split(beh.getUserId()).length-1))
			return;
		

		boolean dungeon = r.isDungeon();
		
		
		Integer potionsc = beh.getCurrency(Store.potions, true);
		boolean epic = (potionsc == null ? false : (potionsc >= 45))
						&& (!(dungeon 
								? ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.dungeonEpicPlaceFavOnly)
								: ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.campaignEpicPlaceFavOnly))
							|| ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign)
									.contains(new JsonPrimitive(r.get(SRC.Raid.twitchDisplayName))));
		
		final Unit[] units = beh.getPlaceableUnits(slot);
		Debug.print(pn+" slot="+slot+" units="+Arrays.toString(units), Debug.units, Debug.info);
		
		Map map = beh.getMap(slot, true);
		
		HashSet<String> upts = map.getUsablePlanTypes();
		upts.remove("noPlacement");
		
		Debug.print(pn+": slot="+slot+", upts="+upts, Debug.units, Debug.info);
		
		if((ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.placeMarkerOnly) 
					&& upts.size() <= 0)
				|| (!ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.allowPlaceFirst) 
					&& (placeSer == null ? true : Json.parseArr(placeSer).size() <= 0))) 
			return;
		
		
		int[] mh = new Heatmap().getMaxHeat(map);
		
		int mdif = Json.parseObj(Options.get("mapDifficulty"))
				.getAsJsonObject(Json.parseObj(Options.get("mapNodes"))
						.getAsJsonObject(map.getNode())
						.get("NodeDifficulty")
						.getAsString())
				.get("Value").getAsInt();
		
		int re = 0;
		int retries = ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unitPlaceRetries);
		int reload = ConfigsV2.getInt(cid, currentLayer, ConfigsV2.mapReloadAfterXRetries);
		bannedPos = new HashSet<>();
		
		List<String> neededUnits = beh.getNeededUnitTypesForQuests();
		boolean preferRogues = ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.preferRoguesOnTreasureMaps) 
				&& r.getFromNode(SRC.MapNode.nodeType).contains("treasure");
		
		if(preferRogues) {
			neededUnits.add("rogue");
			neededUnits.add("flyingarcher");
		}
		
		Debug.print(pn+": slot="+slot+", neededUnits="+neededUnits, Debug.units, Debug.info);
		
		while(true) {
			Debug.print(pn+" ["+slot+"] place "+re, Debug.loop, Debug.info);
			
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiPlaceExploit)) {
				//	TODO multi place exploit
				goMultiPlace = false;
				for(int j=0; j<SRC.Run.exploitThreadCount; j++) {
					final Place pla = findPlace(map, mh, upts, neededUnits, units, epic, mdif, dungeon);
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
								beh.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
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
				final Place pla = findPlace(map, mh, upts, neededUnits, units, epic, mdif, dungeon);
				if(pla == null) {
					Debug.print(pn+": slot="+slot+", Place=null", Debug.units, Debug.info);
					break;
				}
				Debug.print(pn+": slot="+slot+", Place="+pla.toString(), Debug.units, Debug.info);
				String err = beh.placeUnit(slot, pla.unit, pla.epic, pla.pos, pla.isOnPlan);
				bannedPos.add(pla.pos[0]+"-"+pla.pos[1]);
				
				if(epic && err == null)
					beh.decreaseCurrency(Store.potions, 45);
				
				if(err == null) {
					beh.addCurrency(Store.potions, 1);
					beh.addCurrency(pla.unit.get(SRC.Unit.unitType), 1);
					placeTime = LocalDateTime.now().plus(Maths.ranInt(ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.minu),
																	ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.maxu)), 
														ChronoUnit.MILLIS);
					break;
				}
				
				if(err.equals("PERIOD_ENDED"))
					break;
				
				if(re++ < retries) {
					if(re % reload == 0) {
						map = beh.getMap(slot, true);
					}
					continue;
				}
				
				Debug.print(pn+": Run -> place: tdn="+r.toString()+" err="+err, Debug.lowerr, Debug.error, true);
				break;
			}
			
			
			
			
		}
		
	}
	
	
	private static class Place {
		public final Unit unit;
		public final int[] pos;
		public final boolean epic;
		public final boolean isOnPlan;
		public Place(Unit u, int[] pos, boolean epic, boolean isOnPlan) {
			this.unit = u;
			this.pos = pos;
			this.epic = epic;
			this.isOnPlan = isOnPlan;
		}
		@Override
		public String toString() {
			return unit.get(SRC.Unit.unitType)+"|"+pos[0]+"-"+pos[1]+(epic ? "|epic" : "")+(isOnPlan ? "|plan" : "");
		}
	}
	
	private static class Prios {
		private int[][] prio;
		private boolean[][] vibe;
		private Unit[] units;
		public Prios(Unit[] units) {
			this.units = units;
			prio = new int[units.length][4];
			for(int i=0; i<prio.length; i++)
				for(int j=0; j<prio[i].length; j++)
					prio[i][j] = -1;
			vibe = new boolean[units.length][4];
		}
		public void setPrio(int p, int epicPlan, boolean evibe, int normPlan, boolean nvibe, int epic, int norm) {
			prio[p][0] = epicPlan;
			vibe[p][0] = evibe;
			prio[p][1] = normPlan;
			vibe[p][1] = nvibe;
			prio[p][2] = epic;
			prio[p][3] = norm;
		}
		public int[] get(int m) {
			return get(m, false);
		}
		public int[] get(int m, boolean allowVibe) {
			int p = 0;
			for(int i=1; i<prio.length; i++)
				if((allowVibe
						|| !vibe[i][m])
					&& (prio[i][m] > prio[p][m]
						|| (prio[i][m] == prio[p][m]
							&& Integer.parseInt(units[i].get(SRC.Unit.level)) > Integer.parseInt(units[p].get(SRC.Unit.level)))))
					p = i;
			
			if(!allowVibe && prio[p][m] < 0)
				return get(m, true);
			
			int[] ret = {p, prio[p][m]};
			prio[p][m] = -1;
			return ret;
		}
		public boolean isVibe(int p, int m) {
			return vibe[p][m];
		}
	}
	
	private Place findPlace(Map map, int[] mh, HashSet<String> upts, List<String> neededUnits, Unit[] units, boolean epic, int mdif, boolean dungeon) {
		HashSet<String> epts = new HashSet<>();
		if(epic) {
			String[][] mpts = new String[map.width()][map.length()];
			for(int x=0; x<map.width(); x++) 
				for(int y=0; y<map.length(); y++) 
					if(map.is(x, y, SRC.Map.isEmpty))
						mpts[x][y] = map.getPlanType(x, y);
				
			for(int x=0; x<mpts.length-1; x++) {
				for(int y=0; y<mpts[x].length-1; y++) {
					String pt = mpts[x][y];
					if(pt == null)
						continue;
					if(pt.equals(mpts[x+1][y])
							&& pt.equals(mpts[x][y+1])
							&& pt.equals(mpts[x+1][y+1]))
						epts.add(pt);
				}
			}
		}
		
		Prios prio = new Prios(units);
		for(int i=0; i<units.length; i++) {
			final String utype = units[i].get(SRC.Unit.unitType);
			
			int p = ConfigsV2.getUnitInt(cid, currentLayer, utype, dungeon ? ConfigsV2.placedun : ConfigsV2.place);
			int e = ConfigsV2.getUnitInt(cid, currentLayer, utype, dungeon ? ConfigsV2.epicdun : ConfigsV2.epic);
			
			if(!dungeon) {
				int pdi = ConfigsV2.getUnitInt(cid, currentLayer, utype, ConfigsV2.difmin);
				int edi = ConfigsV2.getUnitInt(cid, currentLayer, utype, ConfigsV2.epicdifmin);
				int pda = ConfigsV2.getUnitInt(cid, currentLayer, utype, ConfigsV2.difmax);
				int eda = ConfigsV2.getUnitInt(cid, currentLayer, utype, ConfigsV2.epicdifmax);
				
				if(mdif < pdi || mdif > pda)
					p = -1;
				
				if(mdif < edi || mdif > eda)
					e = -1;
			}
			
			int pp = -1;
			int pe = -1;
			
			HashSet<String> pts = units[i].getPlanTypes();
			pts.remove("vibe");
			for(String pt : pts) {
				if(epts.contains(pt))
					pe = e;
				if(upts.contains(pt))
					pp = p;
			}
			
			
			boolean ve = false;
			
			if(pe == -1 && epts.contains("vibe")) {
				pe = e;
				ve = true;
			}
			
			boolean vp = false;
			
			if(pp == -1 && upts.contains("vibe")) {
				pp = p;
				vp = true;
			}
			
			if(neededUnits.contains(utype)) {
				pp = Integer.MAX_VALUE;
				p = Integer.MAX_VALUE;
			}
			
			
			prio.setPrio(i, pe, ve, pp, vp, e, p);
		}
		
		for(int i=0; i<4; i++) {
			if(!epic && i%2 == 0)
				continue;
			
			while(true) {
				int[] p = prio.get(i);
				if(p[1] < 0)
					break;
				
				HashSet<String> pts = null;
				if(i<2) {
					pts = units[p[0]].getPlanTypes();
					if(!prio.isVibe(p[0], i))
						pts.remove("vibe");
				}
				
				int[] pos = null;
				try {
					pos = new Pathfinding().search(new MapConv().asField(map, units[p[0]].canFly(), pts, mh, bannedPos), pn, i%2==0);
				} catch (NoFinException e) {}
				if(pos == null)
					continue;
				return new Place(units[p[0]], pos, i%2==0, i<2);
			}
			
			//	TODO place marker only Campaign/Dungeon
			
			if(i==1 && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.placeMarkerOnly))
				return null;
		}
		
		return null;
	}
	
	
	private void captain(int slot) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		captain(slot, true, false);
	}

	private void captain(int slot, boolean first, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		
		boolean dungeon = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.dungeonSlot).equals(""+slot);
		
		Raid r = beh.getRaid(slot, true);
		
		if(r != null && ConfigsV2.isSlotLocked(cid, currentLayer, ""+slot)) {
			if(r.isDungeon() && !dungeon) {
				Raid[] all = beh.getRaids(SRC.Manager.all);
				boolean change = true;
				for(int i=0; i<all.length; i++) {
					if(i == slot || all[i] == null)
						continue;
					if(all[i].isDungeon() && ConfigsV2.isSlotLocked(cid, currentLayer, ""+i))
						change = false;
				}
				if(change) {
					ConfigsV2.setStr(cid, currentLayer, ConfigsV2.dungeonSlot, ""+slot);
					dungeon = true;
				}
			} else if(!r.isDungeon() && dungeon) {
				ConfigsV2.setStr(cid, currentLayer, ConfigsV2.dungeonSlot, "(none)");
				dungeon = false;
			}
		}
		
		if(ConfigsV2.isSlotLocked(cid, currentLayer, ""+slot) && !change[slot])
			return;
		
		if(r == null) {
			switchCap(slot, dungeon, null, null, noCap, first, null);
			return;
		}
		
		if(!r.isSwitchable(beh.getServerTime(), ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capInactiveTreshold)))
			return;

		String tdn = r.get(SRC.Raid.twitchDisplayName);
		if(r.isVersus()) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}

		ListType list = dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign;
		
		Boolean ic = ConfigsV2.getCapBoo(cid, currentLayer, tdn, list, ConfigsV2.ic);
		ic = ic == null ? false : ic;
		Boolean il = ConfigsV2.getCapBoo(cid, currentLayer, tdn, list, ConfigsV2.il);
		il = il == null ? false : il;
		Integer fav = ConfigsV2.getCapInt(cid, currentLayer, tdn, list, ConfigsV2.fav);
		fav = fav == null ? 0 : fav;
		String ct = r.getFromNode(SRC.MapNode.chestType);

		if(ct == null) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		ct = Remaper.map(ct);

		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		Integer aloy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxc);
		if(aloy == null)
			new StreamRaidersException(pn+": Run -> captain: slot="+slot+", ct="+ct+", err=failed to get chest max loy");
		
		if(aloy < 0)
			aloy = Integer.MAX_VALUE;
		
		
		int maxTimeLeft = 30 - ConfigsV2.getInt(cid, currentLayer, ConfigsV2.maxTimeLeft);
		if(!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
									.plusMinutes(maxTimeLeft),
								beh.getServerTime())) {
			switchCap(slot, dungeon, r, tdn, noCap, first, maxTimeLeft);
			return;
		}
		
		int minRaidTimeLeft = ConfigsV2.getInt(cid, currentLayer, ConfigsV2.minTimeLeft);
		JsonArray users = Json.parseArr(r.get(SRC.Raid.users));
		if(users != null) {
			String uid = beh.getUserId();
			for(int i=0; i<users.size(); i++)
				if(users.get(i).getAsJsonObject().get("userId").getAsString().equals(uid))
					minRaidTimeLeft = Integer.MIN_VALUE;
		}
		
		if((dungeon ^ r.isDungeon())
			|| r.isOffline(beh.getServerTime(), il, ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capInactiveTreshold))
			|| (!ic && Time.isAfter(beh.getServerTime(),
							Time.parse(r.get(SRC.Raid.creationDate))
								.plusMinutes(30 - minRaidTimeLeft)))
			|| (!ic && !dungeon 
					&& !ConfigsV2.getChestBoolean(cid, currentLayer, ct, ConfigsV2.enabled))
			|| (!ic && (loy < ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.minc) || loy > aloy))
			|| (ConfigsV2.getBoolean(cid, currentLayer, dungeon ? ConfigsV2.dungeonFavOnly : ConfigsV2.campaignFavOnly) 
					&& !ConfigsV2.getFavCaps(cid, currentLayer, list).contains(new JsonPrimitive(tdn)))
			|| fav < 0
			) {
			switchCap(slot, dungeon, r, tdn, noCap, first, null);
			return;
		}
		
		if(change[slot]) {
			if(first)
				switchCap(slot, dungeon, r, tdn, noCap, first, null);
			else
				change[slot] = false;
		}
		
	}
	
	private Hashtable<String, LocalDateTime> banned = new Hashtable<>();
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap, boolean first, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			switchCap(slot, dungeon, r, disname, noCap, overrideBanTime);
		} catch (NoCapMatchesException e) {
			if(!noCap) {
				beh.updateCaps(true, dungeon);
				captain(slot, first, true);
			} else {
				Debug.print(pn+": Run -> switchCap: slot="+slot+", err=No Captain Matches Config", Debug.runerr, Debug.error, true);
				throw e;
			}
		}
	}
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap, Integer overrideBanTime) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {

		LocalDateTime now = Time.parse(beh.getServerTime());
		
		if(!(r == null || disname == null)) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate));
			long plus;
			if(overrideBanTime != null)
				plus = overrideBanTime;
			else if(r.isVersus()) 
				plus = 7;
			else if(r.isDungeon())
				plus = 7;
			else
				plus = 35;
			if(now.isAfter(start.plusMinutes(plus-2)))
				banned.put(disname, now.plusMinutes(2));
			else
				banned.put(disname, start.plusMinutes(plus));
			Debug.print("blocked " + disname, Debug.caps, Debug.info);
		}
		
		
		HashSet<String> removed = new HashSet<>();
		for(String key : banned.keySet().toArray(new String[banned.size()])) {
			if(now.isAfter(banned.get(key))) {
				removed.add(key);
				banned.remove(key);
			}
		}
		Debug.print("unblocked " + removed.toString(), Debug.caps, Debug.info);

		Captain[] caps = beh.getCaps(dungeon);
		Debug.print("got caps " + Arrays.toString(caps), Debug.caps, Debug.info);
		
		ListType list = dungeon
				? ConfigsV2.dungeon 
				: ConfigsV2.campaign;

		Raid[] all = beh.getRaids(SRC.Manager.all);
		ArrayList<String> otherCaps = new ArrayList<>();
		for(Raid raid : all) {
			if(raid == null)
				continue;
			otherCaps.add(raid.get(SRC.Raid.twitchDisplayName));
		}
		
		
		Captain best = null;
		int val = ConfigsV2.getBoolean(cid, currentLayer, dungeon ? ConfigsV2.dungeonFavOnly : ConfigsV2.campaignFavOnly) ? 0 : -1;
		int loy = 0;
		
		HashSet<String> skipped = new HashSet<>();
		
		for(int i=0; i<caps.length; i++) {
			String tdn = caps[i].get(SRC.Captain.twitchDisplayName);
			Integer fav = ConfigsV2.getCapInt(cid, currentLayer, tdn, list, ConfigsV2.fav);
			fav = fav == null ? 0 : fav;
			if(fav < 0 || banned.containsKey(tdn) || otherCaps.contains(tdn)) {
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
		
		Debug.print("skipped " + skipped.toString(), Debug.caps, Debug.info);
		
		if(best == null) 
			throw new NoCapMatchesException();
		
		beh.switchRaid(best, slot);
		
		Debug.print("switched to " + best.get(SRC.Captain.twitchDisplayName), Debug.caps, Debug.info);
		
		captain(slot, false, noCap);
	}

	
	private boolean goMultiChestClaim;
	
	private void chest(int slot) throws NoConnectionException, NotAuthorizedException {
		if(!beh.isReward(slot))
			return;
		if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiChestExploit)) {
			//	TODO multi chest exploit
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
						try_catch: {
							try {
								JsonObject rews = beh.getChest(slot);
								if(rews == null)
									break try_catch;
								for(String rew : rews.keySet())
									addRew(SRC.Run.chests, rew, rews.get(rew).getAsInt());
							} catch (NoConnectionException | NotAuthorizedException e) {}
						}
					}
				});
				t.start();
			}
			goMultiChestClaim = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		} else {
			JsonObject rews = beh.getChest(slot);
			if(rews == null)
				return;
			for(String rew : rews.keySet())
				addRew(SRC.Run.chests, rew, rews.get(rew).getAsInt());
		}
		
	}

	private void upgrade() throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.upgradeMinGold))
			return;
		
		Unit[] us = beh.getUnits(SRC.Manager.isUnitUpgradeable, false);
		if(us.length == 0)
			return;
		
		int[] ps = new int[us.length];
		for(int i=0; i<us.length; i++) 
			ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, us[i].get(SRC.Unit.unitType), ConfigsV2.upgrade);
		
		while(true) {
			int ind = 0;
			for(int i=1; i<ps.length; i++)
				if(ps[i] > ps[ind]) 
					ind = i;
			
			if(ps[ind] < 0)
				break;
			
			String err = beh.upgradeUnit(us[ind], ConfigsV2.getUnitString(cid, currentLayer, us[ind].get(SRC.Unit.unitType), ConfigsV2.spec));
			if(err != null && (!(err.equals("no specUID") || err.equals("cant upgrade unit")))) {
				Debug.print("Run -> upgradeUnits: type=" + us[ind].get(SRC.Unit.unitType) + " err=" + err, Debug.lowerr, Debug.error, true);
				break;
			}
			
			ps[ind] = -1;
		}
	}

	private boolean goMultiUnit;
	
	private void unlock() throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unlockMinGold))
			return;
		
		Unit[] unlockable = beh.getUnits(SRC.Manager.isUnitUnlockable, true);
		if(unlockable.length == 0)
			return;
		
		int[] ps = new int[unlockable.length];
		for(int i=0; i<unlockable.length; i++)
			ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, unlockable[i].get(SRC.Unit.unitType), unlockable[i].isDupe() ? ConfigsV2.dupe : ConfigsV2.unlock);
		
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
			
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiUnitExploit)) {
				//	TODO multi unlock exploit
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
					Debug.print("Run -> unlock: type=" + unlockable[ind].get(SRC.Unit.unitType) + ", err=" + err, Debug.runerr, Debug.warn, true);
			}
			
			ps[ind] = -1;
		}
	}

	private void store() throws NoConnectionException, NotAuthorizedException {
		beh.updateStore(false);
		chests:
		if(beh.getCurrency(Store.keys, false) >= ConfigsV2.getInt(cid, currentLayer, ConfigsV2.storeMinKeys)) {
			String sel = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.canBuyChest);
			String chest;
			JsonObject bc;
			switch(sel) {
			case "dungeon":
				if(Time.isAfter(beh.getServerTime(), Options.get("dungeonchestdate")))
					bc = beh.buyChest("dungeonchest2");
				else
					bc = beh.buyChest("dungeonchest");
				chest = "dungeonchest";
				break;
			case "vampire":
				bc = beh.buyChest("vampirechest");
				chest = "vampirechest";
				break;
			default:
				break chests;
			}
			JsonElement err = bc.get(SRC.errorMessage);
			if(err == null || !err.isJsonPrimitive()) {
				addRew(SRC.Run.bought, chest, 1);
				JsonArray data = bc.getAsJsonArray("data");
				Raid.updateChestRews();
				for(int i=0; i<data.size(); i++) {
					Reward rew = new Reward(data.get(i).getAsString());
					addRew(SRC.Run.bought, rew.name, rew.quantity);
				}
			} else if(!(err.getAsString().equals("after end") || err.getAsString().startsWith("not enough ")))
				Debug.print("Run -> store -> buyChest: err="+err.getAsString()+", chest="+sel, Debug.runerr, Debug.error, true);
		}
		
		ec: {
			String sel = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.canBuyEventChest);
			if(sel.equals("(none)"))
				break ec;
			
			String chest = "polterheist"+sel+"chest";
			JsonObject bc = beh.buyChest(chest);
			JsonElement err = bc.get(SRC.errorMessage);
			if(err == null || !err.isJsonPrimitive()) {
				addRew(SRC.Run.bought, chest, 1);
				JsonArray data = bc.getAsJsonArray("data");
				Raid.updateChestRews();
				for(int i=0; i<data.size(); i++) {
					Reward rew = new Reward(data.get(i).getAsString());
					addRew(SRC.Run.bought, rew.name, rew.quantity);
				}
			} else if(!(err.getAsString().equals("after end") || err.getAsString().startsWith("not enough ")))
				Debug.print("Run -> store -> buyChest: err="+err.getAsString()+", chest="+sel, Debug.runerr, Debug.error, true);
		}
		
		if(beh.getCurrency(Store.gold, false) >= ConfigsV2.getInt(cid, currentLayer, ConfigsV2.scrollsMinGold)) {
			JsonArray items = beh.getStoreItems(SRC.Store.notPurchased);
			if(items.size() != 0) {
				int[] ps = new int[items.size()];
				String[] types = new String[items.size()];
				for(int i=0; i<items.size(); i++) {
					try {
						types[i] = items.get(i).getAsJsonObject()
								.getAsJsonPrimitive("itemId")
								.getAsString()
								.split("pack")[0]
								.replace("scroll", "")
								.replace("paladin", "alliespaladin");
					
						ps[i] = ConfigsV2.getUnitInt(cid, currentLayer, types[i], ConfigsV2.buy);
						
					} catch (NullPointerException e) {
						Debug.printException(": Run -> store: item=" + items.get(i).getAsJsonObject().getAsJsonPrimitive("itemId").getAsString() + ", err=item is not correct", e, Debug.runerr, Debug.error, true);
						ps[i] = -1;
					}
				}
				
				JsonObject packs = Json.parseObj(Options.get("store"));
				
				while(true) {
					int ind = 0;
					for(int i=1; i<ps.length; i++)
						if(ps[i] > ps[ind]) 
							ind = i;
					
					if(ps[ind] < 0)
						break;
					
					int amount = packs.get(items.get(ind).getAsJsonObject().get("itemId").getAsString())
							.getAsJsonObject().get("Quantity").getAsInt();
					
					String err = beh.buyItem(items.get(ind).getAsJsonObject());
					if(err == null)
						addRew(SRC.Run.bought, "scroll"+types[ind].replace("allies", ""), amount);
					else if(!err.equals("not enough gold"))
						Debug.print("Run -> store: item=" + items.get(ind) + ", err=" + err, Debug.lowerr, Debug.error, true);
					
					ps[ind] = -1;
				}
			}
			
			Integer gold = beh.getCurrency(Store.gold, false);
			if(gold != null) {
				int src = beh.getStoreRefreshCount();
				int min = ConfigsV2.getStoreRefreshInt(cid, currentLayer, src > 3 ? 3 : src);
				if(min > -1 && min < gold) {
					String err = beh.refreshStore();
					if(err != null)
						Debug.print("Run -> Store: err="+err, Debug.runerr, Debug.error, true);
					store();
				}
			}
			
		}
	}

	private boolean goMultiQuestClaim;
	
	private void quest() throws NoConnectionException, NotAuthorizedException {
		Quest[] quests = beh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiQuestExploit)) {
				//	TODO multi quest exploit
				goMultiQuestClaim = false;
				final Quest picked = quests[i];
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
				String err = beh.claimQuest(quests[i]);
				if(err != null)
					Debug.print(pn + ": Run -> claimQuests: err=" + err, Debug.runerr, Debug.error, true);
			}
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	private void event() throws NoConnectionException, NotAuthorizedException {
		if(!beh.isEvent())
			return;
		
		boolean bp = beh.hasBattlePass();
		int tier = beh.getEventTier();
		for(int i=1; i<=tier; i++) {
			if(bp)
				collectEvent(i, true);
			
			if(potionsTiers.contains(i) && beh.getCurrency(Store.potions, false) > 10)
				continue;
			
			collectEvent(i, false);
		}
	}
	
	private boolean goMultiEventClaim;
	
	private void collectEvent(int tier, boolean bp) throws NoConnectionException, NotAuthorizedException {
		if(!beh.canCollectEvent(tier, bp))
			return;
		
		if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiEventExploit)) {
			//	TODO multi event exploit
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
									addRew(SRC.Run.event, rew, ce.get("quantity").getAsInt());
							}
						} catch (NoConnectionException e) {
						} catch (NullPointerException e) {
							Debug.print(pn + ": Run -> event -> collectEvent(exploit): tier="+tier+", bp="+bp, Debug.runerr, Debug.error, true);
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
				Debug.print(pn + ": Run -> event -> collectEvent: tier="+tier+", bp="+bp+", err=" + err.getAsString(), Debug.runerr, Debug.error, true);
			} else {
				String rew = ce.get("reward").getAsString();
				if(!rew.equals("badges"))
					addRew(SRC.Run.event, rew, ce.get("quantity").getAsInt());
			}
		}
		
		
		
	}

	public String getCurrentLayer() {
		return currentLayer;
	}
	
	private static final C[] sc = new C[] {Store.gold, Store.potions, Store.meat, Store.candy, Store.keys, Store.bones};
	public static final String[] pveloy = "noloy bronze silver gold".split(" ");
	
	synchronized public void updateFrame() throws NoConnectionException, NotAuthorizedException {
		if(!ready)
			return;
		
		updateLayer();
		
		Raid[] raids = beh.getRaids(SRC.Manager.all);
		
		for(int i=0; i<raids.length; i++) {
			
			if(raids[i] == null) {
				cnames[i] = null;
				cnames[i+4] = null;
				cnames[i+8] = null;
			} else {
				cnames[i] = raids[i].get(SRC.Raid.twitchUserName);
				cnames[i+4] = raids[i].get(SRC.Raid.twitchDisplayName);
				cnames[i+8] = raids[i].isDungeon() ? "d" : "c";
			}

			feh.onUpdateSlot(cid, i, raids[i], ConfigsV2.isSlotLocked(cid, currentLayer, ""+i), change[i]);
			
		}
		
		for(C key : sc)
			feh.onUpdateCurrency(key.get(), beh.getCurrency(key, false));
		
	}
	
	public synchronized void updateLayer() {
		LocalDateTime now = LocalDateTime.now();
		// current time in layer-units (1 = 5 min)
		int n = ((now.get(WeekFields.ISO.dayOfWeek()) - 1) * 288) 
				+ (now.getHour() * 12) 
				+ (now.getMinute() / 5);

		// set current layer
		JsonObject ptimes = ConfigsV2.getPObj(cid, ConfigsV2.ptimes);
		for(String key : ptimes.keySet()) {
			String[] sp = key.split("-");
			if(Integer.parseInt(sp[0]) <= n && Integer.parseInt(sp[1]) >= n) {
				if(key.equals(currentLayerId))
					break;
				currentLayer = ptimes.get(key).getAsString();
				currentLayerId = key;
				updateSettings();
				break;
			}
		}
		
		feh.onUpdateLayer(ConfigsV2.getStr(cid, currentLayer, ConfigsV2.lname), new Color(ConfigsV2.getInt(cid, currentLayer, ConfigsV2.color)));
	}
	
	
	private String[] cnames = new String[12];
	
	public void openBrowser(int slot) {
		if(cnames[slot] == null)
			return;
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("https://twitch.tv/"+cnames[slot]));
			} catch (IOException | URISyntaxException e) {
				Debug.printException("Run -> openBrowser: err=can't open DesktopBrowser", e, Debug.runerr, Debug.error, true);
			}
		} else {
			Debug.print("Run -> openBrowser: err=desktop not supported", Debug.runerr, Debug.error, true);
		}
	}
	
	public Map getMap(int slot) throws NoConnectionException, NotAuthorizedException {
		return beh.getMap(slot, false);
	}
	
	public void updateSettings() {
		String proxy = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyDomain);
		String user = ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyUser);
		beh.setOptions(proxy.equals("") ? null : proxy, 
				ConfigsV2.getInt(cid, currentLayer, ConfigsV2.proxyPort),
				user.equals("") ? null : user,
				ConfigsV2.getStr(cid, currentLayer, ConfigsV2.proxyPass),
				ConfigsV2.getStr(cid, currentLayer, ConfigsV2.userAgent));
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
