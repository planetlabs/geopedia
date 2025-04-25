package com.sinergise.common.web.session;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JOSSOSessionServiceAsync extends SessionServiceAsync {

	void getJOSSOSessionActivity(GetSessionActivityRequest request, AsyncCallback<GetSessionActivityResponse> callback);
}
