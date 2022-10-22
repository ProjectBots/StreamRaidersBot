package configs.viewer;

import java.util.Arrays;

import configs.viewer.layers.ViewerLayerConf;
import include.DeepCopy;
import include.DeepCopyHashtable;
import configs.shared.TimeConf;

public class ViewerConf implements Cloneable {
	
	private static final TimeConf[] DEFAULT_TCONFS = {new TimeConf("(default)", 0)};
	
	@Override
	public ViewerConf clone() {
		return DeepCopy.copyAllFields(new ViewerConf(), this);
	}
	
	/**
	 * lid: ViewerLayerConf
	 */
	public final DeepCopyHashtable<String, ViewerLayerConf> lconfs = new DeepCopyHashtable<>();
	
	/**
	 * has to be sorted by start value<br>
	 * see {@link Arrays#sort(Object[])}
	 */
	public TimeConf[] tconfs = DEFAULT_TCONFS;
	
}
