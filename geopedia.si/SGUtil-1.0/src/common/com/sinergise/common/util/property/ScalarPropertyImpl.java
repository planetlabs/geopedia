package com.sinergise.common.util.property;

import com.sinergise.common.util.property.Property.WritableProperty;


public abstract class ScalarPropertyImpl<T> extends DefaultPropertyImpl<T> implements WritableProperty<T> {
	private static final long serialVersionUID = 3L;
	
	protected T value;
	
	protected ScalarPropertyImpl() {
	}
	
	public ScalarPropertyImpl(T value) {
		setValue(value);
	}
	
	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public void setValue(T value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value == null ? null : value.toString();
	}
}