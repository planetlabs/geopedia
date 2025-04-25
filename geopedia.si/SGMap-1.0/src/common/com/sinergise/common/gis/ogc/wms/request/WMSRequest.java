/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.OGCRequestContext;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.state.gwt.StateGWT;


public class WMSRequest extends OGCRequest {
	
	private static final long serialVersionUID = 1L;
	
	private static final String SERVICE_WMS = "WMS";
	
	/**
	 * The format in which exceptions are to be reported by the WMS
	 * (default=XML).
	 */
	public static final String PARAM_EXCEPTIONS = "EXCEPTIONS";
	
	/**
	 * Update sequence value (used when concurrency is important).
	 */
	public static final String PARAM_UPDATESEQUENCE = "UPDATESEQUENCE";
	
	public static final String EXCEPTIONS_BLANK = "BLANK";
	public static final String EXCEPTIONS_IN_IMAGE = "INIMAGE";
	public static final String EXCEPTIONS_XML = "XML";

	/**
	 * @deprecated serialization only
	 */
	@Deprecated
	public WMSRequest() {
	}

	public WMSRequest(String type) {
		this(new StateGWT());
		set(PARAM_REQUEST, type);
		set(PARAM_SERVICE, SERVICE_WMS);
	}

	public WMSRequest(StateGWT props) {
		super(props);
	}
	
	public WMSRequest(StateGWT props, OGCRequestContext context) {
		super(props, context);
	}
	

	public String getExceptions() {
		String ret = get(PARAM_EXCEPTIONS, EXCEPTIONS_XML);
		if (ret == null)
			return null;
		return ret.toUpperCase();
	}

	public void setExceptions(String value) {
		set(PARAM_EXCEPTIONS, value);
	}

	protected void validateBBox(String paramName, boolean allowEmpty, boolean allowPoint, boolean allowLine) throws OGCException {
		try {
			Envelope box = WMSUtil.fromWMSBBox(get(paramName));
			if (box.isEmpty() && !allowEmpty) {
				throw new OGCException(this, "Empty " + paramName + " value:'"
						+ get(paramName) + "'");
			}
			if (box.isPoint() && !allowPoint) {
				throw new OGCException(this, "Zero-size " + paramName
						+ " value:'" + get(paramName) + "'");
			}
			if (box.isLine() && !allowLine) {
				throw new OGCException(this, "Zero-size " + paramName + " value:'" + get(paramName) + "'");
			}
		} catch (Exception e) {
			throw new OGCException(this, "Illegal " + paramName + " value:'"
					+ get(paramName) + "'");
		}
	}
}
