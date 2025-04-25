package com.sinergise.common.web.session;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SessionServiceAsync {

	void setSessionValues(SetSessionValuesRequest request, AsyncCallback<SetSessionValuesResponse> callback);
	
	void getSessionValues(GetSessionValuesRequest request, AsyncCallback<GetSessionValuesResponse> callback);
	
	void sendHeartbeatMsg(SendHeartbeatMsgRequest request, AsyncCallback<SendHeartbeatMsgResponse> callback);
	
	void getSessionActivity(GetSessionActivityRequest request, AsyncCallback<GetSessionActivityResponse> callback);
}
