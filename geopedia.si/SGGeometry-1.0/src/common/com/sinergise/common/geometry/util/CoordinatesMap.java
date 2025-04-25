package com.sinergise.common.geometry.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.geometry.index.KdTree;
import com.sinergise.common.util.geom.HasCoordinate;

public class CoordinatesMap<K, V extends HasCoordinate> extends HashMap<K,V> {
	
	private static final long serialVersionUID = -1245635543068069670L;
	
	private KdTree<V> index = new KdTree<V>();

	public CoordinatesMap() {
		super();
	}

	public CoordinatesMap(int initialCapacity) {
		super(initialCapacity);
	}

	public CoordinatesMap(Map<K,V> m) {
		super(m);
		index.addAll(m.values());
	}
	
	@Override
	public V put(K key, V value) {
		V prev = super.put(key, value);
		if (prev != null) {
			index.remove(prev);
		}
		index.add(value);
		return prev;
	}
	
	@Override
	public V remove(Object key) {
		V removed = super.remove(key);
		if (removed != null) {
			index.remove(removed);
		}
		return removed;
	}
	
	@Override
	public void clear() {
		super.clear();
		index.clear();
	}

	public V findNearest(HasCoordinate pos) {
		return index.findNearest(pos);
	}
	

	public V findNearest(HasCoordinate pos, double minDistSq, Set<? extends V> excluded) {
		return index.findNearest(pos, minDistSq, excluded);
	}
	

}
