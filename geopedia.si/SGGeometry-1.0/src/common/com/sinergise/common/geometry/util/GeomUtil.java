/*
 *
 */
package com.sinergise.common.geometry.util;

import static com.sinergise.common.util.math.MathUtil.hypot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sinergise.common.geometry.algorithm.LineSegmentIntersector;
import com.sinergise.common.geometry.algorithm.LineSegmentIntersector.EventPoint;
import com.sinergise.common.geometry.algorithm.LineSegmentIntersectorUtil;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.HasGeometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.common.util.geom.LineSegment2D;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.math.AngleUtil;
import com.sinergise.common.util.math.MathUtil;

public class GeomUtil {

	 // CROSSES
	public static final int LINE_Pint_Qint = 1;

	 // TOUCHES ENDPOINT
	public static final int LINE_P1_Q1 = 2; 
	public static final int LINE_P2_Q1 = 4;
	public static final int LINE_P1_Q2 = 8;
	public static final int LINE_P2_Q2 = 16;
	
	// TOUCHES MIDPOINT
	public static final int LINE_P1_Qint = 32;
	public static final int LINE_Pint_Q1 = 64;
	public static final int LINE_P2_Qint = 128;
	public static final int LINE_Pint_Q2 = 256;

	// COMBOS
	public static final int MASK_LINE_P1 = LINE_P1_Q1 | LINE_P1_Qint | LINE_P1_Q2;
	public static final int MASK_LINE_PINT = LINE_Pint_Q1 | LINE_Pint_Qint | LINE_Pint_Q2;
	public static final int MASK_LINE_P2 = LINE_P2_Q1 | LINE_P2_Qint | LINE_P2_Q2;
	public static final int MASK_LINE_Q1 = LINE_P1_Q1 | LINE_Pint_Q1 | LINE_P2_Q1;
	public static final int MASK_LINE_QINT = LINE_P1_Qint | LINE_Pint_Qint | LINE_P2_Qint;
	public static final int MASK_LINE_Q2 = LINE_P1_Q2 | LINE_Pint_Q2 | LINE_P2_Q2;

	public static final int MASK_LINE_INTERIOR = LINE_Pint_Q1 | LINE_Pint_Qint | LINE_Pint_Q2 | LINE_P1_Qint | LINE_P2_Qint;
	public static final int MASK_LINE_NOINTERIOR = LINE_P1_Q1 | LINE_P1_Q2 | LINE_P2_Q1 | LINE_P2_Q2;
	public static final int MASK_LINE_ENDPOINTS = LINE_P1_Q1 | LINE_P1_Qint | LINE_P1_Q2 | LINE_Pint_Q1 | LINE_Pint_Q2 | LINE_P2_Q1 | LINE_P2_Qint | LINE_P2_Q2;
	public static final int MASK_LINE_ALL = MASK_LINE_ENDPOINTS | LINE_Pint_Qint;

	
	/** point is CCW from p1-p2 */
	public static final int PTLINE_LEFT = 1;
	/** point is CW from p1-p2 */
	public static final int PTLINE_RIGHT = 2;

	public static final int PTLINE_BEFORE_P1 = 4;
	public static final int PTLINE_AFTER_P2 = 8;

	public static final int PTLINE_ON_P1 = 16;
	public static final int PTLINE_ON_P2 = 32;
	public static final int PTLINE_INSIDE = 64;

	private static final int SUM_PTLINE_LEFT_RIGHT = PTLINE_LEFT + PTLINE_RIGHT;
	private static final int SUM_PTLINE_LR_MAX = 2*PTLINE_RIGHT;
	
	public static final int MIN_PTLINE_COLINEAR = 4;
	public static final int MIN_PTLINE_INTERSECT = 16;
	
	
	/** point is CCW from p1-p2 */
	public static final int EXT_PTLINE_T_LEFT = 1;
	/** point is CW from p1-p2 */
	public static final int EXT_PTLINE_T_RIGHT = 2;

	public static final int EXT_PTLINE_S_BEFORE_OR_ON_P1 = 4;
	public static final int EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2 = 8;
	public static final int EXT_PTLINE_S_AFTER_OR_ON_P2 = 16;
	
	private static final int MASK_S_P1 = EXT_PTLINE_S_BEFORE_OR_ON_P1 | EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2;
	private static final int MASK_S_P2 = EXT_PTLINE_S_AFTER_OR_ON_P2 | EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2;

	/**
	 * Mask used to detect intersection
	 * Returned by pointLineIntersect when the line is degenerate (p1==p2) and the point lies on both endpoints (p == p1 == p2)
	 */
	public static final int MASK_PTLINE_INTERSECT = PTLINE_ON_P1 | PTLINE_INSIDE | PTLINE_ON_P2;

	
	public static final int MASK_PTLINE_ONPOINT = PTLINE_ON_P1 | PTLINE_ON_P2;

	/**
	 * Returned by pointLineIntersect when the line is degenerate (p1==p2) and the point does not lies on an endpoint (p != p1 == p2)
	 */
	public static final int MASK_PTLINE_OUTSIDE = PTLINE_LEFT | PTLINE_RIGHT | PTLINE_BEFORE_P1 | PTLINE_AFTER_P2;

    private GeomUtil() {}
    
    public static final double[] calcCentroid(double[] featureData) {
        int len=featureData.length;
        if (len<6) { //tocke
            double x=0.5*(featureData[0]+featureData[2]);
            double y=0.5*(featureData[1]+featureData[3]);
            double z=featureData[4];
            return new double[] {x,y,z};
        } else if (len<9) { //linije
            double x=0.5*(featureData[0]+featureData[2]);
            double y=0.5*(featureData[1]+featureData[3]);
            double z=featureData[5];
            return new double[] {x,y,z};
        } else { //poligoni
            double x=featureData[6];
            double y=featureData[7];
            double z=featureData[8];
            return new double[] {x,y,z};
        }
    }
    
    public static final double toDeg(double deg, double min, double sec) {
        return AngleUtil.toDeg(deg, min, sec);
    }
    
