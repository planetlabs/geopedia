/*
 *
 */
package com.sinergise.common.util.settings;

public class NamedTypedObject<T> extends TypedObject<T> {
	public String name;
	
	public NamedTypedObject(final String name, final ResolvedType<T> expectedType, final T value) {
		super(expectedType, value);
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name + " | " + super.toString();
	}
}
