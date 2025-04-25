package com.sinergise.common.gis.io;

import java.io.IOException;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.feature.CFeature;

public class TransformingFeatureWriter implements FeatureWriter {

	private final FeatureWriter wrapped;
	private final Transform<?, ?> transform;
	private final boolean cloneGeometries;
	
	public TransformingFeatureWriter(FeatureWriter wrappedWriter, Transform<?, ?> transform) {
		this (wrappedWriter, transform, true);
	}
	
	public TransformingFeatureWriter(FeatureWriter wrappedWriter, Transform<?, ?> transform, boolean cloneGeometries) {
		this.wrapped = wrappedWriter;
		this.transform = transform;
		this.cloneGeometries = cloneGeometries;
	}

	@Override
	public void append(CFeature feature) throws ObjectWriteException {
		Geometry geom = feature.getGeometry();
		if (cloneGeometries && geom != null) {
			geom = geom.clone();
			feature.setGeometry(geom);
		}
		TransformUtil.transformGeometry(transform, geom);
		wrapped.append(feature);
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}

}
