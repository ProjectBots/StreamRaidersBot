package program;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import include.JsonParser;

public class Raid {

	private JsonObject raid = null;
	
	public Raid(JsonObject raid) {
		this.raid = raid;
	}
	
	public String get(String con) {
		try {
			return raid.getAsJsonPrimitive(con).getAsString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean isOffline(String serverTime, boolean whenNotLive, int treshold) {
		String hvr = get(SRC.Raid.hasViewedResults);
		String hrr = get(SRC.Raid.hasRecievedRewards);
		String ip = get(SRC.Raid.isPlaying);
		String il = get(SRC.Raid.isLive);
		ifc:
		if(hvr != null && hrr != null && ip != null && il != null) {
			if(!hvr.contains("1")) break ifc;
			if(!hrr.contains("1")) break ifc;
			if(!(ip.contains("0") || (whenNotLive && il.contains("0")))) break ifc;
			return true;
		}
		if(treshold == -1) return false;
		try {
			Date st = SRC.dateParse(serverTime);
			
			Date date = SRC.dateParse(get(SRC.Raid.creationDate));
			
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(date);
			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 30 + treshold);
			
			if(st.after(c.getTime())) {
				return true;
			}
		} catch (ParseException e) {
			StreamRaiders.log("Raid -> isOffline: st=" + serverTime + ", cd=" + get(SRC.Raid.creationDate), e);
		}
		
		return false;
	}
	
	public boolean isReward() {
		JsonElement pbc = raid.get("postBattleComplete");
		JsonElement hrr = raid.get("hasRecievedRewards");
		if(!pbc.isJsonPrimitive() || !hrr.isJsonPrimitive()) return false;
		
		if(!pbc.getAsString().contains("1")) return false;
		if(!hrr.getAsString().contains("0")) return false;
		
		return true;
	}
	
	private static final String[][] typChestRews = new String[][] {
		{"gold", "goldAwarded"},
		{"token", "eventTokensReceived"},
		{"potion", "potionsAwarded"}
	};
	
	public static Hashtable<String, String> typViewChestRews = new Hashtable<>();
	
	public static void loadTypViewChestRews() {
		typViewChestRews.put("goldbag", "gold");
		typViewChestRews.put("eventtoken", "token");
		typViewChestRews.put("cooldown", "meat");
		typViewChestRews.put("potion", "potion");
		typViewChestRews.put("epicpotion", "potion");
	}
	
	public JsonObject getChest(SRR req) {
		JsonObject ret = new JsonObject();
		
		JsonObject rawData = JsonParser.json(req.getRaidStatsByUser(raid.getAsJsonPrimitive("raidId").getAsString())).getAsJsonObject("data");
		
		JsonElement chest = rawData.get("chestAwarded");
		if(chest.isJsonPrimitive()) ret.addProperty(chest.getAsString(), 1);
		
		for(int i=0; i<typChestRews.length; i++) {
			JsonElement typ = rawData.get(typChestRews[i][1]);
			if(typ != null) {
				int ityp = typ.getAsInt();
				if(ityp != 0) ret.addProperty(typChestRews[i][0], ityp);
			}
		}
		
		JsonElement bonus = rawData.get("bonusItemReceived");
		if(bonus != null && bonus.isJsonPrimitive()) {
			String ibonus = bonus.getAsString();
			if(ibonus.contains("goldbag")) {
				JsonElement je = ret.get("gold");
				if(je != null) {
					ret.addProperty("gold", je.getAsInt() + Integer.parseInt(ibonus.split("_")[1]));
				} else {
					ret.addProperty("gold", Integer.parseInt(ibonus.split("_")[1]));
				}
			} else if(ibonus.contains("scrolls")) {
				String[] s = ibonus.replace("|", "_").split("_");
				JsonElement je = ret.get(s[2]);
				if(je != null) {
					ret.addProperty(s[2], je.getAsInt() + Integer.parseInt(s[1]));
				} else {
					ret.addProperty(s[2], Integer.parseInt(s[1]));
				}
			} else if(!ibonus.equals("")) {
				StreamRaiders.log("Raid -> getChest: bonus=" + bonus, null);
			}
		}
		
		JsonElement rrews = rawData.get("viewerChestRewards");
		if(rrews != null && rrews.isJsonArray()) {
			JsonArray rews = rrews.getAsJsonArray();
			while(rews.size() > 0) {
				String[] rew = rews.get(0).getAsString().replace("|", "_").split("_");
				
				if(typViewChestRews.containsKey(rew[0])) {
					rew[1] = rew[1].replace("real", "").replace("true", "");
					String cont = typViewChestRews.get(rew[0]);
					JsonElement je = ret.get(cont);
					if(je != null) {
						ret.addProperty(cont, je.getAsInt() + Integer.parseInt(rew[1]));
					} else {
						ret.addProperty(cont, rew[1]);
					}
				} else {
					if(rew[0].contains("scrolls")) {
						JsonElement je = ret.get(rew[2]);
						if(je != null) {
							ret.addProperty(rew[2], je.getAsInt() + Integer.parseInt(rew[1]));
						} else {
							ret.addProperty(rew[2], Integer.parseInt(rew[1]));
						}
					} else if(rew[0].contains("skin")) {
						ret.addProperty(rew[0], 1);
					} else {
						StreamRaiders.log("Raid -> getChest: rew=" + String.join(", ", rew), null);
					}
				}
				rews.remove(0);
			}
		}
		return ret;
		
		/*
		try {
			JsonObject rawData = JsonParser.json(req.getRaidStatsByUser(raid.getAsJsonPrimitive("raidId").getAsString())).getAsJsonObject("data");
			
			try {
				String chest = rawData.getAsJsonPrimitive("chestAwarded").getAsString();
				ret.addProperty(chest, 1);
			} catch (Exception e) {}
			
			try {
				int gold = Integer.parseInt(rawData.getAsJsonPrimitive("goldAwarded").getAsString()) + Integer.parseInt(rawData.getAsJsonPrimitive("treasureChestGold").getAsString());
				if(gold != 0) {
					ret.addProperty("gold", gold);
				}
			} catch (Exception e) {}
			
			try {
				int token = Integer.parseInt(rawData.getAsJsonPrimitive("eventTokensReceived").getAsString());
				if(token != 0) {
					ret.addProperty("token", token);
				}
			}catch (Exception e) {}
			
			try {
				int potion = Integer.parseInt(rawData.getAsJsonPrimitive("potionsAwarded").getAsString());
				if(potion != 0) {
					ret.addProperty("potion", potion);
				}
			}catch (Exception e) {}
			
			try {
				String bonus = rawData.getAsJsonPrimitive("bonusItemReceived").getAsString();
				if(bonus.contains("goldbag")) {
					try {
						ret.addProperty("gold", ret.getAsJsonPrimitive("gold").getAsInt() + Integer.parseInt(bonus.split("_")[1]));
					} catch (Exception e) {
						ret.addProperty("gold", Integer.parseInt(bonus.split("_")[1]));
					}
				} else if(bonus.contains("scrolls")) {
					String[] s = bonus.replace("|", "_").split("_");
					try {
						ret.addProperty(s[2], ret.getAsJsonPrimitive(s[2]).getAsInt() + Integer.parseInt(s[1]));
					} catch (Exception e) {
						ret.addProperty(s[2], Integer.parseInt(s[1]));
					}
				} else {
					if(!bonus.equals("")) {
						System.err.println("Bonus " + bonus);
					}
				}
			}catch (Exception e) {}
			
			try {
				JsonArray rews = rawData.getAsJsonArray("viewerChestRewards");
				if(rews != null) {
					while(rews.size()>0) {
						String[] rew = rews.get(0).getAsString().replace("|", "_").split("_");
						
						if(rew[0].equals("goldbag")) {
							rew[1] = rew[1].replace("real", "");
							try {
								ret.addProperty("gold", ret.getAsJsonPrimitive("gold").getAsInt() + Integer.parseInt(rew[1]));
							} catch (Exception e) {
								ret.addProperty("gold", Integer.parseInt(rew[1]));
							}
						} else if(rew[0].equals("eventtoken")) {
							try {
								ret.addProperty("token", ret.getAsJsonPrimitive("token").getAsInt() + Integer.parseInt(rew[1]));
							} catch (Exception e) {
								ret.addProperty("token", Integer.parseInt(rew[1]));
							}
						} else if(rew[0].contains("scrolls")) {
							try {
								ret.addProperty(rew[2], ret.getAsJsonPrimitive(rew[2]).getAsInt() + Integer.parseInt(rew[1]));
							} catch (Exception e) {
								ret.addProperty(rew[2], Integer.parseInt(rew[1]));
							}
						} else if(rew[0].equals("cooldown")) {
							try {
								ret.addProperty("meat", ret.getAsJsonPrimitive("meat").getAsInt() + Integer.parseInt(rew[1]));
							} catch (Exception e) {
								ret.addProperty("meat", Integer.parseInt(rew[1]));
							}
						}else if(rew[0].contains("skin")) {
							ret.addProperty(rew[0], 1);
						} else if(rew[0].contains("potion")) {
							try {
								ret.addProperty("potion", ret.getAsJsonPrimitive("potion").getAsInt() + Integer.parseInt(rew[1]));
							} catch (Exception e) {
								ret.addProperty("potion", Integer.parseInt(rew[1]));
							}
						} else {
							System.err.println("Reward " + String.join(", ", rew));
						}
						rews.remove(0);
					}
				}
			}catch (Exception e) {}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
		*/
	}
	
	
	
	public boolean canPlaceUnit(String serverTime) {
		try {
			if(raid.get("endTime").isJsonPrimitive()) return false;
			if(raid.get("startTime").isJsonPrimitive()) return false;
			
			Date st = SRC.dateParse(serverTime);
			
			JsonElement jdate = raid.get("lastUnitPlacedTime");
			if(jdate.isJsonPrimitive()) {
				String sdate = jdate.getAsString();
				
				Date date = SRC.dateParse(sdate);
				
				Calendar c = GregorianCalendar.getInstance();
				c.setTime(date);
				c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 5);
				
				if(st.before(c.getTime())) return false;
			}
			/*
			try {
				String sdate = raid.getAsJsonPrimitive("lastUnitPlacedTime").getAsString();
				
				Date date = SRC.date.parse(sdate);
				
				Calendar c = GregorianCalendar.getInstance();
				c.setTime(date);
				c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 5);
				
				if(st.before(c.getTime())) {
					return false;
				}
			} catch (Exception e) {}
			*/
			Date crea = SRC.dateParse(raid.getAsJsonPrimitive("creationDate").getAsString());
			
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(crea);
			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 30);
			
			if(st.after(c.getTime())) return false;
			
			return true;
		} catch (ParseException e) {}
		return false;
	}
	

}
