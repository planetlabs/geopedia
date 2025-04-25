package com.sinergise.gwt.gis.tiles;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.sinergise.common.geometry.tiles.TiledDatasetProperties;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.util.http.CrossSiteHTTPRequest;
import com.sinergise.gwt.util.state.StateHelperGWT;

public class TiledDatasetPropertiesLoaderGWT {
	public interface Callback {
		public void onLoaded(TiledDatasetProperties tdProperties);
		public void onPropertyFileNotFound(TiledDatasetProperties tdProperties);
		public void onFailure(TiledDatasetProperties tdProperties, Throwable th);		
	}
	
	public static void loadTiledDatasetProperties(final TiledDatasetProperties tdProperties, String propertiesConfigURL,
			final Callback callback) {
		CrossSiteHTTPRequest req = CrossSiteHTTPRequest.create(RequestBuilder.GET, propertiesConfigURL);
		try {
			req.sendRequest(null, new RequestCallback()
	        {
	            @Override
	            public void onResponseReceived( Request request, Response response )
	            {
	            	try {
	            		if (response.getStatusCode() == Response.SC_OK) {
		            		StateGWT[] states = StateHelperGWT.readState(response.getText());
		            		if (states!=null && states.length>0) {
		            			tdProperties.configureFromState(states[0]);
		            			callback.onLoaded(tdProperties);
		            			return;
		            		} 
		            		callback.onFailure(tdProperties, new IllegalStateException("Empty configuration"));
		            		return;		            	
	            		} else if (response.getStatusCode()  == Response.SC_NOT_FOUND) {
	            			callback.onPropertyFileNotFound(tdProperties);
	            			return;
	            		} else {
	            			callback.onFailure(tdProperties, new IllegalStateException("Unknown exception. Response status: "+ response.getStatusCode()));
	            			return;
	            		}
	            	} catch (Throwable th) {
	            		callback.onFailure(tdProperties, th);
	            		return;
	            	}
	            }

	            @Override
	            public void onError( Request request, Throwable exception )
	            {
            		callback.onFailure(tdProperties, exception);
	            }
	        });
		} catch (RequestException ex) {
			callback.onFailure(tdProperties, ex);
		}
		return;
	}
}
