/*
 *
 */
package com.sinergise.gwt.util.html;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class HTMLTable {
	private HTMLTable() {
	// Non-instantiable
	}
	
	public static final void cellspacing(final Element el, final int csp) {
		DOM.setElementPropertyInt(el, "cellSpacing", csp);
	}
	
	public static final void cellpadding(final Element el, final int cpd) {
		DOM.setElementPropertyInt(el, "cellPadding", cpd);
	}
	
	public static final void colSpan(final Element el, final int span) {
		DOM.setElementPropertyInt(el, "colSpan", span);
	}
	
	public static void width(final Element element, final String w) {
		DOM.setElementProperty(element, "width", w);
	}
	
	public static void height(final Element element, final String h) {
		DOM.setElementProperty(element, "height", h);
	}
	
	public static void border(final Element element, final int size) {
		DOM.setElementPropertyInt(element, "border", size);
	}
	
	public static Widget nonEmpyLabel(final String text) {
		if (text == null || text.trim().length() == 0) {
			return new HTML("&nbsp;");
		}
		return new Label(text);
	}
}
