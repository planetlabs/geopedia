package com.sinergise.geopedia.client.core;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.map.layers.Rasters;
import com.sinergise.geopedia.client.core.util.GMStats;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;

public class HistoryManager implements CoordinatesListener {

	private PanTimer pTimer = new PanTimer();
	private double cx, cy;
	int zoomLevel;
	
	int themeId = Integer.MIN_VALUE;	
	Table viewedTable = null;
	Feature selectedFeature = null;
	
	
	
	
	private MapWidget mapWidget = null;
	private Rasters rasters = null;
	private boolean suspended = false;
	
	public HistoryManager(MapWidget mapWidget) {
		this.mapWidget = mapWidget;
		MapLayers mapLayers =mapWidget.getMapLayers();
		DisplayCoordinateAdapter dca = mapWidget.getMapComponent().getCoordinateAdapter();
		
		cx = dca.worldCenterX;
		cy = dca.worldCenterY;
		zoomLevel = mapWidget.getMapComponent().getZoomLevel();
		Theme theme = mapLayers.getDefaultTheme().getEntity();
		if (theme!=null) {
			themeId=theme.id;
		}
		rasters = mapWidget.getRasters();
		
		
		FeatureInfoEvent.register(ClientGlobals.eventBus, new FeatureInfoEvent.Handler() {

			@Override
			public void onFeatureInfo(FeatureInfoEvent event) {
				if (event.hasShowDetails()) {
					Feature f = event.getFeature();
					
					String theme = "0";
					if (themeId!=Integer.MIN_VALUE) {
						theme=Integer.toString(themeId);
					}
			        GMStats.stats(GMStats.DIALOG_FEATURE_INFO, new String[] { GMStats.PARAM_THEME, GMStats.PARAM_TABLE, GMStats.PARAM_FEATURE },
			        		new String[] { theme,
			                String.valueOf(f.tableId),
			                String.valueOf(f.id) });
				}
				
				if (event.hasHighlight()) {					
					Feature feature = event.getFeature();
					selectedFeature = feature;
				} else {					
					selectedFeature=null;
				}
				
				applyHistory();
			}
			
		});
		
		OpenSidebarPanelEvent.register(ClientGlobals.eventBus, new OpenSidebarPanelEvent.Handler() {
			
			@Override
			public void onOpenSidebarPanel(OpenSidebarPanelEvent event) {
				if (event.getType() == SidebarPanelType.TABLE_EDITOR_TAB){
					viewedTable = event.getTable();
					applyHistory();
				} 			
			}
			
			@Override
			public void onOpenCustomSidebarPanel(ActivatableTabPanel panel) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCloseSidebarPanel(OpenSidebarPanelEvent event) {
				if (event.getType() == SidebarPanelType.TABLE_EDITOR_TAB){
					viewedTable = null;
					applyHistory();
				}
			}
		});
	}
	
	
	public MapWidget getMapWidget() {
		return mapWidget;
	}
	
	public void start() {
		MapLayers mapState =mapWidget.getMapLayers();
		DisplayCoordinateAdapter dca = mapWidget.getMapComponent().getCoordinateAdapter();
		dca.addCoordinatesListener(this);
		
		mapState.getDefaultTheme().addEntityChangedListener(new EntityChangedListener<Theme>() {
			
			@Override
			public void onEntityChanged(IsEntityChangedSource source, Theme value) {
				int newId = Integer.MIN_VALUE; 
				if (value!=null) {
					newId = value.getId();
				}
				if (newId != themeId) {
					themeId = newId;
					selectedFeature = null;
					viewedTable = null;
					applyHistory();
				}
			}
		});
		
			
		
		rasters.addListener(new Rasters.Listener() {
			
			@Override
			public void rastersChanged(boolean justOnOff) {
				if (justOnOff) {
					applyHistory();
				}
				
			}
		});
	}
	
	
	public void suspend (boolean suspend) {
		suspended = suspend;
	}
	
	private class PanTimer extends Timer {
		private boolean updateScheduled = false;
		@Override
		public void run()
		{
			updateScheduled = false;
			applyHistory();
		}

		public void doUpdate(int millis)
		{
			if (millis == 0) {
				cancel();
				run();
				return;
			}
			cancel();
			updateScheduled = true;
			schedule(millis);
		}
		
	}
		
	
	public String getCurrentToken() {
		
		BaseLayer dSet = rasters.getVisibleBaseLayer();
		Integer baseLayerId = null;
		if (dSet!=null) {
			baseLayerId=dSet.id;
		}
		String token = historyTokenFromParameters(themeId, viewedTable, selectedFeature,
				cx, cy, zoomLevel, baseLayerId);
		
		return token;
	}
	private void applyHistory() {
		if (suspended)
			return;
		
		String token = getCurrentToken();
		
		History.newItem(token, false);
	}
	
	
	private static void appendTokenParam (StringBuffer buf, String param)  {
		if (buf.length()>0) {
			buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
		}
		buf.append(param);
	}
	
