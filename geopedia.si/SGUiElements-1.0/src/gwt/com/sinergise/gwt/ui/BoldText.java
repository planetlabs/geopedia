/**
 * 
 */
package com.sinergise.gwt.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for creating simple bold tag <b>.
 */
public class BoldText extends Widget {

	public BoldText() {
		setElement(DOM.createElement("b"));
	}
	public BoldText(String text) {
		this();
		setText(text);
	}
	
	public void setText(String text) {
		DOM.setInnerText(getElement(), text);
	}
	public void setHTML(String html) {
		DOM.setInnerHTML(getElement(), html);
	}
	public String getText() {
		return getElement().getInnerText();
	}
}
