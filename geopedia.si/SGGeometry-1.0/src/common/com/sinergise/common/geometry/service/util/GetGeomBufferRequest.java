package com.sinergise.common.geometry.service.util;

import com.sinergise.common.geometry.geom.Geometry;

public class GetGeomBufferRequest extends GeomOpRequest {

	private static final long serialVersionUID = 8514522753071945593L;
	
	private Geometry geometry;
	private double distance;
	
	@Deprecated /** Serialization only */
	protected GetGeomBufferRequest() { }
	
	
	public GetGeomBufferRequest(Geometry geom, double distance) {
		this.geometry = geom;
		this.distance = distance;
	}
	
	public GetGeomBufferRequest(Geometry geom, double distance, double gridSize) {
		this(geom, distance);
		setGridSize(gridSize);
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
	
	public double getDistance() {
		return distance;
	}
	
}
