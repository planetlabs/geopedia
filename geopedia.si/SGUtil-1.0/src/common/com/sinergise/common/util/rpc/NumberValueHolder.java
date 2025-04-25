package com.sinergise.common.util.rpc;

import com.sinergise.common.util.rpc.ValueHolder.AValueHolder;

public class NumberValueHolder extends AValueHolder<String> {

	private static final long serialVersionUID = 1L;
	
	protected String numberType;
	
	@Deprecated /** Serialization only */
	protected NumberValueHolder(){}
	
	public NumberValueHolder(String value, String type) {
		super(value);
		numberType = type;
	}
		

}
