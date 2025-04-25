package com.sinergise.geopedia.client.ui.map;

import java.util.ArrayList;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.map.GeopediaTilesProvider;
import com.sinergise.geopedia.client.core.map.HighlightTilesProvider;
import com.sinergise.geopedia.client.core.map.RastersTilesProvider;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.map.layers.Rasters;
import com.sinergise.geopedia.client.core.map.layers.Rasters.Listener;
import com.sinergise.geopedia.client.ui.map.controls.KeyboardHandler;
import com.sinergise.geopedia.client.ui.map.controls.KeyboardHandlerMappings;
import com.sinergise.geopedia.client.ui.map.controls.RasterButtonsPanel;
import com.sinergise.geopedia.client.ui.map.widgets.CopyrightPanel;
import com.sinergise.geopedia.core.config.Copyright;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.WMSBaseLayer;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.gwt.gis.map.ui.actions.CoordinatesLabel;
import com.sinergise.gwt.gis.map.ui.actions.ShowCoordsAction;
import com.sinergise.gwt.gis.map.ui.controls.PanCtrl;
import com.sinergise.gwt.gis.map.ui.controls.ScaleBox;
import com.sinergise.gwt.gis.map.ui.controls.ZoomSliderCtrl;
import com.sinergise.gwt.gis.map.ui.overlays.TiledOverlay;
import com.sinergise.gwt.gis.ogc.ui.TiledWMSRenderer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;
import com.sinergise.gwt.ui.core.MouseHandler;


public class MapWidget extends FlowPanel implements RequiresResize {

	protected static final int Z_BASELAYERS=10;
	protected static final int Z_THEMELAYERS=20;
	protected static final int Z_HIGHLIGHTLAYER = 30;	
	protected MapComponent mapComponent;
	private TiledOverlay themeOvr;
	private TiledOverlay rasterOvr;
	private RastersTilesProvider tilesProvider;
	private TiledOverlay highlightOvr;
	private TiledOverlay wmsBaseOverlay;
	private HighlightTilesProvider highliter;
	private Panel mapHolder;
	
	private CopyrightPanel copyrightPanel;
	private Rasters rasters;
	protected TiledCRS tiledCRS;
	public MapWidget() {
		mapHolder=getMapPanel();
		mapHolder.setStyleName("mapComponent");
		mapComponent = new MapComponent(mapHolder);
		mapComponent.setSize("100%","100%");
		add(mapComponent);
		
		initialize();
		
		
		
		addRastersControls(this);
		addZoomControls(this);
		addPanControls(this);
		addScaleLabel(this, mapComponent.getCoordinateAdapter());
		addFooter(this);
		addCoordinatesLabel(this);
		
		
		
		FeatureInfoEvent.register(ClientGlobals.eventBus, new FeatureInfoEvent.Handler() {

			@Override
			public void onFeatureInfo(FeatureInfoEvent event) {
				if (event.hasZoomTo()) {
					Feature feat = event.getFeature();					
					mapComponent.ensureVisible(feat.envelope, true, feat.getGeometryType().isPoint());
				}
				
				highliter.clearFeats();
				if (event.hasHighlight()) {
					Feature feat = event.getFeature();
					if (!feat.isDeleted()) {
						highliter.addFeat(feat, feat.getTableId());
					}
				}				
				mapComponent.repaint(0);
			}
			
		});
	}
	
