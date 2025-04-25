package com.sinergise.geopedia.core.query.filter;

import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptorVisitor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;

public class TableMetaFieldDescriptor implements ElementDescriptor {

	private static final long serialVersionUID = 3907624087775439942L;
	
	public enum MetaFieldType {IDENTIFIER, FULLTEXT, DELETED, USER, TIMESTAMP}
	private int tableId;
	private MetaFieldType metafieldType;
	
	@Deprecated
	protected TableMetaFieldDescriptor() {		
	}
	
	
	public TableMetaFieldDescriptor(MetaFieldType metafieldType, int tableId) {
		this.metafieldType = metafieldType;
		this.tableId=tableId;
		validate();
	}
	
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		((GeopediaExpressionDescriptorVisitor)visitor).visit(this);
		
	}

	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if (tableId<=0)
			throw new InvalidFilterDescriptorException("Invalid tableId");			
	}
	
	public MetaFieldType getMetaFieldType() {
		return metafieldType;
	}
	public int getTableId() {
		return tableId;
	}

}
