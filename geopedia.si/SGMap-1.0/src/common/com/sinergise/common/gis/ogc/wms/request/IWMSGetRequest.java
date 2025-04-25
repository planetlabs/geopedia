package com.sinergise.common.gis.ogc.wms.request;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.ogc.base.OGCImageRequest;
import com.sinergise.common.gis.ogc.wms.WMSLayerElement;


public interface IWMSGetRequest extends OGCImageRequest {
	public String[] getLayerNames();
	public String[] getStyleNames();
	public void setLayerNames(String[] layerNames);
	public void setLayers(WMSLayerElement[] layers);
	public void setStyleNames(String[] styleNames);
	public boolean hasLayers();
	public void setTransparent(boolean trans);
	public void setTransparentAdjustFormat(boolean trans);
	public void setCRS(CRS crs);
	public String prepareURLwithoutView(String baseURL);
	
}