	protected Panel getMapPanel() {
		return new FlowPanel();
	}
	/*
	
	private void getDatasetPropertiesRPC(final TiledBaseLayer tbl, String reason, final SGAsyncCallback<TiledBaseLayer> callback) {
		RemoteServices.getSessionServiceInstance(ClientSession.cookieName).getDatasetConfiguration(tbl.id, reason, new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) { 
				if (!StringUtil.isNullOrEmpty(result)) {
					try {
	            		StateGWT[] states = StateHelperGWT.readState(result);
		            		if (states!=null && states.length>0) {
		            			tbl.getDatasetProperties().configureFromState(states[0]);
		            		}
	            	} catch (Throwable th) {
	            		th.printStackTrace();
	            	}
				}
        		callback.onSuccess(tbl);				
			}
			
			@Override
			public void onFailure(Throwable caught) {
        		callback.onSuccess(tbl);				
			}
		});
	}

	public void getVisibleRasterDataset(final SGAsyncCallback<TiledBaseLayer> callback) {
		BaseLayer bLayer = rasters.getVisibleBaseLayer();
		if (bLayer!=null && bLayer instanceof TiledBaseLayer)  {		
			final TiledBaseLayer tbl = (TiledBaseLayer)bLayer;
			CrossSiteHTTPRequest req = CrossSiteHTTPRequest.create(RequestBuilder.GET, tbl.getPropertiesFileLocation());
			try {
				req.sendRequest(null, new RequestCallback()
		        {
		            @Override
		            public void onResponseReceived( Request request, Response response )
		            {
	            		if (response.getStatusCode() == Response.SC_OK) {
			            	try {
			            		StateGWT[] states = StateHelperGWT.readState(response.getText());
			            		if (states!=null && states.length>0) {
			            			tbl.getDatasetProperties().configureFromState(states[0]);
			            		}
				            	callback.onSuccess(tbl);
			            	} catch (Throwable th) {
				            	getDatasetPropertiesRPC(tbl, "Failed to parse response: '"+th.getMessage()+"'", callback);
			            	}
			            	return;
	            		} else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
			            	callback.onSuccess(tbl);	            			
	            		} else {
	            			getDatasetPropertiesRPC(tbl, "Unexpected resposne: "+response.getStatusCode()+"'"+response.getStatusText()+"'", callback);
	            		}
		            }

		            @Override
		            public void onError( Request request, Throwable ex ) {
		            	getDatasetPropertiesRPC(tbl, "Exception during CrossSiteRequest:'"+ex.getMessage()+"'", callback);
		            }
		        });
			} catch (RequestException ex) {
				ex.printStackTrace();
				callback.onSuccess(null);
			}
			return;
		} else  {
			callback.onSuccess(null);
			return;
		}
	}
	
*/
	protected void initialize() {
		rasters = new Rasters();
		
		BaseLayer bLayer = rasters.getVisibleBaseLayer();
		TiledBaseLayer tiledBaseLayer = null;
		if (bLayer!= null && bLayer instanceof TiledBaseLayer) {
			tiledBaseLayer = (TiledBaseLayer) bLayer;
		}
		tilesProvider = new RastersTilesProvider(tiledBaseLayer);
		tiledCRS = ClientGlobals.getMainCRS(); 
		CRS crs = tiledCRS.baseCRS;
		rasterOvr = new TiledOverlay(crs, tilesProvider);
		
//		getVisibleRasterDataset(new SGAsyncCallback<TiledBaseLayer>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//			}
//
//			@Override
//			public void onSuccess(TiledBaseLayer result) {
//				tilesProvider = new  RastersTilesProvider(result);
//				rasterOvr.setTilesProvider(tilesProvider);
//			}
//			
//		});
		
		rasters.addListener(new Listener() {
			
			@Override
			public void rastersChanged(boolean justOnOff) {
				BaseLayer bLayer = rasters.getVisibleBaseLayer();
				TiledBaseLayer tiledBaseLayer = null;
				if (bLayer!= null && bLayer instanceof TiledBaseLayer) {
					tiledBaseLayer = (TiledBaseLayer) bLayer;
				}
				
				tilesProvider = new  RastersTilesProvider(tiledBaseLayer);
				rasterOvr.setTilesProvider(tilesProvider);
				mapComponent.repaint(1);

//				getVisibleRasterDataset(new SGAsyncCallback<TiledBaseLayer>() {
//
//					@Override
//					public void onFailure(Throwable caught) {
//					}
//
//					@Override
//					public void onSuccess(TiledBaseLayer result) {
//						tilesProvider = new  RastersTilesProvider(result);
//						rasterOvr.setTilesProvider(tilesProvider);
//						mapComponent.repaint(1);
//					}
//					
//				});
			
			}
		});
	
		rasters.addListener(new Listener() {

			@Override
			public void rastersChanged(boolean justOnOff) {
				BaseLayer bLayer = rasters.getVisibleBaseLayer();
				boolean repaint = false;
				if (wmsBaseOverlay!=null) {
					mapComponent.removeOverlay(wmsBaseOverlay);
					wmsBaseOverlay=null;
					repaint=true;
				}

				if (bLayer != null && bLayer instanceof WMSBaseLayer) {
					WMSBaseLayer wmsBaseLayer = (WMSBaseLayer) bLayer;
					String version = null;
					if (wmsBaseLayer.wmsParameters!=null) {
						version = wmsBaseLayer.wmsParameters.get(OGCRequest.PARAM_VERSION);
					}
					
					WMSLayersSource wmsSource = null;
					if (version==null)
						wmsSource = new WMSLayersSource(wmsBaseLayer.wmsBaseURL);
					else 
						wmsSource = new WMSLayersSource(wmsBaseLayer.wmsBaseURL, wmsBaseLayer.wmsBaseURL, version);
					
					TiledWMSRenderer wmsRenderer = new TiledWMSRenderer(tiledCRS, wmsSource);
					if (wmsBaseLayer.wmsParameters!=null) {
						for (String key:wmsBaseLayer.wmsParameters.keySet()) {
							wmsSource.getRequestDefaults().set(key, wmsBaseLayer.wmsParameters.get(key));
						}
					}
					
					wmsBaseOverlay = new TiledOverlay(tiledCRS.baseCRS, wmsRenderer);
					WMSLayer wmsLayer = new WMSLayer(wmsSource.createLayerSpec(wmsBaseLayer.wmsLayerName));
					wmsLayer.setOpaque(true);
					wmsRenderer.addLayer(wmsLayer);
					mapComponent.addOverlay(wmsBaseOverlay,Z_BASELAYERS+1);
					repaint = true;
				}
				if (repaint) 
					mapComponent.repaint(1);
			}
			
		});
		

		themeOvr = new TiledOverlay(crs, new GeopediaTilesProvider(mapComponent.getMapLayers()));
		themeOvr.setMaxTileFetchRetry(2);
		highliter = new HighlightTilesProvider();
		highlightOvr = new TiledOverlay(crs, highliter);
		highlightOvr.setMaxTileFetchRetry(2);
		
		mapComponent.addOverlay(rasterOvr, Z_BASELAYERS);
		mapComponent.addOverlay(themeOvr, Z_THEMELAYERS);
		mapComponent.addOverlay(highlightOvr, Z_HIGHLIGHTLAYER);
		
		
		
		registerKeyboardActions();
	}
	