    public static final double angle(HasCoordinate c0, HasCoordinate c1, HasCoordinate c2) {
    	return AngleUtil.angle(c0.x(), c0.y(), c1.x(), c1.y(), c2.x(), c2.y());
    }
    
    /**
	 * Computes the distance from a point (x,y) to a line segment (x1,y1) (x2,y2)
	 * 
	 * @param p the point to compute the distance for
	 * @param x1,y1 one point of the line
	 * @param x2,y2 another point of the line (must be different to A)
	 * @return the distance from point to line segment
	 */
    public static final double distancePointLineSegmentSq(final double x, final double y, final double x1, final double y1, final double x2, final double y2) {
    	final double dx = x2 - x1;
    	final double dy = y2 - y1;
		final double dx1 = x1 - x;
		final double dy1 = y1 - y;

		if (dx == 0 && dy==0) return dx1*dx1 + dy1*dy1; // Degenerate Line
		
		final double dx2 = x2 - x;
		final double dy2 = y2 - y;
		
		final int mask = pointLinePositionS(dx, dy, dx1, dy1, dx2, dy2);
		
		if ((mask & EXT_PTLINE_S_BEFORE_OR_ON_P1) != 0) { // P1 is closest
			return dx1*dx1 + dy1*dy1;
		}		
		if ((mask & EXT_PTLINE_S_AFTER_OR_ON_P2) != 0) { // P2 is closest
			return dx2*dx2 + dy2*dy2;
		}
		return internal_distancePointLineSq(dx, dy, dx2, dy2);
    }
    

	public static boolean withinDistancePointLineSegment(double x, double y, double x1, double y1, double x2, double y2, double distance) {
		double minX = Math.min(x1, x2);
		if (x + distance < minX) {
			return false;
		}
		double minY = Math.min(y1, y2);
		if (y + distance < minY) {
			return false;
		}
		double maxX = Math.max(x1, x2);
		if (x > maxX + distance) {
			return false;
		}
		double maxY = Math.max(y1, y2);
		if (y > maxY + distance) {
			return false;
		}
		return distancePointLineSegmentSq(x, y, x1, y1, x2, y2) < distance*distance;
	}
    
    public static double distancePointWholeLineSq(final double ptX, final double ptY, final double x1, final double y1, final double x2, final double y2) {
    	final double dx = x2 - x1;
    	final double dy = y2 - y1;
    	if (dx == 0 && dy==0) { //Degenerate Line
    		if (ptX == x1 && ptY == y1) return 0;
    		throw new IllegalArgumentException("Degenerate line, cannot compute distance");
    	}
		return internal_distancePointLineSq(dx, dy, x2 - ptX, y2 - ptY);
	}

    public static double distancePointWholeLineSigned(final double ptX, final double ptY, final double x1, final double y1, final double x2, final double y2) {
    	final double dx = x2 - x1;
    	final double dy = y2 - y1;
    	if (dx == 0 && dy==0) { //Degenerate Line
    		if (ptX == x1 && ptY == y1) return 0;
    		throw new IllegalArgumentException("Degenerate line, cannot compute distance");
    	}
		return internal_distancePointLineSigned(dx, dy, x2 - ptX, y2 - ptY);
	}

    
	private static double internal_distancePointLineSq(double dx, double dy, double dx2, double dy2) {
		final double topFr = dx2 * dy - dy2 * dx;
		final double lenLineSq = dx * dx + dy * dy;
		return topFr * topFr / lenLineSq;
	}

	/**
	 * Computes the (signed) distance from a point (x,y) to a line segment (x1,y1) (x2,y2)
	 * 
	 * The sign is positive for points lying left of the segment, and negative for those lying to the right of the segment.
	 * 
	 * Point lying before the segment (in the parallel direction) will return distance with a negative sign,
	 * those lying after the segment will return distance with a positive sign. 
	 * 
	 * @param p the point to compute the distance for
	 * @param x1,y1 one point of the line
	 * @param x2,y2 another point of the line (must be different to A)
	 * @return the signed distance from point to line segment
	 */
    public static final double distancePointLineSigned(final double x, final double y, final double x1, final double y1, final double x2, final double y2) {
    	final double dx = x2 - x1;
    	final double dy = y2 - y1;
		final double dx1 = x1 - x;
		final double dy1 = y1 - y;
		
		if (dx == 0 && dy==0) return dx1*dx1 + dy1*dy1; // Degenerate Line
		
		final double dx2 = x2 - x;
		final double dy2 = y2 - y;
		
		final int mask = pointLinePositionS(dx, dy, dx1, dy1, dx2, dy2);
		if ((mask & EXT_PTLINE_S_BEFORE_OR_ON_P1) != 0) { // P1 is closest
			return -hypot(dx1, dy1);
		}		
		if ((mask & EXT_PTLINE_S_AFTER_OR_ON_P2) != 0) { // P2 is closest
			return hypot(dx2, dy2);
		}
		return internal_distancePointLineSigned(dx, dy, dx2, dy2);
    }

	private static double internal_distancePointLineSigned(final double dx, final double dy, final double dx2, final double dy2) {
		return (dx2 * dy - dy2 * dx) / hypot(dx, dy);
	}

	public static final void reversePackedCoords(double[] coords)
    {
		int numSw=(coords.length/2)/2;
		int off=coords.length-2;
		for (int i = 0; i < numSw; i++) {
	        double x=coords[2*i];
	        double y=coords[2*i+1];
	        coords[2*i]=coords[off-2*i];
	        coords[2*i+1]=coords[off-2*i+1];
	        coords[off-2*i]=x;
	        coords[off-2*i+1]=y;
        }
    }
    
