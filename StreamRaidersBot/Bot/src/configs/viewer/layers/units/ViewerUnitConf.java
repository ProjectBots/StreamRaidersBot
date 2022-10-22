package configs.viewer.layers.units;

import include.DeepCopy;

public class ViewerUnitConf implements Cloneable {
	
	@Override
	public ViewerUnitConf clone() {
		return DeepCopy.copyAllFields(new ViewerUnitConf(), this);
	}

	public String sync = "(none)";
	public String spec = "null";
	
	public SyncableUnitConf conf = new SyncableUnitConf();
}
