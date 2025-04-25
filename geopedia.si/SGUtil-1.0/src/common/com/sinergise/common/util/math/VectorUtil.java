package com.sinergise.common.util.math;

import static com.sinergise.common.util.math.AngleUtil.signedNormalAngle;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import com.sinergise.common.util.geom.Position2D;

public class VectorUtil {
	private VectorUtil() {}

	public static final double lengthSq(final double dx, final double dy) {
		return dot(dx, dy, dx, dy);
	}

	public static final double length(final double dx, final double dy) {
		return sqrt(dot(dx, dy, dx, dy));
	}

	public static final double dot(final double ux, final double uy, final double vx, final double vy) {
		return ux * vx + uy * vy;
	}

	public static final double perpDot(final double ux, final double uy, final double vx, final double vy) {
		return dot(-uy, ux, vx, vy);
	}

	public static final double crossZ(final double ux, final double uy, final double vx, final double vy) {
		return dot(-uy, ux, vx, vy);
	}

	public static final double perpCrossZ(final double ux, final double uy, final double vx, final double vy) {
		return -dot(ux, uy, vx, vy);
	}

	public static double bearing(final double ux, final double uy) {
		return atan2(uy, ux);
	}

	public static double bearingTan(final double ux, final double uy) {
		return uy/ux;
	}

	public static double bearingSin(final double ux, final double uy) {
		return uy/length(ux, uy);
	}

	public static double bearingSinSq(final double ux, final double uy) {
		return uy*uy/lengthSq(ux, uy);
	}
	
	public static double bearingCos(final double ux, final double uy) {
		return ux/length(ux, uy);
	}

	public static double bearingCosSq(final double ux, final double uy) {
		return ux*ux/lengthSq(ux, uy);
	}

	public static double bisectorBearing(final double ux, final double uy, final double vx, final double vy) {
		Position2D vec = bisectorVector(ux, uy, vx, vy);
		return signedNormalAngle(bearing(vec.x, vec.y));
	}

	/**
	 * Calculates a (non-normalized) vector that bisects the (acute) angle between the given input vectors
	 */
	public static Position2D bisectorVector(final double ux, final double uy, final double vx, final double vy) {
		double lenSqU = lengthSq(ux, uy);
		double lenSqV = lengthSq(vx, vy);
		final double retX;
		final double retY;
		if (lenSqU == lenSqV) {
			retX = ux + vx;
			retY = uy + vy;
		} else {
			double lenFact = Math.sqrt(lenSqU / lenSqV); 
			retX = ux + lenFact*vx;
			retY = uy + lenFact*vy;
		}
		if (retX == 0 && retY == 0) {
			//Opposing vectors, return a vector orthogonal to the first parameter
			return new Position2D(-uy, ux);
		}
		return new Position2D(retX, retY);
	}
	
	public static double vectorAngle(final double ux, final double uy, final double vx, final double vy) {
		return atan2(crossZ(ux, uy, vx, vy), dot(ux, uy, vx, vy));
	}

	public static double vectorAngleTan(final double ux, final double uy, final double vx, final double vy) {
		return crossZ(ux, uy, vx, vy) / dot(ux, uy, vx, vy);
	}

	public static double vectorAngleSin(final double ux, final double uy, final double vx, final double vy) {
		final double y = crossZ(ux, uy, vx, vy);
		return y / sqrt(lengthSq(ux, uy) * lengthSq(vx, vy));
	}

	public static double vectorAngleSinSq(final double ux, final double uy, final double vx, final double vy) {
		final double cot = dot(ux, uy, vx, vy) / crossZ(ux, uy, vx, vy);
		return 1.0 / (1.0 + cot * cot);
	}

	public static double vectorAngleCos(final double ux, final double uy, final double vx, final double vy) {
		final double x = dot(ux, uy, vx, vy);
		return x / sqrt(lengthSq(ux, uy) * lengthSq(vx, vy));
	}

	public static double vectorAngleCosSq(final double ux, final double uy, final double vx, final double vy) {
		final double tan = crossZ(ux, uy, vx, vy) / dot(ux, uy, vx, vy);
		return 1.0 / (1.0 + tan * tan);
	}

	public static Position2D normalize(Position2D pos) {
		return pos.times(1.0 / pos.distanceFromOrigin());
	}

	public static double projection(Position2D vector, Position2D normalTarget) {
		return dot(vector.x, vector.y, normalTarget.x, normalTarget.y);
	}

	public static double rejection(Position2D vector, Position2D normalTarget) {
		return perpDot(vector.x, vector.y, normalTarget.x, normalTarget.y);
	}
}