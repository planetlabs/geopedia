/*
 *
 */
package com.sinergise.common.util.settings;

public class NamedValue<T> {
	public final T      value;
	public final String name;
	
	public NamedValue(final String name, final T value) {
		this.name = name;
		this.value = value;
	}
}
