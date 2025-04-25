package com.sinergise.gwt.util.logging;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class GenericFailureHandler<T> implements AsyncCallback<T> {
	@Override
	public void onFailure(final Throwable arg0) {
		arg0.printStackTrace();
		final String erroMsg = erroMsg(arg0);
		GWT.log(erroMsg, arg0);
	}
	
	protected String erroMsg(final Throwable arg0) {
		final String errorMsg;
		if (arg0 != null) {
			errorMsg = "Error occured: " + arg0.getMessage();
		} else {
			errorMsg = "Unknown Error occured (Throwable not set to instance).";
		}
		return errorMsg;
	}
}
