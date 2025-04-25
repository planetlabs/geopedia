/**
 * 
 */
package com.sinergise.common.gis.filter;

/**
 * @author tcerovski
 */
public class PropertyName implements ElementDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public PropertyName() { }
	
	public PropertyName(String name) throws InvalidFilterDescriptorException {
		this.name = name;
		validate();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ElementDescriptor#isValid()
	 */
	public boolean isValid() {
		//TODO: validate against feature descriptor
		return name != null && name.trim().length()>0;
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#validate()
	 */
	@Override
	public void validate() throws InvalidFilterDescriptorException {
		if(name == null || name.trim().length() == 0)
			throw new InvalidFilterDescriptorException("Null or empty filter property name");
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.ExpressionDescriptor#accept(com.sinergise.gis.client.filter.ExpressionDescriptorVisitor)
	 */
	@Override
	public void accept(ExpressionDescriptorVisitor visitor) throws InvalidFilterDescriptorException {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Creates new instance of ProperyName element and suppresses InvalidFilterDescriptorException.
	 * If InvalidFilterDescriptorException is thrown, <code>null</code> is returned.
	 */
	public static PropertyName newInstance(String name) {
		try {
			return new PropertyName(name);
		} catch(InvalidFilterDescriptorException e) {
			e.printStackTrace();
			return null;
		}
	}

}
