package configs.viewer.layers;

import include.DeepCopy;

public class CaptainConf implements Cloneable {
	
	@Override
	public CaptainConf clone() {
		return DeepCopy.copyAllFields(new CaptainConf(), this);
	}
	
	//	campaign
	public int cfav = 1;
	public boolean cic = false;
	public boolean cil = false;

	//	dungeon
	public int dfav = 1;
	public boolean dic = false;
	public boolean dil = false;
	
	//	versus	(just for consistency atm)
	public int vfav = 1;
	public boolean vic = false;
	public boolean vil = false;
}
