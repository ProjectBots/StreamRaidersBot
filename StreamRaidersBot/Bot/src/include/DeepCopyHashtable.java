package include;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

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

		forEach((k, v) -> ret.put(ObjectUtils.cloneIfPossible(k), ObjectUtils.cloneIfPossible(v)));

		return ret;
	}

	public DeepCopyHashtable() {
		super();
	}

	public DeepCopyHashtable(int initialCapacity) {
		super(initialCapacity);
	}

	public synchronized void putAllIfAbsent(Map<? extends K, ? extends V> t) {
		for(Map.Entry<? extends K, ? extends V> e : t.entrySet())
			putIfAbsent(e.getKey(), e.getValue());
	}
}
