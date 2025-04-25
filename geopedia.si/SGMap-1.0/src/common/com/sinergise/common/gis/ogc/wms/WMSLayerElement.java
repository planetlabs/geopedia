/*
 *
 */
package com.sinergise.common.gis.ogc.wms;

import com.sinergise.common.gis.map.model.layer.info.FeatureInfoLayer;
import com.sinergise.common.gis.ogc.base.OGCLayersSource;

public interface WMSLayerElement extends FeatureInfoLayer {
	/**
	 * Used in non-wms layers whose data is usually accessed directly from the client browser, 
	 * to associate them with a layer defined in the local WMS, when server needs to render the 
	 * layer, for example when printing a PDF of the map or when exporting the map to an image.  
	 */
	public static final String PROP_WMS_EQUIV = "wmsEquivalent";
	public static final String PROP_RENDERING_GROUP="renderingGroup";
	public static final String PROP_SUPPORTED_RENDER_MODES="supportedRenderModes";
	public static final int RENDER_MODE_TILES = 1;
	public static final int RENDER_MODE_UNTILED = 2;
	
    public String getWMSName();
    public String getWMSStyleName();
    public OGCLayersSource getSource();
}
