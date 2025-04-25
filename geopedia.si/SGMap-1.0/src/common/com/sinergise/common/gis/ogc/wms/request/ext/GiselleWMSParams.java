/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request.ext;

public class GiselleWMSParams {
    public static final String REQ_SERVER_RESET = "ServerReset";

    public static final String CTX_PARAM_PASS = "gisellePassword";
    /**
     * Application name
     */
    public static final String PARAM_APP = "APP";
    /**
     * Configuration name
     */
    public static final String PARAM_CONFIG = "CONFIG";
    /**
     * 0-100 int (100 is best quality)
     */
    public static final String PARAM_IMAGE_QUALITY = "IMAGE_QUALITY";

    /**
     * Default is 0.28mm/px = 90.7142857 px/inch
     */
    public static final String PARAM_DPI = "DPI";
    
    public static final String CACHE_REBUILD ="CACHE_REBUILD";
    
    public static final String PARAM_INCL_FEATURE_GEOM = "INCL_FEATURE_GEOM";
}
