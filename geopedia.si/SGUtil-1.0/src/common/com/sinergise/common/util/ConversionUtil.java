package com.sinergise.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ConversionUtil {
	public static Double toDouble(Object val) {
		if (val == null) return null;
		if (val instanceof Double) return (Double)val;
		if (val instanceof Number) return Double.valueOf(((Number)val).doubleValue());
		try {
			return Double.valueOf(val.toString());
		} catch (Exception e) {
			return null;
		}
	}
	
	public static double toDbl(Object val) {
		if (val == null) return Double.NaN;
		if (val instanceof Number) return ((Number)val).doubleValue();
		try {
			String sVal = val.toString();
			return Double.parseDouble(sVal);
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	public static BigDecimal toBigDecimal(Object val) {
		if (val == null) return null;
		if (val instanceof BigDecimal) return (BigDecimal)val;
		if (val instanceof BigInteger) return new BigDecimal((BigInteger)val);
		if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
			return BigDecimal.valueOf(((Number)val).longValue());
		} else if (val instanceof Number) {
			return BigDecimal.valueOf(((Number)val).doubleValue());
		}
		try {
			return new BigDecimal(String.valueOf(val));
		} catch (Exception e) {
		}
		return null;
	}

	public static Long toLong(Object val) {
		if (val == null) return null;
		if (val instanceof Long) return (Long)val;
		if (val instanceof Number) return Long.valueOf(((Number)val).longValue());
		try {
			String sVal = val.toString();
			return Long.valueOf(sVal);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Integer toInteger(Object val) {
		if (val == null) return null;
		if (val instanceof Integer) return (Integer)val;
		if (val instanceof Number) return Integer.valueOf(((Number)val).intValue());
		try {
			String sVal = val.toString();
			return Integer.valueOf(sVal);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static long toLng(Object val, long ifNull) {
		if (val == null) return ifNull;
		if (val instanceof Number) return ((Number)val).longValue();
		try {
			String sVal = val.toString();
			return Long.parseLong(sVal);
		} catch (Exception e) {
			return ifNull;
		}
	}
}
