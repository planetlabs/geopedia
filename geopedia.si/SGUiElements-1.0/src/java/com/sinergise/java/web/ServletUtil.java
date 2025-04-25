package com.sinergise.java.web;

import static com.sinergise.common.util.web.HttpHeaders.FIELD_CONTENT_DISPOSITION;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.ATTACHMENT;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.INLINE;
import static com.sinergise.java.web.ServletUtil.ContentDispositionMode.NONE;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.common.util.web.HttpHeaders;
import com.sinergise.common.util.web.HttpHeaders.HttpHeaderFieldSpec;

public class ServletUtil {
	public static enum ContentDispositionMode {//
		NONE(null), //
		INLINE("inline"), //
		ATTACHMENT("attachment");

		String headerVal;

		private ContentDispositionMode(String headerVal) {
			this.headerVal = headerVal;
		}

		public String getHeaderValue() {
			return headerVal;
		}
	}

	private static final long YEAR_MS = 365L * 24 * 60 * 60 * 1000;

	public static final String H_PARAM_CONTENT_DISPOSITION_ATTACHMENT = "attachment";
	public static final String H_PARAM_CONTENT_DISPOSITION_INLINE = "inline";
	public static final String H_PARAM_CONTENT_DISPOSITION_FILENAME = "filename";

	public static String getUrlRelativeToApp(HttpServletRequest req) {

		StringBuilder sb = new StringBuilder();
		String servletPath = req.getServletPath();
		sb.append(servletPath.startsWith("/") ? servletPath.substring(1) : servletPath);
		sb.append(StringUtil.emptyIfNull(req.getPathInfo()));

		String queryString = req.getQueryString();
		if (StringUtil.isNullOrEmpty(queryString)) {
			return sb.toString();
		}
		return sb.append('?').append(queryString).toString();
	}

	public static String getFullUrl(HttpServletRequest req) {
		StringBuffer sb = req.getRequestURL();
		String qs = req.getQueryString();
		if (StringUtil.isNullOrEmpty(qs)) {
			return sb.toString();
		}
		return sb.append('?').append(qs).toString();
	}


	public static String findInitParameter(Servlet servlet, String name) {
		String ret;

		ret = findInitParameterInJDNIContext(name);
		if (ret != null) {
			return ret;
		}

		ret = findInitParameter(servlet.getServletConfig(), name, null);
		if (ret != null) {
			return ret;
		}
		return null;
	}

	public static String findInitParameter(ServletContext servletContext, String name, String defaultValue) {
		if (servletContext == null) {
			throw new IllegalStateException("Servlet not initialized");
		}
		String ret = servletContext.getInitParameter(name);
		if (ret == null || ret.length() == 0) {
			return defaultValue;
		}
		return ret;
	}

