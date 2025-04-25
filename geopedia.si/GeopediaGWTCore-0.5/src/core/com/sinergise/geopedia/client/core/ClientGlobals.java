package com.sinergise.geopedia.client.core;


import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.web.bindery.event.shared.EventBus;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.search.SingleSearchExecutor;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.crs.CRSSettings;

public class ClientGlobals {
	public static Configuration configuration;
	public static String baseURL;
	public static int maxSearchResults;
	public static SingleSearchExecutor defaultSearchExecutor = null;
	public static MapWidget mainMapWidget = null;
	public static EventBus eventBus= new SimpleEventBus();
	public static CRSSettings crsSettings;
	
	public static DateTimeFormat FORMAT_DATETIME = DateTimeFormat.getFormat("dd. MM. yyyy HH:mm:ss");
	public static DateTimeFormat FORMAT_DATE =  DateTimeFormat.getFormat("dd. MM. yyyy");
	
	public static MapLayers getMapLayers() {
		return mainMapWidget.getMapLayers();
	}
	
	
	public static CRSSettings getCRSSettings() {
		return crsSettings;
	}
	
	public static WithBounds getMainCRS() {
		return crsSettings.getMainCRS();
	}
}
