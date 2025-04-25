package com.sinergise.common.geometry.util;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;

public interface GeometryVisitor {
	
	void visitPoint(Point point);
	
	void visitPolygon(Polygon poly);
	
	void visitLineString(LineString line);
	
	void visitMultiPoint(MultiPoint mp);
	
	void visitMultiPolygon(MultiPolygon mp);
	
	void visitMultiLineString(MultiLineString mls);
	
	void visitCollection(GeometryCollection<? extends Geometry> collection);

}
