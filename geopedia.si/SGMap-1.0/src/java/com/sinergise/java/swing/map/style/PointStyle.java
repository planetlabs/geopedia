/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Graphics2D;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public interface PointStyle {
    void draw(String text, double wX, double wY, DisplayCoordinateAdapter dca, Graphics2D g);
}
