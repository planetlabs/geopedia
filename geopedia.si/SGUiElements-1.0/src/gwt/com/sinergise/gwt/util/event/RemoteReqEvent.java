package com.sinergise.gwt.util.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that should be fired when long remote requests are started and finished.
 * 
 * @author tcerovski
 */
public class RemoteReqEvent extends GwtEvent<RemoteReqEventHandler> {
	
	public static final Type<RemoteReqEventHandler> TYPE = new Type<RemoteReqEventHandler>();
	
	private boolean finished;
	private final String eventCode;
	
	public RemoteReqEvent() {
		this(null);
	}
	
	public RemoteReqEvent(String eventCode) {
		this(eventCode, false);
	}
	
	
	public RemoteReqEvent(String eventCode, boolean finished) {
		this.finished = finished;
		this.eventCode = eventCode;
	}
	
	@Override
	public Type<RemoteReqEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(RemoteReqEventHandler handler) {
		if(finished) {
			handler.remoteRequestFinished(this);
		} else {
			handler.remoteRequestStarted(this);
		}
	}
	
	public String getCode() {
		return eventCode;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public RemoteReqEvent setFinished() {
		finished = true;
		return this;
	}

}
