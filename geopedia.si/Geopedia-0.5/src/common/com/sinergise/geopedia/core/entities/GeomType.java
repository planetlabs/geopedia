package com.sinergise.geopedia.core.entities;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;


public enum GeomType {
	POINTS(3), LINES(2), POLYGONS(1), POINTS_M(5), LINES_M(6), POLYGONS_M(7), NONE(0);
	private int identifier;
	private GeomType(int identifier) {
		this.identifier=identifier;
	}
	
	public static GeomType forId(int identifier) {
		for (GeomType gt:GeomType.values()) {
			if (gt.identifier == identifier)
				return gt;
		}
		return NONE;
	}
	
	public static GeomType forGeometry(Geometry g)
	{
		if (g == null)
			return NONE;
		
		if (g instanceof Point)
			return POINTS;
		if (g instanceof MultiPoint)
			return POINTS_M;
		if (g instanceof LineString)
			return LINES;
		if (g instanceof MultiLineString)
			return LINES_M;
		if (g instanceof Polygon)
			return POLYGONS;
		if (g instanceof MultiPolygon)
			return POLYGONS_M;
			
		throw new IllegalStateException();
	}
	
	
	public boolean isMulti() {
		return false;
	}
	public boolean isGeom() {
		return isGeom(this);
	}
	public static boolean isGeom(GeomType type) {
		if (type==NONE)
			return false;
		return true;
	}

	public int getIdentifier() {
		return identifier;
	}

	public boolean isPolygon() {
		if (this==POLYGONS || this == POLYGONS_M)
			return true;
		return false;
	}
	
	public boolean isCodelist() {
		if (this==NONE)
			return true;
		return false;
	}

	public boolean isLine() {
		if (this==LINES || this == LINES_M) 
			return true;
		return false;
	}
	
	public boolean isPoint() {
		if (this==POINTS || this == POINTS_M)
			return true;
		return false;
	}
}
