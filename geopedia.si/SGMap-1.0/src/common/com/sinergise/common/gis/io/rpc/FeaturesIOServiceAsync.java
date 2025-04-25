package com.sinergise.common.gis.io.rpc;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FeaturesIOServiceAsync {
	
	public void exportFeatures(ExportFeaturesRequest request, AsyncCallback<ExportFeaturesResponse> callback);

}
