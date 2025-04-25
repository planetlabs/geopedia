package com.sinergise.common.gis.ogc.wfs.request;

import com.sinergise.common.gis.feature.CFeature;

/**
 * TODO: temporary transaction request, should be refactored to use standard WFS request
 *  
 * @author tcerovski
 */

public class WFSInsertOrUpdateRequest extends WFSTransactionRequest {
	
	private static final long serialVersionUID = 1L;
	
	private CFeature[] features;
	
	@Deprecated /** Serialization only */
	protected WFSInsertOrUpdateRequest() {}
	
	public WFSInsertOrUpdateRequest(CFeature... features) {
		this.features = features;
	}
	
	public CFeature[] getFeatures() {
		return features;
	}
	
}
