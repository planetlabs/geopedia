package com.sinergise.common.util.property;

import java.util.Date;

import com.sinergise.common.util.format.DateFormatter;

public class DateProperty extends ScalarPropertyImpl<Date> {
	private static final long serialVersionUID = 1L;

	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public DateProperty() { }
	
	public DateProperty(Date date) {
		super(date);
	}
	
	@Override
	public void setValue(Date value) {
		super.setValue(value);
	}
	
	@Override
	public String toString() {
		if (value == null) {
			return "null";
		}
		return DateFormatter.FORMATTER_DEFAULT_DATE.formatDate(value);
	}
	
}
