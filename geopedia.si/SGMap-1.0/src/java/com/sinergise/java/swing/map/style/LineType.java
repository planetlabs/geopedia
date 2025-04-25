/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.BasicStroke;
import java.awt.Stroke;

public enum LineType {
    SOLID, DASH, DOT, DASH_DOT, DASH_DOT_DOT;

    private static final float W_BREAK = 2;
    private static final float W_DASH = 4;
    private static final float W_DOT = 0.1f;

    /**
     * Method returns Stroke object which is used for drawing particular line
     * style.
     *
     * @param lineStyle type of line style
     * @param styleThickness height of the shape using in line style
     *
     * @return Stroke for drawing line style
     */
    public static Stroke getStrokeInstance(LineType lineStyle, float styleThickness)
    {
        float[] dash;
    
        switch (lineStyle)
        {
            case SOLID:
                return new BasicStroke(styleThickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND);
    
            case DASH:
                dash = new float[]{ W_DASH * styleThickness, W_BREAK * styleThickness };
    
                return new BasicStroke(styleThickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 2.0f, dash, styleThickness);
    
            case DOT:
                dash = new float[]{ W_DOT * styleThickness, W_BREAK * styleThickness };
    
                return new BasicStroke(styleThickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 2.0f, dash, styleThickness);
    
            case DASH_DOT:
                dash = new float[]{ W_DASH * styleThickness, W_BREAK * styleThickness, W_DOT * styleThickness, W_BREAK * styleThickness };
    
                return new BasicStroke(styleThickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 2.0f, dash, 2.0f);
    
            case DASH_DOT_DOT:
                dash = new float[]{ W_DASH * styleThickness, W_BREAK * styleThickness, W_DOT * styleThickness, W_BREAK * styleThickness, W_DOT * styleThickness, W_BREAK * styleThickness };
    
                return new BasicStroke(styleThickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 2.0f, dash, styleThickness);
    
            //break;
            default:
                return new BasicStroke();
        }
    }
}
