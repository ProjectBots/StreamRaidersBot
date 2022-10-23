package include;

import java.util.Hashtable;
import java.util.Map;

/**
 * Cloning this hashtable produces a true deepcopy <b>if and only if</b> all
 * keys/elements produce a deepcopy when cloned<br>
 * Keys/Elements without the {@link Cloneable} interface will not be cloned
 * 
 * @author ProjectBots
 *
 * @param <K>
 * @param <V>
 */
public class DeepCopyHashtable<K, V> extends Hashtable<K, V> implements Cloneable {
	private static final long serialVersionUID = 1L;

	@Override
	public synchronized DeepCopyHashtable<K, V> clone() {
		DeepCopyHashtable<K, V> ret = new DeepCopyHashtable<>(size());
		forEach((k, v) -> ret.put(DeepCopy.copyObject(k), DeepCopy.copyObject(v)));
		return ret;
	}

	public DeepCopyHashtable() {
		super();
	}

	public DeepCopyHashtable(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Copies all of the mappings from the specified map to this.
	 * These mappings will <b>not</b> replace any mappings that this table had for any of the keys currently in the specified map.
	 * @param t mappings to be stored in this map
	 */
	public synchronized void putAllIfAbsent(Map<? extends K, ? extends V> t) {
		for(Map.Entry<? extends K, ? extends V> e : t.entrySet())
			putIfAbsent(e.getKey(), e.getValue());
	}
}
