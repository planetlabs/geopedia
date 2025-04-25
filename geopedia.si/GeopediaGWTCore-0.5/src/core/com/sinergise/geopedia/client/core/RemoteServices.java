package com.sinergise.geopedia.client.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.service.DMVService;
import com.sinergise.geopedia.core.service.DMVServiceAsync;
import com.sinergise.geopedia.core.service.ExportService;
import com.sinergise.geopedia.core.service.ExportServiceAsync;
import com.sinergise.geopedia.core.service.FeatureService;
import com.sinergise.geopedia.core.service.FeatureServiceAsync;
import com.sinergise.geopedia.core.service.ImportService;
import com.sinergise.geopedia.core.service.ImportServiceAsync;
import com.sinergise.geopedia.core.service.MetaService;
import com.sinergise.geopedia.core.service.MetaServiceAsync;
import com.sinergise.geopedia.core.service.SessionService;
import com.sinergise.geopedia.core.service.SessionServiceAsync;

public class RemoteServices {


	private static RemoteServices INSTANCE;
	
	
	private static RemoteServices getInstance() {
		if (INSTANCE == null) {
			INSTANCE  = GWT.create(RemoteServices.class);
		}
		return INSTANCE;
	}
	
	
	
	public static FeatureServiceAsync getFeatureServiceInstance() {
		return getInstance().internalGetFeatureServiceInstance();
	}

	public static SessionServiceAsync getSessionServiceInstance() {
		return getInstance().internalGetSessionServiceInstance();
	}

	public static MetaServiceAsync getMetaServiceInstance() {
		return getInstance().internalGetMetaServiceInstance();
	}

	public static ImportServiceAsync getImportServiceInstance() {
		return getInstance().internalGetImportServiceInstance();
	}

	public static ExportServiceAsync getExportServiceInstance() {
		return getInstance().internalGetExportServiceInstance();
	}
	
	public static DMVServiceAsync getDMVServiceInstance() {
		return getInstance().internalGetDMVServiceInstance();
	}

	protected FeatureServiceAsync internalGetFeatureServiceInstance()
	{
		FeatureServiceAsync instance = (FeatureServiceAsync) GWT.create(FeatureService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + FeatureService.SERVICE_URI);
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		return instance;
	}
	
	
	
	public static class GeopediaRpcRequestBuilder extends RpcRequestBuilder {
		@Override
		protected void doFinish(RequestBuilder rb) {
			String session = ClientSession.getSessionValue();
			if (session!=null) {
				rb.setHeader(Globals.SESSION_HEADER, session);				
			}
			super.doFinish(rb);
		}
		@Override
		protected void doSetCallback(RequestBuilder rb,
				final RequestCallback callback) {
			super.doSetCallback(rb, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {					
					String sessionString = response.getHeader(Globals.SESSION_HEADER);
					if (sessionString!=null) {
						ClientSession.updateSession(sessionString); 
					}					
					callback.onResponseReceived(request, response);
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					callback.onError(request, exception);
				}
			});
		}

	}

	protected SessionServiceAsync internalGetSessionServiceInstance()
	{
		SessionServiceAsync instance = (SessionServiceAsync) GWT.create(SessionService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + SessionService.SERVICE_URI);
		return instance;
	}

	protected MetaServiceAsync internalGetMetaServiceInstance()
	{
		MetaServiceAsync instance = (MetaServiceAsync) GWT.create(MetaService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + MetaService.SERVICE_URI);
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		return instance;
	}

	protected ImportServiceAsync internalGetImportServiceInstance()
	{
		ImportServiceAsync instance = (ImportServiceAsync) GWT.create(ImportService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + ImportService.SERVICE_URI);
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		return instance;
	}

	protected ExportServiceAsync internalGetExportServiceInstance()
	{
		ExportServiceAsync instance = (ExportServiceAsync) GWT.create(ExportService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + ExportService.SERVICE_URI);
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		return instance;
	
	}

	protected DMVServiceAsync internalGetDMVServiceInstance()
	{
		DMVServiceAsync instance = (DMVServiceAsync) GWT.create(DMVService.class);
		ServiceDefTarget target = (ServiceDefTarget) instance;
		target.setServiceEntryPoint(GWT.getHostPageBaseURL() + DMVService.SERVICE_URI);
		target.setRpcRequestBuilder(new GeopediaRpcRequestBuilder());
		return instance;
	
	}
}
