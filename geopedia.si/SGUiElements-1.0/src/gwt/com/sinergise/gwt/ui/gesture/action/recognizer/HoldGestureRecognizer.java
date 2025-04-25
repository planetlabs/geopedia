package com.sinergise.gwt.ui.gesture.action.recognizer;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.Timer;
import com.sinergise.gwt.ui.gesture.TouchGestureUtils;
import com.sinergise.gwt.ui.gesture.action.HoldGestureAction;

/**
 * Recognizer for {@link HoldGestureAction}.
 * 
 * @author tcerovski
 */
public class HoldGestureRecognizer implements TouchGestureRecognizer {
	
	private static final double MAX_MOVE_SQDIST_PX = 25;
	
	private final HoldGestureAction action;
	
	private Map<Integer, HoldTimer> delayTimers = new HashMap<Integer, HoldTimer>();
	
	public HoldGestureRecognizer(HoldGestureAction action) {
		this.action = action;
	}

	public void touchStart(TouchStartEvent event) {
		for (int i=0; i<event.getChangedTouches().length(); i++) {
			Touch touch = event.getChangedTouches().get(i);
			delayTimers.put(Integer.valueOf(touch.getIdentifier()), new HoldTimer(touch));
		}
	}

	public void touchMove(TouchMoveEvent event) {
		
		//cancel event if object is moved for more than threshold limit.
		for (int i=0; i<event.getChangedTouches().length(); i++) {
			Touch touch = event.getChangedTouches().get(i);
			HoldTimer timer = delayTimers.get(Integer.valueOf(touch.getIdentifier()));
			if (timer == null) {
				continue;
			}
			
			if (TouchGestureUtils.touchClientDistanceSq(touch, timer.startX, timer.startY) > MAX_MOVE_SQDIST_PX) {
				timer.cancel();
			}
		}
	}

	public void touchEnd(TouchEndEvent event) {
		cancelTouches(event.getChangedTouches());
	}

	public void touchCancel(TouchCancelEvent event) {
		cancelTouches(event.getChangedTouches());
	}
	
	private void cancelTouches(JsArray<Touch> touches) {
		for (int i=0; i<touches.length(); i++) {
			HoldTimer timer = delayTimers.get(Integer.valueOf(touches.get(i).getIdentifier()));
			if (timer != null) {
				timer.cancel();
			}
		}
	}

	public boolean isHandling() {
		return !delayTimers.isEmpty();
	}

	public void stopHandling() {
		for (HoldTimer timer : delayTimers.values()) {
			timer.cancel();
		}

	}
	
	private class HoldTimer extends Timer {
		
		final Integer touchId;
		final int startX;
		final int startY;
		
		HoldTimer(Touch touch) {
			this.startX = touch.getClientX();
			this.startY = touch.getClientY();
			this.touchId = Integer.valueOf(touch.getIdentifier());
			schedule(action.getHoldDelay());
		}
		
		@Override
		public void run() {
			action.onHold(startX, startY);
			cancel();
		}
		
		@Override
		public void cancel() {
			super.cancel();
			delayTimers.remove(touchId);
		}
		
	}

}
