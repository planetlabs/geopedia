/*
 *
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.transform.AbstractTransform;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.geom.Point;

public abstract class LatLonToCartesian<S extends LatLonCRS, T extends CartesianCRS> extends AbstractTransform<S,T> implements ToCartesian<S,T> {
    public LatLonToCartesian(S source, T target) {
        super(source, target);
    }
    
    @Override
	public double x(double lat, double lon) {
    	return point(new Point(lat,lon)).x;
    }

    @Override
	public double y(double lat, double lon) {
    	return point(new Point(lat,lon)).y;
    }
    
}
