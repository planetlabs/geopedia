package com.sinergise.common.web.servlet;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetServletInitParamsResponse implements Serializable {

	private static final long serialVersionUID = -554935722492998681L;
	
	private Map<String, String> paramsMap;
	
	@Deprecated /** Serialization only */
	protected GetServletInitParamsResponse() { }
	
	public GetServletInitParamsResponse(Map<String, String> paramsMap) {
		this.paramsMap = new HashMap<String, String>(paramsMap);
	}
	
	public Map<String, String> getParamsMap() {
		return Collections.unmodifiableMap(paramsMap);
	}
	
	public String getInitParam(String key) {
		return paramsMap.get(key);
	}
	
	public boolean hasInitParam(String key) {
		return paramsMap.containsKey(key);
	}
}
