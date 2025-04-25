package com.sinergise.common.raster.dataraster.service.average;

import java.io.Serializable;

import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.Envelope;

public class GetDemAverageRequest implements Serializable {

	private static final long serialVersionUID = 6185632935965732418L;
	
	private Polygon polygon;
	private Envelope bbox;
	
	@Deprecated /** Serialization only */
	protected GetDemAverageRequest() {}
	
	
	public GetDemAverageRequest(Envelope bbox) {
		this.bbox = bbox;
		this.polygon = null;
	}
	
	public GetDemAverageRequest(Polygon polygon) {
		this.polygon = polygon;
		this.bbox = polygon.getEnvelope();
	}
	
	public Envelope getBBox() {
		return bbox;
	}
	
	public Polygon getPolygon() {
		return polygon;
	}


	public boolean hasPolygon() {
		return polygon != null && !polygon.isEmpty();
	}
}
