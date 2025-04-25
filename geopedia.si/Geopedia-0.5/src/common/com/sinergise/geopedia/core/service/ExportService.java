package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.core.service.params.ExportStatus;

public interface ExportService extends RemoteService {
	public static final String CMD_EXPORT_START="start";
	public static final String CMD_EXPORT_STATUS="status";
	public static final String CMD_EXPORT_CANCEL="cancel";
	public static final String CMD_EXPORT_DOWNLOAD="download";
	
	public static final String SERVICE_URI = "exportService";
	

	public ExportStatus doExport(String action, ExportSettings settings) throws GeopediaException;
}
