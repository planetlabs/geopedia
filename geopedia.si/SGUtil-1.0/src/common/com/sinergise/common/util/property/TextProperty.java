package com.sinergise.common.util.property;

import com.sinergise.common.util.property.Property.QualifyingProperty;


public final class TextProperty extends ScalarPropertyImpl<String> implements QualifyingProperty {

	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated
	 */
	@Deprecated
	public TextProperty() {
	}
	
	public TextProperty(String text) {
		super(text);
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
	}
	
	@Override
	public String getValueAsName() {
		return getValue();
	}
	
	public void setValueFromName(String name) {
		setValue(name);
	}
}
