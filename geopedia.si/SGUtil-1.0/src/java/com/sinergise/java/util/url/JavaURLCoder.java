package com.sinergise.java.util.url;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.sinergise.common.util.url.URLCoder;

public class JavaURLCoder implements URLCoder {
	@Override
	public String decodePart(final String encodedPart) {
		try {
			return URLDecoder.decode(encodedPart, "UTF-8");
		} catch(final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String encodePart(final String plainPart) {
		try {
			return URLEncoder.encode(plainPart, "UTF-8").replaceAll("\\+", "\\%20");
		} catch(final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isFile(URL url) {
		return url.getProtocol().toLowerCase().startsWith("file");
	}
}
