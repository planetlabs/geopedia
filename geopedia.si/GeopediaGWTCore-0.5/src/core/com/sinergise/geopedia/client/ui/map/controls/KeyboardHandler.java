package com.sinergise.geopedia.client.ui.map.controls;

import java.util.HashMap;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.geom.Envelope;


interface ListenerCollection {
	/**
	 * associate listener with keyCode.
	 */
	public void register(int keyCode, KeyboardHandler.Listener listener);
	/**
	 * remove listener associated with keyCode
	 */
	public void remove(int keyCode);
}

public class KeyboardHandler implements NativePreviewHandler, ListenerCollection {
	
	public interface Listener {
		/**
		 * user pressed the key that is mapped to this listener
		 */
		public void handleKey(int keyCode);
	}
	
	private Widget widget;
	
	private int clientX = 0;
	private int clientY = 0;
	
	/**
	 * true after some listener's handleKey() method has been called.
	 */
	private boolean reacted = false;
	
	public KeyboardHandler(Widget widget) {
		this.widget = widget;
//		DOM.addEventPreview(this);
	}
	
	/**
	 * @return true if mouse was last moved within the boundaries of widget.
	 */
	private boolean within() {
		
		if (widget==null || !widget.isAttached()) {
			return false;
		}
		
		int top = widget.getAbsoluteTop();
		int left = widget.getAbsoluteLeft();
		int right = left + widget.getOffsetWidth();
		int bottom = top + widget.getOffsetHeight();
		
		Envelope mbr = new Envelope(left, top, right, bottom);
		Envelope point = new Envelope(clientX, clientY, clientX, clientY);
		
		return mbr.contains(point);
	}

	
	/** EventPreview implementation */
	@Override
	public void onPreviewNativeEvent(NativePreviewEvent event) {

		Event evt = Event.as(event.getNativeEvent());
		boolean result = true; // return false to cancel the event

		switch (DOM.eventGetType(evt)) {
//			case Event.ONMOUSEMOVE: {
			case Event.ONMOUSEOVER: {
				clientX = evt.getClientX();
				clientY = evt.getClientY();
				break;
			}
			case Event.ONKEYDOWN: {
				if (event.isCanceled() || event.isConsumed())
					return;
				if (within()) {
					int keyCode = DOM.eventGetKeyCode(evt);
					handleKey(keyCode);
				}
				break;
			}
			default: {
				// do nothing
			}
		}
		
		result = !reacted;
		reacted = false;
	}
	
	
	/** ListenerCollection implementation */
	
	private HashMap<Integer, Listener> mappings = new HashMap<Integer, Listener>();
	
	public void register(int keyCode, Listener listener) {
		boolean existing = mappings.containsKey(keyCode);
		if (existing) {
			// TODO:drejmar: should replacing listener be allowed ?
		}
		mappings.put(keyCode, listener);
	}

	public void remove(int keyCode) {
		if (mappings.containsKey(keyCode)) {
			mappings.remove(keyCode);
		}
	}
	
	
	private void handleKey(int keyCode) {
		reacted = false;
		if (mappings.containsKey(keyCode)) {
			Listener listener = mappings.get(keyCode);
			if (listener != null) {
				listener.handleKey(keyCode);
				reacted = true;
			}
		}
	}

	
//	public Integer[] keyCodes() {
//		Set<Integer> keySet = mappings.keySet();
//		return keySet.toArray(new Integer[keySet.size()]);
//	}
}
