package com.sinergise.common.gis.ogc.wms.request.ext;



import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;


public class WMSHighlightRequest extends WMSMapRequest {
	private static final long serialVersionUID = 1L;
	
	public static final String REQ_GET_MAP_HIGHLIGHT="GetMapHighlight";
	public static final String PARAM_HIGHLIGHTED_FEATURES="HIGHLIGHTED_FEATURES";
	public static final String PARAM_HIGHLIGHTED_SELECTION="HIGHLIGHTED_SELECTION";
	public static final String PARAM_RENDER_MAP="RENDER_MAP";
	
	public WMSHighlightRequest() {
		setMapRendered(false);
	}
	
	public void setHighlightedFeatures(WMSSelectionInfo selection) {
		setHighlightedFeatures(this, selection);
	}
	
	public boolean isMapRendered() {
		return WMSUtil.getBoolean(this, PARAM_RENDER_MAP, true);
	}
	
	public void setMapRendered(boolean renderMap) {
        WMSUtil.set(this, PARAM_RENDER_MAP, renderMap, true);
	}

	public static WMSSelectionInfo getSelectionSpec(OGCRequest request) {
		return WMSSelectionInfo.createFromHighlight(request);
	}

	public static void setHighlightedFeatures(OGCRequest request, WMSSelectionInfo highlight) {
		highlight.updateRequestHighlight(request);
	}
}
