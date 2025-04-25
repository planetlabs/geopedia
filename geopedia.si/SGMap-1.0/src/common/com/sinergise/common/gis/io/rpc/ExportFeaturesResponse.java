package com.sinergise.common.gis.io.rpc;

import com.sinergise.common.web.service.FileExporterResponse;

public class ExportFeaturesResponse extends FileExporterResponse {
	
	private String filename;
	
	@Deprecated /** Serialization only */
	protected ExportFeaturesResponse() {}
	
	public ExportFeaturesResponse(String filename) {
		this.filename = filename;
	}
	
	public String getExportedFileName() {
		return filename;
	}

}
