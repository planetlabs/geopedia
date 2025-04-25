package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;

public class ExportStatus implements Serializable{
	private static final long serialVersionUID = -827735404255934517L;

	public static final String STATUS_NOP="nop";
	public static final String STATUS_WORKING="working";
	public static final String STATUS_ERROR="error";
	public static final String STATUS_EXPORTED="exported";
	public static final String STATUS_DOWNLOADED="downloaded";
	
	public String status;
	public long exportedCnt = 0;
	public long totalFeatures;
	public String errorMessage;
	
	public ExportStatus() {
		status = STATUS_NOP;
		totalFeatures=0;
		exportedCnt=0;
		errorMessage=null;
	}
	
	public void setError(String errorMessage) {
		status = STATUS_ERROR;
		this.errorMessage = errorMessage;
	}
	
	public boolean isFinalStatus() {
		if (STATUS_DOWNLOADED.equals(status) ||
			STATUS_ERROR.equals(status))
			return true;
		return false;
	}
	public void setWorking() {
		status = STATUS_WORKING;
		this.errorMessage = null;
	}
	public boolean isWorking() {
		if (STATUS_WORKING.equals(status))
			return true;
		return false;
	}
	
	public boolean isError() {
		if (STATUS_ERROR.equals(status))
			return true;
		return false;
	}
	public void setExported() {
		status = STATUS_EXPORTED;
		this.errorMessage = null;
	}
	public boolean isExported() {
		if (STATUS_EXPORTED.equals(status))
			return true;
		return false;
	}

	public void setDownloaded() {
		status = STATUS_DOWNLOADED;
		this.errorMessage = null;
	}

	public void setNOP() {
		status = STATUS_NOP;
		this.errorMessage = null;
	}
	public boolean isNOP() {
		if (STATUS_NOP.equals(status))
			return true;
		return false;
	}
}
