package com.sinergise.common.util;


import java.util.Arrays;

import com.sinergise.common.util.lang.AsyncChain;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.lang.SGCallable;

public class ExecutionUtil {
	public static final <R> R executeOnErrorReturnNull(final SGCallable<R> toRun) {
		try {
			return toRun.call();
		} catch (final Throwable e) {
			return null;
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final void executeAsyncChain(Object param, Iterable<? extends AsyncFunction<?, ?>> stages, SGAsyncCallback<?> callback) {
		new AsyncChain(stages).executeAsync(param, callback);
	}
	
	@SuppressWarnings({"unchecked"})
	public static final <A, B, C> void executeAsyncChain(A param, AsyncFunction<A, B> stage1, AsyncFunction<? super B, C> stage2, SGAsyncCallback<? super C> callback) {
		executeAsyncChain(param, Arrays.asList(stage1, stage2), callback);
	}
}
