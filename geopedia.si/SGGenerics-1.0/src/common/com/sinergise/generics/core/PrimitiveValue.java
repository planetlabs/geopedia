package com.sinergise.generics.core;

import java.io.Serializable;

public class PrimitiveValue implements ValueHolder, Serializable{

	public String value;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6675890378484988635L;
	public PrimitiveValue() {
	}
	
	public PrimitiveValue(String value2) {
		value = value2;
	}
	
	@Override
	public boolean isNull() {
		if (value != null && value.length()>0)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrimitiveValue other = (PrimitiveValue)obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value); // For Eclipse debug
	}
}
