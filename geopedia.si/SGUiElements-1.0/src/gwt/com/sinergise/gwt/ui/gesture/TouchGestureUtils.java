package com.sinergise.gwt.ui.gesture;

import com.google.gwt.dom.client.Touch;

/**
 * @author tcerovski
 *
 */
public class TouchGestureUtils {

	public static int touchClientDistanceSq(Touch touch, int x, int y) {
		int dx=(touch.getClientX() - x);
		int dy=(touch.getClientY() - y);
		return dx*dx+dy*dy;
	}
	
}
