package com.sinergise.geopedia.geometry.util;


import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.valid.IsValidOp;


/**
 * @author amarolt
 *
 * This class is based on GeomCheck.
 * It is different from GeomCheck, because you have to make an instance before you can use it.
 * This is so that each instance can have its own SRID.
 * 
 * toJts and fromJts methods create geometries of a certain SRID.
 */
public class GeomCheckWithSRID
{
	public /*static*/ String isValid(Geometry g)
	{
		try {
			com.vividsolutions.jts.geom.Geometry jtsGeom = toJts(g);
			IsValidOp ivo = new IsValidOp(jtsGeom);
			if (ivo.isValid())
				return null;
			WKTWriter wktw = new WKTWriter();
			return ivo.getValidationError().getMessage() + ", WKT: " + wktw.write(jtsGeom);
		} catch (Exception e) {
			return "Neznana napaka ("+e.getMessage()+")";
		}
	}
	
	
	public GeomCheckWithSRID(int SRID) {
	    setSRID(SRID);
    }
	
	public GeomCheckWithSRID() {
	    this(1);
	}
	
	
	public /*static*/ final PackedCoordinateSequenceFactory coordSeqFac = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 2);
	public /*static*/ /*final*/ GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 1, coordSeqFac);
	
	public /*static*/ void setSRID(int SRID) {
	    geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), SRID, coordSeqFac);
	}
	
	public /*static*/ com.vividsolutions.jts.geom.Geometry toJts(Geometry g) {
	    checkSRID(g);
	    
		if (g instanceof Point) {
			return toJts((Point)g);
		} else
		if (g instanceof MultiPoint) { 
			return toJts((MultiPoint)g);
		} else
		if (g instanceof LinearRing) {
			return toJts((LinearRing)g);
		} else
		if (g instanceof LineString) {
			return toJts((LineString)g);
		} else
		if (g instanceof MultiLineString) {
			return toJts((MultiLineString)g);
		} else
		if (g instanceof Polygon) {
			return toJts((Polygon)g);
		} else
		if (g instanceof MultiPolygon) {
			return toJts((MultiPolygon)g);
		} else {
			throw new IllegalStateException("Unknown geometry type");
		}
    }
	
	public /*static*/ com.vividsolutions.jts.geom.Point toJts(Point p){
	    checkSRID(p);
		return geomFactory.createPoint(new Coordinate(p.x, p.y));
	}
	
	public /*static*/ com.vividsolutions.jts.geom.MultiPoint toJts(MultiPoint mp) {
	    checkSRID(mp);
		int n = mp.size();
		com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[n];
		for (int a=0; a<n; a++) {
			points[a] = toJts(mp.get(a));
		}
		return geomFactory.createMultiPoint(points);
	}
	
	public /*static*/ com.vividsolutions.jts.geom.LineString toJts(LineString ls) {
	    checkSRID(ls);
		return geomFactory.createLineString(coordSeqFac.create(ls.coords, 2));
	}
	
	public /*static*/ com.vividsolutions.jts.geom.MultiLineString toJts(MultiLineString mls) {
	    checkSRID(mls);
		int n = mls.size();
		com.vividsolutions.jts.geom.LineString[] lines = new com.vividsolutions.jts.geom.LineString[n];
		for (int a=0; a<n; a++) {
			lines[a] = toJts(mls.get(a));
		}
		return geomFactory.createMultiLineString(lines);
	}
	
	public /*static*/ com.vividsolutions.jts.geom.LinearRing toJts(LinearRing lr) {
	    checkSRID(lr);
		return geomFactory.createLinearRing(coordSeqFac.create(lr.coords, 2));
	}
	
	public /*static*/ com.vividsolutions.jts.geom.Polygon toJts(Polygon p) {
	    checkSRID(p);
		com.vividsolutions.jts.geom.LinearRing outer = toJts(p.getOuter());
		com.vividsolutions.jts.geom.LinearRing[] holes = new com.vividsolutions.jts.geom.LinearRing[p.getNumHoles()];
		for (int a=0; a<holes.length; a++)
			holes[a] = toJts(p.getHole(a));
		
		return geomFactory.createPolygon(outer, holes);
	}
	
	public /*static*/ com.vividsolutions.jts.geom.MultiPolygon toJts(MultiPolygon mp) {
	    checkSRID(mp);
		int n = mp.size();
		com.vividsolutions.jts.geom.Polygon[] polys = new com.vividsolutions.jts.geom.Polygon[n];
		for (int a=0; a<n; a++) {
			polys[a] = toJts(mp.get(a));
		}
		return geomFactory.createMultiPolygon(polys);
	}
	
	public /*static*/ Point fromJts(com.vividsolutions.jts.geom.Point p) {
	    checkSRID(p);
//		return new Point(p.getX(), p.getY());
	    Point point = new Point(p.getX(), p.getY());
	    setSRID(point);
	    return point;
	}
	
	public /*static*/ LineString fromJts(com.vividsolutions.jts.geom.LineString ls) {
	    checkSRID(ls);
		int n = ls.getNumPoints();
		double[] coords = new double[n*2];
		int pos = 0;
		CoordinateSequence seq = ls.getCoordinateSequence();
		
		for (int a=0; a<n; a++) {
			coords[pos++] = seq.getX(a);
			coords[pos++] = seq.getY(a);
		}
//		return new LineString(coords);
		LineString lineString = new LineString(coords);
		setSRID(lineString);
		return lineString;
	}
	
	public /*static*/ LinearRing fromJts(com.vividsolutions.jts.geom.LinearRing lr) {
	    checkSRID(lr);
	    int n = lr.getNumPoints();
		double[] coords = new double[n*2];
		int pos = 0;
		CoordinateSequence seq = lr.getCoordinateSequence();
		
		for (int a=0; a<n; a++) {
			coords[pos++] = seq.getX(a);
			coords[pos++] = seq.getY(a);
		}
//		return new LinearRing(coords);
		LinearRing linearRing = new LinearRing(coords);
        setSRID(linearRing);
        return linearRing;
	}
	
	public /*static*/ Polygon fromJts(com.vividsolutions.jts.geom.Polygon p) {
	    checkSRID(p);
		LinearRing outer = fromJts((com.vividsolutions.jts.geom.LinearRing)p.getExteriorRing());
		int nHoles = p.getNumInteriorRing();
		LinearRing[] holes = nHoles == 0 ? null : new LinearRing[nHoles];
		for (int a=0; a<nHoles; a++)
			holes[a] = fromJts((com.vividsolutions.jts.geom.LinearRing)p.getInteriorRingN(a));
//		return new Polygon(outer, holes);
		Polygon polygon = new Polygon(outer, holes);
		setSRID(polygon);
		return polygon;
	}
	
	public /*static*/ MultiPoint fromJts(com.vividsolutions.jts.geom.MultiPoint mp) {
	    checkSRID(mp);
		int n = mp.getNumGeometries();
		Point[] out = new Point[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.Point)mp.getGeometryN(a));
