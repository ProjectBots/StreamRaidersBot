package configs.shared.layers;

import include.DeepCopy;

public class SleepConf implements Cloneable {
	
	@Override
	public SleepConf clone() {
		return DeepCopy.copyAllFields(new SleepConf(), this);
	}
	
	public int min = 100;
	public int max = 720;
	public int sync = -1;
}
