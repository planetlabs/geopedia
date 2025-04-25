package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.core.service.params.ImportSettings;
import com.sinergise.geopedia.core.service.params.ImportStatus;

public interface ImportServiceAsync {
	public void analyzeUploadedFile(String fileToken,  AsyncCallback<ImportSettings> callback);
	public void doImport(String action, ImportSettings settings, AsyncCallback<ImportStatus> callback);	
}
