/**
 * 
 */
package com.sinergise.gwt.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for creating simple paragraphs <p>.
 */
public class SGParagraph extends Widget {

	public SGParagraph(String text) {
		setElement(DOM.createElement("p"));
		setText(text);
	}
	
	public void setText(String text) {
		DOM.setInnerText(getElement(), text);
	}
	public void setHTML(String html) {
		DOM.setInnerHTML(getElement(), html);
	}
	
}
