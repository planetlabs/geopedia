package com.sinergise.gwt.util.url;

import com.google.gwt.http.client.URL;
import com.sinergise.common.util.url.URLCoder;
import com.sinergise.common.util.url.URLUtil;

public class GWTURLCoder implements URLCoder {
	public static final void initialize() {
		if (!URLUtil.isInitialized()) {
			final com.sinergise.gwt.util.url.GWTURLCoder guc = new com.sinergise.gwt.util.url.GWTURLCoder();
			guc.encodePart("bla&se");
			URLUtil.initCoder(guc);
		}
	}
	
	
	@Override
	public String decodePart(final String encodedPart) {
		return URL.decodePathSegment(encodedPart);
	}

	@Override
	public String encodePart(final String plainPart) {
		return URL.encodePathSegment(plainPart);
	}
}
