/*
 *
 */
package com.sinergise.common.geometry.display;

import com.sinergise.common.geometry.util.CoordUtil;


public class ScaleSpec {
    public static final ScaleSpec ZERO=new ScaleSpec(0,false);
    public static final ScaleSpec INFINITY=new ScaleSpec(Double.POSITIVE_INFINITY,false);
    double value;
    boolean isPix;
    
    public ScaleSpec(double value, boolean isInWorldPerPix) {
        this.value=value;
        this.isPix=isInWorldPerPix;
    }
    
    public double scale(double pixSizeInMicrons) {
        if (isPix) return CoordUtil.worldPerDisp(value, pixSizeInMicrons);
        return value;
    }
    
    public double worldPerPix(double pixSizeInMicrons) {
        if (isPix) return value;
        return CoordUtil.worldPerDisp(value, pixSizeInMicrons);
    }
}
