package com.sinergise.geopedia.db.geometry;


import static com.sinergise.common.util.crs.CrsIdentifier.getSrid;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.util.io.ByteArrayOutputStream;

public class WkbWriter
{
	/** tag for big-endian encoding */
	public static final byte ENC_XDR = 0;
	
	/** tag for little-endian encoding */
	public static final byte ENC_NDR = 1;
	
	public static final int T_GEOMETRY = 0;
	public static final int T_POINT = 1;
	public static final int T_LINESTRING = 2;
	public static final int T_POLYGON = 3;
	public static final int T_MULTIPOINT = 4;
	public static final int T_MULTILINESTRING = 5;
	public static final int T_MULTIPOLYGON = 6;
	public static final int T_GEOMETRYCOLLECTION = 7;
	public static final int T_CIRCULARSTRING = 8;
	public static final int T_COMPOUNDCURVE = 9;
	public static final int T_CURVEPOLYGON = 10;
	public static final int T_MULTICURVE = 11;
	public static final int T_MULTISURFACE = 12;
	public static final int T_CURVE = 13;
	public static final int T_SURFACE = 14;
	public static final int T_POLYHEDRALSURFACE = 15;
	public static final int T_TIN = 16;
	
	public static final int T_COORDS_ARE_XY = 0;
	public static final int T_COORDS_ARE_XYZ = 1000;
	public static final int T_COORDS_ARE_XYM = 2000;
	public static final int T_COORDS_ARE_XYZM = 3000;
	
	
	public static byte[] toMySqlInternal(Geometry g)
	{
		if (g == null)
			return null;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.writeIntLE(getSrid(g.getCrsId()));
		
		writeWkbGeometryLE(baos, g);
		
		return baos.toByteArray();
	}
	
	public static void writePoint(ByteArrayOutputStream baos, double x, double y)
	{
		baos.writeDouble(x);
		baos.writeDouble(y);
	}
	
	public static void writeLinearRing(ByteArrayOutputStream baos, Point[] points)
	{
		int len = points.length;
		baos.writeInt(len);
		for (int a = 0; a < len; a++) {
			baos.writeDouble(points[a].x);
			baos.writeDouble(points[a].y);
		}
	}
	
	public static void writeLinearRing(ByteArrayOutputStream baos, double[] packedXYs)
	{
		int len = packedXYs.length;
		baos.writeInt(len >>> 1);
		
		for (int pos = 1; pos < len; pos += 2) {
			baos.writeDouble(packedXYs[pos - 1]);
			baos.writeDouble(packedXYs[pos    ]);
		}
	}
	
