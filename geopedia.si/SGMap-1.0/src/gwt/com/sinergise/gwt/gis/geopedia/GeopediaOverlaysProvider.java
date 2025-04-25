/*
 *
 */
package com.sinergise.gwt.gis.geopedia;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.geopedia.GeopediaLayer;
import com.sinergise.common.gis.geopedia.GeopediaLayersSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.OverlaysFactory;
import com.sinergise.gwt.gis.map.ui.overlays.DefaultOverlaysProvider;
import com.sinergise.gwt.gis.map.ui.overlays.TiledOverlay;


public class GeopediaOverlaysProvider extends DefaultOverlaysProvider {
    public static final GeopediaOverlaysProvider INSTANCE=new GeopediaOverlaysProvider();
    private static boolean initialized = false;
    public static final void initialize() {
    	if (initialized) {
    		return;
    	}
    	initialized = true;
    	OverlaysFactory.INSTANCE.registerProvider(GeopediaLayersSource.INSTANCE.getTypeIdentifier(), GeopediaOverlaysProvider.INSTANCE);
    }
    
    @Override
	protected boolean isCompatible(OverlayComponent<?> previous) {
    	return (previous instanceof TiledOverlay);
    }
    @Override
	protected OverlayComponent<?> addLayerToOverlay(CRS mapCRS, OverlayComponent<?> previous, Layer layer) {
        if (layer instanceof GeopediaLayer) {
            GeopediaLayer lyr=(GeopediaLayer)layer;
            if (lyr.isStatic()) {
                return new TiledOverlay(mapCRS, new GeopediaStaticRenderer(lyr));
            } else if (previous instanceof TiledOverlay) {
            	TiledOverlay tOvr=(TiledOverlay)previous;
                if (!(tOvr.provider instanceof GeopediaLayersRenderer)) {
                    previous=new TiledOverlay(mapCRS,new GeopediaLayersRenderer());
                }
                GeopediaLayersRenderer rnd=(GeopediaLayersRenderer)tOvr.provider;
                rnd.addLayer(lyr);
            }
        }
        return previous;
    }

    public boolean isRenderable(Layer layer) {
        return layer instanceof GeopediaLayer;
    }

}