	private static String historyTokenFromParameters(int themeId, Table viewedTable, Feature selectedFeature, double cx, double cy,
			int zoomLevel, Integer baseLayerId) {
		
		StringBuffer buf = new StringBuffer();
		if (themeId!=Integer.MIN_VALUE) {
			appendTokenParam(buf, EntityConsts.PREFIX_THEME+String.valueOf(themeId));
		}
		
		if (viewedTable!=null) {
			appendTokenParam(buf, EntityConsts.PREFIX_LAYER+String.valueOf(viewedTable.getId()));
		}
		if (selectedFeature!=null) {
			appendTokenParam(buf, EntityConsts.PREFIX_FEATURE
					+String.valueOf(selectedFeature.getTableId())+":"+String.valueOf(selectedFeature.getId()));
		}
		
		if (cx!=Double.NaN && cy!=Double.NaN) {
			appendTokenParam(buf, EntityConsts.PARAM_X+String.valueOf(cx));
			appendTokenParam(buf, EntityConsts.PARAM_Y+String.valueOf(cy));
		}
		
		if (zoomLevel>=0) {
			appendTokenParam(buf, EntityConsts.PARAM_SCALE+String.valueOf(zoomLevel));
		}
		if (baseLayerId!=null) {
			appendTokenParam(buf, EntityConsts.PARAM_BASELAYERS+String.valueOf(baseLayerId));
		}
		return buf.toString();
			
	}
	/*
	private static String historyTokenFromParameters(String theme, String editedTable, String selectedFeature, String cx, String cy, String zoomLevel,
			String baselayers) {

		boolean featureIsSet = false;

		StringBuffer buf = new StringBuffer();

		if (theme != null && theme.length() > 0) {
			try {
				int themeId = new Integer(theme).intValue();
				if (themeId >= 0) {
					buf.append(EntityConsts.PREFIX_THEME + themeId);
				}
			} catch(Throwable cause) {}
		}
		if (layer != null && layer.length() > 0) {
			int layerId = new Integer(layer).intValue();
			if (layerId >= 0) {
				if (buf.length() > 0) {
					buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
				}
				buf.append(EntityConsts.PREFIX_LAYER + layer);
			}
		}
		if (selectedFeature!=null && selectedFeature.length() > 0 ) {
			
		}
		if (feature != null && feature.length() > 0) {
			try {
				{
					if (feature.contains(";")) {
						String[] parts = feature.split(";");
						int layerId = new Integer(parts[0]).intValue();
						int featureId = new Integer(parts[1]).intValue();

						if (featureId >= 0 && layerId >= 0) {
							if (buf.length() > 0) {
								buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
							}
							buf.append(EntityConsts.PREFIX_FEATURE + feature);
							{
								featureIsSet = true;
							}
						}
					} else {
						int featureId = new Integer(feature).intValue();
						if (featureId >= 0) {
							if (buf.length() > 0) {
								buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
							}
							buf.append(EntityConsts.PREFIX_FEATURE + feature);
							{
								featureIsSet = true;
							}
						}
					}
				}
			} catch(Throwable cause) {}
		}
		// do not add cx and cy if feature is set
		if (cx != null && cx.length() > 0 && cy != null && cy.length() > 0) {
			try {
				double _cx = new Double(cx).doubleValue();
				double _cy = new Double(cy).doubleValue();
				if (buf.length() > 0) {
					buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
				}
				buf.append(EntityConsts.PARAM_X + _cx);
				buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
				buf.append(EntityConsts.PARAM_Y + _cy);
			} catch(Throwable cause) {}
		}
		if (zoomLevel != null && zoomLevel.length() > 0) {
			try {
				int _zoomLevel = new Integer(zoomLevel).intValue();
				if (buf.length() > 0) {
					buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
				}
				buf.append(EntityConsts.PARAM_SCALE + _zoomLevel);
			} catch(Throwable cause) {}
		}
		if (baselayers != null && baselayers.length() > 0) {
			if (buf.length() > 0) {
				buf.append(EntityConsts.URL_ITEMS_SEPARATOR);
			}
			buf.append(EntityConsts.PARAM_BASELAYERS + baselayers);
		}
		

		String historyToken = buf.toString();
		return historyToken;
	}
	
	*/
	



	@Override
	public void coordinatesChanged(double newX, double newY, double newScale,
			boolean coordsChanged, boolean scaleChanged) {
			this.cx=newX;
			this.cy=newY;
			this.zoomLevel=mapWidget.getMapComponent().getZoomLevel();		
			if (suspended) return;
			pTimer.doUpdate(1000);
		}

	@Override
	public void displaySizeChanged(int newWidthPx, int newHeightPx) {
		// TODO Auto-generated method stub
		
	}

}
