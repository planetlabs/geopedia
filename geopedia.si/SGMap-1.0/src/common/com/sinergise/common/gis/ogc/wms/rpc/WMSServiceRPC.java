/*
 *
 */
package com.sinergise.common.gis.ogc.wms.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.request.WMSCapabilitiesRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSFeatureInfoRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSetNamedSelectionRequest;
import com.sinergise.common.gis.ogc.wms.response.WMSCapabilitiesResponse;
import com.sinergise.common.gis.ogc.wms.response.WMSFeatureInfoResponse;
import com.sinergise.common.gis.ogc.wms.response.ext.WMSSetNamedSelectionResponse;


public interface WMSServiceRPC extends RemoteService {
	WMSFeatureInfoResponse getFeatureInfo(WMSFeatureInfoRequest request) throws OGCException;
	
	WMSSetNamedSelectionResponse setNamedSelection(WMSSetNamedSelectionRequest request) throws OGCException;
    
	WMSCapabilitiesResponse getCapabilities(WMSCapabilitiesRequest request) throws OGCException;
	
    // ======================================================================================== //
    
    public static class Util {
        public static WMSServiceRPCAsync createInstance(String serviceURL) {
            WMSServiceRPCAsync instance = (WMSServiceRPCAsync) GWT.create(WMSServiceRPC.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint(serviceURL);
            return instance;
        }
    }
}
