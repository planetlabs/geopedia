/*
 *
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.transform.AbstractTransform;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.geom.Point;

public abstract class CartesianToLatLon<S extends CartesianCRS, T extends LatLonCRS> extends AbstractTransform<S,T> implements ToLatLon<S,T> {

    public CartesianToLatLon(S source, T target) {
        super(source, target);
    }
    
    @Override
	public double lat(double x, double y) {
    		return point(new Point(x,y)).x;
    }

    @Override
	public double lon(double x, double y) {
  		return point(new Point(x,y)).y;
    }
    
}
