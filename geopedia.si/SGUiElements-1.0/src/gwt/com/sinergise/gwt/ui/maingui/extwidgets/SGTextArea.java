package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.util.html.ExtDOM;

public class SGTextArea extends TextArea {
	public static boolean DEBUG = false;
	public static final String DEPENDENT_STYLE_EMPTY = "empty";
	
	private boolean hasFocus = false;
	private boolean empty = true;
	private String emptyText = null;
	private boolean selectAllOnFocus = false;
	private int maxLength = Integer.MIN_VALUE;

	public SGTextArea() {
		super();
		initEvents();
	}

	public SGTextArea(Element element) {
		super(element);
		initEvents();
	}

	protected void initEvents() {
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				SGTextArea.this.onFocus();
			}
		});
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				SGTextArea.this.onBlur();
			}
		});
		addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				SGTextArea.this.onKeyUp(event);
			}
		});
	}

	protected void onKeyUp(KeyUpEvent event) {
		if (hasFocus) {
			String curText = super.getText();
			if (maxLength > 0 && curText.length() > maxLength) {
				super.setText(curText.substring(0, maxLength));
				event.preventDefault();
			}
		}
	}

	public void setMaxLength(int length) {
		this.maxLength = length;
	}

	@Override
	public String getText() {
		if (isEmpty()) {
			return "";
		}
		if (isOverLength()) {
			return super.getText().substring(0, maxLength);
		}
		return super.getText();
	}

	protected boolean isOverLength() {
		if (maxLength > 0) {
			String value = super.getText();
			if (value != null && value.length() > maxLength) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
		if (hasFocus) {
			return StringUtil.isNullOrEmpty(getVisibleText());
		}
		return empty;
	}

	protected void onFocus() {
		debug("FOCUS");
		if (hasFocus) {
			return;
		}
		hasFocus = true;
		if (empty && emptyText != null) {
			clear();
		} else if (selectAllOnFocus) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					if (selectAllOnFocus && hasFocus && !empty) {
						selectAll();
					}
				}
			});
		}
	}

	protected String getVisibleText() {
		return super.getText();
	}

	protected void onBlur() {
		debug("BLUR");
		if (!hasFocus) {
			return;
		}
		hasFocus = false;
		empty = StringUtil.isNullOrEmpty(getVisibleText());
		if ((emptyText != null) && empty) {
			clear();
			return;
		}
		if (isOverLength()) {
			super.setText(getText());
		}

	}

	public void clear() {
		empty = true;
		updateStyle();
		if (empty && (emptyText != null) && !hasFocus && !getElement().equals(ExtDOM.getActiveElement())) {
			debug("CLEAR ... focus:" + hasFocus + " setting empText:" + emptyText);
			SGTextArea.this.setSuperText(emptyText);
		} else {
			debug("CLEAR ... focus:" + hasFocus + " clearing text");
			SGTextArea.this.setSuperText(null);
		}
	}

	private void debug(String string) {
		if (DEBUG) {
			System.out.println("SGTEXTAREA " + getName() + " |" + emptyText + "| "+string);
		}
	}

	protected void updateStyle() {
		if (empty && !hasFocus) {
			addStyleDependentName(DEPENDENT_STYLE_EMPTY);
		} else {
			removeStyleDependentName(DEPENDENT_STYLE_EMPTY);
		}
	}

	private void setSuperText(String text) {
		super.setText(text);
	}

	@Override
	public void setText(String text) {
		empty = text == null || text.equals("");
		super.setText(text);
		if (empty) {
			clear();
		}
	}

	public SGTextArea setEmptyText(String emptyText) {
		this.emptyText = emptyText;
		if (empty) {
			clear();
		}
		return this;
	}

	public void setSelectAllOnFocus(boolean val) {
		selectAllOnFocus = val;
	}
}
