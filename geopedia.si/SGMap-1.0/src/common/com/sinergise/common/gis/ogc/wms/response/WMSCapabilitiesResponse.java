package com.sinergise.common.gis.ogc.wms.response;

import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.ogc.wms.AbstractWmsLayerSpec;
import com.sinergise.common.util.web.MimeType;

public class WMSCapabilitiesResponse implements WMSResponse {
	/**
     * This object's mime
     */
	public static final MimeType MIME_OBJECT_CAPABILITIES_RESPONSE = MimeType.getObjectMime(FeatureInfoCollection.class);

	private AbstractWmsLayerSpec[] layers;
	
	@Override
	public MimeType getMimeType() {
		return MIME_OBJECT_CAPABILITIES_RESPONSE;
	}
	
	public AbstractWmsLayerSpec[] getLayers() {
		return layers;
	}
	
	public void setLayers(AbstractWmsLayerSpec[] layers) {
		this.layers = layers;
	}
}
