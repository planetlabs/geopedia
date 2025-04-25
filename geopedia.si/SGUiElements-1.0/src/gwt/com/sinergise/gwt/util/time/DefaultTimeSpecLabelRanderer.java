package com.sinergise.gwt.util.time;

import java.io.IOException;
import java.util.Date;

import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.text.shared.Renderer;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;

class DefaultTimeSpecLabelRanderer implements Renderer<TimeSpec> {
	Resolution res;
	DateTimeFormatRenderer df;
	
	@Override
	public String render(TimeSpec object) {
		if (object!=null) {
			try {
				return df.render(new Date(object.toJavaTimeUtc()));
			} catch (Exception e) {
				/* GWT says we should not ever throw any exceptions */
			}
		}
		
		return null;
	}

	@Override
	public void render(TimeSpec object, Appendable appendable) throws IOException {
		appendable.append(render(object));
	}
	
	public DefaultTimeSpecLabelRanderer() {
		this(new DateTimeFormatRenderer());
	}

	public DefaultTimeSpecLabelRanderer(DateTimeFormatRenderer df) {
		this.df = df;
	}
}