package com.sinergise.common.geometry.util;

import static com.sinergise.common.util.math.MathUtil.roundToNearestMultiple;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;

public class SnapToGridVisitor implements GeometryVisitor {

	private final double gridSize;
	
	public SnapToGridVisitor(double gridSize) {
		this.gridSize = gridSize;
	}

	@Override
	public void visitPoint(Point point) {
		point.x = roundToNearestMultiple(point.x, gridSize);
		point.y = roundToNearestMultiple(point.x, gridSize);
	}
	
	@Override
	public void visitLineString(LineString line) {
		for (int i=0; i<line.coords.length; i++) {
			line.coords[i] = roundToNearestMultiple(line.coords[i], gridSize);
		}
	}

	@Override
	public void visitPolygon(Polygon poly) {
		visitLineString(poly.outer);
		for (int i=0; i<poly.getNumHoles(); i++) {
			visitLineString(poly.getHole(i));
		}
	}

	@Override
	public void visitMultiPoint(MultiPoint mp) {
		visitCollection(mp);
	}

	@Override
	public void visitMultiPolygon(MultiPolygon mp) {
		visitCollection(mp);
	}

	@Override
	public void visitMultiLineString(MultiLineString mls) {
		visitCollection(mls);
	}

	@Override
	public void visitCollection(GeometryCollection<? extends Geometry> collection) {
		for (Geometry part : collection) {
			part.accept(this);
		}
	}

}
