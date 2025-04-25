package com.sinergise.generics.core;

import java.io.Serializable;
import java.util.Arrays;

public class BinaryValueHolder implements ValueHolder, Serializable{

	private static final long serialVersionUID = 688808411018153271L;
	public byte[] value;
	public BinaryValueHolder() {		
	}
	public BinaryValueHolder(byte[] bytes) {
		this.value=bytes;
	}
	
	@Override
	public boolean isNull() {
		if (value==null)
			return true;
		return false;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinaryValueHolder other = (BinaryValueHolder) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

}
