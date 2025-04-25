package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.user.client.Element;
import com.sinergise.gwt.util.html.CSS;


public class EffectElement {
    Element el;
    private int t;
    private int l;
    private int w;
    private int h;
    
    public EffectElement(Element el) {
        this.el=el;
        CSS.position(el, CSS.POS_ABSOLUTE);
    }
    public void setSizeInPix(int w, int h) {
    	if (w>=0) {
    		l=l+this.h/2-h/2;
    		this.w=w;
    		CSS.left(el, l);
        	CSS.width(el, w);
    	}
    	if (h>=0) {
    		t=t+this.w/2-w/2;
    		this.h=h;
    		CSS.top(el, t);
    		CSS.height(el, h);
    	}
    }
    public void setCenterInPix(int x, int y) {
        CSS.left(el, l=x-w/2);
        CSS.top(el, t=y-h/2);
    }
    public void setBoundsInPix(int x1, int y1, int x2, int y2) {
        setTopLeftInPix(Math.min(x1,x2), Math.min(y1,y2));
        setBotRightInPix(Math.max(x1,x2), Math.max(y1,y2));
    }
    public void setTopLeftInPix(int minX, int minY) {
        if (l!=minX) {
            CSS.left(el, l=minX);
        }
        if (t!=minY) {
            CSS.top(el, t=minY);
        }
    }
    public void setBotRightInPix(int maxX, int maxY) {
        CSS.size(el, w = maxX-l, h = maxY-t);
    }
}
