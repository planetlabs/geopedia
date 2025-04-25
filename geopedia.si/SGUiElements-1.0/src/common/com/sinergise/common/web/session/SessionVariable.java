package com.sinergise.common.web.session;

import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;

public class SessionVariable<T extends SessionVariableValue> extends DefaultTypeSafeKey<T> {

	private static final long serialVersionUID = -9064678444118981777L;
	
	public static final <V  extends SessionVariableValue> SessionVariable<V> createVar(String name) {
		return new SessionVariable<V>(name);
	}
	
	@Deprecated /** Serialization only */
	protected SessionVariable(){ }
	
	public SessionVariable(String name) {
		super(name);
	}
	
}
