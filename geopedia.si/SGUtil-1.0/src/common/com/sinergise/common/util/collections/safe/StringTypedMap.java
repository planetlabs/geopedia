package com.sinergise.common.util.collections.safe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.util.Util;

public class StringTypedMap<K extends StringTypedMapKey<? extends V>, V> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	protected Map<String, String> map;
	
	public StringTypedMap() {
		this(new HashMap<String, String>());
	}

	public StringTypedMap(Map<String, String> data) {
		this.map = data;
	}	
	
	public Map<String, String> getStringMap() {
		return map;
	}
	
	public boolean containsKey(K key) {
		return map.containsKey(key.getKeyName());
	}

	protected <T extends V> T internalGet(StringTypedMapKey<T> key) {
		return readVal(key, map.get(key.getKeyName()));
	}
	
	protected <T extends V> T internalPut(StringTypedMapKey<T> key, T value) {
		return readVal(key, map.put(key.getKeyName(), writeVal(key, value)));
	}

	protected <T extends V> T internalRemove(StringTypedMapKey<T> key) {
		return readVal(key, map.remove(key.getKeyName()));
	}
	
	protected <T extends V> T readVal(StringTypedMapKey<T> key, String val) {
		return val == null ? null : key.read(val);
	}
	
	protected <T> String writeVal(StringTypedMapKey<T> key, T value) {
		return value == null ? null : key.write(value);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void clear() {
		map.clear();
	}

	@Override
	public int hashCode() {
		return ((map == null) ? 0 : map.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		@SuppressWarnings("rawtypes")
		StringTypedMap other = (StringTypedMap)obj;
		if (!Util.safeEquals(map, other.map)) {
			return false;
		}
		return true;
	}

	
}
