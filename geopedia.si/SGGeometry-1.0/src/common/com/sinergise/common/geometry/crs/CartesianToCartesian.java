/*
 *
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.transform.AbstractTransform;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.geom.Point;

public abstract class CartesianToCartesian<S extends CartesianCRS, T extends CartesianCRS> extends AbstractTransform<S,T> implements ToCartesian<S,T> {

    public CartesianToCartesian(S source, T target) {
        super(source, target);
    }
    @Override
	public double x(double x, double y) {
    	return point(new Point(x,y)).x;
    }

    @Override
	public double y(double x, double y) {
    	return point(new Point(x,y)).y;
    }
    
}
