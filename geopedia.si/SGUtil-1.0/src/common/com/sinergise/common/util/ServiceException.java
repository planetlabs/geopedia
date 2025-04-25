/**
 * 
 */
package com.sinergise.common.util;

/**
 * GWT serializable exception.
 * 
 * @author tcerovski
 */
public class ServiceException extends Exception {
	
	private static final long serialVersionUID = 610467976388947512L;
	
	public ServiceException() {
		super();
	}
	
	public ServiceException(final String message) {
		super(message);
	}
	
	public ServiceException(final Throwable cause) {
		super(cause);
	}
	
	public ServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
