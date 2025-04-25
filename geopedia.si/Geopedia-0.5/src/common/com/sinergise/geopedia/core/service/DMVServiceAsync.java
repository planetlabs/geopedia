package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.service.params.GetContour;
import com.sinergise.geopedia.core.service.params.GetDMVRequest;
import com.sinergise.geopedia.core.service.params.GetHeight;
import com.sinergise.geopedia.core.service.result.GetContourResult;
import com.sinergise.geopedia.core.service.result.GetDMVResult;
import com.sinergise.geopedia.core.service.result.GetHeightResult;

public interface DMVServiceAsync extends RemoteService {
	
	
	void getDMV		(GetDMVRequest r, AsyncCallback<GetDMVResult> cb);
	
	void getHeight	(GetHeight r, 	AsyncCallback<GetHeightResult> cb);
	
	void getContour	(GetContour r, 	AsyncCallback<GetContourResult> cb);

}
