/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request.ext;

import java.awt.Color;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.base.OGCImageRequest;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSRequest;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.web.MimeType;


public class WMSLegendImageRequest extends WMSRequest {

	private static final long serialVersionUID = 1L;
	
    /**
     * Hexadecimal red-green-blue colour value for the background color (default=0xFFFFFF).
     */
    public static final String PARAM_BGCOLOR = OGCImageRequest.PARAM_BGCOLOR;
    /**
     * Width in pixels of legend picture.
     */
    public static final String PARAM_WIDTH = WMSMapRequest.PARAM_WIDTH;
    /**
     * Height in pixels of legend picture.
     */
    public static final String PARAM_HEIGHT = WMSMapRequest.PARAM_HEIGHT;
    /**
     * The layer for which the image should be retrieved.
     */
    public static final String PARAM_LAYER = "LAYER";
    /**
     * The rendering style for the requested layer 
     */
    public static final String PARAM_STYLE = "STYLE";
    /**
     * Background transparency of the image (default=TRUE).
     */
    public static final String PARAM_TRANSPARENT = OGCImageRequest.PARAM_TRANSPARENT;
    
    public static final String REQ_GET_LEGEND_IMAGE = "GetLegendImage";
    
    public WMSLegendImageRequest() {
        super(REQ_GET_LEGEND_IMAGE);
        setFormat(MimeType.MIME_IMAGE_PNG);
    }
    
    public void setTransparent(boolean trans) {
        WMSUtil.set(this, PARAM_TRANSPARENT, trans, true);
    }
    
    public boolean isTransparent() {
        return WMSUtil.getBoolean(this, PARAM_TRANSPARENT, true);
    }
    
    public String getLayer() {
        return get(PARAM_LAYER);
    }
    
    public String getStyle() {
        return get(PARAM_STYLE);
    }
    
    public void setLayer(String layer) {
        set(PARAM_LAYER, layer);
    }
    
    public void setStyle(String style) {
        set(PARAM_STYLE, style);
    }
    
    public void setImageSize(DimI size) {
        set(PARAM_WIDTH, String.valueOf(size.w()));
        set(PARAM_HEIGHT, String.valueOf(size.h()));
    }
    
    public DimI getImageSize() {
        return new DimI(getInt(PARAM_WIDTH, 24),getInt(PARAM_HEIGHT, 24));
    }
    
    @Override
	public void validate() throws OGCException {
        super.validate();
        validateNotNull(PARAM_LAYER);
    }

    public Color getBackground() {
        return WMSUtil.fromWMSColor(get(PARAM_BGCOLOR, "#FFFFFF"));
    }
}
