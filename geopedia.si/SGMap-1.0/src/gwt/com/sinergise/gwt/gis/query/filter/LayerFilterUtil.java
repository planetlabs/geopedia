package com.sinergise.gwt.gis.query.filter;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.util.string.StringUtil;

public final class LayerFilterUtil {
	
	private LayerFilterUtil() {
		//hide constructor
	}

	private static final String FIELD_SEPARATOR = "@";
	private static final String PAIR_SEPARATOR = ":";
	
	public static Map<String, String> toLayerFilterValuesMap(String filterString) {
		if (StringUtil.isNullOrEmpty(filterString)) return null;
		
		Map<String, String> valuesMap = new HashMap<String, String>();
		for (String pair : filterString.split(FIELD_SEPARATOR)) {
			String[] keyValue = pair.split(PAIR_SEPARATOR);
			if (keyValue.length == 2) valuesMap.put(keyValue[0], keyValue[1]);
		}
		
		return valuesMap;
	}
	
	public static String toLayerFilterString(Map<String, String> valuesMap) {
		StringBuffer sb = new StringBuffer();
		int cnt = 0;
		for (String key : valuesMap.keySet()) {
			if (isNullOrEmpty(valuesMap.get(key))) continue;
			if (cnt++ > 0) sb.append(FIELD_SEPARATOR);
			sb.append(key).append(PAIR_SEPARATOR).append(valuesMap.get(key));
		}
		
		return sb.toString();
	}
}
