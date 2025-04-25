package com.sinergise.generics.gwt.core;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.EntityObject;

public interface IsFilterable {
	void onStateChanged(EntityObject entityObject, AsyncCallback<Void> callback);
}
