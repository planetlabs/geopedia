package com.sinergise.gwt.ui.gesture.action;

import com.sinergise.gwt.ui.gesture.action.recognizer.PanScaleGestureRecognizer;
import com.sinergise.gwt.ui.gesture.action.recognizer.TouchGestureRecognizer;

/**
 * Action for pan and scale events. Moving single object or moving multiple 
 * objects in parallel to translate or opposite direction to scale. 
 * 
 * @author tcerovski
 */
public abstract class PanScaleGestureAction implements TouchGestureAction {
	
	private final boolean handleSingleObject;
	
	/**
	 * Creates new {@link PanScaleGestureAction} handling panning when two
	 * or more objects are touching the surface.
	 */
	public PanScaleGestureAction() {
		this(true);
	}
	
	/**
	 * Creates new {@link PanScaleGestureAction} that can handle panning gesture with 
	 * single object touching touching the surface.
	 * @param handleSingleObject - specifies if single object panning should be handled. 
	 */
	public PanScaleGestureAction(boolean handleSingleObject) {
		this.handleSingleObject = handleSingleObject;
	}

	public TouchGestureRecognizer createGestureRecognizer() {
		return new PanScaleGestureRecognizer(this);
	}
	
	/**
	 * @return <code>true</code> if single touch object actions should be handled.
	 */
	public boolean handleSingleObjects() {
		return handleSingleObject;
	}
	
	/**
	 * Triggered when starting the gesture.
	 */
	public abstract void onStart();
	
	/**
	 * Triggered on pan and scale events. All values represent changes from gesture start.
	 * @param tx - translation in x direction
	 * @param ty - translation in y direction
	 * @param sc - scale factor. Double.NaN if scale should not be handled.
	 */
	public abstract void onPanAndScale(int tx, int ty, double sc);
	
	/**
	 * Triggered when all objects are lifted from the surface and thus ending the gesture.
	 */
	public abstract void onFinish();

}
