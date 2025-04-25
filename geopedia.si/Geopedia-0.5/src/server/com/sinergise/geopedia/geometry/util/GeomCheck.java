package com.sinergise.geopedia.geometry.util;

import java.util.ArrayList;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.valid.IsValidOp;


public class GeomCheck
{
	public static String isValid(Geometry g)
	{
		try {
			com.vividsolutions.jts.geom.Geometry jtsGeom = toJts(g);
			IsValidOp ivo = new IsValidOp(jtsGeom);
			if (ivo.isValid())
				return null;
			WKTWriter wktw = new WKTWriter();
			return ivo.getValidationError().getMessage() + ", WKT: " + wktw.write(jtsGeom);
		} catch (Exception e) {
			return "Neznana napaka ("+e.getMessage()+")";
		}
	}
	
	public static void splitIfLarge(com.vividsolutions.jts.geom.Geometry g, int maxNumPoints, ArrayList<com.vividsolutions.jts.geom.Geometry> ret) {
        if (g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                splitIfLarge(g.getGeometryN(i), maxNumPoints, ret);
            }
            return;
        }
	    if (g.getNumPoints() <= maxNumPoints) {
	        ret.add(g);
	        return;
	    }
	    
	    com.vividsolutions.jts.geom.Envelope env = g.getEnvelopeInternal();
	    com.vividsolutions.jts.geom.Polygon buf = null;
	    if (env.getWidth() >= env.getHeight()) {
	        // Split horizontally
	        double midX = 0.5*(env.getMinX()+env.getMaxX());
	        buf = createJTSRectangle(env.getMinX()-1, env.getMinY()-1, midX, env.getMaxY()+1);
	    } else {
            // Split vertically
            double midY = 0.5*(env.getMinY()+env.getMaxY());
            buf = createJTSRectangle(env.getMinX()-1, env.getMinY()-1, env.getMaxX()+1, midY);
	    }
        com.vividsolutions.jts.geom.Geometry dif = g.intersection(buf);
	    com.vividsolutions.jts.geom.Geometry rem = g.difference(buf);
	    splitIfLarge(dif, maxNumPoints, ret);
	    splitIfLarge(rem, maxNumPoints, ret);
	}
	
	public static Geometry[] splitIfLarge(Geometry g, int maxNumPoints) {
	    ArrayList<com.vividsolutions.jts.geom.Geometry> tmpLst = new ArrayList<com.vividsolutions.jts.geom.Geometry>();
	    splitIfLarge(toJts(g), maxNumPoints, tmpLst);
	    Geometry[] retArr = new Geometry[tmpLst.size()];
	    for (int i = 0; i < retArr.length; i++) {
            retArr[i] = fromJts(tmpLst.get(i));
        }
	    return retArr;
	}

	
	public static com.vividsolutions.jts.geom.Polygon createJTSRectangle(double x1, double y1, double x2, double y2) {
	    return geomFactory.createPolygon(geomFactory.createLinearRing(coordSeqFac.create(new double[]{x1,y1,x1,y2,x2,y2,x2,y1,x1,y1}, 2)), null);
	}

	public static final PackedCoordinateSequenceFactory coordSeqFac = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 2);
	public static final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 1, coordSeqFac);
	
	public static com.vividsolutions.jts.geom.Geometry toJts(Geometry g)
    {
		if (g instanceof Point) {
			return toJts((Point)g);
		} else
		if (g instanceof MultiPoint) { 
			return toJts((MultiPoint)g);
		} else
		if (g instanceof LinearRing) {
			return toJts((LinearRing)g);
		} else
		if (g instanceof LineString) {
			return toJts((LineString)g);
		} else
		if (g instanceof MultiLineString) {
			return toJts((MultiLineString)g);
		} else
		if (g instanceof Polygon) {
			return toJts((Polygon)g);
		} else
		if (g instanceof MultiPolygon) {
			return toJts((MultiPolygon)g);
		} else {
			throw new IllegalStateException("Unknown geometry type");
		}
    }
	
	public static com.vividsolutions.jts.geom.Point toJts(Point p)
	{
		return geomFactory.createPoint(new Coordinate(p.x, p.y));
	}
	
	public static com.vividsolutions.jts.geom.MultiPoint toJts(MultiPoint mp)
	{
		int n = mp.size();
		com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[n];
		for (int a=0; a<n; a++) {
			points[a] = toJts(mp.get(a));
		}
		return geomFactory.createMultiPoint(points);
	}
	
	public static com.vividsolutions.jts.geom.LineString toJts(LineString ls)
	{
		return geomFactory.createLineString(coordSeqFac.create(ls.coords, 2));
	}
	
	public static com.vividsolutions.jts.geom.MultiLineString toJts(MultiLineString mls)
	{
		int n = mls.size();
		com.vividsolutions.jts.geom.LineString[] lines = new com.vividsolutions.jts.geom.LineString[n];
		for (int a=0; a<n; a++) {
			lines[a] = toJts(mls.get(a));
		}
		return geomFactory.createMultiLineString(lines);
	}
	
	public static com.vividsolutions.jts.geom.LinearRing toJts(LinearRing lr)
	{
		return geomFactory.createLinearRing(coordSeqFac.create(lr.coords, 2));
	}
	
	public static com.vividsolutions.jts.geom.Polygon toJts(Polygon p)
	{
		com.vividsolutions.jts.geom.LinearRing outer = toJts(p.getOuter());
		com.vividsolutions.jts.geom.LinearRing[] holes = new com.vividsolutions.jts.geom.LinearRing[p.getNumHoles()];
		for (int a=0; a<holes.length; a++)
			holes[a] = toJts(p.getHole(a));
		
		return geomFactory.createPolygon(outer, holes);
	}
	
	public static com.vividsolutions.jts.geom.MultiPolygon toJts(MultiPolygon mp)
	{
		int n = mp.size();
		com.vividsolutions.jts.geom.Polygon[] polys = new com.vividsolutions.jts.geom.Polygon[n];
		for (int a=0; a<n; a++) {
			polys[a] = toJts(mp.get(a));
		}
		return geomFactory.createMultiPolygon(polys);
	}
	
	public static Point fromJts(com.vividsolutions.jts.geom.Point p)
	{
		return new Point(p.getX(), p.getY());
	}
	
	public static LineString fromJts(com.vividsolutions.jts.geom.LineString ls)
	{
		int n = ls.getNumPoints();
		double[] coords = new double[n*2];
		int pos = 0;
		CoordinateSequence seq = ls.getCoordinateSequence();
		
		for (int a=0; a<n; a++) {
			coords[pos++] = seq.getX(a);
			coords[pos++] = seq.getY(a);
		}
		
		return new LineString(coords);
	}
	
	public static LinearRing fromJts(com.vividsolutions.jts.geom.LinearRing lr)
	{
		int n = lr.getNumPoints();
		double[] coords = new double[n*2];
		int pos = 0;
		CoordinateSequence seq = lr.getCoordinateSequence();
		
		for (int a=0; a<n; a++) {
			coords[pos++] = seq.getX(a);
			coords[pos++] = seq.getY(a);
		}
		
		return new LinearRing(coords);
	}
	
	public static Polygon fromJts(com.vividsolutions.jts.geom.Polygon p)
	{
		LinearRing outer = fromJts((com.vividsolutions.jts.geom.LinearRing)p.getExteriorRing());
		int nHoles = p.getNumInteriorRing();
		LinearRing[] holes = nHoles == 0 ? null : new LinearRing[nHoles];
		for (int a=0; a<nHoles; a++)
			holes[a] = fromJts((com.vividsolutions.jts.geom.LinearRing)p.getInteriorRingN(a));
		return new Polygon(outer, holes);
	}
	
	public static MultiPoint fromJts(com.vividsolutions.jts.geom.MultiPoint mp)
	{
		int n = mp.getNumGeometries();
		Point[] out = new Point[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.Point)mp.getGeometryN(a));
		return new MultiPoint(out);
	}
	
	public static MultiLineString fromJts(com.vividsolutions.jts.geom.MultiLineString mls)
	{
		int n = mls.getNumGeometries();
		LineString[] out = new LineString[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.LineString)mls.getGeometryN(a));
		return new MultiLineString(out);
	}
	
	public static MultiPolygon fromJts(com.vividsolutions.jts.geom.MultiPolygon mp)
	{
		int n = mp.getNumGeometries();
		Polygon[] out = new Polygon[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.Polygon)mp.getGeometryN(a));
		return new MultiPolygon(out);
	}
	
	public static Geometry fromWKT(String wkt) {
		try {
			return fromJts(new WKTReader().read(wkt));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static Geometry fromJts(com.vividsolutions.jts.geom.Geometry g)
	{
		if (g instanceof com.vividsolutions.jts.geom.Polygon) {
			return fromJts((com.vividsolutions.jts.geom.Polygon)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.LineString) {
			return fromJts((com.vividsolutions.jts.geom.LineString)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.Point) {
			return fromJts((com.vividsolutions.jts.geom.Point)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiPolygon) {
			return fromJts((com.vividsolutions.jts.geom.MultiPolygon)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiLineString) {
			return fromJts((com.vividsolutions.jts.geom.MultiLineString)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiPoint) {
			return fromJts((com.vividsolutions.jts.geom.MultiPoint)g);
		} else {
			throw new UnsupportedOperationException("Unknown geom type: "+g.getClass().getName());
		}
	}
}
