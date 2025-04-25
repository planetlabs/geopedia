package com.sinergise.common.geometry.crs.transform;

import java.util.Collection;

import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.crs.HasCrsIdentifier;
import com.sinergise.common.util.geom.Envelope;

public class TransformUtil {

	public static void transformGeometry(Transform<?,?> tr, Geometry geom) {
		if (geom instanceof Point) {
			transformPoint(tr, (Point)geom);
		} else if (geom instanceof LineString) {
			transformLineString(tr, (LineString)geom);
		} else if (geom instanceof Polygon) {
			transformPolygon(tr, (Polygon)geom);
		} else if (geom instanceof GeometryCollection) {
			geom.setCrsId(tr.getTarget().getDefaultIdentifier());
			for (int i=0; i<((GeometryCollection<?>)geom).size(); i++) {
				transformGeometry(tr, ((GeometryCollection<?>)geom).get(i));
			}
		}
	}
	
	public static void transformPolygon(Transform<?,?> tr, Polygon p) {
		p.setCrsId(tr.getTarget().getDefaultIdentifier());
		transformLineString(tr, p.outer);
		for (int i=0; i<p.getNumHoles(); i++) {
			transformLineString(tr, p.holes[i]);
		}
	}
	
	public static void transformLineString(Transform<?,?> tr, LineString ls) {
		ls.setCrsId(tr.getTarget().getDefaultIdentifier());
		transformCoords(tr, ls.coords);
	}
	
	public static void transformPoint(Transform<?,?> tr, Point p) {
		p.setCrsId(tr.getTarget().getDefaultIdentifier());
		tr.point(p, p);
	}
	
	public static void transformGeometries(Transform<?,?> tr, Collection<? extends Geometry> geoms) {
		for (Geometry geom : geoms) {
			transformGeometry(tr, geom);
		}
	}
	
	public static void transformCoords(Transform<?,?> tr, double[] coords) {
		for (int i=0; i<coords.length;) {
			int xi = i++;
			int yi = i++;
			Point p = tr.point(new Point(coords[xi], coords[yi]), new Point());
			
			coords[xi] = p.x;
			coords[yi] = p.y;
		}
	}
	
	public static Envelope transformEnvelope(Transform<?,?> tr, Envelope env) {
		if (tr instanceof EnvelopeTransform) {
			return ((EnvelopeTransform)tr).envelope(env);
		}
		
		Point minPt = new Point(env.getMinX(), env.getMinY());
		Point maxPt = new Point(env.getMaxX(), env.getMaxY());
		tr.point(minPt, minPt);
		tr.point(maxPt, maxPt);
		
		return new Envelope(minPt.x(), minPt.y(), maxPt.x(), maxPt.y(), tr.getTarget().getDefaultIdentifier());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends HasCrsIdentifier> T transformCoordinates(Transform<?,?> tr, T source) {
		if (source instanceof Geometry) {
			transformGeometry(tr, (Geometry)source);
			return source;
		} else if (source instanceof Envelope) {
			return (T)transformEnvelope(tr, (Envelope)source);
		}
		throw new IllegalArgumentException("Unknown source: "+source);
	}
}
