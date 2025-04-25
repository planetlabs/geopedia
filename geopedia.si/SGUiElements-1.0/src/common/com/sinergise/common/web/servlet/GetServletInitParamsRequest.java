package com.sinergise.common.web.servlet;

import static com.sinergise.common.util.ArrayUtil.arraycopy;
import static com.sinergise.common.util.ArrayUtil.isNullOrEmpty;

import java.io.Serializable;

public class GetServletInitParamsRequest implements Serializable {
	
	private static final long serialVersionUID = 3863465091105554181L;
	
	private String[] paramNames;
	
	@Deprecated /** Serzialization only */
	protected GetServletInitParamsRequest() { }
	
	public GetServletInitParamsRequest(String ...paramNames) {
		this.paramNames = arraycopy(paramNames, new String[paramNames.length]);
	}
	
	public String[] getParamsNames() {
		return arraycopy(paramNames, new String[paramNames.length]);
	}
	
	public boolean isEmpty() {
		return isNullOrEmpty(paramNames);
	}

}
