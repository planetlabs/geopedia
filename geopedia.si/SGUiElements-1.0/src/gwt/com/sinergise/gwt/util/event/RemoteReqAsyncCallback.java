package com.sinergise.gwt.util.event;

import com.sinergise.common.util.lang.SGAsyncCallback;

public abstract class RemoteReqAsyncCallback<T> implements SGAsyncCallback<T> {

	final RemoteReqEvent rrEvent;
	
	public RemoteReqAsyncCallback() {
		this(new RemoteReqEvent());
	}
	
	public RemoteReqAsyncCallback(RemoteReqEvent rrEvent) {
		this.rrEvent = rrEvent;
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
	}
	
	public abstract void onRequestSuccess(T result);
	
	public abstract void onRequestFailure(Throwable caught);
	
	@Override
	public final void onSuccess(T result) {
		try {
			onRequestSuccess(result);
		} finally {
			remoteRequestFinished();
		}
	}
	
	@Override
	public final void onFailure(Throwable caught) {
		try {
			onRequestFailure(caught);
		} finally {
			remoteRequestFinished();
		}
	}
	
	private void remoteRequestFinished() {
		RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
	}
	
}
