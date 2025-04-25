package com.sinergise.common.gis.ogc.wfs.request;

import com.sinergise.common.gis.ogc.wms.WMSUtil;

public class WFSDescribeFeatureTypeRequest extends WFSRequest {
	
	private static final long serialVersionUID = 1L;
	
	public static final String REQ_DESCRIBE_FEATURE_TYPE="DescribeFeatureType";
	
	/**
	 * The requested types (comma-separated Strings).
	 */
	public static final String PARAM_TYPE_NAME = "TYPENAME";

	public WFSDescribeFeatureTypeRequest() {
		super(REQ_DESCRIBE_FEATURE_TYPE);
	}
	
	public String[] getTypeNames() {
	    return WMSUtil.decodeArray(get(PARAM_TYPE_NAME,""));
	}

	public void setTypeNames(String[] typeNames) {
	    set(PARAM_TYPE_NAME, WMSUtil.encodeArray(typeNames));
	}
}
