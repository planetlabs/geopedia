package com.sinergise.java.geometry.io.jts;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.geometry.util.APIMapping;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JtsConversionUtil {
	public static final Envelope gwtFromJTS(com.vividsolutions.jts.geom.Envelope env) {
		return new Envelope(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
	}
	
	public static final Geometry gwtFromJTS(com.vividsolutions.jts.geom.Geometry g) {
		if (g==null) throw new IllegalArgumentException("Null geometry param");
		if (g instanceof com.vividsolutions.jts.geom.Point)
			return gwtFromJTS((com.vividsolutions.jts.geom.Point)g);
		if (g instanceof com.vividsolutions.jts.geom.LineString) 
			return gwtFromJTS((com.vividsolutions.jts.geom.LineString)g);
		if (g instanceof com.vividsolutions.jts.geom.Polygon) 
			return gwtFromJTS((com.vividsolutions.jts.geom.Polygon)g);
		if (g instanceof com.vividsolutions.jts.geom.GeometryCollection)
			return gwtFromJTS((com.vividsolutions.jts.geom.GeometryCollection)g);
		throw new IllegalArgumentException("Geometry type "+g.getGeometryType()+" not supported");
	}
	
	public static final Point gwtFromJTS(com.vividsolutions.jts.geom.Point p) {
		return new Point(p.getX(), p.getY());
	}

	public static final MultiPoint gwtFromJTS(com.vividsolutions.jts.geom.MultiPoint p) {
		Point[] ret=new Point[p.getNumGeometries()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=gwtFromJTS((com.vividsolutions.jts.geom.Point)p.getGeometryN(i));
		}
		return new MultiPoint(ret);
	}

	public static final LineString gwtFromJTS(com.vividsolutions.jts.geom.LineString ls) {
		if (ls instanceof com.vividsolutions.jts.geom.LinearRing) 
			return gwtFromJTS((com.vividsolutions.jts.geom.LinearRing)ls);
		return new LineString(toArray(ls.getCoordinateSequence()));
	}
	
	public static final double[] toArray(CoordinateSequence cs) {
		double[] ret=new double[cs.size()*2];
		for (int i = 0; i < cs.size(); i++) {
			ret[2*i]=cs.getX(i);
			ret[2*i+1]=cs.getY(i);
		}
		return ret;
	}

	public static final LinearRing gwtFromJTS(com.vividsolutions.jts.geom.LinearRing lr) {
		return new LinearRing(toArray(lr.getCoordinateSequence()));
	}

	public static final MultiLineString gwtFromJTS(com.vividsolutions.jts.geom.MultiLineString p) {
		LineString[] ret=new LineString[p.getNumGeometries()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=gwtFromJTS((com.vividsolutions.jts.geom.LineString)p.getGeometryN(i));
		}
		return new MultiLineString(ret);
	}

	public static final Polygon gwtFromJTS(com.vividsolutions.jts.geom.Polygon p) {
		LinearRing outer=(LinearRing)gwtFromJTS(p.getExteriorRing());
		LinearRing[] holes=new LinearRing[p.getNumInteriorRing()];
		for (int i = 0; i < holes.length; i++) {
			holes[i]=(LinearRing)gwtFromJTS(p.getInteriorRingN(i));
		}
		return new Polygon(outer, holes);
	}

	public static final MultiPolygon gwtFromJTS(com.vividsolutions.jts.geom.MultiPolygon p) {
		Polygon[] ret=new Polygon[p.getNumGeometries()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=gwtFromJTS((com.vividsolutions.jts.geom.Polygon)p.getGeometryN(i));
		}
		return new MultiPolygon(ret);
	}

	public static final GeometryCollection<?> gwtFromJTS(com.vividsolutions.jts.geom.GeometryCollection p) {
		if (p instanceof com.vividsolutions.jts.geom.MultiPoint) {
			return gwtFromJTS((com.vividsolutions.jts.geom.MultiPoint)p);
		}
		if (p instanceof com.vividsolutions.jts.geom.MultiLineString) {
			return gwtFromJTS((com.vividsolutions.jts.geom.MultiLineString)p);
		}
		if (p instanceof com.vividsolutions.jts.geom.MultiPolygon) {
			return gwtFromJTS((com.vividsolutions.jts.geom.MultiPolygon)p);
		}
		Geometry[] ret=new Geometry[p.getNumGeometries()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=gwtFromJTS(p.getGeometryN(i));
		}
		return new GeometryCollection<Geometry>(ret);
	}
	
	private static final GeometryFactory DEFAULT_GEOM_FACTORY = new GeometryFactory();
	
	public static final com.vividsolutions.jts.geom.Geometry jtsFromGWT(Geometry g) {
		return jtsFromGWT(g, DEFAULT_GEOM_FACTORY);
	}
	
	public static final com.vividsolutions.jts.geom.Geometry jtsFromGWT(Geometry g, GeometryFactory factory) {
		if (g instanceof Point)
			return jtsFromGWT((Point)g, factory);
		if (g instanceof LineString) 
			return jtsFromGWT((LineString)g, factory);
		if (g instanceof Polygon) 
			return jtsFromGWT((Polygon)g, factory);
		if (g instanceof GeometryCollection)
			return jtsFromGWT((GeometryCollection<?>)g, factory);
		throw new IllegalArgumentException("Geometry type "+g+" not supported");
	}
	
	public static final com.vividsolutions.jts.geom.Envelope jtsFromGWT(Envelope e) {
		return APIMapping.toJTS(e);
	}
	
	public static final com.vividsolutions.jts.geom.Point jtsFromGWT(Point p) {
		return jtsFromGWT(p, DEFAULT_GEOM_FACTORY);
	}
	
	public static final com.vividsolutions.jts.geom.Point jtsFromGWT(Point p, GeometryFactory factory) {
		return factory.createPoint(new Coordinate(p.x, p.y));
	}
	
	public static final com.vividsolutions.jts.geom.MultiPoint jtsFromGWT(MultiPoint mp) {
		return jtsFromGWT(mp, DEFAULT_GEOM_FACTORY);
	}

	public static final com.vividsolutions.jts.geom.MultiPoint jtsFromGWT(MultiPoint mp, GeometryFactory factory) {
		com.vividsolutions.jts.geom.Point[] ret=new com.vividsolutions.jts.geom.Point[mp.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = jtsFromGWT(mp.get(i));
		}
		return factory.createMultiPoint(ret);
	}
	
	public static final com.vividsolutions.jts.geom.LineString jtsFromGWT(LineString ls) {
		return jtsFromGWT(ls, DEFAULT_GEOM_FACTORY);
	}

	public static final com.vividsolutions.jts.geom.LineString jtsFromGWT(LineString ls, GeometryFactory factory) {
		if (ls instanceof LinearRing) 
			return jtsFromGWT((LinearRing)ls);
		return factory.createLineString(toArray(ls.coords));
	}
	
	public static final com.vividsolutions.jts.geom.LinearRing jtsFromGWT(LinearRing lr) {
		return jtsFromGWT(lr, DEFAULT_GEOM_FACTORY);
	}
	
	public static final com.vividsolutions.jts.geom.LinearRing jtsFromGWT(LinearRing lr, GeometryFactory factory) {
		return factory.createLinearRing(toArray(lr.coords));
	}
	
	public static final com.vividsolutions.jts.geom.MultiLineString jtsFromGWT(MultiLineString mls) {
		return jtsFromGWT(mls, DEFAULT_GEOM_FACTORY);
	}

	public static final com.vividsolutions.jts.geom.MultiLineString jtsFromGWT(MultiLineString mls, GeometryFactory factory) {
		com.vividsolutions.jts.geom.LineString[] ret=new com.vividsolutions.jts.geom.LineString[mls.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = jtsFromGWT(mls.get(i));
		}
		return factory.createMultiLineString(ret);
	}
	
	public static final com.vividsolutions.jts.geom.Polygon jtsFromGWT(Polygon p) {
		return jtsFromGWT(p, DEFAULT_GEOM_FACTORY);
	}

	public static final com.vividsolutions.jts.geom.Polygon jtsFromGWT(Polygon p, GeometryFactory factory) {
		com.vividsolutions.jts.geom.LinearRing outer = jtsFromGWT(p.outer);
		com.vividsolutions.jts.geom.LinearRing[] holes = new com.vividsolutions.jts.geom.LinearRing[p.getNumHoles()];
		for (int i = 0; i < holes.length; i++) {
			holes[i] = jtsFromGWT(p.getHole(i));
		}
		return factory.createPolygon(outer, holes);
	}
	
	public static final com.vividsolutions.jts.geom.MultiPolygon jtsFromGWT(MultiPolygon mp) {
		return jtsFromGWT(mp, DEFAULT_GEOM_FACTORY);
	}

	public static final com.vividsolutions.jts.geom.MultiPolygon jtsFromGWT(MultiPolygon mp, GeometryFactory factory) {
		com.vividsolutions.jts.geom.Polygon[] ret=new com.vividsolutions.jts.geom.Polygon[mp.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=jtsFromGWT(mp.get(i));
		}
		return factory.createMultiPolygon(ret);
	}

	public static final com.vividsolutions.jts.geom.GeometryCollection jtsFromGWT(GeometryCollection<?> gc, GeometryFactory factory) {
		if (gc instanceof MultiPoint) {
			return jtsFromGWT((MultiPoint)gc);
		}
		if (gc instanceof MultiLineString) {
			return jtsFromGWT((MultiLineString)gc);
		}
		if (gc instanceof MultiPolygon) {
			return jtsFromGWT((MultiPolygon)gc);
		}
		
		com.vividsolutions.jts.geom.Geometry[] lst = new com.vividsolutions.jts.geom.Geometry[gc.size()];
		for (int i = 0; i < lst.length; i++) {
			lst[i] = jtsFromGWT(gc.get(i));
		}
		return factory.createGeometryCollection(lst);
	}
	
	public static Coordinate[] toArray(double[] coords) {
		Coordinate[] cArray = new Coordinate[coords.length/2];
		int cnt=0;
		for(int i=0; i<coords.length; i+=2) 
			cArray[cnt++] = new Coordinate(coords[i], coords[i+1]);
		return cArray;
	}
	
}
