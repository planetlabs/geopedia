package com.sinergise.common.util.rpc;

import java.util.Date;

import com.sinergise.common.util.rpc.ValueHolder.AValueHolder;

public class DateValueHolder extends AValueHolder<Date> {
	
	private static final long serialVersionUID = 1L;

	@Deprecated /** Serialization only */
	protected DateValueHolder() { }
	
	public DateValueHolder(Date value) {
		super(value);
	}

}
