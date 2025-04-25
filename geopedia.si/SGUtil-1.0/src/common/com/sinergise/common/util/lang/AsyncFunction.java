package com.sinergise.common.util.lang;


public interface AsyncFunction<P, R> {
	void executeAsync(P param, SGAsyncCallback<? super R> callback) throws Exception;
}
