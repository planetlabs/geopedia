package com.sinergise.common.util.lang;

import com.sinergise.common.util.string.StringTransformer;

class TimeSpecSerializer implements StringTransformer<TimeSpec> {
	private static final long serialVersionUID = 1L;
	public TimeSpecSerializer() {
	}
	@Override
	public String store(TimeSpec obj) {
		return obj == null ? null : obj.toISOString();
	}
	@Override
	public TimeSpec valueOf(String str) {
		return new TimeSpec(str);
	}
} 