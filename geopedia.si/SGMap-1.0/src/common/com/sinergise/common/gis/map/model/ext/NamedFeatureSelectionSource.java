package com.sinergise.common.gis.map.model.ext;

import com.sinergise.common.util.lang.SGAsyncCallback;

public interface NamedFeatureSelectionSource {
	
	void setNamedSelection(String selectionName, FeatureSelectionInfo selectionInfo, SGAsyncCallback<Void> callback);
	
}
