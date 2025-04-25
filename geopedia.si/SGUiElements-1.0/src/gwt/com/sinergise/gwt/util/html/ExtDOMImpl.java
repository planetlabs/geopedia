/*
 * Copyright 2006 Robert Hanson <iamroberthanson AT gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sinergise.gwt.util.html;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.util.web.Namespace;

public class ExtDOMImpl {
	
	public ExtDOMImpl() {
		init();
	}
	
	public Element createElementNS(final Namespace ns, final String tag) {
		return createElementNS(ns.getUri(), tag);
	}
	
	private native Element createElementNS(String ns, String tag) /*-{
	                                                              return $doc.createElementNS(ns, tag);
	                                                              }-*/;
	
	/**
	 * Set a namespace attr, using the namespace of the element.
	 */
	public native void setAttributeNS(Element elem, String attr, String value) /*-{
	                                                                           elem.setAttributeNS(null, attr, value);
	                                                                           }-*/;
	
	public void setAttributeNS(final Namespace ns, final Element elem, final String attr, final String value) {
		setAttributeNS(ns.getUri(), elem, attr, value);
	}
	
	private native void setAttributeNS(String uri, Element elem, String attr, String value) /*-{
	                                                                                        elem.setAttributeNS(uri, attr, value);
	                                                                                        }-*/;
	
	public native int eventGetPageX(NativeEvent event) /*-{
		if (event.pageX) {
			return event.pageX;
		}
		return event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
	}-*/;
	
	public native int eventGetPageY(NativeEvent event) /*-{
		if (event.pageY) {
			return event.pageY;
		}
		return event.clientY + document.body.scrollTop + document.documentElement.scrollTop;
	}-*/;
	
	public Element createEventCatcher() {
		final Element el = DOM.createDiv();
		DOM.setInnerHTML(el, "&nbsp;");
		return el;
	}
	
	// public native void sinkWheelEvents(Element el) /*-{
	// // DOMMouseScroll is for mozilla.
	// if (el.addEventListener) el.addEventListener('DOMMouseScroll', $wnd.__dispatchEvent, false);
	//        
	// // IE/Opera.
	// el.onmousewheel = $wnd.__dispatchEvent;
	// }-*/;
	//    
	
	public void posMouseCatcher(final Element dragEl, final int x, final int y) {
		DOM.setStyleAttribute(dragEl, "position", "relative");
		DOM.setStyleAttribute(dragEl, "left", x + "px");
		DOM.setStyleAttribute(dragEl, "top", y + "px");
	}
	
	public void init() {
	// initWheelHandler();
	}
	
	// private native void initWheelHandler() /*-{
	// // Initialization code.
	// // If you use your own event management code, change it as required.
	// //
	// // DOMMouseScroll is for mozilla.
	// if ($wnd.addEventListener) $wnd.addEventListener('DOMMouseScroll', $wnd.__dispatchCapturedEvent, false);
	//        
	// // IE/Opera.
	// $wnd.onmousewheel = $doc.onmousewheel = $wnd.__dispatchEvent;
	// }-*/;
	
	// /**
	// * Basically, delta is positive if wheel was scrolled up,
	// * and negative, if wheel was scrolled down.
	// * @param event
	// */
	// public native int eventGetWheelDelta(Event event) /*-{
	// // Event handler for mouse wheel event.
	// var delta = 0;
	// if (event.wheelDelta) { // IE/Opera.
	//                 
	// delta = event.wheelDelta/120;
	//    
	// // In Opera 9, delta differs in sign as compared to IE.
	// if ($wnd.opera) delta = -delta;
	//                 
	// } else if (event.detail) { // Mozilla case.
	// // In Mozilla, sign of delta is different than in IE.
	// // Also, delta is multiple of 3.
	//            
	// delta = -event.detail/3;
	// }
	// return delta;
	// }-*/;
	//
	// public int eventGetWheelX(int eventX, int moveX) {
	// return moveX; // Mozilla bug
	// }
	// public int eventGetWheelY(int eventY, int moveY) {
	// return moveY; // Mozilla bug
	// }
}
