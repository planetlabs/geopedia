package com.sinergise.geopedia.core.query.filter;

import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptorVisitor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;

public class FieldDescriptor implements ElementDescriptor {

	
	private Field field;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2218873398366862716L;
	
	@Deprecated
	protected FieldDescriptor() {		
	}
	
	private FieldDescriptor(Field field) {
		this.field = field.clone(DataScope.MEDIUM);
		validate();
	}

	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		((GeopediaExpressionDescriptorVisitor)visitor).visit(this);
	}

	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if (field==null || !field.hasValidId()) //TODO: more checks
			throw new InvalidFilterDescriptorException("Null or empty filter field");		
	}

	
	public Field getField() {
		return field;
	}
	public static FieldDescriptor newInstance(Field field) {
		try {
			return new FieldDescriptor(field);
		} catch(InvalidFilterDescriptorException e) {
			e.printStackTrace();
			return null;
		}
	}
}
