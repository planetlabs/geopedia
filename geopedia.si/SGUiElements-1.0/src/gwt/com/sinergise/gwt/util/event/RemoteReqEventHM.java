package com.sinergise.gwt.util.event;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Event bus instance for registering and handling all remote request events.
 * 
 * @author tcerovski
 */
public class RemoteReqEventHM extends HandlerManager {

	private static RemoteReqEventHM instance = null;
	
	public static RemoteReqEventHM getInstance() {
		if(instance == null) {
			instance = new RemoteReqEventHM();
		}
		return instance;
	}
	
	private RemoteReqEventHM() {
		super(null);
	}
	
}
