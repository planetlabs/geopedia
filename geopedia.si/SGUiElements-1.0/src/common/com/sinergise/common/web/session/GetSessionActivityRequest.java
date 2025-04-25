package com.sinergise.common.web.session;

import java.io.Serializable;

public class GetSessionActivityRequest implements Serializable {

	private static final long serialVersionUID = 8503818817376722889L;
	
	private String sessionID;
	
	@Deprecated /** Serialization only */
	protected GetSessionActivityRequest() {}
	
	public GetSessionActivityRequest(String sessionID) {
		this.sessionID = sessionID;
	}
	
	public String getSessionID() {
		return sessionID;
	}
	
}
