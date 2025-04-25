/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public enum FillType {
    SOLID, HOLLOW, HORIZONTAL_LINES, VERTICAL_LINES, BACKSLASHED, SLASHED, GRID, SLANTED_GRID;
    private static final double GRID_FACTOR=0.8;
    private static final double SQRT_2 = Math.sqrt(2);
    public static final Color TRANSPARENT_COLOR=new Color(0,0,0,0);
    private static final GraphicsConfiguration gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    public static final Paint createPaint(FillType type, Color frontColor, int size) {
        if (type==SOLID) return frontColor;
        if (type==HOLLOW) return null;
//        int dim=getDim(type, size);
        BufferedImage img=createBufferedImage(type, frontColor, size);
        return new TexturePaint(img, new Rectangle(0,0,img.getWidth(),img.getHeight()));
    }
    
    private static final BufferedImage createBufferedImage(FillType type, Color frontColor, int thick)
    {
            if (type==SOLID || type==HOLLOW) return null;
            
            int size = getDim(type, thick);
            int prThick=(int)(thick*SQRT_2)-1;
            int tr=Transparency.TRANSLUCENT;
            if (frontColor.getAlpha()==255 || frontColor.getAlpha()==0) tr=Transparency.BITMASK;
            BufferedImage ret = gc.createCompatibleImage(size, size,tr);
            int rgb=frontColor.getRGB();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    boolean check=false;
                    if (type == BACKSLASHED) check=(size+i-j)%size<prThick;
                    else if (type == SLASHED) check=(i+j)%size<prThick;
                    else if (type == HORIZONTAL_LINES) check=j%size<thick;
                    else if (type == VERTICAL_LINES) check=i%size<thick;
                    else if (type == GRID) check=i%size<thick || j%size<thick;
                    else if (type == SLANTED_GRID) check=(i+j)%size<prThick || (size+i-j)%size<prThick;
                    if (check) ret.setRGB(i, j, rgb);
                }
            }            
            return ret;
        }
    private static final int getDim(FillType type, int thick)
    {
        if ((type == SLASHED) || (type == BACKSLASHED))
        {
            return ((int)(2*thick*SQRT_2)+1);
        } else if (type==GRID){
            return ((int)(thick*GRID_FACTOR*SQRT_2/(SQRT_2-1)));
        } else if (type==SLANTED_GRID) {
            return ((int)(thick*GRID_FACTOR*2/(SQRT_2-1)));
        } else {
            return (2 * thick);
        }
    }


}
