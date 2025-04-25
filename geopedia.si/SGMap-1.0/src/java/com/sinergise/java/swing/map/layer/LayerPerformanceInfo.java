/*
 *
 */
package com.sinergise.java.swing.map.layer;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;

public interface LayerPerformanceInfo {
    long timeToRender(DisplayCoordinateAdapter dca);
    long maxTimeToRender();
    double updateFreq();
}
