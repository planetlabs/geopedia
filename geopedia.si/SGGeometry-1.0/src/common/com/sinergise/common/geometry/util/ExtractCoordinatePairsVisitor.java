package com.sinergise.common.geometry.util;

import java.util.Collection;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.LineSegment2D;

class ExtractCoordinatePairsVisitor implements GeometryVisitor {
	
	private final Collection<CoordinatePair> pairs;
	
	public ExtractCoordinatePairsVisitor(Collection<CoordinatePair> pairs) {
		this.pairs = pairs;
	}
	
	public Collection<CoordinatePair> getPairs() {
		return pairs;
	}
	
	@Override
	public void visitCollection(GeometryCollection<? extends Geometry> collection) {
		for (Geometry part : collection) {
			part.accept(this);
		}
	}
	
	@Override
	public void visitMultiLineString(MultiLineString mls) {
		visitCollection(mls);
	}
	
	@Override
	public void visitMultiPolygon(MultiPolygon mp) {
		visitCollection(mp);
	}
	
	@Override
	public void visitPolygon(Polygon poly) {
		poly.getOuter().accept(this);
		for (int i=0; i<poly.getNumHoles(); i++) {
			poly.getHole(i).accept(this);
		}
	}
	
	@Override
	public void visitLineString(LineString line) {
		for (int i=1; i<line.getNumCoords(); i++) {
			pairs.add(new LineSegment2D(
				line.getX(i-1),
				line.getY(i-1),
				line.getX(i),
				line.getY(i)
			));
		}
	}
	
	@Override
	public void visitMultiPoint(MultiPoint mp) { }
	
	@Override
	public void visitPoint(Point point) { }
	
}