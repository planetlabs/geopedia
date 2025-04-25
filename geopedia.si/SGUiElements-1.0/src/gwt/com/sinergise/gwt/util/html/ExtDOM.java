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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.common.util.web.Namespace;

public class ExtDOM extends DOM {
	private static ExtDOMImpl impl;
	static {
		impl = (ExtDOMImpl)GWT.create(ExtDOMImpl.class);
	}
	
	public static Element createElementNS(final Namespace ns, final String tag) {
		return impl.createElementNS(ns, tag);
	}
	
	public static int setOffsetWidth(final Element el, final int w) {
		int tempW = w;
		int delta = 0;
		do {
			DOM.setStyleAttribute(el, "width", tempW + "px");
			delta = DOM.getElementPropertyInt(el, "offsetWidth") - w;
			tempW = w - delta;
		} while (delta > 0);
		return tempW;
	}
	
	public static int setOffsetHeight(final Element el, final int h) {
		int tempH = h;
		int delta = 0;
		do {
			DOM.setStyleAttribute(el, "height", tempH + "px");
			delta = DOM.getElementPropertyInt(el, "offsetHeight") - h;
			tempH = h - delta;
		} while (delta > 0);
		return tempH;
	}
	
	public static int setOffsetLeft(final Element el, final int l) {
		int tempL = l;
		int delta = 0;
		do {
			DOM.setStyleAttribute(el, "left", tempL + "px");
			delta = getOffsetLeft(el) - l;
			tempL = l - delta;
		} while (delta > 0);
		return tempL;
	}
	
	public static int setOffsetTop(final Element el, final int t) {
		int tempT = t;
		int delta = 0;
		do {
			DOM.setStyleAttribute(el, "top", tempT + "px");
			delta = getOffsetTop(el) - t;
			tempT = t - delta;
		} while (delta > 0);
		return tempT;
	}
	
	public static int getOffsetLeft(final Element el) {
		final int left = DOM.getAbsoluteLeft(el);
		final Element parent = DOM.getParent(el);
		if (parent == null) {
			return left;
		}
		final int pLeft = DOM.getAbsoluteLeft(parent);
		final int pScr = DOM.getElementPropertyInt(parent, "scrollLeft");
		if (pScr > 0) {
			return left + pScr - pLeft;
		}
		return left - pLeft;
	}
	
	public static int getOffsetTop(final Element el) {
		final int top = DOM.getAbsoluteTop(el);
		final Element parent = DOM.getParent(el);
		if (parent == null) {
			return top;
		}
		final int pTop = DOM.getAbsoluteTop(parent);
		final int pScr = DOM.getElementPropertyInt(parent, "scrollTop");
		if (pScr > 0) {
			return top + pScr - pTop;
		}
		return top - pTop;
	}
	
	/**
	 * Set a namespace attr, using the namespace of the element.
	 */
	public static void setAttributeNS(final Element elem, final String attr, final String value) {
		impl.setAttributeNS(elem, attr, value);
	}
	
	/**
	 * Set a namespace attr, using the namespace of the element.
	 */
	public static void setIntAttributeNS(final Element elem, final String attr, final int value) {
		setAttributeNS(elem, attr, Integer.toString(value));
	}
	
	public static void setAttributeNS(final Namespace ns, final Element elem, final String attr, final String value) {
		impl.setAttributeNS(ns, elem, attr, value);
	}
	
	public static boolean isVisible(final Element elem) {
		return UIObject.isVisible(elem);
	}
	
	public static void setVisible(final Element elem, final boolean visible) {
		UIObject.setVisible(elem, visible);
	}
	
	public static void setZIndex(final Element elem, final int zIndex) {
		DOM.setIntStyleAttribute(elem, "zIndex", zIndex);
	}
	
	public static void position(final Element elem, final String pos) {
		setStyleAttribute(elem, "position", pos);
	}
	
	public static void overflow(final Element elem, final String overflow) {
		setStyleAttribute(elem, "overflow", overflow);
	}
	
	public static void posAbs(final Element elem, final int left, final int top) {
		position(elem, "absolute");
		setLeft(elem, left);
		setTop(elem, top);
	}
	
	public static void setPosition(final Element elem, final int left, final int top) {
		setLeft(elem, left);
		setTop(elem, top);
	}
	
	public static void posAbsSize(final Element elem, final int left, final int top, final int w, final int h) {
		CSS.position(elem, CSS.POS_ABSOLUTE);
		setLeft(elem, left);
		setTop(elem, top);
		setWidth(elem, w);
		setHeight(elem, h);
	}
	
