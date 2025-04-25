package com.sinergise.common.util.rpc;

import com.sinergise.common.util.rpc.ValueHolder.AValueHolder;

public class StringValueHolder extends AValueHolder<String> {

	private static final long serialVersionUID = 1L;

	@Deprecated /** Serialization only */
	protected StringValueHolder(){}
	
	public StringValueHolder(String value) {
		super(value);
	}
		

}
