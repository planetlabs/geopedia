package com.sinergise.common.gis.ogc.wms.request;

public interface IWMSFeatureInfoRequest extends IWMSGetRequest {

	/**
	 * The optional FEATURE_COUNT parameter states the maximum number of features per layer for which feature information shall be returned. Its value is a positive integer. The default value is 1 if this parameter is omitted or is other than a positive integer.
	 */
	public static final String PARAM_FEATURE_COUNT = "FEATURE_COUNT";
	/**
	 * The mandatory INFO_FORMAT parameter indicates what format to use when returning the feature information. Supported values for a GetFeatureInfo request on a WMS server are listed as MIME types in one or more <Request><FeatureInfo><Format> elements of its service metadata. The entire MIME type string in <Format> is used as the value of the INFO_FORMAT parameter. In an HTTP environment, the MIME type shall be set on the returned object using the Content-type entity header. If the request specifies a format not supported by the server, the server shall issue a service exception (code = InvalidFormat).
	 */
	public static final String PARAM_INFO_FORMAT = "INFO_FORMAT";
	/**
	 * The mandatory I and J request parameters are integers that indicate a point of interest on the map that was produced by the embedded GetMap request (the "map request part" described in 7.4.3.3). The point (I,J) is a point in the (i,j) space defined by the Map CS (see 6.7.2). Therefore: -⎯ the value of I shall be between 0 and the maximum value of the i axis; - the value of J shall be between 0 and the maximum value of the j axis; - the point I=0, J=0 indicates the pixel at the upper left corner of the map; - I increases to the right and J increases downward. The point (I,J) represents the centre of the indicated pixel. If the value of I or of J is invalid, the server shall issue a service exception (code = InvalidPoint).
	 */
	public static final String PARAM_PIXEL_LEFT = "I";
	public static final String PARAM_PIXEL_LEFT_OLD = "X";
	/**
	 * @see #PARAM_PIXEL_LEFT
	 */
	public static final String PARAM_PIXEL_TOP = "J";
	public static final String PARAM_PIXEL_TOP_OLD = "Y";
	/**
	 * Width in pixels of map picture.
	 */
	public static final String PARAM_RADIUS = "RADIUS";
	public static final String REQ_GET_FEATURE_INFO = "GetFeatureInfo";

}
