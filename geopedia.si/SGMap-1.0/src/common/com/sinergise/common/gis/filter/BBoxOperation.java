/**
 * 
 */
package com.sinergise.common.gis.filter;

import com.sinergise.common.util.geom.Envelope;

/**
 * @author tcerovski
 */
public class BBoxOperation implements FilterDescriptor {
	
	private static final long serialVersionUID = 1L;
	
	private Envelope bbox;
	private boolean contains;
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public BBoxOperation() {
		//do nothing
	}
	
	public BBoxOperation(Envelope bbox) throws InvalidFilterDescriptorException {
		this(bbox, false);
	}
	
	public BBoxOperation(Envelope bbox, boolean contains) throws InvalidFilterDescriptorException {
		this.bbox = bbox;
		this.contains = contains;
		
		validate();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.filter.FilterDescriptor#getOperationsMask()
	 */
	@Override
	public int getOperationsMask() {
		return FilterCapabilities.SPATIAL_OP_BBOX;
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
		if(bbox == null)
			throw new InvalidFilterDescriptorException("Invalid BBoxOperation: bbox is null");

	}

	public Envelope getBBox() {
		return bbox;
	}
	
	public boolean isContains() {
		return contains;
	}

}
