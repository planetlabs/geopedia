package com.sinergise.java.web;

import static com.sinergise.common.util.web.HttpHeaders.FIELD_ETAG;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.sinergise.java.util.UtilJava;


/**
 * {@link Filter} to add cache control headers for GWT generated files to ensure that the correct files get cached.
 * <a href = "http://stackoverflow.com/questions/3407649/stop-browser-scripts-caching-in-gwt-app">StackOverflow source</a>
 * 
 * @author See Wah Cheng
 * @author Miha Kadunc
 * @created 24 Feb 2009
 */
public class CacheControlFilter implements Filter {
	private static final class ETagBlockingResponseWrapper extends HttpServletResponseWrapper {
		public ETagBlockingResponseWrapper(HttpServletResponse response) {
			super(response);
		}
		
		@Override
		public void addHeader(String name, String value) {
			if (!FIELD_ETAG.getKeyName().equalsIgnoreCase(name)) {
				super.addHeader(name, value);
			}
		}
		
		@Override
		public void setHeader(String name, String value) {
			if (!FIELD_ETAG.getKeyName().equalsIgnoreCase(name)) {
				super.setHeader(name, value);
			}
		}
		
		@Override
		@Deprecated
		public void setStatus(int sc, String sm) {
			super.setStatus(sc, sm);
		}
		
		@Override
		@Deprecated
		public String encodeRedirectUrl(String url) {
			return super.encodeRedirectUrl(url);
		}
		
		@Override
		@Deprecated
		public String encodeUrl(String url) {
			return super.encodeUrl(url);
		}
	} 
	
	private static final String PARAM_CACHE_REGEX = "cacheRegex";
	private static final String PARAM_NOCACHE_REGEX = "noCacheRegex";
	
	private String regexNoCache = null;
	private String regexCache = null;
	
	public void destroy() {}

	public void init(FilterConfig config) throws ServletException {
		UtilJava.initStaticUtils();
		regexCache = ServletUtil.findInitParameter(config, PARAM_CACHE_REGEX, ".+\\.cache\\..+");
		regexNoCache = ServletUtil.findInitParameter(config, PARAM_NOCACHE_REGEX, ".+\\.nocache\\..+");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			final String requestURI = ((HttpServletRequest)request).getRequestURI();

			if (requestURI.matches(regexNoCache)) {
				HttpServletResponse httpResponse = (HttpServletResponse)response;
				ServletUtil.setExpiration(httpResponse, 0);
				response = new ETagBlockingResponseWrapper(httpResponse);
				
			} else if (requestURI.matches(regexCache)) {
				HttpServletResponse httpResponse = (HttpServletResponse)response;
				ServletUtil.setExpiration(httpResponse, Long.MAX_VALUE);
				response = new ETagBlockingResponseWrapper(httpResponse);
			}
		}
		filterChain.doFilter(request, response);
	}
}
