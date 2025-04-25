package com.sinergise.gwt.ui.table;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * @param <T> the data type that will be modified
 */
public abstract class RegexpStringFieldUpdater<T> implements FieldUpdater<T, String> {
	
	private final RegExp regExp;
	
	public RegexpStringFieldUpdater(String pattern) {
		this.regExp = RegExp.compile(pattern);
	}
	
	@Override
	public final void update(int index, T object, String value) {
		MatchResult result = regExp.exec(value);
		if (result.getGroupCount() > 0) {
			onUpdate(index, object, result.getGroup(0));
		}
	}
	
	public abstract void onUpdate(int index, T object, String validatedValue);

}
