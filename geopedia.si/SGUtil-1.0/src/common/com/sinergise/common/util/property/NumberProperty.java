package com.sinergise.common.util.property;


public abstract class NumberProperty<T extends Number> extends ScalarPropertyImpl<T> {

	private static final long serialVersionUID = 1L;

	public NumberProperty() {
	}
	
	public NumberProperty(T val) {
		super(val);
	}
}