//		return new MultiPoint(out);
		MultiPoint multiPoint = new MultiPoint(out);
		setSRID(multiPoint);
		return multiPoint;
	}
	
	public /*static*/ MultiLineString fromJts(com.vividsolutions.jts.geom.MultiLineString mls) {
	    checkSRID(mls);
		int n = mls.getNumGeometries();
		LineString[] out = new LineString[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.LineString)mls.getGeometryN(a));
//		return new MultiLineString(out);
		MultiLineString multiLineString = new MultiLineString(out);
		setSRID(multiLineString);
		return multiLineString;
	}
	
	public /*static*/ MultiPolygon fromJts(com.vividsolutions.jts.geom.MultiPolygon mp) {
	    checkSRID(mp);
		int n = mp.getNumGeometries();
		Polygon[] out = new Polygon[n];
		for (int a=0; a<n; a++)
			out[a] = fromJts((com.vividsolutions.jts.geom.Polygon)mp.getGeometryN(a));
//		return new MultiPolygon(out);
		MultiPolygon multiPolygon = new MultiPolygon(out);
		setSRID(multiPolygon);
		return multiPolygon;
	}
	
	public /*static*/ Geometry fromJts(com.vividsolutions.jts.geom.Geometry g) {
	    checkSRID(g);
	    
		if (g instanceof com.vividsolutions.jts.geom.Polygon) {
			return fromJts((com.vividsolutions.jts.geom.Polygon)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.LineString) {
			return fromJts((com.vividsolutions.jts.geom.LineString)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.Point) {
			return fromJts((com.vividsolutions.jts.geom.Point)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiPolygon) {
			return fromJts((com.vividsolutions.jts.geom.MultiPolygon)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiLineString) {
			return fromJts((com.vividsolutions.jts.geom.MultiLineString)g);
		} else
		if (g instanceof com.vividsolutions.jts.geom.MultiPoint) {
			return fromJts((com.vividsolutions.jts.geom.MultiPoint)g);
		} else {
			throw new UnsupportedOperationException("Unknown geom type: "+g.getClass().getName());
		}
	}	
	
	public /*static*/ void checkSRID(Geometry g) throws IllegalStateException {
	    boolean validSRID = _checkSRID(g);
        if (! validSRID) {
            throw new IllegalStateException(
                    " SRID of current geometry"+CrsIdentifier.getSrid(g.getCrsId())+
                    " has to match the" +
                    " SRID of 'geometry factory'("+geomFactory.getSRID()+").");
        }
	}
	private /*static*/ boolean _checkSRID(Geometry g) throws IllegalStateException {
		int srid = CrsIdentifier.getSrid(g.getCrsId());
        if (g instanceof Point) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof MultiPoint) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof LinearRing) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof LineString) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof MultiLineString) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof Polygon) {
            return geomFactory.getSRID() == srid;
        } else
        if (g instanceof MultiPolygon) {
            return geomFactory.getSRID() == srid;
        } else {
            throw new IllegalStateException("Unknown geometry type");
        }
    }
	private /*static*/ void setSRID(Geometry g) throws IllegalStateException {
		CrsIdentifier crsId = new CrsIdentifier(String.valueOf(geomFactory.getSRID()));
		
        if (g instanceof Point) {
            ((Point)g).setCrsId(crsId);
        } else
        if (g instanceof MultiPoint) {
            ((MultiPoint)g).setCrsId(crsId);
        } else
        if (g instanceof LinearRing) {
            ((LinearRing)g).setCrsId(crsId);
        } else
        if (g instanceof LineString) {
            ((LineString)g).setCrsId(crsId);
        } else
        if (g instanceof MultiLineString) {
            ((MultiLineString)g).setCrsId(crsId);
        } else
        if (g instanceof Polygon) {
            ((Polygon)g).setCrsId(crsId);
        } else
        if (g instanceof MultiPolygon) {
            ((MultiPolygon)g).setCrsId(crsId);
        } else {
            throw new IllegalStateException("Unknown geometry type");
        }
    }
	
	public /*static*/ void checkSRID(com.vividsolutions.jts.geom.Geometry g) throws IllegalStateException {
        boolean validSRID = _checkSRID(g);
        if (! validSRID) {
            throw new IllegalStateException(
                    " SRID of current geometry"+g.getSRID()+
                    " has to match the" +
                    " SRID of 'geometry factory'("+geomFactory.getSRID()+").");
        }
    }
	public /*static*/ boolean _checkSRID(com.vividsolutions.jts.geom.Geometry g) throws IllegalStateException {
        if (g instanceof com.vividsolutions.jts.geom.Polygon) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.Polygon)g).getSRID();
        } else
        if (g instanceof com.vividsolutions.jts.geom.LineString) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.LineString)g).getSRID();
        } else
        if (g instanceof com.vividsolutions.jts.geom.Point) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.Point)g).getSRID();
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.MultiPolygon)g).getSRID();
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiLineString) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.MultiLineString)g).getSRID();
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiPoint) {
            return geomFactory.getSRID() == ((com.vividsolutions.jts.geom.MultiPoint)g).getSRID();
        } else {
            throw new IllegalStateException("Unknown geom type: "+g.getClass().getName());
        }
    }
	/**
	 * @this method is obsolete, because geomFactory sets correct SRID.
	 * @throws IllegalStateException
	 */
	@SuppressWarnings("unused")
	private /*static*/ void setSRID(com.vividsolutions.jts.geom.Geometry g) throws IllegalStateException {
        if (g instanceof com.vividsolutions.jts.geom.Polygon) {
            ((com.vividsolutions.jts.geom.Polygon)g).setSRID(geomFactory.getSRID());
        } else
        if (g instanceof com.vividsolutions.jts.geom.LineString) {
            ((com.vividsolutions.jts.geom.LineString)g).setSRID(geomFactory.getSRID());
        } else
        if (g instanceof com.vividsolutions.jts.geom.Point) {
            ((com.vividsolutions.jts.geom.Point)g).setSRID(geomFactory.getSRID());
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            ((com.vividsolutions.jts.geom.MultiPolygon)g).setSRID(geomFactory.getSRID());
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiLineString) {
            ((com.vividsolutions.jts.geom.MultiLineString)g).setSRID(geomFactory.getSRID());
        } else
        if (g instanceof com.vividsolutions.jts.geom.MultiPoint) {
            ((com.vividsolutions.jts.geom.MultiPoint)g).setSRID(geomFactory.getSRID());
        } else {
            throw new IllegalStateException("Unknown geom type: "+g.getClass().getName());
        }
    }
	
	public 
