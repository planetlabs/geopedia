package com.sinergise.common.util.naming;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Conveniently typed map 
 */
public class IdentifiablesMap<V extends Identifiable> extends HashMap<Identifier, V> {

	private static final long serialVersionUID = 1L;

	public IdentifiablesMap() {
		super();
	}
	
	public IdentifiablesMap(int capacity) {
		super(capacity);
	}
	
	public IdentifiablesMap(Map<? extends Identifier, ? extends V> map) {
		super(map);
	}
	
	public IdentifiablesMap(Collection<? extends V> coll) {
		this(coll.size());
		putAll(coll);
	}
	
	public V put(V value) {
		return put(value.getQualifiedID(), value);
	}
	
	public void putAll(Collection<? extends V> coll) {
		for (V value : coll) {
			put(value);
		}
	}
	
	public V remove(V value) {
		return super.remove(value.getQualifiedID());
	}
	
	public Identifier[] toArray() {
		return keySet().toArray(new Identifier[size()]);
	}
}