	public static void setTop(final Element elem, final int t) {
		setStyleAttribute(elem, "top", t + "px");
	}
	
	public static void setLeft(final Element elem, final int l) {
		setStyleAttribute(elem, "left", l + "px");
	}
	
	public static void setSize(final Element elem, final int w, final int h) {
		setWidth(elem, w);
		setHeight(elem, h);
	}
	
	public static void setHeight(final Element elem, final int h) {
		if (h >= 0) {
			setStyleAttribute(elem, "height", h + "px");
		}
	}
	
	public static void setWidth(final Element elem, final int w) {
		if (w >= 0) {
			setStyleAttribute(elem, "width", w + "px");
		}
	}
	
	public static void display(final Element elem, final String display) {
		setStyleAttribute(elem, "display", display);
	}
	
	public static native void setStatusBarText(String text) /*-{
	                                                        $wnd.status=text;
	                                                        }-*/;
	
	public static int eventGetPageX(final NativeEvent event) {
		return impl.eventGetPageX(event);
	}
	
	public static int eventGetPageY(final NativeEvent event) {
		return impl.eventGetPageY(event);
	}
	
	public static boolean isHeightSet(final Element el) {
		return isSet(CSS.getHeight(el));
	}
	
	public static boolean isWidthSet(final Element el) {
		return isSet(CSS.getWidth(el));
	}
	
	private static boolean isSet(final String val) {
		if (val == null) {
			return false;
		}
		if (val.length() <= 0) {
			return false;
		}
		if ("auto".equals(val)) {
			return false;
		}
		if ("undefined".equals(val)) {
			return false;
		}
		return true;
	}
	
	public static int eventGetElementX(final Event event, final Element el) {
		return eventGetClientX(event) + Window.getScrollLeft() - getAbsoluteLeft(el);
	}
	
	public static int eventGetElementY(final Event event, final Element el) {
		return eventGetClientY(event) + Window.getScrollTop() - getAbsoluteTop(el);
	}

	public static int eventGetElementX(final NativeEvent event, final Element el) {
		return event.getClientX() + Window.getScrollLeft() - getAbsoluteLeft(el);
	}
	
	public static int eventGetElementY(final NativeEvent event, final Element el) {
		return event.getClientY() + Window.getScrollTop() - getAbsoluteTop(el);
	}

	
	public static Element createEventCatcher() {
		final Element el = impl.createEventCatcher();
		DOM.setElementProperty(el, "id", "mouseCatcher");
		return el;
	}
	
	public static void posMouseCatcher(final Element dragEl, final int x, final int y) {
		impl.posMouseCatcher(dragEl, x, y);
	}
	
	/**
	 * @param element
	 * @param opacity 0..255
	 */
	public static final void setOpacity(final Element element, final int opacity) {
		final double op = opacity / 256.0;
		final int ieop = (int)(op * 100);
		DOM.setStyleAttribute(element, "filter", "alpha(" + ieop + ")");
		DOM.setStyleAttribute(element, "opacity", String.valueOf(op));
	}
	
	// /**
	// * Basically, delta is positive if wheel was scrolled up, and negative, if
	// * wheel was scrolled down.
	// *
	// * @param event
	// */
	// public static int eventGetWheelDelta(Event event)
	// {
	// return impl.eventGetWheelDelta(event);
	// }
	//
	// public static int eventGetWheelX(int eventX, int moveX)
	// {
	// return impl.eventGetWheelX(eventX, moveX);
	// }
	//
	// public static int eventGetWheelY(int eventY, int moveY)
	// {
	// return impl.eventGetWheelY(eventY, moveY);
	// }
	
	public static void setTitle(final Element td, final String name) {
		DOM.setElementProperty(td, "title", name);
	}
	
	public static String getId(final Element elem) {
		return DOM.getElementProperty(elem, "id");
	}
	
	public static Element getChildById(final Element parent, final String childId) {
		final Element el = DOM.getElementById(childId);
		if (DOM.isOrHasChild(parent, el)) {
			return el;
		}
		return findChildById(parent, childId);
	}
	
	private static Element findChildById(final Element parent, final String childId) {
		if (childId.equals(getId(parent))) {
			return parent;
		}
		
		Element child = DOM.getFirstChild(parent);
		while (child != null) {
			final Element ret = findChildById(child, childId);
			if (ret != null) {
				return ret;
			}
			child = DOM.getNextSibling(child);
		}
		return null;
	}
	
	public static boolean isEmpty(final Element el) {
		return getFirstChild(el) == null;
	}
	
	public static Element getActiveElement() {
		return Document.get().getDocumentElement().getPropertyJSO("activeElement").cast();
	}
}
