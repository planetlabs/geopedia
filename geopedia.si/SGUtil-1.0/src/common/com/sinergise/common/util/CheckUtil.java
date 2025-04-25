package com.sinergise.common.util;


public class CheckUtil {
	private CheckUtil() {
	}

	public static <T> T checkNotNull(T o, String objectName) {
		if (o == null) {
			throw new NullPointerException(objectName + " is null");
		}
		return o;
	}

	public static <T> T checkArgumentNotNull(T o, String objectName) {
		if (o == null) {
			throw new IllegalArgumentException(objectName + " is null");
		}
		return o;
	}
	
	public static <T> T checkStateNotNull(T o, String objectName) {
		if (o == null) {
			throw new IllegalStateException(objectName + " is null");
		}
		return o;
	}
	
	public static void checkArgument(boolean assertion, String message) {
		if (!assertion) {
			throw new IllegalArgumentException(message);
		}
	}
	
	public static void checkState(boolean assertion, String message) {
		if (!assertion) {
			throw new IllegalStateException(message);
		}
	}
}
