package com.sinergise.gwt.util.event.bus;

/**
 * Application level event bus to be used for handling application-scoped events.
 * 
 * @see SGEventBus
 * Should be used only with application-scoped event sources and handlers.
 *
 */
public final class AppEventBus extends SGEventBus {
	
	private static final AppEventBus INSTANCE = new AppEventBus();
	
	public static AppEventBus getInstance() {
		return INSTANCE;
	}
	
	private AppEventBus() {
		// hide constructor
	}

}
