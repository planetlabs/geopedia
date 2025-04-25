package com.sinergise.common.raster.dataraster.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.raster.dataraster.service.average.*;

public interface DemServiceAsync {

	public void getDEMAverage(GetDemAverageRequest req, AsyncCallback<GetDemAverageResponse> callback);
	
}
