/*
 *
 */
package com.sinergise.geopedia.core.util;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;

public class GeomUtils {
    private GeomUtils() {}
    
    
    
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
    
    public static double toDeg(double deg, double min, double sec) {
        return deg+(min+sec/60)/60;
    }
    
    /**
	 * Computes the distance from a point p to a line segment AB
	 * Note: NON-ROBUST!
	 * 
	 * @param p the point to compute the distance for
	 * @param A one point of the line
	 * @param B another point of the line (must be different to A)
	 * @return the distance from p to line segment AB
	 */
    public static final double distancePointLineSq(double px, double py, double Ax, double Ay, double Bx, double By) {
    	double Bx_Ax = Bx - Ax;
		double By_Ay = By - Ay;

		double len2 = ((Bx_Ax * Bx_Ax) + (By_Ay * By_Ay));
		if (len2 == 0) {
			return (px - Ax) * (px - Ax) + (py - Ay) * (py - Ay);
		}

		double px_Ax = px - Ax;
		double py_Ay = py - Ay;

		double r = ((px_Ax) * (Bx_Ax) + (py_Ay) * (By_Ay)) / (len2);

		if (r <= 0.0) {
			return (int)((px_Ax) * (px_Ax) + (py_Ay) * (py_Ay));
		}
		if (r >= 1.0) {
			double px_Bx = px - Bx;
			double py_By = py - By;

			return (int)((px_Bx * px_Bx) + (py_By * py_By));
		}

		double xp = Ax + r * (Bx_Ax);
		double yp = Ay + r * (By_Ay);

		double px_xp = px - xp;
		double py_yp = py - yp;

		return (int)((px_xp * px_xp) + (py_yp * py_yp));
    }

	public static void reverse(double[] coords)
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
    
    public static int getNumCoords(Geometry g) {
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
            @SuppressWarnings("unchecked")
			GeometryCollection<Geometry> col=(GeometryCollection<Geometry>)g;
            int ret=0;
            int size=col.size();
            for (int i = 0; i < size; i++) {
                ret+=getNumCoords(col.get(i));
            }
            return ret;
        }
        throw new IllegalArgumentException("Unknown Geometry class");
    }

	public static int indexOf(double[] seq, double x, double y)
    {
		int len=seq.length/2;
		for (int i = 0; i < len; i++) {
	        if (seq[2*i]==x && seq[2*i+1]==y) return i;
        }
	    return -1;
    }
	
	public static double distance(Point p1, Point p2) {
		return distance(p1.x,p1.y, p2.x, p2.y);
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(distanceSq(x1,y1,x2,y2));
	}

	public static double distanceSq(double x1, double y1, double x2, double y2) {
		double dx=(x2-x1);
		double dy=(y2-y1);
		return dx*dx+dy*dy;
	}
	
	public static double segArea(double x1, double y1, double x2, double y2) {
	    return 0.5 * (x2 - x1) * (y1 + y2);
	}

    public static double segArea(Point prev, Point next) {
        return segArea(prev.x,prev.y, next.x, next.y);
    }

    public static double distSq(LineString ls, double x, double y, double limitDistSq) {
        double minDistSq=Double.POSITIVE_INFINITY;
        double[] cords=ls.coords;
        int len=ls.getNumCoords();
        double oldX=0;
        double oldY=0;
        for (int i = 0; i < len; i++) {
            double curX=cords[2*i];
            double curY=cords[2*i+1];
            double curDistSq=distanceSq(curX, curY, x, y);
            if (curDistSq<minDistSq) {
                minDistSq=curDistSq;
                if (minDistSq<=limitDistSq) return minDistSq;
            }
            if (i==0) continue;
            curDistSq=distancePointLineSq(x, y, oldX, oldY, curX, curY);
            if (curDistSq<minDistSq) {
                minDistSq=curDistSq;
                if (minDistSq<=limitDistSq) return minDistSq;
            }
        }
        return minDistSq;
    }
	
}
