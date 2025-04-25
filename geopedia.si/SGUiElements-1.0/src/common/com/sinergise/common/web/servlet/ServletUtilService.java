package com.sinergise.common.web.servlet;

import java.util.Map;

import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;

@RemoteServiceRelativePath("servlet")
public interface ServletUtilService extends RemoteService {

	GetServletInitParamsResponse getServletInitParams(GetServletInitParamsRequest request) throws ServiceException;
	
	public static class Proxy {
        private static synchronized ServletUtilServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(ServletUtilService.class);
            }
            return null;
        }
	}
	
	public static ServletUtilServiceAsync INSTANCE = Proxy.createInstance();
	
	
	public static class GetServletInitParamsAsync implements AsyncFunction<Object, Map<String, String>> {
		
		private final GetServletInitParamsRequest request;
		
		public GetServletInitParamsAsync(String ...paramNames) {
			this(new GetServletInitParamsRequest(paramNames));
		}
		
		public GetServletInitParamsAsync(GetServletInitParamsRequest request) {
			this.request = request;
		}
		
		@Override
		public void executeAsync(Object param, final SGAsyncCallback<? super Map<String, String>> callback) throws Exception {
			ServletUtilService.INSTANCE.getServletInitParams(request, new AsyncCallback<GetServletInitParamsResponse>() {
				
				@Override
				public void onSuccess(GetServletInitParamsResponse result) {
					callback.onSuccess(result.getParamsMap());
				}
				
				@Override
				public void onFailure(Throwable caught) {
					LoggerFactory.getLogger(getClass()).error("Failed to get servlet init params", caught);
					callback.onFailure(caught);
				}
			});
		}
		
	}
	
}
