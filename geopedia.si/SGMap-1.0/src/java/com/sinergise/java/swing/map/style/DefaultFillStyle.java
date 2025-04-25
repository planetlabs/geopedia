/*
 *
 */
package com.sinergise.java.swing.map.style;

import static com.sinergise.java.swing.map.style.FillType.SOLID;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public class DefaultFillStyle implements VectorStyle {
    public Color color;

    public int thickness = 0;

    public FillType type = SOLID;

    transient private Paint calculatedPaint = null;

    public DefaultFillStyle() {
    }
    public DefaultFillStyle(FillType type) {
        if (type==FillType.HOLLOW) {
            this.type=type;
            this.color=Color.WHITE;
            this.thickness=1;
            updatePaint();
        } else {
            throw new IllegalArgumentException("Only "+FillType.HOLLOW+" can be used with one parameter constructor!");
        }
    }
    public DefaultFillStyle(FillType type, Color c, int size) {
        this.type=type;
        this.color=c;
        this.thickness=size;
        updatePaint();
    }
    
    public void draw(Shape shp, DisplayCoordinateAdapter dca, Graphics2D g) {
        if (calculatedPaint==null) updatePaint();
        if (calculatedPaint==null) return; //HOLLOW
        Paint oldP=g.getPaint();
        g.setPaint(calculatedPaint);
        g.fill(shp);
        g.setPaint(oldP);
    }

    private void updatePaint() {
        calculatedPaint = FillType.createPaint(type, color, thickness);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color front) {
        this.color = front;
        updatePaint();
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int size) {
        this.thickness = size;
        updatePaint();
    }

    public FillType getType() {
        return type;
    }

    public void setType(FillType type) {
        this.type = type;
        updatePaint();
    }
}
