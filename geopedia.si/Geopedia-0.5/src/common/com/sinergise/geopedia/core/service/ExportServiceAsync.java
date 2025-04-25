package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.core.service.params.ExportStatus;

public interface ExportServiceAsync {
	public void doExport(String action, ExportSettings settings,
			AsyncCallback<ExportStatus> callback);

}
