/**
 * 
 */
package com.sinergise.common.util.event.status;

/**
 * Listens for status updates or error reports.
 * 
 * @author tcerovski
 */
public interface StatusListener {
	
	public void setInfoStatus(String status);
	
	public void setErrorStatus(String error);
	
	public void clearStatus();
	
	
	public static class DummyStatusListener implements StatusListener {
		@Override
		public void clearStatus() { }
		@Override
		public void setInfoStatus(String status) { }
		@Override
		public void setErrorStatus(String error) {}
	}
	
}
