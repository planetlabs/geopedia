package com.sinergise.gwt.util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.Util.UtilImpl;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.gwt.util.format.GwtI18nProvider;
import com.sinergise.gwt.util.logging.GoogleAnalytics;
import com.sinergise.gwt.util.logging.PiwikAnalytics;
import com.sinergise.gwt.util.url.GWTURLCoder;

public class UtilGWT implements EntryPoint {
	
	public static boolean isSpecialKey(final int keyCode) {
		switch (keyCode) {
			case KeyCodes.KEY_ALT:
			case KeyCodes.KEY_BACKSPACE:
			case KeyCodes.KEY_CTRL:
			case KeyCodes.KEY_DELETE:
			case KeyCodes.KEY_DOWN:
			case KeyCodes.KEY_END:
			case KeyCodes.KEY_ENTER:
			case KeyCodes.KEY_ESCAPE:
			case KeyCodes.KEY_HOME:
			case KeyCodes.KEY_LEFT:
			case KeyCodes.KEY_PAGEDOWN:
			case KeyCodes.KEY_PAGEUP:
			case KeyCodes.KEY_RIGHT:
			case KeyCodes.KEY_SHIFT:
			case KeyCodes.KEY_TAB:
			case KeyCodes.KEY_UP:
				return true;
			default:
				return false;
		}
	}
	
	public static native boolean isIE6() /*-{
	                                     var ua = navigator.userAgent.toLowerCase();
	                                     return (ua.indexOf("msie 6.0") != -1);
	                                     }-*/;
	
	public static native boolean isIE6or7() /*-{
	                                        var ua = navigator.userAgent.toLowerCase();
	                                        return (ua.indexOf("msie 6.0") != -1 || ua.indexOf("msie 7.0") != -1);
	                                        }-*/;
	
	public static native boolean isIE8() /*-{
										    var ua = navigator.userAgent.toLowerCase();
										    return (ua.indexOf("msie 6.0") != -1 || ua.indexOf("msie 7.0") != -1 || ua.indexOf("msie 8.0") != -1);
										    }-*/;
	
	public static String getMetaContent(final String name) {
		final HeadElement head = getHead();
		if (head == null) {
			return null;
		}
		final NodeList<Element> metaTags = head.getElementsByTagName("meta");
		for (int i = 0; i < metaTags.getLength(); i++) {
			final MetaElement meta = metaTags.getItem(i).cast();
			if (name.equalsIgnoreCase(meta.getName())) {
				return meta.getContent();
			}
		}
		return null;
	}
	
	public static HeadElement getHead() {
		// Could use Document.get(), but this seems to work better at bootstrap
		Node nd = RootPanel.getBodyElement().getParentNode().getFirstChild();
		do {
			if ("head".equalsIgnoreCase(nd.getNodeName())) {
				return nd.cast();
			}
			nd = nd.getNextSibling();
		} while (nd != null);
		return null;
	}
	
	public static boolean isOrHasDescendant(Widget ancestor, Widget descendant) {
		if (ancestor == null) {
			return false;
		}
		Widget curParent = descendant;
		while (curParent != null) {
			if (ancestor.equals(curParent)) {
				return true;
			}
			curParent = curParent.getParent();
		}
		return false;
	}

	public static Widget getDirectChildForDescendant(Widget parent, Widget descendant) {
		Widget curChild = descendant;
		while (!(curChild instanceof RootPanel)) {
			Widget curParent = curChild.getParent();
			if (parent.equals(curParent)) {
				return curChild;
			}
			curChild = curParent;
		}
		return null;
	}

	public static native JavaScriptObject openWindow(String url, String target, String features) /*-{
		return $wnd.open(url, target, features);
	}-*/;

	public static native void setWindowLocation(JavaScriptObject targetWindow, String url) /*-{
		targetWindow.location.href=url;
	}-*/;

	@Override
	public void onModuleLoad() {
		UtilGwtImplOld utilObj;
		if (GWT.isScript()) {
			utilObj = GWT.<UtilGwtImplOld>create(UtilImpl.class);
		} else {
			utilObj = new UtilGwtImplOld();
		}
		utilObj.init();
		Util.initImplInstance(utilObj);
		
		GwtI18nProvider.initialize();
		GWTURLCoder.initialize();
		
		
		GoogleAnalytics.initialize();
		GoogleAnalytics.trackPageView("");
		
		PiwikAnalytics.initialize();
		PiwikAnalytics.trackPageView("");
	}

	public static String getAbsoluteUrlFromModuleBase(String url) {
		return URLUtil.isAbsolute(url) ? url : (GWT.getModuleBaseURL() + url);
	}
	
	public static String getAbsoluteUrlFromHostPageBase(String url) {
		return URLUtil.isAbsolute(url) ? url : (GWT.getHostPageBaseURL() + url);
	}
}
