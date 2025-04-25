package com.sinergise.common.gis.ogc.wfs.response;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.web.MimeType;


public interface WFSDescribeFeatureTypeResponse extends WFSResponse {
	public static final MimeType FEATURE_DESCRIPTOR_ARR_MIME=MimeType.getObjectMime(CFeatureDescriptor[].class);
	/**
	 * @return CFeatureDescriptor[] for the requested layers/featureTypes
	 */
	public CFeatureDescriptor[] getFeatureDescriptors();
}
