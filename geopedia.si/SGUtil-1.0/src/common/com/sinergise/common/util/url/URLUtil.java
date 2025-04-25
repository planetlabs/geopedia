package com.sinergise.common.util.url;

public class URLUtil {
	private URLUtil() {}
	
	private static URLCoder coder = null;
	
	public static final void initCoder(final URLCoder resolvedCoder) {
		URLUtil.coder = resolvedCoder;
	}
	
	private static final void checkCoder() {
		if (coder == null) {
			throw new IllegalStateException("URLCoder is not initialized. Make sure that com.sinergise.java.util.UtilJava.initStaticUtils() has been called on the server.");
		}
	}
	
	public static final String encodePart(final String plainPart) {
		checkCoder();
		return coder.encodePart(plainPart);
	}
	
	public static final String decodePart(final String encodedPart) {
		checkCoder();
		return coder.decodePart(encodedPart);
	}
	
	public static final String getAuthority(String urlString) {
		// http://www.ietf.org/rfc/rfc2396.txt
		//
		// The authority component is preceded by a double slash "//" and is
		// terminated by the next slash "/", question-mark "?", or by the end of
		// the URI. Within the authority component, the characters ";", ":",
		// "@", "?", and "/" are reserved.
		
		int startIdx = urlString.indexOf("://");
		if (startIdx < 0) return null;
		startIdx += 3;
		int endIdx = urlString.indexOf('/', startIdx);
		if (endIdx < 0) {
			endIdx = urlString.indexOf('?', startIdx);
		}
		if (endIdx < 0) return null;
		return URLUtil.decodePart(urlString.substring(startIdx, endIdx));
	}
	
	public static final String getHost(String urlString) {
		// http://www.ietf.org/rfc/rfc2396.txt
		//
		// 3.2.2. Server-based Naming Authority
		//
		// URL schemes that involve the direct use of an IP-based protocol to a
		// specified server on the Internet use a common syntax for the server
		// component of the URI's scheme-specific data:
		//
		// <userinfo>@<host>:<port>
		//
		// where <userinfo> may consist of a user name and, optionally, scheme-
		// specific information about how to gain authorization to access the
		// server. The parts "<userinfo>@" and ":<port>" may be omitted.
		
		String auth = getAuthority(urlString);
		int idxStart = auth.indexOf('@');
		int idxEnd = auth.lastIndexOf(':');
		if (idxEnd < 0)  {
			if (idxStart<0) return auth;
			idxEnd = auth.length();
		}
		return URLUtil.decodePart(auth.substring(idxStart+1, idxEnd));
	}
	
	public static int getPort(String urlString) {
		String auth = getAuthority(urlString);
		int idx = auth.lastIndexOf(':');
		if (idx<0) return Integer.MIN_VALUE;
		return Integer.parseInt(auth.substring(idx+1));
	}
	
	public static String getProtocol(String urlString) {
		return urlString.substring(0, urlString.indexOf(':'));
	}

	public static String ensurePathSeparatorAtEnd(String urlString) {
		if (urlString.endsWith("/")) {
			return urlString;
		}
		return urlString + '/';
	}
	
	public static boolean isInitialized() {
		return coder != null;
	}

	public static boolean isAbsolute(String url) {
		return url.matches("^[a-z]+\\:\\/\\/.*");
	}
	
}
