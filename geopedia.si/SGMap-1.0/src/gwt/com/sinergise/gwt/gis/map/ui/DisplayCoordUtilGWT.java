/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import com.sinergise.common.geometry.display.DisplayCoordUtil;
import com.sinergise.common.gis.map.ui.IMap;


public class DisplayCoordUtilGWT extends DisplayCoordUtil {
    public static final boolean zoomWithInteger(IMap map, int howMuch, double factorIfContinuous) {
    	boolean didAny = zoomWithInteger(map.getCoordinateAdapter(), map.getUserZooms(), howMuch, factorIfContinuous);
        map.refresh();
        map.repaint(500);
        return didAny;
    }
}
