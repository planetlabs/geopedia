package com.sinergise.common.util.property;

import com.sinergise.common.util.property.Property.QualifyingProperty;


public final class BooleanProperty extends ScalarPropertyImpl<Boolean> implements QualifyingProperty
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated for serialization only
	 */
	@Deprecated
	public BooleanProperty() {
		this(Boolean.FALSE);
	}
	
	public BooleanProperty(Boolean value) {
		super(value==null ? Boolean.FALSE : value);
	}
	
	@Override
	public void setValue(Boolean value) {
		super.setValue(value == null ? Boolean.FALSE : value);
	}
	
    @Override
	public String getValueAsName() {
    	return getValue().toString();
    }
}
