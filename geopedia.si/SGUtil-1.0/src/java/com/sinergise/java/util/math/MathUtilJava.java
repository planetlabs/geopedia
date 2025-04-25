package com.sinergise.java.util.math;

import com.sinergise.common.util.math.MathUtil;

public class MathUtilJava extends MathUtil {
	
	private static final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0d);
	
	public static boolean isNegativeZero(final double val) {
		return (val == 0 && Double.doubleToLongBits(val) == NEG_ZERO_BITS);
	}
	
	public static boolean isPositiveZero(final double val) {
		return (val == 0 && Double.doubleToLongBits(val) != NEG_ZERO_BITS);
	}
	
	public static final int randomInt(int min, int max) {
		return rescaleUnity(Math.random(), min, max);
	}
	
	public static final int rescaleUnity(double d01, int min, int max) {
		return min + (int)(d01*unityToIntFactor(min, max));
	}
	
	public static final double belowFloor(double d) {
		double df = Math.floor(d); 
		return df - Math.ulp(df);
	}

	public static final float belowFloor(float f) {
		float ff = (float)Math.floor(f); 
		return ff - Math.ulp(ff);
	}
	
	public static final double unityToIntFactor(int min, int max) {
		return belowFloor((double)(max - min + 1));
	}

	@SuppressWarnings("cast")
	public static final float unityToIntFactorFloat(int min, int max) {
		return belowFloor((float)(max - min + 1));
	}

	public static boolean randomBoolean() {
		return Math.random() >= 0.5;
	}
}
