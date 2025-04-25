/*
 *
 */
package com.sinergise.common.util.math;

import static com.sinergise.common.util.math.MathUtil.INV_PI_4;
import static com.sinergise.common.util.math.MathUtil.PI_2;
import static com.sinergise.common.util.math.MathUtil.PI_3_4;
import static com.sinergise.common.util.math.MathUtil.PI_4;
import static com.sinergise.common.util.math.MathUtil.TWO_PI;
import static com.sinergise.common.util.math.MathUtil.isNegativeZero;
import static com.sinergise.common.util.math.MathUtil.isPositiveZero;
import static com.sinergise.common.util.math.MathUtil.mod;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Computational geometry utility
 */
public class AngleUtil {
	/**
	 * Warning this method's performance is not very good
	 * 
	 * @param angleRad
	 * @return
	 */
	public static double angleToPseudo(final double angleRad) {
		return pseudoATan2(sin(angleRad), cos(angleRad));
	}

	public static double degreesToPseudo(final double angdeg) {
		return angleToPseudo(toRadians(angdeg));
	}

	/**
	 * Warning this method's performance is not very good
	 * 
	 * @param pseudoRad
	 * @return
	 */
	public static double pseudoToAngle(double pseudoRad) {
		if (pseudoRad == -PI) {
			return -PI;
		}
		pseudoRad *= INV_PI_4;
		if (pseudoRad < -3) {
			return atan2(-pseudoRad - 4, -1);

		} else if (pseudoRad < -1) {
			return atan2(-1, pseudoRad + 2);

		} else if (pseudoRad <= 1) {
			return atan2(pseudoRad, 1);

		} else if (pseudoRad <= 3) {
			return atan2(1, 2 - pseudoRad);

		} else {
			return atan2(4 - pseudoRad, -1);
		}
	}
	
	public static double pseudoATan2(final double y, final double x) {
		if (x > y) {
			if (x > -y) {
				return PI_4 * (y / x); // -pi/4 .. pi/4
			}
			if (x == -y) {
				return -PI_4; // handles (Inf, -Inf)
			}
			return -PI_4 * (2.0 + x / y); // -3pi/4 .. -pi/4
		}
		if (y > -x) {
			if (x == y) {
				return PI_4; // handles Inf
			}
			return PI_4 * (2.0 - x / y); // pi/4 .. 3pi/4
		}
		if (x == 0) {
			return isNegativeZero(x) //
				? (isNegativeZero(y) ? -PI : Double.isNaN(y) ? y : PI) //
				: y;
		}
		if (y > 0 || isPositiveZero(y)) {
			if (y == -x) {
				return PI_3_4; // Handles Inf
			}
			return PI_4 * (y / x + 4.0); // 3pi/4 .. pi
		}
		if (x == y) {
			return -PI_3_4; // Handles Inf
		}
		return PI_4 * (y / x - 4.0); // -pi .. -3pi/4
	}
	
	public static double pseudoATan(final double val) {
		return (val >= -1) //
			? (val <= 1) //
				? PI_4 * val //
				: PI_4 * (2 - 1 / val) //
			: -PI_4 * (2 + 1 / val);
	}

	/**
	 * Inverse of {@link #pseudoATan(double)}
	 * 
	 * @param val
	 * @return
	 */
	public static double tanPseudo(double pseudoAng) {
		// Wrap around to [-PI/4, 3PI/4]
		while (pseudoAng > PI_3_4) {
			pseudoAng -= PI;
		}
		if (pseudoAng == -PI_2) {
			return Double.NEGATIVE_INFINITY;
		}
		while (pseudoAng < -PI_4) {
			pseudoAng += PI;
		}

		if (pseudoAng <= PI_4) {
			return INV_PI_4 * pseudoAng;
		}
		return 1.0 / (2.0 - pseudoAng * INV_PI_4);
	}

	public static double vectorPseudoAngle(final double ux, final double uy, final double vx, final double vy) {
		return pseudoATan2(ux * vy - uy * vx, ux * vx + uy * vy);
	}

	public static double vectorAngle(final double ux, final double uy, final double vx, final double vy) {
		return atan2(ux * vy - uy * vx, ux * vx + uy * vy);
	}

	/**
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return The angle left of the first segment at point (x1, y1), in the range of [0, 2 PI)
	 */
	public static double angle(final double x0, final double y0, final double x1, final double y1, final double x2, final double y2) {
		return -(vectorAngle(x1 - x0, y1 - y0, x2 - x1, y2 - y1) - PI);
	}

	public static double angleDeg(final double x0, final double y0, final double x1, final double y1, final double x2, final double y2) {
		return toDegrees(angle(x0, y0, x1, y1, x2, y2));
	}

	public static double pseudoAngle(final double x0, final double y0, final double x1, final double y1, final double x2, final double y2) {
		return abs(vectorPseudoAngle(x1 - x0, y1 - y0, x1 - x2, y1 - y2));
	}

	public static double sinPseudo(final double pseudoAng) {
		final double tan = tanPseudo(pseudoAng);
		return 1 / sqrt(1 + 1 / (tan * tan));
	}

	/**
	 * @param rad angle in radians
	 * @return angle normalized to lie in the interval (-Pi, Pi]
	 */
	public static double signedNormalAngle(double rad) {
		// Use sign to force interval closing on the right
		return -mod(-rad, TWO_PI, -PI);
	}

	/**
	 * @param rad angle in radians
	 * @return angle normalized to lie in the interval [0, 2Pi)
	 */
	public static double positiveNormalAngle(double rad) {
		return MathUtil.mod(rad, TWO_PI);
	}

	public static final double toDeg(double deg, double min, double sec) {
		if (Double.isNaN(min)) {
			min = 0;
		}
		if (Double.isNaN(sec)) {
			sec = 0;
		}
		return deg + (min + sec / 60) / 60;
	}

}
