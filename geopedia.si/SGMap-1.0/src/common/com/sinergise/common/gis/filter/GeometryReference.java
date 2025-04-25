package com.sinergise.common.gis.filter;

public class GeometryReference implements ElementDescriptor {
	
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
	public String toString() {
		return "GEOMETRY";
	}
	
}
