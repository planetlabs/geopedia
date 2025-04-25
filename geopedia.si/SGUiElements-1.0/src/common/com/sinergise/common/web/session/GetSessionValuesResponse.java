package com.sinergise.common.web.session;

import java.io.Serializable;
import java.util.Map;

import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeMap;

public class GetSessionValuesResponse implements Serializable {

	private static final long serialVersionUID = -2344745683206155754L;
	
	private DefaultTypeSafeMap<SessionVariableValue> valuesMap;
	
	@Deprecated /** Serialization only */
	protected GetSessionValuesResponse() {}
	
	public GetSessionValuesResponse(Map<DefaultTypeSafeKey<? extends SessionVariableValue>, SessionVariableValue> valuesMap) {
		this.valuesMap = new DefaultTypeSafeMap<SessionVariableValue>(valuesMap);
	}
	
	public DefaultTypeSafeMap<SessionVariableValue> getValuesMap() {
		return valuesMap;
	}
	
	public <T extends SessionVariableValue> T getValue(SessionVariable<T> key) {
		return valuesMap.getSafe(key);
	}
	
	public boolean isEmpty() {
		return valuesMap == null || valuesMap.isEmpty();
	}
	
}
