package com.sinergise.geopedia.db.geometry;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.util.io.ByteArrayInputStream;

public final class WkbReader
{
	private static final Logger logger = LoggerFactory.getLogger(WkbReader.class);
	
	static final int ENC_NDR = WkbWriter.ENC_NDR;
	static final int ENC_XDR = WkbWriter.ENC_XDR;
	static final int T_GEOMETRYCOLLECTION = WkbWriter.T_GEOMETRYCOLLECTION;
	static final int T_GEOMETRY = WkbWriter.T_GEOMETRY;
	static final int T_LINESTRING = WkbWriter.T_LINESTRING;
	static final int T_MULTILINESTRING = WkbWriter.T_MULTILINESTRING;
	static final int T_MULTIPOINT = WkbWriter.T_MULTIPOINT;
	static final int T_MULTIPOLYGON = WkbWriter.T_MULTIPOLYGON;
	static final int T_POINT = WkbWriter.T_POINT;
	static final int T_POLYGON = WkbWriter.T_POLYGON;
	
	private double offsetX = 0, offsetY = 0, scaleX = 1, scaleY = 1;
	public EnvelopeBuilder envBuilder = new EnvelopeBuilder();
	private CrsIdentifier defaultCrsIdentifier;
	
	public WkbReader(CrsIdentifier defaultCrsIdentifier) {
		this.defaultCrsIdentifier=defaultCrsIdentifier;
	}
	
	public void resetBounds()
	{
		envBuilder.clear();
	}
	
	public Geometry fromMySqlInternal(byte[] bytes) throws IOException
	{
		if (bytes == null || bytes.length == 0)
			return null;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		
		int srid;
		srid = bais.readIntLE();
		
		Geometry g = readWKBGeometry(bais);
		
		if (g != null) {
			if (srid<=0) {
				g.setCrsId(defaultCrsIdentifier);
			} else {
				g.setCrsId(CrsRepository.INSTANCE.getDefaultIdentifier(new CrsIdentifier(CrsAuthority.MYSQL, srid)));
			}
		}
		
		return g;	
	}
	
	public Geometry readWKBGeometry(ByteArrayInputStream bais) throws IOException
	{
		int enc = bais.read();
		
		if (enc == ENC_NDR) {
			return readWKBGeometryRestLE(bais);
		} else
		if (enc == ENC_XDR) {
			return readWKBGeometryRest(bais);
		} else {
			throw new IOException("Unknown encoding ("+enc+")");
		}
	}
	
	public Geometry readWKBGeometryRest(ByteArrayInputStream bais) throws IOException
	{
		int type = bais.readInt();
		
		switch(type) {
		case T_GEOMETRYCOLLECTION:
			return readWKBGeometryCollectionNoHead(bais);
		case T_LINESTRING:
			return readWKBLineStringNoHead(bais);
		case T_MULTILINESTRING:
			return readWKBMultiLineStringNoHead(bais);
		case T_MULTIPOINT:
			return readWKBMultiPointNoHead(bais);
		case T_MULTIPOLYGON:
			return readWKBMultiPolygonNoHead(bais);
		case T_POINT:
			return readWKBPointNoHead(bais);
		case T_POLYGON:
			return readWKBPolygonNoHead(bais);
		default:
			throw new IOException("Unsupported WKB type "+type);
		}
	}
	
	public GeometryCollection readWKBGeometryCollectionNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nGeoms = bais.readInt();
		
		Geometry[] gs = new Geometry[nGeoms];
		
		for (int a=0; a<nGeoms; a++)
			gs[a] = readWKBGeometry(bais);
		
