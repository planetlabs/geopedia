package com.sinergise.gwt.ui.gesture;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.HasAllTouchHandlers;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.gesture.action.TouchGestureAction;
import com.sinergise.gwt.ui.gesture.action.recognizer.TouchGestureRecognizer;

/**
 * @author tcerovski
 *
 */
public class TouchGestureHandler implements TouchStartHandler, TouchMoveHandler, TouchEndHandler, TouchCancelHandler {

	private Map<TouchGestureAction, TouchGestureRecognizer> gestureRecognizers = new HashMap<TouchGestureAction, TouchGestureRecognizer>();
	
	public TouchGestureHandler(HasAllTouchHandlers component) {
		addHandlers(component);
	}
	
	public TouchGestureHandler(final Widget w) {
		if (w.isAttached()) {
			addHandlers(w);
		} else {
			w.addAttachHandler(new Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					addHandlers(w);
				}
			});
		}
	}
	
	private void addHandlers(HasAllTouchHandlers component) {
		component.addTouchStartHandler(this);
		component.addTouchMoveHandler(this);
		component.addTouchEndHandler(this);
		component.addTouchCancelHandler(this);
	}
	
	private void addHandlers(Widget w) {
		w.addDomHandler(this, TouchCancelEvent.getType());
		w.addDomHandler(this, TouchEndEvent.getType());
		w.addDomHandler(this, TouchMoveEvent.getType());
		w.addDomHandler(this, TouchStartEvent.getType());
	}
	
	public void registerAction(TouchGestureAction action) {
		//each handler should create its own recognizer
		gestureRecognizers.put(action, action.createGestureRecognizer());
	}
	
	public void deregisterAction(TouchGestureAction action) {
		TouchGestureRecognizer recognizer = gestureRecognizers.remove(action);
		if (recognizer != null && recognizer.isHandling()) {
			recognizer.stopHandling();
		}
	}
	
	public void onTouchStart(TouchStartEvent event) {
		handleEvent(event);
	}
	
	public void onTouchMove(TouchMoveEvent event) {
		handleEvent(event);
	}
	
	public void onTouchEnd(TouchEndEvent event) {
		handleEvent(event);
	}
	
	public void onTouchCancel(TouchCancelEvent event) {
		handleEvent(event);
	}
	
	private void handleEvent(TouchEvent<?> event) {
		boolean isAnyoneHandling = false;
		for (TouchGestureRecognizer recognizer : gestureRecognizers.values()) {
			if (event instanceof TouchStartEvent) {
				recognizer.touchStart((TouchStartEvent)event);
			} else if (event instanceof TouchMoveEvent) {
				recognizer.touchMove((TouchMoveEvent)event);
			} else if (event instanceof TouchEndEvent) {
				recognizer.touchEnd((TouchEndEvent)event);
			} else if (event instanceof TouchCancelEvent) {
				recognizer.touchCancel((TouchCancelEvent)event); 
			}
			
			isAnyoneHandling |= recognizer.isHandling();
		}
		
		if (isAnyoneHandling) {
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	
}
