package com.sinergise.gwt.ui.cell;

import com.google.gwt.user.cellview.client.TextColumn;

public abstract class NumberColumn<T> extends TextColumn<T> {

	String defaultIfEmpty = "";

	public NumberColumn(){
	}
	
	public NumberColumn(String defaultIfEmpty) {
		this.defaultIfEmpty = defaultIfEmpty;
	}

	@Override
	public String getValue(T object) {
		return object == null || getNumberValue(object) == null ? defaultIfEmpty : getNumberValue(object).toString();
	}
	
	/**
	 * 
	 * @param object that is known to be not null
	 * @return number to put in cell, can be null
	 */
	public abstract Number getNumberValue(T object);

}
