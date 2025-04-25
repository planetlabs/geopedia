package com.sinergise.common.geometry.io;

import java.util.Collections;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;

@SuppressWarnings("unchecked")
public enum OgcShapeType {
	//Warning: name defines WKT tag name 
	GEOMETRY(null, null, 0, false),
	
	POINT(GEOMETRY, 1) {
		@Override
		public Point createEmpty() {
			return new Point();
		}
	},
	
	CURVE(GEOMETRY, null, 13, false),

	LINESTRING(CURVE, 2) {
		@Override
		public Geometry createEmpty() {
			return new LineString();
		}
	},
	
	CIRCULARSTRING(CURVE, 8),
	
	COMPOUNDCURVE(CURVE, CURVE, 9) {
		@Override
		public OgcShapeType getAutomaticMemberType() {
			return LINESTRING;
		}
	},
	
	CIRCLE(CURVE, 18),
	
	GEODESICSTRING(CURVE, 19),
	
	ELLIPTICALCURVE(CURVE, 20),
	
	NURBSCURVE(CURVE, 21),
	
	CLOTHOID(CURVE, 22),
	
	SPIRALCURVE(CURVE, 23),
	
	SURFACE(GEOMETRY, null, 14, false),
	
	CURVEPOLYGON(SURFACE, CURVE, 10) {
		@Override
		public OgcShapeType getAutomaticMemberType() {
			return LINESTRING;
		}
	},
	
	POLYGON(CURVEPOLYGON, LINESTRING, 3, true) {
		@Override
		public Polygon createInstance(List<? extends Geometry> members) {
			return Polygon.create((List<? extends LineString>)members);
		}
		@Override
		public LineString[] createMemberArray(int len) {
			return new LineString[len];
		}
	},
	
	TRIANGLE(POLYGON, 17),
	
	POLYHEDRALSURFACE(SURFACE, 15),
	
	TIN(POLYHEDRALSURFACE, 16),

	GEOMETRYCOLLECTION(GEOMETRY, GEOMETRY, 7) {
		@Override
		public GeometryCollection<Geometry> createInstance(List<? extends Geometry> members) {
			return new GeometryCollection<Geometry>(members);
		}
		
		@Override
		public Geometry[] createMemberArray(int len) {
			return new Geometry[len];
		}
	},
	
	MULTIPOINT(GEOMETRYCOLLECTION, POINT, 4) {
		@Override
		public MultiPoint createInstance(List<? extends Geometry> members) {
			return new MultiPoint((List<? extends Point>)members);
		}
		@Override
		public Point[] createMemberArray(int len) {
			return new Point[len];
		}
	},
	
	MULTICURVE(GEOMETRYCOLLECTION, CURVE, 11) {
		@Override
		public OgcShapeType getAutomaticMemberType() {
			return LINESTRING;
		}
	},
	
	MULTILINESTRING(MULTICURVE, LINESTRING, 5) {
		@Override
		public MultiLineString createInstance(List<? extends Geometry> members) {
			return new MultiLineString((List<? extends LineString>)members);
		}
		@Override
		public LineString[] createMemberArray(int len) {
			return new LineString[len];
		}
	},
	
	MULTISURFACE(GEOMETRYCOLLECTION, SURFACE, 12) {
		@Override
		public OgcShapeType getAutomaticMemberType() {
			return POLYGON;
		}
	},
	
	MULTIPOLYGON(MULTISURFACE, POLYGON, 6) {
		@Override
		public MultiPolygon createInstance(List<? extends Geometry> members) {
			return new MultiPolygon((List<? extends Polygon>)members);
		}
		
		@Override
		public Polygon[] createMemberArray(int len) {
			return new Polygon[len];
		}
	};
	
	
	/*************************************************/
	
	private static OgcShapeType[] initLookup() {
		OgcShapeType[] ret = new OgcShapeType[values().length];
		for (OgcShapeType val : values()) {
			ret[val.getWkbValue()] = val;
		}
		return ret;
	}
	private static final OgcShapeType[] LOOKUP = initLookup();
	
	/**
	 * @param wkbVal only last three decimal digits will be taken into account
	 * @return
	 */
	public static OgcShapeType fromWkbHeader(int wkbVal) {
		return LOOKUP[wkbVal % 1000];
	}
	
	/**
	 * @param tag; should be trimmed, but case does not matter
	 * @return
	 */
	public static OgcShapeType fromWktTag(String tag, boolean caseSensitive) {
		return caseSensitive ? valueOf(tag) : valueOf(tag.toUpperCase());
	}

	
	/*************************************************/
	
	private final OgcShapeType parent;
	private final OgcShapeType compositeMemberType;
	private final boolean instantiable;
	private final int wkbVal; 
	
	private OgcShapeType(OgcShapeType parent, int wkbValue) {
		this(parent, null, wkbValue, true);
	}

	private OgcShapeType(OgcShapeType parent, OgcShapeType memberType, int wkbValue) {
		this(parent, memberType, wkbValue, true);
	}
	
	private OgcShapeType(OgcShapeType parent, OgcShapeType memberType, int wkbValue, boolean instantiable) {
		this.parent = parent;
		this.wkbVal = wkbValue;
		this.instantiable = instantiable;
		this.compositeMemberType = memberType;
	}
	
	public int getWkbValue() {
		return wkbVal;
	}

	public String getWktTag() {
		return name();
	}

	public boolean isInstantiable() {
		return instantiable;
	}

	public boolean isSuperOf(OgcShapeType shapeType) {
		return shapeType != null && (this == shapeType || isSuperOf(shapeType.parent));
	}

	/**
	 * Gets the (potentially abstract) type of this composite/collection type's children.
	 */
	public OgcShapeType getMemberType() {
		return compositeMemberType;
	}

	/**
	 * @param len  
	 */
	public <T> T[] createMemberArray(int len) {
		throw new UnsupportedOperationException("in "+this);
	}
	
	/**
	 * @param members list of members, e.g. created with <code>Arrays.asList({@link #createMemberArray(int)})</code> 
	 */
	public Geometry createInstance(List<? extends Geometry> members) {
		throw new UnsupportedOperationException("in "+this);
	}
	
	public Geometry createEmpty() {
		return createInstance(Collections.EMPTY_LIST);
	}

	/**
	 * Gets the type that is the default child for this composite/collection shape type.
	 * The default child is the one that doesn't need the tag text in WKT
	 */
	public OgcShapeType getAutomaticMemberType() {
		return compositeMemberType != null && compositeMemberType.isInstantiable() ? compositeMemberType : null;
	}
}