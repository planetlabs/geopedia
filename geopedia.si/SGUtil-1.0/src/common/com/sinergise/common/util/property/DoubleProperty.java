package com.sinergise.common.util.property;

import com.sinergise.common.util.property.Property.QualifyingProperty;


public class DoubleProperty extends NumberProperty<Double> implements QualifyingProperty {
	private static final long serialVersionUID = 1L;

	public DoubleProperty() {
	}
	
	public DoubleProperty(double value) {
		super(Double.isNaN(value) ? null : Double.valueOf(value));
	}
	
	public DoubleProperty(Double value) {
		super(value);
	}
	
	@Override
	public void setValue(Double obj) {
		super.setValue(obj == null || obj.isNaN() ? null : obj);
	}
	
	@Override
	public String getValueAsName() {
		return value == null ? "" : value.toString();
	}

}
