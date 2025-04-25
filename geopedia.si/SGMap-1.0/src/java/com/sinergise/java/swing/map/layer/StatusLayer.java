/*
 *
 */
package com.sinergise.java.swing.map.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.swing.map.OrDisplay;
import com.sinergise.java.swing.map.PaintOperation;


public class StatusLayer extends OrLayerImpl {
    OrDisplay disp;
    LayerPerformanceInfo perfInfo=new LayerPerformanceInfo() {
        public long timeToRender(DisplayCoordinateAdapter dca) {
            return t;
        }
    
        public long maxTimeToRender() {
            return tMax<=0?t:tMax;
        }
        public double updateFreq() {
            return 50;
        }
    };
    private long t=2;
    private long tMax=1;
    
    public StatusLayer() {
        super("Scale");
    }
    
    public LayerPerformanceInfo getPerformanceInfo() {
        return perfInfo;
    }
    
    @Override
	public void addedToDisplay(OrDisplay display) {
        super.addedToDisplay(display);
        this.disp=display;
    }
    
    public Envelope getBounds() {
        return null;
    }

    public void paintLayer(Graphics2D g2d, DisplayCoordinateAdapter dca, PaintOperation mgr) {
        long ts=System.nanoTime();
        String scaleStr;
        double scale=disp.getDisplayScale();
        if (scale<1) {
            scaleStr="1:"+Math.round(1.0/scale);
        } else {
            scaleStr=Math.round(scale)+":1";
        }

        DimI size=dca.getDisplaySize();
        int w=size.w();
        int h=size.h();
        int margin=Math.max(4, Math.min(20, Math.min(w,h)/100));
        
        Rectangle2D bnds=g2d.getFontMetrics().getStringBounds(scaleStr, g2d);
        int myH=(int)bnds.getHeight();
        int myW=(int)bnds.getWidth();
        
        int x=w-margin-myW;
        int y=h-margin-myH;
        
        Composite oldC=g2d.getComposite();
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x-2, y-2, myW+4, myH+4);
        g2d.setComposite(oldC);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(x-2, y-2, myW+4, myH+4);
        
        g2d.setColor(Color.BLACK);
        
        g2d.drawString(scaleStr,(int)(x-bnds.getMinX()),(int)(y-bnds.getMinY()));
        t = ((System.nanoTime()-ts)/1000000L)+1L;
        if (t>tMax) tMax=t;
    }
    
    public boolean isBackground() {
        return false;
    }
}
