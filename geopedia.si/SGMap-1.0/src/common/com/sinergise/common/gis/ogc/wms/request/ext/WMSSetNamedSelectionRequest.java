package com.sinergise.common.gis.ogc.wms.request.ext;

import com.sinergise.common.gis.map.model.ext.FeatureSelectionInfo;
import com.sinergise.common.gis.ogc.wms.request.WMSRequest;

/**
 * @author tcerovski
 *
 */
public class WMSSetNamedSelectionRequest extends WMSRequest {
	
	private static final long serialVersionUID = 1L;

	public static final String PARAM_SELECTION_NAME = "SELECTION_NAME";
	
	public static final String REQ_GET_SET_NAMED_SELECTION = "SetNamedSelection";
	
	private FeatureSelectionInfo selection;
	
	/** Serialization only */
	@Deprecated
	protected WMSSetNamedSelectionRequest() { }
	
	public WMSSetNamedSelectionRequest(String selectionName, FeatureSelectionInfo selection) {
		super(REQ_GET_SET_NAMED_SELECTION);
		set(PARAM_SELECTION_NAME, selectionName);
		this.selection = selection;
	}
	
	public String getSelectionName() {
		return get(PARAM_SELECTION_NAME);
	}
	
	public FeatureSelectionInfo getSelection() {
		return selection;
	}
	
}
