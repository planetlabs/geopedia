package com.sinergise.common.util.event.status;

/**
 * Utility StatusListener that just ignores any status updates.<br>
 * Intended to be used when StatusListener is required but we are not interested at any status updates.
 * 
 * @author tcerovski
 */
public class DummyStatusListener implements StatusListener {
	
	@Override
	public void setErrorStatus(final String error) {
	// ignore
	}
	
	@Override
	public void setInfoStatus(final String status) {
	// ignore
	}
	
	@Override
	public void clearStatus() {
	// nothing to clear
	}
	
}
