package com.sinergise.gwt.ui.gesture.action;

import com.sinergise.gwt.ui.gesture.action.recognizer.TapGestureRecognizer;
import com.sinergise.gwt.ui.gesture.action.recognizer.TouchGestureRecognizer;

/**
 * Action for simple tap gesture - briefly touching the surface with only one object.<br/>
 * Equals mouse click event.
 * 
 * @author tcerovski
 */
public abstract class TapGestureAction implements TouchGestureAction {

	public TouchGestureRecognizer createGestureRecognizer() {
		return new TapGestureRecognizer(this);
	}

	/**
	 * Triggered when object taps the display.
	 * @param x coordinate of the object tapped.
	 * @param y coordinate of the object tapped.
	 */
	public abstract void onTap(int x, int y);
	
}
