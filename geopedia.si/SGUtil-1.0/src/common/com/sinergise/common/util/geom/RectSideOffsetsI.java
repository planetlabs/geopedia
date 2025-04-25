package com.sinergise.common.util.geom;

import com.sinergise.common.util.settings.Settings;

public class RectSideOffsetsI implements Settings {
	public static RectSideOffsetsI EMPTY = new RectSideOffsetsI();
	public static RectSideOffsetsI create(int t, int l, int b, int r) {
		if (t == 0 && l ==0 && b == 0 && r == 0) {
			return EMPTY;
		}
		return new RectSideOffsetsI(t, l, b, r);
	}
	
	private int t = 0;
	private int l = 0;
	private int b = 0;
	private int r = 0;
	
	protected RectSideOffsetsI() {
	}
	public RectSideOffsetsI(int t, int l, int b, int r) {
		this.t = t;
		this.l = l;
		this.b = b;
		this.r = r;
	}
	public int sumW() {
		return l + r;
	}
	public int sumH() {
		return t + b;
	}
	public int b() {
		return b;
	}
	public int t() {
		return t;
	}
	public int r() {
		return r;
	}
	public int l() {
		return l;
	}
	
	@Override
	public String toString() {
		return t+" "+l+" "+b+" "+r;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + b;
		result = prime * result + l;
		result = prime * result + r;
		result = prime * result + t;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RectSideOffsetsI)) {
			return false;
		}
		RectSideOffsetsI other = (RectSideOffsetsI)obj;
		if (b != other.b) {
			return false;
		}
		if (l != other.l) {
			return false;
		}
		if (r != other.r) {
			return false;
		}
		if (t != other.t) {
			return false;
		}
		return true;
	}
}
