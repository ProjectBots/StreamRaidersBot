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
	
	public void addRew(String con, String type, int amount) {
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
			// loop until first layer after current which is not disabled
			for(int i=0; i<ptimes.size(); i++) {
				for(String t : ptimes.keySet()) {
					int s = Integer.parseInt(t.split("-")[0]);
					if(s != (e == 2015 ? 0 : e+1))
						continue;
					
					if(ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.min) < 0 ||
							ConfigsV2.getSleep(cid, ptimes.get(t).getAsString(), ""+slot, ConfigsV2.max) < 0) {
						e = Integer.parseInt(t.split("-")[1]);
						continue;
					}

					// calculate time until next layer which is not disabled
					w = (s-n)*300;
					break if_clause;
				}
			}
		} else {
			// generate random sleep-time
			if(n+(max/300) >= e)
				max = (e-n)*300;
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
	private boolean goMultiPlace = false;
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
		epic = (potionsc == null ? false : (potionsc >= 45 ? true : false))
						&& (!(dungeon 
								? ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.dungeonEpicPlaceFavOnly)
								: ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.campaignEpicPlaceFavOnly))
							|| ConfigsV2.getFavCaps(cid, currentLayer, dungeon ? ConfigsV2.dungeon : ConfigsV2.campaign)
									.contains(new JsonPrimitive(r.get(SRC.Raid.twitchDisplayName))));
		
		Unit[] units = beh.getPlaceableUnits(slot);
		Debug.print(pn+" slot="+slot+" units="+Arrays.toString(units), Debug.units, Debug.info);
		
		Map map = beh.getMap(slot, true);
		JsonArray ppts = map.getUsablePlanTypes();
		ppts.remove(new JsonPrimitive("noPlacement"));
		
		
		if((ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.placeMarkerOnly) 
					&& ppts.size() <= 0)
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
		int i = 0;
		Debug.print(pn+" ppts="+ppts.toString(), Debug.units, Debug.info);
		bannedPos = new HashSet<>();
		while(true) {
			Debug.print(pn+" ["+slot+"] place "+i++, Debug.loop, Debug.info);
			
			Unit u = findUnit(units, ppts, dungeon, mdif);
			Debug.print("choosed " + (u == null ? "null" : u.toString()), Debug.units, Debug.info);
			Debug.print(pn+" isVibing="+isVibing+" isOnPlan="+isOnPlan, Debug.units, Debug.info);
			
			if(u == null)
				break;
			
			JsonArray pts = isOnPlan ? u.getPlanTypes() : null;
			if(!isVibing && isOnPlan)
				pts.remove(new JsonPrimitive("vibe"));

			String err;
			if(Options.is("exploits") && ConfigsV2.getBoolean(cid, currentLayer, ConfigsV2.useMultiPlaceExploit)) {
				goMultiPlace = false;
				int[][] pos = new int[50][];
				int k=0;
				tc:
				try {
					for(; k<pos.length; k++) 
						pos[k] = new Pathfinding().search(new MapConv().asField(map, u.canFly(), pts, mh, bannedPos), ConfigsV2.getPStr(cid, ConfigsV2.name), epic);
				} catch (NoFinException e) {
					if(k > 25)
						break tc;
					if(!isOnPlan) {
						Debug.print("Run -> place: err=no fin", Debug.lowerr, Debug.error, true);
						break;
					}
					continue;
				}
				
				for(k=0; k<pos.length; k++) {
					if(pos[k] == null)
						break;
					final int kk = k;
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							while(!goMultiPlace) {
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {}
							}
							try {
								beh.placeUnit(slot, u, epic, pos[kk], isOnPlan);
							} catch (NoConnectionException e) {}
						}
					});
					t.start();
					bannedPos.add(pos[0]+"-"+pos[1]);
				}
				goMultiPlace = true;
				err = null;
			} else {
				int[] pos = null;
				try {
					pos = new Pathfinding().search(new MapConv().asField(map, u.canFly(), pts, mh, bannedPos), ConfigsV2.getPStr(cid, ConfigsV2.name), epic);
				} catch (NoFinException e) {
					if(!isOnPlan) {
						Debug.print(pn+": Run -> place: slot="+slot+", err=no fin", Debug.lowerr, Debug.error, true);
						break;
					}
					continue;
				}
				
				if(pos == null)
					err = "pos is null";
				else {
					err = beh.placeUnit(slot, u, epic, pos, isOnPlan);
					bannedPos.add(pos[0]+"-"+pos[1]);
				}
			}
			
			if(epic && err == null)
				beh.decreaseCurrency(Store.potions, 45);
			
			if(err == null) {
				beh.addCurrency(Store.potions, 1);
				beh.addCurrency(u.get(SRC.Unit.unitType), 1);
				int ran = Maths.ranInt(ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.minu),
						ConfigsV2.getUnitPlaceDelayInt(cid, currentLayer, ConfigsV2.maxu));
				placeTime = LocalDateTime.now().plus(ran, ChronoUnit.MILLIS);
				break;
			}
			
			if(err.equals("PERIOD_ENDED"))
				break;
			
			if(re++ < retries) {
				if(re % reload == 0) {
					map = beh.getMap(slot, true);
					ppts = map.getUsablePlanTypes();
					ppts.remove(new JsonPrimitive("noPlacement"));
				}
				continue;
			}
			
			Debug.print("Run -> place: tdn="+r.toString()+" err="+err, Debug.lowerr, Debug.error, true);
			break;
			
		}
		
	}
	
	
	private boolean isVibing = false;
	private boolean isOnPlan = false;
	private boolean epic = false;
	
	private Unit findUnit(Unit[] units, JsonArray ppts, boolean dungeon, int mdif) {
		
		isVibing = ppts.size() == 1 && ppts.get(0).getAsString().equals("vibe");
		isOnPlan = ppts.size() != 0;
		
		int[] prio = new int[units.length];
		
		List<String> neededUnits = beh.getNeededUnitTypesForQuests();
		
		
		for(int i=0; i<units.length; i++) {
			final String utype = units[i].get(SRC.Unit.unitType);
			prio[i] = -1;
			if(!dungeon) {
				int dmin = ConfigsV2.getUnitInt(cid, currentLayer, utype, epic ? ConfigsV2.epicdifmin : ConfigsV2.difmin);
				int dmax = ConfigsV2.getUnitInt(cid, currentLayer, utype, epic ? ConfigsV2.epicdifmax : ConfigsV2.difmax);
				if(mdif > dmax || mdif < dmin)
					continue;
			}
			if(isOnPlan) {
				JsonArray pts = units[i].getPlanTypes().deepCopy();
				if(!isVibing)
					pts.remove(new JsonPrimitive("vibe"));
				for(int j=0; j<pts.size(); j++) {
					JsonElement pt = pts.get(j);
					if(ppts.contains(pt)) {
						prio[i] = neededUnits.contains(units[i].get(SRC.Unit.unitType)) 
								? Integer.MAX_VALUE
								: ConfigsV2.getUnitInt(cid, currentLayer, utype,
									epic && dungeon
									? ConfigsV2.epicdun
									: epic
										? ConfigsV2.epic
										: dungeon
											? ConfigsV2.placedun
											: ConfigsV2.place
										);
						break;
					}
				
				}
			} else {
				prio[i] = neededUnits.contains(units[i].get(SRC.Unit.unitType)) 
						? Integer.MAX_VALUE
						: ConfigsV2.getUnitInt(cid, currentLayer, utype,
							epic && dungeon
							? ConfigsV2.epicdun
							: epic
								? ConfigsV2.epic
								: dungeon
									? ConfigsV2.placedun
									: ConfigsV2.place
								);
			}
			
			
		}
		
		int h = 0;
		for(int i=0; i<prio.length; i++)
			if(prio[h] < prio[i]
					|| (prio[h] == prio[i] 
						&& Integer.parseInt(units[h].get(SRC.Unit.level)) < Integer.parseInt(units[i].get(SRC.Unit.level))))
				h = i;
		
		if(epic && prio[h] < 0) {
			epic = false;
			return findUnit(units, ppts, dungeon, mdif);
		}
		
		if(prio[h] < 0)
			if(ppts.size() == 0) {
				System.out.println("prio < 0");
				return null;
			}
			else
				return findUnit(units, new JsonArray(), dungeon, mdif);
		
		if(isOnPlan) {
			JsonArray pts = units[h].getPlanTypes();
			if(!isVibing)
				pts.remove(new JsonPrimitive("vibe"));
			for(int i=0; i<pts.size(); i++)
				ppts.remove(pts.get(i));
			Debug.print(pn+" ppts="+ppts.toString()+", pts=" + pts.toString(), Debug.units, Debug.info);
		}
		
		return units[h];
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
			switchCap(slot, dungeon, null, null, noCap, first);
			return;
		}
		
		if(!r.isSwitchable(beh.getServerTime(), ConfigsV2.getInt(cid, currentLayer, ConfigsV2.capInactiveTreshold)))
			return;

		String tdn = r.get(SRC.Raid.twitchDisplayName);
		if(r.isVersus()) {
			switchCap(slot, dungeon, r, tdn, noCap, first);
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
			switchCap(slot, dungeon, r, tdn, noCap, first);
			return;
		}
		
		ct = Remaper.map(ct);

		int loy = dungeon ? (r.get(SRC.Raid.allyBoons)+",").split(",").length : Integer.parseInt(r.get(SRC.Raid.pveWins));
		
		Integer aloy = ConfigsV2.getChestInt(cid, currentLayer, ct, ConfigsV2.maxc);
		if(aloy == null)
			new StreamRaidersException(pn+": Run -> captain: slot="+slot+", ct="+ct+", err=failed to get chest max loy");
		
		if(aloy < 0)
			aloy = Integer.MAX_VALUE;
		
		
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
			|| (!ic && Time.isAfter(Time.parse(r.get(SRC.Raid.creationDate))
								.plusMinutes(30 - ConfigsV2.getInt(cid, currentLayer, ConfigsV2.maxTimeLeft)),
							beh.getServerTime()))
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
			switchCap(slot, dungeon, r, tdn, noCap, first);
			return;
		}
		
		if(change[slot]) {
			if(first)
				switchCap(slot, dungeon, r, tdn, noCap, first);
			else
				change[slot] = false;
		}
		
	}
	
	private Hashtable<String, LocalDateTime> banned = new Hashtable<>();
	
	public static class NoCapMatchesException extends Exception {
		private static final long serialVersionUID = 6502943388417577268L;
	}
	
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap, boolean first) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {
		try {
			switchCap(slot, dungeon, r, disname, noCap);
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
	
	private void switchCap(int slot, boolean dungeon, Raid r, String disname, boolean noCap) throws NoConnectionException, NotAuthorizedException, NoCapMatchesException {

		LocalDateTime now = Time.parse(beh.getServerTime());
		
		if(!(r == null || disname == null)) {
			LocalDateTime start = Time.parse(r.get(SRC.Raid.creationDate));
			int plus;
			if(r.isVersus()) 
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

	//	TODO multi chest exploit???
	private void chest(int slot) throws NoConnectionException, NotAuthorizedException {
		JsonObject rews = beh.getChest(slot);
		if(rews == null)
			return;
		for(String rew : rews.keySet())
			addRew(SRC.Run.chests, rew, rews.get(rew).getAsInt());
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

	
	//	TODO multi unlock exploit???
	private void unlock() throws NoConnectionException, NotAuthorizedException {
		
		if(beh.getCurrency(Store.gold, false) < ConfigsV2.getInt(cid, currentLayer, ConfigsV2.unlockMinGold))
			return;
		
		Unit[] unlockable = beh.getUnits(SRC.Manager.isUnitUnlockable, false);
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
			
			String err = beh.unlockUnit(unlockable[ind]);
			if(err != null && !err.equals("not enough gold")) 
				Debug.print("Run -> unlock: type=" + unlockable[ind].get(SRC.Unit.unitType) + ", err=" + err, Debug.runerr, Debug.warn, true);
			
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

	//	TODO multi quest exploit
	private void quest() throws NoConnectionException, NotAuthorizedException {
		Quest[] quests = beh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			String err = beh.claimQuest(quests[i]);
			if(err != null)
				Debug.print(pn + ": Run -> claimQuests: err=" + err, Debug.runerr, Debug.error, true);
		}
	}

	private static final HashSet<Integer> potionsTiers = new HashSet<>(Arrays.asList(5, 11, 14, 22, 29));
	
	//	TODO multi event exploit
	private void event() throws NoConnectionException, NotAuthorizedException {
		if(!beh.isEvent())
			return;
		
		boolean bp = beh.hasBattlePass();
		int tier = beh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(bp) {
				JsonObject ce = beh.collectEvent(i, true, pn);
				JsonElement err = ce.get(SRC.errorMessage);
				if(err != null && err.isJsonPrimitive() && !err.getAsString().equals("cant collect")) 
					Debug.print(pn + ": Run -> event -> pass: err=" + err.getAsString(), Debug.runerr, Debug.error, true);
				
			}
			
			
			if(potionsTiers.contains(i) && beh.getCurrency(Store.potions, false) > 10)
				continue;
			
			JsonObject ce = beh.collectEvent(i, false, pn);
			JsonElement err = ce.get(SRC.errorMessage);
			if(err != null && err.isJsonPrimitive()) {
				if(!err.getAsString().equals("cant collect"))
					Debug.print(pn + ": Run -> event -> basic: err=" + err.getAsString(), Debug.runerr, Debug.error, true);
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
				updateSettings();
				currentLayer = ptimes.get(key).getAsString();
				currentLayerId = key;
				break;
			}
		}
		
		feh.onUpdateLayer(ConfigsV2.getStr(cid, currentLayer, ConfigsV2.lname), new Color(ConfigsV2.getInt(cid, currentLayer, ConfigsV2.color)));
		
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
