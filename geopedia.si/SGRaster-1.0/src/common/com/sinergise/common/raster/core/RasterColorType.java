/*
 *
 */
package com.sinergise.common.raster.core;

import static com.sinergise.common.raster.core.RasterColorType.ColorRep.BW;
import static com.sinergise.common.raster.core.RasterColorType.ColorRep.GRAY;
import static com.sinergise.common.raster.core.RasterColorType.ColorRep.RGB;
import static com.sinergise.common.raster.core.RasterColorType.Transparency.ALPHA;
import static com.sinergise.common.raster.core.RasterColorType.Transparency.BITMASK;
import static com.sinergise.common.raster.core.RasterColorType.Transparency.OPAQUE;

public final class RasterColorType {
	public static enum ColorRep { BW, GRAY, RGB }
	public static enum Transparency { OPAQUE, BITMASK, ALPHA }
	
    public static final RasterColorType TYPE_BINARY = new RasterColorType("BINARY", BW, OPAQUE);
    public static final RasterColorType TYPE_BINARY_BITMASK = new RasterColorType("BINARY_M", BW, BITMASK);
    public static final RasterColorType TYPE_BINARY_ALPHA = new RasterColorType("BINARY_A", BW, ALPHA);
    public static final RasterColorType TYPE_GRAYSCALE = new RasterColorType("GRAYSCALE", GRAY, OPAQUE);
    public static final RasterColorType TYPE_GRAYSCALE_BITMASK = new RasterColorType("GRAYSCALE_M", GRAY, BITMASK);
    public static final RasterColorType TYPE_GRAYSCALE_ALPHA = new RasterColorType("GRAYSCALE_A", GRAY, ALPHA);
    public static final RasterColorType TYPE_RGB = new RasterColorType("RGB", RGB, OPAQUE);
    public static final RasterColorType TYPE_RGB_BITMASK = new RasterColorType("RGB_B", RGB, BITMASK);
    public static final RasterColorType TYPE_RGB_ALPHA = new RasterColorType("RGBA", RGB, ALPHA);
    
    private final ColorRep col;
    private final Transparency trans;
    private final String name;
    private RasterColorType(String name, ColorRep colors, Transparency alphaType) {
        this.name=name;
        this.col = colors;
        this.trans = alphaType;
    }
    public boolean hasAlpha() {
        return trans == ALPHA;
    }
    public boolean isOpaque() {
        return trans == OPAQUE;
    }
    
    public RasterColorType getTypeForCombiningWith(RasterColorType other) {
    	return getTypeForCombination(this, other);
    }
    
    public static RasterColorType getTypeForCombination(final RasterColorType t1, final RasterColorType t2) {
        if (t1==t2) return t1;
        if (t1.col.compareTo(t2.col) >= 0) {
        	if (t1.trans.compareTo(t2.trans) >= 0) return t1;
        	return t1.getWithTransparency(t2.trans);
        } else if (t2.trans.compareTo(t1.trans) >= 0) return t2;
        return t2.getWithTransparency(t1.trans);
    }
    
    public RasterColorType getWithTransparency(Transparency newAlpha) {
    	if (newAlpha == trans) return this;
    	return getTypeFor(col, newAlpha);
	}
    
	public static final RasterColorType getTypeFor(ColorRep newCol, Transparency newAlpha) {
    	switch (newCol) {
			case BW: return newAlpha==OPAQUE ? TYPE_BINARY : newAlpha == BITMASK ? TYPE_BINARY_BITMASK : TYPE_BINARY_ALPHA;
			case GRAY: return newAlpha==OPAQUE ? TYPE_GRAYSCALE : newAlpha == BITMASK ? TYPE_BINARY_BITMASK : TYPE_BINARY_ALPHA;
			case RGB: return newAlpha==OPAQUE ? TYPE_BINARY : newAlpha == BITMASK ? TYPE_BINARY_BITMASK : TYPE_BINARY_ALPHA;
		}
    	throw new IllegalArgumentException("Unknown color code: "+newCol);
	}
	public final Transparency getTransparency() {
		return trans;
	}
	
	public final ColorRep getColorBands() {
		return col;
	}
    
    public final RasterColorType getForSubsampling() {
        if (col == BW) return getTypeFor(GRAY, trans==BITMASK ? ALPHA : trans);
        if (trans == BITMASK) return getWithTransparency(ALPHA);
        return this;
    }
    
    /**
     * @return one of {@link #TYPE_BINARY}, {@link #TYPE_GRAYSCALE}, {@link #TYPE_RGB}, {@link #TYPE_RGB_ALPHA}
     */
    public final RasterColorType normalize() {
    	if (trans != OPAQUE) return TYPE_RGB_ALPHA;
    	return this;
    }
    
    /**
     * @param typeName
     * @return Type identified by the typeName or null if the type could not be matched
     */
    public static RasterColorType parseType(String typeName) {
        if (typeName==null || typeName.trim().length()==0) return null;
        
        if (TYPE_BINARY.name.equalsIgnoreCase(typeName)) return TYPE_BINARY;
        if (TYPE_BINARY_BITMASK.name.equalsIgnoreCase(typeName)) return TYPE_BINARY_BITMASK;
        if (TYPE_BINARY_ALPHA.name.equalsIgnoreCase(typeName)) return TYPE_BINARY_ALPHA;
        
        if (TYPE_GRAYSCALE.name.equalsIgnoreCase(typeName)) return TYPE_GRAYSCALE;
        if (TYPE_GRAYSCALE_BITMASK.name.equalsIgnoreCase(typeName)) return TYPE_GRAYSCALE_BITMASK;
        if (TYPE_GRAYSCALE_ALPHA.name.equalsIgnoreCase(typeName)) return TYPE_GRAYSCALE_ALPHA;
        
        if (TYPE_RGB.name.equalsIgnoreCase(typeName)) return TYPE_RGB;
        if (TYPE_RGB_BITMASK.name.equalsIgnoreCase(typeName)) return TYPE_RGB_BITMASK;
        if (TYPE_RGB_ALPHA.name.equalsIgnoreCase(typeName)) return TYPE_RGB_ALPHA;
        
        return null;
    }
}
