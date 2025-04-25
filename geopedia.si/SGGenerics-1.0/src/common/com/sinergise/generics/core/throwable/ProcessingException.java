package com.sinergise.generics.core.throwable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProcessingException extends Exception implements IsSerializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4153085517671450486L;

	private String causeType;
	private String causeMessage;
	
	/*Serialization garbage*/
	protected ProcessingException() {
		super();
	}
	
	public ProcessingException (String message, Throwable cause) {
		super(message,cause);
		if (cause != null) {
			this.causeType       = cause.getClass().toString();
			this.causeMessage    = cause.getMessage();
		}
	}

	public String getCauseType() {
		return causeType;
	}

	public String getCauseMessage() {
		return causeMessage;
	}
	
}
