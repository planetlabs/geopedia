package com.sinergise.geopedia.core.query.filter;

import com.sinergise.common.gis.filter.ExpressionDescriptorVisitor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;

public interface GeopediaExpressionDescriptorVisitor extends ExpressionDescriptorVisitor{
	public static final String PROPERTY_NAME_IDENTIFIER="id";
	public static final String PROPERTY_NAME_FULLTEXT="ft";
	
	
	
	void visit(FieldDescriptor fd) throws InvalidFilterDescriptorException;	
	void visit(TableMetaFieldDescriptor pd)  throws InvalidFilterDescriptorException;
	void visit(TableFieldDescriptor descriptor) throws InvalidFilterDescriptorException;
}
