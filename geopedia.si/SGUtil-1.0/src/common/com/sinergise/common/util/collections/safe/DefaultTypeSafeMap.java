package com.sinergise.common.util.collections.safe;

import java.util.Map;

public class DefaultTypeSafeMap<V> extends TypeSafeMap<DefaultTypeSafeKey<? extends V>, V> {
	
	private static final long serialVersionUID = 4913644494499259676L;

	public static final <V> DefaultTypeSafeMap<V> create() {
		return new DefaultTypeSafeMap<V>();
	}
	
	public static final <V> DefaultTypeSafeMap<V> wrap(Map<DefaultTypeSafeKey<? extends V>, V> m) {
		return new DefaultTypeSafeMap<V>(m);
	}
	
	public DefaultTypeSafeMap() {
		super();
	}
	
	public DefaultTypeSafeMap(Map<DefaultTypeSafeKey<? extends V>, V> m) {
		super(m);
	}
	
	public <T extends V> T getSafe(DefaultTypeSafeKey<T> key) {
		return internalGet(key);
	}
	
	public <T extends V> T putSafe(DefaultTypeSafeKey<T> key, T value) {
		return internalPut(key, value);
	}

	public <T extends V> T removeSafe(DefaultTypeSafeKey<T> key) {
		return internalRemove(key);
	}

}

