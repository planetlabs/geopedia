package com.sinergise.gwt.util.http.xdomainrequest;

import com.google.gwt.http.client.Request;

public class IECrossSiteRequest extends Request {
	static final int UNSENT = 0;
	static final int OPEN = 1;
	static final int SENT = 2;
	static final int DONE = 3;
	private int _status = UNSENT;
	private XDomainRequest _xhr;

	void setStatus(int status) {
		_status = status;
	}

	public IECrossSiteRequest(XDomainRequest xhr) {
		if (xhr == null) {
			throw new NullPointerException();
		}
		_xhr = xhr;
	}

	@Override
	public void cancel() {
		if (isPending()) {
			_xhr.abort();
		}
	}

	@Override
	public boolean isPending() {
		return (_status == OPEN || _status == SENT);
	}
}