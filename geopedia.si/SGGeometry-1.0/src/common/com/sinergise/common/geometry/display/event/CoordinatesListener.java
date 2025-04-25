/*
 *
 */
package com.sinergise.common.geometry.display.event;

public interface CoordinatesListener {
    void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged);
    void displaySizeChanged(int newWidthPx, int newHeightPx);
}