	public static String findInitParameter(ServletConfig config, String name, String defaultValue) {
		if (config == null) {
			throw new IllegalStateException("Servlet not initialized");
		}
		String ret = config.getInitParameter(name);
		if (ret != null && ret.length() > 0) {
			return ret;
		}

		return findInitParameter(config.getServletContext(), name, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> findInitParametersWithPrefix(ServletConfig config, String prefix) {
		if (config == null) {
			throw new IllegalStateException("Servlet not initialized");
		}

		ServletContext ctx = config.getServletContext();
		Map<String, String> params = new HashMap<String, String>();

		Enumeration<String> paramsEnum = ctx.getInitParameterNames();
		while (paramsEnum.hasMoreElements()) {
			String name = paramsEnum.nextElement();
			if (name.startsWith(prefix)) {
				params.put(name, ctx.getInitParameter(name));
			}
		}

		paramsEnum = config.getInitParameterNames();
		while (paramsEnum.hasMoreElements()) {
			String name = paramsEnum.nextElement();
			if (name.startsWith(prefix)) {
				params.put(name, config.getInitParameter(name));
			}
		}


		return params;
	}

	private static String findInitParameterInJDNIContext(String name) {
		String ret = null;
		try {
			Context ic = new InitialContext();
			Object str = ic.lookup("java:/comp/env/" + name);
			if (str != null) {
				ret = str.toString();
			}
		} catch(Throwable e) {
			//ignore
		}
		return ret;
	}


	public static String findInitParameter(FilterConfig config, String name, String defaultValue) {
		if (config == null) {
			throw new IllegalStateException("Filter not initialized");
		}
		String ret = config.getInitParameter(name);
		if (ret != null && ret.length() > 0) {
			return ret;
		}

		return findInitParameter(config.getServletContext(), name, defaultValue);
	}

	public static final File getTempDir(Servlet servlet) {
		return (File)servlet.getServletConfig().getServletContext().getAttribute("javax.servlet.context.tempdir");
	}


	public static String getClientInfoString(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.getRemoteHost()).append(":").append(req.getRemotePort()).append("\n  ");

		if (appendHeaderFieldDescription(sb, HttpHeaders.FIELD_REFERER, req)) {
			sb.append("\n  ");
		}
		if (appendHeaderFieldDescription(sb, HttpHeaders.FIELD_USER_AGENT, req)) {
			sb.append("\n  ");
		}
		if (appendHeaderFieldDescription(sb, HttpHeaders.FIELD_VIA, req)) {
			sb.append("\n  ");
		}
		return sb.toString();
	}

	public static String encodeHeaderFieldParam(String name, String value) {
		return name + "=" + value;
	}

	public static <T> void setHeaderField(HttpServletResponse res, HttpHeaderFieldSpec<T> field, T value) {
		res.setHeader(field.getKeyName(), field.write(value));
	}

	public static <T> T getHeaderField(HttpServletRequest req, HttpHeaderFieldSpec<T> field) throws Exception {
		return field.read(req.getHeader(field.getKeyName()));
	}

	public static void setAttached(HttpServletResponse res) {
		setAttachedFileName(res, null);
	}

	public static void setAttachedFileName(HttpServletResponse res, String fileName) {
		setFilename(res, fileName, false);
	}

	public static void setFilename(HttpServletResponse res, String fileName, boolean inline) {
		setFilename(res, fileName, inline ? INLINE : ATTACHMENT);
	}

	public static void setFilename(HttpServletResponse res, String fileName, ContentDispositionMode mode) {
		if (mode == null || mode == NONE) {
			return;
		}
		String fNamePart = StringUtil.isNullOrEmpty(fileName) ? "" : "; "
			+ encodeHeaderFieldParam(H_PARAM_CONTENT_DISPOSITION_FILENAME, '\"' + fileName + '\"');
		setHeaderField(res, FIELD_CONTENT_DISPOSITION, mode.getHeaderValue() + fNamePart);
	}

	public static void setExpiration(HttpServletResponse res, long duration) {
		final long time = System.currentTimeMillis();
		final Date now = new Date(time);
		setHeaderField(res, HttpHeaders.FIELD_DATE, now);
		if (duration > 0) {
			if (duration > YEAR_MS) {
				duration = YEAR_MS;
			}
			final long dSec = duration / 1000L;
			setHeaderField(res, HttpHeaders.FIELD_VARY, "Accept-Encoding");
			setHeaderField(res, HttpHeaders.FIELD_EXPIRES, new Date(time + duration));
			setHeaderField(res, HttpHeaders.FIELD_AGE, String.valueOf(2 * dSec));
			setHeaderField(res, HttpHeaders.FIELD_LAST_MODIFIED, new Date(time - 2 * duration));
			setHeaderField(res, HttpHeaders.FIELD_CACHE_CONTROL, "public, max-age=" + String.valueOf(3 * dSec));
		} else {
			setHeaderField(res, HttpHeaders.FIELD_EXPIRES, now);
			setHeaderField(res, HttpHeaders.FIELD_PRAGMA, "no-cache");
			setHeaderField(res, HttpHeaders.FIELD_CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
		}
	}

	public static DataSource getDataSourceFromContext(String dataSourceName, Logger logr) {
		if (dataSourceName == null || dataSourceName.trim().length() == 0) {
			return null;
		}
		try {
			InitialContext ctx = new InitialContext();
			return (DataSource)ctx.lookup(dataSourceName);
		} catch(Exception e) {
			if (logr != null) {
				logr.warn("Could not find data source for name '" + dataSourceName + "'", e);
			}
			return null;
		}
	}

	/**
	 * @param servlet
	 * @param defaultDSName
	 * @param lastT
	 * @return
	 */
	public static DataSource getDataSourceWithParam(Servlet servlet, String dsParamName, String defaultDSName, Logger logr) {
		String dataSource = findInitParameter(servlet, dsParamName);
		if (dataSource == null) {
			dataSource = defaultDSName;
		}
		if (dataSource == null) {
			return null;
		}
		return getDataSourceFromContext(dataSource, logr);
	}


	public static boolean appendHeaderFieldDescription(Appendable result, HttpHeaderFieldSpec<?> field,
		HttpServletRequest req) {
		try {
			Object val = getHeaderField(req, field);
			if (val != null) {
				result.append(field.getKeyName()).append(':').append(' ').append(val.toString());
				return true;
			}
		} catch(Exception e) {}
		return false;
	}

	public static Map<String, List<String>> parseQueryStringUtf8(String queryString) {
		HashMap<String, List<String>> ret = new HashMap<String, List<String>>();

		String[] parts = queryString.split("&");
		for (String part : parts) {
			parseQueryStringPartUtf8(ret, part);
		}
		return ret;
	}

	private static void parseQueryStringPartUtf8(HashMap<String, List<String>> ret, String part) {
		String[] keyVal = part.split("=");
		String key = URLUtil.decodePart(keyVal[0]);
		List<String> vals = getOrCreateVals(ret, key);
		if (keyVal.length > 1) {
			vals.add(URLUtil.decodePart(keyVal[1]));
		}
	}

	private static List<String> getOrCreateVals(HashMap<String, List<String>> ret, String key) {
		List<String> valsForKey = ret.get(key);
		if (valsForKey == null) {
			ret.put(key, valsForKey = new ArrayList<String>(1));
		}
		return valsForKey;
	}
}
