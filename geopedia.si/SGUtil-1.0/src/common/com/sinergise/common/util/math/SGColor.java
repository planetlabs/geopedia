package com.sinergise.common.util.math;

import java.io.Serializable;

import com.sinergise.common.util.string.StringUtil;

public class SGColor implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final SGColor BLACK = new SGColor(0xFF000000);
	public static final SGColor TRANSPARENT = new SGColor(0x00000000);
	
	private int value;
	
	private static int validateComponent(int a) {
		if (a < 0 || a > 255) throw new IllegalArgumentException("Value out of range [0-255], was "+a);
		return a;
	}

	//GWT Serialization
	@SuppressWarnings("unused")
	private SGColor() {		
	}
	
	public SGColor (int argb) {
		this.value=argb;
	}
		
	public SGColor(int r, int g, int b, int a) {
		this((validateComponent(a) << 24) | (validateComponent(r) << 16) | (validateComponent(g) << 8) | validateComponent(b));
	}
	
	public int getARGB() {
		return value;
	}
	
	public int getAlpha() {
		return (value >>> 24);
	}

	public int getRed() {
		return (value >>> 16) & 0xFF;
	}

	public int getGreen() {
		return (value >>> 8) & 0xFF;
	}

	public int getBlue() {
		return value & 0xFF;
	}
	
	public String getHTMLColor() {
		return "#" + StringUtil.padWith(Integer.toHexString(value & 0x00FFFFFF).toUpperCase(), '0', 6, true);
	}
}
