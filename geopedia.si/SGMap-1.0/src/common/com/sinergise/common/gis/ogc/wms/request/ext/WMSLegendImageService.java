/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request.ext;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.response.WMSImageResult;


public interface WMSLegendImageService {
    WMSImageResult  getLegendImage(WMSLegendImageRequest request) throws OGCException;
}