//	com.vividsolutions.jts.geom.Geometry
	void
	apply(Geometry g, CustomCoordinateFilter coordinateFilter) {
	    if (g==null || coordinateFilter==null) {
	        return;
	    }
	    
	    if (g instanceof Point) {
            Point point = ((Point)g);
            Coordinate coordinate = new Coordinate(point.x, point.y);
            coordinateFilter.filter(coordinate);
            point.x = coordinate.x;
            point.y = coordinate.y;
        } else
        if (g instanceof MultiPoint) {
            MultiPoint multiPoint = ((MultiPoint)g);
            int size = multiPoint.size();
            if (size > 0) {
                for (int i = 0; i< size; i++) {
                    Point point = multiPoint.get(i);
                    if (point != null) {
                        apply(point, coordinateFilter); // recursive
                    }
                }
            }
        } else
        if (g instanceof LinearRing) {
            LinearRing linearRing = ((LinearRing)g);
            double[] coords = linearRing.coords;
            double[] result = apply(coords, coordinateFilter);
            linearRing.coords = result;
        } else
        if (g instanceof LineString) {
            LineString lineString = ((LineString)g);
            double[] coords = lineString.coords;
            double[] result = apply(coords, coordinateFilter);
            lineString.coords = result;
        } else
        if (g instanceof MultiLineString) {
            MultiLineString multiLineString = ((MultiLineString)g);
            int size = multiLineString.size();
            if (size > 0) {
                for (int i = 0; i< size; i++) {
                    LineString lineString = multiLineString.get(i);
                    if (lineString != null) {
                        apply(lineString, coordinateFilter); // recursive
                    }
                }
            }
        } else
        if (g instanceof Polygon) {
            Polygon polygon = ((Polygon)g);
            LinearRing outer = polygon.outer;
            if (outer != null) {
                apply(outer, coordinateFilter); // recursive
            }
            LinearRing[] holes = polygon.holes;
            if (holes!=null && holes.length > 0) {
                for (LinearRing hole : holes) {
                    if (hole != null) {
                        apply(hole, coordinateFilter); // recursive
                    }
                }
            }
        } else
        if (g instanceof MultiPolygon) {
            MultiPolygon multiPolygon = ((MultiPolygon)g);
            int size = multiPolygon.size();
            if (size > 0) {
                for (int i = 0; i< size; i++) {
                    Polygon polygon = multiPolygon.get(i);
                    if (polygon != null) {
                        apply(polygon, coordinateFilter); // recursive
                    }
                }
            }
        } else {
            throw new IllegalStateException("Unknown geometry type");
        }
	    
	    setSRID(g);
	}
	
