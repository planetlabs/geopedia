/*
 *
 */
package com.sinergise.common.util.state.gwt;

import java.util.Vector;

import com.sinergise.common.util.Util;

public class PropertyChangeListenerCollection<T> extends Vector<PropertyChangeListener<T>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean           alowFireEvents   = true;
	
	/**
	 * Fires a change event to all listeners.
	 * 
	 * @param sender the widget sending the event.
	 */
	public void fireChange(final Object sender, final String propertyName, final T oldValue, final T newValue) {
		if (isAlowFireEvents()) {
			if (Util.safeEquals(oldValue, newValue)) {
				return;
			}
			for (int i = size() - 1; i >= 0; i--) {
				final PropertyChangeListener<T> pcl = get(i);
				try {
					pcl.propertyChange(sender, propertyName, oldValue, newValue);
				} catch(final Exception e) {
					System.out.println("Exception in listener for propertyChange " + propertyName + ":" + e);
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isAlowFireEvents() {
		return alowFireEvents;
	}
	
	public void setAlowFireEvents(final boolean fireEvents) {
		this.alowFireEvents = fireEvents;
	}
	
}
