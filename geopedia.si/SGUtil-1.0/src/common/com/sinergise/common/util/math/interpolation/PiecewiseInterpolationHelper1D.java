package com.sinergise.common.util.math.interpolation;

import java.util.Arrays;

public class PiecewiseInterpolationHelper1D {
	private double lowX = Double.POSITIVE_INFINITY;
	private double highX = Double.NEGATIVE_INFINITY;
	private double binFact = 0;
	public int binIndex = -1;
	private final double[] xVals;
	private final int xLen;
	private final int maxIdx;

	public PiecewiseInterpolationHelper1D(double[] xVals) {
		this.xVals = xVals;
		this.xLen = xVals.length;
		this.maxIdx = xLen - 1;
		exactMatch(0);
	}

	/**
	 * Returns a double value in the interval [0..1) which represents the relative x position within i-th bin of the source data.
	 * The bin index is stored in this object's binIndex field.
	 *  
	 * @param xVal
	 * @return
	 */
	public final double indexAndRatio(final double xVal) {
		if (xVal > highX) {
			return afterBinarySearch(xVal, Arrays.binarySearch(xVals, binIndex+1, xVals.length, xVal));
		}
		if (xVal < lowX) {
			return afterBinarySearch(xVal, Arrays.binarySearch(xVals, 0, binIndex, xVal));
		}
		return (xVal - lowX) * binFact;
	}

	private double afterBinarySearch(final double xVal, int idx) {
		if (idx >= 0) {
			return exactMatch(idx);
		}
		int newIdx = -(idx + 1);
		if (newIdx == 0) {
			return underflow();
		} else if (newIdx == xLen) {
			return overflow();
		}
		return binMatch(xVal, newIdx - 1);
	}

	private double binMatch(final double xVal, int bIdx) {
		binIndex = bIdx;
		lowX = xVals[binIndex];
		highX = xVals[binIndex + 1];
		binFact = 1.0 / (highX - lowX);
		return (xVal - lowX) * binFact;
	}

	private double overflow() {
		binIndex = maxIdx;
		lowX = xVals[maxIdx]; // Shouldn't be infty so that it multiplies with 0 into 0
		highX = Double.MAX_VALUE;
		binFact = 0;
		return 0;
	}

	private double underflow() {
		binIndex = 0;
		lowX = -Double.MAX_VALUE; // Shouldn't be infty so that it multiplies with 0 into 0
		highX = xVals[0];
		binFact = 0;
		return 0;
	}

	private double exactMatch(final int idx) {
		if (idx == maxIdx) {
			return overflow();
		}
		binIndex = idx;
		lowX = xVals[idx];
		highX = xVals[idx + 1];
		binFact = 1.0 / (highX - lowX);
		return 0;
	}
}