package com.sinergise.generics.gwt.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.core.GenericsClientSession;
import com.sinergise.generics.gwt.widgetprocessors.GWTTableDataProvider;

/**
 * 
 * @author pkolaric
 *
 */
public class GenericsGWTCache {
	private static GenericsGWTCache INSTANCE = null;	
	private static GenericsGWTCache getInstance() {
		if (INSTANCE==null) INSTANCE = new GenericsGWTCache();
		return INSTANCE;
	}
	
	public static void getCollectionValues(DataFilter filter, String datasourceID, int startIdx, int stopIdx, 
			AsyncCallback<ArrayValueHolder> callback, boolean cached) {
		getInstance().internalGetCollectionValues(filter, datasourceID, startIdx, stopIdx, callback, cached);
	}
	
	public static void invalidateDatasource(String datasourceId, AsyncCallback<Void> callback) {
		getInstance().internalInvalidateDatasource(datasourceId, callback);
	}
	
	private class DataRequest {
		public DataRequest(DataFilter filter, String datasourceID, int startIdx, int stopIdx, 
				AsyncCallback<ArrayValueHolder> callback) {
			this.filter=filter;
			this.datasourceID = datasourceID;
			this.startIdx=startIdx;
			this.stopIdx=stopIdx;
			this.callback=callback;
		}
		
		DataFilter filter;
		String datasourceID;
		int startIdx;
		int stopIdx;
		AsyncCallback<ArrayValueHolder> callback;
	}
	private static class DatasourceKey {
		String datasourceId;
		String locale;
		DataFilter filter;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((datasourceId == null) ? 0 : datasourceId.hashCode());
			result = prime * result
					+ ((locale == null) ? 0 : locale.hashCode());
			result = prime * result
					+ ((filter == null) ? 0 : filter.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatasourceKey other = (DatasourceKey) obj;
			if (datasourceId == null) {
				if (other.datasourceId != null)
					return false;
			} else if (!datasourceId.equals(other.datasourceId))
				return false;
			if (locale == null) {
				if (other.locale != null)
					return false;
			} else if (!locale.equals(other.locale))
				return false;
			if (filter == null) {
				if (other.filter != null)
					return false;
			} else if (!filter.equals(other.filter))
				return false;
			return true;
		}
		
	}
	HashMap<DatasourceKey, ArrayValueHolder> cachedDatasources= new HashMap<DatasourceKey,ArrayValueHolder>();
	ArrayList<DataRequest> pendingRequests = new ArrayList<DataRequest>();
	
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);

	private void internalInvalidateDatasource(String datasourceId, AsyncCallback<Void> callback) {
		genericsService.invalidateCache(datasourceId, callback);
	}
	
	private void loadDataToCache(final String datasourceID, DataFilter filter) {
		final DatasourceKey dsKey = new DatasourceKey();
		dsKey.locale = GenericsClientSession.getInstance().locale;
		dsKey.datasourceId = datasourceID;
		dsKey.filter = filter;
		
		if (cachedDatasources.containsKey(dsKey)) return;
		cachedDatasources.put(dsKey, null);
			
		genericsService.getCollectionValues(filter, datasourceID, -1, -1, new AsyncCallback<ArrayValueHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				Iterator<DataRequest> it=pendingRequests.iterator();
				while (it.hasNext()) {
					DataRequest req = it.next();
					if (req.datasourceID.equals(datasourceID)) {
						it.remove();
						req.callback.onFailure(caught);
					}
				}
				
			}

			@Override
			public void onSuccess(ArrayValueHolder result) {
				synchronized(pendingRequests) {
					Iterator<DataRequest> it=pendingRequests.iterator();
					while (it.hasNext()) {
						DataRequest req = it.next();
						if (req.datasourceID.equals(datasourceID)) {
							it.remove();
							ArrayValueHolder filteredData = GWTTableDataProvider.processData(result, req.filter, req.startIdx, req.stopIdx, null);
							req.callback.onSuccess(filteredData);
						}
					}
					cachedDatasources.put(dsKey, result);
				}
			}
		});
	}
	
	private void internalGetCollectionValues(DataFilter filter, String datasourceID, int startIdx, int stopIdx, 
			AsyncCallback<ArrayValueHolder> callback, boolean cached) {
		if (!cached) {
			genericsService.getCollectionValues(filter, datasourceID, startIdx, stopIdx, callback);
		} else {
			final DatasourceKey dsKey = new DatasourceKey();
			dsKey.locale = GenericsClientSession.getInstance().locale;
			dsKey.datasourceId = datasourceID;
			dsKey.filter = filter;
			
			ArrayValueHolder data = cachedDatasources.get(dsKey);
			if (data!=null) {
				ArrayValueHolder filteredData = GWTTableDataProvider.processData(data, filter, startIdx, stopIdx, null);
				callback.onSuccess(filteredData);
			} else {
				DataRequest dr = new DataRequest(filter, datasourceID, startIdx, stopIdx, callback);
				synchronized(pendingRequests) {
					pendingRequests.add(dr);
				}
				loadDataToCache(datasourceID, filter);
			}
		}
	}
}
