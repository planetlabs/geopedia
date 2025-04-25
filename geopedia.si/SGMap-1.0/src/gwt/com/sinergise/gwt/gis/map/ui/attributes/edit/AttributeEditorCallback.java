package com.sinergise.gwt.gis.map.ui.attributes.edit;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.util.lang.SGAsyncCallback;

public interface AttributeEditorCallback {

	void onConfirm(CFeature modified, SGAsyncCallback<Void> confirmCallback);
	void onCancel();
	
}
