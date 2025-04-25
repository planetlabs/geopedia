package com.sinergise.common.util;

import com.sinergise.common.util.lang.SGAsyncCallback;

public abstract class SimpleExecutor<T> implements SGAsyncCallback<T> {
	public static abstract class WithCallback<S> extends SimpleExecutor<S> {
		private final SGAsyncCallback<S> cb;
		
		public WithCallback(final SGAsyncCallback<S> cb) {
			this.cb = cb;
		}
		
		@Override
		protected void processFailure(final Throwable arg0) {
			cb.onFailure(arg0);
		}
		
		@Override
		protected void processSuccess(final S result) {
			cb.onSuccess(result);
		}
	}
	
	private boolean started  = false;
	private boolean finished = false;
	
	@Override
	public final void onFailure(final Throwable caught) {
		finished = true;
		processFailure(caught);
	}
	
	@Override
	public final void onSuccess(final T result) {
		finished = true;
		processSuccess(result);
	}
	
	public final void execute() {
		try {
			started = true;
			finished = false;
			internalInvoke();
		} catch(final Throwable t) {
			if (!finished) {
				finished = true;
				processFailure(t);
			}
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean hasBeenStarted() {
		return started;
	}
	
	public void reset() {
		if (started != finished) {
			throw new IllegalStateException("Cannot reset while waiting for result");
		}
		started = false;
		finished = false;
	}
	
	protected abstract void internalInvoke() throws Exception;
	
	protected abstract void processFailure(Throwable caught);
	
	protected abstract void processSuccess(T result);
	
}
