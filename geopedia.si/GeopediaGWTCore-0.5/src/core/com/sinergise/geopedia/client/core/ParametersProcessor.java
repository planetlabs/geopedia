package com.sinergise.geopedia.client.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.map.layers.Rasters;
import com.sinergise.geopedia.client.core.search.FeatureByIdSearcher;
import com.sinergise.geopedia.client.core.search.SearchExecutor;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public class ParametersProcessor {

	private ArrayList<Processor> additionalProcessors = new ArrayList<Processor>();

	public interface Processor {
		public boolean processParameters(HashMap<String, String> itemMap);
	}
	
	protected MapLayers mapState;
	protected MapComponent mapComponent;
	protected SearchExecutor searchExecutor;
	protected Rasters rasters;
	
	public ParametersProcessor (MapComponent mapComponent, Rasters rasters,  SearchExecutor searchExecutor) {
		this.mapComponent = mapComponent;
		this.searchExecutor = searchExecutor;
		this.rasters = rasters;
		mapState = mapComponent.getMapLayers();
	}
	
	public void addAdditionalProcessor(Processor p) {
		additionalProcessors.add(p);
	}
	private Integer toInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {			
		}
		return null;
	}
	private Double toDouble(String value) {
		if (value==null)
			return null;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {			
		}
		return null;
	}
	
	
	public static void parseParameters (HashMap<String,String> itemMap, String paramString) {
	    
        if (paramString != null && paramString.length() > 0) {
            String[] elems = paramString.split("\\" + EntityConsts.URL_ITEMS_SEPARATOR);
            for (String elem : elems) {
                String normLink = elem.replaceAll("\\(", "");
                normLink = normLink.replaceAll("\\)", "");
                if (normLink.length() > 1) {
                    String paramName = String.valueOf(normLink.charAt(0));
                    String paramVal = normLink.substring(1);
                    itemMap.put(paramName, paramVal);
                }
            }
        }
	}
	
	public static int[] parseBaseLayers(String pValue) {
		if (pValue==null)
			return null;
		String[] rasterLayers = pValue.split(Globals.BASELAYERS_SEPARATOR);
        ArrayList<Integer> layers = new ArrayList<Integer>();
        for (int j = 0; j < rasterLayers.length; j++) {
        	try {
                layers.add(Integer.parseInt(rasterLayers[j]));
        	} catch (NumberFormatException ex) {}
        }
        final int lyrs[] = new int[layers.size()];
        for (int i=0;i<layers.size();i++)
        	lyrs[i]=layers.get(i);
        return lyrs;
	}
	
	
	public void processParameters(String paramString) {
		HashMap<String, String> itemMap = new HashMap<String,String>();
		parseParameters(itemMap,paramString);
		processParameters(itemMap);
	}
	
	
	public void processParameters(ArrayList<String> paramStrings) {
		HashMap<String, String> itemMap = new HashMap<String,String>();
		for (String paramString:paramStrings)
			parseParameters(itemMap,paramString);
		processParameters(itemMap);
	}
	
	public void processParameters(final HashMap<String, String> itemMap) {
		
        String pValue =null;
         
        boolean locationSet = false;
        boolean scaleSet = false;
        // parse WGS84 coordinate parameter
        String wgsCoords = itemMap.remove(EntityConsts.PARAM_WGS48_COORDINATES);
        if (!StringUtil.isNullOrEmpty(wgsCoords)) {
        	String coords [] = wgsCoords.split(",");
        	if (coords.length==2) {
        		try {
        			Point p = new Point (Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
        			CRSSettings crsSettings = ClientGlobals.crsSettings;
        			Transform<?,?> transform = crsSettings.getTransform(
        					CRS.WGS84.getDefaultIdentifier(), crsSettings.getMainCrsId(), false);
        			if (transform!=null) {
        				transform.point(p, p);
        				itemMap.put(EntityConsts.PARAM_X, String.valueOf(p.x()));
        				itemMap.put(EntityConsts.PARAM_Y, String.valueOf(p.y()));
        			}
        		} catch (NumberFormatException ex) {
        		}
        	}
        }
        
        if (itemMap.get(EntityConsts.PARAM_X)!=null || 
       		itemMap.get(EntityConsts.PARAM_Y)!=null) {
        	Double x = toDouble(itemMap.get(EntityConsts.PARAM_X));
        	Double y = toDouble(itemMap.get(EntityConsts.PARAM_Y));
        	setMapPosition(x,y);
        	locationSet = true;
        	
        }
        
        if ((pValue=itemMap.get(EntityConsts.PARAM_SCALE))!=null) {
        	Integer scale = toInteger(pValue);
        	if (scale!=null) {
        		setMapScale(scale);
        		scaleSet=true;
        	}
        }
        
        
        final Integer setBaseLayer;
        if ((pValue=itemMap.get(EntityConsts.PARAM_BASELAYERS))!=null) {        	
            String[] rasterLayers = pValue.split(Globals.BASELAYERS_SEPARATOR);
            ArrayList<Integer> layers = new ArrayList<Integer>();
            for (int j = 0; j < rasterLayers.length; j++) {
            	try {
	                layers.add(Integer.parseInt(rasterLayers[j]));
            	} catch (NumberFormatException ex) {}
            }
            final int lyrs[] = new int[layers.size()];
            for (int i=0;i<layers.size();i++)
            	lyrs[i]=layers.get(i);
            
            if (lyrs.length>0) {
	            setBaseLayer = lyrs[0];
            } else {
            	setBaseLayer = null;
            }
        } else {
        	setBaseLayer=null;
        }
 
        
        
        Integer themeToSet = null;
        
        // set theme
        if ((pValue=itemMap.get(EntityConsts.PREFIX_THEME))!=null) {
        	themeToSet = toInteger(pValue);
        }
        
        if (themeToSet != null) {
        	final boolean lSet = locationSet;
			final boolean sSet = scaleSet;
        	mapState.setDefaultTheme(themeToSet, new AsyncCallback<ThemeHolder>() {

				@Override
				public void onFailure(Throwable caught) {
					handleException(caught, itemMap);
				}

				@Override
				public void onSuccess(ThemeHolder result) {
					themeEnabled(result.getEntity());
					afterThemeProcessors(itemMap, setBaseLayer, lSet,sSet);
				}
			});
        } else {
        	afterThemeProcessors(itemMap, setBaseLayer, locationSet, scaleSet);
        }
       
	}
	
	private static String parameterMapToString(HashMap<String,String> paramMap) {
		boolean first = true;
		String out="";
		for (String key:paramMap.keySet()) {
			if (!first) out+=EntityConsts.URL_ITEMS_SEPARATOR;
			out+=key+paramMap.get(key);
			first=false;
		}
		return out;
	}
	private void handleException(Throwable caught, HashMap<String,String> params) {
		if (caught instanceof GeopediaException) {
			if (((GeopediaException)caught).getType() == GeopediaException.Type.NOT_LOGGED_IN) {
				ClientGlobals.eventBus.fireEvent(ClientSessionEvent.createAutologinEvent(parameterMapToString(params)));
			}
		}
	}
	
	private void afterThemeProcessors(final HashMap<String, String> itemMap, Integer baseLayerId, final boolean locationSet, final boolean scaleSet) {
		
		Theme th = mapState.getDefaultTheme().getEntity();
		if (th!=null) {
			rasters.setEnabled(th.baseLayers);
		}
		rasters.setVisibleBaseLayer(baseLayerId);

		
		Integer viewedTable = null;		
		Integer featureTable = null;
		
		String strFeature = itemMap.get(EntityConsts.PREFIX_FEATURE);
		String strTable = itemMap.get(EntityConsts.PREFIX_LAYER);
		if (strFeature!=null){
			if (strFeature.contains(EntityConsts.SEPARATOR_FEATURE_TABLE)) {
				String split[] = strFeature.split(EntityConsts.SEPARATOR_FEATURE_TABLE);
				featureTable = Integer.parseInt(split[0]);			
				if (strTable!=null) {
					viewedTable = Integer.parseInt(strTable);
				}
			} else if (strTable!=null) {
				featureTable = Integer.parseInt(strTable);				
			}
		} else if (strTable!=null){
			viewedTable = Integer.parseInt(strTable);
		}
		
		if (viewedTable!=null) {
			final boolean setFeature = featureTable!=null;
			mapState.enableTable(viewedTable, true, new AsyncCallback<Table>() {

				@Override
				public void onFailure(Throwable caught) {
					// silently ignore
				}

				@Override
				public void onSuccess(Table result) {
					OpenSidebarPanelEvent event = new OpenSidebarPanelEvent(SidebarPanelType.TABLE_EDITOR_TAB)
					.setTable(result);
					if (setFeature) { //TODO open tab but not focus!
						event.setNoFocus();
					}
					ClientGlobals.eventBus.fireEvent(event);					
				}
				
			});
		}
		
		if (featureTable != null) { 
			mapState.enableTable(featureTable, true, new AsyncCallback<Table>() {
	
				@Override
				public void onFailure(Throwable caught) {
					handleException(caught, itemMap);
				}
	
				@Override
				public void onSuccess(Table result) {
					tableEnabled(result);
					onAfterTableProcessor(itemMap, result, locationSet, scaleSet);
				}
				
			});
		} else {
			tableEnabled(null);
			onAfterTableProcessor(itemMap, null,  locationSet, scaleSet);
		}
	}
	
	protected void tableEnabled(Table table) {}
	protected void themeEnabled(Theme theme) {}
	private void onAfterTableProcessor(final HashMap<String, String> itemMap, final Table featureTable, boolean locationSet, boolean scaleSet) {

		 // set feature
		String featStr = itemMap.get(EntityConsts.PREFIX_FEATURE);
        if (featStr!=null && featureTable != null) {
        	Integer featureId = null;
        	if (featStr.contains(EntityConsts.SEPARATOR_FEATURE_TABLE)) {
        		String split[] = featStr.split(EntityConsts.SEPARATOR_FEATURE_TABLE);
        		featureId = Integer.parseInt(split[1]);
        	} else {
        		featureId = Integer.parseInt(featStr);
        	}
        	if (featureId!=null) {
        		FeatureByIdSearcher searcher = new FeatureByIdSearcher(featureTable.getId(), featureId);
        		searcher.showFeature(!locationSet, !scaleSet, mapComponent);
        		searchExecutor.executeSearch(searcher);
        	}
        }
        
        
        for (Processor p:additionalProcessors) {
        	if (!p.processParameters(itemMap))
        		return;
        }
	}

	protected void setMapScale(int scale) {
		DisplayCoordinateAdapter dca = mapComponent.getCoordinateAdapter();
		dca.setScale(mapComponent.getUserZooms().scale(scale, dca.pixSizeInMicrons));
		mapComponent.repaint(0);
	}
	
	protected void setMapPosition(Double x, Double y) {
		DisplayCoordinateAdapter dca = mapComponent.getCoordinateAdapter();
		if (x==null)
			x=dca.worldCenterX;
		if (y==null)
			y=dca.worldCenterY;
		
		dca.setWorldCenter(x, y);
		mapComponent.repaint(0);
	}
	
	
	
	protected void setActiveTable (int tableId) {
		//mapState.setActiveTable(tableId,0);
	}
	
}
