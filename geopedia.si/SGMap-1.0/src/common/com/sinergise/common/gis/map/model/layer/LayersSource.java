/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.naming.Identifiable;



public interface LayersSource extends Identifiable {
    /**
     *  
     */
    public static final String CAPABILITY_HIGHLIGHT = "featureHighlight";
    public static final String CAPABILITY_QUERY_FILTER = "queryFilter";
    public static final String CAPABILITY_FEATURE_INFO = "featureInfo";
    public static final String CAPABILITY_LEGEND_IMAGE = "legendImage";
    public static final String CAPABILITY_LEGEND_IMAGE_SIZE = "legendImage.size";

    boolean isInitialized();

    LayerSpec findLayerSpec(final String layerID, final boolean ignoreCase);

    Layer createLayer(LayerSpec spec);

    String getTypeIdentifier();
    
    /**
     * @param capability one of CAPABILITY_* constants
     * @return
     */
    boolean supports(String capability);

	public abstract <T extends LayersSource> void asyncInitialize(AsyncCallback<? super T> cb);
}
