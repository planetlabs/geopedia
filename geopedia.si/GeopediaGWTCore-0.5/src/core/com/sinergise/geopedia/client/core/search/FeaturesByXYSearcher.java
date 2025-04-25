package com.sinergise.geopedia.client.core.search;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;

public class FeaturesByXYSearcher implements Searcher{
	
	private class ActualSearcher {
		private double x,y;
		int scale;
		private ArrayList<ThemeTableLink> themeTables;
		private SearchListener listener;
		private int pos;
		private int maxResultsPerTable;
		
		public ActualSearcher(ArrayList<ThemeTableLink> themeTables, double x, double y, int scale, int maxResultsPerTable,
	            SearchListener listener) {
			this.scale=scale;
			this.x=x; this.y=y;
			this.themeTables = themeTables;
			this.listener=listener;
			this.maxResultsPerTable = maxResultsPerTable;
			pos=0;
		}
		
		public void next() {
			if (pos == 0) {
				listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_START, null);
			} 
			if (pos >=themeTables.size()) {
				listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE, null);
				return;
			}
			
			final ThemeTableLink ttl = themeTables.get(pos++);
			final Table table = ttl.getTable();
			if (!table.getGeometryType().isGeom()) {
				next();
				return;
			}
			listener.systemNotification(SystemNotificationType.TABLE_SEARCH_START,table, null);
			
			
			Query query = new Query();
			query.startIdx=0;
			query.stopIdx=maxResultsPerTable;
			query.options.add(Query.Options.VISIBLE);
			query.options.add(Query.Options.FLDMETA_ENVLENCEN);
			query.options.add(Query.Options.FLDMETA_BASE);
			query.options.add(Query.Options.FLDUSER_ALL);			
			query.tableId = table.id;
			if (ttl.getId()>0) {
				query.themeTableLink = ttl.clone(DataScope.BASIC);
			}
			query.scale = scale;
			query.dataTimestamp = table.lastDataWrite;
			//TODO: add theme!!
			double distance =  mapComponent.getCoordinateAdapter().worldFromPix.length(15);
			query.filter = new LogicalOperation(new ExpressionDescriptor[]{
					new BBoxOperation(new Envelope(x-distance, y-distance, x+distance,y+distance))
					 , FilterFactory.createDeletedDescriptor(table.id, false)}
				, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
			
			RemoteServices.getFeatureServiceInstance().executeQuery(query, 
					new AsyncCallback<FeaturesQueryResults>() {

						@Override
						public void onFailure(Throwable caught) {
							listener.searchResults(null, table, false, true,caught.getMessage());
							next();							
						}

						@Override
						public void onSuccess(FeaturesQueryResults result) {
							List<Feature> featList = result.getCollection();
							if (featList!=null && featList.size()>0) {
								listener.searchResults(featList.toArray(new Feature[featList.size()]), table, result.hasMoreData(), false,null);
							}
							listener.systemNotification(SystemNotificationType.TABLE_SEARCH_DONE,table, null);
							next();
						}
			});
		}
	}

	private MapComponent mapComponent;
	private MapLayers layers;
	private int scale;
	private double x,y;
	private int maxResultsPerTable;
	
	public FeaturesByXYSearcher(double x, double y, MapComponent map, int maxSearchResults) {
		this.mapComponent = map;
		this.layers = map.getMapLayers();;
		this.scale=map.getZoomLevel();
		this.x=x;
		this.y=y;
		this.maxResultsPerTable = maxSearchResults;
	}
	
	public void search(SearchListener listener) {
		 ArrayList<ThemeTableLink> tablesToSearch = new ArrayList<ThemeTableLink>();
		 layers.getThemeTables(tablesToSearch,true);
		 //TODO: put active layer on top?
		 
		ArrayList<ThemeTableLink> visibleLayers = new ArrayList<ThemeTableLink>();
		
		 int tablesRead = 0;
		 for (ThemeTableLink themeTable:tablesToSearch) {		
			 Table table = themeTable.getTable();
			 tablesRead++;
			 if (table!=null && !table.filterByScale(scale, ClientGlobals.getMainCRS())) {
		    	visibleLayers.add(themeTable);
			 }
		 }
		 if (tablesRead==tablesToSearch.size()) {
			 if (visibleLayers.isEmpty()) {
				 listener.systemNotification(SystemNotificationType.ERROR,"");
				 return;
			 }
			 
			 new ActualSearcher(visibleLayers,x,y,scale,maxResultsPerTable, listener).next();
		 } else {
			 listener.systemNotification(SystemNotificationType.ERROR, "Internal error!");
		 }
	}
}
