package com.sinergise.gwt.util.http;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

public class CrossSiteHTTPRequest {

	public static CrossSiteHTTPRequest create(Method httpMethod, String url) {
		CrossSiteHTTPRequest req = GWT.create(CrossSiteHTTPRequest.class);
		req.initialize(httpMethod, url);
		return req;
	}
	
	protected CrossSiteHTTPRequest() {		
	}	
	
	protected RequestBuilder rb;
	protected void initialize(Method httpMethod, String url) {
		rb = new RequestBuilder(httpMethod,url);
	}
	public Request sendRequest (String requestData, RequestCallback callback) throws RequestException {
		return rb.sendRequest(requestData, callback);
	}
}
