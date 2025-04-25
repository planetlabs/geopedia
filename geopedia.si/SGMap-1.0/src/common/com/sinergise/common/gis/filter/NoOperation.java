/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public class NoOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}
	
	@Override
	public void validate() {
		//nothing to validate
	}
	
	@Override
	public int getOperationsMask() {
		return FilterCapabilities.NO_OP;
	}
}
