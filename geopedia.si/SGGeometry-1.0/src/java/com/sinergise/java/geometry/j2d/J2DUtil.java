package com.sinergise.java.geometry.j2d;

import static java.awt.geom.Path2D.WIND_EVEN_ODD;
import static java.awt.geom.Path2D.WIND_NON_ZERO;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.Envelope;


// TODO: rename, refactor to instance 
public class J2DUtil {
	private static final class TmpPoint extends ThreadLocal<Point> {
		public TmpPoint() {}

		@Override
		protected Point initialValue() {
			return new Point();
		}
	}

	private static final TmpPoint tmpPt1 = new TmpPoint();
	private static final TmpPoint tmpPt2 = new TmpPoint();

	public static GeneralPath toShape(final Geometry geom) {
		return toShape(geom, null);
	}
	
	public static GeneralPath toShape(final Geometry geom, final Transform<?, ?> trans) {
		if (geom == null)
			return null;
		final Point tmp1 = tmpPt1.get();
		final Point tmp2 = tmpPt2.get();
		final GeneralPath path = new GeneralPath(WIND_EVEN_ODD);
		addToPath(path, geom, trans, tmp1, tmp2);
		return path;
	}
	
	private static void addToPath(final GeneralPath path, final Geometry geom, final Transform<?, ?> trans,
		final Point tmp1, final Point tmp2) {
		if (geom == null)
			return;
		if (geom instanceof Polygon) {
			Polygon poly = (Polygon)geom;
			addToPath(path, poly.outer, trans, tmp1, tmp2);
			if (poly.getNumHoles() > 0) {
				for (LineString hole : poly.holes) {
					addToPath(path, hole, trans, tmp1, tmp2);
				}
			}
		} else if (geom instanceof LineString) {
			addToPath(path, (LineString)geom, trans, tmp1, tmp2);
		} else if (geom instanceof GeometryCollection<?>) {
			for (Geometry gPart : (GeometryCollection<?>)geom) {
				addToPath(path, gPart, trans, tmp1, tmp2);
			}
		} else {
			throw new UnsupportedOperationException("Geometry type not supported: " + geom.getClass());
		}
	}
	
	public static final void addToPath(final GeneralPath ret, final LineString ls, final Transform<?, ?> trans,
		final Point tmp1, final Point tmp2) {
		final double[] ringCrd = ls.coords;
		final int crdLen = ringCrd.length;

		if (trans!=null) { // transform
			int i = 0;
			trans.point(tmp1.setLocation(ringCrd[i++], ringCrd[i++]), tmp2);
			ret.moveTo(tmp2.x, tmp2.y);
			while (i < crdLen) {
				trans.point(tmp1.setLocation(ringCrd[i++], ringCrd[i++]), tmp2);
				ret.lineTo(tmp2.x, tmp2.y);
			}
		} else {  // no transform
			int i = 0;
			ret.moveTo(ringCrd[i++], ringCrd[i++]);
			while (i < crdLen) {
				ret.lineTo(ringCrd[i++], ringCrd[i++]);
			}
		}
	}

	public static Shape toShape(Envelope e, AffineTransform2D trans) {
		final GeneralPath ret = new GeneralPath(WIND_NON_ZERO);
		final Point tmp1 = tmpPt1.get();
		final Point tmp2 = tmpPt2.get();

		double minX = e.getMinX();
		double minY = e.getMinY();
		double maxX = e.getMaxX();
		double maxY = e.getMaxY();

		trans.point(tmp1.setLocation(minX, minY), tmp2);
		ret.moveTo(tmp2.x, tmp2.y);
		trans.point(tmp1.setLocation(maxX, minY), tmp2);
		ret.lineTo(tmp2.x, tmp2.y);
		trans.point(tmp1.setLocation(maxX, maxY), tmp2);
		ret.lineTo(tmp2.x, tmp2.y);
		trans.point(tmp1.setLocation(minX, maxY), tmp2);
		ret.lineTo(tmp2.x, tmp2.y);
		ret.closePath();
		return ret;
	}
}
