package com.sinergise.java.raster.colorfilter;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.common.util.math.MathUtil;

public class BandStretch extends ColorFilter {
	public static final String	TYPE_BAND_STRETCH	= "BandStretch";
	private double[] mins;
	private double[] factors;
	
	public static BandStretch createWithMeanAndWidth(double[] mean255, double[] width255) {
		double[] mins = new double[3];
		double[] factors = new double[3];
		for (int i = 0; i < 3; i++) {
			mins[i] = mean255[i] - width255[i]/2;
			factors[i] = 255 / width255[i];
		}
		return new BandStretch(mins, factors);
	}

	public static BandStretch createWithMinAndMax(double[] min255, double[] max255) {
		double[] factors = new double[3];
		for (int i = 0; i < 3; i++) {
			factors[i] = 255 / Math.max(1,max255[i] - min255[i]);
		}
		return new BandStretch(min255, factors);
	}
	
	public BandStretch(double[] mins, double[] factors) {
		super(TYPE_BAND_STRETCH);
		this.mins = mins;
		this.factors = factors;
	}
	
	@Override
	public boolean filter(int[] rgba) {
		for (int i = 0; i < 3; i++) {
			rgba[i] = ColorUtil.clip(MathUtil.roundToInt((rgba[i] - mins[i]) * factors[i]), 0, 255);
		}
		return true;
	}

	@Override
	public int getNumComponents(int inputSampSize) {
		return inputSampSize;
	}

	@Override
	public void appendIdentifier(StringBuffer out) {
		out.append(type);
		for (double min : mins) {
			out.append(min);
		}
		for (double f : factors) {
			out.append(f);
		}
	}
}