    public static final int getNumCoords(Geometry g) {
        if (g instanceof Point) return 1;
        if (g instanceof MultiPoint) return ((MultiPoint)g).size();
        if (g instanceof LineString) return ((LineString)g).getNumCoords();
        if (g instanceof Polygon) {
            Polygon poly=(Polygon)g;
            int ret=getNumCoords(poly.getOuter());
            int numHoles=poly.getNumHoles();
            for (int i = 0; i < numHoles; i++) {
                ret+=getNumCoords(poly.getHole(i));
            }
            return ret;
        }
        if (g instanceof GeometryCollection) {
            GeometryCollection<?> col=(GeometryCollection<?>)g;
            int ret=0;
            int size=col.size();
            for (int i = 0; i < size; i++) {
                ret+=getNumCoords(col.get(i));
            }
            return ret;
        }
        throw new IllegalArgumentException("Unknown Geometry class");
    }
	
	private static final int ptLineDegenerate(final double x1, final double y1, final double x2, final double y2, final double x, final double y) {
		if (x1 == x && y1 == y) return (x1 == x2 && y1 == y2) ? MASK_PTLINE_INTERSECT : PTLINE_ON_P1;
		if (x2 == x && y2 == y) return PTLINE_ON_P2;
		if (x1==x2 && y1==y2) return MASK_PTLINE_OUTSIDE;
		
		if (x1 < x2) {
			if (x < x1) return PTLINE_BEFORE_P1; // x1 <= x
			if (x > x2) return PTLINE_AFTER_P2;  // x1 <= x <= x2
			return PTLINE_INSIDE;
		}
		if (x > x1) return PTLINE_BEFORE_P1;	// x <= x1
		if (x < x2) return PTLINE_AFTER_P2;		// x2 <= x <= x1

		if (y1 < y2) {
			if (y < y1) return PTLINE_BEFORE_P1; // y1 <= y
			if (y > y2) return PTLINE_AFTER_P2;  // y1 <= y <= y2
			return PTLINE_INSIDE;
		}
		if (y > y1) return PTLINE_BEFORE_P1;	// y <= y1
		if (y < y2) return PTLINE_AFTER_P2;		// y2 <= y <= y1
		return PTLINE_INSIDE;
	}
	
	public static final int indexOfXY(double[] seq, HasCoordinate c) {
		return indexOfXY(seq, c.x(), c.y());
	}
	

	public static final int indexOfXY(double[] seq, double x, double y)
    {
		int len=seq.length/2;
		for (int i = 0; i < len; i++) {
	        if (seq[2*i]==x && seq[2*i+1]==y) return i;
        }
	    return -1;
    }
	
	public static final double distance(HasCoordinate p1, HasCoordinate p2) {
		return distance(p1.x(),p1.y(), p2.x(), p2.y());
	}
	
	public static final double distanceSq(HasCoordinate p1, HasCoordinate p2) {
		return distanceSq(p1.x(),p1.y(), p2.x(), p2.y());
	}

