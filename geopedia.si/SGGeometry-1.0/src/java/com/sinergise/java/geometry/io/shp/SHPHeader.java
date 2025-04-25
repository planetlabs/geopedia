package com.sinergise.java.geometry.io.shp;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;



//TODO: Add _M types (x, y, optional m); 
//TODO: Make it clear that _Z types can include m (x,y,z,optional m)
//TODO: Shapefile doesn't allow NaN, +-inf; measures can have NO_DATA value, which is anything smaller than -10^38
public class SHPHeader {
	public static enum ShapefileType {
		NULL(0), 
		POINT(1),
		POLYLINE(3),
		POLYGON(5),
		MULTIPOINT(8),
		POINT_Z(11),
		POLYLINE_Z(13),
		POLYGON_Z(15),
		MULTIPOINT_Z(18),
		POINT_M(21),
		POLYLINE_M(23),
		POLYGON_M(25),
		MULTIPOINT_M(28),
		MULTIPATCH(31);
		
		int val;
		private ShapefileType(int value) {
			this.val = value;
		}
		
		public static ShapefileType valueOf(int val) {
			for (ShapefileType t : values()) {
				if (t.val == val) {
					return t;
				}
			}
			throw new IllegalArgumentException("Unknown shapefile type: "+val);
		}
		
		public static ShapefileType forGeometry(Geometry g) {
			if (g == null) {
				return NULL;
			}
			if (g instanceof MultiPoint) {
				return MULTIPOINT;
			}
			if (g instanceof Point) {
				return POINT;
			}
			if (g instanceof Polygon || g instanceof MultiPolygon) {
				return POLYGON;
			}
			if (g instanceof MultiLineString || g instanceof LineString) {
				return POLYLINE;
			}
			throw new IllegalArgumentException("Unsupported geometry type: "+g);
		}

		public int getValue() {
			return val;
		}
	}
	public static final int FILE_CODE = 9994;
	public static final int VERSION = 1000;
	
	private int fileLengthIn16bitWords = 0;	
	private ShapefileType shapeType = null;
	private double xmin=0;
	private double xmax=0;
	private double ymin=0;
	private double ymax=0;
	
	
	public int getFileLengthIn16bitWords() {
		return fileLengthIn16bitWords;
	}
	public void setFileLengthIn16bitWords(int fileLength) {
		if (fileLength < 0) {
			throw new IllegalArgumentException("File length < 0. Was "+fileLength+". Maybe the file is too large?");
		}
		this.fileLengthIn16bitWords = fileLength;
	}

	public ShapefileType getShapeType() {
		return shapeType;
	}
	public void setShapeType(ShapefileType shapeType) {
		this.shapeType = shapeType;
	}
	public double getXmin() {
		return xmin;
	}
	public void setXmin(double xmin) {
		this.xmin = xmin;
	}
	public double getXmax() {
		return xmax;
	}
	public void setXmax(double xmax) {
		this.xmax = xmax;
	}
	public double getYmin() {
		return ymin;
	}
	public void setYmin(double ymin) {
		this.ymin = ymin;
	}
	public double getYmax() {
		return ymax;
	}
	public void setYmax(double ymax) {
		this.ymax = ymax;
	}
	
	
}
