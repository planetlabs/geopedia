/*
 *
 */
package com.sinergise.gwt.ui;


import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sinergise.common.util.geom.DimI;

/**
 * @deprecated Use LayoutComposite 
 */
@Deprecated
public abstract class PixSizedComposite extends CompositeExt implements RequiresResize {
    protected DimI lastSetDim = DimI.EMPTY;
    protected boolean wSetInPix=false;
    protected boolean hSetInPix=false;
    private Timer tmr=new Timer() {
        @Override
		public void run() {
            if (wSetInPix && hSetInPix) {
                cancel();
                return;
            }
            updateSize();
        }
    };
    
    @Override
	public void setHeight(String height) {
    	super.setHeight(height);
    	if (height != null && height.endsWith("px")) {
    		lastSetDim = lastSetDim.createForHeight(Integer.parseInt(height.substring(0, height.length() - 2)));
    		hSetInPix=true;
    		if (wSetInPix) tmr.cancel();
    	} else {
    	    hSetInPix=false;
    	    tmr.scheduleRepeating(100);
    	}
        componentResized(lastSetDim);
    }

    @Override
	public void setWidth(String width) {
    	super.setWidth(width);
    	if (width != null && width.endsWith("px")) {
    		lastSetDim = lastSetDim.createForWidth(Integer.parseInt(width.substring(0, width.length() - 2)));
    		wSetInPix=true;
    		if (hSetInPix) tmr.cancel();
    	} else {
    	    wSetInPix=false;
    	    tmr.scheduleRepeating(100);
    	}
    	updateSize();
        componentResized(lastSetDim);
    }
    
    public void updateSize() {
        int w=lastSetDim.w();
        int h=lastSetDim.h();
        if (!wSetInPix) {
            w=getOffsetWidth();                
        }
        if (!hSetInPix) {
            h=getOffsetHeight();
        }
        if (!lastSetDim.equals(w, h)) {
            lastSetDim = DimI.create(w, h);
            componentResized(lastSetDim);
        }
        tmr.cancel();
        tmr.scheduleRepeating(500);
    }
    
    public void onResize() {
    	wSetInPix = true;
    	hSetInPix = true;
    	tmr.cancel();
    	lastSetDim = getOffsetSize();
    	componentResized(lastSetDim);
    }
    
    public DimI getOffsetSize() {
		return DimI.create(getOffsetWidth(), getOffsetHeight());
	}

	protected abstract void componentResized(DimI newSize);
}
