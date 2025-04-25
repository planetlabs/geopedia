package com.sinergise.java.util.string;

import java.awt.Color;

import com.sinergise.common.util.string.StringUtil;

public class StringUtilJava extends StringUtil {
	
	public static Class<?> classFromString(final String strVal) {
		return StringSerializer.valueOf(strVal, Class.class);
	}
	
	public static Color parseColor(final String colorString) throws NumberFormatException {
		if (colorString.startsWith("0x") || colorString.startsWith("#")) {
			final long clr = Long.decode(colorString).longValue();
			return new Color((int)(clr & 0xFFFFFFFF), colorString.length() > 8);
		}
		if (colorString.matches("[0123456789abcdefABCDEF]{6,8}")) {
			final long clr = Long.parseLong(colorString, 16);
			return new Color((int)(clr & 0xFFFFFFFF), colorString.length() > 6);
		}
		throw new NumberFormatException("Invalid color string: " + colorString);
	}
}
