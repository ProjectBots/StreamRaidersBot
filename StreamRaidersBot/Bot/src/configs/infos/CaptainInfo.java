package configs.infos;

import java.util.Hashtable;

import configs.viewer.layers.CaptainConf;
import srlib.RaidType;
import srlib.viewer.CaptainData;

public class CaptainInfo {
	
	/**
	 * captainId: captainInfo
	 */
	public static final Hashtable<Integer, CaptainInfo> cinfos = new Hashtable<>();
	
	/**
	 * puts a new CaptainInfo in {@link #cinfos} if not already present
	 * @param cap captain
	 * @return the captain info for this captain
	 */
	public static CaptainInfo add(CaptainData cap) {
		if(cinfos.containsKey(cap.captainId))
			return cinfos.get(cap.captainId);
		//	constructor puts the new CaptainInfo in cinfos
		return new CaptainInfo(cap.captainId, cap.twitchDisplayName, cap.twitchUserName);
	}
	
	/**
	 * adds the the unit to {@link #unitsPlaced}
	 * @param cap captain
	 * @param unitId the id of the unit
	 */
	public static void addUnit(CaptainData cap, int unitId) {
		Hashtable<Integer, Integer> up = add(cap).unitsPlaced;
		up.put(unitId, up.containsKey(unitId) ? up.get(unitId)+1 : 1);
	}
	
	/**
	 * adds the cid and chest to {@link #chestsOpened}
	 * @param cap
	 * @param cid
	 * @param chest
	 */
	public static void addChest(CaptainData cap, String cid, String chest) {
		cid += "#"+chest;
		Hashtable<String, Integer> co = add(cap).chestsOpened;
		co.put(cid, co.containsKey(cid) ? co.get(cid)+1 : 1);
	}
	
	/**
	 * updates wins/losses/leaves/times accordingly<br>
	 * @param cap captain
	 * @param rt type of the raid
	 * @param wll -1=loss, 0=leave, 1=win
	 * @param time raid start until chest ready
	 * @throws IllegalArgumentException if wll outside of [-1;1]
	 */
	public static void addRaid(CaptainData cap, RaidType rt, short wll, long time) {
		CaptainInfo ci = add(cap);
		
		switch (wll) {
		case -1:
			ci.defeats[rt.typeInt]++;
			break;
		case 0:
			ci.leaves[rt.typeInt]++;
			//	return to skip time add
			return;
		case 1:
			ci.wins[rt.typeInt]++;
			break;
		default:
			throw new IllegalArgumentException("only -1, 0 and 1 are allowed for wll, wll="+wll);
		}
		
		ci.times[rt.typeInt] += time;
	}
	
	public String tdn, tun;
	
	/**
	 * win/defeat/leave count<br>
	 * {@link RaidType#typeInt} is the index for the specified RaidType
	 */
	public int[] wins, defeats, leaves;
	/** 
	 * time, in seconds, that the captain needed for the raids the bot placed in<br>
	 * {@link RaidType#typeInt} is the index for the specified RaidType
	 */
	public int[] times;
	
	
	/** unitId: x times placed */
	public final Hashtable<Integer, Integer> unitsPlaced = new Hashtable<>();
	
	/**
	 * cid#chestType: x times opened<br>
	 * <br>
	 * ex.: 89306004-5ca0-494e-89e5-7ab0830393a3#chestboostedgold => 69
	 */
	public final Hashtable<String, Integer> chestsOpened = new Hashtable<>();
	
	public CaptainConf conf = null;
	
	/**
	 * puts the new CaptainInfo in {@link #cinfos}
	 * @param capId
	 * @param tdn
	 * @param tun
	 * @param wins
	 * @param defeats
	 * @param leaves
	 * @param times
	 */
	public CaptainInfo(int capId, String tdn, String tun, int[] wins, int[] defeats, int[] leaves, int[] times) {
		this.tdn = tdn;
		this.tun = tun;
		
		this.wins = wins;
		this.defeats = defeats;
		this.leaves = leaves;
		this.times = times;
		
		cinfos.put(capId, this);
	}
	
	/**
	 * puts the new CaptainInfo in {@link #cinfos}
	 * @param capId
	 * @param tdn
	 * @param tun
	 */
	private CaptainInfo(int capId, String tdn, String tun) {
		this.tdn = tdn;
		this.tun = tun;
		
		this.wins = new int[RaidType.highestTypeInt+1];
		this.defeats = new int[RaidType.highestTypeInt+1];
		this.leaves = new int[RaidType.highestTypeInt+1];
		this.times = new int[RaidType.highestTypeInt+1];

		cinfos.put(capId, this);
	}
	
}
