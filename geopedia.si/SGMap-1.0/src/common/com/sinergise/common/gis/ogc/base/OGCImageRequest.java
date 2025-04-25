package com.sinergise.common.gis.ogc.base;

public interface OGCImageRequest {
	/**
	 * Hexadecimal red-green-blue colour value for the background color (default=0xFFFFFF).
	 */
	public static final String PARAM_BGCOLOR = "BGCOLOR";
	/**
	 * Background transparency of map (default=FALSE).
	 */
	public static final String PARAM_TRANSPARENT = "TRANSPARENT";
	
	/**
	 * Elevation value of layer desired.
	 */
	public static final String PARAM_DIMENSION_ELEVATION = "ELEVATION";
	/**
	 * Time value of layer desired.
	 */
	public static final String PARAM_DIMENSION_TIME = "TIME";

	int getImageWidth();
	int getImageHeight();
	boolean isTransparent();
}
