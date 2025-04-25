package com.sinergise.common.gis.map.print;

import static com.sinergise.common.gis.ogc.wms.request.ext.GiselleWMSParams.PARAM_CONFIG;
import static com.sinergise.common.gis.ogc.wms.request.ext.GiselleWMSParams.PARAM_DPI;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sinergise.common.gis.map.print.TemplateSpec.PaperSize;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSHighlightRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;

public class PrintParams implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String PARAM_PAPER_SIZE = "PAPER_SIZE";
	public static final String PARAM_TEMPLATE = "TEMPLATE";
	public static final String PARAM_PRINT_SCALE = "PRINT_SCALE";

	public static final String PREFIX_CUSTOM_PARAM = "PARAM_";

	public String fileName;
	public boolean asAttachment = false;

	public MimeType format = MimeType.MIME_DOCUMENT_PDF;
	public String template;
	public PaperSize size = null;
	public double DPI = 600;
	public PrintScaleValue scaleSpec = PrintScaleValue.AUTO_FROM_ENVELOPE;
	public Envelope envelope = Envelope.getEmpty();
	public String crsID;

	public String configName = null;
	public WMSSelectionInfo highlight = WMSSelectionInfo.NO_SELECTION;
	public TemplateParamValues customParams = new TemplateParamValues();

	public PrintParams() {}

	public PrintParams(PrintParams other) {
		setFrom(other);
	}

	public void setFrom(PrintParams other) {
		this.fileName = other.fileName;
		this.asAttachment = other.asAttachment;
		
		this.format = other.format;
		this.template = other.template;
		this.size = other.size;
		this.DPI = other.DPI;
		this.scaleSpec = other.scaleSpec;
		this.envelope = other.envelope;
		this.crsID = other.crsID;
		this.highlight = other.highlight;
		this.customParams.clear();
		this.customParams.putAll(other.customParams);
	}

	public static PrintParams createFrom(OGCRequest request) {
		PrintParams ret = new PrintParams();
		ret.crsID = WMSUtil.getCrsCode(request);
		
		String pSizeVal = request.get(PARAM_PAPER_SIZE);
		if (pSizeVal != null) {
			ret.size = PaperSize.valueOf(pSizeVal);
		}
		ret.format = request.getFormat();
		ret.template = request.get(PARAM_TEMPLATE);
		ret.DPI = Double.parseDouble(request.get(PARAM_DPI, String.valueOf(ret.DPI)));
		ret.scaleSpec = PrintScaleValue.fromCanonicalString(request.get(PARAM_PRINT_SCALE));
		ret.envelope = WMSUtil.fromWMSBBox(request.get(WMSMapRequest.PARAM_BBOX));
		ret.highlight = WMSHighlightRequest.getSelectionSpec(request);
		ret.configName = request.get(PARAM_CONFIG);

		for (Iterator<String> it = request.paramNames(); it.hasNext();) {
			String pName = it.next();
			if (pName.startsWith(PREFIX_CUSTOM_PARAM)) {
				ret.customParams.put(TemplateParam.create(pName), request.get(pName));
			}
		}

		return ret;
	}


	public void updateRequest(OGCRequest request, SGAsyncCallback<OGCRequest> callback) {
		if (size != null) {
			request.set(PARAM_PAPER_SIZE, size.name());
		}
		request.set(PARAM_TEMPLATE, template);
		request.set(PARAM_DPI, String.valueOf(DPI));
		request.set(PARAM_PRINT_SCALE, scaleSpec.toCanonicalString());
		if (configName != null) {
			request.set(PARAM_CONFIG, configName);
		}
		request.setFormat(format);


		if (crsID != null) {
			request.set(WMSMapRequest.PARAM_CRS, crsID);
		}
		if (envelope != null) {
			request.set(WMSMapRequest.PARAM_BBOX, WMSUtil.toWMSBBox(envelope));
		}

		if (customParams != null && !customParams.isEmpty()) {
			for (Entry<TemplateParam, String> en : customParams.entrySet()) {
				request.set(en.getKey().getParamName(), en.getValue());
			}
		}
		
		if (asAttachment) {
			request.set(OGCRequest.PARAM_RESP_ATTACHMENT, Boolean.TRUE.toString());
		}
		
		if (highlight != null) {
			highlight.updateRequestHighlight(request, callback);
		} else {
			callback.onSuccess(request);
		}
	}

	public double getCenterX() {
		return envelope.getCenterX();
	}

	public double getCenterY() {
		return envelope.getCenterY();
	}

	public void setCustom(TemplateParam param, String value) {
		customParams.put(param, value);
	}

	public String getCustom(TemplateParam param) {
		return param.getValue(this);
	}

	public WMSSelectionInfo getHighlight() {
		return highlight;
	}

	public String getUrlPath(String baseUrl) {
		if (StringUtil.isNullOrEmpty(fileName)) {
			return baseUrl;
		}
		return baseUrl + "/" + fileName;
	}
}