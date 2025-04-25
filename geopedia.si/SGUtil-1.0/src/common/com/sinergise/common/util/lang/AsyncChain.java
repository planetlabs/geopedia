package com.sinergise.common.util.lang;

import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.FAILED;
import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.SUCCESS;
import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.WAITING;

import java.util.Iterator;


@SuppressWarnings({"rawtypes", "unchecked"})
public class AsyncChain<P, R> implements AsyncFunction<P, R> {
	private class StageCallback implements SGAsyncCallback<Object> {
		private AsyncCallbackStatus status = WAITING;
		
		public StageCallback() {
		}

		@Override
		public void onFailure(Throwable caught) {
			if (status != WAITING) {
				throw new IllegalStateException("onFailure called a second time after the async call returned (prev success: "+(status==SUCCESS)+")");
			}
			status = FAILED;
			AsyncChain.this.onFailure(caught);
		}
		
		@Override
		public void onSuccess(Object result) {
			if (status != WAITING) {
				throw new IllegalStateException("onSuccess called a second time after the async call returned (prev success: "+(status==SUCCESS)+")");
			}
			status = SUCCESS;
			AsyncChain.this.onSuccess(result);
		}
	}
	
	private final Iterable<AsyncFunction<?, ?>> stages;

	private SGAsyncCallback<? super R> callback;
	private Iterator it;
	
	public AsyncChain(Iterable<AsyncFunction<?,?>> stages) {
		this.stages = stages;
	}
	
	@Override
	public void executeAsync(P param, SGAsyncCallback<? super R> cb) {
		try {
			this.callback = cb;
			if (it != null) throw new IllegalStateException("Can't execute while executing");
			it = stages.iterator();
			callNext(param);
		} catch (Throwable t) {
			onFailure(t);
		}
	}
	
	private void cleanup() {
		callback = null;
		it = null;
	}

	public void onSuccess(Object result) {
		callNext(result);
	}

	private void callNext(Object result) {
		try {
			if (it.hasNext()) {
				((AsyncFunction)it.next()).executeAsync(result, new StageCallback());
			} else {
				finish((R)result);
				return;
			}
		} catch (Throwable t) {
			onFailure(t);
		}
	}

	public void onFailure(Throwable failure) {
		if (callback == null) {
			NullPointerException npe = new NullPointerException("callback");
			npe.initCause(failure);
			throw npe;
		}
		callback.onFailure(failure);
	}

	private void finish(R result) {
		try {
			callback.onSuccess(result);
			cleanup();
		} catch (Throwable t) {
			onFailure(t);
		}
	}
}