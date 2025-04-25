/*
 *
 */
package com.sinergise.gwt.gis.map.ui.overlays;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.OverlayProvider;


public abstract class DefaultOverlaysProvider implements OverlayProvider {

    @Override
	public OverlayComponent<?> addLayer(CRS mapCRS, OverlayComponent<?> previous, Layer layer) {
        if (isCompatible(previous)) {
            return addLayerToOverlay(mapCRS, previous, layer);
        }
        OverlayComponent<?> prv=addLayerToOverlay(mapCRS, null, layer);
        if (prv==null) {
        	return previous;
        }
        return prv;
    }
    
    protected abstract boolean isCompatible(OverlayComponent<?> previous);
    
    protected abstract OverlayComponent<?> addLayerToOverlay(CRS mapCRS, OverlayComponent<?> previous, Layer layer);
}
