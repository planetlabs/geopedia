package com.sinergise.gwt.ui.gesture.action;

import com.sinergise.gwt.ui.gesture.action.recognizer.TouchGestureRecognizer;

/**
 * Interface for (touch) gesture actions.
 * 
 * @author tcerovski
 */
public interface TouchGestureAction {

	/**
	 * Creates a gesture recognizer instance for this action.
	 * @return New gesture recognizer for this action.
	 */
	TouchGestureRecognizer createGestureRecognizer();
	
}
