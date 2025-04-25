package com.sinergise.gwt.gis.query.filter;

import java.util.Map;

public interface LayerFilterListener {

	void layerFilterSet(String layerName, Map<String, String> filterValues);
	
}
