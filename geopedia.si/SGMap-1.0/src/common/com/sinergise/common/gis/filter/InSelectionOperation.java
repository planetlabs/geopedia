package com.sinergise.common.gis.filter;

import com.sinergise.common.util.ArrayUtil;

public class InSelectionOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	private ElementDescriptor element;
	private ElementDescriptor[] selection;
	
	
	/** @deprecated Serialization only */
	@Deprecated
	protected InSelectionOperation() { }
	
	public InSelectionOperation (ElementDescriptor element, ElementDescriptor ...selection) {
		this.element = element;
		this.selection = selection;
	}
	
	public ElementDescriptor getElement() {
		return element;
	}
	
	public ElementDescriptor[] getSelection() {
		return selection;
	}

	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if (element == null) {
			throw new InvalidFilterDescriptorException("Invalid SelectionOperation: element is null");
		} else if (ArrayUtil.isNullOrEmpty(selection)) {
			throw new InvalidFilterDescriptorException("Invalid SelectionOperation: no selection");
		}
	}

	@Override
	public int getOperationsMask() {
		return FilterCapabilities.SCALAR_OP_COMP_EQUALTO;
	}

}
