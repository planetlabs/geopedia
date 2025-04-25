package com.sinergise.java.geometry.io.wkb;

import static com.sinergise.common.geometry.io.OgcShapeType.GEOMETRYCOLLECTION;
import static com.sinergise.common.geometry.io.OgcShapeType.LINESTRING;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTILINESTRING;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTIPOINT;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTIPOLYGON;
import static com.sinergise.common.geometry.io.OgcShapeType.POINT;
import static com.sinergise.common.geometry.io.OgcShapeType.POLYGON;
import static com.sinergise.java.geometry.io.wkb.WkbByteOrder.BIG_ENDIAN;

import java.io.IOException;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.GeometryWriter;
import com.sinergise.common.geometry.io.OgcGeomType;
import com.sinergise.common.geometry.io.OgcShapeType;
import com.sinergise.common.geometry.io.wkt.WKTGeomTagVisitor;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.java.util.io.BinaryOutput;
import com.sinergise.java.util.io.ByteArrayOutputStream;

public class WkbWriter implements GeometryWriter {
	private final WKTGeomTagVisitor tagFetcher = new WKTGeomTagVisitor();
	private final boolean bigEndian;
	private final byte byteOrderVal;
	private final boolean hasZ = false;
	private final boolean hasM = false;
	private final BinaryOutput out;

	public WkbWriter() {
		this(BIG_ENDIAN);
	}

	public WkbWriter(BinaryOutput output) {
		this(output, BIG_ENDIAN);
	}
	
	public WkbWriter(WkbByteOrder byteOrder) {
		this(new ByteArrayOutputStream(), byteOrder);
	}

	public WkbWriter(BinaryOutput output, WkbByteOrder byteOrder) {
		this.bigEndian = byteOrder == BIG_ENDIAN;
		this.byteOrderVal = byteOrder.getWkbValue();
		this.out = output;
	}
	
	/**
	 * This method should not be combined with streaming mode
	 * 
	 * @param geometry
	 * @return empty byte[] for null argument; wkb representation of input otherwise
	 * @throws IOException
	 * @throws RuntimeException 
	 */
	public byte[] writeGeometry(Geometry geometry) throws RuntimeException {
		if (geometry == null) {
			return ArrayUtil.emptyByteArray;
		}
		final ByteArrayOutputStream baos = (ByteArrayOutputStream)out;
		baos.reset();
		try {
			append(geometry);
		} catch (ObjectWriteException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}
	
	@Override
	public void append(Geometry geometry) throws ObjectWriteException {
		try {
			appendUnknownGeometry(geometry);
		} catch (IOException e) {
			throw new ObjectWriteException("Unexpected error while writing geometry", e);
		}
	}
	
	private void appendUnknownGeometry(Geometry geometry) throws IOException, ObjectWriteException {
		switch (tagFetcher.getWktShapeType(geometry)) {
			case POINT: 
				appendPoint((Point)geometry);
				break;
			case LINESTRING:
				appendLineString((LineString)geometry);
				break;
			case POLYGON:
				appendPolygon((Polygon)geometry);
				break;
			case MULTIPOINT:
				appendMultiPoint((MultiPoint)geometry);
				break;
			case MULTILINESTRING:
				appendMultiLineString((MultiLineString)geometry);
				break;
			case MULTIPOLYGON:
				appendMultiPolygon((MultiPolygon)geometry);
				break;
			case GEOMETRYCOLLECTION:
				appendCollection((GeometryCollection<?>)geometry);
				break;
			default:
				throw new UnsupportedOperationException("Shape type not supported "+tagFetcher.getWktShapeType(geometry));
		}
	}
	
	protected void appendPoint(Point point) throws IOException {
		writeHeader(POINT, point);
		writeDouble(point.x());
		writeDouble(point.y());
		if (hasZ) {
			writeDouble(point.z());
		}
		if (hasM) {
			writeDouble(Double.NaN);	
		}
	}

	/**
	 * @param geom can be used by subclasses to expand header 
	 */
	private void writeHeader(OgcShapeType shapeType, Geometry geom) throws IOException {
		out.write(byteOrderVal);
		writeShapeType(shapeType, geom);
	}

	/**
	 * @param geom can be used by subclasses to expand GeomType header 
	 */
	protected void writeShapeType(OgcShapeType shapeType, Geometry geom) throws IOException {
		writeInt(OgcGeomType.toWkbHeaderInt32(shapeType, hasZ, hasM));
	}

	protected final void writeInt(int value) throws IOException {
		if (bigEndian) {
			out.writeInt(value);
		} else {
			out.writeIntLE(value);
		}
	}
	
	private void writeDouble(double value) throws IOException {
		if (bigEndian) {
			out.writeDouble(value);
		} else {
			out.writeDoubleLE(value);
		}
	}
	
	private void appendLineString(LineString lineString) throws IOException {
		writeHeader(LINESTRING, lineString);
		appendLineStringContent(lineString);
	}

	private void appendLineStringContent(LineString lineString) throws IOException {
		int nCoords = lineString.getNumCoords();
		writeInt(nCoords);
		if (nCoords > 0) {
			appendPoints(lineString.coords);
		}
	}
	
	private void appendPoints(double[] coordinates) throws IOException {
		out.writeDoubles(coordinates, 0, coordinates.length, bigEndian);
	}
	
	private void appendPolygon(Polygon polygon) throws IOException {
		int nRings = polygon.getNumRings();
		writeHeaderWithCount(POLYGON, polygon, nRings);
		if (nRings > 0) {
			appendLineStringContent(polygon.outer);
			if (polygon.holes!=null) {
				for (LinearRing hole : polygon.holes) {
					appendLineStringContent(hole);
				}
			}
		}
	}
	
	protected void appendMultiPoint(MultiPoint coll) throws IOException {
		writeHeaderWithCount(MULTIPOINT, coll, coll.size());
		for (Point g : coll) {
			appendPoint(g);
		}
	}
	
	private void appendMultiLineString(MultiLineString coll) throws IOException {
		writeHeaderWithCount(MULTILINESTRING, coll, coll.size());
		for (LineString g : coll) {
			appendLineString(g);
		}
	}
	
	private void appendMultiPolygon(MultiPolygon coll) throws IOException {
		writeHeaderWithCount(MULTIPOLYGON, coll, coll.size());
		for (Polygon g : coll) {
			appendPolygon(g);
		}
	}
	
	private void writeHeaderWithCount(OgcShapeType shapeType, Geometry geom, int count) throws IOException {
		writeHeader(shapeType, geom);
		writeInt(count);
	}
	
	private void appendCollection(GeometryCollection<?> coll) throws IOException, ObjectWriteException {
		writeHeaderWithCount(GEOMETRYCOLLECTION, coll, coll.size());
		for (Geometry g : coll) {
			appendUnknownGeometry(g);
		}
	}

	@Override
	public void close() throws IOException {
		IOUtil.close(out);
	}
	
	public static byte[] writeWkb(Geometry geom) throws RuntimeException {
		WkbWriter wkbWriter = new WkbWriter();
		try {
			return wkbWriter.writeGeometry(geom);
		} finally {
			IOUtil.closeSilent(wkbWriter);
		}
	}
}
