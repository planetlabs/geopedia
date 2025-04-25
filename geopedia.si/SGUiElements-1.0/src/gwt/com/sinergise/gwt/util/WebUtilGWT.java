package com.sinergise.gwt.util;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.sinergise.common.util.web.HttpMethod;

public class WebUtilGWT {
	public static Method toGwtReqBuilderMethod(HttpMethod sgMethod) {
		switch (sgMethod) {
			case GET: return RequestBuilder.GET;
			case HEAD: return RequestBuilder.HEAD;
			case PUT: return RequestBuilder.PUT;
			case POST: return RequestBuilder.POST;
			case DELETE: return RequestBuilder.DELETE;
		}
		throw new IllegalArgumentException("Unknown HttpMethod: "+sgMethod);
	}
}
