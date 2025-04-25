package com.sinergise.common.gis.ogc.wfs.request;

public interface CommonParams {
	public static interface XLinkSingle {

		public static final String PARAM_TRAVERSE_DEPTH = "TRAVERSEXLINKDEPTH";
		public static final String PARAM_TRAVERSE_EXPIRY = "TRAVERSEXLINKEXPIRY";
	}
	public static interface XLinkList extends XLinkSingle {

		public static final String PARAM_TRAVERSE_DEPTH_LIST = "PROPTRAVXLINKDEPTH";
		public static final String PARAM_TRAVERSE_EXPIRY_LIST = "PROPTRAVXLINKEXPIRY";
	}
}
