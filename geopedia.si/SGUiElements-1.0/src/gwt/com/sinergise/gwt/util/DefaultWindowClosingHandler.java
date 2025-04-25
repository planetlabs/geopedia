package com.sinergise.gwt.util;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;

public class DefaultWindowClosingHandler implements ClosingHandler {
	
	private static DefaultWindowClosingHandler instance = new DefaultWindowClosingHandler();
	
	public static DefaultWindowClosingHandler getInstance() {
		if (instance == null) {
			instance = new DefaultWindowClosingHandler();
		}
		return instance;
	}
	
	public static void initialize() {
		getInstance();
	}
	
	private boolean closing = false;
	
	private DefaultWindowClosingHandler() {
		Window.addWindowClosingHandler(this);
	}

	@Override
	public void onWindowClosing(ClosingEvent event) {
		closing = true;
	}
	
	public boolean isClosing() {
		return closing;
	}
	
	public static boolean isWindowClosing() {
		return getInstance().isClosing();
	}
	
}
