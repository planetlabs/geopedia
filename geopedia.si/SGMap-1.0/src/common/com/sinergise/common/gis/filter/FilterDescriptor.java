/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public interface FilterDescriptor extends ExpressionDescriptor {

	/**
	 * @return Mask for operations performed in this filter expression.
	 */
	public int getOperationsMask();
	
}
