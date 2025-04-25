package com.sinergise.common.gis.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class RepresentsFeatureCollection extends HashSet<RepresentsFeature> implements HasFeatureRepresentations {

	public RepresentsFeatureCollection() {
		super();
	}

	public RepresentsFeatureCollection(Collection<? extends RepresentsFeature> c) {
		super(c);
	}

	public RepresentsFeatureCollection(int initialCapacity) {
		super(initialCapacity);
	}
	
	public RepresentsFeatureCollection(RepresentsFeature ...features) {
		this(Arrays.asList(features));
	}

	@Override
	public Collection<RepresentsFeature> getFeatures() {
		return this;
	}

	public static RepresentsFeatureCollection singleton(RepresentsFeature f) {
		return new RepresentsFeatureCollection(Collections.singleton(f));
	}
	
}
