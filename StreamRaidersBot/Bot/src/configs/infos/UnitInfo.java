package configs.infos;

import java.util.ArrayList;
import java.util.Hashtable;

import configs.ProfileConfs;
import srlib.units.UnitType;

public class UnitInfo {
	
	/**
	 * unitId: unitInfo
	 */
	public static final Hashtable<Integer, UnitInfo> uinfos = new Hashtable<>();
	
	public ArrayList<UnitInfo> getForProfile(String pid) {
		ArrayList<UnitInfo> ret = new ArrayList<>(uinfos.size()/ProfileConfs.pconfs.size());
		for(UnitInfo ui : uinfos.values())
			if(ui.pid.equals(pid))
				ret.add(ui);
		
		return ret;
	}
	
	
	public int level = 1;
	public final UnitType type;
	public final String pid;
	public final boolean capunit;
	
	/**
	 * puts the new UnitInfo in {@link #uinfos}
	 * @param unitId
	 * @param type
	 * @param pid
	 * @param capunit
	 * @param level
	 */
	public UnitInfo(int unitId, UnitType type, String pid, boolean capunit, int level) {
		this.type = type;
		this.pid = pid;
		this.capunit = capunit;
		this.level = level;
		
		uinfos.put(unitId, this);
	}
}
