package com.sinergise.common.gis.io.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.util.ServiceException;

@RemoteServiceRelativePath("featuresIOService")
public interface FeaturesIOService extends RemoteService{

	public ExportFeaturesResponse exportFeatures(ExportFeaturesRequest request)  throws ServiceException;
}
