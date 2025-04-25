/**
 * 
 */
package com.sinergise.common.gis.filter;

import com.sinergise.common.util.property.TextProperty;

/**
 * @author tcerovski
 */
public class IdentifierOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	private Literal<?> idValue;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public IdentifierOperation() { }
	
	public IdentifierOperation(Literal<?> idValue) throws InvalidFilterDescriptorException {
		this.idValue = idValue;
		validate();
	}
	
	public IdentifierOperation(String idString) throws InvalidFilterDescriptorException {
		this.idValue = Literal.newInstance(new TextProperty(idString));
		validate();
	}

	@Override
	public int getOperationsMask() {
		return FilterCapabilities.FID_OP;
	}

	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if(idValue == null) {
			throw new InvalidFilterDescriptorException("Null cannot be an identifier value.");
		}
	}

	public Literal<?> getIdValue() {
		return idValue;
	}

}
