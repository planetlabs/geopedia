/*
 *
 */
package com.sinergise.common.util.math.interpolation;

import java.util.Arrays;

/**
 * Used for interpolation when we have a map of {x,y} pairs and we want to get an interpolated value
 * 
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class Interpolation {
	
	
	public static final double bilinear(final double u, final double v, double val00, double val10, double val01, double val11) {
		final double u1 = 1 - u;
		final double v1 = 1 - v;
		return u1*v1*val00 + u*v1*val10 + u1*v*val01 + u*v*val11;
	}
	
	public static final <T extends ScalarMultiplicative<T>> T bilinear(final double u, final double v,  T val00, T val10, T val01, T val11) {
		final double u1 = 1 - u;
		final double v1 = 1 - v;
		return val00.multiply(u1*v1).plus(val10.multiply(u*v1)).plus(val01.multiply(u1*v)).plus(val11.multiply(u*v));
	}
	
	/**
	 * @param x
	 * @param pointX ordered array of x values
	 * @param pointY array of y values
	 * @return
	 */
	public static final double[] linear(final double x, final double[] pointX, final double[][] pointYs, double[] ret) {
		final int numVars = pointYs[0].length;
		final int numPts = pointX.length;
		final double[] toCpy;
		int idx = Arrays.binarySearch(pointX, x);
		if (idx >= 0) {
			toCpy = pointYs[idx];
		} else {
			idx = -(idx + 1);
			if (idx == 0) {
				toCpy = pointYs[0];
			} else if (idx == numPts) {
				toCpy = pointYs[numPts - 1];
			} else {
				final double lowX = pointX[idx - 1];
				final double ratio = (x - lowX) / (pointX[idx] - lowX);
				final double ratio1 = 1.0 - ratio;

				final double[] lowY = pointYs[idx - 1];
				final double[] highY = pointYs[idx];

				for (int i = 0; i < numVars; i++) {
					ret[i] = ratio1 * lowY[i] + ratio * highY[i];
				}
				return ret;
			}
		}
		System.arraycopy(toCpy, 0, ret, 0, numVars);
		return ret;
	}

	/**
	 * @param x
	 * @param pointX ordered array of x values
	 * @param pointY array of y values
	 * @return
	 */
	public static double[] nearestNeighbour(final double x, final double[] pointX, final double[][] pointYs) {
		int idx = Arrays.binarySearch(pointX, x);
		if (idx >= 0) { return pointYs[idx]; }
		idx = -(idx + 1);
		if (idx == pointX.length) { return pointYs[pointX.length - 1]; }
		return pointYs[idx];
	}
}
