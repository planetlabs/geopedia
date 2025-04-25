package com.sinergise.common.util;

import java.util.Comparator;

public class Util {
	public static interface UtilImpl {
		double ceilPow2(double val);

		double floorPow2(double val);

		int hashCode(double dVal);
	}
	private static UtilImpl IMPL;
	
	public static void initImplInstance(final UtilImpl instance) {
		IMPL = instance;
	}

	public static final UtilImpl getImpl() {
		checkImpl();
		return IMPL;
	}
	
	private static void checkImpl() {
		CheckUtil.checkState(IMPL != null, "UtilImpl not initialized. Did you forget to call UtilJava.initStaticUtils() ?");
	}	
	
	/**
	 * @return <code>true</code> if both are <code>null</code> or <code>a</code> is not <code>null</code> and <code>a.equals(b)</code>
	 */
	public static boolean safeEquals(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b); 
	}
	
	public static boolean safeEquals(final char a, final Character b) {
		return b == null ? false : b.charValue() == a;
	}

	public static boolean safeEquals(final int a, final Integer b) {
		return b == null ? false : b.intValue() == a;
	}

	public static boolean safeEquals(final long a, final Long b) {
		return b == null ? false : b.longValue() == a;
	}

	public static boolean safeEquals(final short a, final Short b) {
		return b == null ? false : b.shortValue() == a;
	}
	
	/**
	 * NOTE: This method treats all NaN values as equal
	 */
	public static boolean safeEquals(final double a, final Double b) {
		return b == null ? false : b.isNaN() ? Double.isNaN(a) : b.doubleValue() == a;
	}

	/**
	 * NOTE: This method treats all NaN values as equal
	 */
	public static boolean safeEquals(final float a, final Float b) {
		return b == null ? false : b.isNaN() ? Float.isNaN(a) : b.floatValue() == a;
	}
	
	public static boolean safeEquals(final boolean a, final Boolean b) {
		return b == null ? false : b.booleanValue() == a;
	}

	public static boolean safeEquals(final byte a, final Byte b) {
		return b == null ? false : b.byteValue() == a;
	}
	
	public static <T> T ifnull(final T value, final T ifNull) {
		return value == null ? ifNull : value;
	}
	
	public static <T, R> R ifnull(final T value, final R ifNotNull, final R ifNull) {
		return iftrue(value != null, ifNotNull, ifNull);
	}
	
	public static <T> T iftrue(final boolean condition, final T ifTrue, final T ifFalse) {
		return condition ? ifTrue : ifFalse;
	}

	public static <T> int safeCompare(Comparable<? super T> o1, T o2, boolean nullGreater) {
		if (o1 == null) {
			return o2 != null ? nullGreater ? 1 : -1 : 0;
		}
		return o2 == null ? nullGreater ? -1 : 1 : o1.compareTo(o2);
	}
	
	public static <T> int safeCompare(T o1, T o2, Comparator<? super T> comp, boolean nullGreater) {
		if (o1 == null) {
			return o2 != null ? nullGreater ? 1 : -1 : 0;
		}
		return o2 == null ? nullGreater ? -1 : 1 : comp.compare(o1, o2);
	}

	public static int safeHashCode(Object val) {
		return val == null ? 0 : val.hashCode();
	}
	
	/**
	 * 
	 * @param val
	 * @return !val or null
	 */
	public static Boolean not(Boolean val) {
		return val  == null ? null : Boolean.valueOf(!val.booleanValue());
	}

	/**
	 * null-safe check for Boolean.TRUE
	 * null returns false
	 */
	public static boolean isTrue(Boolean val) {
		return isTrue(val, false);
	}
	
	public static boolean isTrue(Boolean val, boolean ifNull) {
		return val==null ? ifNull : val.booleanValue();
	}
	

	/**
	 * null-safe check for Boolean.FALSE
	 * null returns false 
	 */
	public static boolean isFalse(Boolean val) {
		return isFalse(val, false);
	}
	
	public static boolean isFalse(Boolean val, boolean ifNull) {
		return val == null ? ifNull : !val.booleanValue();
	}
	
	/**
	 * @param val
	 * @return null iff <code>val</code> is the last enum value
	 */
	public static <E extends Enum<E>> E next(E val) {
		E[] enumConstants = val.getDeclaringClass().getEnumConstants();
		final int idx = val.ordinal()+1;
		if (idx >= enumConstants.length) {
			return null;
		}
		return enumConstants[idx];
	}

	/**
	 * @param val
	 * @return null iff <code>val</code> is the first enum value
	 */
	public static <E extends Enum<E>> E previous(E val) {
		E[] enumConstants = val.getDeclaringClass().getEnumConstants();
		final int idx = val.ordinal() - 1;
		if (idx < 0) {
			return null;
		}
		return enumConstants[idx];
	}

	public static <T> T iftrue(Boolean value, T ifTrue, T ifFalseOrNull) {
		return iftrue(value, ifTrue, ifFalseOrNull, ifFalseOrNull);
	}

	
	public static <T> T iftrue(Boolean value, T ifTrue, T ifFalse, T ifNull) {
		return value == null ? ifNull : value.booleanValue() ? ifTrue : ifFalse;
	}
}
