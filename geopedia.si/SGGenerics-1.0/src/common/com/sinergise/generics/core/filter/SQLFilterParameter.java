package com.sinergise.generics.core.filter;

import java.io.Serializable;

import com.sinergise.generics.core.TypeAttribute;

public class SQLFilterParameter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8317686183600883992L;


	public TypeAttribute ta;
	public String value;
	protected SQLFilterParameter() {
		
	}
	
	public SQLFilterParameter (String parmName, int parmType, String parmValue) {
		this.ta = new TypeAttribute(Integer.MIN_VALUE, parmName, parmType);
		this.value = parmValue;
	}
	public SQLFilterParameter (TypeAttribute ta, String value) {
		this.ta=ta;
		this.value=value;
	}
	public TypeAttribute getTypeAttribute() {
		return ta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ta == null) ? 0 : ta.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SQLFilterParameter))
			return false;
		SQLFilterParameter other = (SQLFilterParameter) obj;
		if (ta == null) {
			if (other.ta != null)
				return false;
		} else if (!ta.equals(other.ta))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
	
}
