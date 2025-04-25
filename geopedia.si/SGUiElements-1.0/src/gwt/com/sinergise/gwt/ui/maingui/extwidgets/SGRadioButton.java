package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.RadioButton;

public class SGRadioButton<T> extends RadioButton {
	private final T inherentValue;
	
	public SGRadioButton(SGRadioButtonGroup<T> group, T value) {
		this(group, value, false);
	}
	
	public SGRadioButton(SGRadioButtonGroup<T> group, T value, boolean selected) {
		super(group.getName());
		setValue(Boolean.valueOf(selected));
		
		group.add(this);
		
		this.inherentValue = value;
	}
	
	public SGRadioButton(SGRadioButtonGroup<T> group, T value, String label, boolean selected) {
		this(group, value, selected);
		setText(label);
	}
	
	public T getInherentValue(){
		return inherentValue;
	}
}
