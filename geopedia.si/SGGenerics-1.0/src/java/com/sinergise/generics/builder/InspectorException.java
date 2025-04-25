package com.sinergise.generics.builder;

public class InspectorException extends RuntimeException{

	public InspectorException(String message) {
		super(message);
	}

	public InspectorException(Exception ex) {
		super(ex);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6154992896607763259L;

}
