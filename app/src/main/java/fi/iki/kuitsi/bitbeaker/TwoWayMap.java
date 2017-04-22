package fi.iki.kuitsi.bitbeaker;

import android.support.v4.util.SimpleArrayMap;

/**
 * This class simulates a map from which you can retrieve data
 * both with the keys and the values.
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class TwoWayMap<K, V> {
	private final SimpleArrayMap<K, V> map = new SimpleArrayMap<>();
	private final SimpleArrayMap<V, K> reversedMap = new SimpleArrayMap<>();
	private final K defaultKey;
	private final V defaultValue;

	/**
	 * Initializes the map so that when the key or value
	 * is not found, null is returned.
	 */
	public TwoWayMap() {
		this(null, null);
	}

	/**
	 * This initializes the map so that when the key or value
	 * is not found, the default value for the other is returned.
	 *
	 * @param defaultKey What getByValue should return if it doesn't find the value
	 * @param defaultValue What getByKey should return if it doesn't find the key
	 */
	public TwoWayMap(K defaultKey, V defaultValue) {
		this.defaultKey = defaultKey;
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the Object mapped from the specified key, or {@link #defaultKey} if no such mapping
	 * has been made.
	 */
	public V getByKey(K key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return defaultValue;
	}

	/**
	 * Gets the key that is mapped to the specified value, or {@link #defaultValue} if no
	 * such mapping has been made.
	 */
	public K getByValue(V value) {
		if (reversedMap.containsKey(value)) {
			return reversedMap.get(value);
		}
		return defaultKey;
	}

	/**
	 * Adds a mapping from the specified key to the specified value, replacing the previous mapping
	 * from the specified key if there was one.
	 */
	public void put(K key, V value) {
		V oldValue = map.get(key);
		map.put(key, value);
		reversedMap.remove(oldValue);
		reversedMap.put(value, key);
	}
}
