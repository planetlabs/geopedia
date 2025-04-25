package com.sinergise.common.util.lang;

import java.util.Arrays;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus;
import com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackWrapper;

public class AsyncCallablesSyncExecutor {
	
	private HashMap<SGAsyncCallable<?>, AsyncCallbackWrapper<?>> callablesMap;
	
	private AsyncCallback<Void> onCompleteCb = null;
	private boolean executing = false;
	
	public AsyncCallablesSyncExecutor(SGAsyncCallable<?> ...callables) {
		this(Arrays.asList(callables));
	}
	
	public AsyncCallablesSyncExecutor(Iterable<SGAsyncCallable<?>> callables) {
		callablesMap = new HashMap<SGAsyncCallable<?>, SGAsyncCallback.AsyncCallbackWrapper<?>>();
		
		SGAsyncCallback<Object> syncCb = new SGAsyncCallback<Object>() {
			@Override
			public void onSuccess(Object result) {
				onCallbackSuccess();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				onCallbackFailure(caught);
			}
		};
		
		for (SGAsyncCallable<?> c : callables) {
			callablesMap.put(c, new AsyncCallbackWrapper<Object>(syncCb));
		}
	}
	
	void onCallbackSuccess() {
		if (isComplete()) {
			onComplete();
		}
	}
	
	void onCallbackFailure(Throwable e) {
		executing = false;
		onCompleteCb.onFailure(e);
	}
	
	private boolean isComplete() {
		if (!executing) return false;
		
		for (AsyncCallbackWrapper<?> cb : callablesMap.values()) {
			if (cb.getStatus() == AsyncCallbackStatus.WAITING) {
				return false;
			}
		}
		return true;
	}
	
	private void onComplete() {
		executing = false;
		onCompleteCb.onSuccess(null);
	}
	
	public AsyncCallbackStatus getCallableStatus(SGAsyncCallable<?> c) {
		return callablesMap.get(c).getStatus();
	}
	
	@SuppressWarnings("unchecked")
	public <R> R getCallableResult(SGAsyncCallable<R> c) {
		return (R)callablesMap.get(c).getResult();
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void execute(AsyncCallback<Void> callback) {
		if (executing) {
			throw new IllegalStateException("Already executing");
		}
		
		this.onCompleteCb = callback;
		executing = true;
		
		for (SGAsyncCallable<?> c : callablesMap.keySet()) {
			try {
				c.call((SGAsyncCallback)callablesMap.get(c));
			} catch(Throwable e) {
				executing = false;
				callback.onFailure(e);
			}
		}
	}

}
