package com.sinergise.gwt.util;

import com.google.gwt.http.client.RequestBuilder;
import com.sinergise.common.util.web.HttpHeaders.HttpHeaderFieldSpec;
import com.sinergise.common.util.web.HttpMethod;

public class SGRequestBuilder extends RequestBuilder {
	public SGRequestBuilder(HttpMethod method, String url) {
		super(WebUtilGWT.toGwtReqBuilderMethod(method), url);
	}
	
	public SGRequestBuilder(Method httpMethod, String url) {
		super(httpMethod, url);
	}
	
	public <T> void setHeader(HttpHeaderFieldSpec<? super T> header, T value) {
		super.setHeader(header.getKeyName(), header.write(value));
	}
}
