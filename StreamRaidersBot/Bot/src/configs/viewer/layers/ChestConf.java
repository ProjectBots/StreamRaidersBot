package configs.viewer.layers;

import include.DeepCopy;

public class ChestConf implements Cloneable {

	@Override
	public ChestConf clone() {
		return DeepCopy.copyAllFields(new ChestConf(), this);
	}
	
	public int minLoy = 0;
	public int maxLoy = -1;
	public int minTime = 10;
	public int maxTime = 1800;
	
	public boolean enabled = false;
	
}
