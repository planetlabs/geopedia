package com.sinergise.java.util;

import com.sinergise.common.util.Util;
import com.sinergise.common.util.Util.UtilImpl;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.java.util.format.JavaFormatProvider;
import com.sinergise.java.util.settings.LegacyTransformers;
import com.sinergise.java.util.url.JavaURLCoder;

public class UtilJava extends Util implements UtilImpl {
	private static final long DOUBLE_MANTISSA = 0x000fffffffffffffL;
	private static final long DOUBLE_EXP = 0x7ff0000000000000L;
	private static final long DOUBLE_SIGN = 0x8000000000000000L;
	private static final long DOUBLE_SIGN_EXP = DOUBLE_SIGN | DOUBLE_EXP;

	static {
		initImplInstance(new UtilJava());
		JavaFormatProvider.init();
		URLUtil.initCoder(new JavaURLCoder());
		try {
			Class.forName("com.sinergise.java.ui.UIUtilJava").getDeclaredMethod("initStaticUtils", new Class[]{}).invoke(null);
		} catch (Throwable t) {
		}
		try {
			Class.forName("com.sinergise.java.geometry.util.GeomUtilJava").getDeclaredMethod("initStaticUtils", new Class[]{}).invoke(null);
		} catch (Throwable t) {
		}
		LegacyTransformers.init();
	}

	public static void initStaticUtils() {
		//will force static initializer above
	}
	
	@Override
	public double floorPow2(double val) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(val) & DOUBLE_SIGN_EXP);
	}
	
	@Override
	public double ceilPow2(double val) {
		long bits = Double.doubleToRawLongBits(val);
		return (bits & DOUBLE_MANTISSA) == 0 ? val : 2*Double.longBitsToDouble(bits & DOUBLE_SIGN_EXP);
	}
	
	@Override
	public int hashCode(double dVal) {
		final long bits = Double.doubleToRawLongBits(dVal);
		return (int)(bits ^ (bits >>> 32));
	}
}
