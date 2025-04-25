package com.sinergise.common.geometry.io;

import java.io.IOException;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.geom.Geometry;

public class TransformingGeometryReader implements GeometryReader {
	
	private final GeometryReader wrapped;
	private final Transform<?, ?> transform;
	
	public TransformingGeometryReader(GeometryReader wrappedReader, Transform<?, ?> transform) {
		this.wrapped = wrappedReader;
		this.transform = transform;
	}

	@Override
	public Geometry readNext() throws ObjectReadException {
		Geometry geom = wrapped.readNext();
		TransformUtil.transformGeometry(transform, geom);
		return geom;
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
