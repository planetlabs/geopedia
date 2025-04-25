package com.sinergise.generics.core.filter;

import java.util.Arrays;

import com.sinergise.generics.core.EntityObject;

public class SimpleFilter implements DataFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5934101143641341810L;
	
	public static final String BINARY_MATCH_FILTER_PREFIX="&&";
	public static final String BINARY_MATCH_FILTER_SEPARATOR=",";
	public static final String addBinaryMatchPrefix(String value, Number mask) {
		return BINARY_MATCH_FILTER_PREFIX+value+BINARY_MATCH_FILTER_SEPARATOR+String.valueOf(mask);
	}
	
	public static final boolean isMatchBinary(String value) {
		return value.startsWith(BINARY_MATCH_FILTER_PREFIX);
	}
	
	public static final String extractValue(String value) {
		String valNoPrefix = removeBinaryMatchPrefix(value);
		String split[] = valNoPrefix.split(",");
		if (split.length==2)
			return split[0];
		return value;
	}
	
	public static final String extractMask(String value) {
		String valNoPrefix = removeBinaryMatchPrefix(value);
		String split[] = valNoPrefix.split(",");
		if (split.length==2)
			return split[1];
		return value;
	}
	private static final String removeBinaryMatchPrefix(String value) {
		if (value!=null && value.length()>BINARY_MATCH_FILTER_PREFIX.length()) {
			return value.substring(BINARY_MATCH_FILTER_PREFIX.length());
		}
		return value;
	}
	
	private byte [] operators;
	private EntityObject filterData;
	
	public SimpleFilter() {
		this.operators = new byte[0];
		this.filterData = null;
	}
	
	
	public SimpleFilter(EntityObject filterData, byte[] op) {
		this.filterData = filterData;
		int atCount = filterData.getAttributeCount();
		this.operators = new byte[atCount];
		for (int i=0;i<atCount;i++) {
			this.operators[i]=op[i];
		}

	}
	public SimpleFilter(EntityObject filterData, byte defaultOperator) {
		this.filterData = filterData;
		int atCount = filterData.getAttributeCount();
		operators = new byte[atCount];
		for (int i=0;i<atCount;i++) {
			operators[i]=defaultOperator;
		}
	}
	
	public SimpleFilter(EntityObject filterData) {
		this(filterData, OPERATOR_AND);
	}
	
	
	
	

	public void setOperator(int field, byte operator) {
		if (operators==null)
			throw new RuntimeException("Oops, operators structure not initialized.");
		if (field<0 || field > operators.length)
			throw new IllegalArgumentException("Field must be between 0 and "+operators.length);
		operators[field]=operator;
	}
	
	public EntityObject getFilterData() {
		return filterData;
	}
	
	public byte[] getOperators() {
		return operators;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filterData == null) ? 0 : filterData.hashCode());
		result = prime * result + Arrays.hashCode(operators);
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
		SimpleFilter other = (SimpleFilter) obj;
		if (filterData == null) {
			if (other.filterData != null)
				return false;
		} else if (!filterData.equals(other.filterData))
			return false;
		if (!Arrays.equals(operators, other.operators))
			return false;
		return true;
	}
	
	
}


/**
 * 
 * CompoundEntityFilter
 * 
 * array of simpleFilters
 * first simple filter's operator used to everything combine together
 * sequential operators used to combine fields together
 *

 *
if (ftype=simple) {
	for (f:fieldsLength)
	  if (f.isField) {
	  	filterPart buildFilterPart(f.getField)
	  }
}


if (f is compound)
	get f1, get f2
	build filter (
*/