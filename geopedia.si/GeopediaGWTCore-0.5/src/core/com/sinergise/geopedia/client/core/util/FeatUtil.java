package com.sinergise.geopedia.client.core.util;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.service.FeatureServiceAsync;

public class FeatUtil {
    private static FeatureServiceAsync mySrvc;    
    private static void checkService() {
    	if (mySrvc==null) {
    		mySrvc=RemoteServices.getFeatureServiceInstance();
    	}
    }
    
    
    public static void getFeatureById(int tblId, long lastTableWrite, int featureId, boolean includeGeom,
    		final AsyncCallback<FeaturesQueryResults> cb)
    {
    	
    	Query query = new Query();
		query.startIdx=0;
		query.stopIdx=1;
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);
		if (includeGeom)
			query.options.add(Query.Options.FLDMETA_GEOMETRY);	
		query.tableId = tblId;
		query.scale = ClientGlobals.mainMapWidget.getMapComponent().getZoomLevel();
		query.dataTimestamp = lastTableWrite;
		query.filter = FilterFactory.createIdentifierDescriptor(tblId, featureId);
		RemoteServices.getFeatureServiceInstance().executeQuery(query, 
				new AsyncCallback<FeaturesQueryResults>() {

					@Override
					public void onFailure(Throwable caught) {
						cb.onFailure(caught);					
					}

					@Override
					public void onSuccess(FeaturesQueryResults result) {
						List<Feature> featList = result.getCollection();
						if (featList!=null && featList.size()>0) {
							cb.onSuccess(result);
						} else {
							cb.onSuccess(null);
						}
					}
		});
    	
    }

}
