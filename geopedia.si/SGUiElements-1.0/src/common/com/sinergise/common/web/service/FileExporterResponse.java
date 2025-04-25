package com.sinergise.common.web.service;

import java.io.Serializable;

public class FileExporterResponse implements Serializable {
	private static final long serialVersionUID = 1670517358010023715L;
	
	protected String queryString;

	public FileExporterResponse() {
		super();
	}
	
	public FileExporterResponse(String queryString) {
		super();
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	
}