package com.sinergise.geopedia.core.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.params.ImportSettings;
import com.sinergise.geopedia.core.service.params.ImportStatus;

public interface ImportService extends RemoteService {
	public static final String CMD_IMPORT_START="start";
	public static final String CMD_IMPORT_STATUS="status";
	public static final String CMD_IMPORT_CANCEL="cancel";
	
	public static final String SERVICE_URI = "importService";

	public ImportSettings analyzeUploadedFile(String fileToken) throws GeopediaException;
	public ImportStatus doImport(String action, ImportSettings settings) throws UpdateException;
}