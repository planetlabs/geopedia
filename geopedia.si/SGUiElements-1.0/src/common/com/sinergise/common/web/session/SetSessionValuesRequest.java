package com.sinergise.common.web.session;

import java.io.Serializable;
import java.util.Map;

import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeMap;

public class SetSessionValuesRequest implements Serializable {

	private static final long serialVersionUID = 1652761494182692873L;
	
	private DefaultTypeSafeMap<SessionVariableValue> valuesMap;
	
	public SetSessionValuesRequest() {
		valuesMap = new DefaultTypeSafeMap<SessionVariableValue>();
	}
	
	public <T extends SessionVariableValue> SetSessionValuesRequest(SessionVariable<T> var, T value) {
		this.valuesMap = new DefaultTypeSafeMap<SessionVariableValue>();
		valuesMap.putSafe(var, value);
	}
	
	public SetSessionValuesRequest(Map<DefaultTypeSafeKey<? extends SessionVariableValue>, SessionVariableValue> valuesMap) {
		this.valuesMap = new DefaultTypeSafeMap<SessionVariableValue>(valuesMap);
	}
	
	public <T extends SessionVariableValue> void add(SessionVariable<T> var, T value) {
		valuesMap.putSafe(var, value);
	}
	
	public DefaultTypeSafeMap<SessionVariableValue> getValuesMap() {
		return valuesMap;
	}
	
	public boolean isEmpty() {
		return valuesMap == null || valuesMap.isEmpty();
	}

	public <T extends SessionVariableValue> T getSafe(SessionVariable<T> var) {
		return valuesMap.getSafe(var);
	}
	
}
