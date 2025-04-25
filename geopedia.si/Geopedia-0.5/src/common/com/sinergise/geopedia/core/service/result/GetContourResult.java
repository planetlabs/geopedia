package com.sinergise.geopedia.core.service.result;

import java.io.Serializable;

import com.sinergise.common.geometry.geom.MultiLineString;

@SuppressWarnings("serial")
public class GetContourResult implements Serializable {

	public double pointHeight;
	public MultiLineString contour;

	@Deprecated //serialization only
	protected GetContourResult() { }

	public GetContourResult(MultiLineString contour, double pointHeight) {
		this.contour = contour;
		this.pointHeight = pointHeight;
	}
	
	
}
