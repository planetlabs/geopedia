/*
 *
 */
package com.sinergise.common.gis.geopedia;

import com.sinergise.common.gis.geopedia.GeopediaLayersSource.GeopediaAbstractSpec;
import com.sinergise.common.gis.map.model.layer.Layer;


public class GeopediaLayer extends Layer {
    boolean isStatic=false;
    public GeopediaLayer(GeopediaAbstractSpec src) {
        super(src);
        setTitle(src.getTitle());
        isStatic=src.isStatic();
    }
    public boolean isStatic() {
        return isStatic;
    }
    @Override
	public boolean isOpaque() {
        return ((GeopediaAbstractSpec)getSpec()).isOpaque();
    }
    
    @Override
    public void setDirty() {
    	// TODO Handle changes in GP layer's data and meta timestamps
    }
    
    @Override
    public GeopediaLayer setOn(boolean on) {
    	super.setOn(on);
    	return this;
    }
}
