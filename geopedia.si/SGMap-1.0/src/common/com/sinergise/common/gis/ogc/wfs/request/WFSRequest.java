package com.sinergise.common.gis.ogc.wfs.request;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.web.MimeType;


public class WFSRequest extends OGCRequest {
	private static final String SERVICE_WFS = "WFS";
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Handle associated with the request.
	 */
	public static final String PARAM_HANDLE = "HANDLE";
	/**
	 * The targeted service.
	 */
	public static final String PARAM_NAMESPACE = "NAMESPACE";
	/**
	 * The format of the returned FeatureDescriptor[].
	 */
	public static final String PARAM_OUTPUT_FORMAT = "OUTPUTFORMAT";
	
	public static final MimeType MIME_GML_2_2_1 = MimeType.constructMimeType("text/xml; subtype=gml/2.1.2");
	public static final MimeType MIME_GML_3_1_1 = MimeType.constructMimeType("text/xml; subtype=gml/3.1.1");
	
	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public WFSRequest() {
		// TODO Auto-generated constructor stub
	}
	
	protected WFSRequest(String requestType) {
		this(new StateGWT());
		set(PARAM_REQUEST, requestType);
		set(PARAM_SERVICE, SERVICE_WFS);
		set(PARAM_VERSION, "1.1");
	}
	
	public WFSRequest(StateGWT props) {
		super(props);
	}
	
	public String getOutputFormat() {
		return get(PARAM_OUTPUT_FORMAT, null);
	}
	@Override
	public MimeType getFormat() {
		return MimeType.constructMimeType(get(PARAM_OUTPUT_FORMAT, null));
	}
	@Override
	public void setFormat(MimeType formatMime) {
		setFormat(formatMime.createContentTypeString());
	}
	public void setFormat(String format) {
		if ("GML2".equals(format)) format=MIME_GML_2_2_1.createContentTypeString();
		if ("XMLSCHEMA".equals(format)) format=MIME_GML_2_2_1.createContentTypeString();
		set(PARAM_OUTPUT_FORMAT, format);
	}	
	@Override
	public void validate() throws OGCException {
		super.validate();
		validateNotNull(PARAM_SERVICE);
		validateNotNull(PARAM_VERSION);
	}
}
