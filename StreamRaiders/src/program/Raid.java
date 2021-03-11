package program;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.gson.JsonArray;
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
		if(get(SRC.Raid.hasViewedResults).contains("1")) {
			try {
				if(get(SRC.Raid.hasRecievedRewards).contains("1")) {
					if(get(SRC.Raid.isPlaying).contains("0") || (whenNotLive && get(SRC.Raid.isLive).contains("0"))) {
						return true;
					}
				} 
			} catch (Exception e) {}
		}
		if(treshold == -1) return false;
		try {
			Date st = SRC.date.parse(serverTime);
			
			Date date = SRC.date.parse(get(SRC.Raid.creationDate));
			
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(date);
			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 30 + treshold);
			
			if(st.after(c.getTime())) {
				return true;
			}
		} catch (Exception e) {}
		
		return false;
	}
	
	public boolean isReward() {
		try {
			if(!raid.getAsJsonPrimitive("postBattleComplete").toString().equals("\"1\"")) {
				return false;
			}
			
			if(!raid.getAsJsonPrimitive("hasRecievedRewards").getAsString().equals("0")) {
				return false;
			}
			
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	
	public JsonObject getChest(SRR req) {
		JsonObject ret = new JsonObject();
		
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
	}
	
	
	
	public boolean canPlaceUnit(String serverTime) {
		try {
			try {
				raid.getAsJsonPrimitive("endTime").getAsString();
				return false;
			} catch (Exception e) {}
			
			Date st = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(serverTime);
			
			try {
				String sdate = raid.getAsJsonPrimitive("lastUnitPlacedTime").getAsString();
				
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdate);
				
				Calendar c = GregorianCalendar.getInstance();
				c.setTime(date);
				c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 5);
				
				if(st.before(c.getTime())) {
					return false;
				}
			} catch (Exception e) {}
			
			Date crea = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(raid.getAsJsonPrimitive("creationDate").getAsString());
			
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(crea);
			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 30);
			
			if(st.after(c.getTime())) {
				return false;
			}
			
			try {
				raid.getAsJsonPrimitive("startTime").getAsString();
				return false;
			} catch (Exception e) {}
			
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	
	
	
}
