/*
 *
 */
package com.sinergise.common.util.geom;

import java.io.Serializable;

import com.sinergise.common.util.string.HasCanonicalStringRepresentation;

public class DimI implements Serializable, HasCanonicalStringRepresentation {
	/**
     * 
     */
    private static final long serialVersionUID = 5878902797755103939L;
    
    public static final DimI EMPTY = new DimI();
    
	public static DimI valueOf(String str) {
		String[] parts = str.split("\\s+");
		return new DimI(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
	}
	
	public static DimI create(int w, int h) {
		if (w <= 0 || h <= 0) {
			return EMPTY;
		}
		return new DimI(w, h);
	}
	
	public static DimI round(double w, double h) {
		return create((int)Math.round(w), (int)Math.round(h));
	}
	
	private int h = 0;
	private int w = 0;
	
	private DimI() {
	// serialization and EMPTY
	}
	
	public DimI(final int w, final int h) {
		assert w > 0 && h > 0 : "Can't create DimI with non-positive width or height";
		this.w = w;
		this.h = h;
	}
	
	public int w() {
		return w;
	}
	
	public long area() {
		return (long)w * h;
	}

	public int h() {
		return h;
	}

	public DimI createForSize(final int newW, final int newH) {
		if (newW == w && newH == h) {
			return create(newW, newH);
		}
		return this;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + h;
		result = PRIME * result + w;
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof DimI)) {
			return false;
		}
		final DimI other = (DimI)obj;
		return equals(other.w, other.h);
	}
	
	public boolean equals(final int w2, final int h2) {
		return w == w2 && h == h2;
	}
	
	public boolean isEmpty() {
		return w <= 0 || h <= 0;
	}
	
	@Override
	public String toCanonicalString() {
		return w + " " + h;
	}

	public DimI createForWidth(int newW) {
		if (newW == w) {
			return this;
		}
		return create(newW, h);
	}

	public DimI createForHeight(int newH) {
		if (newH == h) {
			return this;
		}
		return create(w, newH);
	}
}
