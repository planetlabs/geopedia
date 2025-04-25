/**
 * 
 */
package com.sinergise.gwt.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget which wraps the text in the appropriate &lt;h*&gt;&lt;/h*&gt; HTML tag.
 * @author tcerovski
 */
public abstract class Heading extends Widget {

	public static class H1 extends Heading {
		public H1(String text) {
			super(text, 1);
		}
	}
	public static class H2 extends Heading {
		public H2(String text) {
			super(text, 2);
		}
	}
	public static class H3 extends Heading {
		public H3(String text) {
			super(text, 3);
		}
	}
	public static class H4 extends Heading {
		public H4(String text) {
			super(text, 4);
		}
	}
	public static class H5 extends Heading {
		public H5(String text) {
			super(text, 5);
		}
	}
	
	public static class H6 extends Heading {
		public H6(String text) {
			super(text, 6);
		}
	}
	
	private Heading(String text, int level) {
		if(level < 1) 
			level = 1;
		setElement(DOM.createElement("h"+level));
		setText(text);
	}
	
	public void setText(String text) {
		DOM.setInnerText(getElement(), text);
	}
	
}
