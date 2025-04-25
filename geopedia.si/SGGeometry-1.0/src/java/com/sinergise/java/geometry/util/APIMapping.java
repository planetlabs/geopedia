package com.sinergise.java.geometry.util;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.HasCoordinate;
import com.vividsolutions.jts.geom.Envelope;


public class APIMapping {
    public static AffineTransform toJ2D(AffineTransform2D src) {
        return new AffineTransform(src.paramsToArray());
    }
    
    public static Rectangle2D toJ2D(com.sinergise.common.util.geom.Envelope env) {
    	return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    public static Rectangle2D toJ2D(Envelope env) {
    	return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }
    
    public static AffineTransform2D fromJ2D(AffineTransform src) {
        double[] mtr = new double[6];
        src.getMatrix(mtr);
        return new AffineTransform2D(null, null, mtr);
    }

	public static Envelope toJTS(com.sinergise.common.util.geom.Envelope env) {
		if (env.isEmpty()) return new Envelope();
		return new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
	}

	public static com.sinergise.common.util.geom.Envelope fromJ2D(Rectangle2D env) {
		return new com.sinergise.common.util.geom.Envelope(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
	}

	public static Point2D toJ2D(HasCoordinate pt) {
		return new Point2D.Double(pt.x(), pt.y());
	}

	public static Rectangle toJ2D(EnvelopeI e) {
		return new Rectangle(e.minX(), e.minY(), e.getWidth(), e.getHeight());
	}

	public static com.sinergise.common.util.geom.Envelope fromJTS(Envelope env) {
		return new com.sinergise.common.util.geom.Envelope(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
	}
}
