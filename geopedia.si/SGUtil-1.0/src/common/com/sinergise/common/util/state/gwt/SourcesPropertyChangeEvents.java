/*
 *
 */
package com.sinergise.common.util.state.gwt;

public interface SourcesPropertyChangeEvents<T> {
	/**
	 * Adds a listener interface.
	 * 
	 * @param listener the listener interface to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener<? super T> listener);
	
	/**
	 * Removes a previously added listener interface.
	 * 
	 * @param listener the listener interface to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener<? super T> listener);
}
