package com.sinergise.common.util.lang;

import com.sinergise.common.util.string.StringUtil;

public class TypeUtil {
	public static final Double toDouble(final Object val) {
		if (val == null) return null;
		if (val instanceof Double) return (Double)val;
		if (val instanceof Number) return Double.valueOf(((Number)val).doubleValue());
		return Double.valueOf((String)val);
	}

	public static Integer toInteger(final Object val) {
		if (val == null) return null;
		if (val instanceof Integer) return (Integer)val;
		if (val instanceof Number) return Integer.valueOf(((Number)val).intValue());
		return Integer.valueOf(val.toString());
	}

	public static Long toLong(final Object val) {
		if (val == null) return null;
		if (val instanceof Long) return (Long)val;
		if (val instanceof Number) return Long.valueOf(((Number)val).longValue());
		return Long.valueOf(val.toString());
	}

	public static Boolean toBoolean(final Object val) {
		if (val == null) return null;
		if (val instanceof Boolean) return (Boolean)val;
		return StringUtil.toBoolean(val.toString());
	}
	
	public static boolean toBoolean(final Object val, boolean ifUnclear) {
		Boolean ret = toBoolean(val);
		return ret==null? ifUnclear : ret.booleanValue();
	}
	
	public static String toString(final Object val) {
		return StringUtil.toString(val);
	}

	public static final Long boxL(final long val) {
		return Long.valueOf(val);
	}

	public static final Integer boxI(final int val) {
		return Integer.valueOf(val);
	}
	
	public static final Short boxS(final short val) {
		return Short.valueOf(val);
	}
	
	public static final Double boxD(final double val) {
		return Double.valueOf(val);
	}

	public static final Float boxF(final float val) {
		return Float.valueOf(val);
	}

	public static final Character boxC(final char val) {
		return Character.valueOf(val);
	}
	
	public static final Byte boxByte(final byte val) {
		return Byte.valueOf(val);
	}

	public static final Boolean boxB(final boolean val) {
		return (val ? Boolean.TRUE : Boolean.FALSE);
	}
	
	public static long unboxL(Long value, long ifNull) {
		return value == null ? ifNull : value.longValue();
	}

	public static int unboxI(Integer value, int ifNull) {
		return value == null ? ifNull : value.intValue();
	}

	public static short unboxS(Short value, short ifNull) {
		return value == null ? ifNull : value.shortValue();
	}

	public static double unboxD(Double value, double ifNull) {
		return value == null ? ifNull : value.doubleValue();
	}

	public static float unboxF(Float value, float ifNull) {
		return value == null ? ifNull : value.floatValue();
	}

	public static char unboxC(Character value, char ifNull) {
		return value == null ? ifNull : value.charValue();
	}

	public static byte unboxByte(Byte value, byte ifNull) {
		return value == null ? ifNull : value.byteValue();
	}

	public static boolean unboxB(Boolean value, boolean ifNull) {
		return value == null ? ifNull : value.booleanValue();
	}

	public static long unboxL(Long value) {
		return value.longValue();
	}

	public static int unboxI(Integer value) {
		return value.intValue();
	}

	public static short unboxS(Short value) {
		return value.shortValue();
	}

	public static double unboxD(Double value) {
		return value.doubleValue();
	}

	public static float unboxF(Float value) {
		return value.floatValue();
	}

	public static char unboxC(Character value) {
		return value.charValue();
	}

	public static byte unboxByte(Byte value) {
		return value.byteValue();
	}

	public static boolean unboxB(Boolean value) {
		return value.booleanValue();
	}
}
