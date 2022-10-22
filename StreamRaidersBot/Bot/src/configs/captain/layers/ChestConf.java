package configs.captain.layers;

import include.DeepCopy;

public class ChestConf implements Cloneable {

	@Override
	public ChestConf clone() {
		return DeepCopy.copyAllFields(new ChestConf(), this);
	}
	
	public int weight = 10;
}
