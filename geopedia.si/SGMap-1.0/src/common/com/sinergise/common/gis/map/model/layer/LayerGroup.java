/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.util.geom.Envelope;


public class LayerGroup extends LayerTreeElement {
    private transient DisplayBounds.Disp bounds;
    
    public LayerGroup(String title) {
        this(title, title);
    }
    
    /**
	 * 
	 */
	public LayerGroup(String id, String title) {
		super(id, title);
	}
	
	@Override
	public LayerGroup setOn(boolean on) {
		return (LayerGroup)super.setOn(on);
	}
	
	@Override
	public void setDirty() {
		// Do nothing
	}
	
    public LayerTreeElement addLayer(LayerTreeElement el) {
        super.add(el);
        return el;
    }
    
    @Override
	public boolean isRenderable() {
        return false;
    }
    
    @Override
	public DisplayBounds.Disp getBounds() {
        if (bounds==null) {
            bounds=new DisplayBounds.Disp();
        }
        if (children==null) return bounds;
        for (LayerTreeElement ch : children) {
           	bounds.expandToInclude(ch.getBounds());
        }
        return bounds;
    }
    
    @Override
	public boolean hasAnythingToRender() {
        if (!deepOn()) return false;
        if (children==null) return false;
        for (LayerTreeElement child : children) {
            if (child.hasAnythingToRender()) return true;
        }
        return false;
    }
    
    @Override
	public boolean isOpaque() {
    	if (!deepOn()) return false;
    	if (children==null) return false;
    	
    	// At least one child should be opaque and have the same bounds as the group
    	DisplayBounds.Disp myBnds=getBounds();
        for (LayerTreeElement child : children) {
            if (child.isOpaque() && child.getBounds().equals(myBnds)) return true;
        }
    	return false;
    }
    
    @Override
	public boolean isOpaque(double scale, Envelope mbr) {
    	if (!deepOn()) return false;
    	if (children==null) return false;
    	
        for (LayerTreeElement child : children) {
            if (child.isOpaque(scale, mbr)) return true;
        }
    	return false;
    }
    
    @Override
	public boolean hasAnythingToRender(double scale, double pixSizeInMicrons,
            Envelope mbr) {
        if (!deepOn()) return false;
        if (children==null) return false;
        for (LayerTreeElement child : children) {
            if (child.hasAnythingToRender(scale, pixSizeInMicrons, mbr)) return true;
        }
        return false;
    }
}
