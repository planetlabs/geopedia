package com.sinergise.gwt.util.time;

import java.util.Date;

import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;
import com.sinergise.common.util.lang.TimeSpecRange;
import com.sinergise.gwt.ui.calendar.SGDatePicker;

public class TimeSpecUtilGWT {
	public static TimeSpecRange toRange(SGDatePicker from, SGDatePicker to) {
		return toRange(from, to, Resolution.DAY);
	}
	public static TimeSpecRange toRange(SGDatePicker from, SGDatePicker to, Resolution r) {
		Date a = from.getDate();
		Date b = to.getDate();
		
		if (a == null && b == null)
			return null;
		
		return new TimeSpecRange(
			TimeSpec.createFor(a, r),
			TimeSpec.createFor(b, r));
	}
}
