package com.sinergise.gwt.gis.ogc.ui;


import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;

public interface WMSRenderer {
	public WMSLayersSource getService();

	public void addLayer(WMSLayer lyr);

}
