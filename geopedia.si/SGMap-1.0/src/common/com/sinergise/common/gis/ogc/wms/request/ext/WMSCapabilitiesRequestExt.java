package com.sinergise.common.gis.ogc.wms.request.ext;

import com.sinergise.common.gis.ogc.wms.WMSLayerElement;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSCapabilitiesRequest;

public class WMSCapabilitiesRequestExt extends WMSCapabilitiesRequest {
	public static final String CAP_PART_SERVICE = "SERVICE";
	public static final String CAP_PART_LAYER = "LAYER";

	public static final String PARAM_QUERY_LAYERS = "QUERY_LAYERS";
	/**
	 * Set of flags SERVICE (including request, excetption and extended info) and LAYER
	 */
	public static final String PARAM_CAPABILITIES_PARTS = "CAPABILITIES_PARTS";
	
	public WMSCapabilitiesRequestExt() {
		super();
		setCapabilitiesParts(new String[] {CAP_PART_LAYER});
	}
	
	public WMSCapabilitiesRequestExt(String[] lyrNames) {
		this();
		setQueryLayerNames(lyrNames);
	}

	public void setCapabilitiesParts(String[] strings) {
		set(PARAM_CAPABILITIES_PARTS, WMSUtil.encodeArray(strings));
	}
	
	public String[] getCapabilitiesParts() {
		return WMSUtil.decodeArray(get(PARAM_CAPABILITIES_PARTS));
	}

	public String[] getQueryLayers() {
		return WMSUtil.decodeArray(get(PARAM_QUERY_LAYERS, ""));
	}

	public void setQueryLayers(WMSLayerElement[] layers) {
		setQueryLayerNames(WMSUtil.toWMSLayerNames(layers));
	}

	public void setQueryLayerNames(String[] names) {
		set(PARAM_QUERY_LAYERS, WMSUtil.encodeArray(names));
	}
}
