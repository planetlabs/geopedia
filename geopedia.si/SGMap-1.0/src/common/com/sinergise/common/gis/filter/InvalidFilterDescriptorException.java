/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public class InvalidFilterDescriptorException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated serialization only
	 */
	@Deprecated
	public InvalidFilterDescriptorException() {
	}
	public InvalidFilterDescriptorException(String msg) {
		super(msg);
	}
}
