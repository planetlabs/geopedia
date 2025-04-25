package com.sinergise.common.util.property;

import com.sinergise.common.util.property.Property.QualifyingProperty;



public class LongProperty extends NumberProperty<Long> implements QualifyingProperty {
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated for serialization only
	 */
	@Deprecated
	public LongProperty() {
	}
	
	public LongProperty(long value)
	{
		this(Long.valueOf(value));
	}

	public LongProperty(Long value) {
		super(value);
	}

	/**
	 * Overridden for type safety
	 */
	@Override
	public void setValue(Long value) {
		super.setValue(value);
	}
	
    @Override
	public String getValueAsName() {
    	return value == null ? "" : String.valueOf(value);
    }
    
    public void setValueFromName(String name) {
    	setValue(name.isEmpty() ? null : Long.valueOf(name));
    }
}
