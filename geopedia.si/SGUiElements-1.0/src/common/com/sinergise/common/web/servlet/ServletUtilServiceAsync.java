package com.sinergise.common.web.servlet;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServletUtilServiceAsync {

	void getServletInitParams(GetServletInitParamsRequest request, AsyncCallback<GetServletInitParamsResponse> callback);
	
}
