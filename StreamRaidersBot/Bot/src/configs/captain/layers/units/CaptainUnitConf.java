package configs.captain.layers.units;

import include.DeepCopy;

public class CaptainUnitConf implements Cloneable {

	@Override
	public CaptainUnitConf clone() {
		return DeepCopy.copyAllFields(new CaptainUnitConf(), this);
	}
	
	public String sync = "(none)";
	public String spec = "null";
	
	public SyncableUnitConf conf = new SyncableUnitConf();
	
}
