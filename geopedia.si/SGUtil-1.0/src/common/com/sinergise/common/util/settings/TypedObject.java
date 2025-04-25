/*
 *
 */
package com.sinergise.common.util.settings;

public class TypedObject<T> {
	public ResolvedType<T> expectedType;
	public T               value;
	
	/**
	 * @param expectedType
	 * @param value
	 */
	public TypedObject(final ResolvedType<T> expectedType, final T value) {
		super();
		this.expectedType = expectedType;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return expectedType.toString() + " : " + value;
	}
}
