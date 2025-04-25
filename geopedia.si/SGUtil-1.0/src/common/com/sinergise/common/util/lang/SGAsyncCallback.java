package com.sinergise.common.util.lang;

import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.FAILED;
import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.SUCCESS;
import static com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackStatus.WAITING;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SGAsyncCallback<T> extends AsyncCallback<T> {
	class Empty<T> implements SGAsyncCallback<T> {
		@Override
		public void onFailure(Throwable caught) {
		}

		@Override
		public void onSuccess(T result) {
		}
		@SuppressWarnings("unchecked")
		public <C, S extends SGAsyncCallback<C>> S cast() {
			return (S)this;
		}
	}
	Empty<Object> EMPTY = new Empty<Object>();

	public static enum AsyncCallbackStatus {WAITING, FAILED, SUCCESS}
	
	/**
	 * Wraps another callback, forwarding calls to onSuccess() and onFailure(),
	 * storing the status and results for future retrieval.
	 * 
	 * @author Miha
	 * @param <T>
	 */
	public static class AsyncCallbackWrapper<T> implements SGAsyncCallback<T> {
		
		public static final <S> AsyncCallbackWrapper<S> wrap(AsyncCallback<S> delegate) {
			return new AsyncCallbackWrapper<S>(delegate);
		}

		private AsyncCallbackStatus status = WAITING;
		private Object data;
		private final AsyncCallback<? super T> delegate;
		
		public AsyncCallbackWrapper(AsyncCallback<? super T> delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public void onFailure(Throwable caught) {
			if (status != WAITING) return;
			status = AsyncCallbackStatus.FAILED;
			this.data = caught;
			delegate.onFailure(caught);
			onComplete();
		}
		
		@Override
		public void onSuccess(T result) {
			if (status != WAITING) return;
			status = AsyncCallbackStatus.SUCCESS;
			this.data = result;
			delegate.onSuccess(result);
			onComplete();
		}
		
		protected void onComplete() {
		}
		
		@SuppressWarnings("unchecked")
		public T getResult() {
			if (status == SUCCESS) return (T)data;
			throw new IllegalStateException("Cannot call getResult on status "+status);
		}

		public Throwable getFailure() {
			if (status == FAILED) return (Throwable)data;
			throw new IllegalStateException("Cannot call getFailure on status "+status);
		}
		
		public AsyncCallbackStatus getStatus() {
			return status;
		}
	}
	
	/**
	 * Wraps an AsyncCallback and allows the result of the operation
	 * to be processed before calling the delegate callback's onSuccess.
	 * Also allows chaining of async calls.
	 * 
	 * The default implementation calls processOriginalResult() when callback is received.
	 * 
	 * To add another async call after the first one is completed, override onSuccess() 
	 * and, when completed, delegate to the callSuccess() function, which will notify the delegating
	 * callback of completion.
	 */
	public static abstract class AsyncCallbackAdapter<T, S> implements SGAsyncCallback<T> {
		protected final AsyncCallback<? super S> delegateCB;

		public AsyncCallbackAdapter(AsyncCallback<? super S> delegate) {
			this.delegateCB = delegate;
		}
		
		@Override
		public void onSuccess(T result) {
			final S myResult;
			try {
				myResult = processOriginalResult(result);
			} catch (Throwable t) {
				delegateCB.onFailure(t);
				return;
			}
			callSuccess(myResult);
		}
		
		@SuppressWarnings("unchecked")
		protected S processOriginalResult(T result) {
			return (S)result;
		}

		protected final void callSuccess(S result) {
			delegateCB.onSuccess(result);
		}
		
		@Override
		public void onFailure(Throwable caught) {
			delegateCB.onFailure(caught);
		}
	}
//	void onFailure(Throwable failure);
//	void onSuccess(T result);
}
