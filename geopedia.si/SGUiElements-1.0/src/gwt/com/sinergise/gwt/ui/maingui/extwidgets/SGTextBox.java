package com.sinergise.gwt.ui.maingui.extwidgets;


import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;

public class SGTextBox extends TextBox {
	private class AllHandler implements FocusHandler, BlurHandler {
		public void onFocus(FocusEvent event) {
		}

		public void onBlur(BlurEvent event) {
		}
	}
	
	public static boolean	DEBUG					= false;

	public static final String		DEPENDENT_STYLE_EMPTY	= "empty";
	protected String				emptyText				= null;
	private boolean					empty					= true;
	protected boolean				hasFocus				= false;
	protected boolean				selectAllOnFocus		= true;

	protected String defaultValue							= null;
	
	
	public SGTextBox() {
		super();
		initEvents();
	}
	
	public SGTextBox(String text) {
		setText(text);
	}

	public SGTextBox(Element element) {
		super(element);
		initEvents();
	}
	
	protected void initEvents() {
		AllHandler handler = new AllHandler();

		sinkEvents(Event.FOCUSEVENTS);
		addFocusHandler(handler);
		addBlurHandler(handler);
	}

	@Override
	public String getValue() {
		if (isEmpty()) return "";
		return super.getValue();
	}

	@Override
	public String getText() {
		if (isEmpty()) return "";
		return super.getText();
	}

	protected void onFocus() {
		hasFocus = true;
		if (empty && emptyText != null) {
			clear();
		} else if (selectAllOnFocus) {
			selectAll();
			updateStyle();
		}
	}

	protected void onBlur() {
		hasFocus = false;
		onValueChange();
	}
	
