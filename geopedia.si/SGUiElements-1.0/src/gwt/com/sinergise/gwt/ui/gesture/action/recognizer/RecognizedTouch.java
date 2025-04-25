package com.sinergise.gwt.ui.gesture.action.recognizer;

import com.google.gwt.dom.client.Touch;

/**
 * @author tcerovski
 *
 */
public class RecognizedTouch {

	private final Integer touchId;
	private int startX;
	private int startY;
	private int lastX;
	private int lastY;
	
	public RecognizedTouch(Touch touch) {
		touchId = Integer.valueOf(touch.getIdentifier());
		startX = touch.getClientX();
		startY = touch.getClientY();
		update(touch);
	}
	
	public void update(Touch touch) {
		if (touchId.equals(Integer.valueOf(touch.getIdentifier()))) {
			lastX = touch.getClientX();
			lastY = touch.getClientY(); 
		}
	}
	
	public void setStart(int startX, int startY) {
		this.startX = startX;
		this.startY = startY;
	}
	
	public int lastX() {
		return lastX;
	}
	
	public int lastY() {
		return lastY;
	}
	
	public int startX() {
		return startX;
	}
	
	public int startY() {
		return startY;
	}
	
	public Integer identifier() {
		return touchId;
	}
	
}
