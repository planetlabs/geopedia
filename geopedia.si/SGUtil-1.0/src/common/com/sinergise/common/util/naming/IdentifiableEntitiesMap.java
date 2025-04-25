package com.sinergise.common.util.naming;

import java.util.Collection;
import java.util.Map;

import com.sinergise.common.util.naming.EntityIdentifier.IdentifierResolveListener;

public class IdentifiableEntitiesMap<V extends IdentifiableEntity> extends IdentifiablesMap<V> {
	private static final long serialVersionUID = 1L;
	
	private IdentifierResolveListener keyResolveListener = new IdentifierResolveListener() {
		@Override
		public void onIdentifierResolved(EntityIdentifier ID) {
			put(ID.getResolvedId(), remove(ID));
		}
	};

	public IdentifiableEntitiesMap() {
		super();
	}
	
	public IdentifiableEntitiesMap(int capacity) {
		super(capacity);
	}
	
	public IdentifiableEntitiesMap(Map<? extends Identifier, ? extends V> map) {
		super(map);
	}
	
	public IdentifiableEntitiesMap(Collection<? extends V> coll) {
		super(coll);
	}
	
	@Override
	public boolean containsKey(Object key) {
		EntityIdentifier ID = (EntityIdentifier)key;
		return super.containsKey(ID) || super.containsKey(ID.getResolvedId());
	}
	
	
	@Override
	public V put(Identifier key, V value) {
		value.getQualifiedID().addResolveListener(keyResolveListener);
		return super.put(((EntityIdentifier)key).getResolvedId(), value);
	}
	
	@Override
	public V remove(Object key) {
		EntityIdentifier ID = (EntityIdentifier)key;
		ID.removeResolveListener(keyResolveListener);
		
		V val = super.remove(ID);
		if (val == null) {
			val = super.remove(ID.getResolvedId());
		}
		return val;
	}
	
	@Override
	public V get(Object key) {
		EntityIdentifier ID = (EntityIdentifier)key;
		
		V val = super.get(ID);
		if (val == null) {
			val = super.get(ID.getResolvedId());
		}
		return val;
	}
	
}
