package com.sinergise.common.gis.feature;

public class FeatureAccessException extends Exception {

	private static final long serialVersionUID = 1L;

	public FeatureAccessException() {
		super();
	}

	public FeatureAccessException(String msg) {
		super(msg);
	}

	public FeatureAccessException(String msg, Throwable cause) {
		super(msg);
		if (cause!=null) cause.printStackTrace();
	}
}
