package com.sinergise.common.ui.upload;

public class UploadException extends Exception {
	private static final long serialVersionUID = -6740385310160203170L;

	UploadItem failedItem = null;
	public UploadException() {
		super();
	}

	public UploadException(String message, Throwable cause) {
		super(message, cause);
	}

	public UploadException(String message) {
		super(message);
	}

	public UploadException(Throwable cause) {
		super(cause);
	}
	
	public UploadException(String msg, Throwable cause, UploadItem itm) {
		this(msg, cause);
		failedItem = itm;
	}

	public void setFailedItem(UploadItem failedItem) {
		this.failedItem = failedItem;
	}
	
	public UploadItem getFailedItem() {
		return failedItem;
	}
}
