package configs.viewer;

import java.util.Arrays;
import java.util.Hashtable;

import configs.viewer.layers.LayerConf;
import configs.shared.TimeConf;

public class ViewerConf {

	public final Hashtable<String, LayerConf> lconfs = new Hashtable<>();
	
	/**
	 * has to be sorted by start value<br>
	 * see {@link Arrays#sort(Object[])}
	 */
	public TimeConf[] tconfs;
	
	/**
	 * 0 chests
	 * 1 bought
	 * 2 event
	 */
	public final Hashtable<Short, Hashtable<String, Integer>> stats = new Hashtable<>();
	
}
