package com.sinergise.gwt.util.event;

import com.google.gwt.event.shared.EventHandler;


/**
 * Interface for {@link RemoteReqEvent} handlers.
 * 
 * @author tcerovski
 */
public interface RemoteReqEventHandler extends EventHandler {

	/**
	 * Called when RemoteReqEvent.Started is fired on the handler manager.
	 */
	public void remoteRequestStarted(RemoteReqEvent event);
	
	/**
	 * Called when RemoteReqEvent.Finished is fired on the handler manager.
	 */
	public void remoteRequestFinished(RemoteReqEvent event);
	 
}
