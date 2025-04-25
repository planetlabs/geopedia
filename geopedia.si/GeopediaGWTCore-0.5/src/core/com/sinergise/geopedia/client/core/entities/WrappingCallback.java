package com.sinergise.geopedia.client.core.entities;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class WrappingCallback<T> implements AsyncCallback<T>
{
	private AsyncCallback<T> wrapped;

	public WrappingCallback(AsyncCallback<T> wrapped)
	{
		this.wrapped = wrapped;
	}

	public void onFailure(Throwable caught)
    {
		if (wrapped != null)
			wrapped.onFailure(caught);
    }

	protected abstract void handleResult(T result);
	
	public void onSuccess(T result)
    {
		try {
			handleResult(result);
		} finally {
			if (wrapped != null)
				wrapped.onSuccess(result);
		}
    }
}
