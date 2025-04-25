package com.sinergise.gwt.util.http.xdomainrequest;

import com.google.gwt.core.client.JavaScriptObject;

public final class XDomainRequest extends JavaScriptObject {
	public static native XDomainRequest create() /*-{
		var me = new Object();
		me.xdr = new $wnd.XDomainRequest();
		return me;
	}-*/;

	protected XDomainRequest() {}

	public final native void setTimeout(int value) /*-{
		this.xdr.timeout = value;
	}-*/;

	public final native void abort() /*-{
		this.xdr.abort();
	}-*/;

	public final native void clear() /*-{
		var self = this;
		$wnd.setTimeout(function() {
			self.xdr.onload = new Function();
			self.xdr.onprogress = new Function();
			self.xdr.onerror = new Function();
			self.xdr.ontimeout = new Function();
		}, 0);
	}-*/;

	public final native String getContentType() /*-{
		return this.xdr.contentType;
	}-*/;

	public final native int getTimeout() /*-{
		return this.xdr.timeout;
	}-*/;

	public final native String getResponseText() /*-{
		return this.xdr.responseText;
	}-*/;

	public final native void open(String httpMethod, String url) /*-{
		this.xdr.open(httpMethod, url, true);
	}-*/;

	public final native void send(String requestData) /*-{
		this.xdr.send(requestData);
	}-*/;

	public final native void setHandler(XDomainRequestHandler handler) /*-{
		var _this = this;
		this.xdr.onload = function() {
			handler.@com.sinergise.gwt.util.http.xdomainrequest.XDomainRequestHandler::onLoad(Lcom/sinergise/gwt/util/http/xdomainrequest/XDomainRequest;)(_this);
		};
		this.xdr.onerror = function() {
			handler.@com.sinergise.gwt.util.http.xdomainrequest.XDomainRequestHandler::onLoad(Lcom/sinergise/gwt/util/http/xdomainrequest/XDomainRequest;)(_this);
		};
		this.xdr.ontimeout = function() {
			handler.@com.sinergise.gwt.util.http.xdomainrequest.XDomainRequestHandler::onTimeout(Lcom/sinergise/gwt/util/http/xdomainrequest/XDomainRequest;)(_this);
		};
		this.xdr.onprogress = function() {
			handler.@com.sinergise.gwt.util.http.xdomainrequest.XDomainRequestHandler::onProgress(Lcom/sinergise/gwt/util/http/xdomainrequest/XDomainRequest;)(_this);
		};
	}-*/;
}