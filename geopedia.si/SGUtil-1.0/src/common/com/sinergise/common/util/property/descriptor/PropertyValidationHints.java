package com.sinergise.common.util.property.descriptor;

import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.collections.safe.TypeSafeMap;
import com.sinergise.common.util.property.descriptor.PropertyValidationHints.PropertyValidationHintKey;

public class PropertyValidationHints extends TypeSafeMap<PropertyValidationHintKey<?>, Object> {
	
	private static final long serialVersionUID = 1L;
	
	public static final PropertyValidationHintKey<Boolean> KEY_IGNORE_MANDATORY_VALIDATION 	= null;
	
	public <T> T getSafe(PropertyValidationHintKey<T> key) {
		return internalGet(key);
	}
	
	public <T> T putSafe(PropertyValidationHintKey<T> key, T value) {
		return internalPut(key, value);
	}
	
	public <T> T getDefaultSafe(PropertyValidationHintKey<T> key) {
		return internalGetDefault(key);
	}
	
	public <T> T removeSafe(PropertyValidationHintKey<T> key) {
		return internalRemove(key);
	}
	
	public boolean getSafeBoolean(PropertyValidationHintKey<Boolean> key, boolean defaultVal) {
		Boolean val = getSafe(key);
		if (val != null) {
			return val.booleanValue();
		}
		return defaultVal;
	}
	
	public static class PropertyValidationHintKey<T> extends DefaultTypeSafeKey<T> {
		
		private static final long serialVersionUID = 1L;

		protected PropertyValidationHintKey() {
		}
		
		public PropertyValidationHintKey(String keyName) {
			super(keyName);
		}
		
	}

}
