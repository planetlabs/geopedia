/*
 *
 */
package com.sinergise.common.util.state.gwt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.lang.Function;

public abstract class DefaultStateOriginator implements StateGWTOriginator {
	private static final long                   serialVersionUID = 2L;
	
	protected StateGWT                          data;
	protected transient HashMap<String, Object> wrappersCache;
	
	public DefaultStateOriginator() {
		this((StateGWT)null);
	}
	
	public DefaultStateOriginator(StateGWT st) {
		if (st == null) {
			st = new StateGWT();
		}
		this.data = st;
	}
	
	public DefaultStateOriginator(DefaultStateOriginator other) {
		this(new StateGWT());
		data.setFrom(other.data, true);
	}
	
	protected void checkLocked() {
		if (data.isReadOnly()) {
			throw new IllegalStateException("Cannot change state after it has been locked");
		} 
	}
	
	@Override
	public StateGWT storeInternalState(final StateGWT target) {
		return data;
	}
	
	public Set<String> keySet() {
		if (data == null) return null;
		return data.keySet();
	}
	
	public Map<String, String> getPropertyMap() {
		return data.getPropertyMap();
	}
	
	@Override
	public void loadInternalState(final StateGWT st) {
		final StateGWT old = this.data;
		this.data = st;
		internalStateUpdated(old);
	}
	
	/**
	 * Called afer the internal data has been (re)loaded via the {@link #loadInternalState(StateGWT)} method.
	 * 
	 * @param old The previous value of the internal state. Current value is accessible through the {@link #data} field.
	 */
	protected void internalStateUpdated(final StateGWT old) {
		updateWrappersCache();
	}
	
	protected void checkWrappers() {
		if (wrappersCache == null) {
			wrappersCache = new HashMap<String, Object>();
		}
	}
	
	@SuppressWarnings("unused")
	protected <T> T wrap(String key, StateGWT st) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unused")
	protected StateGWT unwrap(String key, Object value) {
		throw new UnsupportedOperationException();
	}
	
	protected <T> T getWrapped(String key) {
    	checkWrappers();
        @SuppressWarnings("unchecked")
		T ret=(T)wrappersCache.get(key);
        if (ret==null) {
        	StateGWT st=data.getState(key);
            if (st==null) return null;
            ret = this.<T>wrap(key, st);
            wrappersCache.put(key, ret);
        }
        return ret;
	}
	
	//TODO: Support wrapped-only mode, where storing to state only happens when needed (serialization etc.)
	protected void setWrapped(String key, StateGWTOriginator value) {
		data.putState(key, value.storeInternalState(data.getState(key)));
		checkWrappers();
		wrappersCache.put(key, value);
	}

	protected void setWrapped(String key, Object value) {
		data.putState(key, unwrap(key, value));
		checkWrappers();
		wrappersCache.put(key, value);
	}
	
	protected void updateWrappersCache() {
		if (wrappersCache == null) {
			return;
		}
		for (final Iterator<Map.Entry<String, Object>> it = wrappersCache.entrySet().iterator(); it.hasNext();) {
			final Map.Entry<String, Object> ent = it.next();
			final String key = ent.getKey();
			if (data.containsChild(key)) {
				Object oldWrapper = ent.getValue();
				if (oldWrapper instanceof StateGWTOriginator) {
					((StateGWTOriginator)oldWrapper).storeInternalState(data.getState(key));
				} else {
					ent.setValue(wrap(key, data.getState(key)));
				}
			} else {
				it.remove();
			}
		}
	}

	/**
	 * When locked, no property can be set on this object
	 * 
	 * @param readOnly
	 */
	public DefaultStateOriginator lock() {
		data.setReadOnly(true);
		return this;
	}

	//TODO: Clean up the whole wrapping mess
	protected static Object getOrCreateWrapper(AbstractAuxiliaryInfo parent, String key, Function<StateGWT, ? extends StateGWTOriginator> constructor) {
		parent.checkWrappers();
        Object ret = parent.wrappersCache.get(key);
        if (ret==null) {
        	StateGWT st = parent.data.getState(key);
        	if (st==null) {
        		st=new StateGWT();
        		parent.data.putState(key, st);
        	}
            ret = constructor.execute(st);
            parent.wrappersCache.put(key, ret);
        }
        return ret;
        
	}
}