	protected void registerKeyboardActions() {
		KeyboardHandler keyboardHandler = new KeyboardHandler(this);
		KeyboardHandlerMappings.setup(mapComponent,keyboardHandler); 
		Event.addNativePreviewHandler(keyboardHandler);
	}
	
	
	public HighlightTilesProvider getFeatureHighlighter() {
		return highliter;
	}
	public MouseHandler getMouseHandler() {
		return mapComponent.mouser;
	}
	public void repaint() {
		mapComponent.repaint(5);
	}
	
	public MapLayers getMapLayers() {
		return mapComponent.getMapLayers();
	}
	
	public MapComponent getMapComponent() {
		return mapComponent;
	}
	public Rasters getRasters() {
		return rasters;
	}
	
	
	public ArrayList<Copyright> getCopyrights() {
		if (copyrightPanel==null)
			return null;
		return copyrightPanel.getCopyrights();
	}
	
	@Override
	protected void onAttach() {
	    super.onAttach();
	    mapComponent.getMapLayers().addValueChangeListener(new EntityChangedListener<Theme>() {
			
			@Override
			public void onEntityChanged(IsEntityChangedSource source, Theme value) {
				mapComponent.repaint(300);
			}
		});
	}

	
	protected void addRastersControls(FlowPanel panel) {
		panel.add(new RasterButtonsPanel(rasters));
	}
	
	protected void addZoomControls(FlowPanel panel) {
		ZoomSliderCtrl zsc = new ZoomSliderCtrl(mapComponent);
		zsc.setVisible(true);
		panel.add(zsc);
	}
	
	protected void addPanControls(FlowPanel panel) {
		panel.add(new PanCtrl(mapComponent));
	}

	protected void addScaleLabel(FlowPanel panel, DisplayCoordinateAdapter dca) {
		panel.add(new ScaleBox(dca));
	}
	
	protected void addCoordinatesLabel(FlowPanel panel) {
		CoordinatesLabel clMap = new CoordinatesLabel();
		DisplayCoordinateAdapter dca = mapComponent.getCoordinateAdapter();
		clMap.setCRS(dca.worldCRS);
		Transform<?,?> tr = ClientGlobals.getCRSSettings().getTransform(dca.worldCRS, CRS.WGS84);
		if (tr!=null && tr instanceof ToLatLon<?,?> )
			clMap.setTransform((ToLatLon<?,?>)tr);
		ShowCoordsAction sca = new ShowCoordsAction(dca, clMap);
		mapComponent.getMouseHandler().registerAction(sca,  MouseHandler.MOD_NONE);
		panel.add(clMap);
	}
	
	
	protected CopyrightPanel createCopyrightPanel (Rasters rasters, DisplayCoordinateAdapter dca) {
		return new CopyrightPanel(rasters, mapComponent.getCoordinateAdapter());
	}
	protected void addFooter(FlowPanel panel) {
		if (copyrightPanel != null) 
			return;
		copyrightPanel = createCopyrightPanel(rasters, mapComponent.getCoordinateAdapter());
		copyrightPanel.setStyleName("mapFooter");
		panel.add(copyrightPanel);
	}

	public HighlightTilesProvider getHighlightProvider() {
		return highliter;
	}

	@Override
	public void onResize() {
		if (getOffsetHeight()<300) {
			addStyleName("smallHeight");
		} else {
			removeStyleName("smallHeight");
		}
		
	}
	
	public void setAppVersion(String appVersionTxt) {
		HTML appVersion = new HTML(appVersionTxt);
		appVersion.setStyleName("appVersion");
		this.add(appVersion);
	}
}