//	public void apply(Geometry g) {
//	    apply(g, this.coordinateFilter);
//    }
    private double[] apply(double[] coords, CustomCoordinateFilter coordinateFilter) {
        if (coords != null && coords.length > 0) {

            double[] result = new double[coords.length];

            int dimension = 2;
            if (coords.length % dimension != 0) {
                throw new IllegalArgumentException("Packed array does not contain "
                        + "an integral number of coordinates");
            }

            int n = coords.length / dimension;
            CoordinateSequence coordinateSequence = coordSeqFac.create(coords, dimension);
            n = coordinateSequence.size();

            for (int a = 0; a < n; a++) {
                Coordinate coordinate = coordinateSequence.getCoordinate(a);
                {
                    coordinateFilter.filter(coordinate);
                }
                result[a * 2] = coordinate.x;
                result[a * 2 + 1] = coordinate.y;
            }
            return result;
        }
        return null;
}
	/**
     * @param coords
     * @param coordinateFilter
     * @return i is ascending, while j is descending, because the order of
     *         coordinate pairs inside coords[] is opposite in clock-wise
     *         orientation than the order that is used by JTS.
     */
	
	public interface CustomCoordinateFilter {
        void filter(Coordinate coord);
    }
	
	public static final int SRID_WGS84 = 4326;
	
	public static final CustomCoordinateFilter D48_TO_WGS84 = new CustomCoordinateFilter() {
        public void filter(Coordinate coordinate) {
            double x = coordinate.x;
            double y = coordinate.y;
//            coordinate.x = Transforms.D48_TO_WGS84.lat(x, y);
//            coordinate.y = Transforms.D48_TO_WGS84.lon(x, y);
            coordinate.x = Transforms.D48_TO_WGS84.lon(x, y);
            coordinate.y = Transforms.D48_TO_WGS84.lat(x, y);
        }
    };
//    public static final CustomCoordinateFilter D48_TO_WGS84 = new CustomCoordinateFilter() {
//        public void filter(Coordinate coordinate) {
//            double x = coordinate.x;
//            double y = coordinate.y;
//            coordinate.x = Transforms.D48_TO_WGS84.lat(x, y);
//            coordinate.y = Transforms.D48_TO_WGS84.lon(x, y);
//        }
//    };
}
