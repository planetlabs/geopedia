/*
 *
 */
package com.sinergise.common.gis.ogc;


public class OGCException extends Exception {
	private OGCRequest request;
	private int httpCode=500;

	private transient Throwable myCause = this;

	public OGCException() {
	}

	public OGCException(OGCRequest request, String message) {
		super(message);
		this.request = request;
	}

	public OGCException(OGCRequest request, String message, Throwable cause) {
		super(message);
		initCause(cause);
		this.request = request;
	}

	/**
	 * Overriden to support cause
	 */
	@Override
	public Throwable initCause(Throwable cause) {
		if (this.myCause != this)
			throw new IllegalStateException("Can't overwrite cause");
		if (cause == this)
			throw new IllegalArgumentException("Self-causation not permitted");
		this.myCause = cause;
		if (cause!=null && cause.toString().indexOf("FileNotFound")>=0) {
			httpCode=404;// NOT FOUND
		}
		return this;
	}

	@Override
	public Throwable getCause() {
		return myCause == this ? null : myCause;
	}

	public OGCRequest getRequest() {
		return request;
	}

	public int getHttpCode() {
		return httpCode;
	}
	
	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}
}
