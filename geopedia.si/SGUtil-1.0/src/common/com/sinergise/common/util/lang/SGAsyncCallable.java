package com.sinergise.common.util.lang;

public interface SGAsyncCallable<R> {

	void call(SGAsyncCallback<? super R> callback);
	
}
