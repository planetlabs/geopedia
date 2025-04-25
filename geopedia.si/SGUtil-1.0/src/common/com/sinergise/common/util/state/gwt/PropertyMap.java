/*
 *
 */
package com.sinergise.common.util.state.gwt;

import java.util.HashMap;

public class PropertyMap implements SourcesPropertyChangeEvents<Object> {
	public final Object                             eventsSource;
	public PropertyChangeListenerCollection<Object> listeners;
	public HashMap<String, Object>                  data = null;
	
	public PropertyMap(final Object eventSource) {
		this.eventsSource = eventSource;
	}
	
	protected PropertyMap() {
		this.eventsSource = this;
	}
	
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener<Object> listener) {
		if (listeners == null) {
			listeners = new PropertyChangeListenerCollection<Object>();
		}
		listeners.add(listener);
	}
	
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener<Object> listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}
	
	public void fireChange(final String propertyName, final Object oldValue, final Object newValue) {
		if (listeners == null) {
			return;
		}
		listeners.fireChange(eventsSource, propertyName, oldValue, newValue);
	}
	
	public String getString(final String propName, final String defaultValue) {
		if (data == null) {
			return defaultValue;
		}
		final Object ret = data.get(propName);
		if (ret == null) {
			return defaultValue;
		}
		return String.valueOf(ret);
	}
	
	public Object set(final String name, final Object value) {
		if (data == null) {
			data = new HashMap<String, Object>();
		}
		if (value == null) {
			final Object old = data.remove(name);
			if (old != null) {
				fireChange(name, old, null);
			}
			return old;
		}
		final Object old = data.put(name, value);
		if (!value.equals(old)) {
			fireChange(name, old, value);
		}
		return old;
	}
	
	public boolean getBoolean(final String propName, final boolean defaultValue) {
		if (data == null) {
			return defaultValue;
		}
		final Object ret = data.get(propName);
		if (ret == null) {
			return defaultValue;
		}
		if (!(ret instanceof Boolean)) {
			return Boolean.valueOf((String)ret).booleanValue();
		}
		return ((Boolean)ret).booleanValue();
	}
}
