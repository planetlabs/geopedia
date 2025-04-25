/*
 *
 */
package com.sinergise.common.util.property;

import java.io.Serializable;

import com.sinergise.common.util.auxprops.PropertyAuxiliaryData;

public interface Property<T> extends Serializable {
	
	public interface WritableProperty<T> extends Property<T> {
		
		boolean isWritable();
		
		void setValue(T value);
	}
	
	
	public interface QualifyingProperty {
		
		String getValueAsName();
	}
	
	PropertyAuxiliaryData getAuxData(boolean create);
	
	T getValue();
	
	boolean isNull();
	
	abstract void setLoaded(boolean b);
	abstract boolean isLoaded();

}
