package com.sinergise.gwt.gis.map.shapes.editor;

import java.util.Collection;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.util.lang.SGAsyncCallback;

public interface GeometryEditorCallback {

	void onConfirm(Collection<CFeature> modified, SGAsyncCallback<Void> confirmCallback);
	void onCancel();
	
}
