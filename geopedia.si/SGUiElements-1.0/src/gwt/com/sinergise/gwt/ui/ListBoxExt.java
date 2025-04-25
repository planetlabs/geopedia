/**
 * 
 */
package com.sinergise.gwt.ui;

import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.common.util.Util;

/**
 * @author tcerovski
 */
public class ListBoxExt extends ListBox implements HasValue<String> {
	private static final int INSERT_AT_END = -1;
	private boolean valueChangeHandlerInitialized;


	public ListBoxExt() {
		this(false);
	}

	public ListBoxExt(boolean isMultipleSelect) {
		super(isMultipleSelect);
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		// Initialization code
		if (!valueChangeHandlerInitialized) {
			valueChangeHandlerInitialized = true;
			addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(ListBoxExt.this, getValue());
				}
			});
		}
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public void clearSelection() {
		setSelectedIndex(-1);
	}

	public String getValue() {
		return getSelectElement().getValue();
	}

	protected SelectElement getSelectElement() {
		return getElement().cast();
	}

	public void setValue(String value, boolean fireEvents) {
	    String oldValue = getValue();
	    setValue(value);
	    if (fireEvents) ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

	/** @deprecated use {@link #setValue(String)} instead */
	@Deprecated
	public void setSelectedValue(String value) {
		setValue(value);
	}
	
	public void setValue(String value) {
		getSelectElement().setValue(value);
	}

	public void addUnselectableItem(String name) {
		insertUnselectableItem(name, INSERT_AT_END);
	}

	public void insertUnselectableItem(String name, int itemIdx) {
		super.insertItem(name, itemIdx);
		SelectElement select = getSelectElement();
		if (itemIdx == INSERT_AT_END) itemIdx = select.getLength() - 1;
		setOptionEnabled(itemIdx, false);
	}

	public int indexOfValue(String value) {
		int len = getItemCount();
		for (int i = 0; i < len; i++) {
			if (Util.safeEquals(value, getValue(i))) return i;
		}
		return -1;
	}

	public void setOptionEnabled(String optValue, boolean enabled) {
		setOptionEnabled(indexOfValue(optValue), enabled);
	}

	public void removeOption(String optValue) {
		removeItem(indexOfValue(optValue));
	}

	public void setOptionEnabled(int optIdx, boolean enabled) {
		setOptionEnabled(getSelectElement(), optIdx, enabled);
	}

	public native void setOptionEnabled(SelectElement select, int optIdx, boolean enabled) /*-{
																							select.options[optIdx].disabled = enabled ? "" : "disabled";
																							}-*/;
}
