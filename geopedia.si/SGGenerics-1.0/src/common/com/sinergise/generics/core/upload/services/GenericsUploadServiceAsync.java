package com.sinergise.generics.core.upload.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.upload.entities.GenericDocument;

public interface GenericsUploadServiceAsync {
	void saveGenericDocument(GenericDocument doc, AsyncCallback<GenericDocument> callback);
	void loadGenericDocument(Integer docId, boolean loadFileData, AsyncCallback<GenericDocument> callback);
}
