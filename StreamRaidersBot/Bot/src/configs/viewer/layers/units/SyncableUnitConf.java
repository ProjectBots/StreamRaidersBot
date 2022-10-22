package configs.viewer.layers.units;

import include.DeepCopy;

public class SyncableUnitConf implements Cloneable {

	@Override
	public SyncableUnitConf clone() {
		return DeepCopy.copyAllFields(new SyncableUnitConf(), this);
	}
	
	public int place = -1;
	public int epic = -1;
	public int placedun = -1;
	public int epicdun = -1;
	public int upgrade = -1;
	public int unlock = -1;
	public int dupe = -1;
	public int buy = -1;
	
	public String chests = "";
	public String favOnly = "";
	public String markerOnly = "";
	public String canVibe =  "nc,nd,ec,ed,v";
	public String dunEpicMode = "ifNeeded" ;
}
