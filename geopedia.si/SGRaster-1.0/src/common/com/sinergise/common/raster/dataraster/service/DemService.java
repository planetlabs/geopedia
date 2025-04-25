package com.sinergise.common.raster.dataraster.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.raster.dataraster.service.average.*;
import com.sinergise.common.util.ServiceException;

@RemoteServiceRelativePath("dem")
public interface DemService extends RemoteService {
	
	public GetDemAverageResponse getDEMAverage(GetDemAverageRequest req) throws ServiceException;
	
	public static class App {
        public static synchronized DemServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(DemService.class);
            }
            return null;
        }
	}
	
	public static DemServiceAsync INSTANCE = App.createInstance();
}
