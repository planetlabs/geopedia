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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.util.web.Namespace;

public class ExtDOMImplIE6 extends ExtDOMImpl {
	
	@Override
	public Element createElementNS(final Namespace ns, final String tag) {
		return createElementNS(ns.getPrefix(), tag);
	}
	
	private native Element createElementNS(String prefix, String tag) /*-{
	                                                                  return $doc.createElement(prefix + ":" + tag);
	                                                                  }-*/;
	
	/**
	 * Set a namespace attr, using the namespace of the element.
	 */
	@Override
	public native void setAttributeNS(Element elem, String attr, String value) /*-{
	                                                                           elem.setAttribute(attr, value);
	                                                                           }-*/;
	
	@Override
	public void setAttributeNS(final Namespace ns, final Element elem, final String attr, final String value) {
		setAttributeNS(ns.getPrefix(), elem, attr, value);
	}
	
	private native void setAttributeNS(String prefix, Element elem, String attr, String value) /*-{
	                                                                                           elem.setAttribute(prefix + ":" + attr, value);
	                                                                                           }-*/;
	
	@Override
	public Element createEventCatcher() {
		final Element img = DOM.createImg();
		DOM.setElementProperty(img, "src", GWT.getModuleBaseURL()+"trPix.gif");
		return img;
	}
	
	@Override
	public void posMouseCatcher(final Element dragEl, final int x, final int y) {
		ExtDOM.posAbs(dragEl, x, y);
	}
	
	public int eventGetWheelX(final int eventX) {
		return eventX;
	}
	
	public int eventGetWheelY(final int eventY) {
		return eventY;
	}
}
