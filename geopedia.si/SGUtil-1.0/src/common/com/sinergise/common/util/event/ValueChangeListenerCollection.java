/*
 *
 */
package com.sinergise.common.util.event;

import java.util.Vector;

import com.sinergise.common.util.Util;

@SuppressWarnings("serial")
public class ValueChangeListenerCollection<T> extends Vector<ValueChangeListener<? super T>> {
	
	/**
	 * Fires a change event to all listeners.
	 * 
	 * @param sender - the widget sending the event.
	 */
	public boolean fireChange(final SourcesValueChangeEvents<? extends T> sender, final T oldValue, final T newValue) {
		if (Util.safeEquals(oldValue, newValue)) {
			return false;
		}
		
		for (int i = size() - 1; i >= 0; i--) {
			get(i).valueChanged(sender, oldValue, newValue);
		}
		return true;
	}
	
	public static final <T> ValueChangeListenerCollection<T> newInstance() {
		return new ValueChangeListenerCollection<T>();
	}
}
