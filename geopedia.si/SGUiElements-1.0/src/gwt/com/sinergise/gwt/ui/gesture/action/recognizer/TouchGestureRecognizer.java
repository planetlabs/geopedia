package com.sinergise.gwt.ui.gesture.action.recognizer;

import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;

/**
 * Interface for gesture recognizers that listen for touch events and trigger appropriate gesture actions.
 * 
 * @author tcerovski
 */
public interface TouchGestureRecognizer {

	void touchStart(TouchStartEvent event);
	void touchMove(TouchMoveEvent event);
	void touchEnd(TouchEndEvent event);
	void touchCancel(TouchCancelEvent event);

	/**
	 * Checks if the recognizer is handling a gesture, i.e. when a gesture might be triggered or is already started.
	 * @return <code>true</code> if the recognizer is handling a gesture, <code>false</code> otherwise.
	 */
	boolean isHandling();
	
	/**
	 * Stops handling of recognizers gesture.
	 */
	void stopHandling();
}
