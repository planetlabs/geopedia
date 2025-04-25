/*
 *
 */
package com.sinergise.common.gis.ogc.wms.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.ogc.wms.request.WMSCapabilitiesRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSFeatureInfoRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSetNamedSelectionRequest;
import com.sinergise.common.gis.ogc.wms.response.WMSCapabilitiesResponse;
import com.sinergise.common.gis.ogc.wms.response.WMSFeatureInfoResponse;
import com.sinergise.common.gis.ogc.wms.response.ext.WMSSetNamedSelectionResponse;


//TODO: Move to common
public interface WMSServiceRPCAsync {
	//TODO: Remove reference to GWT
    void getFeatureInfo(WMSFeatureInfoRequest request, AsyncCallback<WMSFeatureInfoResponse> callback);
    
    void setNamedSelection(WMSSetNamedSelectionRequest request, AsyncCallback<WMSSetNamedSelectionResponse> callback);
    
    void getCapabilities(WMSCapabilitiesRequest request, AsyncCallback<WMSCapabilitiesResponse> callback);
}
