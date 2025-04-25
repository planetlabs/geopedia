package com.sinergise.generics.core;


public class LookupPrimitiveValue extends PrimitiveValue {

	public String lookedUpValue;
	/**
	 * 
	 */
	private static final long serialVersionUID = 9210126503131741393L;
	
	public LookupPrimitiveValue() {
		
	}
	public LookupPrimitiveValue(PrimitiveValue pv) {
		this.value = pv.value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lookedUpValue == null) ? 0 : lookedUpValue.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		LookupPrimitiveValue other = (LookupPrimitiveValue)obj;
		if (lookedUpValue == null) {
			if (other.lookedUpValue != null) return false;
		} else if (!lookedUpValue.equals(other.lookedUpValue)) return false;
		return true;
	}
}
