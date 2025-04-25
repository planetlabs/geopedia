/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public class ComparisonOperation implements FilterDescriptor {

	private static final long serialVersionUID = 1L;

	private ElementDescriptor left;
	private ElementDescriptor right;
	private ElementDescriptor middle;
	private int operation;

	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public ComparisonOperation() {}

	/**
	 * Constructor for unary comparison operations.
	 * 
	 * @param elem Comparable Literal or Property to compare against.
	 * @param operation Unary comparison operation mask.
	 * @throws InvalidFilterDescriptorException when creating invalid operation
	 */
	public ComparisonOperation(ElementDescriptor elem, int operation) throws InvalidFilterDescriptorException {
		this(elem, null, null, operation);
	}

	/**
	 * Constructor for binary comparison operations.
	 * 
	 * @param left comparable Literal or Property to compare.
	 * @param operation Binary comparison operation mask.
	 * @param right comparable Literal or Property to compare.
	 * @throws InvalidFilterDescriptorException when creating invalid operation
	 */
	public ComparisonOperation(ElementDescriptor left, int operation, ElementDescriptor right)
		throws InvalidFilterDescriptorException {
		this(left, null, right, operation);
	}

	/**
	 * Constructor for ternary comparison operations.
	 * 
	 * @param left comparable Literal or Property to compare.
	 * @param middle comparable Literal or Property to compare.
	 * @param right comparable Literal or Property to compare.
	 * @param operation Ternary comparison operation mask.
	 * @throws InvalidFilterDescriptorException when creating invalid operation
	 */
	public ComparisonOperation(ElementDescriptor left, ElementDescriptor middle, ElementDescriptor right, int operation)
		throws InvalidFilterDescriptorException {
		this.left = left;
		this.right = right;
		this.middle = middle;
		this.operation = operation;
		validate();
	}

	public boolean isUnaryOperation() {
		return !isBinaryOperation() && !isTernaryOperation() && (left != null || right != null || middle != null);
	}

	public boolean isBinaryOperation() {
		return !isTernaryOperation()
			&& ((left != null && right != null) || (middle != null && left != null) || (middle != null && right != null));
	}

	public boolean isTernaryOperation() {
		return (left != null && right != null && middle != null);
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
		if (left == null && right == null && middle == null) {
			throw new InvalidFilterDescriptorException("No element descriptors to compare.");
		}
		if ((operation & FilterCapabilities.COMPARISON_OPS) == 0) {
			throw new InvalidFilterDescriptorException("Not a comparison operation.");
		}
		if (isUnaryOperation() && (operation & FilterCapabilities.COMPARISON_UNARY_OPS) == 0) {
			throw new InvalidFilterDescriptorException("Invalid unary comparison operation.");
		}
		if (isBinaryOperation() && (operation & FilterCapabilities.COMPARISON_BINARY_OPS) == 0) {
			throw new InvalidFilterDescriptorException("Invalid binary comparison operation.");
		}
		if (isTernaryOperation() && (operation & FilterCapabilities.COMPARISON_TERNARY_OPS) == 0) {
			throw new InvalidFilterDescriptorException("Invalid ternary comparison operation.");
		}
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.FilterDescriptor#getOperationsMask()
	 */
	@Override
	public int getOperationsMask() {
		return operation;
	}

	public int getOperation() {
		return operation;
	}

	public ElementDescriptor getLeft() {
		return left;
	}

	public ElementDescriptor getRight() {
		return right;
	}

	public ElementDescriptor getMiddle() {
		return middle;
	}

	public static ComparisonOperation createBetween(ElementDescriptor l, ElementDescriptor min, ElementDescriptor max) {
		return new ComparisonOperation(l, min, max, FilterCapabilities.SCALAR_OP_COMP_BETWEEN);
	}

	public static ComparisonOperation createEqual(ElementDescriptor l, ElementDescriptor r) {
		return new ComparisonOperation(l, FilterCapabilities.SCALAR_OP_COMP_EQUALTO, r);
	}
}
