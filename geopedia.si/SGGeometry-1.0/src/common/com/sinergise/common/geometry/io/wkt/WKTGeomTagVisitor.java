package com.sinergise.common.geometry.io.wkt;

import static com.sinergise.common.geometry.io.OgcShapeType.*;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.OgcShapeType;
import com.sinergise.common.geometry.util.GeometryVisitor;

public class WKTGeomTagVisitor implements GeometryVisitor {
	public final static String UNKNOWN = "UNKNOWN";
	
	private OgcShapeType shapeType;
	
	public String getWKTGeometryTag(Geometry geom) {
		getWktShapeType(geom);
		return shapeType == null ? UNKNOWN : shapeType.getWktTag();
	}

	public OgcShapeType getWktShapeType(Geometry geom) {
		shapeType = null;
		geom.accept(this);
		return shapeType;
	}

	@Override
	public void visitPoint(Point point) {
		shapeType = POINT;
	}

	@Override
	public void visitPolygon(Polygon poly) {
		shapeType = POLYGON;
	}

	@Override
	public void visitLineString(LineString line) {
		shapeType = LINESTRING;
	}

	@Override
	public void visitMultiPoint(MultiPoint mp) {
		shapeType = MULTIPOINT;
	}

	@Override
	public void visitMultiPolygon(MultiPolygon mp) {
		shapeType = MULTIPOLYGON;
	}

	@Override
	public void visitMultiLineString(MultiLineString mls) {
		shapeType = MULTILINESTRING;
	}

	@Override
	public void visitCollection(GeometryCollection<? extends Geometry> collection) {
		shapeType = GEOMETRYCOLLECTION;
	}

	public static String getTagFor(Geometry g) {
		return new WKTGeomTagVisitor().getWKTGeometryTag(g);
	}

	public static OgcShapeType getShapeTypeFor(Geometry g) {
		return new WKTGeomTagVisitor().getWktShapeType(g);
	}

}
