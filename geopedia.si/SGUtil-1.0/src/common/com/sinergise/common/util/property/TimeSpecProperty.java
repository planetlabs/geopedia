package com.sinergise.common.util.property;

import java.util.Date;

import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.lang.TimeSpec;

public class TimeSpecProperty extends ScalarPropertyImpl<TimeSpec> {
	private static final long serialVersionUID = 1L;

	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public TimeSpecProperty() { }
	
	public TimeSpecProperty(TimeSpec date) {
		super(date);
	}
	
	@Override
	public void setValue(TimeSpec value) {
		super.setValue(value);
	}
	
	@Override
	public String toString() {
		if (value == null) {
			return "null";
		}
		return DateFormatter.FORMATTER_DEFAULT_DATE.formatDate(new Date(value.toJavaTimeWithJvmLocalTimeZoneOffset()));
	}
	
}
