/*
 *
 */
package com.sinergise.common.gis.ogc.wms;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSHighlightRequest;
import com.sinergise.common.gis.ogc.wms.response.WMSImageResult;
import com.sinergise.common.gis.ogc.wms.rpc.WMSServiceRPC;


public interface WMSService extends WMSServiceRPC {
    WMSImageResult getMap(WMSMapRequest request) throws OGCException;
    WMSImageResult getMapHighlight(WMSHighlightRequest request) throws OGCException;
}
