package com.sinergise.geopedia.pro.client.ui.widgets;

import com.sinergise.gwt.ui.ListBoxExt;

public class EnumListBox<E extends Enum<E>> extends ListBoxExt{

	private E[] values;
	public EnumListBox(E[] values) {
		this.values=values;
		for(int i=0;i<values.length;i++) {
			addItem(getI18NLabelFor(values[i]),String.valueOf(i));
		}
		
	}
	
	
	public E getEnumValue() {
		return values[Integer.valueOf(super.getValue())];
	}
	
	public void setEnumValue(E value) {
		setValue(String.valueOf(value.ordinal()));
	}
	public String getI18NLabelFor(Enum<E> enm) {
		return enm.name();
	}
}
