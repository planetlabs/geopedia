/*
 *
 */
package com.sinergise.common.gis.ogc.wfs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.ogc.wfs.request.WFSDescribeFeatureTypeRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSGetFeatureRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSTransactionRequest;
import com.sinergise.common.gis.ogc.wfs.response.WFSDescribeFeatureTypeResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSGetFeatureResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;

//TODO: Move to common
public interface WFSServiceAsync {
	// TODO: Make this compilable in non-gwt setting

	void describeFeatureType(WFSDescribeFeatureTypeRequest request, AsyncCallback<WFSDescribeFeatureTypeResponse> callback);

	void getFeature(WFSGetFeatureRequest request, AsyncCallback<WFSGetFeatureResponse> callback);
	
	void transaction(WFSTransactionRequest request, AsyncCallback<WFSTransactionResponse> callback);
}
