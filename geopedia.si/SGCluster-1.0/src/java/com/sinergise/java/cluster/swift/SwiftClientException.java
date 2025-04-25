package com.sinergise.java.cluster.swift;

import org.apache.http.StatusLine;

public class SwiftClientException extends Exception{
	/**
	 * 
	 */
	public StatusLine statusLine;
	
	private static final long serialVersionUID = 6280136021790304200L;

	
	public static SwiftClientException notLoggedInException() {
		return new SwiftClientException("Not logged in!");
	}
	
	public static SwiftClientException noCredentialsException() {
		return new SwiftClientException("This operation requires account credentials, but there are none!");
	}
	
	public SwiftClientException(String message) {
		super(message);
	
	}

	public SwiftClientException(String message, StatusLine statusLine) {
		super(message);
		this.statusLine=statusLine;
	}


	public SwiftClientException( String message, Throwable cause) {
		super(message,cause);
	}	
	
	
}
