package com.sinergise.common.geometry.io;

import java.io.IOException;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.geom.Geometry;

public class TransformingGeometryWriter implements GeometryWriter {
	
	private final GeometryWriter wrapped;
	private final Transform<?, ?> transform;
	private final boolean cloneGeometries;
	
	public TransformingGeometryWriter(GeometryWriter wrappedWriter, Transform<?, ?> transform) {
		this (wrappedWriter, transform, true);
	}
	
	public TransformingGeometryWriter(GeometryWriter wrappedWriter, Transform<?, ?> transform, boolean cloneGeometries) {
		this.wrapped = wrappedWriter;
		this.transform = transform;
		this.cloneGeometries = cloneGeometries;
	}

	@Override
	public void append(Geometry geom) throws ObjectWriteException {
		if (cloneGeometries) {
			geom = geom.clone();
		}
		TransformUtil.transformGeometry(transform, geom);
		wrapped.append(geom);
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}

}
