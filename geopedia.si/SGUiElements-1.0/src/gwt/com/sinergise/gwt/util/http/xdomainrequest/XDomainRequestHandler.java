package com.sinergise.gwt.util.http.xdomainrequest;

public interface XDomainRequestHandler {
	public void onLoad(XDomainRequest req);
	public void onProgress(XDomainRequest req);
	public void onError(XDomainRequest req);
	public void onTimeout(XDomainRequest req);
}
