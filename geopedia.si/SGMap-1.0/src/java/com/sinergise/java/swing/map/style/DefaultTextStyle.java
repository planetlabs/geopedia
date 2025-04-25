/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public class DefaultTextStyle implements PointStyle {
    public Font font;
    public Color c;
    private transient Font backFont;
    private transient float backOffX;
    private transient float backOffY;
    private transient Color backCol;
    
    public DefaultTextStyle() {
        font = new Font("Tahoma",Font.BOLD, 12);
        c = Color.WHITE;
        updateTransient();
    }
    
    private void updateTransient() {
        backFont=font.deriveFont(Font.BOLD,font.getSize2D()+0.1f);
        backOffX=1f;
        backOffY=1f;
        double gray=0.3*c.getRed()+0.59*c.getGreen()+0.11*c.getBlue();
        if (gray>0.5*255) {
            backCol = new Color(0x70000000,true);
        } else {
            backCol = new Color(0x70FFFFFF,true);
        }
    }
    
    public void draw(String text, double wX, double wY, DisplayCoordinateAdapter dca, Graphics2D g) {
        //TODO: remove this
        //c=new Color((int)(Math.random()*Integer.MAX_VALUE));
        font=new Font("Tahoma", Font.BOLD, 11);
        updateTransient();
        // to here
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(backCol);
        g.setFont(backFont);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i==0 && j==0) continue;
                g.drawString(text, (float)dca.pixFromWorld.x(wX)+i*backOffX, (float)dca.pixFromWorld.y(wY)+j*backOffY);
            }
        }
        g.setColor(c);
        g.setFont(font);
        g.drawString(text, (float)dca.pixFromWorld.x(wX), (float)dca.pixFromWorld.y(wY));
    }
}