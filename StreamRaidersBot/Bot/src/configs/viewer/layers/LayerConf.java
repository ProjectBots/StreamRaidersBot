package configs.viewer.layers;

import java.util.Hashtable;

import configs.shared.layers.SleepConf;
import configs.viewer.layers.units.UnitConf;
import srlib.units.UnitRarity;

public class LayerConf {

	public String name = "(default)";
	public int color = -4144960;
	
	//	TODO do not save/export default rarity
	public final Hashtable<String, UnitConf> uconfs = new Hashtable<>();
	public final Hashtable<String, ChestConf> chconfs = new Hashtable<>();
	public final Hashtable<String, Integer> storePrios = new Hashtable<>();
	public final Hashtable<String, CaptainConf> caconfs = new Hashtable<>();
	
	//	TODO add on load and not here
	{
		for(UnitRarity ur : UnitRarity.values()) {
			final int prio = (ur.rank+1)*10;
			
			UnitConf uc = new UnitConf();
			
			if(ur != UnitRarity.LEGENDARY) {
				uc.conf.place = prio;
				uc.conf.placedun = prio;
				uc.conf.chests = "chestbronze,chestsilver,chestgold,chestboostedgold,chestboss,chestbosssuper,chestboostedtoken,chestboostedscroll,chestboostedskin,dungeonchest,bonechest";
			} else
				uc.conf.chests = "chestboostedgold,chestbosssuper,chestboostedskin,chestboostedscroll,dungeonchest,bonechest";
			
			uc.conf.upgrade = prio;
			uc.conf.unlock = prio;
			uc.conf.dupe = prio;
			uc.conf.buy = prio;
			uconfs.put(ur.toString(), uc);
		}
	}
	
	public final SleepConf[] sconfs = {new SleepConf(), new SleepConf(), new SleepConf(), new SleepConf(), new SleepConf()};
	
	public final int[] storeRefresh = {5000, 10000, -1, -1};
	public final boolean[] lockedSlots = {false, false, false, false};
	
	/**
	 * 0 storeMinGold<br>
	 * 1 storeMinKeys<br>
	 * 2 storeMinBones<br>
	 * 3 storeMinEventcurrency<br>
	 * 4 upgradeMinGold<br>
	 * 5 unlockMinGold<br>
	 */
	public final int[] minCur = {0, 0, 0, 0, 0, 0};
	
	
	
	/**
	 * 0 unitUpdate<br>
	 * 1 raidUpdate<br>
	 * 2 mapUpdate<br>
	 * 3 storeUpdate<br>
	 * 4 skinUpdate<br>
	 * 5 questEventRewardsUpdate<br>
	 * 6 capsUpdate<br>
	 * 7 soulsUpdate<br>
	 */
	public final int[] updateTimes = {10, 1, 5, 30, 60, 15, 10, 60};
	
	/**
	 * 0 min<br>
	 * 1 max<br>
	 */
	public final int[] unitPlaceDelay = {0, 0};
	
	public int dungeonSlot = -1;
	public int versusSlot = -1;
	public int unitPlaceRetries = 5;
	public int mapReloadAfterXRetries = 3;
	public int maxUnitPerRaid = 5;
	public int capInactiveTreshold = 10;
	public int proxyPort = 0;
	
	
	
	/**
	 * 0 useMultiPlaceExploit<br>
	 * 1 useMultiQuestExploit<br>
	 * 2 useMultiEventExploit<br>
	 * 3 useMultiChestExploit<br>
	 * 4 useMultiUnitExploit<br>
	 */
	public final boolean[] useExpoit = {false, false, false, false, false};
	
	public boolean storePriceAsDefaultPrio = false;
	public boolean preferRoguesOnTreasureMaps = false;
	public boolean allowPlaceFirst = true;
	public boolean useSkinFromCaptain = true;
	public boolean proxyMandatory = true;
	
	public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/0.0";
	public String proxyDomain = "";
	public String proxyUser = "";
	public String proxyPass = "";
}
