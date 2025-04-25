package com.sinergise.common.geometry.service.util;

import java.io.Serializable;

import com.sinergise.common.geometry.geom.Geometry;

public class GeomOpResult implements Serializable {

	private static final long serialVersionUID = 2391664655585371037L;
	
	private Geometry result;
	
	@Deprecated /** Serialization only */
	protected GeomOpResult() { }
	
	public GeomOpResult(Geometry result) {
		this.result = result;
	}
	
	public boolean hasResult() {
		return result != null && !result.isEmpty();
	}
	
	public Geometry getResult() {
		return result;
	}

}
