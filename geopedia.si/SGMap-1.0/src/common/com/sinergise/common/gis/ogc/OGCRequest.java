package com.sinergise.common.gis.ogc;

import static com.sinergise.common.util.web.HttpMethod.GET;

import java.util.Iterator;

import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.common.util.web.HttpMethod;
import com.sinergise.common.util.web.MimeType;

public class OGCRequest extends OGCObject {
	private static final long serialVersionUID = 1L;
	
	/*
	 * @deprecated Not used since WMS 1.0.0
	 */
	private static final String PARAM_WMTVER = "WMTVER";
	private OGCRequestContext contextData;

	private HttpMethod method = GET;
	
	public OGCRequest() {
		this(null);
	}
	
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	public HttpMethod getMethod() {
		return method;
	}

	public OGCRequest(StateGWT sourceState) {
		this(sourceState, null);
	}

	public OGCRequest(StateGWT props, OGCRequestContext contextData) {
		super(props);
		if (contextData != null) {
			this.contextData = contextData;
		} else {
			this.contextData = new OGCRequestContext();
		}
	}

	/**
	 * Service type ("WMS", "WFS", "WMTS", "WCS", ...)
	 */
    public static final String PARAM_SERVICE = "SERVICE";

	/**
	 * Request name ("GetMap", "GetCapabilities", "GetFeatureInfo", ...).
	 */
	public static final String PARAM_REQUEST = "REQUEST";
	
	/**
	 * Request version.
	 */
	public static final String PARAM_VERSION = "VERSION";

	/**
	 * Languages accepted as a result. All human-readable strings in the response should be in one of the languages requested.
	 */
	public static final String PARAM_ACCEPT_LANGUAGES = "AcceptLanguages";
	
	/**
	 * Default parameter name for the desired format (mime-type) of the response to this request.
	 */
	public static final String PARAM_FORMAT = "FORMAT";

	/**
	 * Setting this to true causes the server to produce headers which will make the browser treat the response as attachment.  
	 */
	public static final String PARAM_RESP_ATTACHMENT = "HTTP_ATT";
	
	/**
	 * Requested response locale
	 */
	public static final String PARAM_LOCALE = "LOCALE";

	public static final String REQ_GET_CAPABILITIES = "GetCapabilities";
	
	
	
	public String getRequestType() {
		return get(PARAM_REQUEST, null);
	}

	public String getVersion() {
		return get(PARAM_VERSION, get(PARAM_WMTVER));
	}

	public void setVersion(String version) {
		set(PARAM_VERSION, version);
	}

	public String get(String paramName) {
		return get(paramName, null);
	}

	public String get(String paramName, String defaultValue) {
		if (PARAM_WMTVER.equals(paramName)) {
			paramName = PARAM_VERSION;
		}
		paramName = paramName.toUpperCase();
		if (properties.containsPrimitive(paramName)) {
			return properties.getString(paramName.toUpperCase(), defaultValue);
		}
		return defaultValue;
	}

	public String set(String paramName, String paramValue) {
		if (PARAM_WMTVER.equalsIgnoreCase(paramName)) {
			paramName = PARAM_VERSION;
		}
		return properties.putString(paramName.toUpperCase(), paramValue);
	}

	public void validate() throws OGCException {
		validateNotNull(PARAM_REQUEST);
	}

	protected void validateNotNull(String paramName) throws OGCException {
		if (!containsParam(paramName))
			throw new OGCException(this, paramName + " is mandatory");
	}

	public boolean containsParam(String name) {
		return properties.contains(name);
	}

	/**
	 * Actually concatenates all params into valid HTTP request string
	 * 
	 * @param baseURL
	 * @return
	 */
	public String createRequestURL(String baseURL) {
		StringBuffer buf = new StringBuffer();
		buf.append(baseURL);
		buf.append('?');
		boolean first = true;
		for (Iterator<String> iter = paramNames(); iter.hasNext();) {
			String name = iter.next();
			String val = get(name);
			if (val != null) {
				if (!first)
					buf.append('&');
				first = false;
				buf.append(name);
				buf.append('=');
				buf.append(URLUtil.encodePart(val));
			}
		}
		return buf.toString();
	}

	public Iterator<String> paramNames() {
		return properties.keyIterator();
	}

	public int getInt(String paramName, int defaultValue) {
		return properties.getInt(paramName.toUpperCase(), defaultValue);
	}

	public double getDouble(String paramName, double defaultValue) {
		return properties.getDouble(paramName.toUpperCase(), defaultValue);
	}

	public Double getDouble(String paramName, Double defaultValue) {
		return properties.getDouble(paramName.toUpperCase(), defaultValue);
	}

	public void setInt(String paramName, int value) {
		properties.putInt(paramName.toUpperCase(), value);
	}

	public void setDefaults(OGCRequest defaultRequest) {
		for (Iterator<String> iterator = defaultRequest.properties.primitiveKeyIterator(); iterator.hasNext();) {
			String name = iterator.next();
			if (!containsParam(name)) {
				set(name, defaultRequest.get(name));
			}
		}
	}

	public OGCRequestContext getContextData() {
		return contextData;
	}

	public void setContextData(OGCRequestContext contextData) {
		this.contextData = contextData;
	}
	

	public MimeType getFormat() {
		return WMSUtil.getMimeType(this, PARAM_FORMAT);
    }

	public void setFormat(MimeType format) {
		WMSUtil.setMimeType(this, PARAM_FORMAT, format);
	}
}