		return makeGeomColl(gs);
	}
	
	public LineString readWKBLineStringNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nPoints = bais.readInt();
		
		double[] coords = new double[nPoints * 2];
		int last = coords.length;
		double x, y;
		for (int a=0; a<last; ) {
			x = bais.readDouble() * scaleX + offsetX;
			y = bais.readDouble() * scaleY + offsetY;
			
			envBuilder.expandToInclude(x, y);
				
			coords[a++] = x;
			coords[a++] = y;
		}
		
		if (nPoints > 0 && coords[0] == coords[last-2] && coords[1] == coords[last-1]) {
			return new LinearRing(coords);
		}
		return new LineString(coords);
	}
	
	public Point readWKBPointNoHead(ByteArrayInputStream bais) throws IOException
	{
		double x = bais.readDouble() * scaleX + offsetX;
		double y = bais.readDouble() * scaleY + offsetY;
		envBuilder.expandToInclude(x, y);
		return new Point(x, y);
	}
	
	public Polygon readWKBPolygonNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nRings = bais.readInt();
		if (nRings < 1)
			return new Polygon();
		
		boolean unclosed = false;
		
		LinearRing outer = null;
		LinearRing[] holes = nRings == 1 ? null : new LinearRing[nRings - 1];
		
		for (int a=0; a<nRings; a++) {
			int nCoords = bais.readInt();
			int nOrdinates = nCoords * 2;
			double[] xys = new double[nOrdinates];
			double x, y;
			for (int b=0; b<nOrdinates; ) {
				x = bais.readDouble() * scaleX + offsetX;
				y = bais.readDouble() * scaleY + offsetY;
				envBuilder.expandToInclude(x, y);
				xys[b++] = x;
				xys[b++] = y;
			}
			
			LinearRing ring;
			
			if (nCoords > 0 && (xys[0] != xys[nOrdinates-2] || xys[1] != xys[nOrdinates-1])) {
				unclosed = true;
				ring = null;
			} else {
				ring = new LinearRing(xys);
			}
			
			if (a == 0) {
				outer = ring;
			} else {
				holes[a-1] = ring;
			}
		}
		
		// read the whole thing anyway, who knows what follows
		if (unclosed)
			throw new IOException("Invalid polygon - unclosed ring");
		
		return new Polygon(outer, holes);
	}
	
	public MultiLineString readWKBMultiLineStringNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nLines = bais.readInt();
		if (nLines < 1)
			return new MultiLineString();
		
		boolean failed = false;
		LineString[] lines = new LineString[nLines];
		for (int a=0; a<nLines; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof LineString)) {
				failed = true;
			} else {
				lines[a] = (LineString) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multilinestring - one of the geometries wasn't a linestring");
		
		return new MultiLineString(lines);
	}
	
	public MultiPoint readWKBMultiPointNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nPoints = bais.readInt();
		if (nPoints < 1)
			return new MultiPoint();
		
		boolean failed = false;
		Point[] points = new Point[nPoints];
		for (int a=0; a<nPoints; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof Point)) {
				failed = true;
			} else {
				points[a] = (Point) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multipoint - one of the geometries wasn't a point");
		
		return new MultiPoint(points);
	}
	
	public MultiPolygon readWKBMultiPolygonNoHead(ByteArrayInputStream bais) throws IOException
	{
		int nPolygons = bais.readInt();
		if (nPolygons < 1)
			return new MultiPolygon();
		
		boolean failed = false;
		Polygon[] polygons = new Polygon[nPolygons];
		for (int a=0; a<nPolygons; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof Polygon)) {
				failed = true;
			} else {
				polygons[a] = (Polygon) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multipolygon - one of the geometries wasn't a polygon");
		
		return new MultiPolygon(polygons);
	}
	
	public Geometry readWKBGeometryRestLE(ByteArrayInputStream bais) throws IOException
	{
		int type = bais.readIntLE();
		
		switch(type) {
		case T_GEOMETRYCOLLECTION:
			return readWKBGeometryCollectionNoHeadLE(bais);
		case T_LINESTRING:
			return readWKBLineStringNoHeadLE(bais);
		case T_MULTILINESTRING:
			return readWKBMultiLineStringNoHeadLE(bais);
		case T_MULTIPOINT:
			return readWKBMultiPointNoHeadLE(bais);
		case T_MULTIPOLYGON:
			return readWKBMultiPolygonNoHeadLE(bais);
		case T_POINT:
			return readWKBPointNoHeadLE(bais);
		case T_POLYGON:
			return readWKBPolygonNoHeadLE(bais);
		default:
			throw new IOException("Unsupported WKB type "+type);
		}
	}
	
	public GeometryCollection readWKBGeometryCollectionNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nGeoms = bais.readIntLE();
		
		Geometry[] gs = new Geometry[nGeoms];
		
		for (int a=0; a<nGeoms; a++)
			gs[a] = readWKBGeometry(bais);
		
		return makeGeomColl(gs);
	}
	
	static GeometryCollection makeGeomColl(Geometry[] gs)
	{
		int nPoints = 0;
		int nLines = 0;
		int nPolygons = 0;
		
		for (Geometry g : gs) {
			if (g instanceof Point) {
				nPoints++;
			} else
			if (g instanceof MultiPoint){ 
				nPoints += ((MultiPoint)g).size();
			} else
			if (g instanceof LineString) {
				nLines++;
			} else
			if (g instanceof MultiLineString){ 
				nLines += ((MultiLineString)g).size();
			} else
			if (g instanceof Polygon) {
				nPolygons++;
			} else
			if (g instanceof MultiPolygon) { 
				nPolygons += ((MultiPolygon)g).size();
			} else {
				throw new IllegalStateException("Unknown geometry type");
			}
		}
		
		int howMany = 0;
		if (nPoints > 0) howMany++;
		if (nLines > 0) howMany++;
		if (nPolygons > 0) howMany++;
		
		if (howMany == 0)
			throw new IllegalStateException("Empty collections not supported");
		if (howMany > 1)
			throw new IllegalStateException("Heterogenous collections not supported");
		
		if (nPoints > 0) {
			Point[] out = new Point[nPoints];
			int pos = 0;
			for (Geometry g : gs) {
				if (g instanceof Point) {
					out[pos++] = (Point) g;
				} else {
					MultiPoint mp = (MultiPoint) g;
					int n = mp.size();
					for (int a=0; a<n; a++)
						out[pos++] = mp.get(a);
				}
			}
			return new MultiPoint(out);
		} else
		if (nLines > 0) {
			LineString[] out = new LineString[nPoints];
			int pos = 0;
			for (Geometry g : gs) {
				if (g instanceof LineString) {
					out[pos++] = (LineString) g;
				} else {
					MultiLineString mp = (MultiLineString) g;
					int n = mp.size();
					for (int a=0; a<n; a++)
						out[pos++] = mp.get(a);
				}
			}
			return new MultiLineString(out);
		} else {
			Polygon[] out = new Polygon[nPoints];
			int pos = 0;
			for (Geometry g : gs) {
				if (g instanceof Polygon) {
					out[pos++] = (Polygon) g;
				} else {
					MultiPolygon mp = (MultiPolygon) g;
					int n = mp.size();
					for (int a=0; a<n; a++)
						out[pos++] = mp.get(a);
				}
			}
			return new MultiPolygon(out);
		}
	}
	
	public LineString readWKBLineStringNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nPoints = bais.readIntLE();
		
		double[] coords = new double[nPoints * 2];
		int last = coords.length;
		double x, y;
		for (int a=0; a<last; ) {
			x = bais.readDoubleLE() * scaleX + offsetX;
			y = bais.readDoubleLE() * scaleY + offsetY;
			envBuilder.expandToInclude(x, y);
			coords[a++] = x;
			coords[a++] = y;
		}
		
		if (nPoints > 0 && coords[0] == coords[last-2] && coords[1] == coords[last-1]) {
			return new LinearRing(coords);
		}
		return new LineString(coords);
	}
	
	public Point readWKBPointNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		double x = bais.readDoubleLE() * scaleX + offsetX;
		double y = bais.readDoubleLE() * scaleY + offsetY;
		envBuilder.expandToInclude(x, y);
		return new Point(x, y);
	}
	
	public Polygon readWKBPolygonNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nRings = bais.readIntLE();
		if (nRings < 1)
			return new Polygon();
		
		boolean unclosed = false;
		
		LinearRing outer = null;
		LinearRing[] holes = nRings == 1 ? null : new LinearRing[nRings - 1];
		
		for (int a=0; a<nRings; a++) {
			int nCoords = bais.readIntLE();
			int nOrdinates = nCoords * 2;
			double[] xys = new double[nOrdinates];
			double x, y;
			for (int b=0; b<nOrdinates; ) {
				x = bais.readDoubleLE() * scaleX + offsetX;
				y = bais.readDoubleLE() * scaleY + offsetY;
				envBuilder.expandToInclude(x, y);
				xys[b++] = x;
				xys[b++] = y;
			}
			
			LinearRing ring;
			
			if (nCoords > 0 && (xys[0] != xys[nOrdinates-2] || xys[1] != xys[nOrdinates-1])) {
				unclosed = true;
				ring = null;
			} else {
				ring = new LinearRing(xys);
			}
			
			if (a == 0) {
				outer = ring;
			} else {
				holes[a-1] = ring;
			}
		}
		
		// read the whole thing anyway, who knows what follows
		if (unclosed)
			throw new IOException("Invalid polygon - unclosed ring");
		
		return new Polygon(outer, holes);
	}
	
	public MultiLineString readWKBMultiLineStringNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nLines = bais.readIntLE();
		if (nLines < 1)
			return new MultiLineString();
		
		boolean failed = false;
		LineString[] lines = new LineString[nLines];
		for (int a=0; a<nLines; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof LineString)) {
				failed = true;
			} else {
				lines[a] = (LineString) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multilinestring - one of the geometries wasn't a linestring");
		
		return new MultiLineString(lines);
	}
	
	public MultiPoint readWKBMultiPointNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nPoints = bais.readIntLE();
		if (nPoints < 1)
			return new MultiPoint();
		
		boolean failed = false;
		Point[] points = new Point[nPoints];
		for (int a=0; a<nPoints; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof Point)) {
				failed = true;
			} else {
				points[a] = (Point) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multipoint - one of the geometries wasn't a point");
		
		return new MultiPoint(points);
	}
	
	public MultiPolygon readWKBMultiPolygonNoHeadLE(ByteArrayInputStream bais) throws IOException
	{
		int nPolygons = bais.readIntLE();
		if (nPolygons < 1)
			return new MultiPolygon();
		
		boolean failed = false;
		Polygon[] polygons = new Polygon[nPolygons];
		for (int a=0; a<nPolygons; a++) {
			Geometry g = readWKBGeometry(bais);
			if (g == null || !(g instanceof Polygon)) {
				failed = true;
			} else {
				polygons[a] = (Polygon) g;
			}
		}
		
		if (failed)
			throw new IOException("Invalid multipolygon - one of the geometries wasn't a polygon");
		
		return new MultiPolygon(polygons);
	}

	public void setTransform(double scaleX, double scaleY, double offsetX, double offsetY)
    {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
    }
	
	public double calcX(double x)
	{
		return x * scaleX + offsetX;
	}
	
	public double calcY(double y)
	{
		return y * scaleY + offsetY;
	}

	public Envelope getBounds() {
		return envBuilder.getEnvelope();
	}
}
