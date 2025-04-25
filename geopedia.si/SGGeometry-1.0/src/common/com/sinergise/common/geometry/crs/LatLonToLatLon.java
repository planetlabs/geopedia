package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.transform.AbstractTransform;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.geom.Point;

public abstract class LatLonToLatLon<S extends LatLonCRS, T extends LatLonCRS> extends AbstractTransform<S,T> implements ToLatLon<S,T> {

	public LatLonToLatLon(S src, T tgt) {
			super(src, tgt);
	}
	
	@Override
	public double lat(double srcLat, double srcLon) {
		return point(new Point(srcLat, srcLon), new Point()).x;
	}
	
	@Override
	public double lon(double srcLat, double srcLon) {
		return point(new Point(srcLat, srcLon), new Point()).y;
	}
}
