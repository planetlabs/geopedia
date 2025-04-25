/*
 *
 */
package com.sinergise.common.gis.ogc.wfs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wfs.request.WFSDescribeFeatureTypeRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSGetFeatureRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSTransactionRequest;
import com.sinergise.common.gis.ogc.wfs.response.WFSDescribeFeatureTypeResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSGetFeatureResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;

public interface WFSService extends RemoteService {
    
	WFSDescribeFeatureTypeResponse describeFeatureType(WFSDescribeFeatureTypeRequest request) throws OGCException;
    
    WFSGetFeatureResponse getFeature(WFSGetFeatureRequest request) throws OGCException;
    
    WFSTransactionResponse transaction(WFSTransactionRequest request) throws OGCException;
    
    
    // ========================================================================================
    
    public static class Util {
        public static WFSServiceAsync createInstance(String serviceURL) {
        	WFSServiceAsync instance = (WFSServiceAsync) GWT.create(WFSService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint(serviceURL);
            return instance;
        }
    }
}
