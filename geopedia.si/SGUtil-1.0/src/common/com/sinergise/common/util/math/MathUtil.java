/*
 *
 */
package com.sinergise.common.util.math;

import static java.lang.Math.PI;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.sinergise.common.util.Util;
import com.sinergise.common.util.lang.Function;

public class MathUtil {
	public static final double DEGREE_IN_RAD  = 0.01745329251994329576923690768488612713442871888541725456097191440171;
	public static final double GOLDEN_RATIO   = 1.61803398874989484820458683436563811772;
	
	public static final double INV_PI_2       = 0.636619772367581343075535053490057448137838582961825794990669376235587190536906140360455211065012343824291370907031832;
	public static final double INV_PI_4       = 1.2732395447351626861510701069801148962756771659236515899813387524711743810738122807209104221300246876485827418140636643;
	
	public static final double LN_10          = 2.302585092994045684017991454684364207601;
	public static final double LN_2           = 0.6931471805599453094172321214581765680755001343602552541206800095;
	public static final double LOG2_E         = 1.442695040888963407359924681001892137426645954152985934135449407;
	public static final double INV_LN_2       = LOG2_E;
	
	//Should be "012..Z".toCharArray() but GWT doesn't handle static initialization code very well
	public static final char[] NUMERAL_CHARS  = new char[] {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public static final double PI_2           = PI * 0.5;
	public static final double PI_3_4         = PI * 0.75;
	public static final double PI_4           = PI * 0.25;
	public static final double TWO_PI 		  = PI * 2;
	
	
	public static final double RAD_IN_DEGREES = 57.2957795130823208767981548141051703324054724665643215491602438612;
	public static final double SEC_IN_RAD     = 0.000004848136811095359935899141023579479759563533023727015155825531778;
	/**
	 * arcsecond
	 */
	public static final double AS_IN_RAD     = 4.848136811095359935899141023579479759563533023727015155825531778252803096120692899117337693429193006e-6;
	/**
	 * milliarcsecond
	 */
	public static final double MAS_IN_RAD     = 4.848136811095359935899141023579479759563533023727015155825531778252803096120692899117337693429193006e-9;
	
	public static final double SQRT2          = 1.4142135623730951454746218587388284504413604736328125;
	public static final double INV_SQRT2      = 0.707106781186547524400844362104849039284835937688474036588;
	
	public static final double BELOW_ONE	  = 0.9999999999999999; // Should be exactly as many digits
	public static final float  BELOW_ONE_F    = 0.99999994f; // Should be exactly as many digits
	
	public static final int TWO_POW_30 = 0x40000000;
	
	public static double atan2(final double y, final double x) {
		if (x == 0) {
			if (y > 0) {
				return Math.PI / 2;
			} else if (y < 0) {
				return -Math.PI / 2;
			}
			return 0;
		} else if (x < 0) {
			if (y < 0) {
				return Math.atan(y / x) - Math.PI;
			}
			return Math.atan(y / x) + Math.PI;
		} else {
			return Math.atan(y / x);
		}
	}
	
	/**
	 * @param min minimal bounds (inclusive)
	 * @param value value to compare
	 * @param max maximal bounds (inclusive)
	 * @return min <= value <= max
	 */
	public static boolean between(final double min, final double value, final double max) {
		return min <= value && value <= max;
	}
	
	public static boolean between(final long min, final long value, final long max) {
		return min <= value && value <= max;
	}
	
	public static boolean between(final int min, final int value, final int max) {
		return min <= value && value <= max;
	}
	

	public static <T extends Comparable<? super T>> boolean between(T min, T value, T max) {
		return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
	}
	
	public static final int bitCount(int i) {
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		i = (i + (i >>> 4)) & 0x0f0f0f0f;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;
	}

	private static final int[] MultiplyDeBruijnBitPosition2 = new int[] {0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};

	public static int log2forPow2(int pow2) {
		return MultiplyDeBruijnBitPosition2[(pow2 * 0x077CB531) >>> 27];
	}

	public static int ceilPow2Int(int x) {
		x--;
	    x |= x >> 1;
	    x |= x >> 2;
	    x |= x >> 4;
	    x |= x >> 8;
	    x |= x >> 16;
	    return ++x;
	}

	public static int floorPow2Int(int x) {
	    return ceilPow2Int(x+1) >>> 1;
	}
	
	public static int numSignificantBits(int x) {
		if (x==0) return 0;
		return log2forPow2(floorPow2Int(x))+1;
	}
	
	public static int bitCount(long i) {
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		return (int)i & 0x7f;
	}

	public static float clamp(final float min, final float val, final float max) {
		return val < min ? min : val > max ? max : val;
	}

	public static double clamp(final double min, final double val, final double max) {
		return val < min ? min : val > max ? max : val;
	}
	
	public static int clamp(final int min, final int val, final int max) {
		return val < min ? min : val > max ? max : val;
	}
	
	public static final int distSq(final int dx, final int dy) {
		return dx * dx + dy * dy;
	}
	
	public static final int distSq(final int x0, final int y0, final int x1, final int y1) {
		return distSq(x1 - x0, y1 - y0);
	}
	
	public static int extractExp(final double value, final double base) {
		return floorInt(log(value * (1 + 1e-6)) / log(base));
	}
	
	public static final int floorInt(final double d) {
		return d<0 ? (int)(d-1) : (int)d;
	}
	
	public static long fromHex(final String hexString) {
		return Long.parseLong(hexString, 16);
	}
	
	public static double fromLogRatio(final double ratio, final double min, final double max) {
		return min * Math.exp(ratio * log(max / min));// log(value/min)/log(max/min);
	}
	
	public static int hashCode(final double dVal) {
		return Util.getImpl().hashCode(dVal);
	}
	
	public static int hashCode(final float fVal) {
		return hashCode((double)fVal);
	}
	
	public static int hashCode(final long lVal) {
		return (int)(lVal ^ (lVal >>> 32));
	}
	
	public static double hypot(final double x, final double y) {
		return Math.sqrt(x * x + y * y);
	}

	public static double hypotSq(final double x, final double y) {
		return x * x + y * y;
	}
	
	public static void intToHexLE(int value, final StringBuffer out) {
		for (int a = 0; a < 4; a++) {
			int v = (value >>> 4) & 15;
			if (v < 10) {
				out.append((char)('0' + v));
			} else {
				out.append((char)(('a' - 10) + v));
			}
			v = value & 15;
			if (v < 10) {
				out.append((char)('0' + v));
			} else {
				out.append((char)(('a' - 10) + v));
			}
			
			value >>>= 8;
		}
	}
	
	public static double invertIfLarge(final double val) {
		return val <= 1 ? val : (1.0 / val);
	}
	
	public static double invertIfSmall(final double val) {
		return val >= 1 ? val : (1.0 / val);
	}
	
	public static boolean isNegativeZero(final double val) {
		return (val == 0 && 1.0 / val < 0);
	}
	
	public static boolean isPositiveZero(final double val) {
		return (val == 0 && 1.0 / val > 0);
	}
	
	public static double log(final double value) {
		if (value == 10) {
			return LN_10;
		}
		if (value == 1) {
			return 0;
		}
		if (value == 2) {
			return LN_2;
		}
		if (value == Math.E) {
			return 1;
		}
		return Math.log(value);
	}
	
	public static int floorLog2(double value) {
		return (int)Math.floor(log2(value));
	}
	
	public static double log2(double value) {
		// Use the < 1 branch so that subnormal doubles get handled properly
		if (value > 1.0) {
			return -log2forLt1(1.0/value);
		}
		return log2forLt1(value);
	}
	
	private static double log2forLt1(double value) {
		assert value <= 1;
		
		int cnt = 0;
		while (value < 3.7252902984619140625e-9) { // (2^-28) divide to increase precision
			if (value == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			cnt += 28;
			value *= 268435456;//2^28
		}
		return -cnt + (Math.log(value) / LN_2);
	}
	
	public static double logRatio(final double min, final double value, final double max) {
		return log(value / min) / log(max / min);
	}
	
	public static double[] powers(final double d, final int i) {
		final double[] ret = new double[i + 1];
		ret[0] = 1;
		if (i == 0) {
			return ret;
		}
		ret[1] = d;
		if (i == 1) {
			return ret;
		}
		if (i < 2) {
			return ret;
		}
		for (int j = 2; j <= i; j++) {
			ret[j] = Math.pow(d, j);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param value
	 * @param decPrefixes list of values between 1 (inclusive) and 10 (exclusive) that the value should be rounded to 
	 * @param retPrefixExp array of {prefix, exponent} if needed as a result
	 * @return rounded value (prefix * 10^exponent)
	 */
	public static double roundToList(final double value, final double[] decPrefixes, final double[] retPrefixExp) {
		final double exp = extractExp(value, 10);
		final double pref = value * Math.pow(10, -exp);
		int i = 0;
		
		double minRatio = invertIfSmall(10 * decPrefixes[0] / pref);
		retPrefixExp[0] = decPrefixes[0];
		retPrefixExp[1] = exp + 1;
		
		while (i < decPrefixes.length) {
			final double curRatio = invertIfSmall(decPrefixes[i] / pref);
			if (curRatio < minRatio) {
				minRatio = curRatio;
				retPrefixExp[0] = decPrefixes[i];
				retPrefixExp[1] = exp;
			}
			i++;
		}
		return retPrefixExp[0] * Math.pow(10, retPrefixExp[1]);
	}
	
	public static double roundToDecimal(final double value, final double maxResidual) {
		if (maxResidual > 1) {
			long factr = (long)Math.pow(10,Math.floor(Math.log10(maxResidual)));
			return roundToNearestMultiple(value, factr);
		}
		long factr = (long)Math.pow(10,Math.ceil(Math.log10(1/maxResidual)));
		return Math.rint(value*factr)/factr;
	}
	
	/**
	 * Returns floor(argument) for positive, and ceil(argument) for negative arguments.
	 * Effectively rounding towards zero.
	 */
	public static final double floorAbs(double argument) {
		return argument<0?Math.ceil(argument):Math.floor(argument);
	}

	/**
	 * Returns ceil(argument) for positive, and floor(argument) for negative arguments.
	 * Effectively rounding away from zero. 
	 */
	public static final double ceilAbs(double argument) {
		return argument<0?Math.floor(argument):Math.ceil(argument);
	}
	
	public static double roundWithBase(final double base, final double value, final double maxResidual) {
		if (maxResidual<=0) return value;
		double logRes = Math.log(maxResidual)/Math.log(base);
		double fact = Math.pow(base, Math.floor(logRes)); // Start with minimal factor that makes sense
		
		double ret = 0;
		double tmp = 0;
		do {
			ret = tmp;
			if (fact < 1) {
				double f1 = Math.rint(1.0/fact);
				tmp = Math.rint(value*f1)/f1;
			} else {
				tmp = roundToNearestMultiple(value, fact);
			}
			fact *= base;
		} while (Math.abs(tmp-value) < maxResidual);
		return ret;
	}
	
	public static final Appendable toHex(final long value, final int len, Appendable out) throws IOException {
		final long val = value;
		int shift = 4 * (len - 1); 
		long mask = 0xFL << (4*(len - 1));
		do {
		    out.append(digitChar((int)((val & mask) >>> shift)));
		    mask >>>= 4;
			shift-=4;
		} while (shift>=0);
		return out;
	}

	public static final Appendable toHex(final int value, final int len, Appendable out) throws IOException {
		final int val = value;
		int shift = 4 * (len - 1); 
		int mask = 0xF << (4*(len - 1));
		do {
		    out.append(digitChar((val & mask) >>> shift));
		    mask >>>= 4;
			shift-=4;
		} while (shift>=0);
		return out;
	}
	
	public static final StringBuilder toHex(final long value, final int len) {
		try {
			return (StringBuilder)toHex(value, len, new StringBuilder(len));
		} catch(IOException e) {
			throw new RuntimeException(e); // should never happen
		}
	}
	
	public static final StringBuilder toHex(final int value, final int len) {
		try {
			return (StringBuilder)toHex(value, len, new StringBuilder(len));
		} catch(IOException e) {
			throw new RuntimeException(e); // should never happen
		}
	}
	
	public static final char digitChar(int digit) {
        if (digit < 10) return (char)('0' + digit);
        return (char)('a' - 10 + digit);
	}
	
	/**
	 * Treats all NaNs as equal. -0 is not equal to 0
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(double a, double b) {
		return isNegativeZero(a) ? isNegativeZero(b) : a == b || (a != a && b != b);
	}

	public static final void normalize(final double[] vector, final double[] ret) {
		final int len1 = vector.length-1;
		double sum = 0;
		for (int i = len1; i >= 0; i--) {
			final double val = vector[i];
			sum += val*val;
		}
		final double factor = Math.sqrt(sum);
		for (int i = len1; i >= 0; i--) {
			ret[i] = vector[i] / factor;
		}
	}

	public static double[] tabulateDbl(double min, double max, int num, Function<Double, ? extends Number> func) {
		final double[] ret = new double[num];
		final int num1 = num - 1;
		for (int i = 0; i < num; i++) {
			ret[i] = func.execute(Double.valueOf((min*(num1 - i) + max*i)/num1)).doubleValue();
		}
		return ret;
	}
	
	public static float[] tabulateFlt(float min, float max, int num, Function<Float, ? extends Number> func) {
		final float[] ret = new float[num];
		final double num1 = num - 1;
		for (int i = 0; i < num; i++) {
			ret[i] = func.execute(Float.valueOf((float)((min*(num1 - i) + max*i)/num1))).floatValue();
		}
		return ret;
	}

	public static double sum(float[] vals, int off, int length) {
		double ret = 0;
		for (int i = off+length-1; i>=off; i--) {
			ret += vals[i];
		}
		return ret;
	}
	
	public static double sum(double[] vals, int off, int length) {
		double ret = 0;
		for (int i = off+length-1; i>=off; i--) {
			ret += vals[i];
		}
		return ret;
	}
	
	public static int ushortAverage(short a, short b) {
		return (1 + (a & 0xFFFF) + (b & 0xFFFF)) >>> 1;
	}
	
	/**
	 * @param val
	 * @param modulo
	 * @param offset
	 * @return value in interval [offset, offset+modulo)
	 */
	public static double mod(double val, double modulo, double offset) {
		return offset + mod(val - offset, modulo);
	}

	public static double mod(double d, double modulo) {
		return d >= 0 ? d % modulo : (modulo + d % modulo) % modulo; //double negative to properly handle boundary
	}

	public static int mod(int val, int modulo) {
		return val >= 0 ? val % modulo : (modulo + val % modulo) % modulo; //double negative to properly handle boundary
	}
	
	public static int hexDigitValue(char hexDigit) {
		return hexDigit <= '9' ? (hexDigit - '0') : hexDigit <= 'Z' ? (10 + hexDigit - 'A') : (10 + hexDigit - 'a');
	}
	
	public static final double sqr(final double a) {
		return a*a;
	}
	
	public static final BigDecimal toBigDecimal(final Number a) {
		if (a==null) return null;
		// Check for double first, as this is usually more common
		if (a instanceof Double) return BigDecimal.valueOf(a.doubleValue());
		if (a instanceof BigDecimal) return (BigDecimal)a;
		if (a instanceof BigInteger) return new BigDecimal((BigInteger)a);
		if (a instanceof Long || a instanceof Integer || a instanceof Short || a instanceof Byte) return BigDecimal.valueOf(a.longValue());
		return BigDecimal.valueOf(a.doubleValue());
	}

	public static final BigDecimal toBigDecimal(final Object a) throws NumberFormatException {
		if (a==null) {
			return null;
		}
		// Check for number first, as this is usually more common
		if (a instanceof Number) {
			return toBigDecimal((Number)a);
		}
		return new BigDecimal(String.valueOf(a));
	}

	public static int roundToInt(double value) {
		long rnd = Math.round(value);
		if (rnd > Integer.MAX_VALUE || rnd < Integer.MIN_VALUE) {
			throw new IllegalArgumentException("Can't round values larger than max integer");
		}
		return (int)rnd;
	}
	
	public static int compare(int a, int b) {
		return a != b ? a > b ? 1 : -1 : 0;
	}
	
	public static int compare(long a, long b) {
		return a != b ? a > b ? 1 : -1 : 0;
	}
	
	/**
	 * NaN will not give consistent results; 0 equals -0
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int fastCompare(double a, double b) {
		return a != b ? a > b ? 1 : -1 : 0;
	}
	
	public static int compare(double first, double second) {
		return Double.compare(first, second);
	}

	public static double roundToNearestMultiple(double value, double stepSize) {
		return floorToMultiple(value + 0.5*stepSize, stepSize);
	}

	public static double floorToMultiple(double value, double stepSize) {
		return value - mod(value, stepSize);
	}
	
	public static int floorToMultiple(int value, int stepSize) {
		return value - mod(value, stepSize);
	}
	
	/**
	 * Integer division with rounding towards negative infinity;
	 * div(-5, 3) = -2   whereas -5/3 = -1 
	 * 
	 * @param dividend may be negative
	 * @param divisor greater than 0
	 * @return
	 */
	public static int divFloor(int dividend, int divisor) {
		return dividend >= 0 ? dividend/divisor : (dividend + 1)/divisor - 1;
	}
	
	public static short floorToMultiple(short value, short stepSize) {
		return (short)(value - mod(value, stepSize));
	}

	public static double powerOf2(int exp) {
		return scalb(1, exp);
	}

	public static double ceilPow2(double val) {
		assert val >= 0;
		return Util.getImpl().ceilPow2(val);
	}

	public static double floorPow2(double val) {
		assert val >= 0;
		return Util.getImpl().floorPow2(val);
	}
	
	/**
	 * @deprecated use ceilPow2
	 */
	@Deprecated
	public static double nextPow2(double val) {
		return ceilPow2(val);
	}

	/**
	 * @see Math#scalb(double, int)
	 */
	public static double scalb(double val, int exp) {
		if (exp < 0) {
			do {
				exp += 30;
				val /= TWO_POW_30;
			} while (exp < 0);
		} else {
			while (exp > 30) {
				val *= TWO_POW_30;
				exp -= 30;
			}
		}
		switch (exp) {
			case 0: return val;
			case 1: return val * 0x2;
			case 2: return val * 0x4;
			case 3: return val * 0x8;
			case 4: return val * 0x10;
			case 5: return val * 0x20;
			case 6: return val * 0x40;
			case 7: return val * 0x80;
			case 8: return val * 0x100;
			case 9: return val * 0x200;
			default: return val * (1 << exp); 
		}
	}
}