	public static final double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(distanceSq(x1,y1,x2,y2));
	}

	public static final double distanceSq(double x1, double y1, double x2, double y2) {
		double dx=(x2-x1);
		double dy=(y2-y1);
		return dx*dx + dy*dy;
	}
	
	public static final double segArea(double x1, double y1, double x2, double y2) {
	    return 0.5 * (x2 - x1) * (y1 + y2);
	}

    public static final double segArea(HasCoordinate prev, HasCoordinate next) {
        return segArea(prev.x(), prev.y(), next.x(), next.y());
    }

    /**
	 * Computes whether a ring defined by an array of {@link HasCoordinate}is oriented counter-clockwise.
	 * <p>
	 * This will handle coordinate lists which contain repeated points.
	 * 
	 * @param ring
	 *            an array of coordinates forming a ring
	 * @return <code>true</code> if the ring is oriented counter-clockwise.
	 * @throws IllegalArgumentException
	 *             if the ring is degenerate (does not contain 3 distinct points)
	 */
	public static boolean isCCW(LinearRing ring)
	{
		return isCCW(ring.coords);
	}
	
	public static boolean isCCW(double[] coords) {
		// # of points without closing endpoint
		int nPts = coords.length/2 - 1;

		// find highest point
		double hipX = coords[0];
		double hipY = coords[1];

		int hii = 0;
		for (int i = 1; i <= nPts; i++) {
			double y = coords[2*i + 1];
			if (y > hipY) {
				hipY = y;
				hipX = coords[2*i];
				hii = i;
			}
		}

		// find distinct point before highest point
		int iPrev = hii;
		do {
			if ((--iPrev) < 0)
				iPrev = nPts - 1;
		} while (coords[2*iPrev] == hipX && coords[2*iPrev + 1] == hipY && iPrev != hii);

		// find distinct point after highest point
		int iNext = hii;
		do {
			if ((++iNext) >= nPts)
				iNext = 0;
		} while (coords[2*iNext] == hipX && coords[2*iNext + 1] == hipY && iNext != hii);

		double prevX = coords[2*iPrev];
		double prevY = coords[2*iPrev + 1];

		double nextX = coords[2*iNext];
		double nextY = coords[2*iNext + 1];

		// this will catch all cases where there are not 3 distinct points,
		// including the case where the input array has fewer than 4 elements
		if ((prevX == hipX && prevY == hipY) || (nextX == hipX && nextY == hipY)
		        || (prevX == nextX && prevY == nextY)) {
			return false;
		}

		int disc = computeOrientation(prevX, prevY, hipX, hipY, nextX, nextY);

		/**
		 * If disc is exactly 0, lines are collinear. There are two possible cases: (1) the lines lie along
		 * the x axis in opposite directions (2) the lines lie on top of one another
		 * 
		 * (1) is handled by checking if next is left of prev ==> CCW (2) should never happen, so we're going
		 * to ignore it! (Might want to assert this)
		 */
		boolean isCCW = false;
		if (disc == 0) {
			// poly is CCW if prev x is right of next x
			isCCW = (prevX > nextX);
		} else {
			// if area is positive, points are ordered CCW
			isCCW = (disc > 0);
		}
		return isCCW;
	}
	
	/**
	 * Computes the orientation of a point q to the directed line segment p1-p2. The orientation of a point
	 * relative to a directed line segment indicates which way you turn to get to q after travelling from p1
	 * to p2.
	 * 
	 * @return 1 if q is counter-clockwise from p1-p2
	 * @return -1 if q is clockwise from p1-p2
	 * @return 0 if q is collinear with p1-p2
	 */
	public final static int computeOrientation(double p1x, double p1y, double p2x, double p2y, double qx, double qy)
	{
		return orientationIndex(p1x, p1y, p2x, p2y, qx, qy);
	}
	
	/**
	 * Computes the orientation of a point q to the directed line segment p1-p2. The orientation of a point
	 * relative to a directed line segment indicates which way you turn to get to q after travelling from p1
	 * to p2.
	 * 
	 * @return PTLINE_* bitmask indicating relation between the line and the point
	 */
	public final static int extendedOrientation(double x1, double y1, double x2, double y2, double x, double y)
	{
		final int or = orientationIndex(x1, y1, x2, y2, x, y);
		if (or > 0) return PTLINE_LEFT;
		if (or < 0) return PTLINE_RIGHT;
		return ptLineDegenerate(x1, y1, x2, y2, x, y);
	}

	
	/**
	 * Computes the position of a point q relative to the directed line segment p1-p2.
	 * The result is a combination of flags indicating position with coordinates t 
	 * (perpendicular to the line) and s (along the line). 
	 * There are 15 possibilities, 3 for the t coordinate (left, right, or on the carrier of the line segment)
	 * and 5 for the s coordinate (before p1, at p1, between p1 p2, at p2, after p2)
	 * 
	 * @return EXT_PTLINE_* bitmask indicating relation between the line and the point
	 */
	public final static int pointLinePosition(double x1, double y1, double x2, double y2, double x, double y)
	{
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		final double dx2 = x2 - x;
		final double dy2 = y2 - y;
		return pointLinePositionT(dx, dy, dx2, dy2) | pointLinePositionS(dx, dy, x1 - x, y1 - y, dx2, dy2);
	}
	
	/**
	 * @param dx  x2 - x1
	 * @param dy  y2 - y1
	 * @param dx1 x1 - x
	 * @param dy1 y1 - y
	 * @param dx2 x2 - x
	 * @param dy2 y2 - y
	 * 
	 * @return bit mask of EXT_PTLINE_S_BEFORE_OR_ON_P1, EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2, EXT_PTLINE_S_AFTER_OR_ON_P2
	 */
	public final static int pointLinePositionS(final double dx, final double dy, final double dx1, final double dy1, final double dx2, final double dy2)
	{
		final int leftS1 = RobustDeterminant.signOfDet2x2(-dy, dx, dy - dx1, - dy1 - dx);
		final int leftS2 = RobustDeterminant.signOfDet2x2(-dy, dx, dy - dx2, - dy2 - dx);
		return (leftS1 >= 0 ? EXT_PTLINE_S_BEFORE_OR_ON_P1 : 0)
			 | ((leftS1 <= 0 && leftS2 >= 0) ? EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2 : 0)
			 | (leftS2 <= 0 ? EXT_PTLINE_S_AFTER_OR_ON_P2 : 0);
	}
	
	/**
	 * @param dx  x2 - x1
	 * @param dy  y2 - y1
	 * @param dx2 x - x2
	 * @param dy2 y - y2
	 * 
	 * @return bit mask of EXT_PTLINE_T_LEFT and EXT_PTLINE_T_RIGHT
	 */
	public final static int pointLinePositionT(final double dx, final double dy, final double dx2, final double dy2)
	{
		final int leftT = -RobustDeterminant.signOfDet2x2(dx, dy, dx2, dy2);
		return (leftT >= 0 ? EXT_PTLINE_T_LEFT : 0) | (leftT <= 0 ? EXT_PTLINE_T_RIGHT : 0);
	}
	
	/**
	 * Returns the index of the direction of the point <code>q</code> relative to a vector specified by
	 * <code>p1-p2</code>.
	 * 
	 * @param p1
	 *            the origin point of the vector
	 * @param p2
	 *            the final point of the vector
	 * @param q
	 *            the point to compute the direction to
	 * 
	 * @return 1 if q is counter-clockwise (left) from p1-p2
	 * @return -1 if q is clockwise (right) from p1-p2
	 * @return 0 if q is collinear with p1-p2
	 */
	public static int orientationIndex(double p1x, double p1y, double p2x, double p2y, double qx, double qy)
	{
		return RobustDeterminant.signOfDet2x2(p2x - p1x, p2y - p1y, qx - p2x, qy - p2y);
	}
	
	/**
	 * Test whether a point lies inside a ring. The ring may be oriented in either direction. If the point
	 * lies on the ring boundary the result of this method is unspecified.
	 * <p>
	 * This algorithm does not attempt to first check the point against the envelope of the ring.
	 * 
	 * @param p point to check for ring inclusion
	 * @param ring
	 *            assumed to have first point identical to last point
	 * @return <code>true</code> if p is inside ring
	 */
	public static boolean isPointInRing(HasCoordinate p, LinearRing ring) {
		return isPointInRing(p.x(), p.y(), ring);
	}
	
	
	/**
	 * Test whether a point lies inside a ring. The ring may be oriented in either direction. If the point
	 * lies on the ring boundary the result of this method is unspecified.
	 * <p>
	 * This algorithm does not attempt to first check the point against the envelope of the ring.
	 * 
	 * @param p
	 *            point to check for ring inclusion
	 * @param ring
	 *            assumed to have first point identical to last point
	 * @return <code>true</code> if p is inside ring
	 */
	public static boolean isPointInRing(double px, double py, LinearRing ring)
	{
		int i;

		double xInt; // x intersection of segment with ray
		int crossings = 0; // number of segment/ray crossings

		int nPts = ring.getNumCoords();

		if (nPts < 3)
			return false;

		double lastx = ring.getX(0) - px;
		double lasty = ring.getY(0) - py;
		/*
		 * For each segment l = (i-1, i), see if it crosses ray from test point in positive x direction.
		 */
		for (i = 1; i < nPts; i++) {
			double x = ring.getX(i) - px;
			double y = ring.getY(i) - py;

			if (((y > 0) && (lasty <= 0)) || ((lasty > 0) && (y <= 0))) {
				xInt = RobustDeterminant.signOfDet2x2(x, y, lastx, lasty) / (lasty - y);

				if (0.0 < xInt) {
					crossings++;
				}
			}

			lastx = x;
			lasty = y;
		}
		/*
		 * p is inside if number of crossings is odd.
		 */
		return (crossings & 1) == 1;
	}
	
	/**
	 * Test whether a point lies inside a polygon. If the point
	 * lies on the polygon boundary the result of this method is unspecified.
	 * <p>
	 * This algorithm does not attempt to first check the point against the envelope of the polygon.
	 * 
	 * @param p
	 *            point to check for polygon inclusion
	 * @param poly
	 *            polygon
	 * @return <code>true</code> if p is inside polygon
	 */
	public static boolean isPointInPolygon(double px, double py, Polygon poly) {
		if (poly.outer == null) return false;

		//check if inside outer ring
		if (!GeomUtil.isPointInRing(px, py, poly.outer)) return false;

		//check if not in hole
		if (poly.getNumHoles() > 0) {
			for (LinearRing inner : poly.holes) {
				if (GeomUtil.isPointInRing(px, py, inner)) return false;
			}
		}
		return true;
	}
	
	public static final int PT_RING_OUTSIDE = 0;
	public static final int PT_RING_BOUNDARY_VERTEX = 1;
	public static final int PT_RING_BOUNDARY_LINE = 2;
	public static final int PT_RING_INSIDE = 4;
	
	/**
	 * Test where a point lies relative to the ring. If the The ring may be oriented in either direction. If the point
	 * lies on the ring boundary the result of this method is unspecified.
	 * <p>
	 * This algorithm does not attempt to first check the point against the envelope of the ring.
	 * 
	 * @param p
	 *            point to check for ring inclusion
	 * @param ring
	 *            assumed to have first point identical to last point
	 * @return <code>true</code> if p is inside ring
	 */
	public static int pointInRingRelation(double px, double py, LinearRing ring)
	{
		int i;

		double xInt; // x intersection of segment with ray
		int crossings = 0; // number of segment/ray crossings

		int nPts = ring.getNumCoords();

		if (nPts == 2) {
			int ptLine = pointLineIntersect(ring.coords[0], ring.coords[1], ring.coords[2], ring.coords[3], px, py);
			int ret = 0;
			if ((ptLine & PTLINE_INSIDE) != 0) ret |= PT_RING_BOUNDARY_LINE; 
			if ((ptLine & PTLINE_ON_P1) != 0 || (ptLine & PTLINE_ON_P2) != 0) ret |= PT_RING_BOUNDARY_VERTEX; 
			return ret;
		} else if (nPts == 1) {
			if (px==ring.coords[0] && py==ring.coords[1]) return PT_RING_BOUNDARY_VERTEX;
			return PT_RING_OUTSIDE;
		}
		int ret = 0;

		double lastx = ring.getX(0) - px;
		double lasty = ring.getY(0) - py;
		/*
		 * For each segment l = (i-1, i), see if it crosses ray from test point in positive x direction.
		 */
		for (i = 1; i < nPts; i++) {
			double x = ring.getX(i) - px;
			double y = ring.getY(i) - py;
			if (x==0 && y==0) {
				return PT_RING_BOUNDARY_VERTEX;
			}

			if (((y > 0) && (lasty <= 0)) || ((lasty > 0) && (y <= 0))) {
				xInt = RobustDeterminant.signOfDet2x2(x, y, lastx, lasty) / (lasty - y);

				if (xInt > 0.0) {
					crossings++;
				} else if (xInt==0) {
					return PT_RING_BOUNDARY_LINE;
				}
			}

			lastx = x;
			lasty = y;
		}
		/*
		 * p is inside if number of crossings is odd.
		 */
		if ((crossings & 1) == 1) {
			return PT_RING_INSIDE;
		}
		return ret;
	}
	
	/**
	 * Returns the signed area for a ring. The area is positive if the ring is oriented CW.
	 */
	public static double signedArea(LinearRing ring)
	{
		return signedArea(ring.coords);
	}
	
	public static double signedArea(double coords[])
	{
		int size = coords.length/2;
		if (size < 3)
			return 0.0;
		double sum = 0.0;
		double bx = coords[0];
		double by = coords[1];
		for (int i = 1; i < size; i++) {
			double cx = coords[i << 1];
			double cy = coords[(i << 1) + 1];
			sum += (bx + cx) * (cy - by);
			bx = cx;
			by = cy;
		}
		return -sum / 2.0;
	}
	
	/**
	 * 
	 * @param x1 line p1.x
	 * @param y1 line p1.y
	 * @param x2 line p2.x
	 * @param y2 line p2.y
	 * @param x point x
	 * @param y point y
	 * @return 0 if point doesn't intersect the line, PTLINE_ON_P1, PTLINE_ON_P2 or PTLINE_INSIDE if it does
	 */
	public static final int pointLineIntersect(final double x1, final double y1, final double x2, final double y2, final double x, final double y) {
		return extendedOrientation(x1, y1, x2, y2, x, y) & MASK_PTLINE_INTERSECT;
	}
	
	public static final int lineLineIntersect(CoordinatePair p, CoordinatePair q) {
		return lineLineIntersect(p.x1(), p.y1(), p.x2(), p.y2(), q.x1(), q.y1(), q.x2(), q.y2());
	}

	/**
	 * Note: Degenerate segment (point1 == point2) has interior and both endpoints in the same location
	 * 
	 * @param px1
	 * @param py1
	 * @param px2
	 * @param py2
	 * @param qx1
	 * @param qy1
	 * @param qx2
	 * @param qy2
	 * @return a combination of LINE_* bits or 0 if there is no intersection 
	 */
	public static final int lineLineIntersect(final double px1, final double py1, final double px2, final double py2, final double qx1, final double qy1, final double qx2, final double qy2)
	{
		final int q1 = extendedOrientation(px1, py1, px2, py2, qx1, qy1);
		final int q2 = extendedOrientation(px1, py1, px2, py2, qx2, qy2);
		final int p1 = extendedOrientation(qx1, qy1, qx2, qy2, px1, py1);
		final int p2 = extendedOrientation(qx1, qy1, qx2, qy2, px2, py2);

		// Interiors crossing
		final int sumQ = q1 + q2;
		final int sumP = p1 + p2;
		if (sumQ == SUM_PTLINE_LEFT_RIGHT && sumP == SUM_PTLINE_LEFT_RIGHT) return LINE_Pint_Qint;
		if (sumQ <= SUM_PTLINE_LR_MAX && sumP <= SUM_PTLINE_LR_MAX) return 0;
		
		// Handle degenerate cases first
		final boolean pDeg = (px1 == px2 && py1 == py2);
		final boolean qDeg = (qx1 == qx2 && qy1 == qy2);
		if (pDeg) {
			if (qDeg) return (px1 == qx1 && py1 == qy1) ? MASK_LINE_ALL : 0;
			return insideLineForIntersect(p1, MASK_LINE_Q1, MASK_LINE_QINT, MASK_LINE_Q2);
		}
		if (qDeg) return insideLineForIntersect(q1, MASK_LINE_P1, MASK_LINE_PINT, MASK_LINE_P2);

		// Collinear cases - possibility of endpoint touching, interiors intersecting and equality
		if (q1 >= MIN_PTLINE_COLINEAR && p1 >= MIN_PTLINE_COLINEAR) { //All points are Collinear
			int ret = 0;
			ret |= insideLineForIntersect(p1, LINE_P1_Q1, LINE_P1_Qint | LINE_Pint_Qint, LINE_P1_Q2);
			ret |= insideLineForIntersect(p2, LINE_P2_Q1, LINE_P2_Qint | LINE_Pint_Qint, LINE_P2_Q2);
			ret |= insideLineForIntersect(q1, LINE_P1_Q1, LINE_Pint_Q1 | LINE_Pint_Qint, LINE_P2_Q1);
			ret |= insideLineForIntersect(q2, LINE_P1_Q2, LINE_Pint_Q2 | LINE_Pint_Qint, LINE_P2_Q2);
			// Equals case
			if (   ((p1 & MASK_PTLINE_ONPOINT) != 0 && (p2 & MASK_PTLINE_ONPOINT) != 0) 
				|| ((q1 & MASK_PTLINE_ONPOINT) != 0 && (q2 & MASK_PTLINE_ONPOINT) != 0)) {
				
				ret |= LINE_Pint_Qint;
				
			}
			return ret;
		}
		
		// Endpoint or midpoint touching
		if (q1 >= MIN_PTLINE_COLINEAR) return insideLineForIntersect(q1,LINE_P1_Q1, LINE_Pint_Q1, LINE_P2_Q1);
		if (p1 >= MIN_PTLINE_COLINEAR) return insideLineForIntersect(p1,LINE_P1_Q1, LINE_P1_Qint, LINE_P1_Q2);
		if (q2 >= MIN_PTLINE_COLINEAR) return insideLineForIntersect(q2,LINE_P1_Q2, LINE_Pint_Q2, LINE_P2_Q2);
		if (p2 >= MIN_PTLINE_COLINEAR) return insideLineForIntersect(p2,LINE_P2_Q1, LINE_P2_Qint, LINE_P2_Q2);
		
		throw new RuntimeException("Should never get here");
	}
	
	private static int insideLineForIntersect(int pointInLine, int pointLine1, int pointLineInt, int pointLine2) {
		int ret = 0;
		if ((pointInLine & PTLINE_ON_P1) != 0) ret |= pointLine1;
		if ((pointInLine & PTLINE_INSIDE) != 0) ret |= pointLineInt;
		if ((pointInLine & PTLINE_ON_P2) != 0) ret |= pointLine2;
		return ret;
	}

	public static void pointLineNearestConstrained(final double x1, final double y1, final double x2, final double y2, final double x, final double y, final HasCoordinateMutable out, int allowedSMask) {
		final double dx = x2-x1;
		final double dy = y2-y1;
		final double dx1 = x1-x;
		final double dy1 = y1-y;
		final double dx2 = x2-x;
		final double dy2 = y2-y;
		
		final int extS = pointLinePositionS(dx, dy, dx1, dy1, dx2, dy2);
		if ((extS & allowedSMask) == 0) { // Not allowed; set to closest allowed endpoint
			final double distSq1 = ((allowedSMask & MASK_S_P1) == 0) ? Double.POSITIVE_INFINITY : dx1 * dx1 + dy1 * dy1;
			final double distSq2 = ((allowedSMask & MASK_S_P2) == 0) ? Double.POSITIVE_INFINITY : dx2 * dx2 + dy2 * dy2;
			if (distSq1 < distSq2) {
				out.setLocation(new Position2D(x1, y1));
			} else {
				out.setLocation(new Position2D(x2, y2));
			}
			return;
		}
		final int extT = pointLinePositionT(dx, dy, dx2, dy2);
		if (extT  == (EXT_PTLINE_T_LEFT | EXT_PTLINE_T_RIGHT)) { // On the line
			out.setLocation(new Position2D(x, y));
			return;
		}
		final double lenSq = dx*dx + dy*dy;
		out.setLocation(new Position2D(x - dy*(x2*dy1-x1*dy2+x*dy)/lenSq, y - dx*(x1*dy2-x2*dy1-x*dy)/lenSq));
	}
	
	public static void pointLineStringNearest(final double x1, final double y1, final double x2, final double y2, final double x, final double y, final HasCoordinateMutable out) {
		pointLineNearestConstrained(x1, y1, x2, y2, x, y, out, EXT_PTLINE_S_BETWEEN_OR_ON_P1_P2);
	}
	
	public static void pointLineNearest(final double x1, final double y1, final double x2, final double y2, final double x, final double y, final Point out) {
		final int extOr = pointLinePositionT(x2-x1, y2-y1, x2-x, y2-y);
		if (extOr == (EXT_PTLINE_T_LEFT | EXT_PTLINE_T_RIGHT)) {
			out.x = x;
			out.y = y;
			return;
		}
		
		final double dx = x2-x1;
		final double dy = y2-y1;
		final double dy1 = y1-y;
		final double dy2 = y2-y;
		
		final double lenSq = dx*dx + dy*dy;
		
		out.x = x - dy*(x2*dy1-x1*dy2+x*dy)/lenSq;
		out.y = y - dx*(x1*dy2-x2*dy1-x*dy)/lenSq;
	}

	public static boolean equals2D(HasCoordinate p1, HasCoordinate p2) {
		if (p1 == null) {
			return p2 == null;
		}
		if (p2 == null) {
			return false;
		}
		final double x1 = p1.x(); 
		if (Double.isNaN(x1)) {
			return Double.isNaN(p2.x()) && Double.isNaN(p2.y());
		}
		return x1==p2.x() && p1.y() == p2.y();
	}
	
	/**
	 * Calculates new c2 to resize vector between c1 and c2 to newSize.
	 */
	public static HasCoordinate resizeVector(HasCoordinate c1, HasCoordinate c2, double newSize) {
		double scale = newSize/GeomUtil.distance(c1, c2);
		return new Position2D(
			c1.x() + (c2.x() - c1.x()) * scale, 
			c1.y() + (c2.y() - c1.y()) * scale);
	}

	public static boolean isOverlapOrWithin(int intersection) {
		return ((intersection & LINE_Pint_Qint) != 0) && ((intersection & MASK_LINE_ENDPOINTS) != 0);
	}
	
	
	
	public static final int PTENV_IN = 0;
	public static final int PTENV_OUT_LEFT = 1;
	public static final int PTENV_OUT_TOP = 2;
	public static final int PTENV_OUT_RIGHT = 4;
	public static final int PTENV_OUT_BOTTOM = 8;
	
	/** 
     * Determines where the specified point lies in respect to given rectangle (envelope).
     * @return 0 if the point lies inside the envelope otherwise a logical OR of appropriate relation codes.
     */
	public static int pointRectangleRelation(double x, double y, Envelope env) {
		int rel = 0;
	    if (env.getWidth() <= 0) {
	    	rel |= PTENV_OUT_LEFT | PTENV_OUT_RIGHT;
	    } else if (x < env.getMinX()) {
	    	rel |= PTENV_OUT_LEFT;
	    } else if (x > env.getMaxX()) {
	    	rel |= PTENV_OUT_RIGHT;
	    }
	    if (env.getHeight() <= 0) {
	    	rel |= PTENV_OUT_TOP | PTENV_OUT_BOTTOM;
	    } else if (y < env.getMinY()) {
	    	rel |= PTENV_OUT_BOTTOM;
	    } else if (y > env.getMaxY()) {
		rel |= PTENV_OUT_TOP;
	    }
	    return rel;
	}
	
	/**
	 * Clips a line defined by the specified points to the given rectangle (envelope).
	 * 
	 * @return <code>true</code> if the line is inside the envelope, <code>false</code> otherwise.
	 */
	public static boolean clipLine(Point p1, Point p2, Envelope env) {
		
		double x1 = p1.x();
		double y1 = p1.y();
		double x2 = p2.x();
		double y2 = p2.y();
		
		int r1 = pointRectangleRelation(x1, y1, env);
		int r2 = pointRectangleRelation(x2, y2, env);
		
		while ((r1 | r2) != 0) {
			if ((r1 & r2) != 0) { //if line is outside of rectangle
				p1.setLocation(0, 0);
				p2.setLocation(0, 0);
				return false; 
			}
			
			double dx = p2.x() - p1.x();
			double dy = p2.y() - p1.y();
			
			double minX = env.getMinX();
			double maxX = env.getMaxX();
			double maxY = env.getMaxY();
			double minY = env.getMinY();
			if (r1 != 0) { //first point outside
				
				if ((r1 & PTENV_OUT_LEFT) != 0 && dx != 0) {
					y1 += (minX - x1) * dy/dx;
					x1 = minX;
					
				} else if ((r1 & PTENV_OUT_RIGHT) != 0 && dx != 0) {
					y1 += (maxX - x1) * dy/dx;
					x1 = maxX;
					
				} else if ((r1 & PTENV_OUT_TOP) != 0 && dy != 0) {
					x1 += (maxY - y1) * dx/dy;
					y1 = maxY;
					
				} else if ((r1 & PTENV_OUT_BOTTOM) != 0) {
					x1 += (minY - y1) * dx/dy;
					y1 = minY;
				}
				
				r1 = pointRectangleRelation(x1, y1, env);
			}
			else if (r2 != 0) { //second point outside
				
				if ((r2 & PTENV_OUT_LEFT) != 0) {
					y2 += (minX - x2) * dy/dx;
					x2 = minX;
					
				} else if ((r2 & PTENV_OUT_RIGHT) != 0) {
					y2 += (maxX - x2) * dy/dx;
					x2 = maxX;
					
				} else if ((r2 & PTENV_OUT_TOP) != 0) {
					x2 += (maxY - y2) * dx/dy;
					y2 = maxY;
					
				} else if ((r2 & PTENV_OUT_BOTTOM) != 0) {
					x2 += (minY - y2) * dx/dy;
					y2 = minY;
				}
				
				r2 = pointRectangleRelation(x2, y2, env);
			}
			
		}
		
		p1.setLocation(x1, y1);
		p2.setLocation(x2, y2);
		return true;
		
	}
	
	public static Geometry extractMultiPolygon(Geometry geometry) {
		List<Polygon> polys = extractPolygons(geometry);
		if (polys.size() == 1) {
			return polys.get(0);
		} else if (polys.size() > 1) {
			return new MultiPolygon(polys.toArray(new Polygon[polys.size()]));
		} else {
			return null;
		}
	}
	
	public static List<Polygon> extractPolygons(Geometry geometry) {
		List<Polygon> result = new ArrayList<Polygon>();
		extractPolygons(geometry, result);
		return result;
	}
	
	public static void extractPolygons(Geometry geometry, List<? super Polygon> result) {
		if (geometry instanceof Polygon) {
			result.add((Polygon)geometry);
		} else if (geometry instanceof GeometryCollection<?>) {
			for (Geometry part : (GeometryCollection<?>) geometry) {
				extractPolygons(part, result);
			}
		}
	}
	
	public static List<Geometry> extractMultiPolygons(Geometry geometry) {
		List<Geometry> result = new ArrayList<Geometry>();
		extractMultiPolygons(geometry, result);
		return result;
	}
	
	public static void extractMultiPolygons(Geometry geometry, List<Geometry> result) {
		if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
			result.add(geometry);
		} else if (geometry instanceof GeometryCollection<?>) {
			for (Geometry part : (GeometryCollection<?>) geometry) {
				extractMultiPolygons(part, result);
			}
		}
	}
	
	public static List<LineString> extractLineStrings(Geometry geometry) {
		List<LineString> result = new ArrayList<LineString>();
		extractLineStrings(geometry, result);
		return result;
	}
	
	public static void extractLineStrings(Geometry geometry, List<? super LineString> result) {
		if (geometry instanceof LineString) {
			result.add((LineString)geometry);
		} else if (geometry instanceof Polygon) {
			Polygon poly = (Polygon) geometry;
			result.add(poly.outer);
			for (int i=0; i<poly.getNumHoles(); i++) {
				result.add(poly.holes[i]);
			}
		} else if (geometry instanceof GeometryCollection<?>) {
			for (Geometry part : (GeometryCollection<?>) geometry) {
				extractLineStrings(part, result);
			}
		}
	}
	
	public static Collection<CoordinatePair> extractCoordinatePairs(Geometry geometry) {
		List<CoordinatePair> pairs = new ArrayList<CoordinatePair>();
		extractCoordinatePairs(geometry, pairs);
		return pairs;
	}
	
	public static void extractCoordinatePairs(Geometry geometry, Collection<CoordinatePair> pairs) {
		geometry.accept(new ExtractCoordinatePairsVisitor(pairs));
	}
	
	public static void extractCoordinates(Geometry geometry, Collection<HasCoordinate> coords) {
		geometry.accept(new ExtractCoordinatesVisitor(coords));
	}
	
	public static boolean hasLineIntersections(Geometry geom) {
		List<CoordinatePair> lines = new ArrayList<CoordinatePair>(); 
		extractCoordinatePairs(geom, lines);
		
		SearchItemReceiver<EventPoint> interiorFilter = new SearchItemReceiver<EventPoint>() {
			@Override
			public Boolean execute(EventPoint item) {
				return Boolean.valueOf(!LineSegmentIntersectorUtil.INTERIOR_FILTER.eval(item));
			}
		};
		
		LineSegmentIntersector intrs = new LineSegmentIntersector(lines, interiorFilter);
		//intersection was found if whole geometry was NOT searched
		return !intrs.findIntersections();
	}
	
	public static LineSegment2D nearestEdge(HasCoordinate c, Geometry geom) {
		
		CoordinatePair nearest = null;
		double nearestDistSq = Double.MAX_VALUE;
		
		for (CoordinatePair seg : extractCoordinatePairs(geom)) {
			double distSq = distancePointLineSegmentSq(c.x(), c.y(), seg.x1(), seg.y1(), seg.x2(), seg.y2());
			if (distSq < nearestDistSq) {
				nearestDistSq = distSq;
				nearest = seg;
			}
		}
		
		return new LineSegment2D(nearest);
	}
	
	public static LineString findLineStringContainingEdge (Geometry geom, LineSegment2D edge, double gridSize) {
		
		edge.snapToGrid(gridSize);
		SnapToGridVisitor snapVisitor = new SnapToGridVisitor(gridSize);
		
		for (LineString line : extractLineStrings(geom)) {
			if (gridSize > 0) {
				line.accept(snapVisitor);
			}
			
			for (int i=1; i<line.getNumCoords(); i++) {
				if (edge.equals(line.getX(i-1), line.getY(i-1), line.getX(i), line.getY(i))) {
					return line;
				}
			}
		}
		
		return null;
	}
	
	public static void snapToGrid(Geometry g, double gridSize) {
		g.accept(new SnapToGridVisitor(gridSize));
	}

	/**
	 * Compares bearings identified by the two points; minimum is at negative x axis, going *clockwise* to (0, 1), (1, 0) and finally (-1, 0)
	 * Implementation is consistent with -compare(atan2(y1, x1), atan2(y2, x2)) 
	 */
	public static int compareBearing(double x1, double y1, double x2, double y2) {
		final int q1 = quad(x1, y1);
		final int q2 = quad(x2, y2);
		return (q1 != q2) //
			? -MathUtil.compare(q1, q2) : //
			  -MathUtil.compare(slope(x1, y1), slope(x2, y2));
	}

	private static double slope(double x, double y) {
		return x == 0 && y == 0 ? 0 : y / x;
	}

	/**
	 * 3 | 2
	 * -----
	 * 0 | 1 
	 */
	private static int quad(double x, double y) {
		return y < 0 ? (x < 0 ? 0 : 1) : (x < 0 ? 3 : 2);
	}

	public static HasCoordinate getInteriorPoint(LinearRing outer) {
		int level=1;
		Envelope env = outer.getEnvelope();
		while (level < 1024) { //quad-style scan
			double dx = env.getWidth()/level;
			double dy = env.getHeight()/level;
			double x=env.getMinX() + 0.5 * dx;
			for (int i = 0; i < level; i++) {
				double y=env.getMinY() + 0.5 * dy;
				for (int j = 0; j < level; j++) {
					if ((pointInRingRelation(x, y, outer) & PT_RING_INSIDE) != 0) {
						return new Position2D(x, y);
					}
					y+=dy;
				}
				x+=dx;
			}
		}
		return null;
	}
	
	public static double totalArea(Collection<? extends HasGeometry> geomHolders) {
		double area = 0;
		
		for (HasGeometry geomHolder : geomHolders) {
			Geometry g = geomHolder.getGeometry();
			if (g != null) {
				area += g.getArea();
			}
		}
		
		return area;
	}
}
 