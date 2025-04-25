/**
 * 
 */
package com.sinergise.common.gis.filter;

import java.io.Serializable;

/**
 * @author tcerovski
 */
public interface ExpressionDescriptor extends Serializable {

	void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException;
	
	void validate() throws InvalidFilterDescriptorException;
	
}
