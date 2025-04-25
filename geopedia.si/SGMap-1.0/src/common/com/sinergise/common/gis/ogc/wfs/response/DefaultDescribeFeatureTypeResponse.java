package com.sinergise.common.gis.ogc.wfs.response;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.web.MimeType;


public class DefaultDescribeFeatureTypeResponse implements WFSDescribeFeatureTypeResponse {
	CFeatureDescriptor[] arr;
	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public DefaultDescribeFeatureTypeResponse() {
		// TODO Auto-generated constructor stub
	}
	public DefaultDescribeFeatureTypeResponse(CFeatureDescriptor[] data) {
		this.arr=data;
	}
	@Override
	public CFeatureDescriptor[] getFeatureDescriptors() {
		return arr;
	}
	@Override
	public MimeType getMimeType() {
		return FEATURE_DESCRIPTOR_ARR_MIME;
	}
	public static WFSDescribeFeatureTypeResponse createFrom(WFSDescribeFeatureTypeResponse describeFeatureType) {
		return new DefaultDescribeFeatureTypeResponse(describeFeatureType.getFeatureDescriptors());
	}
}
