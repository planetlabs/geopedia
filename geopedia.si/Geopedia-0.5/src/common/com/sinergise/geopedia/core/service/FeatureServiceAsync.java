package com.sinergise.geopedia.core.service;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.service.result.FeatureHeightResult;


public interface FeatureServiceAsync
{
	
	public void queryFeatureHeights(int tableId, int featureId, AsyncCallback<List<FeatureHeightResult>> callback);
	
		

	/*-----------------------------------------------------------------------------------------*/
	
	public void saveFeature(Feature feature, AsyncCallback<Feature> callback);

	public void getForeignReferences(int tableId, String filter,  AsyncCallback<ArrayList<ForeignReferenceProperty>> callback);
	
	
	public void executeQuery (Query query, AsyncCallback<FeaturesQueryResults> callback);
	
	public void createFeatureFromGPX(int tableId, String fileToken, AsyncCallback<ArrayList<Feature>> callback);
	
}