/*
 *
 */
package com.sinergise.gwt.gis.ogc.ui;

import static com.sinergise.gwt.gis.ogc.wms.WMSLayersSource.SOURCE_TYPE_WMS;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.gis.ogc.wms.WMSLayerElement;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.OverlaysFactory;
import com.sinergise.gwt.gis.map.ui.overlays.DefaultOverlaysProvider;
import com.sinergise.gwt.gis.map.ui.overlays.TiledOverlay;
import com.sinergise.gwt.gis.map.ui.overlays.highlight.HighlightOverlayProvider;
import com.sinergise.gwt.gis.ogc.wfs.WFSFeatureSource;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;


public class WMSOverlaysProvider extends DefaultOverlaysProvider implements HighlightOverlayProvider {
    private TiledCRS defaultCRS;
    
    public WMSOverlaysProvider(TiledCRS defaultCRS) {
        this.defaultCRS=defaultCRS;
    }
    
    @Override
    protected boolean isCompatible(OverlayComponent<?> previous) {
    	if (previous instanceof UntiledWMSOverlay) return true;
    	if (previous instanceof TiledOverlay) {
    		TiledOverlay to=(TiledOverlay)previous;
    		return to.provider instanceof TiledWMSRenderer;
    	}
    	return false;
    }
    
    protected int determineRenderingMode(OverlayComponent<?> previous, WMSLayer layer) {
    	if (!layer.getSource().supports(WMSLayersSource.CAPABILITY_TILED_RENDERING)) return WMSLayerElement.RENDER_MODE_UNTILED;
        if (!layer.isTilesEnabled()) return WMSLayerElement.RENDER_MODE_UNTILED;
        if (!layer.isUntiledEnabled()) return WMSLayerElement.RENDER_MODE_TILES;

        if (previous instanceof UntiledWMSOverlay) {
    		UntiledWMSOverlay rnd=(UntiledWMSOverlay)previous;
    		if (rnd.getService()==layer.getSource()) return WMSLayerElement.RENDER_MODE_UNTILED;
    	}
    	return WMSLayerElement.RENDER_MODE_TILES;
    }
    
    @Override
	protected OverlayComponent<?> addLayerToOverlay(CRS mapCRS, OverlayComponent<?> previous, Layer layer) {
    	if (!(layer instanceof WMSLayer)) return previous;
        WMSLayer lyr=(WMSLayer)layer;
        
        int renderMode=determineRenderingMode(previous, lyr);
        
        if (renderMode==WMSLayerElement.RENDER_MODE_TILES) {
        	if ((previous instanceof TiledOverlay) && ((TiledOverlay)previous).provider instanceof TiledWMSRenderer) {
        		TiledWMSRenderer rnd=(TiledWMSRenderer)((TiledOverlay)previous).provider;
        		if (rnd.canAdd(lyr)) {
        			rnd.addLayer(lyr);
                    return previous;       			
        		}
        	}
            TiledWMSRenderer newRend=new TiledWMSRenderer(defaultCRS,lyr.getSource());
            newRend.addLayer(lyr);
            return new TiledOverlay(mapCRS, newRend);
        }
		// go untiled
		if (previous instanceof UntiledWMSOverlay) {
			UntiledWMSOverlay rnd=(UntiledWMSOverlay)previous;
			if (rnd.canAdd(lyr)) {
				rnd.addLayer(lyr);
				return rnd;
			}
		}
		UntiledWMSOverlay ret=new UntiledWMSOverlay(lyr.getSource(), mapCRS);
		ret.addLayer(lyr);
		return ret;
    }
    @Override
	public boolean canHandle(FeaturesSource dataSource) {
    	WMSLayersSource lSrc=null;
    	if (dataSource instanceof WFSFeatureSource) {
    		lSrc=((WFSFeatureSource)dataSource).getLayersSource();
    	} else if (dataSource instanceof WMSLayersSource) {
    		lSrc=(WMSLayersSource)dataSource;
    	}
    	return lSrc != null;
    }
    
    @Override
	public UntiledWMSOverlay createHighlightOverlay(CRS mapCRS, FeaturesSource dataSource, SelectionSetLayer highlightLayer) {
    	WMSLayersSource lSrc=null;
    	if (dataSource instanceof WFSFeatureSource) {
    		lSrc=((WFSFeatureSource)dataSource).getLayersSource();
    	} else if (dataSource instanceof WMSLayersSource) {
    		lSrc=(WMSLayersSource)dataSource;
    	}
    	return new UntiledWMSOverlay(lSrc, mapCRS, highlightLayer);
    }

	public static void initialize(TiledCRS defaultTiles) {
		OverlaysFactory.INSTANCE.registerProvider(SOURCE_TYPE_WMS, new WMSOverlaysProvider(defaultTiles));
	}
}
