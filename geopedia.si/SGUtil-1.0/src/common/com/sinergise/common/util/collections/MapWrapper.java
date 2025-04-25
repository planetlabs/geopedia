package com.sinergise.common.util.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class MapWrapper<K, V> implements Map<K, V>, Serializable {

	private static final long serialVersionUID = 6611500671116243069L;
	
	protected Map<K, V> map;
	
	public MapWrapper() {
		this(new HashMap<K, V>());
	}

	public MapWrapper(Map<K, V> wrapped) {
		super();
		this.map = wrapped;
	}

	public Map<K, V> getMap() {
		return map;
	}
	
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	@Deprecated
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsKeySafe(K key) {
		return map.containsKey(key);
	}
	
	@Override
	@Deprecated
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public boolean containsValueSafe(V value) {
		return map.containsValue(value);
	}

	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}
	
	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}
}