/**
 * 
 */
package com.sinergise.common.gis.filter;

import static com.sinergise.common.util.ArrayUtil.isNullOrEmpty;

import java.util.Collection;


/**
 * @author tcerovski
 */
public class LogicalOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;

	private ExpressionDescriptor[] expressions;
	private int operation;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public LogicalOperation() { }
	
	public LogicalOperation(ExpressionDescriptor[] expressions, int operation) throws InvalidFilterDescriptorException {
		this.expressions = expressions;
		this.operation = operation;
		
		validate();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#accept(com.sinergise.gis.client.filter.ExpressionDescriptorVisitor)
	 */
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#validate()
	 */
	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if(expressions == null || expressions.length == 0)
			throw new InvalidFilterDescriptorException("No filter expressions to evaluate.");
		if((operation & FilterCapabilities.LOGICAL_OPS) == 0)
			throw new InvalidFilterDescriptorException("Not a logical operation.");
		if(expressions.length == 1 && (operation & FilterCapabilities.LOGICAL_UNARY_OPS) == 0)
			throw new InvalidFilterDescriptorException("Invalid unary logical operation.");
		if(expressions.length > 1 && (operation & FilterCapabilities.LOGICAL_BINARY_OPS) == 0)
			throw new InvalidFilterDescriptorException("Invalid binary logical operation.");
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.FilterDescriptor#getOperationsMask()
	 */
	@Override
	public int getOperationsMask() {
		//return mask for this operation and all child operations
		int mask = operation;
		for(int i=0; i<expressions.length; i++) {
			if(expressions[i] instanceof FilterDescriptor)
				mask |= ((FilterDescriptor)expressions[i]).getOperationsMask();
		}
		return mask;
	}

	public ExpressionDescriptor[] getExpressions() {
		return expressions;
	}

	public int getOperation() {
		return operation;
	}

	
	public static FilterDescriptor aggregateExpressions(int aggregatorOp, Collection<FilterDescriptor> expressions) {
		return aggregateExpressions(aggregatorOp, expressions.toArray(new FilterDescriptor[expressions.size()]));
	}
	
	public static FilterDescriptor aggregateExpressions(int aggregatorOp, FilterDescriptor ...expressions) {
		if (isNullOrEmpty(expressions)) {
			return new NoOperation();
		}
		
		if (expressions.length == 1) {
			return expressions[0];
		}
		
		return new LogicalOperation(expressions, aggregatorOp);
	}
	
}
