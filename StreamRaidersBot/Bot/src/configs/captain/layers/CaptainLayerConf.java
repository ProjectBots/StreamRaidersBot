package configs.captain.layers;

import configs.captain.layers.units.CaptainUnitConf;
import configs.shared.layers.SleepConf;
import include.DeepCopy;
import include.DeepCopyHashtable;
import srlib.units.UnitRarity;

public class CaptainLayerConf implements Cloneable {

	@Override
	public CaptainLayerConf clone() throws CloneNotSupportedException {
		return DeepCopy.copyAllFields(new CaptainLayerConf(), this);
	}
	
	public String name = "(default)";
	public int color = -4144960;
	
	//	TODO do not save/export default rarity
	public final DeepCopyHashtable<String, CaptainUnitConf> uconfs = new DeepCopyHashtable<>();
	public final DeepCopyHashtable<String, Integer> storePrios = new DeepCopyHashtable<>();
	public final DeepCopyHashtable<String, ChestConf> cconfs = new DeepCopyHashtable<>();
	
	
	public final SleepConf[] sconfs = {new SleepConf(), new SleepConf()};
	
	
	//	TODO add on load and not here
	{
		for(UnitRarity ur : UnitRarity.values()) {
			final int prio = (ur.rank+1)*10;
			
			CaptainUnitConf uc = new CaptainUnitConf();
			
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
	
	public final int[] storeRefresh = {5000, 10000, -1, -1};
	
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
	 */
	public final int[] updateTimes = {10, 1, 5, 30, 60, 15};
	
	
	public int unitPlaceRetries = 5;
	public int mapReloadAfterXRetries = 3;
	public int proxyPort = 0;
	
	public boolean storePriceAsDefaultPrio = false;
	public boolean preferRoguesOnTreasureMaps = false;
	public boolean proxyMandatory = true;
	
	public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/0.0";
	public String proxyDomain = "";
	public String proxyUser = "";
	public String proxyPass = "";
	
}
