package com.sinergise.common.web.service;

import java.io.Serializable;

import com.sinergise.common.util.web.MimeType;

public class FileExporterRequest implements Serializable {
	private static final long serialVersionUID = -2687887723741026379L;
	
	protected MimeType exportType;
	protected String filename;
	
	public FileExporterRequest() {
		super();
	}

	public FileExporterRequest(MimeType exportType, String filename) {
		this();
		this.exportType = exportType;
		this.filename = filename;
	}

	public MimeType getExportType() {
		return exportType;
	}

	public void setExportType(MimeType exportType) {
		this.exportType = exportType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}