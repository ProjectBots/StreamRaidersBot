package configs.captain;

import java.util.Arrays;

import configs.captain.layers.CaptainLayerConf;
import configs.shared.TimeConf;
import include.DeepCopy;
import include.DeepCopyHashtable;

public class CaptainConf implements Cloneable {

	@Override
	public CaptainConf clone() {
		return DeepCopy.copyAllFields(new CaptainConf(), this);
	}
	
	/**
	 * lid: CaptainLayerConf
	 */
	public final DeepCopyHashtable<String, CaptainLayerConf> lconfs = new DeepCopyHashtable<>();
	
	/**
	 * must be sorted by start value<br>
	 * see {@link Arrays#sort(Object[])}
	 */
	public TimeConf[] tconfs;
	
	
}
