package com.sinergise.gwt.ui.gesture.action;

import com.sinergise.gwt.ui.gesture.action.recognizer.HoldGestureRecognizer;
import com.sinergise.gwt.ui.gesture.action.recognizer.TouchGestureRecognizer;

/**
 * Action for simple hold gesture - holding an object on the surface for some time.
 * Each object holding on surface will trigger one event.
 * 
 * @author tcerovski
 */
public abstract class HoldGestureAction implements TouchGestureAction {
	
	private static int DEFAULT_HOLD_DELAY_MS = 2500;
	
	private final int holdDelay;
	
	/**
	 * Creates new {@link HoldGestureAction} with default delay.
	 */
	public HoldGestureAction() {
		this(DEFAULT_HOLD_DELAY_MS);
	}
	
	/**
	 * Creates new {@link HoldGestureAction} with specified delay.
	 * @param holdDelay - Time in milliseconds needed for object to touch 
	 * the surface without moving before onHold is triggered.
	 */
	public HoldGestureAction(int holdDelay) {
		this.holdDelay = holdDelay;
	}
	
	public TouchGestureRecognizer createGestureRecognizer() {
		return new HoldGestureRecognizer(this);
	}

	/**
	 * Triggered when object touches the display for specified delay time.
	 * @param x coordinate of the object hold.
	 * @param y coordinate of the object hold.
	 */
	public abstract void onHold(int x, int y);
	
	/**
	 * @return Hold delay time for this action (in millisecodns).
	 */
	public int getHoldDelay() {
		return holdDelay;
	}
	
}
