package com.sinergise.gwt.ui.gesture.action.recognizer;

import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.sinergise.gwt.ui.gesture.TouchGestureUtils;
import com.sinergise.gwt.ui.gesture.action.TapGestureAction;

/**
 * Recognizer for {@link TapGestureAction}.
 * 
 * @author tcerovski
 */
public class TapGestureRecognizer implements TouchGestureRecognizer {
	
	private static final double MAX_MOVE_SQDIST_PX = 25;
	private static final int MAX_TAP_DELAY_MS = 1000; 
	
	private final TapGestureAction action;
	
	private Touch startTouch = null;
	private int startX;
	private int startY;
	private long startTimeStamp = 0;
	
	public TapGestureRecognizer(TapGestureAction action) {
		this.action = action;
	}
	
	public boolean isHandling() {
		return startTouch != null;
	}

	public void stopHandling() {
		startTouch = null;
	}

	public void touchStart(TouchStartEvent event) {
		
		//only single tap should be handled - cancel if another tap occurs.
		if (isHandling()) {
			stopHandling();
			
		//one touch only
		} else if (event.getTouches().length() == 1) {
			startTouch = event.getTouches().get(0);
			startX = startTouch.getClientX();
			startY = startTouch.getClientY();
			startTimeStamp = System.currentTimeMillis();
		}
	}

	public void touchMove(TouchMoveEvent event) {
		if (isHandling()) {
			//should still be one touch only - check distance moved
			if (TouchGestureUtils.touchClientDistanceSq(event.getChangedTouches().get(0), startX, startY) > MAX_MOVE_SQDIST_PX) {
				stopHandling();
			}
		}
	}

	public void touchEnd(TouchEndEvent event) {
		if (isHandling()) {
			//should still be one touch only - check time pressed
			if (System.currentTimeMillis() - startTimeStamp < MAX_TAP_DELAY_MS) 
			{
				action.onTap(startTouch.getClientX(), startTouch.getClientY());
			}
			stopHandling();
		}
	}

	public void touchCancel(TouchCancelEvent event) {
		stopHandling();
	}

}
