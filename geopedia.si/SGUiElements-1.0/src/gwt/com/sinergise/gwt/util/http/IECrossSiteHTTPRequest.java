package com.sinergise.gwt.util.http;

import com.google.gwt.http.client.RequestBuilder.Method;
import com.sinergise.gwt.util.http.xdomainrequest.IECrossSiteRequestBuilder;

public class IECrossSiteHTTPRequest extends CrossSiteHTTPRequest {
	@Override
	protected void initialize(Method httpMethod, String url) {
		rb = new IECrossSiteRequestBuilder(httpMethod,url);
	}
}
