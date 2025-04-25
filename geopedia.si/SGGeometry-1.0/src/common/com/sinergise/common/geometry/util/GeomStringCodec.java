package com.sinergise.common.geometry.util;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
/**
 * Class encodes geometry data to string (serialization for efficient data transport)
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class GeomStringCodec {
	public static final String numChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final int[] charToNum = new int['Z' + 1];
	static {
		int numl = charToNum.length;
		for (int a = 0; a < numl; a++)
			charToNum[a] = -1;

		int l = numChars.length();
		for (int a = 0; a < l; a++)
			charToNum[numChars.charAt(a)] = a;
	}

	public static Geometry fromString(String s) {
		if (s == null)
			return null;

		if (s.startsWith("1"))
			return fromString_v1(s);

		throw new IllegalStateException("Unknown version");
	}

	static Geometry fromString_v1(String s) {
		char type = s.charAt(1);
		int[] pos = new int[] { 2 };

		if (type == 'p') {
			double x = decCoord_v1(s, pos);
			double y = decCoord_v1(s, pos);
			return new Point(x, y);
		} else if (type == 'P') {
			int numPoints = decInt_v1(s, pos);
			Point[] points = new Point[numPoints];
			for (int a = 0; a < numPoints; a++) {
				double x = decCoord_v1(s, pos);
				double y = decCoord_v1(s, pos);
				points[a] = new Point(x, y);
			}
			return new MultiPoint(points);
		} else if (type == 'l') { // linestring
			int numCoords = decInt_v1(s, pos);
			double[] coords = new double[2 * numCoords];
			int cpos = 0;
			for (int a = 0; a < numCoords; a++) {
				coords[cpos++] = decCoord_v1(s, pos);
				coords[cpos++] = decCoord_v1(s, pos);
			}
			return new LineString(coords);
		} else if (type == 'L') { // multilinestring
			int numLines = decInt_v1(s, pos);
			LineString[] lines = new LineString[numLines];
			for (int a = 0; a < numLines; a++) {
				int numCoords = decInt_v1(s, pos);
				double[] coords = new double[2*numCoords];
				for (int b = 0; b < (numCoords*2); b++) {
					coords[b] = decCoord_v1(s, pos);
				}
				lines[a] = new LineString(coords);
			}
			return new MultiLineString(lines);
		} else if (type == 'y') { // poly
			int numHoles = decInt_v1(s, pos);
			Polygon p = new Polygon();
			p.holes = new LinearRing[numHoles];
			p.outer = decRing_v1(s, pos);
			for (int a = 0; a < numHoles; a++)
				p.holes[a] = decRing_v1(s, pos);
			return p;
		} else if (type == 'Y') { // multipoly
			int numPolys = decInt_v1(s, pos);
			Polygon[] polys = new Polygon[numPolys];
			for (int a = 0; a < numPolys; a++) {
				int nHoles = decInt_v1(s, pos);
				Polygon p = new Polygon();
				p.outer = decRing_v1(s, pos);
				p.holes = new LinearRing[nHoles];
				for (int b = 0; b < nHoles; b++)
					p.holes[b] = decRing_v1(s, pos);
				polys[a] = p;
			}
			return new MultiPolygon(polys);
		} else {
			throw new IllegalStateException("Invalid geom type");
		}
	}

	public static String toString(Geometry g) {
		if (g == null)
			return null;

		StringBuffer sb = new StringBuffer();
		sb.append('1'); // version

		if (g instanceof Point) {
			Point p = (Point) g;
			sb.append('p');
			encCoord_v1(sb, p.x);
			encCoord_v1(sb, p.y);
		} else if (g instanceof MultiPoint) {
			MultiPoint mp = (MultiPoint) g;
			int size = mp.size();

			sb.append('P');
			encInt_v1(sb, size);
			for (int a = 0; a < size; a++) {
				Point p = mp.get(a);
				encCoord_v1(sb, p.x);
				encCoord_v1(sb, p.y);
			}
		} else if (g instanceof LineString) {
			LineString ls = (LineString) g;
			int size = ls.getNumCoords();
			sb.append('l');
			encInt_v1(sb, size);
			double[] coords = ls.coords;
			int pos = 0;
			for (int a = 0; a < size; a++) {
				encCoord_v1(sb, coords[pos++]);
				encCoord_v1(sb, coords[pos++]);
			}
		} else if (g instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) g;
			int size = mls.size();
			sb.append('L');
			encInt_v1(sb, size);
			for (int a = 0; a < size; a++) {
				LineString ls = mls.get(a);
				int nCoords = ls.getNumCoords();
				encInt_v1(sb, nCoords);
				double[] coords = ls.coords;
//				int pos = 0;
				for (int b = 0; b < coords.length; b ++) {
					encCoord_v1(sb, coords[b]);
//					encCoord_v1(sb, coords[pos++]);
				}
			}
		} else if (g instanceof Polygon) {
			Polygon p = (Polygon) g;
			int nHoles = p.getNumHoles();
			sb.append('y');
			encInt_v1(sb, nHoles);
			encRing_v1(sb, p.outer);
			for (int a = 0; a < nHoles; a++)
				encRing_v1(sb, p.holes[a]);
		} else if (g instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) g;
			int size = mp.size();
			sb.append('Y');
			encInt_v1(sb, size);
			for (int a = 0; a < size; a++) {
				Polygon p = mp.get(a);
				int nHoles = p.getNumHoles();
				encInt_v1(sb, nHoles);
				encRing_v1(sb, p.outer);
				for (int b = 0; b < nHoles; b++)
					encRing_v1(sb, p.holes[b]);
			}
		} else {
			throw new IllegalStateException("Unknown geometry type");
		}

		return sb.toString();
	}

	static void encRing_v1(StringBuffer sb, LinearRing lr) {
		int nCoo = lr.getNumCoords();
		if (nCoo < 4)
			throw new IllegalStateException("Invalid linear ring");

		encInt_v1(sb, nCoo);
		int pos = 2;
		double[] coords = lr.coords;
		for (int a = 1; a < nCoo; a++) {
			encCoord_v1(sb, coords[pos++]);
			encCoord_v1(sb, coords[pos++]);
		}
	}

	static LinearRing decRing_v1(String s, int[] pos) {
		int nCoords = decInt_v1(s, pos);
		double[] coords = new double[nCoords];
		int cpos = 2;
		for (int a = 1; a < nCoords; a++) {
			coords[cpos++] = decCoord_v1(s, pos);
			coords[cpos++] = decCoord_v1(s, pos);
		}
		coords[0] = coords[cpos - 2];
		coords[1] = coords[cpos - 1];
		return new LinearRing(coords);
	}

	static void encInt_v1(StringBuffer sb, int val) {
		// String ss = Integer.toString(val, 36);
		String ss = Integer.toHexString(val);
		sb.append(numChars.charAt(ss.length()));
		sb.append(ss);
	}

	static int decInt_v1(String s, int[] pos) {
		int ppos = pos[0];
		int len = charToNum[s.charAt(ppos++)];
		// int result = Integer.parseInt(s.substring(ppos, ppos+len), 36);
		int result = Integer.parseInt(s.substring(ppos, ppos + len), 16);
		pos[0] = ppos + len;
		return result;
	}

	static final double COORD_RES_V1 = 0.0625;

	static final double INV_COORD_RES_V1 = 16; // == 1.0 / COORD_RES_V1;

	static void encCoord_v1(StringBuffer sb, double d) {
		long lval = Math.round(d * INV_COORD_RES_V1);
		if (lval == Long.MIN_VALUE || lval == Long.MAX_VALUE)
			throw new IllegalStateException("Absolute value too large");

		encLong_v1(sb, lval);
	}

	static double decCoord_v1(String s, int[] pos) {
		return decLong_v1(s, pos) * COORD_RES_V1;
	}

	static void encLong_v1(StringBuffer sb, long l) {
		// String ss = Long.toString(i, 36);
		String ss = Long.toHexString(l);
		sb.append(numChars.charAt(ss.length()));
		sb.append(ss);
	}

	static long decLong_v1(String s, int[] pos) {
		int ppos = pos[0];
		int len = charToNum[s.charAt(ppos++)];
		// long result = Long.parseLong(s.substring(ppos, ppos+len), 36);
		long result = Long.parseLong(s.substring(ppos, ppos + len), 16);
		pos[0] = ppos + len;
		return result;
	}
}