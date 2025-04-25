/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public class DefaultLineStyle implements VectorStyle {
    public Color    color=Color.BLACK;
    public float    thickness=1;
    public LineType type=LineType.SOLID;
    transient private Stroke calculatedStroke=null;
    
    public DefaultLineStyle() {
    }
    
    public DefaultLineStyle(LineType type, Color col, float thick) {
        this.type=type;
        this.thickness=thick;
        this.color=col;
        updateStroke();
    }

    
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public float getThickness() {
        return thickness;
    }
    public void setThickness(float thickness) {
        this.thickness = thickness;
        updateStroke();
    }
    public LineType getType() {
        return type;
    }
    public void setType(LineType type) {
        this.type = type;
        updateStroke();
    }
    private void updateStroke() {
        calculatedStroke=LineType.getStrokeInstance(type, thickness);
    }
    
    private void checkStroke() {
        if (calculatedStroke==null) updateStroke();
    }
    
    protected void prepare(Graphics2D g) {
        checkStroke();
        g.setColor(color);
        g.setStroke(calculatedStroke);
    }
    
    public void draw(Shape shp, DisplayCoordinateAdapter dca, Graphics2D g) {
        prepare(g);
        g.draw(shp);
    }
}