	public static void writeWkbPoint(ByteArrayOutputStream baos, Point p)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_POINT);
		writePoint(baos, p.x, p.y);
	}
	
	public static void writeWkbLineString(ByteArrayOutputStream baos, LineString ls)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_LINESTRING);
		
		int nCoords = ls.getNumCoords();
		baos.writeInt(nCoords);
		for (int a=0; a<nCoords; a++) {
			writePoint(baos, ls.getX(a), ls.getY(a));
		}
	}
	
	public static void writeWkbPolygon(ByteArrayOutputStream baos, Polygon p)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_POLYGON);
		
		int nHoles = p.getNumHoles();
		baos.writeInt(nHoles + 1);
		writeLinearRing(baos, p.outer.coords);
		for (int a=0; a<nHoles; a++)
			writeLinearRing(baos, p.getHole(a).coords);
	}
	
	public static void writeWkbMultiPoint(ByteArrayOutputStream baos, MultiPoint mp)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_MULTIPOINT);
		
		int nPoints = mp.size();
		baos.writeInt(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbPoint(baos, mp.get(a));
	}
	
	public static void writeWkbMultiLineString(ByteArrayOutputStream baos, MultiLineString mp)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_MULTILINESTRING);
		
		int nPoints = mp.size();
		baos.writeInt(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbLineString(baos, mp.get(a));
	}
	
	public static void writeWkbMultiPolygon(ByteArrayOutputStream baos, MultiPolygon mp)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_MULTIPOLYGON);
		
		int nPoints = mp.size();
		baos.writeInt(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbPolygon(baos, mp.get(a));
	}
	
	public static void writeWkbGeometryCollection(ByteArrayOutputStream baos, GeometryCollection<Geometry> mp)
	{
		baos.write(ENC_XDR);
		baos.writeInt(T_GEOMETRYCOLLECTION);
		
		int nPoints = mp.size();
		baos.writeInt(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbGeometry(baos, mp.get(a));
	}
	
	public static void writeWkbGeometry(ByteArrayOutputStream baos, Geometry g)
	{
		if (g == null) {
			// XXX TODO check this - is an empty geometry collection equivalent to null?
			// OTOH, for MySQL we produce null already in toMySqlInternal
			baos.write(ENC_XDR);
			baos.writeInt(T_GEOMETRYCOLLECTION);
			baos.writeInt(0);
			return;
		}
		
		if (g instanceof GeometryCollection) {
			if (g instanceof MultiPolygon) {
				writeWkbMultiPolygon(baos, (MultiPolygon) g);
			} else
			if (g instanceof MultiLineString) {
				writeWkbMultiLineString(baos, (MultiLineString) g);
			} else
			if (g instanceof MultiPoint) {
				writeWkbMultiPoint(baos, (MultiPoint) g);
			} else {
				writeWkbGeometryCollection(baos, (GeometryCollection) g);
			}
		} else {
			if (g instanceof Polygon) {
				writeWkbPolygon(baos, (Polygon) g);
			} else
			if (g instanceof LineString) {
				writeWkbLineString(baos, (LineString) g);
			} else
			if (g instanceof Point) {
				writeWkbPoint(baos, (Point) g);
			} else {
				throw new IllegalStateException("Unknown type of geometry: "+g.getClass().getName());
			}
		}
	}
	
	public static void writePointLE(ByteArrayOutputStream baos, double x, double y)
	{
		baos.writeDoubleLE(x);
		baos.writeDoubleLE(y);
	}
	
	public static void writeLinearRingLE(ByteArrayOutputStream baos, Point[] points)
	{
		int len = points.length;
		baos.writeIntLE(len);
		for (int a = 0; a < len; a++) {
			baos.writeDoubleLE(points[a].x);
			baos.writeDoubleLE(points[a].y);
		}
	}
	
	public static void writeLinearRingLE(ByteArrayOutputStream baos, double[] packedXYs)
	{
		int len = packedXYs.length;
		baos.writeIntLE(len >>> 1);
		
		for (int pos = 1; pos < len; pos += 2) {
			baos.writeDoubleLE(packedXYs[pos - 1]);
			baos.writeDoubleLE(packedXYs[pos    ]);
		}
	}
	
	public static void writeWkbPointLE(ByteArrayOutputStream baos, Point p)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_POINT);
		writePointLE(baos, p.x, p.y);
	}
	
	public static void writeWkbLineStringLE(ByteArrayOutputStream baos, LineString ls)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_LINESTRING);
		
		int nCoords = ls.getNumCoords();
		baos.writeIntLE(nCoords);
		for (int a=0; a<nCoords; a++) {
			writePointLE(baos, ls.getX(a), ls.getY(a));
		}
	}
	
	public static void writeWkbPolygonLE(ByteArrayOutputStream baos, Polygon p)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_POLYGON);
		
		int nHoles = p.getNumHoles();
		baos.writeIntLE(nHoles + 1);
		writeLinearRingLE(baos, p.outer.coords);
		for (int a=0; a<nHoles; a++)
			writeLinearRingLE(baos, p.getHole(a).coords);
	}
	
	public static void writeWkbMultiPointLE(ByteArrayOutputStream baos, MultiPoint mp)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_MULTIPOINT);
		
		int nPoints = mp.size();
		baos.writeIntLE(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbPointLE(baos, mp.get(a));
	}
	
	public static void writeWkbMultiLineStringLE(ByteArrayOutputStream baos, MultiLineString mp)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_MULTILINESTRING);
		
		int nPoints = mp.size();
		baos.writeIntLE(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbLineStringLE(baos, mp.get(a));
	}
	
	public static void writeWkbMultiPolygonLE(ByteArrayOutputStream baos, MultiPolygon mp)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_MULTIPOLYGON);
		
		int nPoints = mp.size();
		baos.writeIntLE(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbPolygonLE(baos, mp.get(a));
	}
	
	public static void writeWkbGeometryCollectionLE(ByteArrayOutputStream baos, GeometryCollection mp)
	{
		baos.write(ENC_NDR);
		baos.writeIntLE(T_GEOMETRYCOLLECTION);
		
		int nPoints = mp.size();
		baos.writeIntLE(nPoints);
		for (int a=0; a<nPoints; a++)
			writeWkbGeometryLE(baos, mp.get(a));
	}
	
	public static void writeWkbGeometryLE(ByteArrayOutputStream baos, Geometry g)
	{
		if (g == null) {
			// XXX TODO check this - is an empty geometry collection equivalent to null?
			// OTOH, for MySQL we produce null already in toMySqlInternal
			baos.write(ENC_NDR);
			baos.writeIntLE(T_GEOMETRYCOLLECTION);
			baos.writeIntLE(0);
			return;
		}
		
		if (g instanceof GeometryCollection) {
			if (g instanceof MultiPolygon) {
				writeWkbMultiPolygonLE(baos, (MultiPolygon) g);
			} else
			if (g instanceof MultiLineString) {
				writeWkbMultiLineStringLE(baos, (MultiLineString) g);
			} else
			if (g instanceof MultiPoint) {
				writeWkbMultiPointLE(baos, (MultiPoint) g);
			} else {
				writeWkbGeometryCollectionLE(baos, (GeometryCollection) g);
			}
		} else {
			if (g instanceof Polygon) {
				writeWkbPolygonLE(baos, (Polygon) g);
			} else
			if (g instanceof LineString) {
				writeWkbLineStringLE(baos, (LineString) g);
			} else
			if (g instanceof Point) {
				writeWkbPointLE(baos, (Point) g);
			} else {
				throw new IllegalStateException("Unknown type of geometry: "+g.getClass().getName());
			}
		}
	}
}
