package com.sinergise.common.gis.io;

import java.io.IOException;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.gis.feature.CFeature;

public class TransformingFeatureReader implements FeatureReader {
	
	private final FeatureReader wrapped;
	private final Transform<?, ?> transform;
	
	public TransformingFeatureReader(FeatureReader wrappedReader, Transform<?, ?> transform) {
		this.wrapped = wrappedReader;
		this.transform = transform;
	}

	@Override
	public CFeature readNext() throws ObjectReadException {
		CFeature feature = wrapped.readNext();
		TransformUtil.transformGeometry(transform, feature.getGeometry());
		return feature;
	}

	@Override
	public boolean hasNext() throws ObjectReadException {
		return wrapped.hasNext();
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}

}
