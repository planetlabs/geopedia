package com.sinergise.geopedia.core.query.filter;

import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptorVisitor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;

public class TableFieldDescriptor implements ElementDescriptor {

	private static final long serialVersionUID = 6907978734763520151L;
	private String fieldIdentifier;

	
	public TableFieldDescriptor(String fieldIdentifier) {
		this.fieldIdentifier = fieldIdentifier;
	}
	
	
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		((GeopediaExpressionDescriptorVisitor)visitor).visit(this);
	}

	@Override
	public void validate() throws InvalidFilterDescriptorException {
		//TODO implement validation
	}


	public String getFieldIdentifier() {
		return fieldIdentifier;
	}

	
}
