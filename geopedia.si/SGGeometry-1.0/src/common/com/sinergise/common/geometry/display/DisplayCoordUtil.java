/*
 *
 */
package com.sinergise.common.geometry.display;

import com.sinergise.common.util.math.MathUtil;


public class DisplayCoordUtil {
    public static final boolean zoomWithInteger(DisplayCoordinateAdapter coords, ScaleLevelsSpec spec, int howMuch, double factorIfContinuous) {
        if (howMuch==0) return false;
        double old=coords.getScale();
        if (spec!=null) {
            int lev=spec.nearestZoomLevel(old, coords.pixSizeInMicrons);
            int newLev=MathUtil.clamp(spec.getMinLevelId(), lev+howMuch, spec.getMaxLevelId());
            coords.setScale(spec.scale(newLev, coords.pixSizeInMicrons));
        } else {
        	coords.setScale(old*Math.pow(factorIfContinuous,-howMuch));
        }
        return old != coords.getScale();
    }
}
