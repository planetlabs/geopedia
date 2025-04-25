package com.sinergise.common.util.property;

import com.sinergise.common.util.auxprops.PropertyAuxiliaryData;
import com.sinergise.common.util.messages.ValidationMessage;

public abstract class DefaultPropertyImpl<T> implements Property<T> {
	private static final long serialVersionUID = 3L;
	
	protected PropertyAuxiliaryData auxData = null;
	protected boolean isLoaded = true;
	
	@Override
	public PropertyAuxiliaryData getAuxData(boolean create) {
		if (auxData == null && create) {
			auxData = constructAuxData();
	    }
	    return auxData;
	}
	
	protected PropertyAuxiliaryData constructAuxData() {
		return new PropertyAuxiliaryData();
	}

	public ValidationMessage getValidationMessage() {
		if (auxData == null) {
			return null;
		}
		return auxData.getValidationMessage();
	}
	
    @Override
	public final int hashCode() {
    	return super.hashCode();
    }
    
    @Override
	public final boolean equals(Object obj) {
    	return this == obj;
    }

	@Override
	public String toString() {
		T value = getValue();
		if (value == null) {
			return null;
		}
		if (value == this) {
			return null; /* obviously, the implementor's getValue is broken */
		}
		return String.valueOf(value);
	}

	@Override
	public boolean isNull() {
		return getValue() == null;
	}
	
	@Override
	public void setLoaded(boolean b) {
		isLoaded=b;
	}
	
	@Override
	public boolean isLoaded() {
		return isLoaded;
	}
}