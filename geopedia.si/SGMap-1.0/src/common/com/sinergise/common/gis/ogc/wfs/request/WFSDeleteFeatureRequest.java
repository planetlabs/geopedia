package com.sinergise.common.gis.ogc.wfs.request;

import com.sinergise.common.gis.feature.CFeatureIdentifier;

/**
 * TODO: temporary transaction request, should be refactored to use standard WFS request
 *  
 * @author tcerovski
 */

public class WFSDeleteFeatureRequest extends WFSTransactionRequest {

private static final long serialVersionUID = 1L;
	
	private CFeatureIdentifier[] featureIds;
	
	@Deprecated /** Serialization only */
	protected WFSDeleteFeatureRequest() {}
	
	public WFSDeleteFeatureRequest(CFeatureIdentifier ...featureIds) {
		this.featureIds = featureIds;
	}
	
	public CFeatureIdentifier[] getFeatureIds() {
		return featureIds;
	}
	
}
