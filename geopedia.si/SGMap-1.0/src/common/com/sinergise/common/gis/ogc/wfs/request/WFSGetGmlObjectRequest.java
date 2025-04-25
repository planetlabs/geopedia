package com.sinergise.common.gis.ogc.wfs.request;


public class WFSGetGmlObjectRequest extends WFSRequest implements CommonParams.XLinkSingle {

	private static final long serialVersionUID = 1L;
	
	public static final String REQ_GET_GML_OBJECT="GetGmlObject";
	public static final String PARAM_GML_OBJECT_ID="GMLOBJECTID";
	
	public WFSGetGmlObjectRequest() {
		super(REQ_GET_GML_OBJECT);
	}
	
	public String getGmlObjectId() {
		return get(PARAM_GML_OBJECT_ID);
	}
	
	public void setGmlObjectId(String gmlObjectId) {
		set(PARAM_GML_OBJECT_ID, gmlObjectId);
	}
}
