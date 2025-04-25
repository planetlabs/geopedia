package com.sinergise.common.util.collections.safe;

import com.sinergise.common.util.Util;

public class DefaultTypeSafeKey<T> implements TypeSafeKey<T> {
	private static final long serialVersionUID = 1L;

	public static final <T> DefaultTypeSafeKey<T> create(String name) {
		return new DefaultTypeSafeKey<T>(name);
	}
	
	private String keyName;
	
	protected DefaultTypeSafeKey() {
		//GWT serialization
	}
	
	public DefaultTypeSafeKey(String keyName) {
		this.keyName = keyName;
	}
	
	@Override
	public String toString() {
		return keyName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DefaultTypeSafeKey<?>)) {
			return false;
		}
		return Util.safeEquals(keyName, ((DefaultTypeSafeKey<?>)obj).keyName);
	}
	
	@Override
	public int hashCode() {
		return keyName.hashCode();
	}
	
	public String getKeyName() {
		return keyName;
	}
}