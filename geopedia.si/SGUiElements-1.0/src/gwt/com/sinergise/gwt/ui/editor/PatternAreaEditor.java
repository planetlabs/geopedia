package com.sinergise.gwt.ui.editor;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sinergise.common.util.format.Format.PatternAreaFormatter;

public class PatternAreaEditor extends FilteredEditor {
	
	private final PatternAreaFormatter formatter;
	
	public PatternAreaEditor() {
		this(new PatternAreaFormatter("km.ha.aa.mm"));
	}
	
	public PatternAreaEditor(PatternAreaFormatter formatter) {
		this.formatter = formatter;
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setEditorValue(getEditorValue());
			}
		});
	}
	
	@Override
	public void onFocus(FocusEvent event) {
		selectAll();
		super.onFocus(event);
	}
	
	public Double getEditorValue() {
		String text = getText();
		text = text.replaceAll("\\.", "");
		try {
			return Double.valueOf(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void setEditorValue(Double value) {
		if (value == null) {
			setText("");
		} else {
			setText(formatter.format(value.intValue()));
		}
	}

	@Override
	protected boolean allowChar(char ch, int index) {
		return Character.isDigit(ch) || ch == '.';
	}

}
