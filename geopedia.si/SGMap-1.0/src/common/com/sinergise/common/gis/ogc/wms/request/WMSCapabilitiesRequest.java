/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request;

import com.sinergise.common.gis.ogc.OGCException;


public class WMSCapabilitiesRequest extends WMSRequest {
	
	private static final long serialVersionUID = 1L;

    public WMSCapabilitiesRequest() {
        super(REQ_GET_CAPABILITIES);
    }
    
    @Override
	public void validate() throws OGCException {
        super.validate();
        validateNotNull(PARAM_SERVICE);
    }
    
}
