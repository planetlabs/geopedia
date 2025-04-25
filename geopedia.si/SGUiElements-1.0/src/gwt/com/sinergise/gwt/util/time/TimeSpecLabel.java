package com.sinergise.gwt.util.time;

import java.util.Date;

import com.google.gwt.user.client.ui.ValueLabel;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;

public class TimeSpecLabel extends ValueLabel<TimeSpec> {
	
	public TimeSpecLabel() {
		this((TimeSpec)null);
	}
	
	public TimeSpecLabel(Date date) {
		this(TimeSpec.createFor(date, Resolution.DAY));
	}
	
	public TimeSpecLabel(TimeSpec ts) {
		super(new DefaultTimeSpecLabelRanderer());
		setValue(ts);
	}
	
	public void setValue(Date date) {
		setValue(date == null ? null : TimeSpec.createFor(date, Resolution.DAY));
	}
}