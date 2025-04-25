package com.sinergise.common.web.session;

import java.io.Serializable;
import java.util.Date;

public class GetSessionActivityResponse implements Serializable {

	private static final long serialVersionUID = -6954881530036256581L;
	
	private String sessionID;
	private int maxInactiveInterval;
	private Date creationTime;
	private Date lastAccessedTime;
	private int inactivityTime;
	private boolean expired;
	
	@Deprecated /**Serialization only */
	protected GetSessionActivityResponse() { }
	
	public GetSessionActivityResponse(String sessionID, boolean expired) {
		this.sessionID = sessionID;
		this.expired = expired;
	}
	
	public String getSessionID() {
		return sessionID;
	}
	
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}
	
	/**
	 * @return an integer specifying the number of seconds this session remains open between client requests
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}
	
	public void setLastAccessedTime(Date lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
		this.inactivityTime = (int)(new Date().getTime() - lastAccessedTime.getTime())/1000;
	}
	
	/**
	 * @return a Date representing the last time the client sent a request associated with this session
	 */
	public Date getLastAccessedTime() {
		return lastAccessedTime;
	}
	
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	
	/**
	 * @return a Date specifying when this session was created
	 */
	public Date getCreationTime() {
		return creationTime;
	}
	
	/**
	 * @return an integer specifying the number of seconds since last client request
	 */
	public int getInactivityTime() {
		return inactivityTime;
	}
	
	/**
	 * @return an integer specifying the number of seconds to session expiration if no client requests will be made
	 */
	public int getTimeToExpiration() {
		return getMaxInactiveInterval() - getInactivityTime();
	}
	
	/**
	 * @return <code>true</code> if the server session has expired
	 */
	public boolean isExpired() {
		return expired;
	}
	
}
