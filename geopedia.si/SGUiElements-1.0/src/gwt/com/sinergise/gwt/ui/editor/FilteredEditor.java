/*
 *
 */
package com.sinergise.gwt.ui.editor;


import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.util.UtilGWT;

/**
 * 
 * This class is capable of filtering/substituting user input characters
 * on-the-fly Implement allowChar(...) to define, which chars get filtered.
 * 
 * @author mkadunc
 */
public abstract class FilteredEditor extends SGTextBox implements FocusHandler, BlurHandler {
	public FilteredEditor() {
		super();
		addFocusHandler(this);
		addBlurHandler(this);
		addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				normalize();
			}
		});
		addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				int charCode = event.getUnicodeCharCode();
				if (charCode==0) {
					int keyCode = event.getNativeEvent().getKeyCode();
					if (UtilGWT.isSpecialKey(keyCode)) {
						return;
					}
				} else {
					if (UtilGWT.isSpecialKey(event.getCharCode()))
						return;
				}
				int index = getCursorPos();
				if (event.isMetaKeyDown() || event.isControlKeyDown() || event.isAltKeyDown()) return;
				if (allowChar(event.getCharCode(), index))
					return;
				cancelKey();
			}

		});		
	}
	
	public void normalize() {
	}
	
	public void onBlur(BlurEvent event) {
		normalize();
	}
	
	public void onFocus(FocusEvent event) {
	}
	
	public FilteredEditor(String text) {
		this();
		setText(text);
	}

	/**
	 * Sets the string to text box programmatically. If allowChar(...) return
	 * false for a character in input string, substitute method is called.
	 * 
	 */
	@Override
	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (!allowChar(ch, buf.length())) {
				ch = substitute(ch, buf.length());
			}
			buf.append(ch);
		}
		super.setText(buf.toString());
	}
	
	/**
	 * @param ch the input character
	 * @return an appropriate substitute (eg: lower, upper case)
	 */
	protected char substitute(char ch, int index) {
		if (allowChar(ch, index))
			return ch;

		char lower = Character.toLowerCase(ch);
		if (lower!=ch && allowChar(lower, index))
			return lower;

		char upper = Character.toUpperCase(ch);
		if (upper != ch && upper != lower && allowChar(upper, index))
			return upper;

		return ch;
	}

	protected abstract boolean allowChar(char ch, int index);
}
