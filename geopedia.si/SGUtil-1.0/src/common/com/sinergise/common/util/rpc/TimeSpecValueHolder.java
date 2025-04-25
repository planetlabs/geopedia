package com.sinergise.common.util.rpc;

import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.rpc.ValueHolder.AValueHolder;

public class TimeSpecValueHolder extends AValueHolder<TimeSpec> {
	
	private static final long serialVersionUID = 1L;

	@Deprecated /** Serialization only */
	protected TimeSpecValueHolder(){}
	
	public TimeSpecValueHolder(TimeSpec value){
		super(value);
	}

}
