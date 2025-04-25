package com.sinergise.geopedia.core.common;

import java.io.Serializable;

public class HasSession<T> implements Serializable{
	private static final long serialVersionUID = 1676738270801918174L;
	private String session;
	private T extraData;
	
	public String getSession() {
		return session;
	}
	public T getExtraData() {
		return extraData;
	}
	
	public HasSession(String session, T extraData) {
		this.session = session;
		this.extraData = extraData;
	}
	protected HasSession() {		
	}
	public HasSession(String session) {
		this.session = session;
		this.extraData = null;
	}
}
