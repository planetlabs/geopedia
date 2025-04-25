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


public class WMSLegendImageBundleRequest extends WMSRequest {

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
     * The layers for which the image should be retrieved.
     */
    public static final String PARAM_LAYERS = "LAYERS";
    /**
     * The rendering style for the requested layer 
     */
    public static final String PARAM_STYLES = "STYLES";
    /**
     * Background transparency of the image (default=TRUE).
     */
    public static final String PARAM_TRANSPARENT = OGCImageRequest.PARAM_TRANSPARENT;
    
    public static final String REQ_GET_LEGEND_IMAGE_BUNDLE = "GetLegendImageBundle";
    
    public WMSLegendImageBundleRequest() {
        super(REQ_GET_LEGEND_IMAGE_BUNDLE);
        setFormat(MimeType.MIME_IMAGE_PNG);
    }
    
    public void setTransparent(boolean trans) {
        WMSUtil.set(this, PARAM_TRANSPARENT, trans, true);
    }
    
    public boolean isTransparent() {
        return WMSUtil.getBoolean(this, PARAM_TRANSPARENT, true);
    }
    
    public String[] getLayers() {
    	return WMSUtil.decodeArray(get(PARAM_LAYERS));
    }
    
    public String[] getStyles() {
        return WMSUtil.decodeArray(get(PARAM_STYLES));
    }
    
    public void setLayers(String[] layers) {
        set(PARAM_LAYERS, WMSUtil.encodeArray(layers));
    }
    
    public void setStyles(String[] styles) {
    	set(PARAM_STYLES, WMSUtil.encodeArray(styles));
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
        validateNotNull(PARAM_LAYERS);
    }

    public Color getBackground() {
        return WMSUtil.fromWMSColor(get(PARAM_BGCOLOR, "#FFFFFF"));
    }
}
