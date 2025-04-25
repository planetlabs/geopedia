package com.sinergise.common.web.i18n;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.util.lang.Pair;

public class GetDictionaryResponse implements Serializable {

	private static final long serialVersionUID = -5023920982847902242L;
	
	private String dictionaryName;
	private Map<String, String> dictionaryMap;
	
	@Deprecated /** Serialization only */
	protected GetDictionaryResponse() { }
	
	public GetDictionaryResponse(String dictionaryName, Map<String, String> dictionaryMap) {
		this.dictionaryMap = dictionaryMap;
		this.dictionaryName = dictionaryName;
	}
	
	public GetDictionaryResponse(String dictionaryName, List<Pair<String, String>> dictionaryValues) {
		this.dictionaryMap = new HashMap<String, String>(dictionaryValues.size());
		for (Pair<String, String> pair : dictionaryValues) {
			dictionaryMap.put(pair.getFirst(), pair.getSecond());
		}
		this.dictionaryName = dictionaryName;
	}
	
	public Map<String, String> getDictionaryMap() {
		if (dictionaryMap == null) return null;
		return Collections.unmodifiableMap(dictionaryMap);
	}
	
	public String getDictionaryName() {
		return dictionaryName;
	}
	
}
