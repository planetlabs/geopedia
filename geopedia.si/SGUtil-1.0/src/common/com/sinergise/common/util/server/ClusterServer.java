package com.sinergise.common.util.server;

import java.io.Serializable;
import java.util.Date;

import com.sinergise.common.util.naming.Identifier;

public abstract class ClusterServer implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public enum Status { ONLINE, OFFLINE}
	protected Identifier identifier;
	private String serverURL;
	protected Date lastStatusChangeTS;	
	protected Status status = Status.ONLINE;
	
	
	public ClusterServer (Identifier identifier, String serverURL) {
		this.identifier = identifier;
		this.serverURL=serverURL;
	}

	/** Serialization only **/
	@Deprecated
	protected ClusterServer() {
	}
	
	public String getURL() {
		return serverURL;
	}
	
	public abstract String getValidationRequestURL();
	public abstract Status validateValidationResponse(String response);
	
	public void setStatus(Status status) {
		this.status = status;
		this.lastStatusChangeTS = new Date();
	}
	
	public boolean isOnline() {
		return status == Status.ONLINE;
	}

	public Identifier getIdentifier() {
		return identifier;
	}
}
