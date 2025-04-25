package com.sinergise.gwt.ui.gesture.action.recognizer;

import static com.sinergise.common.util.math.MathUtil.distSq;
import static java.lang.Math.sqrt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.sinergise.gwt.ui.core.TimerExt;
import com.sinergise.gwt.ui.gesture.action.PanScaleGestureAction;

/**
 * Recognizer for {@link PanScaleGestureActionOld}.
 * 
 * @author tcerovski
 */
public class PanScaleGestureRecognizer implements TouchGestureRecognizer {
	
	private static final int MIN_SCALE_DIST = 60;
	private static final int MIN_TRANSLATION = 5;
	
	private final PanScaleGestureAction action;
	
	private boolean handled = false;
	private Map<Integer, RecognizedTouch> registeredTouches = new LinkedHashMap<Integer, RecognizedTouch>();
	
	private final TimerExt handleTimer = new TimerExt(100, false) {
        @Override
		public void execute() {
        	handleTouchMove();
        }
    };
	
	public PanScaleGestureRecognizer(PanScaleGestureAction action) {
		this.action = action;
	}

	public void touchStart(TouchStartEvent event) {
		registerTouches(event.getChangedTouches());
	}

	public void touchMove(TouchMoveEvent event) {
		for (int i=0; i<event.getTouches().length(); i++) {
			Touch touch = event.getTouches().get(i);
			RecognizedTouch regTouch = registeredTouches.get(Integer.valueOf(touch.getIdentifier()));
			if (regTouch != null) {
				regTouch.update(touch);
			}
		}
		handleTimer.schedule();
	}
	
	private void handleTouchMove() {
		//use first two touches only (or one if translating)
		Iterator<RecognizedTouch> touchIter = registeredTouches.values().iterator();
		RecognizedTouch t1 = touchIter.next();
		RecognizedTouch t2 = registeredTouches.size() > 1 ? touchIter.next() : t1;
		
		//can't find matching touches
		if (t1 == null || t2 == null) {
			stopHandling();
			return;
		}
		
		handleTransformation(t1.startX(), t1.startY(), t2.startX(), t2.startY(), 
					t1.lastX(), t1.lastY(), t2.lastX(), t2.lastY());
	}
	
	private void handleTransformation(int s1x, int s1y, int s2x, int s2y, 
										 int e1x, int e1y, int e2x, int e2y) 
	{
		double sd = sqrt(distSq(s1x, s1y, s2x, s2y));
		double ed = sqrt(distSq(e1x, e1y, e2x, e2y));
		
		double sc = 1;
		//prevent division by 0 and scaling when doing two (close) finger panning
		if (sd != 0 && (ed > MIN_SCALE_DIST && sd > MIN_SCALE_DIST)) { 
			sc = ed/sd;
		}
		
		int tx = (e1x-s1x + e2x-s2x)/2;
		int ty = (e1y-s1y + e2y-s2y)/2;
		
		if (sc == 1 && Math.abs(tx) < MIN_TRANSLATION && Math.abs(ty) < MIN_TRANSLATION) {
			return;
		}
		
		if (!handled) {
			handled = true;
			action.onStart();
		}
		
		action.onPanAndScale(tx, ty, sc);
	}

	public void touchEnd(TouchEndEvent event) {
		cancelTouches(event.getChangedTouches());
	}

	public void touchCancel(TouchCancelEvent event) {
		cancelTouches(event.getChangedTouches());
	}
	
	private void registerTouches(JsArray<Touch> touches) {
		for (int i=0; i<touches.length(); i++) {
			Touch touch = touches.get(i);
			registeredTouches.put(Integer.valueOf(touch.getIdentifier()), new RecognizedTouch(touch));
		}
	}
	
	private void cancelTouches(JsArray<Touch> touches) {
		//Fire the timer in case we filtered something
		handleTimer.execute();
		handleTimer.cancel();
		for (int i=0; i<touches.length(); i++) {
			registeredTouches.remove(Integer.valueOf(touches.get(i).getIdentifier()));
		}
		
		if (isHandling()) {
			reset();
		} else if (handled) {
			stopHandling();
			action.onFinish();
		}
	}

	public boolean isHandling() {
		if (registeredTouches.size() > 1) {
			return handled;
		} else if (registeredTouches.size() > 0 && action.handleSingleObjects()) {
			return handled;
		}
		return false;
	}

	public void stopHandling() {
		registeredTouches.clear();
		handled = false;
	}
	
	private void reset() {
		for (RecognizedTouch touch : registeredTouches.values()) {
			touch.setStart(touch.lastX(), touch.lastY());
		}
		action.onStart();
	}
	
}
