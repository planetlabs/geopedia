package com.sinergise.common.web.i18n;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DictionaryServiceAsync {

	void getDictionary(GetDictionaryRequest request, AsyncCallback<GetDictionaryResponse> callback);
	
}
