/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Graphics2D;
import java.awt.Shape;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public class CompositeStyle implements VectorStyle, PointStyle {
    public VectorStyle[] styles;
    public PointStyle[] points;

    public CompositeStyle() {
    }
    
    public CompositeStyle(VectorStyle[] styles) {
        this.styles=styles;
    }
    
    public void draw(Shape seq, DisplayCoordinateAdapter dca, Graphics2D g) {
        if (styles==null) return;
        for (int i = 0; i < styles.length; i++) {
            styles[i].draw(seq, dca, g);
        }
    }
    
    public void draw(String text, double wX, double wY, DisplayCoordinateAdapter dca, Graphics2D g) {
        if (points==null) return;
        for (int i = 0; i < points.length; i++) {
            points[i].draw(text, wX, wY, dca, g);
        }
    }
}
