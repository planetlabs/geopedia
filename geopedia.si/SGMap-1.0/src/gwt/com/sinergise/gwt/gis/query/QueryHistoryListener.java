package com.sinergise.gwt.gis.query;

import java.util.Map;

/**
 * @author tcerovski
 *
 */
public interface QueryHistoryListener {

	void executeQuery(String featureType, Map<String, String> valuesMap);
	
}
