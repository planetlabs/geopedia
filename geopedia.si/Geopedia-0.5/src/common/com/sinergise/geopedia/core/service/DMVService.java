package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.service.params.GetContour;
import com.sinergise.geopedia.core.service.params.GetDMVRequest;
import com.sinergise.geopedia.core.service.params.GetHeight;
import com.sinergise.geopedia.core.service.result.GetContourResult;
import com.sinergise.geopedia.core.service.result.GetDMVResult;
import com.sinergise.geopedia.core.service.result.GetHeightResult;

public interface DMVService extends RemoteService {
	
	public static final String SERVICE_URI = "dmvService";
	
	GetDMVResult 		getDMV		(GetDMVRequest request);
	
	GetHeightResult 	getHeight	(GetHeight request);

	GetContourResult 	getContour	(GetContour request);
	
}