	protected void onValueChange() {
		empty = StringUtil.isNullOrEmpty(getVisibleText());
		if ((emptyText != null) && empty) {
			clear();
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// we need to be the first to update when something changes; 
		// using handlers could trigger listeners first, and they will 
		// get the old state
		processBeforeFiring(event);
		super.fireEvent(event);
	}

	protected void processBeforeFiring(GwtEvent<?> event) {
		if (DEBUG) try {
			System.out.println(emptyText+" SGTEXTBOX FIRE: " + event.toDebugString());
		} catch(Throwable t) {}
		
		Type<?> evtType = event.getAssociatedType();
		if (FocusEvent.getType().equals(evtType)) {
			onFocus();
		} else if (BlurEvent.getType().equals(evtType)) {
			onBlur();
		} else if (ValueChangeEvent.getType().equals(evtType)) {
			onValueChange();
		}
	}

	protected String getVisibleText() {
		return super.getText();
	}

	@Override
	public void setText(String text) {
		if (DEBUG) System.out.println(emptyText + " SET TEXT: focus = " + hasFocus + " empty = " + empty + " text = " + text);

		if (isNullOrEmpty(text)) {
			if (!empty) clear();
			return;
		}	
		if (empty) {
			empty = false;
			updateStyle();
		}
		super.setText(text);
	}
	
	//we need this function if we don't want text to have 'empty' style
	public void setNormalText(String txt) {
		super.setText(txt);
	}

	public void setText(String text, final boolean keepCursorAndSelection) {
		setText(text, keepCursorAndSelection, !CSS.RIGHT.equals(CSS.getTextAlign(getElement())));
	}

	public void setText(String text, final boolean keepCursorAndSelection, boolean leftAligned) {
		if (text.equals(getText())) return;
		if (!keepCursorAndSelection || !hasFocus) {
			setText(text);
			return;
		}
		int caretPos = getCursorPos();
		int selLen = getSelectionLength();
		int tLen = getText().length();

		//sometimes caret is ahead of text
		if (caretPos > tLen) tLen = caretPos;

		assert caretPos >= 0 && caretPos <= tLen : "Caret position should be between 0 and len (c = " + caretPos + ", l = " + tLen + ")";
		assert selLen >= 0 && caretPos + selLen <= tLen : "Selection length should be between 0 and len - caretPos (sLen = " + selLen
				+ ", c = " + caretPos + ", l = " + tLen + ")";


		if (!leftAligned) {
			caretPos = tLen - caretPos - selLen;
			assert caretPos >= 0 && caretPos <= tLen : "Caret position should be between 0 and len (c = " + caretPos + ", l = " + tLen
					+ ")";
		}

		setText(text);
		//re-read in case the browser manipulated the text
		text = getText();

		tLen = text.length();
		caretPos = Math.min(caretPos, tLen);
		selLen = Math.min(tLen - caretPos, selLen);

		assert caretPos >= 0 && caretPos <= tLen : "Caret position should be between 0 and len (" + caretPos + ")";
		assert selLen >= 0 && caretPos + selLen <= tLen : "Selection length should be between 0 and len - caretPos (selLen = " + selLen
				+ ", cPos = " + caretPos + ", len = " + tLen + ")";

		if (!leftAligned) {
			caretPos = tLen - caretPos - selLen;
		}
		setSelectionRange(caretPos, selLen);
	}

	
	public void setDefaultValue(String defaultValue){
		this.defaultValue = defaultValue;
	}
	
	public String getDefaultValue(){
		return defaultValue;
	}
	
	public void clear() {
		if (DEBUG) System.out.println("SGTEXTBOX CLEAR: focus: " + hasFocus + " empText:" + emptyText);
		empty = true;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				updateStyle();
				if ((emptyText != null) && !hasFocus && !getElement().equals(ExtDOM.getActiveElement())) {
					SGTextBox.this.setSuperText(emptyText);
				} else {
					SGTextBox.this.setSuperText(null);				
				}
			}
		});
	}
	
	private void setSuperText(String text) {
		super.setText(text);
	}

	protected void updateStyle() {
		if (empty && !hasFocus) {
			addStyleDependentName(DEPENDENT_STYLE_EMPTY);
		} else {
			removeStyleDependentName(DEPENDENT_STYLE_EMPTY);
		}
	}

	public String getEmptyText() {
		return emptyText;
	}

	public SGTextBox setEmptyText(String emptyText) {
		this.emptyText = emptyText;
		if (empty) {
			clear();
		}
		return this;
	}

	public boolean isEmpty() {
		if (hasFocus) {
			return StringUtil.isNullOrEmpty(getVisibleText());
		}
		return empty;
	}

	public void insertTextKeepSelection(int index, String string, boolean keepCursorLeftIfOnBoundary, boolean includeNewInSelectionIfOnBoundary) {
		String text = getText();
		int sLen = string.length();

		int sPos = getCursorPos();
		int sEnd = sPos + getSelectionLength();

		if (sPos == index && !includeNewInSelectionIfOnBoundary) {
			if (!keepCursorLeftIfOnBoundary) {
				sPos += sLen;
				sEnd += sLen;
			}
		} else if (sPos > index) {
			sPos += sLen;
			sEnd += sLen;
		} else if (sEnd == index && includeNewInSelectionIfOnBoundary) {
			sEnd += sLen;
		} else if (sEnd > index) {
			sEnd += sLen;
		}

		setText(text.substring(0, index) + string + text.substring(index));
		setSelectionRange(sPos, sEnd - sPos);
	}

	public void deleteTextKeepSelection(int index, int len) {
		if (len < 1) return;
		String text = getText();
		int sPos = getCursorPos();
		int sEnd = sPos + getSelectionLength();

		if (sPos > index + len) {
			sPos -= len;
			sEnd -= len;
		} else if (sPos > index) {
			sEnd = Math.max(index, sEnd - (index + len - sPos));
			sPos = index;
		}
		setText(text.substring(0, index) + text.substring(index + len));
		setSelectionRange(sPos, sEnd - sPos);
	}

	public boolean hasFocus() {
		return hasFocus;
	}
}
