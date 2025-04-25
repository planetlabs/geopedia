/**
 * 
 */
package com.sinergise.common.geometry.crs.transform;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;

public interface Transform<S extends CRS, T extends CRS> {
	public static interface InvertibleTransform<SS extends CRS, TT extends CRS> extends Transform<SS, TT> {
		InvertibleTransform<TT, SS> inverse();
	}
	public static interface EnvelopeTransform {
		Envelope envelope(Envelope src);
	}
	Point point(Point src, Point ret);
	S getSource();
	T getTarget();
	String getName();
}