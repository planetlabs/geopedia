/*
 *
 */
package com.sinergise.common.util.state.gwt;

public interface PropertyChangeListener<T> {
	void propertyChange(Object sender, String propertyName, T oldValue, T newValue);
}
