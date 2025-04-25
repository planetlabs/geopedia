/*
 *
 */
package com.sinergise.common.util.event;

public interface ValueChangeListener<T> {
	void valueChanged(Object sender, T oldValue, T newValue);
}
