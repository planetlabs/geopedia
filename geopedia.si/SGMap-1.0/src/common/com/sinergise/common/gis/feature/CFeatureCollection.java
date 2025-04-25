package com.sinergise.common.gis.feature;

import java.util.Collection;
import java.util.HashSet;

import com.sinergise.common.util.collections.CollectionUtil;

public class CFeatureCollection extends HashSet<CFeature> implements HasFeatures {
	
	private static final long serialVersionUID = 7795002650396921358L;

	public CFeatureCollection() {
		super();
	}

	public CFeatureCollection(Collection<? extends CFeature> c) {
		super(c);
	}

	public CFeatureCollection(int initialCapacity) {
		super(initialCapacity);
	}
	
	public CFeatureCollection(CFeature ...features) {
		this(features.length);
		for (CFeature f : features) {
			add(f);
		}
	}

	public boolean isSingleton() {
		return size() == 1;
	}

	public CFeature getFirst() {
		return CollectionUtil.first(this);
	}
	
	@Override
	public Collection<CFeature> getFeatures() {
		return this;
	}

	public static CFeatureCollection singleton(CFeature f) {
		return new CFeatureCollection(f);
	}

}
